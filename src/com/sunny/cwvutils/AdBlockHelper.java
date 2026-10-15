package com.sunny.cwvutils;

import java.util.List;

import android.content.Context;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import com.sunny.CustomWebView.RequestInterceptor;

@DesignerComponent(version = 1, versionName = "1.0", description = "Modular content blocking helper powered by AdBlock Plus format filters."
        +
        " Attach to a CustomWebView to intercept and block ad requests." +
        " <br> Developed by Sunny Gupta", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "https://i.ibb.co/4wLNN1Hs/ktvu4bapylsvnykoyhdm-c-fill-w-20-h-20.png", helpUrl = "https://github.com/vknow360/CustomWebView", androidMinSdk = 21)
@SimpleObject(external = true)
public class AdBlockHelper extends AndroidNonvisibleComponent implements RequestInterceptor {

    private final Context context;

    public AdBlockHelper(ComponentContainer container) {
        super(container.$form());
        this.context = container.$context();
        AdBlocker.init(context);
    }

    @Override
    public WebResourceResponse intercept(String url, String pageUrl, boolean isMainFrame, String acceptHeader) {
        if (!AdBlocker.isEnabled() || url == null || isMainFrame) {
            return null;
        }
        if (pageUrl != null && url.equalsIgnoreCase(pageUrl)) {
            return null;
        }
        long resourceType = resolveResourceType(url, false, acceptHeader);
        if (AdBlocker.isAd(url, pageUrl, resourceType)) {
            AdBlocker.recordBlocked();
            return AdBlocker.createEmptyResourceForUrl(url);
        }
        AdBlocker.recordAllowed();
        return null;
    }

    @Override
    public void onPageFinished(WebView webView) {
        if (AdBlocker.isEnabled()) {
            AdBlocker.injectCosmeticHelper(webView);
        }
    }

    private long resolveResourceType(String url, boolean isMainFrame, String acceptHeader) {
        if (isMainFrame) {
            return FlatFilterEngine.TYPE_DOCUMENT;
        }

        String lowerUrl = url.toLowerCase();

        if (lowerUrl.contains(".js")) {
            return FlatFilterEngine.TYPE_SCRIPT;
        } else if (lowerUrl.contains(".css")) {
            return FlatFilterEngine.TYPE_STYLESHEET;
        } else if (lowerUrl.contains(".png") || lowerUrl.contains(".jpg")
                || lowerUrl.contains(".jpeg") || lowerUrl.contains(".gif")
                || lowerUrl.contains(".webp") || lowerUrl.contains(".svg")
                || lowerUrl.contains(".ico")) {
            return FlatFilterEngine.TYPE_IMAGE;
        } else if (lowerUrl.contains(".woff") || lowerUrl.contains(".woff2")
                || lowerUrl.contains(".ttf") || lowerUrl.contains(".eot")) {
            return FlatFilterEngine.TYPE_FONT;
        } else if (lowerUrl.contains(".mp4") || lowerUrl.contains(".webm")
                || lowerUrl.contains(".mp3") || lowerUrl.contains(".ogg")) {
            return FlatFilterEngine.TYPE_MEDIA;
        }

        if (acceptHeader != null) {
            String accept = acceptHeader.toLowerCase();
            if (accept.contains("text/html")) {
                return FlatFilterEngine.TYPE_SUBDOCUMENT;
            } else if (accept.contains("text/css")) {
                return FlatFilterEngine.TYPE_STYLESHEET;
            } else if (accept.contains("image/")) {
                return FlatFilterEngine.TYPE_IMAGE;
            } else if (accept.contains("application/javascript")
                    || accept.contains("text/javascript")) {
                return FlatFilterEngine.TYPE_SCRIPT;
            } else if (accept.contains("font/") || accept.contains("application/font")) {
                return FlatFilterEngine.TYPE_FONT;
            } else if (accept.contains("video/") || accept.contains("audio/")) {
                return FlatFilterEngine.TYPE_MEDIA;
            }
        }

        if (lowerUrl.contains("/api/") || lowerUrl.contains("/ajax/")
                || lowerUrl.contains(".json")) {
            return FlatFilterEngine.TYPE_XMLHTTP;
        }

        return FlatFilterEngine.TYPE_OTHER;
    }

    @SimpleFunction(description = "Attach this AdBlockHelper to a CustomWebView to enable request interception.")
    public void AttachTo(Object webView) {
        if (webView instanceof com.sunny.CustomWebView.CustomWebView) {
            ((com.sunny.CustomWebView.CustomWebView) webView).setRequestInterceptor(this);
        }
    }

