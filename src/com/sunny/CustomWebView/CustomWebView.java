package com.sunny.CustomWebView;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.FrameLayout;
import androidx.webkit.ServiceWorkerControllerCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.sunny.CustomWebView.util.Constants;

import java.io.UnsupportedEncodingException;
import java.util.*;

@DesignerComponent(version = 13, versionName = "13", description = "An extended form of Web Viewer <br> Developed by Sunny Gupta", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "https://i.ibb.co/4wLNN1Hs/ktvu4bapylsvnykoyhdm-c-fill-w-20-h-20.png", helpUrl = "https://github.com/vknow360/CustomWebView", androidMinSdk = 21)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE,android.permission.ACCESS_DOWNLOAD_MANAGER,android.permission.ACCESS_FINE_LOCATION,android.permission.RECORD_AUDIO, android.permission.MODIFY_AUDIO_SETTINGS, android.permission.CAMERA,android.permission.VIBRATE,android.webkit.resource.VIDEO_CAPTURE,android.webkit.resource.AUDIO_CAPTURE,android.launcher.permission.INSTALL_SHORTCUT")
@UsesLibraries(libraries = "androidx-webkit.jar")
public final class CustomWebView extends AndroidNonvisibleComponent implements WView.SwipeCallback {
    private final Activity activity;
    private WView webView;
    private final Context context;
    private boolean followLinks = true;
    private boolean prompt = true;
    private String UserAgent = "";
    private final WebViewInterface wvInterface;
    private JsPromptResult jsPromptResult;
    private String MOBILE_USER_AGENT = "";
    private ValueCallback<Uri[]> mFilePathCallback;
    private Message dontSend;
    private Message reSend;
    private PermissionRequest permissionRequest;
    private final CookieManager cookieManager;
    private JsResult jsResult;
    private JsResult jsAlert;
    private HttpAuthHandler httpAuthHandler;
    private boolean deepLinks = false;
    private boolean isLoading = false;
    private final HashMap<Integer, WView> wv = new HashMap<>();
    private int iD = -1;
    private boolean desktopMode = false;
    private int zoomPercent = 100;
    private boolean zoomEnabled = true;
    private boolean displayZoom = true;
    private Message resultObj;
    private final float deviceDensity;
    private GeolocationPermissions.Callback theCallback;
    private String theOrigin;
    private SslErrorHandler sslHandler;
    private final List<String> customDeepLink = new ArrayList<>();
    private boolean isScrollEnabled = true;

    public CustomWebView(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
        context = activity;
        wvInterface = new WebViewInterface(this);
        cookieManager = CookieManager.getInstance();
        deviceDensity = container.$form().deviceDensity();
        webView = new WView(-1, context, this);
    }

    private int d2p(int d) {
        return Math.round(d / deviceDensity);
    }

    private int p2d(int p) {
        return Math.round(p * deviceDensity);
    }

    public Activity getActivity() {
        return activity;
    }

    public WView getWebView() {
        return webView;
    }

    public Context getContext() {
        return context;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setSSLHandler(SslErrorHandler sslHandler) {
        this.sslHandler = sslHandler;
    }

    public void setFormHandlers(Message dontResend, Message resend) {
        this.dontSend = dontResend;
        this.reSend = resend;
    }

    public void setAuthHandler(HttpAuthHandler httpAuthHandler) {
        this.httpAuthHandler = httpAuthHandler;
    }

    public boolean isCustomDeepLink(String url) {
        return customDeepLink.contains(url.split(":")[0]);
    }

    public void setGeolocationCallback(GeolocationPermissions.Callback callback, String origin) {
        theOrigin = origin;
        theCallback = callback;
    }

    public void setFilePathCallback(ValueCallback<Uri[]> callback) {
        mFilePathCallback = callback;
    }

    public void setResultObject(Message resultObj) {
        this.resultObj = resultObj;
    }

    public void setJsPromptResult(JsPromptResult res) {
        jsPromptResult = res;
    }

    public void setJsResult(JsResult res) {
        jsResult = res;
    }

    public void setJsAlertResult(JsResult res) {
        jsAlert = res;
    }

    public void setPermissionRequestCallback(PermissionRequest request) {
        permissionRequest = request;
    }

    @SuppressWarnings("deprecation")
    @SimpleFunction()
    public void SetDarkMode(boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.getSettings(), enable);
        } else {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                WebSettingsCompat.setForceDarkStrategy(
                        webView.getSettings(),
                        WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
                WebSettingsCompat.setForceDark(
                        webView.getSettings(),
                        enable ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
            }
        }
    }

    @SimpleFunction(description = "Creates the webview in given arrangement with id")
    public void CreateWebView(Object container, final int id) {
        if (container == null)
            return;
        final View view = ((AndroidViewComponent) container).getView();
        if (!wv.containsKey(id)) {
            WView w = new WView(id, context, this);
            resetWebView(w);
            ViewGroup vg = (ViewGroup) view;
            vg.addView(w, new FrameLayout.LayoutParams(-1, -1));
            wv.put(id, w);
        }
    }

    @SimpleFunction(description = "Returns webview object from id")
    public Object GetWebView(int id) {
        if (wv.containsKey(id)) {
            return wv.get(id);
        }
        return null;
    }

    @SimpleFunction(description = "Set specific webview to current webview by id")
    public void SetWebView(final int id) {
        if (wv.containsKey(id)) {
            webView = wv.get(id);
            webView.setVisibility(View.VISIBLE);
            iD = id;
        }
    }

