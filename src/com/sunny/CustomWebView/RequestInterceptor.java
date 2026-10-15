package com.sunny.CustomWebView;

import android.webkit.WebResourceResponse;

/**
 * Callback interface for intercepting and blocking web requests.
 * Register via {@link CustomWebView#setRequestInterceptor(RequestInterceptor)}.
 */
public interface RequestInterceptor {

    /**
     * Determines whether a request should be blocked.
     *
     * @param url          the full request URL
     * @param pageUrl      the top-level page URL, may be null
     * @param isMainFrame  true if this is a main frame navigation
     * @param acceptHeader the Accept header value, may be null
     * @return a {@link WebResourceResponse} to block (e.g. empty body), or null to allow
     */
    WebResourceResponse intercept(String url, String pageUrl, boolean isMainFrame, String acceptHeader);

    /**
     * Called when a page finishes loading for post-load modifications
     * such as cosmetic filtering.
     *
     * @param webView the WebView that finished loading
     */
    void onPageFinished(android.webkit.WebView webView);
}
