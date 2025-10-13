package com.sunny.CustomWebView;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.*;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Base64;
import android.view.*;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.appinventor.components.runtime.util.YailDictionary;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@DesignerComponent(
        version = 13,
        versionName = "13",
        description = "An extended form of Web Viewer <br> Developed by Sunny Gupta",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://i.ibb.co/4wLNN1Hs/ktvu4bapylsvnykoyhdm-c-fill-w-20-h-20.png",
        helpUrl = "https://github.com/vknow360/CustomWebView",
        androidMinSdk = 21
)
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
    private PrintJob printJob;
    private final CookieManager cookieManager;
    private JsResult jsResult;
    private JsResult jsAlert;
    private HttpAuthHandler httpAuthHandler;
    private boolean deepLinks = false;
    private String jobName = "";
    private boolean isLoading = false;
    private final HashMap<Integer, WView> wv = new HashMap<>();
    private boolean blockAds = false;
    private String AD_HOSTS = "";
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
        wvInterface = new WebViewInterface();
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
    @SimpleFunction()
    public boolean IsDarkModeSupported(){
        return WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) && WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY);
    }
    @SimpleFunction()
    public void SetDarkMode(boolean enable){
        WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.getSettings(),enable);
        WebSettingsCompat.setForceDarkStrategy(webView.getSettings(), WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
        WebSettingsCompat.setForceDark(webView.getSettings(),
                enable ? WebSettingsCompat.FORCE_DARK_ON : WebSettingsCompat.FORCE_DARK_OFF);
    }

    @SimpleFunction(description = "Creates the webview in given arrangement with id")
    public void CreateWebView(Object container, final int id) {
        if (!(wv.containsKey(id) && container == null)) {
            final View view = ((AndroidViewComponent)container).getView();
            if (!wv.containsKey(id)) {
                WView w = new WView(id, context, this);
                resetWebView(w);
                ViewGroup vg = (ViewGroup) view;
                vg.addView(w, new FrameLayout.LayoutParams(-1, -1));
                wv.put(id, w);
            }
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
        web.setWebViewClient(new WebClient());
        web.setWebChromeClient(new ChromeClient());
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
		web.getSettings().setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW );
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
                FindResultReceived(getIndex((web)), i, i1, b);
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
                }else {
                    return(event.getAction() == MotionEvent.ACTION_MOVE);
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
                        LongClicked(getIndex(web), item, str, type);
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
                    OnScrollChanged(getIndex(web), i, i1, i2, i3, web.canScrollHorizontally(-1), web.canScrollHorizontally(1));
                }
            });
        }else {
            web.setScrollChangeListener(new WView.OnScrollChangeListener(){
                @Override
                public void onScrollChange(Context v, int i, int i1, int i2, int i3) {
                    OnScrollChanged(getIndex(web), i, i1, i2, i3, web.canScrollHorizontally(-1), web.canScrollHorizontally(1));
                }
            });
        }
    }

    @SimpleFunction()
    public void DownloadBlob(String url, String mimeType,String fileName, String downloadDir){
        webView.loadUrl(wvInterface.getBase64StringFromBlobUrl(url,mimeType,fileName,downloadDir));
    }
    public void GotBlobBase64(final String base64String,final String fileName,final String downloadDir) {
        AsynchUtil.runAsynchronously(new Runnable() {
            @Override
            public void run() {
                final File dFile;
                if (downloadDir.startsWith("~")) {
                    dFile = new File(context.getExternalFilesDir(downloadDir.substring(1)),fileName);
                } else {
                    dFile = new File(Environment.getExternalStoragePublicDirectory(downloadDir),fileName);
                }
                if (!dFile.getParentFile().exists()){
                    dFile.getParentFile().exists();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(dFile);
                    ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(base64String.split(",")[1],Base64.DEFAULT));
                    byte[] bytes = new byte[1024];
                    int c;
                    while((c = bis.read(bytes)) != -1){
                        fos.write(bytes,0,c);
                    }
                    bis.close();
                    fos.close();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BlobFileDownloaded(dFile.getPath());
                        }
                    });
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        });
    }

    @SimpleEvent()
    public void BlobFileDownloaded(String filePath){
        EventDispatcher.dispatchEvent(this,"BlobFileDownloaded",filePath);
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
        return wvInterface.webViewString;
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

    @SimpleProperty(description = "Determines whether to follow links when they are tapped in the WebViewer." + "If you follow links, you can use GoBack and GoForward to navigate the browser history")
    public boolean FollowLinks() {
        return followLinks;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets whether to enable deep links or not i.e. tel: , whatsapp: , sms: , etc.")
    public void DeepLinks(boolean d) {
        deepLinks = d;
    }

    @SimpleProperty(description = "Returns whether deep links are enabled or not")
    public boolean DeepLinks() {
        return deepLinks;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets whether to follow links or not")
    public void FollowLinks(boolean follow) {
        followLinks = follow;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets whether to block ads or not")
    public void BlockAds(boolean block) {
        blockAds = block;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets the ad hosts which will be blocked")
    public void AdHosts(String hosts) {
        AD_HOSTS = hosts;
    }

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
		if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_CACHE_MODE)){
            ServiceWorkerControllerCompat.getInstance().getServiceWorkerWebSettings().setCacheMode(mode);

        }
        webView.getSettings().setCacheMode(mode);
    }

    @SimpleProperty(description = "Gets cache mode of active webview")
    public int CacheMode() {
        return webView.getSettings().getCacheMode();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets whether the WebView should support zooming using its on-screen zoom controls and gestures")
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
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets whether the WebView should display on-screen zoom controls")
    public void DisplayZoom(boolean bool) {
        displayZoom = bool;
    }

    @SimpleProperty(description = "Gets whether the WebView should display on-screen zoom controls")
    public boolean DisplayZoom() {
        return displayZoom;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "100")
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets the zoom of the page in percent. The default is 100")
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

    //@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
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
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_FILE_ACCESS)){
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
    @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "Sets whether webview will prompt for permission and raise 'OnPermissionRequest' event or not else assume permission is granted.")
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

    @SimpleFunction(description = "Creates a shortcut of given website on home screen")
    public void CreateShortcut(String url, String iconPath, String title) {
        try {
            Bitmap img = MediaUtil.getBitmapDrawable(form,iconPath).getBitmap();
            if (img != null) {
                String screen = context.getSharedPreferences("TinyDB1", Context.MODE_PRIVATE).getString("ssn", "");
                String pkg = context.getPackageName();
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);
                String clsName = Objects.requireNonNull(context.getPackageManager().resolveActivity(context.getPackageManager().getLaunchIntentForPackage(pkg), 0)).activityInfo.name.replaceAll("Screen1", screen.length() == 0 ? "Screen1" : JsonUtil.getObjectFromJson(screen, true).toString());
                intent.setClassName(context, clsName);
                List<String> startValue = new ArrayList<>();
                startValue.add(url);
                startValue.add("2");
                intent.putExtra("APP_INVENTOR_START", JsonUtil.getJsonRepresentation(startValue));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    Intent installer = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
                    installer.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                    installer.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
                    installer.putExtra(Intent.EXTRA_SHORTCUT_ICON, img);
                    installer.putExtra("duplicate", false);
                    context.sendBroadcast(installer);
                } else {
                    ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
                    if (shortcutManager.isRequestPinShortcutSupported()) {
                        ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, title)
                                .setShortLabel(title)
                                .setIcon(Icon.createWithBitmap(img))
                                .setIntent(intent)
                                .build();
                        Intent pinnedShortcutCallbackIntent =
                                shortcutManager.createShortcutResultIntent(shortcutInfo);
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
        }else if (resultObj != null){
            try{
                resultObj.sendToTarget();
            }catch (Exception ignored){}
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
            ((FrameLayout) w.getParent()).removeView(w);
            w.destroy();
            wv.remove(id);
            iD = 0;
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
    public void GoToUrl2(int id,String url) {
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
    public void OnConsoleMessage(String message, int lineNumber, int sourceID, String level) {
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
    public void OnScrollChanged(int id, int scrollX, int scrollY, int oldScrollX, int oldScrollY, boolean canGoLeft, boolean canGoRight) {
        EventDispatcher.dispatchEvent(this, "OnScrollChanged", id, scrollX, scrollY, oldScrollX, oldScrollY, canGoLeft, canGoRight);
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

    private class WebClient extends WebViewClient {
        private static final String ASSET_PREFIX = "file:///appinventor_asset/";

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http")) {
                return !followLinks;
            } else {
                if (deepLinks) {
                    return DeepLinkParser(url);
                }
            }
            return false;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url1 = request.getUrl().toString();
            if (url1.startsWith("http")) {
                return !followLinks;
            } else {
                if (deepLinks) {
                    return DeepLinkParser(url1);
                }
            }
            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            //RequestIntercepted(url, YailDictionary.makeDictionary());
            if (url.startsWith("http://localhost/") || url.startsWith(ASSET_PREFIX)) {
                if (blockAds) {
                    AdBlocker ab = new AdBlocker();
                    boolean ad = ab.isAd(url);
                    return ad ? ab.createEmptyResource() :
                            super.shouldInterceptRequest(view, url);
                }
                return handleAppRequest(url);
            }
            return super.shouldInterceptRequest(view,url);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
            /*
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RequestIntercepted(url, YailDictionary.makeDictionary((Map<Object, Object>) (Map) request.getRequestHeaders()));
                }
            });
             */
            if ("localhost".equals(request.getUrl().getAuthority())
                    || request.getUrl().toString().startsWith(ASSET_PREFIX)) {
                if (blockAds) {
                    AdBlocker ab = new AdBlocker();
                    boolean ad = ab.isAdHost(request.getUrl().getHost());
                    return ad ? ab.createEmptyResource() :
                            super.shouldInterceptRequest(view,request);
                }
                return handleAppRequest(request.getUrl().toString());
            }
            return super.shouldInterceptRequest(view,request);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (wv.get(CurrentId()) == view) {
                if (isLoading) {
                    isLoading = false;
                    PageLoaded(getIndex(view));
                }
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            sslHandler = handler;
            OnReceivedSslError(error.getPrimaryError());
            /*if (ignoreSslErrors) {
                handler.proceed();
            } else {
                handler.cancel();
            }*/
        }

        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            dontSend = dontResend;
            reSend = resend;
            OnFormResubmission(getIndex(view));
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            OnErrorReceived(getIndex(view), description, errorCode, failingUrl);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            OnErrorReceived(getIndex(view), errorResponse.getReasonPhrase(), errorResponse.getStatusCode(), request.getUrl().toString());
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            OnErrorReceived(getIndex(view), error.getDescription().toString(), error.getErrorCode(), request.getUrl().toString());
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if (!isLoading) {
                PageStarted(getIndex(view), url);
                isLoading = true;
            }
        }

        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            httpAuthHandler = handler;
            OnReceivedHttpAuthRequest(getIndex(view), host, realm);
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
                stream = form.openAsset(path);
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
                if(str.length > 1){
                    Uri[] uris = new Uri[str.length];
                    int i=0;
                    for (String u : str) {
                        uris[i] = Uri.parse(u);
                        i++;
                    }
                    mFilePathCallback.onReceiveValue(uris);
                }else{
                    mFilePathCallback.onReceiveValue(new Uri[]{Uri.parse(contentUri)});
                }
                mFilePathCallback = null;
            }
        }
    }


    private class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (!prompt) {
                callback.invoke(origin, true, true);
            } else {
                theCallback = callback;
                theOrigin = origin;
                OnGeolocationRequested(origin);
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }
            OnShowCustomView();
            mCustomView = view;
            mOriginalOrientation = activity.getRequestedOrientation();
            mCustomViewCallback = callback;
            ((FrameLayout) activity.getWindow().getDecorView()).addView(mCustomView,
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                activity.getWindow().setDecorFitsSystemWindows(false);
                WindowInsetsController controller = activity.getWindow().getInsetsController();
                if(controller != null) {
                    mOriginalSystemUiVisibility = controller.getSystemBarsBehavior();
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }else {
                mOriginalSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
                activity.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            }*/
            mOriginalSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            OnShowCustomView();
        }

        @Override
        public void onHideCustomView() {
            ((FrameLayout) activity.getWindow().getDecorView()).removeView(mCustomView);
            mCustomView = null;
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                activity.getWindow().setDecorFitsSystemWindows(false);
                WindowInsetsController controller = activity.getWindow().getInsetsController();
                if(controller != null) {
                    controller.show(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(mOriginalSystemUiVisibility);
                }
            }else {
                activity.getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
            }*/
            activity.getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
            activity.setRequestedOrientation(mOriginalOrientation);
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
            OnHideCustomView();
        }

        @Override
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            FileUploadNeeded(getIndex(view), fileChooserParams.getAcceptTypes()[0], fileChooserParams.isCaptureEnabled());
            return FileAccess();
        }

        @Override
        public boolean onCreateWindow(WebView view,final boolean isDialog,final boolean isUserGesture, Message resultMsg) {
            if (SupportMultipleWindows()) {
                final int i = getIndex(view);
                resultObj = resultMsg;
                if (nWM.equalsIgnoreCase("LEGACY")) {
                    final WebView mWebView = new WebView(context);
                    mWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageStarted(WebView view, String url, Bitmap favicon) {
                            OnNewWindowRequest(i, url, isDialog, !isUserGesture);
                            mWebView.stopLoading();
                            mWebView.destroy();
                        }
                    });
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(mWebView);
                    resultMsg.sendToTarget();
                } else {
                    String str = "";
                    Message href = view.getHandler().obtainMessage();
                    view.requestFocusNodeHref(href);
                    String url = href.getData().getString("url");
                    if (url == null) {
                        if (webView.getHitTestResult().getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                            url = webView.getHitTestResult().getExtra();
                        }
                    }
                    if (url != null) {
                        str = url;
                    }
                    OnNewWindowRequest(i, str, isDialog, isUserGesture);
                }
            }
            return SupportMultipleWindows();
        }

        @Override
        public void onCloseWindow(WebView window) {
            OnCloseWindowRequest(getIndex(window));
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            OnProgressChanged(getIndex(view), newProgress);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            OnConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.lineNumber(), consoleMessage.messageLevel().toString());
            return true;
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            if (!prompt) {
                request.grant(request.getResources());
            } else {
                permissionRequest = request;
                String[] strings = request.getResources();
                List<String> permissions = Arrays.asList(strings);
                OnPermissionRequest(permissions);
            }
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            jsPromptResult = result;
            OnJsPrompt(getIndex(view), url, message, defaultValue);
            return EnableJS();
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            OnJsAlert(getIndex(view), url, message);
            jsAlert = result;
            return EnableJS();
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            jsResult = result;
            OnJsConfirm(getIndex(view), url, message);
            return EnableJS();
        }
    }

    private int getIndex(WebView view) {
        List<WView> w = new ArrayList<>(wv.values());
        return new ArrayList<>(wv.keySet()).get(w.indexOf(view));
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
                        /*String[] str = permissions.split(",");
                        if (str.length == permissionRequest.getResources()) {
                            permissionRequest.grant(str);
                        }*/
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
            GotCertificate(true, certificate.getIssuedBy().getDName(), certificate.getIssuedTo().getDName(), certificate.getValidNotAfterDate().toString());
        } else {
            GotCertificate(false, "", "", "");
        }
    }

    @SimpleFunction(description = "Sets cookies for given url")
    public void SetCookies(String url, String cookieString) {
        try {
            CookieManager.getInstance().setCookie(url, cookieString);
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
        EventDispatcher.dispatchEvent(this, "FindResultReceived", id, activeMatchOrdinal, numberOfMatches, isDoneCounting);
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

    public class WebViewInterface {
        String webViewString;

        WebViewInterface() {
            webViewString = "";
        }

        @JavascriptInterface
        public String getWebViewString() {
            return webViewString;
        }

        @JavascriptInterface
        public void setWebViewString(final String newString) {
            webViewString = newString;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    WebViewStringChanged(newString);
                }
            });
        }
        public void setWebViewStringFromBlocks(final String newString) {
            webViewString = newString;
        }

        @JavascriptInterface
        public void gotBase64FromBlobData(final String base64Data,String fileName,String downloadDir) {
            GotBlobBase64(base64Data,fileName,downloadDir);
        }
        public String getBase64StringFromBlobUrl(String blobUrl, String mimeType,String fileName,String downloadDir) {
            return "javascript: var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.setRequestHeader('Content-type','" + mimeType + ";charset=UTF-8');" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    console.log(this.status);"+
                    "    if (this.status == 200) {" +
                    "        var blobFile = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobFile);" +
                    "        reader.onloadend = function() {" +
                    "            var base64data = reader.result;" +
                    "            window.AppInventor.gotBase64FromBlobData(base64data,'"+fileName+"','"+downloadDir+"');" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();";
        }
    }

    @SimpleEvent(description = "Event raised when a website asks for specific permission(s) in list format.")
    public void OnPermissionRequest(List<String> permissionsList) {
        EventDispatcher.dispatchEvent(this, "OnPermissionRequest", permissionsList);
    }

    @SimpleEvent(description = "Event raised after getting previus print's result.")
    public void GotPrintResult(String printId, boolean isCompleted, boolean isFailed, boolean isBlocked) {
        EventDispatcher.dispatchEvent(this, "GotPrintResult", printId, isCompleted, isFailed, isBlocked);
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

    @SimpleFunction(description = "Prints the content of webview with given document name")
    public void PrintWebContent(String documentName) throws Exception {
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        if (documentName.isEmpty()) {
            jobName = webView.getTitle() + "_Document";
        } else {
            jobName = documentName;
        }
        PrintDocumentAdapter printAdapter = new PrintDocumentAdapterWrapper(webView.createPrintDocumentAdapter(jobName));
        if (printManager != null) {
            printJob = printManager.print(jobName, printAdapter,
                    new PrintAttributes.Builder().build());
        }
    }

    @SimpleFunction(description = "Hides previously shown custom view")
    public void HideCustomView() {
        webView.getWebChromeClient().onHideCustomView();
    }

    @SimpleFunction(description = "Restarts current/previous print job. You can request restart of a failed print job.")
    public void RestartPrinting() throws Exception {
        printJob.restart();
    }

    @SimpleFunction(description = "Cancels current print job. You can request cancellation of a queued, started, blocked, or failed print job.")
    public void CancelPrinting() throws Exception {
        printJob.cancel();
    }

    private void CancelJsRequests() {
        if (jsAlert != null) {
            jsAlert.cancel();
            jsAlert = null;
        } else if (jsResult != null) {
            jsResult.cancel();
            jsResult = null;
        } else if (jsPromptResult != null) {
            jsPromptResult.cancel();
            jsPromptResult = null;
        } else if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(null);
            mFilePathCallback = null;
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
                    webView.loadUrl(fallbackUrl);
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
        } else if (!customDeepLink.isEmpty() && customDeepLink.contains(url.split(":")[0])) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            activity.startActivity(intent);
            return true;
        }
        return false;
    }

    public class PrintDocumentAdapterWrapper extends PrintDocumentAdapter {

        private final PrintDocumentAdapter delegate;

        public PrintDocumentAdapterWrapper(PrintDocumentAdapter adapter) {
            super();
            this.delegate = adapter;
        }

        @Override
        public void onLayout(PrintAttributes printAttributes, PrintAttributes printAttributes1, CancellationSignal cancellationSignal, LayoutResultCallback layoutResultCallback, Bundle bundle) {
            delegate.onLayout(printAttributes, printAttributes1, cancellationSignal, layoutResultCallback, bundle);
        }

        @Override
        public void onWrite(PageRange[] pageRanges, ParcelFileDescriptor parcelFileDescriptor, CancellationSignal cancellationSignal, WriteResultCallback writeResultCallback) {
            delegate.onWrite(pageRanges, parcelFileDescriptor, cancellationSignal, writeResultCallback);
        }

        public void onFinish() {
            delegate.onFinish();
            GotPrintResult(jobName, printJob.isCompleted(), printJob.isFailed(), printJob.isBlocked());

        }
    }

    private class AdBlocker {
        private String getHost(String url){
            try {
                return new URL(url).getHost() != null ? new URL(url).getHost() : "";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
        private boolean isAd(String url) {
            try {
                return isAdHost(getHost(url));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private boolean isAdHost(String host) {
            if (webView.getUrl().contains(host)) {
                return false;
            } else {
                return AD_HOSTS.contains(host);
            }
        }

        private WebResourceResponse createEmptyResource() {
            return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
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
    public void Scrollable(boolean b){
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