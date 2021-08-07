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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.*;
import android.print.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.autofill.AutofillManager;
import android.webkit.*;
import android.widget.FrameLayout;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.runtime.util.JsonUtil;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@DesignerComponent(version = 11,
        versionName = "11",
        description = "An extended form of Web Viewer <br> Developed by Sunny Gupta",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png",
        helpUrl = "https://github.com/vknow360/CustomWebView",
        androidMinSdk = 21)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.WRITE_EXTERNAL_STORAGE,android.permission.ACCESS_DOWNLOAD_MANAGER,android.permission.ACCESS_FINE_LOCATION,android.permission.RECORD_AUDIO, android.permission.MODIFY_AUDIO_SETTINGS, android.permission.CAMERA,android.permission.VIBRATE,android.webkit.resource.VIDEO_CAPTURE,android.webkit.resource.AUDIO_CAPTURE,android.launcher.permission.INSTALL_SHORTCUT")
public final class CustomWebView extends AndroidNonvisibleComponent{
    public Activity activity;
    public WebView webView;
    public Context context;
    public boolean followLinks = true;
    public boolean prompt = true;
    public String UserAgent = "";
    public WebViewInterface wvInterface;
    public JsPromptResult jsPromptResult;
    private String MOBILE_USER_AGENT = "";
    private ValueCallback<Uri[]> mFilePathCallback;
    public Message dontSend;
    public Message reSend;
    public PermissionRequest permissionRequest;
    public PrintJob printJob;
    public CookieManager cookieManager;
    public JsResult jsResult;
    public JsResult jsAlert;
    public HttpAuthHandler httpAuthHandler;
    public boolean deepLinks = false;
    public String jobName = "";
    public boolean isLoading = false;
    public HashMap<Integer, WebView> wv = new HashMap<>();
    public boolean blockAds = false;
    public static List<String> AD_HOSTS = new ArrayList<>();
    public int iD = 0;
    public boolean desktopMode = false;
    public int zoomPercent = 100;
    public boolean zoomEnabled = true;
    public boolean displayZoom = true;
    public Message resultObj;
    public float deviceDensity;
    public GeolocationPermissions.Callback theCallback;
    public String theOrigin;
    public SslErrorHandler sslHandler;

    public CustomWebView(ComponentContainer container) {
        super(container.$form());
        activity = container.$context();
        context = activity;
        wvInterface = new WebViewInterface();
        cookieManager = CookieManager.getInstance();
        deviceDensity = container.$form().deviceDensity();
        webView = new WebView(context);
        resetWebView(webView);
    }

    public int d2p(int d) {
        return Math.round(d / deviceDensity);
    }

    public int p2d(int p) {
        return Math.round(p * deviceDensity);
    }

