package com.sunny.CustomWebView;

import android.net.Uri;
import android.util.LruCache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Optimized filter engine using tokenization and bloom filters
 * for fast URL matching against AdBlock Plus / uBlock Origin rules
 */
public class FilterEngine {
    
    // Cache for matching results (mobile-optimized size)
    private static final int CACHE_SIZE = 4096;
    private final LruCache<String, MatchResult> matchCache;
    
    // Token-based index for fast lookup
    private final Map<String, List<FilterRule>> tokenIndex;
    
    // Rules organized by type for priority processing
    private final List<FilterRule> exceptionRules;
    private final List<FilterRule> importantRules;
    private final List<FilterRule> normalRules;
    private final List<FilterRule> regexRules;
    private final List<FilterRule> domainAnchoredRules;
    
    // Simple bloom filter for quick rejection (space-efficient)
    private final BloomFilter bloomFilter;
    
    // Statistics
    private final AtomicInteger totalRules;
    private final AtomicInteger blockedRequests;
    private final AtomicInteger allowedRequests;
    
    // Thread-safe whitelist
    private final Set<String> whitelist;
    
    public FilterEngine() {
        this.matchCache = new LruCache<>(CACHE_SIZE);
        this.tokenIndex = new ConcurrentHashMap<>();
        this.exceptionRules = new ArrayList<>();
        this.importantRules = new ArrayList<>();
        this.normalRules = new ArrayList<>();
        this.regexRules = new ArrayList<>();
        this.domainAnchoredRules = new ArrayList<>();
        this.bloomFilter = new BloomFilter(50000); // ~50K rules capacity
        this.totalRules = new AtomicInteger(0);
        this.blockedRequests = new AtomicInteger(0);
        this.allowedRequests = new AtomicInteger(0);
        this.whitelist = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }
    
    /**
     * Result of a match operation
     */
    public static class MatchResult {
        public final boolean shouldBlock;
        public final FilterRule matchedRule;
        
        public MatchResult(boolean shouldBlock, FilterRule matchedRule) {
            this.shouldBlock = shouldBlock;
            this.matchedRule = matchedRule;
        }
        
        private static final MatchResult ALLOW = new MatchResult(false, null);
        private static final MatchResult BLOCK = new MatchResult(true, null);
        
        public static MatchResult allow() {
            return ALLOW;
        }
        
        public static MatchResult block() {
            return BLOCK;
        }
        
        public static MatchResult block(FilterRule rule) {
            return new MatchResult(true, rule);
        }
        
        public static MatchResult allow(FilterRule rule) {
            return new MatchResult(false, rule);
        }
    }
    
    /**
     * Add a filter rule to the engine
     */
    public void addRule(FilterRule rule) {
        if (rule == null || rule.getRuleType() == FilterRule.RuleType.COMMENT) {
            return;
        }
        
        // Organize rules by type for efficient processing
        if (rule.isException()) {
            exceptionRules.add(rule);
        } else if (rule.isImportant()) {
            importantRules.add(rule);
        } else if (rule.getPattern() != null && rule.getPattern().contains("/")) {
            // Regex or complex patterns
            regexRules.add(rule);
        } else {
            normalRules.add(rule);
        }
        
        // Index by tokens for fast lookup
        String[] tokens = rule.getTokens();
        if (tokens != null && tokens.length > 0) {
            // Use the most specific token (usually the longest)
            String bestToken = findBestToken(tokens);
            if (bestToken != null) {
                tokenIndex.computeIfAbsent(bestToken, new Function<String, List<FilterRule>>() {
                    @Override
                    public List<FilterRule> apply(String k) {
                        return new ArrayList<>();
                    }
                }).add(rule);
                bloomFilter.add(bestToken);
            }
        }
        
        totalRules.incrementAndGet();
    }
    
    /**
     * Add multiple rules from string array
     */
    public void addRules(String[] rules) {
        if (rules == null) return;
        
        for (String ruleStr : rules) {
            FilterRule rule = FilterRule.parse(ruleStr);
            if (rule != null) {
                addRule(rule);
            }
        }
    }
    
