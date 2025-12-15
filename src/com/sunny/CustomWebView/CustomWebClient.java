package com.sunny.CustomWebView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.webkit.*;
import androidx.annotation.Nullable;
import com.google.appinventor.components.runtime.Form;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class CustomWebClient extends WebViewClient {
    private static final String TAG = "CustomWebClient";
    private static final String ASSET_PREFIX = "file:///appinventor_asset/";
    private final CustomWebView customWebView;
    private final Activity activity;
    private final Context context;

    private volatile @Nullable String topLevelHost;

    CustomWebClient(CustomWebView customWebView) {
        this.customWebView = customWebView;
        this.activity = customWebView.getActivity();
        this.context = customWebView.getContext();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return handleUrlOverride(url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        return handleUrlOverride(url);
    }

    private boolean handleUrlOverride(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        // Handle HTTP/HTTPS URLs
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return !customWebView.FollowLinks();
        }

        // Handle deep links (tel:, mailto:, intent:, etc.)
        return customWebView.DeepLinks() && DeepLinkParser(url);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
        String url = request.getUrl().toString();
        if ("localhost".equals(request.getUrl().getAuthority())
                || request.getUrl().toString().startsWith(ASSET_PREFIX)) {
            return handleAppRequest(request.getUrl().toString());
        }

        // Enhanced ad blocking with resource type detection
        if (AdBlocker.isEnabled()) {
            long resourceType = getResourceType(url, request);
            // Use topLevelHost to construct page URL (safe for background thread)
            String pageUrl = getPageUrlSafe();

            if (AdBlocker.isAd(url, pageUrl, resourceType)) {
                return AdBlocker.createEmptyResourceForUrl(url);
            }
        }
        return super.shouldInterceptRequest(view, request);
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.startsWith("http://localhost/") || url.startsWith(ASSET_PREFIX)) {
            return handleAppRequest(url);
        }

        // Enhanced ad blocking with resource type detection
        if (AdBlocker.isEnabled()) {
            long resourceType = getResourceType(url, null);
            // Use topLevelHost to construct page URL (safe for background thread)
            String pageUrl = getPageUrlSafe();

            if (AdBlocker.isAd(url, pageUrl, resourceType)) {
                return AdBlocker.createEmptyResourceForUrl(url);
            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    /**
     * Get page URL safely without calling WebView methods (thread-safe)
     */
    private String getPageUrlSafe() {
        String host = topLevelHost;
        if (host != null && !host.isEmpty()) {
            return "https://" + host;
        }
        return null;
    }

    /**
     * Detect resource type from URL and request
     */
    private long getResourceType(String url, WebResourceRequest request) {
        // Detect main frame request (API 21+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request != null) {
            if (request.isForMainFrame()) {
                return FlatFilterEngine.TYPE_DOCUMENT;
            }
        }

        if (url == null) {
            return FlatFilterEngine.TYPE_OTHER;
        }

        String lowerUrl = url.toLowerCase();

        // Check file extension
        if (lowerUrl.contains(".js")) {
            return FlatFilterEngine.TYPE_SCRIPT;
        } else if (lowerUrl.contains(".css")) {
            return FlatFilterEngine.TYPE_STYLESHEET;
        } else if (lowerUrl.contains(".png") || lowerUrl.contains(".jpg") ||
                lowerUrl.contains(".jpeg") || lowerUrl.contains(".gif") ||
                lowerUrl.contains(".webp") || lowerUrl.contains(".svg") ||
                lowerUrl.contains(".ico")) {
            return FlatFilterEngine.TYPE_IMAGE;
        } else if (lowerUrl.contains(".woff") || lowerUrl.contains(".woff2") ||
                lowerUrl.contains(".ttf") || lowerUrl.contains(".eot")) {
            return FlatFilterEngine.TYPE_FONT;
        } else if (lowerUrl.contains(".mp4") || lowerUrl.contains(".webm") ||
                lowerUrl.contains(".mp3") || lowerUrl.contains(".ogg")) {
            return FlatFilterEngine.TYPE_MEDIA;
        }

        // Check by request headers if available (API 21+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request != null) {
            Map<String, String> headers = request.getRequestHeaders();
            if (headers != null) {
                String accept = headers.get("Accept");
                if (accept != null) {
                    accept = accept.toLowerCase();
                    if (accept.contains("text/css")) {
                        return FlatFilterEngine.TYPE_STYLESHEET;
                    } else if (accept.contains("image/")) {
                        return FlatFilterEngine.TYPE_IMAGE;
                    } else if (accept.contains("application/javascript") ||
                            accept.contains("text/javascript")) {
                        return FlatFilterEngine.TYPE_SCRIPT;
                    } else if (accept.contains("font/") || accept.contains("application/font")) {
                        return FlatFilterEngine.TYPE_FONT;
                    } else if (accept.contains("video/") || accept.contains("audio/")) {
                        return FlatFilterEngine.TYPE_MEDIA;
                    }
                }
            }
        }

        // Check for XHR/Fetch requests
        if (lowerUrl.contains("/api/") || lowerUrl.contains("/ajax/") ||
                lowerUrl.contains(".json")) {
            return FlatFilterEngine.TYPE_XMLHTTP;
        }

        return FlatFilterEngine.TYPE_OTHER;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (customWebView.getWebView() == view) {
            if (customWebView.isLoading()) {
                customWebView.setLoading(false);
                customWebView.PageLoaded(view.getId());
            }

            // Inject AdBlock cosmetic filters
            if (AdBlocker.isEnabled()) {
                AdBlocker.injectCosmeticHelper(view);
            }

            // Inject custom print function that prevents navigation
            String printScript = "javascript:(function() {" +
                    "    console.log('Injecting custom print function');" +
                    "    " +
                    "    // Override window.print to prevent default behavior" +
                    "    window.print = function() {" +
                    "        console.log('Custom print called');" +
                    "        if (typeof window.AppInventor !== 'undefined') {" +
                    "            try {" +
                    "                window.AppInventor.print();" +
                    "                return false; // Prevent default behavior" +
                    "            } catch(e) {" +
                    "                console.error('Print failed:', e);" +
                    "            }" +
                    "        } else {" +
                    "            console.error('AppInventor interface not available');" +
                    "        }" +
                    "        return false; // Always prevent default" +
                    "    };" +
                    "    " +
                    "    // Also handle print events" +
                    "    document.addEventListener('keydown', function(e) {" +
                    "        if ((e.ctrlKey || e.metaKey) && e.key === 'p') {" +
                    "            e.preventDefault();" +
                    "            window.print();" +
                    "            return false;" +
                    "        }" +
                    "    });" +
                    "    " +
                    "    // Handle beforeprint event" +
                    "    window.addEventListener('beforeprint', function(e) {" +
                    "        e.preventDefault();" +
                    "        window.print();" +
                    "        return false;" +
                    "    });" +
                    "    " +
                    "    console.log('Print override complete');" +
                    "})();";

            view.evaluateJavascript(printScript, null);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        customWebView.setSSLHandler(handler);
        customWebView.OnReceivedSslError(error.getPrimaryError());
        /*
         * if (ignoreSslErrors) {
         * handler.proceed();
         * } else {
         * handler.cancel();
         * }
         */
    }

    @Override
    public void onFormResubmission(WebView view, Message dontResend, Message resend) {
        customWebView.setFormHandlers(dontResend, resend);
        customWebView.OnFormResubmission(view.getId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        customWebView.OnErrorReceived(view.getId(), description, errorCode, failingUrl);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        customWebView.OnErrorReceived(view.getId(), errorResponse.getReasonPhrase(), errorResponse.getStatusCode(),
                request.getUrl().toString());
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        customWebView.OnErrorReceived(view.getId(), error.getDescription().toString(), error.getErrorCode(),
                request.getUrl().toString());
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        try {
            Uri u = Uri.parse(url);
            topLevelHost = u != null ? u.getHost() : null;
        } catch (Exception ignored) {
            topLevelHost = null;
        }
        if (!customWebView.isLoading()) {
            customWebView.PageStarted(view.getId(), url);
            customWebView.setLoading(true);
        }
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
        customWebView.setAuthHandler(handler);
        customWebView.OnReceivedHttpAuthRequest(view.getId(), host, realm);
    }

    private WebResourceResponse handleAppRequest(String url) {
        String path;
        if (url.startsWith(ASSET_PREFIX)) {
            path = url.substring(ASSET_PREFIX.length());
        } else {
            path = url.substring(url.indexOf("//localhost/") + 12);
        }
        InputStream stream;
        try {
            stream = Form.getActiveForm().openAsset(path);
            Map<String, String> headers = new HashMap<>();
            headers.put("Access-Control-Allow-Origin", "localhost");
            String mimeType = URLConnection.getFileNameMap().getContentTypeFor(path);
            String encoding = "utf-8";
            if (mimeType == null
                    || (!mimeType.startsWith("text/") && !mimeType.equals("application/javascript"))) {
                encoding = null;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return new WebResourceResponse(mimeType, encoding, 200, "OK", headers, stream);
            } else {
                return new WebResourceResponse(mimeType, encoding, stream);
            }
        } catch (Exception e) {
            ByteArrayInputStream error = new ByteArrayInputStream("404 Not Found".getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found", null, error);
            } else {
                return new WebResourceResponse("text/plain", "utf-8", error);
            }
        }
    }

    private boolean DeepLinkParser(String url) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent;
        if (url.startsWith("tel:")) {
            intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            activity.startActivity(intent);
            return true;
        } else if (url.startsWith("mailto:") || url.startsWith("sms:")) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(intent);
            return true;
        } else if (url.startsWith("whatsapp:")) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.whatsapp");
            activity.startActivity(intent);
            return true;
        } else if (url.startsWith("geo:")) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.google.android.apps.maps");
            if (intent.resolveActivity(packageManager) != null) {
                activity.startActivity(intent);
                return true;
            } else {
                return false;
            }
        } else if (url.startsWith("intent:")) {
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if (intent.resolveActivity(packageManager) != null) {
                    activity.startActivity(intent);
                    return true;
                }
                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                if (fallbackUrl != null) {
                    customWebView.getWebView().loadUrl(fallbackUrl);
                }
                intent = new Intent(Intent.ACTION_VIEW).setData(
                        Uri.parse("market://details?id=" + intent.getPackage()));
                if (intent.resolveActivity(packageManager) != null) {
                    activity.startActivity(intent);
                    return true;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else if (customWebView.isCustomDeepLink(url)) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(intent);
            return true;
        }
        return false;
    }
}