    @SimpleFunction(description = "Creates the webview in given arrangement with id")
    public void CreateWebView(HVArrangement container, final int id) {
        if (!(wv.containsKey(id) && container == null)) {
            final View v = container.getView();
            if (!wv.containsKey(id)) {
                WebView w = new WebView(context);
                resetWebView(w);
                FrameLayout frameLayout = (FrameLayout) v;
                frameLayout.addView(w, new FrameLayout.LayoutParams(-1, -1));
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

    public void resetWebView(final WebView web) {
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
        web.getSettings().setDatabaseEnabled(false);
        web.getSettings().setGeolocationEnabled(false);
        if (UserAgent.isEmpty()) {
            UserAgent = MOBILE_USER_AGENT;
        }
        web.getSettings().setUserAgentString(UserAgent);
        web.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                    OnDownloadNeeded(getIndex(web), s, s2, s3, l);
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
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        web.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
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
                    return webView.isLongClickable();
                }
                return false;
            }
        });
        web.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                OnScrollChanged(getIndex(web), i, i1, i2, i3, web.canScrollHorizontally(-1), web.canScrollHorizontally(1));
            }
        });
        // added in v11
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            web.getSettings().setSaveFormData(true);
        }else{
            AutofillManager autofillManager = context.getSystemService(AutofillManager.class);
            autofillManager.requestAutofill(webView);
        }
    }

    @SimpleFunction(description = "Returns a list of used ids")
    public List<Integer> GetIds() {
        return new ArrayList<>(wv.keySet());
    }

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Set webview string")
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

    @SimpleProperty(category = PropertyCategory.BEHAVIOR, description = "Get webview string")
    public String WebViewString() {
        return wvInterface.webViewString;
    }

    @SimpleProperty(description = "")
    public void ScrollBarStyle(int style) {
        webView.setScrollBarStyle(style);
    }

    @SimpleProperty(description = "")
    public int ScrollBarStyle() {
        return webView.getScrollBarStyle();
    }

    @SimpleProperty(description = "")
    public void OverScrollMode(int mode) {
        webView.setOverScrollMode(mode);
    }

    @SimpleProperty(description = "")
    public int OverScrollMode() {
        return webView.getOverScrollMode();
    }

    @SimpleProperty(description = "")
    public void LayerType(int type) {
        webView.setLayerType(type, null);
    }

    @SimpleProperty(description = "")
    public int LayerType() {
        return webView.getLayerType();
    }

    @SimpleProperty()
    public void RotationAngle(float rotation) {
        webView.setRotation(rotation);
    }

    @SimpleProperty()
    public float RotationAngle() {
        return webView.getRotation();
    }

    @SimpleProperty(description = "Get webview user agent", category = PropertyCategory.BEHAVIOR)
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

    @SimpleProperty(description = "Title of the page currently viewed", category = PropertyCategory.BEHAVIOR)
    public String CurrentPageTitle() {
        return (webView.getTitle() == null) ? "" : webView.getTitle();
    }

    @SimpleProperty(description = "Determines whether to follow links when they are tapped in the WebViewer." + "If you follow links, you can use GoBack and GoForward to navigate the browser history")
    public boolean FollowLinks() {
        return followLinks;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Sets whether to enable deep links or not i.e. tel: , whatsapp: , sms: , etc.")
    public void DeepLinks(boolean d) {
        deepLinks = d;
    }

    @SimpleProperty(description = "Returns whether deep links are enabled or not")
    public boolean DeepLinks() {
        return deepLinks;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Sets whether to follow links or not")
    public void FollowLinks(boolean follow) {
        followLinks = follow;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Sets whether to block ads or not")
    public void BlockAds(boolean block) {
        blockAds = block;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "Sets the ad hosts which will be blocked")
    public void AdHosts(String hosts) {
        AD_HOSTS.addAll(Arrays.asList(hosts.split(",")));
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
        webView.getSettings().setCacheMode(mode);
    }

    @SimpleProperty(description = "Gets cache mode of active webview")
    public int CacheMode() {
        return webView.getSettings().getCacheMode();
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Sets whether the WebView should support zooming using its on-screen zoom controls and gestures")
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
    @SimpleProperty(description = "Sets whether the WebView should display on-screen zoom controls")
    public void DisplayZoom(boolean bool) {
        displayZoom = bool;
    }

    @SimpleProperty(description = "Gets whether the WebView should display on-screen zoom controls")
    public boolean DisplayZoom() {
        return displayZoom;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "100")
    @SimpleProperty(description = "Sets the zoom of the page in percent. The default is 100")
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

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    @SimpleProperty(description = "Sets whether to enable text selection and context menu")
    public void LongClickable(boolean bool) {
        webView.setLongClickable(!bool);
    }

    @SimpleProperty(description = "Returns whether text selection and context menu are enabled or not")
    public boolean LongClickable() {
        return !webView.isLongClickable();
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

    /*@SimpleProperty(description = "Returns whether webview ignores SSL errors", category = PropertyCategory.BEHAVIOR)
    public boolean IgnoreSslErrors() {
        return ignoreSslErrors;
    }

    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "False")
    @SimpleProperty(description = "Determine whether or not to ignore SSL errors. Set to true to ignore " +
            "errors. Use this to accept self signed certificates from websites")
    public void IgnoreSslErrors(boolean ignore) {
        ignoreSslErrors = ignore;
    }*/

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
    @SimpleProperty(description = "Sets whether webview will prompt for permission and raise 'OnPermissionRequest' event or not else assume permission is granted.")
    public void PromptForPermission(boolean pr) {
        prompt = pr;
    }

    @SimpleProperty(description = "Sets background color of webview")
    public void BackgroundColor(int bgColor) {
        webView.setBackgroundColor(bgColor);
    }
    // added in v11
    @SimpleProperty(description = "Specifies whether webview should autofill saved credentials or not")
    public void Autofill(boolean enable){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            webView.getSettings().setSaveFormData(enable);
        }else{
            if (enable) {
                AutofillManager autofillManager = context.getSystemService(AutofillManager.class);
                autofillManager.requestAutofill(webView);
            }else {
                webView.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
            }
        }
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
        webView.loadData(html, "text/html", "UTF-8");
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
            Bitmap img = BitmapFactory.decodeFile(iconPath);
            if (img != null) {
                String screen = context.getSharedPreferences("TinyDB1", Context.MODE_PRIVATE).getString("ssn", "");
                String pkg = context.getPackageName();
                Intent intent = new Intent();
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

    @SimpleFunction(description = "Loads the URL with postData using 'POST' method into active WebView.")
    public void PostData(String url, String data) {
        webView.postUrl(url, data.getBytes(StandardCharsets.UTF_8));
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
    public void LoadWithHeaders(String url, List<List<String>> headers) {
        if (headers.size() != 0 && headers.get(0).size() == 2) {
            java.util.Map<String, String> header = new HashMap<String, String>();
            for (List<String> list : headers) {
                header.put(list.get(0), list.get(1));
            }
            webView.loadUrl(url, header);
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
    public class WebClient extends WebViewClient {
        public HashMap<String, Boolean> loadedUrls = new HashMap<>();
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
            if (blockAds) {
                boolean ad;
                AdBlocker ab = new AdBlocker();

                if (!loadedUrls.containsKey(url)) {
                    ad = ab.isAd(url);
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }
                return ad ? ab.createEmptyResource() :
                        null;
            } else {
                return null;
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (blockAds) {
                boolean ad;
                AdBlocker ab = new AdBlocker();
                String uri = request.getUrl().toString();
                if (!loadedUrls.containsKey(uri)) {
                    ad = ab.isAd(uri);
                    loadedUrls.put(uri, ad);
                } else {
                    ad = loadedUrls.get(uri);
                }
                return ad ? ab.createEmptyResource() :
                        null;
            } else {
                return null;
            }
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
                mFilePathCallback.onReceiveValue(new Uri[]{Uri.parse(contentUri)});
                mFilePathCallback = null;
            }
        }
    }

    public class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;
        private final int FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE;

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            if (!prompt) {
                callback.invoke(origin, true, true);
            }else {
                theCallback = callback;
                theOrigin = origin;
                OnGeolocationRequested(origin);
                /*
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setCancelable(false);
                alertDialog.setTitle("Permission Request");
                if (origin.equals("file://")) {
                    origin = "This Application";
                }
                alertDialog.setMessage(origin + " would like to access your location.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Allow",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                theCallback.invoke(theOrigin, true, true);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Refuse",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                theCallback.invoke(theOrigin, false, true);
                            }
                        });
                alertDialog.show();
                */
            }
        }

        @Override
        public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
            OnShowCustomView();
            if (mCustomView != null) {
                onHideCustomView();
                return;
            }
            mCustomView = paramView;
            mOriginalSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            mOriginalOrientation = activity.getRequestedOrientation();
            mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) activity.getWindow()
                    .getDecorView())
                    .addView(mCustomView, new FrameLayout.LayoutParams(-1, -1));
            activity.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            mCustomView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int i) {
                    updateControls();
                }
            });
        }

        void updateControls() {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mCustomView.getLayoutParams();
            params.bottomMargin = 0;
            params.topMargin = 0;
            params.leftMargin = 0;
            params.rightMargin = 0;
            params.height = -1;
            params.width = -1;
            mCustomView.setLayoutParams(params);
            activity.getWindow().getDecorView().setSystemUiVisibility(FULL_SCREEN_SETTING);
        }

        @Override
        public void onHideCustomView() {
            OnHideCustomView();
            ((FrameLayout) activity.getWindow().getDecorView()).removeView(mCustomView);
            mCustomView = null;
            activity.getWindow().getDecorView().setSystemUiVisibility(mOriginalSystemUiVisibility);
            activity.setRequestedOrientation(mOriginalOrientation);
            mCustomViewCallback.onCustomViewHidden();
            mCustomViewCallback = null;
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

        @Override
        public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            FileUploadNeeded(getIndex(view), fileChooserParams.getAcceptTypes()[0], fileChooserParams.isCaptureEnabled());
            return FileAccess();
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            if (SupportMultipleWindows()) {
                resultObj = resultMsg;
                OnNewWindowRequest(getIndex(view), isDialog, isUserGesture);
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

    public int getIndex(WebView view) {
        List<WebView> w = new ArrayList<>(wv.values());
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
    public void OnNewWindowRequest(int id,/*String url,*/boolean isDialog, boolean isPopup) {
        EventDispatcher.dispatchEvent(this, "OnNewWindowRequest", id,/*url,*/isDialog, isPopup);
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
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (permissions.isEmpty()) {
                        permissionRequest.deny();
                    } else {
                        // lets just skip this part :)
                        /*String[] str = permissions.split(",");
                        if (str == permissionRequest.getResources()) {
                            permissionRequest.grant(str);
                        }*/
                        permissionRequest.grant(permissionRequest.getResources());
                    }
                    permissionRequest = null;
                }
            });
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
    public class WebViewInterface{
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
    }

    @SimpleEvent(description = "Event raised when a website asks for specific permission(s) in list format.")
    public void OnPermissionRequest(List<String> permissionsList) {
        EventDispatcher.dispatchEvent(this, "OnPermissionRequest", permissionsList);
    }

    @SimpleEvent(description = "Event raised after getting previus print's result.")
    public void GotPrintResult(String printId, boolean isCompleted, boolean isFailed, boolean isBlocked) {
        EventDispatcher.dispatchEvent(this, "GotPrintResult", printId, isCompleted, isFailed, isBlocked);
    }
    @SimpleEvent()
    public void OnGeolocationRequested(String origin){
        EventDispatcher.dispatchEvent(this,"OnGeolocationRequested",origin);
    }
    @SimpleFunction()
    public void AllowGeolocationAccess(boolean allow,boolean remember){
        if (theCallback != null){
            theCallback.invoke(theOrigin,allow,remember);
            theCallback = null;
            theOrigin = "";
        }
    }
    @SimpleEvent()
    public void OnReceivedSslError(int errorCode){
        EventDispatcher.dispatchEvent(this,"OnReceivedSslError",errorCode);
    }
    @SimpleFunction()
    public void ProceedSslError(boolean proceed){
        if (sslHandler != null){
            if (proceed) {
                sslHandler.proceed();
            }else {
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

    public void CancelJsRequests() {
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


    public boolean DeepLinkParser(String url) {
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
            intent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
            //intent.putExtra(Intent.EXTRA_TEXT, Uri.parse(url).getQueryParameter("text"));
            //intent.setType("text/plain");
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

    public static class AdBlocker {
        public boolean isAd(String url) {
            try {
                return isAdHost(url != null && new URL(url).getHost() != null ? new URL(url).getHost() : "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private boolean isAdHost(String host) {
            if (host.isEmpty()) {
                return false;
            }
            int index = host.indexOf(".");
            return index >= 0 && (AD_HOSTS.contains(host) ||
                    index + 1 < host.length() && isAdHost(host.substring(index + 1)));
        }

        public WebResourceResponse createEmptyResource() {
            return new WebResourceResponse("text/plain", "utf-8", null);
        }
    }
}
