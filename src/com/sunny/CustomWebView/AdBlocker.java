package com.sunny.CustomWebView;

import android.content.Context;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.net.Uri;

/**
 * Advanced AdBlocker using High-Performance Flat Engine.
 */
public class AdBlocker {

    private static AdBlocker instance;
    private static final AtomicBoolean enabled = new AtomicBoolean(false);

    private final FlatFilterEngine filterEngine;
    private FilterListLoader filterListLoader;
    private List<String> cosmeticRules;

    private AdBlocker() {
        this.filterEngine = new FlatFilterEngine();
        this.cosmeticRules = new ArrayList<>();
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
            if (!rule.isEmpty()) {
                getInstance().filterEngine.addRule("||" + rule.trim() + "^", 0, 0, rule.trim());
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
        if (rule.isEmpty() || rule.startsWith("!"))
            return;

        if (rule.contains("##")) {
            cosmeticRules.add(rule);
            return;
        }

        int type = 0; // Block
        if (rule.startsWith("@@")) {
            type = 1; // Exception
            rule = rule.substring(2);
        }

        String domain = null;
        long mask = 0;

        int dollars = rule.lastIndexOf('$');
        if (dollars != -1) {
            String options = rule.substring(dollars + 1);
            rule = rule.substring(0, dollars);
            mask = parseOptions(options);
        } else {
            // No options specified
            if (type == 0) { // Blocking rule
                // Block everything EXCEPT main document by default (standard AdBlock behavior)
                // This prevents blocking the page itself unless $document is strictly specified
                mask = FlatFilterEngine.MASK_ALL_TYPES & ~FlatFilterEngine.TYPE_DOCUMENT;
            } else { // Exception rule (@@)
                // Exception applies to everything including document
                mask = FlatFilterEngine.MASK_ALL_TYPES;
            }
        }

        if (rule.startsWith("||")) {
            int end = rule.indexOf('^');
            if (end == -1)
                end = rule.indexOf('/');
            if (end != -1) {
                domain = rule.substring(2, end);
            } else {
                domain = rule.substring(2);
            }
        }

        filterEngine.addRule(rule, type, mask, domain);
    }

    private long parseOptions(String options) {
        long mask = 0;
        String[] opts = options.split(",");
        for (String o : opts) {
            if (o.equals("script"))
                mask |= FlatFilterEngine.TYPE_SCRIPT;
            else if (o.equals("image"))
                mask |= FlatFilterEngine.TYPE_IMAGE;
            else if (o.equals("stylesheet"))
                mask |= FlatFilterEngine.TYPE_STYLESHEET;
            else if (o.equals("document"))
                mask |= FlatFilterEngine.TYPE_DOCUMENT;
            else if (o.equals("third-party"))
                mask |= FlatFilterEngine.FLAG_THIRD_PARTY;
        }
        return mask;
    }

    public static boolean isAd(String url) {
        return isAd(url, null, FlatFilterEngine.TYPE_OTHER);
    }

    public static boolean isAd(String url, String pageUrl, long resourceType) {
        if (!enabled.get() || url == null)
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
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

    public static void enable(boolean status) {
        enabled.set(status);
    }

    public static boolean isEnabled() {
        return enabled.get();
    }

    public static void injectCosmeticHelper(WebView view) {
        String url = view.getUrl();
        if (url == null)
            return;

        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (host == null)
            return;

        StringBuilder css = new StringBuilder();
        int ruleCount = 0;

        AdBlocker blocker = getInstance();
        // Simple O(N) scan - optimized in future iterations if needed
        for (String rule : blocker.cosmeticRules) {
            String domainPart = "";
            String selectorPart;

            int sepIndex = rule.indexOf("##");
            if (sepIndex == -1)
                continue;

            if (sepIndex > 0) {
                // Has domain constraint: example.com##...
                domainPart = rule.substring(0, sepIndex);
            }
            selectorPart = rule.substring(sepIndex + 2);

            if (domainPart.isEmpty() || host.endsWith(domainPart)) {
                if (ruleCount > 0)
                    css.append(",");
                css.append(selectorPart);
                ruleCount++;
            }
        }

        if (ruleCount > 0) {
            String js = "(function() {" +
                    "var style = document.createElement('style');" +
                    "style.innerHTML = '" + css.toString() + " { display: none !important; }';" +
                    "document.head.appendChild(style);" +
                    "})();";
            view.evaluateJavascript(js, null);
        }
    }

    public static int getBlockedHostsCount() {
        return getInstance().filterEngine.getRuleCount();
    }

    public static int getBlockedRequestsCount() {
        return 0;
    }

    public static int getAllowedRequestsCount() {
        return 0;
    }

    public static int getWhitelistCount() {
        return 0;
    }

    public static void addToWhitelist(String h) {
    }

    public static void removeFromWhitelist(String h) {
    }

    public static boolean isWhitelisted(String u) {
        return false;
    }

    public static void clearCaches() {
    }

    public static String getCacheStats() {
        return "Flat Engine Active";
    }

    public static String getStats() {
        return "Rules: " + getInstance().filterEngine.getRuleCount();
    }

    public static void clear() {
        getInstance().filterEngine.clear();
    }

    public static void clearRules() {
        getInstance().filterEngine.clear();
    }

    public static void addFilterRules(String s) {

        getInstance().addRules(Arrays.asList(s.split("\n")));
    }

    public static void addFilterRules(String[] s) {
        getInstance().addRules(Arrays.asList(s));
    }

    public static void addFilterRule(String s) {
        getInstance().parseAndAddRule(s);
    }
}
