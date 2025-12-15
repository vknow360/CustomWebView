package com.sunny.CustomWebView;

import android.net.Uri;
import java.util.Arrays;

/**
 * High-Performance AdBlock Engine using Structure-of-Arrays (SoA) layout.
 * 
 * DESIGN:
 * - Minimizes Object allocation during matching (Zero-GC hot path).
 * - Uses primitive arrays (int[], long[], byte[]) instead of List<Object>.
 * - Bitmask-based resource type checking.
 * - Open-Addressing Hash Map for O(1) domain lookups.
 */
public class FlatFilterEngine {

    private static final int INITIAL_CAPACITY = 16384;
    private static final int HASH_MAP_SIZE = 131072; // Must be power of 2
    private static final int HASH_MASK = HASH_MAP_SIZE - 1;

    // --- SOA RAA STORAGE ---
    private int count = 0;
    private byte[] ruleTypes; // 0: BLOCK, 1: EXCEPTION
    private long[] optionsMask; // Resource types & flags
    private int[] domainHashes; // Hash of the domain (0 if generic)
    private String[] patterns; // Text pattern (e.g. "/ads/")
    private boolean[] isRegex; // True if pattern is a regex

    // --- DOMAIN INDEX (Open Addressing / Chaining) ---
    // domainMapHead[hash & mask] -> first index in rule arrays
    // nextRuleIndex[index] -> next index with same hash
    private int[] domainMapHead;
    private int[] nextRuleIndex;

    // --- GENERIC RULES ---
    private int[] genericRuleIndices;
    private int genericCount = 0;

    // --- BITMASKS ---
    public static final long TYPE_SCRIPT = 1L;
    public static final long TYPE_IMAGE = 1L << 1;
    public static final long TYPE_STYLESHEET = 1L << 2;
    public static final long TYPE_OBJECT = 1L << 3;
    public static final long TYPE_XMLHTTP = 1L << 4;
    public static final long TYPE_SUBDOCUMENT = 1L << 5;
    public static final long TYPE_MEDIA = 1L << 6;
    public static final long TYPE_FONT = 1L << 7;
    public static final long TYPE_POPUP = 1L << 8;
    public static final long TYPE_WEBSOCKET = 1L << 9;
    public static final long TYPE_OTHER = 1L << 10;
    public static final long TYPE_DOCUMENT = 1L << 11;

    public static final long FLAG_THIRD_PARTY = 1L << 60;
    public static final long FLAG_MATCH_CASE = 1L << 61;

    // Mask for all types (excluding flags)
    public static final long MASK_ALL_TYPES = TYPE_SCRIPT | TYPE_IMAGE | TYPE_STYLESHEET |
            TYPE_OBJECT | TYPE_XMLHTTP | TYPE_SUBDOCUMENT |
            TYPE_MEDIA | TYPE_FONT | TYPE_POPUP |
            TYPE_WEBSOCKET | TYPE_OTHER | TYPE_DOCUMENT;

    public FlatFilterEngine() {
        resize(INITIAL_CAPACITY);
        domainMapHead = new int[HASH_MAP_SIZE];
        Arrays.fill(domainMapHead, -1);
        genericRuleIndices = new int[INITIAL_CAPACITY / 4];
    }

    /**
     * Add a rule to the engine.
     */
    public synchronized void addRule(String pattern, int type, long mask, String domain) {
        if (count >= ruleTypes.length) {
            resize(count * 2);
        }

        int idx = count;
        ruleTypes[idx] = (byte) type;
        optionsMask[idx] = mask;
        patterns[idx] = pattern;

        isRegex[idx] = pattern.length() > 2 && pattern.startsWith("/") && pattern.endsWith("/");

        if (domain != null && !domain.isEmpty()) {
            int dHash = domain.hashCode();
            domainHashes[idx] = dHash;

            int mapIdx = dHash & HASH_MASK;
            nextRuleIndex[idx] = domainMapHead[mapIdx];
            domainMapHead[mapIdx] = idx;
        } else {
            domainHashes[idx] = 0;
            if (genericCount >= genericRuleIndices.length) {
                genericRuleIndices = Arrays.copyOf(genericRuleIndices, genericRuleIndices.length * 2);
            }
            genericRuleIndices[genericCount++] = idx;
        }

        count++;
    }

