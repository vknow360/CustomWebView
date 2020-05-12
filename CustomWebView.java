package com.sunny.CustomWebView;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.print.*;
import android.view.View;
import android.webkit.*;
import android.widget.FrameLayout;
import java.util.Arrays;
import java.util.List;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.ActivityResultListener;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import android.view.MotionEvent;
@DesignerComponent(version = 0, description = "Developed by Sunny Gupta", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png")
@SimpleObject(external=true)
public final class CustomWebView extends AndroidNonvisibleComponent implements ActivityResultListener{
    public boolean NO_VIEW = true;
    private final int FILECHOOSER_RESULTCODE = 1;
    public Activity activity;
    public WebView webView;
    public Context context;
    public boolean Js = true;
    public boolean AutoplayMedia = false;
    public boolean AutoLoadImages = true;
    public boolean BlockNetworkLoads = false;
    public boolean FileAccess = false;
    public boolean ZoomDisplay = true;
    public boolean SupportZoom = true;
    public boolean scrollbar = true;
    public int ZoomPercent = 100;
    public int FontSize = 16;
    public boolean UsesLocation = false;
    public boolean LongClickable =true;
    public boolean followLinks = true;
    public boolean prompt = true;
    public String UserAgent = "";
    public boolean DesktopMode = false;
    public boolean ignoreSslErrors = true;
    public boolean LoadLocalFiles = true;
    public boolean SupportMultipleWindows = true;
    public WebViewInterface wvInterface;
    public String WebViewString ;
    public boolean UseWideViewPort = true;
    public boolean LoadWithOverviewMode = true;
    public JsPromptResult jsPromptResult ;
    private String DESKTOP_USER_AGENT = "";
    private String MOBILE_USER_AGENT = "";
    public ValueCallback<Uri[]> mFilePathCallback;
    //public ValueCallback<Uri> kFilePathCallback;
    public Message dontSend;
    public Message reSend;
    public boolean hasLocationAccess = false;
    public boolean hasReadAccess = false;
    public ComponentContainer mcontainer;
    public PermissionRequest PermissionRequest;
    public PrintJob printJob;

	public CustomWebView(ComponentContainer container) {
    super(container.$form());
    mcontainer = container;
    context = container.$context();
    activity = (Activity) context;
    webView = new WebView(context);
    wvInterface = new WebViewInterface(webView.getContext());
    webView.addJavascriptInterface(wvInterface, "AppInventor");
          webView.addJavascriptInterface(wvInterface, "Makeroid");
          webView.addJavascriptInterface(wvInterface, "Kodular");
          MOBILE_USER_AGENT = webView.getSettings().getUserAgentString();
          DESKTOP_USER_AGENT = MOBILE_USER_AGENT.replace("Android","diordnA").replace("Mobile","eliboM");
          webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
          webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                OnDownloadNeeded(s,s2,s3,l);
            }
        });
        webView.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int i, int i1, boolean b) {
                FindResultReceived(i,i1,b);
            }
        });
        webView.setOnTouchListener(new View.OnTouchListener() {
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
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                String item = hitTestResult.getExtra();
                int type = hitTestResult.getType();
                if (item == null){
                    item = "";
                }
                LongClicked(item,type);
                return !LongClickable;
            }
        });
        webView.setWebViewClient(new WebClient());
        webView.setWebChromeClient(new ChromeClient());
        webView.getSettings().setJavaScriptEnabled(Js);
        webView.getSettings().setAllowFileAccess(LoadLocalFiles);
        webView.getSettings().setSupportZoom(SupportZoom);
        webView.getSettings().setDisplayZoomControls(ZoomDisplay);
        webView.setLongClickable(LongClickable);
        webView.getSettings().setBuiltInZoomControls(SupportZoom);
        webView.setInitialScale(ZoomPercent);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(SupportZoom);
        webView.setVerticalScrollBarEnabled(scrollbar);
        webView.setHorizontalScrollBarEnabled(scrollbar);
        webView.getSettings().setDefaultFontSize(FontSize);
        webView.getSettings().setBlockNetworkImage(!AutoLoadImages);
        webView.getSettings().setLoadWithOverviewMode(LoadWithOverviewMode);
        webView.getSettings().setUseWideViewPort(UseWideViewPort);
        webView.getSettings().setLoadsImagesAutomatically(AutoLoadImages);
        webView.getSettings().setBlockNetworkLoads(BlockNetworkLoads);
        webView.getSettings().setAllowFileAccessFromFileURLs(LoadLocalFiles);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(LoadLocalFiles);
        webView.getSettings().setAllowContentAccess(LoadLocalFiles);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(AutoplayMedia);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(SupportMultipleWindows);
        webView.getSettings().setSupportMultipleWindows(SupportMultipleWindows);
        webView.getSettings().setTextZoom(ZoomPercent);
        webView.getSettings().setGeolocationEnabled(UsesLocation);
        UserAgent = MOBILE_USER_AGENT;
        webView.getSettings().setUserAgentString(UserAgent);
  }
  @Override
    public void resultReturned(int requestCode, int resultCode, Intent intent) {
        if(requestCode==FILECHOOSER_RESULTCODE && Build.VERSION.SDK_INT >= 21) {
            if(resultCode != -1){
                mFilePathCallback.onReceiveValue(null);
                mFilePathCallback = null;
                return;
            }
            Uri[] arrayOfUri2 = WebChromeClient.FileChooserParams.parseResult(resultCode,intent);
            Uri[] arrayOfUri1 = arrayOfUri2;
            if (arrayOfUri2 == null) {
                ClipData clipData = intent.getClipData();
                resultCode = clipData.getItemCount();
                Uri[] arrayOfUri = new Uri[resultCode];
                requestCode = 0;
                while (true) {
                    arrayOfUri1 = arrayOfUri;
                    if (requestCode < resultCode) {
                        arrayOfUri[requestCode] = clipData.getItemAt(requestCode).getUri();
                        requestCode++;
                        continue;
                    }
                    break;
                }
            }
            mFilePathCallback.onReceiveValue(arrayOfUri1);
            mFilePathCallback = null;
        }/*else {
            if(resultCode != -1){
                kFilePathCallback.onReceiveValue(null);
                kFilePathCallback = null;
                return;
            }
            if (intent.getData() != null){
                kFilePathCallback.onReceiveValue(intent.getData());
            }
        }*/
    }
  @DesignerProperty(editorType = "component:com.google.appinventor.components.runtime.VerticalArrangement")
  @SimpleProperty(userVisible = false)
  public void WebviewVArrangement(HVArrangement container){
      if(NO_VIEW){
        View v = container.getView();
        FrameLayout frameLayout = (FrameLayout)v;
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(-1,-1);
        frameLayout.addView(webView,param);
        NO_VIEW = false;
      }  
  }
  @DesignerProperty(editorType = "component:com.google.appinventor.components.runtime.HorizontalArrangement")
  @SimpleProperty(userVisible = false)
  public void WebviewHArrangement(HVArrangement container){
    if(NO_VIEW){
        View v = container.getView();
        FrameLayout frameLayout = (FrameLayout)v;
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(-1,-1);
        frameLayout.addView(webView,param);
        NO_VIEW = false;
      }
  }
  @SimpleFunction()
  public void CreateWebView(AndroidViewComponent container){
    if(NO_VIEW){
        View v = container.getView();
        FrameLayout frameLayout = (FrameLayout) v;
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(-1,-1);
        frameLayout.addView(webView,param);
        NO_VIEW = false;
      }
  }
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void WebViewString(String newString) {
    wvInterface.setWebViewStringFromBlocks(newString);
  }
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public String WebViewString() {
    return wvInterface.webViewString;
  }
   
  @SimpleProperty(
      description = "",category = PropertyCategory.BEHAVIOR)
  public String UserAgent() {
    return UserAgent;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty()
  public void UserAgent(String userAgent) {
    UserAgent = userAgent;
	webView.getSettings().setUserAgentString(UserAgent);
	Reload();
  }
  
  @SimpleProperty(
      description = "URL of the page currently viewed",
      category = PropertyCategory.BEHAVIOR)
  public String CurrentUrl() {
    return (webView.getUrl() == null) ? "" : webView.getUrl();
  }
  @SimpleProperty(
      description = "Title of the page currently viewed",
      category = PropertyCategory.BEHAVIOR)
  public String CurrentPageTitle() {
    return (webView.getTitle() == null) ? "" : webView.getTitle();
  }
  @SimpleProperty(
      description = "Determines whether to follow links when they are tapped in the WebViewer.  " +
          "If you follow links, you can use GoBack and GoForward to navigate the browser history. ",
      category = PropertyCategory.BEHAVIOR)
  public boolean FollowLinks() {
    return followLinks;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void FollowLinks(boolean follow) {
    followLinks = follow;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty()
  public void AutoplayMedia(boolean follow) {
    AutoplayMedia = follow;
    webView.getSettings().setMediaPlaybackRequiresUserGesture(AutoplayMedia);
  }
  @SimpleProperty()
  public boolean AutoplayMedia() {
    return AutoplayMedia;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void ZoomEnabled(boolean follow) {
    SupportZoom = follow;
    webView.getSettings().setBuiltInZoomControls(SupportZoom);
    webView.getSettings().setSupportZoom(SupportZoom);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void AutoLoadImages(boolean follow) {
    AutoLoadImages = follow;
    webView.getSettings().setBlockNetworkImage(!AutoLoadImages);
    webView.getSettings().setLoadsImagesAutomatically(AutoLoadImages);
  }
  @SimpleProperty()
  public boolean AutoLoadImages() {
    return AutoLoadImages;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void DisplayZoom(boolean follow) {
    ZoomDisplay = follow;
    webView.getSettings().setDisplayZoomControls(ZoomDisplay);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
      defaultValue = "100")
  @SimpleProperty()
  public void ZoomPercent(int follow) {
    ZoomPercent = follow;
    webView.getSettings().setTextZoom(ZoomPercent);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
      defaultValue = "16")
  @SimpleProperty()
  public void FontSize(int follow) {
    FontSize = follow;
    webView.getSettings().setDefaultFontSize(FontSize);
  }
  @SimpleProperty()
  public int FontSize() {
    return FontSize;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty()
  public void DesktopMode(boolean mode) {
    DesktopMode = mode;
    if(mode){
        UserAgent = DESKTOP_USER_AGENT;
    }else{
        UserAgent = MOBILE_USER_AGENT;
    }
    webView.getSettings().setUserAgentString(UserAgent);
	Reload();
  }
  @SimpleProperty()
  public boolean DesktopMode() {
    return DesktopMode;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void LongClickable(boolean follow) {
    LongClickable = follow;
    webView.setLongClickable(LongClickable);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void FileAccess(boolean follow) {
    LoadLocalFiles = follow;
        if (!hasReadAccess){
            new Handler().post(new Runnable() {
                @Override
                public void run() {
            form.askPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    new PermissionResultHandler() {
                        @Override
                        public void HandlePermissionResponse(String permission, boolean granted) {
                            hasReadAccess = granted;
                        }
                    });
                }
            });
            if (hasLocationAccess){
                webView.getSettings().setAllowFileAccess(LoadLocalFiles);
                webView.getSettings().setAllowFileAccessFromFileURLs(LoadLocalFiles);
                webView.getSettings().setAllowUniversalAccessFromFileURLs(LoadLocalFiles);
                webView.getSettings().setAllowContentAccess(LoadLocalFiles);
            }else{
                webView.getSettings().setAllowFileAccess(false);
                webView.getSettings().setAllowFileAccessFromFileURLs(false);
                webView.getSettings().setAllowUniversalAccessFromFileURLs(false);
                webView.getSettings().setAllowContentAccess(false);
            }
        }
  }
  @SimpleProperty(description="")
  public boolean FileAccess() {
    return LoadLocalFiles;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void SupportMultipleWindows(boolean follow) {
    SupportMultipleWindows = follow;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty()
  public void BlockNetworkLoads(boolean follow) {
    BlockNetworkLoads = follow;
    webView.getSettings().setBlockNetworkLoads(BlockNetworkLoads);
  }
  @SimpleProperty()
  public boolean BlockNetworkLoads() {
    return BlockNetworkLoads;
  }
  @SimpleProperty()
  public boolean SupportMultipleWindows() {
    return SupportMultipleWindows;
  }
  @SimpleProperty()
  public boolean LongClickable() {
    return LongClickable;
  }
  @SimpleProperty(
      description = "Determine whether or not to ignore SSL errors. Set to true to ignore " +
          "errors. Use this to accept self signed certificates from websites.",
      category = PropertyCategory.BEHAVIOR)
  public boolean IgnoreSslErrors() {
    return ignoreSslErrors;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty()
  public void IgnoreSslErrors(boolean ignoreSslErrors) {
    ignoreSslErrors = ignoreSslErrors;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void LoadWithOverviewMode(boolean ignoreSslErrors) {
    LoadWithOverviewMode = ignoreSslErrors;
    webView.getSettings().setLoadWithOverviewMode(LoadWithOverviewMode);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void UseWideViewPort(boolean ignoreSslErrors) {
    UseWideViewPort = ignoreSslErrors;
    webView.getSettings().setUseWideViewPort(UseWideViewPort);
  }
  @SimpleProperty()
  public boolean LoadWithOverviewMode() {
    return LoadWithOverviewMode;
  }
  @SimpleProperty()
  public boolean UseWideViewPort() {
    return UseWideViewPort;
  }
  @DesignerProperty(editorType= PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,defaultValue="True")
  @SimpleProperty()
  public void EnableJS(boolean js) {
    Js = js;
    webView.getSettings().setJavaScriptEnabled(Js);
  }
  @SimpleProperty()
  public boolean EnableJS() {
    return Js;
  }
   @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false,
      description = "Whether or not to give the application permission to use the Javascript geolocation API")
  public void UsesLocation(boolean uses) {
    UsesLocation = uses;
    if (!hasLocationAccess){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
        form.askPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                new PermissionResultHandler() {
                    @Override
                    public void HandlePermissionResponse(String permission, boolean granted) {
                        hasLocationAccess = granted;
                    }
                });
            }
        });
        if (hasLocationAccess){
            webView.getSettings().setGeolocationDatabasePath(activity.getFilesDir().getAbsolutePath());
            webView.getSettings().setDatabaseEnabled(true);
        }else{
            webView.getSettings().setDatabaseEnabled(false);
        }
        webView.getSettings().setGeolocationEnabled(UsesLocation);
    }
  }
  @SimpleProperty(description = "If True, then prompt the user of the WebView to give permission to access the geolocation API. " +
      "If False, then assume permission is granted.")
  public boolean PromptForPermission() {
    return prompt;
  }
  
  @DesignerProperty(defaultValue = "True", editorType = "boolean")
  @SimpleProperty(description = "Whether to display a scrollbar or not")
  public void Scrollbar(boolean paramBoolean) {
    scrollbar = paramBoolean;
    webView.setVerticalScrollBarEnabled(scrollbar);
    webView.setHorizontalScrollBarEnabled(scrollbar);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(userVisible = true)
  public void PromptForPermission(boolean prompt) {
    prompt = prompt;
  }
  @SimpleProperty(userVisible = true)
  public void BackgroundColor(int bgColor) {
    webView.setBackgroundColor(bgColor);
  }
@SimpleEvent(description = "When the JavaScript calls AppInventor.setWebViewString this event is run.")
  public void WebViewStringChange(String value) {
    EventDispatcher.dispatchEvent(this, "WebViewStringChange",value);
  }
  @SimpleFunction(description="")
  public void StopLoading(){
        webView.stopLoading();
    }
  @SimpleFunction(description="")
   public void Reload(){
        webView.reload();
    }
    @SimpleFunction(description="")
    public void LoadHtml(String html){
        webView.loadDataWithBaseURL("",html,"text/html","UTF-8",null);
    }
  @SimpleFunction(description="")
  public boolean CanGoBack(){
        return webView.canGoBack();
    }
  @SimpleFunction(description="")
  public boolean CanGoForward(){
        return webView.canGoForward();
    }
  @SimpleFunction(description="")
  public void ClearCookies(){
    CookieManager cookieManager = CookieManager.getInstance();
    if(Build.VERSION.SDK_INT >= 21){
        cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean aBoolean) {
                CookiesRemoved(aBoolean);
            }
        });
        cookieManager.flush();
        return;
    }
    try {
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
        cookieSyncManager.startSync();
        cookieManager.removeAllCookie();
        cookieManager.removeSessionCookie();
        cookieSyncManager.stopSync();
        cookieSyncManager.sync();
        CookiesRemoved(true);
        return;
    }catch (Exception e){
        CookiesRemoved(false);
        return;
    }
}

   @SimpleEvent(description="")
   public void CookiesRemoved(boolean successful){
    EventDispatcher.dispatchEvent(this,"CookiesRemoved",successful);
    }
  @SimpleFunction(description="")
  public void ClearCache(){
        webView.clearCache(true);
    }
  @SimpleFunction(description="")
  public void ClearInternalHistory(){
        webView.clearHistory();
    }
  @SimpleFunction(description="")
  public void GoBack(){
	  if(CanGoBack()){
        webView.goBack();
	  }
    }
  @SimpleFunction(description="")
  public void GoForward(){
	  if(CanGoForward()){
        webView.goForward();
	  }
    }
  @SimpleFunction(description="")
  public boolean CanGoBackOrForward(int steps){
        return webView.canGoBackOrForward(steps);
    }
  @SimpleFunction(description="")
  public void GoBackOrForward(int steps){
	  if(CanGoBackOrForward(steps)){
        webView.goBackOrForward(steps);
	  }
    }
	
	@SimpleFunction(description="")
    public void GoToUrl(String url){
            webView.loadUrl(url);
        }
	@SimpleEvent(description="")
    public void PageLoaded(){
EventDispatcher.dispatchEvent(this, "PageLoaded");
    }
	@SimpleEvent(description="")
    public void OnDownloadNeeded(String url,String contentDisposition,String mimeType,long size){
EventDispatcher.dispatchEvent(this, "OnDownloadNeeded",url,contentDisposition,mimeType,size);
    }
	@SimpleEvent(description="")
    public void OnProgressChanged(int progress){
EventDispatcher.dispatchEvent(this, "OnProgressChanged",progress);
    }
	@SimpleEvent(description="")
    public void OnConsoleMessage(String message, int lineNumber, int sourceID, String level){
EventDispatcher.dispatchEvent(this, "OnConsoleMessage",message,lineNumber,sourceID,level);
    }
	
	@SimpleFunction(description="")
    public void EvaluateJavaScript(String script){
        if (Build.VERSION.SDK_INT >= 19) {
            webView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    AfterJavaScriptEvaluated(s);
                }
            });
        }
        webView.loadUrl(script);
    }
	@SimpleEvent(description="")
    public void AfterJavaScriptEvaluated(String result){
EventDispatcher.dispatchEvent(this, "AfterJavaScriptEvaluated",result);
    }
	
	@SimpleFunction(description="")
	public void ClearMatches(){
        webView.clearMatches();
    }
	@SimpleEvent(description="")
	public void LongClicked(String item,int type){
EventDispatcher.dispatchEvent(this, "LongClicked",item,type);
    }
	@SimpleEvent(description="")
	public void OnErrorReceived(String message,int errorCode,String url){
EventDispatcher.dispatchEvent(this, "OnErrorReceived",message,errorCode,url);
    }
	
    public class WebClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return !followLinks;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            PageLoaded();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (ignoreSslErrors){
                handler.proceed();
            }else {
                handler.cancel();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return  !followLinks;
        }


        @Override
        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            dontSend = dontResend;
            reSend = resend;
            OnFormResubmission();
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return null;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return null;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            OnErrorReceived(description,errorCode,failingUrl);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            OnErrorReceived(error.getDescription().toString(),error.getErrorCode(),request.getUrl().toString());
        }
    }
    public class ChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            final GeolocationPermissions.Callback theCallback = callback;
            final String theOrigin = origin;
            if (prompt) {
                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("Permission Request");
                if (origin.equals("file://"))
                    origin = "This Application";
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
                return;
            }
            callback.invoke(origin, true, true);
        }

        /*public void openFileChooser(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
            kFilePathCallback = filePathCallback;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(acceptType);
            try{
                activity.startActivityForResult(intent,FILECHOOSER_RESULTCODE);
            }catch (Exception e){
                filePathCallback.onReceiveValue(null);
            }
        }*/

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            mFilePathCallback = filePathCallback;
            Intent intent = fileChooserParams.createIntent();
            intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
            intent.setType("*/*");
            try{
                activity.startActivityForResult(intent,FILECHOOSER_RESULTCODE);
            }catch (Exception e){
                filePathCallback.onReceiveValue(null);
            }
            return FileAccess;
        }

        @Override
        public boolean onCreateWindow(WebView view,final boolean isDialog,final boolean isUserGesture, Message resultMsg) {
            if (SupportMultipleWindows){
               final WebView mWebView = new WebView(context);
                mWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        OnNewWindowRequest(url,isDialog,!isUserGesture);
                        mWebView.stopLoading();
                        mWebView.destroy();
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport)resultMsg.obj;
                transport.setWebView(mWebView);
                resultMsg.sendToTarget();
            }
            return SupportMultipleWindows;
        }

        @Override
        public void onCloseWindow(WebView window) {
            OnNewWindowCloseRequest();
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            OnProgressChanged(newProgress);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            OnConsoleMessage(consoleMessage.message(),consoleMessage.lineNumber(),consoleMessage.lineNumber(),consoleMessage.messageLevel().toString());
            return true;
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            if (!prompt){
                request.grant(request.getResources());
            }else{
                PermissionRequest = request;
                OnPermissionRequest(Arrays.asList(request.getResources()));
            }
        }

    }

    @SimpleEvent()
    public void OnFormResubmission(){
        EventDispatcher.dispatchEvent(this,"OnFormResubmission");
    }
    @SimpleFunction()
    public void ResubmitForm(boolean reSubmit){
        if (reSubmit){
            reSend.sendToTarget();
        }else {
            dontSend.sendToTarget();
        }
    }
	@SimpleEvent()
	public void OnNewWindowRequest(String url,boolean isDialog,boolean isPopup){
		EventDispatcher.dispatchEvent(this, "OnNewWindowRequest",url,isDialog,isPopup);
    }
    @SimpleEvent()
    public void OnNewWindowCloseRequest(){
        EventDispatcher.dispatchEvent(this,"OnNewWindowCloseRequest");
    }
	@SimpleFunction(description="")
	public int ContentHeight(){
        return webView.getContentHeight();
    }
	@SimpleEvent(description="")
	 public void GotCertificate(String issuedBy,String issuedTo,String validTill){
        EventDispatcher.dispatchEvent(this, "GotCertificate",issuedBy,issuedTo,validTill);
    }
	@SimpleFunction(description="")
	public void GetSslCertificate(){
        SslCertificate certificate = webView.getCertificate();
        if (certificate != null) {
            GotCertificate(certificate.getIssuedBy().getDName(),certificate.getIssuedTo().getDName(),certificate.getValidNotAfterDate().toString());
        }
    }
	@SimpleEvent(description="")
	public void FindResultReceived(int activeMatchOrdinal,int numberOfMatches,boolean isDoneCounting){
        EventDispatcher.dispatchEvent(this, "FindResultReceived",activeMatchOrdinal,numberOfMatches,isDoneCounting);
    }
    @SimpleFunction()
    public void ClearLocation(){
        GeolocationPermissions.getInstance().clearAll();
    }
	@SimpleFunction(description="")
	public void Find(String string){
        webView.findAllAsync(string);
    }
	@SimpleFunction(description="")
	public String GetCookies(String url){
        return CookieManager.getInstance().getCookie(url);
    }
	@SimpleFunction(description="")
	public void FindNext(boolean forward){
        webView.findNext(forward);
    }
	public class WebViewInterface {
        Context mcontext;
        String webViewString;
        WebViewInterface(Context c) {
            mcontext = c;
            webViewString = " ";
        }
        @JavascriptInterface
        public String getWebViewString() {
            return webViewString;
        }
        @JavascriptInterface
        public void setWebViewString(final String newString) {
            webViewString = newString;
            WebViewStringChange(newString);
        }
        public void setWebViewStringFromBlocks(final String newString) {
            webViewString = newString;
        }
    }
    @SimpleFunction()
    public void GrantPermission(List<String> permissions){
        if (PermissionRequest != null){
            PermissionRequest.grant((String[]) permissions.toArray());
        }
    }
    @SimpleEvent()
    public void OnPermissionRequest(List<String> permissionsList){
        EventDispatcher.dispatchEvent(this,"OnPermissionRequest",permissionsList);
    }
    @SimpleEvent()
    public void GotPrintResult(String id,boolean isCompleted,boolean isFailed,boolean isBlocked){
        EventDispatcher.dispatchEvent(this,"GotPrintResult",id,isCompleted,isFailed,isBlocked);
    }
    @SimpleFunction()
    public void PrintWebContent(int colorMode){
        boolean gotResult = false;
        PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printDocumentAdapter = webView.createPrintDocumentAdapter(webView.getTitle());
        PrintAttributes.Builder printAttributes = new PrintAttributes.Builder();
        printAttributes.setColorMode(colorMode);
        printJob = printManager.print(webView.getTitle(),printDocumentAdapter,printAttributes.build());
        while (!gotResult){
            gotResult = printJob.isStarted() || printJob.isBlocked() || printJob.isCancelled() || printJob.isCompleted() || printJob.isFailed();
        }
        GotPrintResult(printJob.toString(),printJob.isCompleted(),printJob.isFailed(),printJob.isBlocked());
    }
    @SimpleFunction()
    public void RestartPrinting(){
        boolean gotResult = false;
        printJob.restart();
        while (!gotResult){
            gotResult = printJob.isStarted() || printJob.isBlocked() || printJob.isCancelled() || printJob.isCompleted() || printJob.isFailed();
        }
        GotPrintResult(printJob.toString(),printJob.isCompleted(),printJob.isFailed(),printJob.isBlocked());
    }
    @SimpleFunction()
    public void CancelPrinting(){
        boolean gotResult = false;
        printJob.cancel();
        while (!gotResult){
            gotResult = printJob.isStarted() || printJob.isBlocked() || printJob.isCancelled() || printJob.isCompleted() || printJob.isFailed();
        }
        GotPrintResult(printJob.toString(),printJob.isCompleted(),printJob.isFailed(),printJob.isBlocked());
    }

}