    /**
     * Add rules from list
     */
    public void addRules(List<String> rules) {
        if (rules == null) return;
        
        for (String ruleStr : rules) {
            FilterRule rule = FilterRule.parse(ruleStr);
            if (rule != null) {
                addRule(rule);
            }
        }
    }
    
    /**
     * Check if URL should be blocked
     */
    public MatchResult shouldBlock(String url, String pageUrl, FilterRule.ResourceType resourceType) {
        if (url == null || url.isEmpty()) {
            return MatchResult.allow();
        }
        
        // Check whitelist first
        if (isWhitelisted(url)) {
            allowedRequests.incrementAndGet();
            return MatchResult.allow();
        }
        
        // Check cache
        String cacheKey = url + "|" + (pageUrl != null ? pageUrl : "") + "|" + resourceType;
        MatchResult cached = matchCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // Extract context
        String domain = extractDomain(pageUrl);
        boolean isThirdParty = isThirdPartyRequest(url, pageUrl);
        
        // Quick rejection using bloom filter
        String[] tokens = extractTokens(url);
        boolean mightMatch = false;
        for (String token : tokens) {
            if (bloomFilter.mightContain(token)) {
                mightMatch = true;
                break;
            }
        }
        
        if (!mightMatch) {
            // Very likely no rules match this URL
            MatchResult result = MatchResult.allow();
            matchCache.put(cacheKey, result);
            allowedRequests.incrementAndGet();
            return result;
        }
        
        // Check exception rules first (highest priority)
        for (FilterRule rule : exceptionRules) {
            if (rule.matches(url, domain, resourceType, isThirdParty)) {
                MatchResult result = MatchResult.allow(rule);
                matchCache.put(cacheKey, result);
                allowedRequests.incrementAndGet();
                return result;
            }
        }
        
        // Check important rules (override normal rules)
        for (FilterRule rule : importantRules) {
            if (rule.matches(url, domain, resourceType, isThirdParty)) {
                MatchResult result = MatchResult.block(rule);
                matchCache.put(cacheKey, result);
                blockedRequests.incrementAndGet();
                return result;
            }
        }
        
        // Check token-indexed rules (most efficient)
        for (String token : tokens) {
            List<FilterRule> rules = tokenIndex.get(token);
            if (rules != null) {
                for (FilterRule rule : rules) {
                    if (!rule.isException() && !rule.isImportant() && 
                        rule.matches(url, domain, resourceType, isThirdParty)) {
                        MatchResult result = MatchResult.block(rule);
                        matchCache.put(cacheKey, result);
                        blockedRequests.incrementAndGet();
                        return result;
                    }
                }
            }
        }
        
        // Check regex and complex patterns (slower, last resort)
        for (FilterRule rule : regexRules) {
            if (rule.matches(url, domain, resourceType, isThirdParty)) {
                MatchResult result = MatchResult.block(rule);
                matchCache.put(cacheKey, result);
                blockedRequests.incrementAndGet();
                return result;
            }
        }
        
        // No match found, allow
        MatchResult result = MatchResult.allow();
        matchCache.put(cacheKey, result);
        allowedRequests.incrementAndGet();
        return result;
    }
    
    /**
     * Extract domain from URL
     */
    private String extractDomain(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            Uri uri = Uri.parse(url);
            return uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Check if request is third-party
     */
    private boolean isThirdPartyRequest(String url, String pageUrl) {
        if (url == null || pageUrl == null) {
            return false;
        }
        
        String urlDomain = extractDomain(url);
        String pageDomain = extractDomain(pageUrl);
        
        if (urlDomain == null || pageDomain == null) {
            return false;
        }
        
        // Get base domains (e.g., example.com from sub.example.com)
        String urlBase = getBaseDomain(urlDomain);
        String pageBase = getBaseDomain(pageDomain);
        
        return !urlBase.equals(pageBase);
    }
    
    /**
     * Get base domain (e.g., example.com from sub.example.com)
     */
    private String getBaseDomain(String domain) {
        if (domain == null) return null;
        
        String[] parts = domain.split("\\.");
        if (parts.length >= 2) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        }
        return domain;
    }
    
