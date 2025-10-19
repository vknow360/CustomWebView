package com.sunny.CustomWebView;

import android.content.Context;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Advanced AdBlocker with support for AdBlock Plus / uBlock Origin filter syntax
 * Features:
 * - Token-based matching for fast filtering
 * - Bloom filters for quick rejection
 * - Mobile-optimized with limited memory footprint
 * - Support for filter lists from assets and URLs
 * - Exception rules and important rules
 * - Resource type filtering
 * - Third-party request detection
 */
public class AdBlocker {

    private static AdBlocker instance;
    private static final AtomicBoolean enabled = new AtomicBoolean(false);
    
    // Core filter engine
    private final FilterEngine filterEngine;
    private FilterListLoader filterListLoader;
    
    // Context for loading filter lists
    private Context context;

    private AdBlocker() {
        this.filterEngine = new FilterEngine();
    }

    public static AdBlocker getInstance() {
        synchronized (AdBlocker.class) {
            if (instance == null) {
                instance = new AdBlocker();
            }
        }
        return instance;
    }

    /**
     * Initialize with context (required for loading filter lists from assets)
     */
    public static void init(Context context) {
        getInstance().context = context;
        getInstance().filterListLoader = new FilterListLoader(context);
    }
    
    /**
     * LEGACY: Initialize with comma-separated host list (backward compatible)
     * Converts to new filter format
     */
    public static void init(String hosts) {
        if (hosts == null || hosts.isEmpty()) {
            return;
        }
        
        // Convert legacy format to AdBlock rules
        List<String> rules = FilterListLoader.convertLegacyHosts(hosts);
        getInstance().filterEngine.addRules(rules);
    }
    