    private void resetWebView(final WView web) {
        web.addJavascriptInterface(wvInterface, "AppInventor");
        MOBILE_USER_AGENT = web.getSettings().getUserAgentString();
        web.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        web.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        web.setFocusable(true);
        web.setWebViewClient(new CustomWebClient(this));
        web.setWebChromeClient(new CustomWebChromeClient(this));

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDisplayZoomControls(displayZoom);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowFileAccessFromFileURLs(false);
        web.getSettings().setAllowUniversalAccessFromFileURLs(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setSupportZoom(zoomEnabled);
        web.getSettings().setBuiltInZoomControls(zoomEnabled);
        web.setLongClickable(false);
        web.getSettings().setTextZoom(zoomPercent);
        cookieManager.setAcceptThirdPartyCookies(web, true);
        web.getSettings().setDomStorageEnabled(true);
        web.setVerticalScrollBarEnabled(true);
        web.setHorizontalScrollBarEnabled(true);
        web.getSettings().setDefaultFontSize(16);
        web.getSettings().setBlockNetworkImage(false);
        web.getSettings().setLoadsImagesAutomatically(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setBlockNetworkLoads(false);
        web.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        web.getSettings().setSupportMultipleWindows(true);
        web.getSettings().setGeolocationDatabasePath(null);
        web.getSettings().setDatabaseEnabled(true);
        web.getSettings().setGeolocationEnabled(false);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        if (UserAgent.isEmpty()) {
            UserAgent = MOBILE_USER_AGENT;
        }
        web.getSettings().setUserAgentString(UserAgent);
        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                OnDownloadNeeded(web.getId(), s, s2, s3, l);
            }
        });
        web.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int i, int i1, boolean b) {
                FindResultReceived(web.getId(), i, i1, b);
            }
        });
        web.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isScrollEnabled) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!v.hasFocus()) {
                                v.requestFocus();
                            }
                            break;
                    }
                    return false;
                } else {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            }
        });
        web.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!webView.isLongClickable()) {
                    return true;
                } else {
                    final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                    String item = hitTestResult.getExtra();
                    int type = hitTestResult.getType();
                    if (type != WebView.HitTestResult.UNKNOWN_TYPE) {
                        if (item == null) {
                            item = "";
                        }
                        String str = "";
                        if (type == 8) {
                            Message message = new Handler().obtainMessage();
                            web.requestFocusNodeHref(message);
                            str = (String) message.getData().get("url");
                        }
                        LongClicked(web.getId(), item, str, type);
                        return !webView.isLongClickable();
                    }
                    return false;
                }
            }
        });
        if (Build.VERSION.SDK_INT >= 23) {
            web.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                    OnScrollChanged(web.getId(), i, i1, i2, i3, web.canScrollHorizontally(-1),
                            web.canScrollHorizontally(1));
                }
            });
        } else {
            web.setScrollChangeListener(new WView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(Context v, int i, int i1, int i2, int i3) {
                    OnScrollChanged(web.getId(), i, i1, i2, i3, web.canScrollHorizontally(-1),
                            web.canScrollHorizontally(1));
                }
            });
        }
    }

    @SimpleFunction()
    public void DownloadBlob(String url, String mimeType, String fileName, String downloadDir) {
        webView.loadUrl(wvInterface.getBase64StringFromBlobUrl(url, mimeType, fileName, downloadDir));
    }

    @SimpleEvent()
    public void BlobFileDownloaded(String filePath) {
        EventDispatcher.dispatchEvent(this, "BlobFileDownloaded", filePath);
    }

    @SimpleFunction(description = "Returns a list of used ids")
    public List<Integer> GetIds() {
        return new ArrayList<>(wv.keySet());
    }

    @SimpleProperty(description = "Set webview string")
    public void WebViewString(String newString) {
        wvInterface.setWebViewStringFromBlocks(newString);
    }

    @SimpleProperty(description = "Returns the visibility of current webview")
    public boolean Visible() {
        return webView.getVisibility() == View.VISIBLE;
    }

    @SimpleFunction(description = "Sets the visibility of webview by id")
    public void SetVisibility(int id, boolean visibility) {
        if (wv.containsKey(id)) {
            if (visibility) {
                wv.get(id).setVisibility(View.VISIBLE);
            } else {
                wv.get(id).setVisibility(View.GONE);
            }
        }
    }

    @SimpleProperty(description = "Get webview string")
    public String WebViewString() {
        return wvInterface.getWebViewString();
    }

    @SimpleProperty(description = "Sets scroll bar style")
    public void ScrollBarStyle(int style) {
        webView.setScrollBarStyle(style);
    }

    @SimpleProperty(description = "Gets scroll bar style")
    public int ScrollBarStyle() {
        return webView.getScrollBarStyle();
    }

    @SimpleProperty(description = "Sets over scroll mode")
    public void OverScrollMode(int mode) {
        webView.setOverScrollMode(mode);
    }

    @SimpleProperty(description = "Gets over scroll mode")
    public int OverScrollMode() {
        return webView.getOverScrollMode();
    }

    @SimpleProperty(description = "Sets layer type")
    public void LayerType(int type) {
        webView.setLayerType(type, null);
    }

    @SimpleProperty(description = "Gets layer type")
    public int LayerType() {
        return webView.getLayerType();
    }

    @SimpleProperty(description = "Sets rotation angle")
    public void RotationAngle(float rotation) {
        webView.setRotation(rotation);
    }

    @SimpleProperty(description = "Gets rotation angle")
    public float RotationAngle() {
        return webView.getRotation();
    }

    @SimpleProperty(description = "Get webview user agent")
    public String UserAgent() {
        return UserAgent;
    }

    @SimpleProperty(description = "Sets the WebView's user-agent string. If the string is null or empty, the system default value will be used. ")
    public void UserAgent(String userAgent) {
        if (!userAgent.isEmpty()) {
            UserAgent = userAgent;
        } else {
            UserAgent = MOBILE_USER_AGENT;
        }
        webView.getSettings().setUserAgentString(UserAgent);
    }

    @SimpleProperty(description = "URL of the page currently viewed")
    public String CurrentUrl() {
        return (webView.getUrl() == null) ? "" : webView.getUrl();
    }

    @SimpleProperty(description = "Title of the page currently viewed")
    public String CurrentPageTitle() {
        return (webView.getTitle() == null) ? "" : webView.getTitle();
    }

    @SimpleProperty(description = "Determines whether to follow links when they are tapped in the WebViewer."
            + "If you follow links, you can use GoBack and GoForward to navigate the browser history")
    public boolean FollowLinks() {
        return followLinks;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets whether to enable deep links or not i.e. tel: , whatsapp: , sms: , etc.")
    public void DeepLinks(boolean d) {
        deepLinks = d;
    }

    @SimpleProperty(description = "Returns whether deep links are enabled or not")
    public boolean DeepLinks() {
        return deepLinks;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets whether to follow links or not")
    public void FollowLinks(boolean follow) {
        followLinks = follow;
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

    // ========== NEW ENHANCED AD BLOCKER API ==========

    @SimpleFunction(description = "Initialize AdBlocker with context (required for loading filter lists from assets)")
    public void InitAdBlocker() {
        AdBlocker.init(context);
    }

    @SimpleFunction(description = "Load filter list from app assets. Use callback blocks to handle success/error.")
    public void LoadFilterListFromAsset(final String assetPath) {
        AdBlocker.loadFilterListFromAsset(assetPath, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                OnFilterListLoaded(rules.size(), source);
            }

            @Override
            public void onError(String error, String source) {
                OnFilterListError(error, source);
            }

            @Override
            public void onProgress(int loaded, String source) {
                OnFilterListProgress(loaded, source);
            }
        });
    }

    @SimpleFunction(description = "Load filter list from external URL. Use callback events to handle success/error.")
    public void LoadFilterListFromUrl(final String url) {
        AdBlocker.loadFilterListFromUrl(url, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                OnFilterListLoaded(rules.size(), source);
            }

            @Override
            public void onError(String error, String source) {
                OnFilterListError(error, source);
            }

            @Override
            public void onProgress(int loaded, String source) {
                OnFilterListProgress(loaded, source);
            }
        });
    }

    @SimpleFunction(description = "Load filter list from file path. Use callback events to handle success/error.")
    public void LoadFilterListFromFile(final String filePath) {
        AdBlocker.loadFilterListFromFile(filePath, new FilterListLoader.LoadCallback() {
            @Override
            public void onSuccess(List<String> rules, String source) {
                OnFilterListLoaded(rules.size(), source);
            }

            @Override
            public void onError(String error, String source) {
                OnFilterListError(error, source);
            }

            @Override
            public void onProgress(int loaded, String source) {
                OnFilterListProgress(loaded, source);
            }
        });
    }

    @SimpleFunction(description = "Load default minimal filter list with common ad domains")
    public void LoadDefaultFilterList() {
        AdBlocker.loadDefaultFilters();
    }

    @SimpleFunction(description = "Add custom filter rules in AdBlock Plus format (e.g., ||example.com^, /ads/*, etc.)")
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

    // ========== END NEW ENHANCED AD BLOCKER API ==========

    @SimpleProperty(description = "Sets whether the WebView requires a user gesture to play media")
    public void AutoplayMedia(boolean bool) {
        webView.getSettings().setMediaPlaybackRequiresUserGesture(bool);
    }

    @SimpleProperty(description = "Returns whether the WebView requires a user gesture to play media")
    public boolean AutoplayMedia() {
        return webView.getSettings().getMediaPlaybackRequiresUserGesture();
    }

    @SimpleProperty(description = "Sets cache mode for active webview")
    public void CacheMode(int mode) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_CACHE_MODE)) {
            ServiceWorkerControllerCompat.getInstance().getServiceWorkerWebSettings().setCacheMode(mode);

        }
        webView.getSettings().setCacheMode(mode);
    }

    @SimpleProperty(description = "Gets cache mode of active webview")
    public int CacheMode() {
        return webView.getSettings().getCacheMode();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets whether the WebView should support zooming using its on-screen zoom controls and gestures")
    public void ZoomEnabled(boolean bool) {
        zoomEnabled = bool;
    }

    @SimpleProperty(description = "Gets whether the WebView should support zooming using its on-screen zoom controls and gestures")
    public boolean ZoomEnabled() {
        return zoomEnabled;
    }

    @SimpleProperty(description = "Sets whether the WebView should load image resources")
    public void AutoLoadImages(boolean bool) {
        webView.getSettings().setBlockNetworkImage(!bool);
        webView.getSettings().setLoadsImagesAutomatically(bool);
    }

    @SimpleProperty(description = "Returnss whether the WebView should load image resources")
    public boolean AutoLoadImages() {
        return webView.getSettings().getLoadsImagesAutomatically();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets whether the WebView should display on-screen zoom controls")
    public void DisplayZoom(boolean bool) {
        displayZoom = bool;
    }

    @SimpleProperty(description = "Gets whether the WebView should display on-screen zoom controls")
    public boolean DisplayZoom() {
        return displayZoom;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "100")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets the zoom of the page in percent. The default is 100")
    public void ZoomPercent(int zoom) {
        zoomPercent = zoom;
    }

    @SimpleProperty(description = "Gets the zoom of the page in percent")
    public int ZoomPercent() {
        return zoomPercent;
    }

    @SimpleProperty(description = "Sets the default font size of text. The default is 16.")
    public void FontSize(int size) {
        webView.getSettings().setDefaultFontSize(size);
    }

    @SimpleProperty(description = "Returns the font size of text")
    public int FontSize() {
        return webView.getSettings().getDefaultFontSize();
    }

    @SimpleProperty(description = "Sets whether to load content in desktop mode")
    public void DesktopMode(boolean mode) {
        if (mode) {
            UserAgent = UserAgent.replace("Android", "diordnA").replace("Mobile", "eliboM");
        } else {
            UserAgent = UserAgent.replace("diordnA", "Android").replace("eliboM", "Mobile");
        }
        webView.getSettings().setUserAgentString(UserAgent);
        desktopMode = mode;
    }

    @SimpleProperty(description = "Returns whether to load content in desktop mode")
    public boolean DesktopMode() {
        return desktopMode;
    }

    // @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
    // defaultValue = "True")
    @SimpleProperty(description = "Sets whether to enable text selection and context menu")
    public void LongClickable(boolean bool) {
        webView.setLongClickable(bool);
    }

    @SimpleProperty(description = "Returns whether text selection and context menu are enabled or not")
    public boolean LongClickable() {
        return webView.isLongClickable();
    }

    @SimpleProperty(description = "Sets the initial scale for active WebView. 0 means default. If initial scale is greater than 0, WebView starts with this value as initial scale.")
    public void InitialScale(int scale) {
        webView.setInitialScale(scale);
    }

    @SimpleProperty(description = "Sets whether webview can access local files.Use this to enable file uploading and loading files using HTML")
    public void FileAccess(boolean allow) {
        webView.getSettings().setAllowFileAccess(allow);
        webView.getSettings().setAllowFileAccessFromFileURLs(allow);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(allow);
        webView.getSettings().setAllowContentAccess(allow);
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_FILE_ACCESS)) {
            ServiceWorkerControllerCompat.getInstance().getServiceWorkerWebSettings().setAllowFileAccess(allow);
        }
    }

    @SimpleProperty(description = "Returns whether webview can access local files")
    public boolean FileAccess() {
        return webView.getSettings().getAllowFileAccess();
    }

    @SimpleProperty(description = "Sets whether the WebView supports multiple windows")
    public void SupportMultipleWindows(boolean support) {
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(support);
        webView.getSettings().setSupportMultipleWindows(support);
    }

    @SimpleProperty(description = "Sets whether the WebView should not load resources from the network.Use this to save data.")
    public void BlockNetworkLoads(boolean block) {
        webView.getSettings().setBlockNetworkLoads(block);
    }

    @SimpleProperty(description = "Returns whether the WebView should not load resources from the network")
    public boolean BlockNetworkLoads() {
        return webView.getSettings().getBlockNetworkLoads();
    }

    @SimpleProperty(description = "Returns whether the WebView supports multiple windows")
    public boolean SupportMultipleWindows() {
        return webView.getSettings().getJavaScriptCanOpenWindowsAutomatically();
    }

    public String nWM = "DEFAULT";

    @SimpleProperty()
    public void NewWindowApproach(String str) {
        if (!str.isEmpty()) {
            nWM = str;
        }
    }

    @SimpleProperty(description = "Sets whether the WebView loads pages in overview mode, that is, zooms out the content to fit on screen by width. This setting is taken into account when the content width is greater than the width of the WebView control.")
    public void LoadWithOverviewMode(boolean bool) {
        webView.getSettings().setLoadWithOverviewMode(bool);
    }

    @SimpleProperty(description = "Sets whether the WebView should enable support for the 'viewport' HTML meta tag or should use a wide viewport.")
    public void UseWideViewPort(boolean bool) {
        webView.getSettings().setUseWideViewPort(bool);
    }

    @SimpleProperty(description = "Returns whether the WebView loads pages in overview mode")
    public boolean LoadWithOverviewMode() {
        return webView.getSettings().getLoadWithOverviewMode();
    }

    @SimpleProperty(description = "Returns whether the WebView should enable support for the 'viewport' HTML meta tag or should use a wide viewport.")
    public boolean UseWideViewPort() {
        return webView.getSettings().getUseWideViewPort();
    }

    @SimpleProperty(description = "Tells the WebView to enable JavaScript execution.")
    public void EnableJS(boolean js) {
        webView.getSettings().setJavaScriptEnabled(js);
    }

    @SimpleProperty(description = "Returns whether webview supports JavaScript execution")
    public boolean EnableJS() {
        return webView.getSettings().getJavaScriptEnabled();
    }

    @SimpleProperty(description = "Whether or not to give the application permission to use the Javascript geolocation API")
    public void UsesLocation(boolean uses) {
        webView.getSettings().setGeolocationEnabled(uses);
    }

    @SimpleProperty(description = "Returns whether webview will prompt for permission and raise 'OnPermissionRequest' event or not")
    public boolean PromptForPermission() {
        return prompt;
    }

    @SimpleProperty(description = "Whether to display horizonatal and vertical scrollbars or not")
    public void ScrollBar(boolean bool) {
        webView.setVerticalScrollBarEnabled(bool);
        webView.setHorizontalScrollBarEnabled(bool);
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Sets whether webview will prompt for permission and raise 'OnPermissionRequest' event or not else assume permission is granted.")
    public void PromptForPermission(boolean pr) {
        prompt = pr;
    }

    @SimpleProperty(description = "Sets background color of webview")
    public void BackgroundColor(int bgColor) {
        webView.setBackgroundColor(bgColor);
    }

    @SimpleEvent(description = "When the JavaScript calls AppInventor.setWebViewString this event is run.")
    public void WebViewStringChanged(String value) {
        EventDispatcher.dispatchEvent(this, "WebViewStringChanged", value);
    }

    @SimpleFunction(description = "Stops the current load.")
    public void StopLoading() {
        webView.stopLoading();
    }

    @SimpleFunction(description = "Reloads the current URL.")
    public void Reload() {
        CancelJsRequests();
        webView.reload();
    }

    @SimpleFunction(description = "Loads the given data into this WebView using a 'data' scheme URL.")
    public void LoadHtml(String html) {
        CancelJsRequests();
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    @SimpleFunction(description = "Gets whether this WebView has a back history item")
    public boolean CanGoBack() {
        return webView.canGoBack();
    }

    @SimpleFunction(description = "Gets whether this WebView has a forward history item.")
    public boolean CanGoForward() {
        return webView.canGoForward();
    }

    @SimpleFunction(description = "Removes all cookies and raises 'CookiesRemoved' event")
    public void ClearCookies() {
        cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean aBoolean) {
                CookiesRemoved(aBoolean);
            }
        });
        cookieManager.flush();
    }

    @SimpleFunction()
    public void RegisterScreenForShortcut(String screenName) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        String replaceAll = this.form.getClass().getName().replace(this.form.getClass().getSimpleName(), screenName);
        prefs.edit().putString(Constants.KEY_SCREEN_NAME, replaceAll).apply();
    }

    @SuppressWarnings("deprecation")
    @SimpleFunction(description = "Creates a shortcut of given website on home screen")
    public void CreateShortcut(String url, String iconPath, String title) {
        try {
            Bitmap img = MediaUtil.getBitmapDrawable(form, iconPath).getBitmap();
            if (img != null) {
                String screen = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
                        .getString(Constants.KEY_SCREEN_NAME, "");
                String pkg = context.getPackageName();
                Intent intent = screen.isEmpty() ? context.getPackageManager().getLaunchIntentForPackage(pkg)
                        : new Intent();
                if (!screen.isEmpty()) {
                    intent.setClassName(context, screen);
                }
                List<String> startValue = new ArrayList<>();
                startValue.add(url);
                startValue.add("2");
                intent.putExtra("APP_INVENTOR_START", JsonUtil.getJsonRepresentation(startValue));
                intent.setPackage(null);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    Intent installer = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                    installer.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                    installer.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
                    installer.putExtra(Intent.EXTRA_SHORTCUT_ICON, img);
                    installer.putExtra("duplicate", false);
                    context.sendBroadcast(installer);
                } else {
                    ShortcutManager shortcutManager = (ShortcutManager) context
                            .getSystemService(Context.SHORTCUT_SERVICE);
                    if (shortcutManager.isRequestPinShortcutSupported()) {
                        ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, title)
                                .setShortLabel(title)
                                .setIcon(Icon.createWithBitmap(img))
                                .setIntent(intent)
                                .build();
                        Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(shortcutInfo);
                        PendingIntent successCallback = PendingIntent.getBroadcast(context, 0,
                                pinnedShortcutCallbackIntent, 0);
                        shortcutManager.requestPinShortcut(shortcutInfo, successCallback.getIntentSender());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SimpleEvent(description = "Event raised after 'ClearCokies' method with result")
    public void CookiesRemoved(boolean successful) {
        EventDispatcher.dispatchEvent(this, "CookiesRemoved", successful);
    }

    @SimpleFunction(description = "Clears the resource cache.")
    public void ClearCache() {
        webView.clearCache(true);
    }

    @SimpleFunction(description = "Tells this WebView to clear its internal back/forward list.")
    public void ClearInternalHistory() {
        webView.clearHistory();
    }

    @SimpleFunction(description = "Loads requested url in given webview")
    public void LoadInNewWindow(int id) {
        if (wv.containsKey(id) && resultObj != null) {
            WebView w = wv.get(id);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultObj.obj;
            transport.setWebView(w);
            resultObj.sendToTarget();
            resultObj = null;
        } else if (resultObj != null) {
            try {
                resultObj.sendToTarget();
            } catch (Exception ignored) {
            }
        }
    }

    @SimpleFunction(description = "Performs zoom in in the WebView")
    public void ZoomIn() {
        webView.zoomIn();
    }

    @SimpleFunction(description = "Performs zoom out in the WebView")
    public void ZoomOut() {
        webView.zoomOut();
    }

    @SimpleFunction(description = "Scrolls the contents of the WebView down by half the page size")
    public void PageDown(boolean bottom) {
        webView.pageDown(bottom);
    }

    @SimpleFunction(description = "Scrolls the contents of the WebView up by half the page size")
    public void PageUp(boolean top) {
        webView.pageUp(top);
    }

    @SimpleFunction(description = "Performs a zoom operation in the WebView by given zoom percent")
    public void ZoomBy(int zoomP) {
        webView.zoomBy(zoomP);
    }

    @SimpleFunction(description = "Returns current id")
    public int CurrentId() {
        return iD;
    }

    @SimpleFunction(description = "Goes back in the history of this WebView.")
    public void GoBack() {
        if (CanGoBack()) {
            webView.goBack();
        }
    }

    @SimpleFunction(description = "Goes forward in the history of this WebView.")
    public void GoForward() {
        if (CanGoForward()) {
            webView.goForward();
        }
    }

    @SimpleFunction(description = "Destroys the webview and removes it completely from view system")
    public void RemoveWebView(final int id) {
        if (wv.containsKey(id)) {
            WebView w = wv.get(id);
            ViewGroup parent = (ViewGroup) w.getParent();
            if (parent != null) {
                parent.removeView(w);
            }
            w.destroy();
            wv.remove(id);
            iD = Constants.DEFAULT_WEBVIEW_ID;
        }
    }

    @SimpleFunction(description = "Gets whether the page can go back or forward the given number of steps.")
    public boolean CanGoBackOrForward(int steps) {
        return webView.canGoBackOrForward(steps);
    }

    @SimpleFunction(description = "Goes to the history item that is the number of steps away from the current item. Steps is negative if backward and positive if forward.")
    public void GoBackOrForward(int steps) {
        if (CanGoBackOrForward(steps)) {
            webView.goBackOrForward(steps);
        }
    }

    @SimpleFunction(description = "Loads the given URL.")
    public void GoToUrl(String url) {
        CancelJsRequests();
        webView.loadUrl(url);
    }

    @SimpleFunction(description = "Loads the given URL.")
    public void GoToUrl2(int id, String url) {
        CancelJsRequests();
        wv.get(id).loadUrl(url);
    }

    @SimpleFunction(description = "Loads the URL with postData using 'POST' method into active WebView.")
    public void PostData(String url, String data) throws UnsupportedEncodingException {
        webView.postUrl(url, data.getBytes("UTF-8"));
    }

    @SimpleFunction(description = "Does a best-effort attempt to pause any processing that can be paused safely, such as animations and geolocation. Note that this call does not pause JavaScript.")
    public void PauseWebView(int id) {
        wv.get(id).onPause();
    }

    @SimpleFunction(description = "Resumes the previously paused WebView.")
    public void ResumeWebView(int id) {
        wv.get(id).onResume();
    }

    @SimpleFunction(description = "Gets the progress for the given webview")
    public int GetProgress(int id) {
        return wv.get(id).getProgress();
    }

    @SimpleEvent(description = "Event triggered when a window needs to be closed")
    public void OnCloseWindowRequest(int id) {
        EventDispatcher.dispatchEvent(this, "OnCloseWindowRequest", id);
    }

    @SimpleEvent(description = "Event raised when page loading has finished.")
    public void PageLoaded(int id) {
        EventDispatcher.dispatchEvent(this, "PageLoaded", id);
    }

    @SimpleEvent(description = "Event raised when downloading is needed.")
    public void OnDownloadNeeded(int id, String url, String contentDisposition, String mimeType, long size) {
        EventDispatcher.dispatchEvent(this, "OnDownloadNeeded", id, url, contentDisposition, mimeType, size);
    }

    @SimpleEvent(description = "Event raised when page loading progress has changed.")
    public void OnProgressChanged(int id, int progress) {
        EventDispatcher.dispatchEvent(this, "OnProgressChanged", id, progress);
    }

    @SimpleEvent(description = "Event raised after getting console message.")
    public void OnConsoleMessage(String message, int lineNumber, String sourceID, String level) {
        EventDispatcher.dispatchEvent(this, "OnConsoleMessage", message, lineNumber, sourceID, level);
    }

    @SimpleFunction(description = "Asynchronously evaluates JavaScript in the context of the currently displayed page.")
    public void EvaluateJavaScript(String script) {
        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                AfterJavaScriptEvaluated(s);
            }
        });
    }

    @SimpleFunction(description = "Get internal history of given webview.")
    public List<String> GetInternalHistory(int id) {
        List<String> history = new ArrayList<>();
        if (wv.containsKey(id)) {
            WebBackForwardList webBackForwardList = wv.get(id).copyBackForwardList();
            for (int i = 0; i < webBackForwardList.getSize(); ++i) {
                WebHistoryItem webHistoryItem = webBackForwardList.getItemAtIndex(i);
                history.add(webHistoryItem.getUrl());
            }
        }
        return history;
    }

    @SimpleFunction(description = "Loads the given URL with the specified additional HTTP headers defined is list of lists.")
    public void LoadWithHeaders(String url, YailDictionary headers) {
        if (!headers.isEmpty()) {
            Map<String, String> optionsMap = new HashMap<>();
            for (Object key : headers.keySet()) {
                optionsMap.put((String) key, (String) headers.get(key));
            }
            webView.loadUrl(url, optionsMap);
        } else {
            GoToUrl(url);
        }
    }

    @SimpleFunction(description = "Saves the current site as a web archive")
    public void SaveArchive(String dir) {
        webView.saveWebArchive(dir, true, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                if (s == null) {
                    AfterArchiveSaved(false, "");
                } else {
                    AfterArchiveSaved(true, s);
                }
            }
        });
    }

    @SimpleEvent(description = "Event raised after 'SaveArchive' method.If 'success' is true then returns file path else empty string.")
    public void AfterArchiveSaved(boolean success, String filePath) {
        EventDispatcher.dispatchEvent(this, "AfterArchiveSaved", success, filePath);
    }

    @SimpleEvent(description = "Event raised after evaluating Js and returns result.")
    public void AfterJavaScriptEvaluated(String result) {
        EventDispatcher.dispatchEvent(this, "AfterJavaScriptEvaluated", result);
    }

    @SimpleEvent(description = "Event raised when webview gets scrolled")
    public void OnScrollChanged(int id, int scrollX, int scrollY, int oldScrollX, int oldScrollY, boolean canGoLeft,
            boolean canGoRight) {
        EventDispatcher.dispatchEvent(this, "OnScrollChanged", id, scrollX, scrollY, oldScrollX, oldScrollY, canGoLeft,
                canGoRight);
    }

    @SimpleFunction(description = "Clears the highlighting surrounding text matches.")
    public void ClearMatches() {
        webView.clearMatches();
    }

    @SimpleEvent(description = "Event raised when something is long clicked in webview with item(image,string,empty,etc) and type(item type like 0,1,8,etc)")
    public void LongClicked(int id, String item, String secondaryUrl, int type) {
        EventDispatcher.dispatchEvent(this, "LongClicked", id, item, secondaryUrl, type);
    }

    @SimpleFunction(description = "Scrolls the webview to given position")
    public void ScrollTo(final int x, final int y) {
        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.scrollTo(p2d(x), p2d(y));
            }
        }, 300);
    }

    @SimpleFunction(description = "Return the scrolled left position of the webview")
    public int GetScrollX() {
        return d2p(webView.getScrollX());
    }

    @SimpleFunction(description = "Return the scrolled top position of the webview")
    public int GetScrollY() {
        return d2p(webView.getScrollY());
    }

    @SimpleEvent(description = "Event raised when any error is received during loading url and returns message,error code and failing url")
    public void OnErrorReceived(int id, String message, int errorCode, String url) {
        EventDispatcher.dispatchEvent(this, "OnErrorReceived", id, message, errorCode, url);
    }

    @SimpleEvent(description = "Event raised when file uploading is needed")
    public void FileUploadNeeded(int id, String mimeType, boolean isCaptureEnabled) {
        EventDispatcher.dispatchEvent(this, "FileUploadNeeded", id, mimeType, isCaptureEnabled);
    }

    @SimpleFunction(description = "Uploads the given file from content uri.Use empty string to cancel the upload request.")
    public void UploadFile(String contentUri) {
        if (mFilePathCallback != null) {
            if (contentUri.isEmpty()) {
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
            } else {
                String[] str = contentUri.split(",");
                if (str.length > 1) {
                    Uri[] uris = new Uri[str.length];
                    int i = 0;
                    for (String u : str) {
                        uris[i] = Uri.parse(u);
                        i++;
                    }
                    mFilePathCallback.onReceiveValue(uris);
                } else {
                    mFilePathCallback.onReceiveValue(new Uri[] { Uri.parse(contentUri) });
                }
                mFilePathCallback = null;
            }
        }
    }

    @SimpleEvent(description = "Event raised when resubmission of form is needed")
    public void OnFormResubmission(int id) {
        EventDispatcher.dispatchEvent(this, "OnFormResubmission", id);
    }

    @SimpleFunction(description = "Whether to resubmit form or not.")
    public void ResubmitForm(boolean reSubmit) {
        if (reSend != null && dontSend != null) {
            if (reSubmit) {
                reSend.sendToTarget();
            } else {
                dontSend.sendToTarget();
            }
            reSend = null;
            dontSend = null;
        }
    }

    @SimpleEvent(description = "Event raised when new window is requested by webview with boolean 'isDialog' and 'isPopup'")
    public void OnNewWindowRequest(int id, String url, boolean isDialog, boolean isPopup) {
        EventDispatcher.dispatchEvent(this, "OnNewWindowRequest", id, url, isDialog, isPopup);
    }

    @SimpleEvent(description = "Event raised when current page enters in full screen mode")
    public void OnShowCustomView() {
        EventDispatcher.dispatchEvent(this, "OnShowCustomView");
    }

    @SimpleEvent(description = "Event raised when current page exits from full screen mode")
    public void OnHideCustomView() {
        EventDispatcher.dispatchEvent(this, "OnHideCustomView");
    }

    @SimpleFunction(description = "Gets height of HTML content")
    public int ContentHeight() {
        return d2p(webView.getContentHeight());
    }

    @SimpleFunction(description = "Grants given permissions to webview.Use empty list to deny the request.")
    public void GrantPermission(final String permissions) {
        if (permissionRequest != null) {
            if (permissions.isEmpty()) {
                permissionRequest.deny();
            } else {
                // lets just skip this part :)
                /*
                 * String[] str = permissions.split(",");
                 * if (str.length == permissionRequest.getResources()) {
                 * permissionRequest.grant(str);
                 * }
                 */
                permissionRequest.grant(permissionRequest.getResources());
            }
            permissionRequest = null;
        }
    }

    @SimpleEvent(description = "Event raised after getting SSL certificate of current displayed url/website with boolean 'isSecure' and Strings 'issuedBy','issuedTo' and 'validTill'.If 'isSecure' is false and other values are empty then assume that website is not secure")
    public void GotCertificate(boolean isSecure, String issuedBy, String issuedTo, String validTill) {
        EventDispatcher.dispatchEvent(this, "GotCertificate", isSecure, issuedBy, issuedTo, validTill);
    }

    @SimpleFunction(description = "Gets the SSL certificate for the main top-level page and raises 'GotCertificate' event")
    public void GetSslCertificate() {
        SslCertificate certificate = webView.getCertificate();
        if (certificate != null) {
            GotCertificate(true, certificate.getIssuedBy().getDName(), certificate.getIssuedTo().getDName(),
                    certificate.getValidNotAfterDate().toString());
        } else {
            GotCertificate(false, "", "", "");
        }
    }

    @SimpleFunction(description = "Sets cookies for given url")
    public void SetCookies(String url, String cookieString) {
        try {
            CookieManager.getInstance().setCookie(url, cookieString);
            CookieManager.getInstance().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SimpleEvent(description = "Event raised when Js have to show an alert to user")
    public void OnJsAlert(int id, String url, String message) {
        EventDispatcher.dispatchEvent(this, "OnJsAlert", id, url, message);
    }

    @SimpleEvent(description = "Tells to display a confirm dialog to the user.")
    public void OnJsConfirm(int id, String url, String message) {
        EventDispatcher.dispatchEvent(this, "OnJsConfirm", id, url, message);
    }

    @SimpleEvent(description = "Event raised when JavaScript needs input from user")
    public void OnJsPrompt(int id, String url, String message, String defaultValue) {
        EventDispatcher.dispatchEvent(this, "OnJsPrompt", id, url, message, defaultValue);
    }

    @SimpleFunction(description = "Dismiss previously requested Js alert")
    public void DismissJsAlert() {
        if (jsAlert != null) {
            jsAlert.cancel();
            jsAlert = null;
        }
    }

    @SimpleFunction(description = "Inputs a confirmation response to Js")
    public void ContinueJs(String input) {
        if (jsPromptResult != null) {
            jsPromptResult.confirm(input);
            jsPromptResult = null;
        }
    }

    @SimpleFunction(description = "Whether to proceed JavaScript originated request")
    public void ConfirmJs(boolean confirm) {
        if (jsResult != null) {
            if (confirm) {
                jsResult.confirm();
            } else {
                jsResult.cancel();
            }
            jsResult = null;
        }
    }

    @SimpleEvent(description = "Notifies that the WebView received an HTTP authentication request.")
    public void OnReceivedHttpAuthRequest(int id, String host, String realm) {
        EventDispatcher.dispatchEvent(this, "OnReceivedHttpAuthRequest", id, host, realm);
    }

    @SimpleEvent(description = "Event indicating that page loading has started in web view.")
    public void PageStarted(int id, String url) {
        EventDispatcher.dispatchEvent(this, "PageStarted", id, url);
    }

    @SimpleFunction(description = "Instructs the WebView to proceed with the authentication with the given credentials.If both parameters are empty then it will cancel the request.")
    public void ProceedHttpAuthRequest(String username, String password) {
        if (httpAuthHandler != null) {
            if (username.isEmpty() && password.isEmpty()) {
                httpAuthHandler.cancel();
            } else {
                httpAuthHandler.proceed(username, password);
            }
            httpAuthHandler = null;
        }
    }

    @SimpleEvent(description = "Event raised after 'Find' method with int 'activeMatchOrdinal','numberOfMatches' and 'isDoneCounting'")
    public void FindResultReceived(int id, int activeMatchOrdinal, int numberOfMatches, boolean isDoneCounting) {
        EventDispatcher.dispatchEvent(this, "FindResultReceived", id, activeMatchOrdinal, numberOfMatches,
                isDoneCounting);
    }

    @SimpleFunction(description = "Clear all location preferences.")
    public void ClearLocation() {
        GeolocationPermissions.getInstance().clearAll();
    }

    @SimpleFunction(description = "Finds all instances of find on the page and highlights them, asynchronously. Successive calls to this will cancel any pending searches.")
    public void Find(String string) {
        webView.findAllAsync(string);
    }

    @SimpleFunction(description = "Get cookies for specific url")
    public String GetCookies(String url) {
        String cookies = CookieManager.getInstance().getCookie(url);
        return cookies != null ? cookies : "";
    }

    @SimpleFunction(description = "Invokes the graphical zoom picker widget for this WebView. This will result in the zoom widget appearing on the screen to control the zoom level of this WebView.Note that it does not checks whether zoom is enabled or not.")
    public void InvokeZoomPicker() {
        webView.invokeZoomPicker();
    }

    @SimpleFunction(description = "Highlights and scrolls to the next match if 'forward' is true else scrolls to previous match.")
    public void FindNext(boolean forward) {
        webView.findNext(forward);
    }

    @SimpleEvent(description = "Event raised when a website asks for specific permission(s) in list format.")
    public void OnPermissionRequest(List<String> permissionsList) {
        EventDispatcher.dispatchEvent(this, "OnPermissionRequest", permissionsList);
    }

    @SimpleEvent(description = "Event raised when page asks for location access. Developer must handle/show dialog from him/herself.")
    public void OnGeolocationRequested(String origin) {
        EventDispatcher.dispatchEvent(this, "OnGeolocationRequested", origin);
    }

    @SimpleFunction()
    public void AllowGeolocationAccess(boolean allow, boolean remember) {
        if (theCallback != null) {
            theCallback.invoke(theOrigin, allow, remember);
            theCallback = null;
            theOrigin = "";
        }
    }

    @SimpleEvent()
    public void OnReceivedSslError(int errorCode) {
        EventDispatcher.dispatchEvent(this, "OnReceivedSslError", errorCode);
    }

    @SimpleFunction()
    public void ProceedSslError(boolean proceed) {
        if (sslHandler != null) {
            if (proceed) {
                sslHandler.proceed();
            } else {
                sslHandler.cancel();
            }
            sslHandler = null;
        }
    }
    // Deprecated: CustomWebView now supports window.print() through
    // JavascriptInterface
    // @SimpleFunction(description = "Prints the content of webview with given
    // document name")
    // public void PrintWebContent(String documentName) throws Exception {
    // PrintManager printManager = (PrintManager)
    // context.getSystemService(Context.PRINT_SERVICE);
    // if (documentName.isEmpty()) {
    // jobName = webView.getTitle() + "_Document";
    // } else {
    // jobName = documentName;
    // }
    // PrintDocumentAdapter printAdapter = new
    // PrintDocumentAdapterWrapper(webView.createPrintDocumentAdapter(jobName));
    // if (printManager != null) {
    // printJob = printManager.print(jobName, printAdapter,
    // new PrintAttributes.Builder().build());
    // }
    // }

    @SimpleFunction(description = "Hides previously shown custom view")
    public void HideCustomView() {
        webView.getWebChromeClient().onHideCustomView();
    }

    private void CancelJsRequests() {
        if (jsAlert != null) {
            jsAlert.cancel();
            jsAlert = null;
        }
        if (jsResult != null) {
            jsResult.cancel();
            jsResult = null;
        }
        if (jsPromptResult != null) {
            jsPromptResult.cancel();
            jsPromptResult = null;
        }
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
            mFilePathCallback = null;
        }
    }

    @SimpleFunction(description = "Clears the form data of the webview <br> Added by Xoma")
    public void ClearFormData(final int id) {
        final WebView view = wv.get(id);
        if (view != null) {
            view.clearFormData();
        }
    }

    @SimpleFunction(description = "Registers to open specified link in associated external app(s)")
    public void RegisterDeepLink(String scheme) {
        customDeepLink.add(scheme);
    }

    @SimpleProperty(description = "Sets whether vibration feedback enabled on long click ")
    public void VibrationEnabled(boolean v) {
        webView.setHapticFeedbackEnabled(v);
    }

    @SimpleProperty(description = "Returns whether vibration feedback enabled on long click ")
    public boolean VibrationEnabled() {
        return webView.isHapticFeedbackEnabled();
    }

    @SimpleProperty()
    public void Scrollable(boolean b) {
        isScrollEnabled = b;
    }

    @SimpleEvent(description = "Event raised when webview is swiped")
    public void Swiped(int id, int direction) {
        EventDispatcher.dispatchEvent(this, "Swiped", id, direction);
    }

    @Override
    public void onSwipe(int i, int i1) {
        Swiped(i, i1);
    }

}