    /**
     * Extract tokens from URL for fast lookup
     */
    private String[] extractTokens(String url) {
        if (url == null || url.length() < 3) {
            return new String[0];
        }
        
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        String lowerUrl = url.toLowerCase();
        
        for (int i = 0; i < lowerUrl.length(); i++) {
            char c = lowerUrl.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                token.append(c);
            } else {
                if (token.length() >= 3) {
                    tokens.add(token.toString());
                }
                token.setLength(0);
            }
        }
        
        if (token.length() >= 3) {
            tokens.add(token.toString());
        }
        
        return tokens.toArray(new String[0]);
    }
    
    /**
     * Find best token for indexing (most specific/longest)
     */
    private String findBestToken(String[] tokens) {
        if (tokens == null || tokens.length == 0) {
            return null;
        }
        
        String best = null;
        int maxLen = 0;
        
        for (String token : tokens) {
            if (token.length() > maxLen) {
                maxLen = token.length();
                best = token;
            }
        }
        
        return best;
    }
    
    /**
     * Add domain to whitelist
     */
    public void addToWhitelist(String domain) {
        if (domain != null && !domain.isEmpty()) {
            whitelist.add(domain.toLowerCase());
        }
    }
    
    /**
     * Remove domain from whitelist
     */
    public void removeFromWhitelist(String domain) {
        if (domain != null && !domain.isEmpty()) {
            whitelist.remove(domain.toLowerCase());
        }
    }
    
    /**
     * Check if URL is whitelisted
     */
    public boolean isWhitelisted(String url) {
        if (url == null) return false;
        
        String domain = extractDomain(url);
        if (domain == null) return false;
        
        domain = domain.toLowerCase();
        
        // Check exact match and parent domains
        if (whitelist.contains(domain)) {
            return true;
        }
        
        int dotIndex = domain.indexOf('.');
        while (dotIndex > 0 && dotIndex < domain.length() - 1) {
            domain = domain.substring(dotIndex + 1);
            if (whitelist.contains(domain)) {
                return true;
            }
            dotIndex = domain.indexOf('.');
        }
        
        return false;
    }
    
    /**
     * Clear all rules and caches
     */
    public void clear() {
        exceptionRules.clear();
        importantRules.clear();
        normalRules.clear();
        regexRules.clear();
        domainAnchoredRules.clear();
        tokenIndex.clear();
        matchCache.evictAll();
        bloomFilter.clear();
        whitelist.clear();
        totalRules.set(0);
        blockedRequests.set(0);
        allowedRequests.set(0);
    }
    
    /**
     * Clear only cache (keep rules)
     */
    public void clearCache() {
        matchCache.evictAll();
    }
    
    /**
     * Get statistics
     */
    public int getTotalRules() {
        return totalRules.get();
    }
    
    public int getBlockedCount() {
        return blockedRequests.get();
    }
    
    public int getAllowedCount() {
        return allowedRequests.get();
    }
    
    public int getCacheSize() {
        return matchCache.size();
    }
    
    public String getStats() {
        return String.format("Rules: %d, Blocked: %d, Allowed: %d, Cache: %d/%d",
                totalRules.get(), blockedRequests.get(), allowedRequests.get(),
                matchCache.size(), CACHE_SIZE);
    }
    
    /**
     * Simple Bloom Filter implementation for space-efficient set membership testing
     */
    private static class BloomFilter {
        private final BitSet bitSet;
        private final int size;
        private final int hashCount;
        
        public BloomFilter(int expectedElements) {
            // Calculate optimal size and hash count
            this.size = (int) (expectedElements * 10); // 10 bits per element
            this.hashCount = 3; // 3 hash functions
            this.bitSet = new BitSet(size);
        }
        
        public void add(String element) {
            if (element == null) return;
            
            for (int i = 0; i < hashCount; i++) {
                int hash = hash(element, i);
                bitSet.set(Math.abs(hash % size));
            }
        }
        
        public boolean mightContain(String element) {
            if (element == null) return false;
            
            for (int i = 0; i < hashCount; i++) {
                int hash = hash(element, i);
                if (!bitSet.get(Math.abs(hash % size))) {
                    return false;
                }
            }
            return true;
        }
        
        public void clear() {
            bitSet.clear();
        }
        
        private int hash(String element, int seed) {
            int h = seed;
            for (int i = 0; i < element.length(); i++) {
                h = 31 * h + element.charAt(i);
            }
            return h;
        }
    }
}
