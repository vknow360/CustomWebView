package com.sunny.cwvutils;

import android.content.Context;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced AdBlocker using High-Performance FlatFilterEngine.
 */
public class AdBlocker {

    private static AdBlocker instance;
    private static final AtomicBoolean enabled = new AtomicBoolean(false);

    private final FlatFilterEngine filterEngine;
    private FilterListLoader filterListLoader;

    // Fast domain-indexed cosmetic rules
    private final Map<String, List<String>> domainCosmeticMap = new ConcurrentHashMap<>();
    private final List<String> genericCosmeticList = new CopyOnWriteArrayList<>();

    // Whitelist and live request metrics
    private final Set<String> whitelist = Collections.synchronizedSet(new HashSet<String>());
    private final AtomicInteger blockedRequests = new AtomicInteger(0);
    private final AtomicInteger allowedRequests = new AtomicInteger(0);

    private AdBlocker() {
        this.filterEngine = new FlatFilterEngine();
    }

    public static synchronized AdBlocker getInstance() {
        if (instance == null) {
            instance = new AdBlocker();
        }
        return instance;
    }

    public static void init(Context context) {
        getInstance().filterListLoader = new FilterListLoader(context);
    }

    public static void init(String hosts) {
        if (hosts == null || hosts.isEmpty())
            return;
        String[] rules = hosts.split(",");
        for (String rule : rules) {
            String trimmed = rule.trim();
            if (!trimmed.isEmpty()) {
                getInstance().filterEngine.addRule("^", 0, 0, trimmed);
            }
        }
    }