    /**
     * LEGACY: Initialize asynchronously (backward compatible)
     */
    public static void initAsync(final String hosts) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                init(hosts);
            }
        }).start();
    }
    
    /**
     * Load filter list from app assets
     */
    public static void loadFilterListFromAsset(String assetPath, final FilterListLoader.LoadCallback callback) {
        final AdBlocker adBlocker = getInstance();
        
        if (adBlocker.filterListLoader == null) {
            if (callback != null) {
                callback.onError("AdBlocker not initialized with context. Call init(Context) first.", assetPath);
            }
            return;
        }
        
        adBlocker.filterListLoader.loadFromAsset(assetPath, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                adBlocker.filterEngine.addRules(rules);
                if (callback != null) {
                    callback.onSuccess(rules, source);
                }
            }
            
            @Override
            public void onError(String error, String source) {
                if (callback != null) {
                    callback.onError(error, source);
                }
            }
            
            @Override
            public void onProgress(int loaded, String source) {
                if (callback != null) {
                    callback.onProgress(loaded, source);
                }
            }
        });
    }
    
    /**
     * Load filter list from external URL
     */
    public static void loadFilterListFromUrl(String url, final FilterListLoader.LoadCallback callback) {
        final AdBlocker adBlocker = getInstance();
        
        if (adBlocker.filterListLoader == null) {
            if (callback != null) {
                callback.onError("AdBlocker not initialized with context. Call init(Context) first.", url);
            }
            return;
        }
        
        adBlocker.filterListLoader.loadFromUrl(url, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                adBlocker.filterEngine.addRules(rules);
                if (callback != null) {
                    callback.onSuccess(rules, source);
                }
            }
            
            @Override
            public void onError(String error, String source) {
                if (callback != null) {
                    callback.onError(error, source);
                }
            }
            
            @Override
            public void onProgress(int loaded, String source) {
                if (callback != null) {
                    callback.onProgress(loaded, source);
                }
            }
        });
    }
    
    /**
     * Load filter list from file
     */
    public static void loadFilterListFromFile(String filePath, final FilterListLoader.LoadCallback callback) {
        final AdBlocker adBlocker = getInstance();
        
        if (adBlocker.filterListLoader == null) {
            if (callback != null) {
                callback.onError("AdBlocker not initialized with context. Call init(Context) first.", filePath);
            }
            return;
        }
        
        adBlocker.filterListLoader.loadFromFile(filePath, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                adBlocker.filterEngine.addRules(rules);
                if (callback != null) {
                    callback.onSuccess(rules, source);
                }
            }
            
            @Override
            public void onError(String error, String source) {
                if (callback != null) {
                    callback.onError(error, source);
                }
            }
            
            @Override
            public void onProgress(int loaded, String source) {
                if (callback != null) {
                    callback.onProgress(loaded, source);
                }
            }
        });
    }
    
    /**
     * Load multiple filter lists
     */
    public static void loadMultipleFilterLists(String[] sources, boolean fromAssets, final FilterListLoader.LoadCallback callback) {
        final AdBlocker adBlocker = getInstance();
        
        if (adBlocker.filterListLoader == null) {
            if (callback != null) {
                callback.onError("AdBlocker not initialized with context. Call init(Context) first.", "multiple");
            }
            return;
        }
        
        adBlocker.filterListLoader.loadMultiple(sources, fromAssets, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                adBlocker.filterEngine.addRules(rules);
                if (callback != null) {
                    callback.onSuccess(rules, source);
                }
            }
            
            @Override
            public void onError(String error, String source) {
                if (callback != null) {
                    callback.onError(error, source);
                }
            }
            
            @Override
            public void onProgress(int loaded, String source) {
                if (callback != null) {
                    callback.onProgress(loaded, source);
                }
            }
        });
    }
    
    /**
     * Load default minimal filter list
     */
    public static void loadDefaultFilters() {
        List<String> defaultRules = FilterListLoader.createDefaultRules();
        getInstance().filterEngine.addRules(defaultRules);
    }
    
    /**
     * Add custom filter rules
     */
    public static void addFilterRules(String[] rules) {
        if (rules != null && rules.length > 0) {
            getInstance().filterEngine.addRules(rules);
        }
    }
    
    /**
     * Add custom filter rules from string
     */
    public static void addFilterRules(String rulesString) {
        List<String> rules = FilterListLoader.parseRulesString(rulesString);
        getInstance().filterEngine.addRules(rules);
    }
    
    /**
     * Add single filter rule
     */
    public static void addFilterRule(String rule) {
        FilterRule filterRule = FilterRule.parse(rule);
        if (filterRule != null) {
            getInstance().filterEngine.addRule(filterRule);
        }
    }
    
    /**
     * Enable/disable ad blocking
     */
    public static void enable(boolean status) {
        enabled.set(status);
    }
    
    /**
     * Check if ad blocking is enabled
     */
    public static boolean isEnabled() {
        return enabled.get();
    }
    
    /**
     * Add domain to whitelist
     */
    public static void addToWhitelist(String host) {
        getInstance().filterEngine.addToWhitelist(host);
    }
    
    /**
     * Remove domain from whitelist
     */
    public static void removeFromWhitelist(String host) {
        getInstance().filterEngine.removeFromWhitelist(host);
    }
    
    /**
     * Check if URL is whitelisted
     */
    public static boolean isWhitelisted(String url) {
        return getInstance().filterEngine.isWhitelisted(url);
    }
    
    /**
     * DEPRECATED: Legacy method for setting regex pattern
     * Use addFilterRule() with regex format instead: /pattern/
     */
    @Deprecated
    public static void setAdPattern(String regex) {
        if (regex != null && !regex.isEmpty()) {
            // Convert to AdBlock regex format
            String rule = "/" + regex + "/";
            addFilterRule(rule);
        }
    }
    
    /**
     * Check if URL should be blocked
     * @param url The URL to check
     * @return true if URL matches a blocking rule
     */
    public static boolean isAd(String url) {
        return isAd(url, null, FilterRule.ResourceType.OTHER);
    }
    
    /**
     * Check if URL should be blocked with context
     * @param url The URL to check
     * @param pageUrl The page URL (for third-party detection)
     * @param resourceType The resource type
     * @return true if URL matches a blocking rule
     */
    public static boolean isAd(String url, String pageUrl, FilterRule.ResourceType resourceType) {
        if (!enabled.get() || url == null || url.isEmpty()) {
            return false;
        }
        
        FilterEngine.MatchResult result = getInstance().filterEngine.shouldBlock(url, pageUrl, resourceType);
        return result.shouldBlock;
    }
    
    /**
     * Get detailed match result
     */
    public static FilterEngine.MatchResult checkUrl(String url, String pageUrl, FilterRule.ResourceType resourceType) {
        if (!enabled.get() || url == null || url.isEmpty()) {
            return FilterEngine.MatchResult.allow();
        }
        
        return getInstance().filterEngine.shouldBlock(url, pageUrl, resourceType);
    }
    
    /**
     * Create empty WebResourceResponse for blocked requests
     */
    public WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
    
    /**
     * Create empty WebResourceResponse with specific MIME type
     */
    public WebResourceResponse createEmptyResource(String mimeType) {
        if (mimeType == null) {
            mimeType = "text/plain";
        }
        return new WebResourceResponse(mimeType, "utf-8", new ByteArrayInputStream("".getBytes()));
    }
    
    /**
     * Create empty WebResourceResponse with MIME type guessed from URL
     */
    public static WebResourceResponse createEmptyResourceForUrl(String url) {
        String mimeType = guessMimeType(url);
        return new WebResourceResponse(mimeType, "utf-8", new ByteArrayInputStream("".getBytes()));
    }
    
    /**
     * Guess MIME type from URL extension
     */
    private static String guessMimeType(String url) {
        if (url == null) return "text/plain";
        
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.contains(".js")) return "application/javascript";
        if (lowerUrl.contains(".css")) return "text/css";
        if (lowerUrl.contains(".png")) return "image/png";
        if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg")) return "image/jpeg";
        if (lowerUrl.contains(".gif")) return "image/gif";
        if (lowerUrl.contains(".webp")) return "image/webp";
        if (lowerUrl.contains(".svg")) return "image/svg+xml";
        if (lowerUrl.contains(".woff")) return "font/woff";
        if (lowerUrl.contains(".woff2")) return "font/woff2";
        
        return "text/plain";
    }
    
    /**
     * Get total number of rules loaded
     */
    public static int getBlockedHostsCount() {
        return getInstance().filterEngine.getTotalRules();
    }
    
    /**
     * DEPRECATED: Use getWhitelistCount()
     */
    @Deprecated
    public static int getWhitelistCount() {
        return 0; // Not tracked separately anymore
    }
    
    /**
     * Get total number of blocked requests
     */
    public static int getBlockedRequestsCount() {
        return getInstance().filterEngine.getBlockedCount();
    }
    
    /**
     * Get total number of allowed requests
     */
    public static int getAllowedRequestsCount() {
        return getInstance().filterEngine.getAllowedCount();
    }
    
    /**
     * Get cache statistics
     */
    public static String getCacheStats() {
        return getInstance().filterEngine.getStats();
    }
    
    /**
     * Get detailed statistics
     */
    public static String getStats() {
        return getInstance().filterEngine.getStats();
    }
    
    /**
     * Clear all data and disable blocking
     */
    public static void clear() {
        getInstance().filterEngine.clear();
        enabled.set(false);
    }
    
    /**
     * Clear only caches (keep rules)
     */
    public static void clearCaches() {
        getInstance().filterEngine.clearCache();
    }
    
    /**
     * Clear all rules but keep whitelist and settings
     */
    public static void clearRules() {
        getInstance().filterEngine.clear();
    }
}