    private void resize(int newSize) {
        ruleTypes = Arrays.copyOf(ruleTypes == null ? new byte[0] : ruleTypes, newSize);
        optionsMask = Arrays.copyOf(optionsMask == null ? new long[0] : optionsMask, newSize);
        domainHashes = Arrays.copyOf(domainHashes == null ? new int[0] : domainHashes, newSize);
        patterns = Arrays.copyOf(patterns == null ? new String[0] : patterns, newSize);
        isRegex = Arrays.copyOf(isRegex == null ? new boolean[0] : isRegex, newSize);
        nextRuleIndex = Arrays.copyOf(nextRuleIndex == null ? new int[0] : nextRuleIndex, newSize);
    }

    public boolean shouldBlock(String url, String pageDomain, long resourceType) {
        if (url == null || url.isEmpty())
            return false;

        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        boolean isThirdParty = isThirdParty(host, pageDomain);
        if (isThirdParty) {
            resourceType |= FLAG_THIRD_PARTY;
        }

        // 1. CHECK DOMAIN SPECIFIC RULES (O(1) lookup)
        if (host != null) {
            String currentDomain = host;
            while (true) {
                int dHash = currentDomain.hashCode();
                int idx = domainMapHead[dHash & HASH_MASK];

                while (idx != -1) {
                    if (domainHashes[idx] == dHash) {
                        if (checkMatch(idx, url, resourceType)) {
                            // Exception (1) -> ALLOW, Block (0) -> BLOCK
                            if (ruleTypes[idx] == 1)
                                return false;
                            return true;
                        }
                    }
                    idx = nextRuleIndex[idx];
                }

                int dotIdx = currentDomain.indexOf('.');
                if (dotIdx == -1)
                    break;
                currentDomain = currentDomain.substring(dotIdx + 1);
            }
        }

        // 2. CHECK GENERIC RULES
        for (int i = 0; i < genericCount; i++) {
            int idx = genericRuleIndices[i];
            if (checkMatch(idx, url, resourceType)) {
                if (ruleTypes[idx] == 1)
                    return false;
                return true;
            }
        }

        return false;
    }

    private boolean checkMatch(int idx, String url, long contextMask) {
        long ruleMask = optionsMask[idx];

        if ((ruleMask & FLAG_THIRD_PARTY) != 0) {
            if ((contextMask & FLAG_THIRD_PARTY) == 0)
                return false;
        }

        long typeMask = ruleMask & 0x0FFFFFFFFFFFFFFFL;
        if (typeMask != 0) {
            if ((typeMask & contextMask) == 0)
                return false;
        }

        String pattern = patterns[idx];
        if (pattern.isEmpty())
            return true;

        if (isRegex[idx]) {
            try {
                // Regex fallback (slow path)
                return url.matches(pattern.substring(1, pattern.length() - 1));
            } catch (Exception e) {
                return false;
            }
        }

        if (pattern.indexOf('*') != -1) {
            return fastWildcardMatch(url, pattern);
        }

        return url.contains(pattern);
    }

    // Fast Wildcard Matcher
    private boolean fastWildcardMatch(String text, String pattern) {
        int tIdx = 0, pIdx = 0, starIdx = -1, matchIdx = 0;
        int tLen = text.length();
        int pLen = pattern.length();

        while (tIdx < tLen) {
            if (pIdx < pLen && (pattern.charAt(pIdx) == '?' || pattern.charAt(pIdx) == text.charAt(tIdx))) {
                tIdx++;
                pIdx++;
            } else if (pIdx < pLen && pattern.charAt(pIdx) == '*') {
                starIdx = pIdx;
                matchIdx = tIdx;
                pIdx++;
            } else if (starIdx != -1) {
                pIdx = starIdx + 1;
                matchIdx++;
                tIdx = matchIdx;
            } else {
                return false;
            }
        }

        while (pIdx < pLen && pattern.charAt(pIdx) == '*') {
            pIdx++;
        }

        return pIdx == pLen;
    }

    private boolean isThirdParty(String host, String pageDomain) {
        if (pageDomain == null || host == null)
            return false;
        if (host.equals(pageDomain))
            return false;

        String hostBase = getBaseDomain(host);
        String pageBase = getBaseDomain(pageDomain);

        return !hostBase.equals(pageBase);
    }

    private String getBaseDomain(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot == -1)
            return domain;
        int prevDot = domain.lastIndexOf('.', lastDot - 1);
        if (prevDot == -1)
            return domain;
        return domain.substring(prevDot + 1);
    }

    public synchronized void clear() {
        count = 0;
        genericCount = 0;
        Arrays.fill(domainMapHead, -1);
    }

    public int getRuleCount() {
        return count;
    }
}
