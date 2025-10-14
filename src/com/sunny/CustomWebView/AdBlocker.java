package com.sunny.CustomWebView;

import android.util.LruCache;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class AdBlocker {

    private static AdBlocker adBlocker;

    // Enhanced caching for better performance
    private static final LruCache<String, Boolean> URL_CACHE = new LruCache<>(8192);
    private static final LruCache<String, Boolean> HOST_CACHE = new LruCache<>(4096);

    // Thread-safe collections
    private static final Set<String> AD_HOSTS = Collections.synchronizedSet(new HashSet<String>());
    private static final Set<String> WHITELIST = Collections.synchronizedSet(new HashSet<String>());
    
    // Ad detection keywords for enhanced blocking
    private static final Set<String> AD_KEYWORDS = new HashSet<>(Arrays.asList(
        "ads", "banner", "sponsor", "promotion", "analytics", "track", "doubleclick", 
        "adservice", "googlesyndication", "adnxs", "adsystem", "amazon-adsystem",
        "facebook.com/tr", "google-analytics", "googletagmanager", "googleadservices",
        "outbrain", "taboola", "adsense", "adform", "criteo", "pubmatic", "openx"
    ));
    
    // Resource patterns that are typically ads
    private static final Set<String> AD_PATHS = new HashSet<>(Arrays.asList(
        "/ads/", "/ad/", "/banner", "/popup", "/tracking", "/analytics",
        "/advert", "/sponsor", "/promo", "/affiliate"
    ));
    
    // Resource extensions to block
    private static final Set<String> BLOCKABLE_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".js", ".png", ".jpg", ".jpeg", ".gif", ".webp", ".svg", ".css", ".woff", ".woff2"
    ));

    private static final AtomicBoolean enabled = new AtomicBoolean(false);
    private static final AtomicBoolean regexEnabled = new AtomicBoolean(false);
    
    // Background thread executor for async operations
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    // Regex patterns for advanced blocking (optional)
    private static volatile Pattern adPattern;

    public static AdBlocker getInstance(){
        synchronized (AdBlocker.class) {
            if (adBlocker == null) {
                adBlocker = new AdBlocker();
            }
        }
        return adBlocker;
    }

    /**
     * Initialize ad hosts synchronously (backward compatible)
     */
    public static void init(String hosts) {
        if (hosts == null || hosts.isEmpty()) {
            return;
        }
        
        synchronized (AD_HOSTS) {
            AD_HOSTS.clear();
            String[] hostsArray = hosts.split(",");
            for (String host : hostsArray) {
                String trimmedHost = host.trim();
                if (!trimmedHost.isEmpty()) {
                    AD_HOSTS.add(trimmedHost.toLowerCase());
                }
            }
        }
        
        // Clear caches when hosts change
        URL_CACHE.evictAll();
        HOST_CACHE.evictAll();
    }
    
    /**
     * Initialize ad hosts asynchronously for better performance
     */
    public static void initAsync(final String hosts) {
        if (hosts == null || hosts.isEmpty()) {
            return;
        }
        
        executor.submit(new Runnable() {
            @Override
            public void run() {
                init(hosts);
            }
        });
    }
    
    /**
     * Add regex pattern for advanced ad detection
     */
    public static void setAdPattern(String regex) {
        try {
            adPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            regexEnabled.set(true);
        } catch (Exception e) {
            regexEnabled.set(false);
            adPattern = null;
        }
    }

    public static void enable(boolean status) {
        enabled.set(status);
    }
    
    public static boolean isEnabled() {
        return enabled.get();
    }
    
    /**
     * Add domain to whitelist
     */
    public static void addToWhitelist(String host) {
        if (host != null && !host.isEmpty()) {
            synchronized (WHITELIST) {
                WHITELIST.add(host.toLowerCase());
            }
        }
    }
    
    /**
     * Remove domain from whitelist
     */
    public static void removeFromWhitelist(String host) {
        if (host != null && !host.isEmpty()) {
            synchronized (WHITELIST) {
                WHITELIST.remove(host.toLowerCase());
            }
        }
    }
    
    /**
     * Check if URL is whitelisted
     */
    public static boolean isWhitelisted(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        try {
            String host = new URL(url).getHost();
            if (host != null) {
                host = host.toLowerCase();
                synchronized (WHITELIST) {
                    return WHITELIST.contains(host) || isSubdomainWhitelisted(host);
                }
            }
        } catch (MalformedURLException ignored) {
        }
        return false;
    }
    
    private static boolean isSubdomainWhitelisted(String host) {
        synchronized (WHITELIST) {
            int dot = host.indexOf('.');
            while (dot > 0 && dot < host.length() - 1) {
                host = host.substring(dot + 1);
                if (WHITELIST.contains(host)) {
                    return true;
                }
                dot = host.indexOf('.');
            }
            return false;
        }
    }

    /**
     * Enhanced ad detection with multiple strategies
     */
    public static boolean isAd(String url) {
        if (url == null || url.isEmpty() || !enabled.get()) {
            return false;
        }
        
        // Check whitelist first
        if (isWhitelisted(url)) {
            return false;
        }
        
        // Check URL cache
        Boolean urlCached = URL_CACHE.get(url);
        if (urlCached != null) {
            return urlCached;
        }

        boolean result = false;
        try {
            URL urlObj = new URL(url);
            String host = urlObj.getHost();
            String path = urlObj.getPath();
            String query = urlObj.getQuery();
            
            if (host != null) {
                host = host.toLowerCase();
                
                // Check host cache
                Boolean hostCached = HOST_CACHE.get(host);
                if (hostCached != null && hostCached) {
                    result = true;
                } else if (hostCached == null) {
                    // Perform host-based detection
                    result = isAdHost(host) || containsAdKeywords(host);
                    HOST_CACHE.put(host, result);
                }
                
                // Additional URL-specific checks if host is not blocked
                if (!result) {
                    result = isAdPath(path) || isAdQuery(query) || 
                             (regexEnabled.get() && adPattern != null && adPattern.matcher(url).find());
                }
                
                // Only block if it's a resource type we should block (not HTML pages)
                if (result && !isBlockableResource(url)) {
                    result = false;
                }
            }
        } catch (MalformedURLException ignored) {
            // Try keyword matching on the full URL as fallback
            result = containsAdKeywords(url.toLowerCase());
        }
        
        URL_CACHE.put(url, result);
        return result;
    }
    
    /**
     * Check if URL represents a blockable resource type
     */
    private static boolean isBlockableResource(String url) {
        if (url == null) return false;
        
        String lowerUrl = url.toLowerCase();
        
        // Check for ad-related paths
        for (String adPath : AD_PATHS) {
            if (lowerUrl.contains(adPath)) {
                return true;
            }
        }
        
        // Check file extensions
        for (String ext : BLOCKABLE_EXTENSIONS) {
            if (lowerUrl.contains(ext)) {
                return true;
            }
        }
        
        // Block query parameters that suggest ads
        return lowerUrl.contains("ad=") || lowerUrl.contains("ads=") || 
               lowerUrl.contains("banner=") || lowerUrl.contains("sponsor=");
    }
    
    /**
     * Enhanced host matching with subdomain support
     */
    private static boolean isAdHost(String host) {
        synchronized (AD_HOSTS) {
            if (AD_HOSTS.contains(host)) return true;
            
            // Check subdomains
            int dot = host.indexOf('.');
            while (dot > 0 && dot < host.length() - 1) {
                host = host.substring(dot + 1);
                if (AD_HOSTS.contains(host)) return true;
                dot = host.indexOf('.');
            }
            return false;
        }
    }
    
    /**
     * Keyword-based ad detection
     */
    private static boolean containsAdKeywords(String text) {
        if (text == null) return false;
        
        String lowerText = text.toLowerCase();
        for (String keyword : AD_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Path-based ad detection
     */
    private static boolean isAdPath(String path) {
        if (path == null) return false;
        
        String lowerPath = path.toLowerCase();
        for (String adPath : AD_PATHS) {
            if (lowerPath.contains(adPath)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Query parameter-based ad detection
     */
    private static boolean isAdQuery(String query) {
        if (query == null) return false;
        
        String lowerQuery = query.toLowerCase();
        return lowerQuery.contains("ad=") || lowerQuery.contains("ads=") || 
               lowerQuery.contains("banner=") || lowerQuery.contains("sponsor=") ||
               lowerQuery.contains("utm_") || lowerQuery.contains("gclid=");
    }

    /**
     * Create empty resource with dynamic MIME type detection
     */
    public WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }
    
    /**
     * Create empty resource with specific MIME type
     */
    public WebResourceResponse createEmptyResource(String mimeType) {
        if (mimeType == null) {
            mimeType = "text/plain";
        }
        return new WebResourceResponse(mimeType, "utf-8", new ByteArrayInputStream("".getBytes()));
    }
    
    /**
     * Create empty resource with MIME type guessed from URL
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

    public static int getBlockedHostsCount() {
        synchronized (AD_HOSTS) {
            return AD_HOSTS.size();
        }
    }
    
    public static int getWhitelistCount() {
        synchronized (WHITELIST) {
            return WHITELIST.size();
        }
    }
    
    /**
     * Get cache statistics for performance monitoring
     */
    public static String getCacheStats() {
        return String.format("URL Cache: %d/%d, Host Cache: %d/%d", 
                URL_CACHE.size(), URL_CACHE.maxSize(),
                HOST_CACHE.size(), HOST_CACHE.maxSize());
    }
    
    /**
     * Clear all data and disable blocking
     */
    public static void clear() {
        synchronized (AD_HOSTS) {
            AD_HOSTS.clear();
        }
        synchronized (WHITELIST) {
            WHITELIST.clear();
        }
        URL_CACHE.evictAll();
        HOST_CACHE.evictAll();
        enabled.set(false);
        regexEnabled.set(false);
        adPattern = null;
    }
    
    /**
     * Clear only caches (keep host lists)
     */
    public static void clearCaches() {
        URL_CACHE.evictAll();
        HOST_CACHE.evictAll();
    }
}