    @SimpleFunction(description = "Detach this AdBlockHelper from a CustomWebView.")
    public void DetachFrom(Object webView) {
        if (webView instanceof com.sunny.CustomWebView.CustomWebView) {
            com.sunny.CustomWebView.CustomWebView cwv = (com.sunny.CustomWebView.CustomWebView) webView;
            if (cwv.getRequestInterceptor() == this) {
                cwv.setRequestInterceptor(null);
            }
        }
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets whether to block ads or not")
    public void BlockAds(boolean block) {
        AdBlocker.enable(block);
    }

    @SimpleProperty(description = "Returns whether ads are blocked or not")
    public boolean BlockAds() {
        return AdBlocker.isEnabled();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets the ad hosts which will be blocked")
    public void AdHosts(String hosts) {
        AdBlocker.init(hosts);
    }

    @SimpleFunction(description = "Initialize ad hosts asynchronously for better performance")
    public void InitAdHostsAsync(String hosts) {
        AdBlocker.initAsync(hosts);
    }

    @SimpleFunction(description = "Add a domain to the ad blocking whitelist")
    public void AddToWhitelist(String host) {
        AdBlocker.addToWhitelist(host);
    }

    @SimpleFunction(description = "Remove a domain from the ad blocking whitelist")
    public void RemoveFromWhitelist(String host) {
        AdBlocker.removeFromWhitelist(host);
    }

    @SimpleFunction(description = "Check if a URL is whitelisted")
    public boolean IsWhitelisted(String url) {
        return AdBlocker.isWhitelisted(url);
    }

    @SimpleFunction(description = "Get the number of blocked ad hosts")
    public int GetBlockedHostsCount() {
        return AdBlocker.getBlockedHostsCount();
    }

    @SimpleFunction(description = "Get the number of whitelisted domains")
    public int GetWhitelistCount() {
        return AdBlocker.getWhitelistCount();
    }

    @SimpleFunction(description = "Get ad blocker cache statistics")
    public String GetAdBlockerStats() {
        return AdBlocker.getCacheStats();
    }

    @SimpleFunction(description = "Clear ad blocker caches only (keep host lists)")
    public void ClearAdBlockerCaches() {
        AdBlocker.clearCaches();
    }

    @SimpleFunction(description = "Clear all ad blocker data")
    public void ClearAdBlockerData() {
        AdBlocker.clear();
    }

    @SimpleFunction(description = "Initialize AdBlocker with context (required for loading filter lists from assets)")
    public void InitAdBlocker() {
        AdBlocker.init(context);
    }

    private FilterListLoader.LoadCallback createUiCallback() {
        return new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(final List<String> rules, final String source) {
                form.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OnFilterListLoaded(rules != null ? rules.size() : 0, source);
                    }
                });
            }

            @Override
            public void onError(final String error, final String source) {
                form.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OnFilterListError(error != null ? error : "", source);
                    }
                });
            }

            @Override
            public void onProgress(final int loaded, final String source) {
                form.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OnFilterListProgress(loaded, source);
                    }
                });
            }
        };
    }

    @SimpleFunction(description = "Load filter list from app assets")
    public void LoadFilterListFromAsset(final String assetPath) {
        AdBlocker.loadFilterListFromAsset(assetPath, createUiCallback());
    }

    @SimpleFunction(description = "Load filter list from external URL")
    public void LoadFilterListFromUrl(final String url) {
        AdBlocker.loadFilterListFromUrl(url, createUiCallback());
    }

    @SimpleFunction(description = "Load filter list from file path")
    public void LoadFilterListFromFile(final String filePath) {
        AdBlocker.loadFilterListFromFile(filePath, createUiCallback());
    }

    @SimpleFunction(description = "Load default minimal filter list with common ad domains")
    public void LoadDefaultFilterList() {
        AdBlocker.loadDefaultFilters();
    }

    @SimpleFunction(description = "Add custom filter rules in AdBlock Plus format")
    public void AddFilterRules(String rulesString) {
        AdBlocker.addFilterRules(rulesString);
    }

    @SimpleFunction(description = "Add a single filter rule in AdBlock Plus format")
    public void AddFilterRule(String rule) {
        AdBlocker.addFilterRule(rule);
    }

    @SimpleFunction(description = "Get total number of filter rules loaded")
    public int GetFilterRulesCount() {
        return AdBlocker.getBlockedHostsCount();
    }

    @SimpleFunction(description = "Get total number of blocked requests")
    public int GetBlockedRequestsCount() {
        return AdBlocker.getBlockedRequestsCount();
    }

    @SimpleFunction(description = "Get total number of allowed requests")
    public int GetAllowedRequestsCount() {
        return AdBlocker.getAllowedRequestsCount();
    }

    @SimpleFunction(description = "Get detailed AdBlocker statistics")
    public String GetAdBlockerDetailedStats() {
        return AdBlocker.getStats();
    }

    @SimpleFunction(description = "Clear all filter rules but keep whitelist and settings")
    public void ClearFilterRules() {
        AdBlocker.clearRules();
    }

    // ===== Events =====

    @SimpleEvent(description = "Event fired when a filter list is successfully loaded")
    public void OnFilterListLoaded(int rulesCount, String source) {
        EventDispatcher.dispatchEvent(this, "OnFilterListLoaded", rulesCount, source);
    }

    @SimpleEvent(description = "Event fired when filter list loading fails")
    public void OnFilterListError(String error, String source) {
        EventDispatcher.dispatchEvent(this, "OnFilterListError", error, source);
    }

    @SimpleEvent(description = "Event fired during filter list loading to show progress")
    public void OnFilterListProgress(int loaded, String source) {
        EventDispatcher.dispatchEvent(this, "OnFilterListProgress", loaded, source);
    }
}