    public static void initAsync(final String hosts) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                init(hosts);
            }
        }).start();
    }

    public static void loadFilterListFromAsset(String assetPath, final FilterListLoader.LoadCallback callback) {
        final AdBlocker adBlocker = getInstance();
        if (adBlocker.filterListLoader == null) {
            if (callback != null)
                callback.onError("AdBlocker not initialized", assetPath);
            return;
        }

        adBlocker.filterListLoader.loadFromAsset(assetPath, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                adBlocker.addRules(rules);
                if (callback != null)
                    callback.onSuccess(rules, source);
            }

            @Override
            public void onError(String error, String source) {
                if (callback != null)
                    callback.onError(error, source);
            }

            @Override
            public void onProgress(int loaded, String source) {
                if (callback != null)
                    callback.onProgress(loaded, source);
            }
        });
    }

    public static void loadFilterListFromUrl(String url, final FilterListLoader.LoadCallback callback) {
        final AdBlocker adBlocker = getInstance();
        if (adBlocker.filterListLoader == null) {
            if (callback != null)
                callback.onError("AdBlocker not initialized", url);
            return;
        }

        adBlocker.filterListLoader.loadFromUrl(url, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                adBlocker.addRules(rules);
                if (callback != null)
                    callback.onSuccess(rules, source);
            }

            @Override
            public void onError(String error, String source) {
                if (callback != null)
                    callback.onError(error, source);
            }

            @Override
            public void onProgress(int loaded, String source) {
                if (callback != null)
                    callback.onProgress(loaded, source);
            }
        });
    }

    public static void loadFilterListFromFile(String filePath, final FilterListLoader.LoadCallback callback) {
        final AdBlocker adBlocker = getInstance();
        if (adBlocker.filterListLoader == null) {
            if (callback != null)
                callback.onError("AdBlocker not initialized", filePath);
            return;
        }

        adBlocker.filterListLoader.loadFromFile(filePath, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                adBlocker.addRules(rules);
                if (callback != null)
                    callback.onSuccess(rules, source);
            }

            @Override
            public void onError(String error, String source) {
                if (callback != null)
                    callback.onError(error, source);
            }

            @Override
            public void onProgress(int loaded, String source) {
                if (callback != null)
                    callback.onProgress(loaded, source);
            }
        });
    }

    public static void loadDefaultFilters() {
        List<String> defaults = FilterListLoader.createDefaultRules();
        getInstance().addRules(defaults);
    }

    private void addRules(List<String> rules) {
        for (String rule : rules) {
            parseAndAddRule(rule);
        }
    }

    private void parseAndAddRule(String rule) {
        rule = rule.trim();
        if (rule.isEmpty() || rule.startsWith("!") || rule.startsWith("["))
            return;

        if (rule.contains("##") || rule.contains("#?#") || rule.contains("#@#") || rule.contains("#$#")) {
            addCosmeticRule(rule);
            return;
        }

        int type = 0; // Block
        if (rule.startsWith("@@")) {
            type = 1; // Exception
            rule = rule.substring(2).trim();
        }

        String domain = null;
        long mask = 0;
        List<String> incList = null;
        List<String> excList = null;

        int dollars = rule.lastIndexOf('$');
        if (dollars != -1) {
            String options = rule.substring(dollars + 1);
            rule = rule.substring(0, dollars).trim();

            String[] opts = options.split(",");
            boolean hasTypeOpt = false;
            for (String o : opts) {
                o = o.trim();
                if (o.startsWith("domain=")) {
                    String[] dparts = o.substring(7).split("\\|");
                    for (String dp : dparts) {
                        dp = dp.trim();
                        if (dp.startsWith("~")) {
                            if (excList == null) excList = new ArrayList<>(2);
                            excList.add(dp.substring(1).toLowerCase());
                        } else if (!dp.isEmpty()) {
                            if (incList == null) incList = new ArrayList<>(2);
                            incList.add(dp.toLowerCase());
                        }
                    }
                } else if (o.equals("script")) {
                    mask |= FlatFilterEngine.TYPE_SCRIPT;
                    hasTypeOpt = true;
                } else if (o.equals("image")) {
                    mask |= FlatFilterEngine.TYPE_IMAGE;
                    hasTypeOpt = true;
                } else if (o.equals("stylesheet")) {
                    mask |= FlatFilterEngine.TYPE_STYLESHEET;
                    hasTypeOpt = true;
                } else if (o.equals("xmlhttprequest") || o.equals("xhr")) {
                    mask |= FlatFilterEngine.TYPE_XMLHTTP;
                    hasTypeOpt = true;
                } else if (o.equals("subdocument")) {
                    mask |= FlatFilterEngine.TYPE_SUBDOCUMENT;
                    hasTypeOpt = true;
                } else if (o.equals("document")) {
                    mask |= FlatFilterEngine.TYPE_DOCUMENT;
                    hasTypeOpt = true;
                } else if (o.equals("media")) {
                    mask |= FlatFilterEngine.TYPE_MEDIA;
                    hasTypeOpt = true;
                } else if (o.equals("font")) {
                    mask |= FlatFilterEngine.TYPE_FONT;
                    hasTypeOpt = true;
                } else if (o.equals("popup")) {
                    mask |= FlatFilterEngine.TYPE_POPUP;
                    hasTypeOpt = true;
                } else if (o.equals("websocket")) {
                    mask |= FlatFilterEngine.TYPE_WEBSOCKET;
                    hasTypeOpt = true;
                } else if (o.equals("other")) {
                    mask |= FlatFilterEngine.TYPE_OTHER;
                    hasTypeOpt = true;
                } else if (o.equals("third-party") || o.equals("3p")) {
                    mask |= FlatFilterEngine.FLAG_THIRD_PARTY;
                } else if (o.equals("~third-party") || o.equals("~3p") || o.equals("first-party") || o.equals("1p")) {
                    mask |= FlatFilterEngine.FLAG_FIRST_PARTY;
                } else if (o.equals("match-case")) {
                    mask |= FlatFilterEngine.FLAG_MATCH_CASE;
                } else if (o.equals("~script")) {
                    mask |= (FlatFilterEngine.MASK_ALL_TYPES & ~FlatFilterEngine.TYPE_SCRIPT);
                    hasTypeOpt = true;
                } else if (o.equals("~image")) {
                    mask |= (FlatFilterEngine.MASK_ALL_TYPES & ~FlatFilterEngine.TYPE_IMAGE);
                    hasTypeOpt = true;
                } else if (o.equals("~stylesheet")) {
                    mask |= (FlatFilterEngine.MASK_ALL_TYPES & ~FlatFilterEngine.TYPE_STYLESHEET);
                    hasTypeOpt = true;
                } else if (o.equals("~document")) {
                    mask |= (FlatFilterEngine.MASK_ALL_TYPES & ~FlatFilterEngine.TYPE_DOCUMENT);
                    hasTypeOpt = true;
                }
            }

            if (!hasTypeOpt) {
                if (type == 0) {
                    mask |= (FlatFilterEngine.MASK_ALL_TYPES & ~FlatFilterEngine.TYPE_DOCUMENT);
                } else {
                    mask |= FlatFilterEngine.MASK_ALL_TYPES;
                }
            }
        } else {
            // No options specified
            if (type == 0) {
                mask = FlatFilterEngine.MASK_ALL_TYPES & ~FlatFilterEngine.TYPE_DOCUMENT;
            } else {
                mask = FlatFilterEngine.MASK_ALL_TYPES;
            }
        }

        // CRITICAL: Skip rules with empty patterns after stripping options
        if (rule.isEmpty()) {
            return;
        }

        String pattern = rule;
        if (rule.startsWith("||")) {
            String rem = rule.substring(2);
            int sep = -1;
            for (int i = 0; i < rem.length(); i++) {
                char c = rem.charAt(i);
                if (c == '^' || c == '/' || c == '*' || c == ':' || c == '?') {
                    sep = i;
                    break;
                }
            }
            if (sep != -1) {
                domain = rem.substring(0, sep).toLowerCase();
                pattern = rem.substring(sep);
            } else {
                domain = rem.toLowerCase();
                pattern = "^";
            }
        }

        String[] incArr = (incList != null && !incList.isEmpty()) ? incList.toArray(new String[0]) : null;
        String[] excArr = (excList != null && !excList.isEmpty()) ? excList.toArray(new String[0]) : null;

        filterEngine.addRule(pattern, type, mask, domain, incArr, excArr);
    }

    private void addCosmeticRule(String rule) {
        int sepIndex = rule.indexOf("##");
        if (sepIndex == -1) {
            sepIndex = rule.indexOf("#?#");
        }
        if (sepIndex == -1) {
            return;
        }

        String domainPart = "";
        if (sepIndex > 0) {
            domainPart = rule.substring(0, sepIndex).trim();
        }
        String selectorPart = rule.substring(sepIndex + 2).trim();
        if (selectorPart.isEmpty()) {
            return;
        }

        if (domainPart.isEmpty()) {
            genericCosmeticList.add(selectorPart);
        } else {
            String[] domains = domainPart.split(",");
            for (String d : domains) {
                d = d.trim().toLowerCase();
                if (!d.isEmpty()) {
                    List<String> list = domainCosmeticMap.get(d);
                    if (list == null) {
                        list = new CopyOnWriteArrayList<>();
                        domainCosmeticMap.put(d, list);
                    }
                    list.add(selectorPart);
                }
            }
        }
        cachedGenericCosmeticCss = null;
    }

    public static boolean isAd(String url) {
        return isAd(url, null, FlatFilterEngine.TYPE_OTHER);
    }

    public static boolean isAd(String url, String pageUrl, long resourceType) {
        if (!enabled.get() || url == null)
            return false;
        if (isWhitelisted(url) || (pageUrl != null && isWhitelisted(pageUrl)))
            return false;
        return getInstance().filterEngine.shouldBlock(url, pageUrl, resourceType);
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    public static WebResourceResponse createEmptyResource(String mime) {
        return new WebResourceResponse(mime, "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    public static WebResourceResponse createEmptyResourceForUrl(String url) {
        String mime = "text/plain";
        if (url != null) {
            String lower = url.toLowerCase();
            if (lower.contains(".js")) {
                mime = "application/javascript";
            } else if (lower.contains(".css")) {
                mime = "text/css";
            } else if (lower.contains(".png") || lower.contains(".jpg") || lower.contains(".gif") || lower.contains(".webp")) {
                mime = "image/png";
            }
        }
        Map<String, String> headers = Collections.singletonMap("Access-Control-Allow-Origin", "*");
        return new WebResourceResponse(mime, "utf-8", 403, "Blocked by AdBlocker", headers, new ByteArrayInputStream(new byte[0]));
    }

    public static void enable(boolean status) {
        enabled.set(status);
    }

    public static boolean isEnabled() {
        return enabled.get();
    }

    /**
     * Injects CSS element-hiding selectors for the current page in microseconds.
     */
    public static void injectCosmeticHelper(WebView view) {
        if (!enabled.get())
            return;

        String url = view.getUrl();
        if (url == null || url.isEmpty())
            return;

        int proto = url.indexOf("://");
        int hostStart = (proto != -1) ? proto + 3 : 0;
        int hostEnd = url.length();
        for (int i = hostStart; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == '/' || c == '?' || c == ':' || c == '#') {
                hostEnd = i;
                break;
            }
        }
        if (hostEnd <= hostStart)
            return;

        String host = url.substring(hostStart, hostEnd).toLowerCase();

        StringBuilder css = new StringBuilder();
        int ruleCount = 0;

        AdBlocker blocker = getInstance();

        // O(1) domain lookup with parent domain walk
        String curr = host;
        while (!curr.isEmpty()) {
            List<String> selectors = blocker.domainCosmeticMap.get(curr);
            if (selectors != null) {
                for (String sel : selectors) {
                    if (ruleCount > 0)
                        css.append(",");
                    css.append(sel);
                    ruleCount++;
                }
            }
            int dot = curr.indexOf('.');
            if (dot == -1) break;
            curr = curr.substring(dot + 1);
        }

        // Also inject generic cosmetic rules
        String genericCss = blocker.getCachedGenericCosmeticCss();
        if (genericCss != null && !genericCss.isEmpty()) {
            if (ruleCount > 0)
                css.append(",");
            css.append(genericCss);
            ruleCount++;
        }

        if (ruleCount > 0) {
            String escapedCss = css.toString().replace("'", "\\'");
            String js = "(function() {" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    "style.innerHTML = '" + escapedCss + " { display: none !important; }';" +
                    "document.head.appendChild(style);" +
                    "})();";
            view.evaluateJavascript(js, null);
        }
    }

    private volatile String cachedGenericCosmeticCss = null;

    private String getCachedGenericCosmeticCss() {
        if (cachedGenericCosmeticCss != null) {
            return cachedGenericCosmeticCss;
        }
        if (genericCosmeticList.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String sel : genericCosmeticList) {
            if (count > 0) sb.append(",");
            sb.append(sel);
            count++;
        }
        cachedGenericCosmeticCss = sb.toString();
        return cachedGenericCosmeticCss;
    }

    public static void recordBlocked() {
        getInstance().blockedRequests.incrementAndGet();
    }

    public static void recordAllowed() {
        getInstance().allowedRequests.incrementAndGet();
    }

    public static int getBlockedHostsCount() {
        return getInstance().filterEngine.getRuleCount();
    }

    public static int getBlockedRequestsCount() {
        return getInstance().blockedRequests.get();
    }

    public static int getAllowedRequestsCount() {
        return getInstance().allowedRequests.get();
    }

    public static int getWhitelistCount() {
        return getInstance().whitelist.size();
    }

    public static void addToWhitelist(String h) {
        if (h != null && !h.trim().isEmpty()) {
            String host = h.trim().toLowerCase();
            getInstance().whitelist.add(host);
            getInstance().filterEngine.addRule("^", 1, FlatFilterEngine.MASK_ALL_TYPES, host);
        }
    }

    public static void removeFromWhitelist(String h) {
        if (h != null && !h.trim().isEmpty()) {
            getInstance().whitelist.remove(h.trim().toLowerCase());
        }
    }

    public static boolean isWhitelisted(String u) {
        if (u == null || u.isEmpty())
            return false;
        AdBlocker blocker = getInstance();
        if (blocker.whitelist.isEmpty())
            return false;

        int proto = u.indexOf("://");
        int hostStart = (proto != -1) ? proto + 3 : 0;
        int hostEnd = u.length();
        for (int i = hostStart; i < u.length(); i++) {
            char c = u.charAt(i);
            if (c == '/' || c == '?' || c == ':' || c == '#') {
                hostEnd = i;
                break;
            }
        }
        String host = (hostEnd > hostStart) ? u.substring(hostStart, hostEnd).toLowerCase() : "";
        while (!host.isEmpty()) {
            if (blocker.whitelist.contains(host))
                return true;
            int dot = host.indexOf('.');
            if (dot == -1) break;
            host = host.substring(dot + 1);
        }
        return false;
    }

    public static void clearCaches() {
    }

    public static String getCacheStats() {
        return "Flat Engine Active";
    }

    public static String getStats() {
        AdBlocker b = getInstance();
        return "Rules: " + b.filterEngine.getRuleCount()
                + " | Blocked: " + b.blockedRequests.get()
                + " | Allowed: " + b.allowedRequests.get()
                + " | Whitelisted: " + b.whitelist.size();
    }

    public static void clear() {
        AdBlocker blocker = getInstance();
        blocker.filterEngine.clear();
        blocker.domainCosmeticMap.clear();
        blocker.genericCosmeticList.clear();
        blocker.cachedGenericCosmeticCss = null;
        blocker.whitelist.clear();
        blocker.blockedRequests.set(0);
        blocker.allowedRequests.set(0);
    }

    public static void clearRules() {
        AdBlocker blocker = getInstance();
        blocker.filterEngine.clear();
        blocker.domainCosmeticMap.clear();
        blocker.genericCosmeticList.clear();
        blocker.cachedGenericCosmeticCss = null;
        blocker.blockedRequests.set(0);
        blocker.allowedRequests.set(0);
    }

    public static void addFilterRules(String s) {
        if (s == null || s.isEmpty()) return;
        getInstance().addRules(Arrays.asList(s.split("\n")));
    }

    public static void addFilterRules(String[] s) {
        if (s == null) return;
        getInstance().addRules(Arrays.asList(s));
    }

    public static void addFilterRule(String s) {
        if (s == null) return;
        getInstance().parseAndAddRule(s);
    }
}
