package com.sunny.CustomWebView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
@DesignerComponent(version = 1, description = "Developed by Sunny Gupta", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png")
@SimpleObject(external=true)
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.READ_EXTERNAL_STORAGE")
public final class CustomWebView extends AndroidNonvisibleComponent{
	public WebView webView;
    public Context context;
    public boolean Js = true;
    public boolean FileAccess = true;
    public boolean ZoomDisplay = true;
    public boolean SupportZoom = true;
	public boolean FitContent = true;
	public boolean BlockNetworkLoads = false;
    public int ZoomPercent = 100;
	public int FontSize = 16;
    public boolean AllowLocation = true;
    public boolean LongClickable =true;
    public String homeUrl = "";
    public boolean followLinks = true;
	public boolean AutoLoadImages = true;
    public boolean prompt = true;
    public String UserAgent = "";
    public boolean DesktopMode = false;
    public boolean ignoreSslErrors = true;
    public boolean LoadLocalFiles = true;
    public boolean SupportMultipleWindows = true;
    WebViewInterface wvInterface;
    public String WebViewString;
    private final String DESKTOP_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2049.0 Safari/537.36";
    private final String MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

	public CustomWebView(ComponentContainer container) {
    super(container.$form());
    context = container.$context();
	webView = new WebView(context);
	wvInterface = new WebViewInterface();
	UserAgent = webView.getSettings().getUserAgentString();
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
      description = "URL of the page the WebViewer should initially open to.  " +
          "Setting this will load the page.",
      category = PropertyCategory.BEHAVIOR)
  public String HomeUrl() {
    return homeUrl;
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
	resetWebViewClient(webView);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty()
  public void HomeUrl(String url) {
    homeUrl = url;
    webView.clearHistory();
    GoToUrl(homeUrl);
  }
  @SimpleProperty(
      description = "URL of the page currently viewed.   This could be different from the " +
          "Home URL if new pages were visited by following links.",
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
    resetWebViewClient(webView);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void ZoomEnabled(boolean follow) {
    SupportZoom = follow;
    resetWebViewClient(webView);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void AutoLoadImages(boolean follow) {
    AutoLoadImages = follow;
    resetWebViewClient(webView);
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
    resetWebViewClient(webView);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
      defaultValue = "100")
  @SimpleProperty()
  public void ZoomPercent(int follow) {
    ZoomPercent = follow;
    resetWebViewClient(webView);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
      defaultValue = "16")
  @SimpleProperty()
  public void FontSize(int follow) {
    FontSize = follow;
    resetWebViewClient(webView);
	Reload();
  }
  @SimpleProperty()
  public int FontSize() {
    return FontSize;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty()
  public void FitContent(boolean mode) {
    FitContent = mode;
	resetWebViewClient(webView);
  }
  @SimpleProperty()
  public boolean FitContent() {
    return FitContent;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty()
  public void DesktopMode(boolean mode) {
    DesktopMode = mode;
	UserAgent = DESKTOP_USER_AGENT;
    resetWebViewClient(webView);
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
    resetWebViewClient(webView);
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void LoadLocalFiles(boolean follow) {
    LoadLocalFiles = follow;
    resetWebViewClient(webView);
  }
  @SimpleProperty(description="")
  public boolean LoadLocalFiles() {
    return LoadLocalFiles;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty()
  public void SupportMultipleWindows(boolean follow) {
    SupportMultipleWindows = follow;
    resetWebViewClient(webView);
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
    resetWebViewClient(webView);
  }
  @SimpleFunction(
      description = "Loads the home URL page.  This happens automatically when " +
          "the home URL is changed.")
  public void GoHome() {
    GoToUrl(homeUrl);
  }
   @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "False")
  @SimpleProperty(userVisible = false,
      description = "Whether or not to give the application permission to use the Javascript geolocation API. " +
          "This property is available only in the designer.")
  public void UsesLocation(boolean uses) {
    AllowLocation = uses;
	resetWebViewClient(webView);
  }
  @SimpleProperty(description = "If True, then prompt the user of the WebView to give permission to access the geolocation API. " +
      "If False, then assume permission is granted.")
  public boolean PromptForPermission() {
    return prompt;
  }
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(userVisible = true)
  public void PromptForPermission(boolean prompt) {
    prompt = prompt;
  }
@SimpleEvent(description = "When the JavaScript calls AppInventor.setWebViewString this event is run.")
  public void WebViewStringChange(String value) {
    EventDispatcher.dispatchEvent(this, "WebViewStringChange",value);
  }
  @SimpleFunction(description="")
  public void LoadHtml(String path){
        webView.loadData(path,"text/html", "UTF-8");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.removeAllCookies(null);
        } else {
            cookieManager.removeAllCookie();
        }
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
   public void CreateWebView(HVArrangement view){
	   View v = view.getView();
        FrameLayout frameLayout = (FrameLayout) v;
        resetWebViewClient(webView);
        frameLayout.addView(webView);
       /* FrameLayout.LayoutParams  param = (FrameLayout.LayoutParams) webView.getLayoutParams();
        param.leftMargin = 0;
        param.topMargin = 0;
        param.height = v.getHeight();
        param.width = v.getWidth();*/
		FrameLayout.LayoutParams  param = new FrameLayout.LayoutParams(new RelativeLayout.LayoutParams(-1,-1));
        webView.setLayoutParams(param);
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
      webView.evaluateJavascript(script, new ValueCallback<String>() {
          @Override
          public void onReceiveValue(String s) {
             AfterJavaScriptEvaluated(s);
          }
      });
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
	public void OnErrorReceived(String message,int errorCode){
EventDispatcher.dispatchEvent(this, "OnErrorReceived",message,errorCode);
    }
	public void resetWebViewClient(WebView View){
        View.getSettings().setJavaScriptEnabled(Js);
        View.getSettings().setAllowFileAccess(FileAccess);
        View.getSettings().setSupportZoom(SupportZoom);
        View.getSettings().setDisplayZoomControls(ZoomDisplay);
        View.setLongClickable(LongClickable);
        View.getSettings().setBuiltInZoomControls(SupportZoom);
        View.setInitialScale(ZoomPercent);
        View.getSettings().setDefaultFontSize(FontSize);
        View.getSettings().setBlockNetworkImage(!AutoLoadImages);
        View.getSettings().setLoadWithOverviewMode(FitContent);
        View.getSettings().setUseWideViewPort(FitContent);
        View.getSettings().setLoadsImagesAutomatically(AutoLoadImages);
        View.getSettings().setBlockNetworkImage(BlockNetworkLoads);
        View.getSettings().setAllowFileAccessFromFileURLs(LoadLocalFiles);
        View.getSettings().setAllowUniversalAccessFromFileURLs(LoadLocalFiles);
        View.getSettings().setAllowContentAccess(LoadLocalFiles);
        View.getSettings().setJavaScriptCanOpenWindowsAutomatically(SupportMultipleWindows);
        View.getSettings().setSupportMultipleWindows(SupportMultipleWindows);
        View.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                OnDownloadNeeded(s,s2,s3,l);
            }
        });
        View.getSettings().setGeolocationEnabled(AllowLocation);
        View.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                LongClicked(hitTestResult.getExtra(),hitTestResult.getType());
                return false;
            }
        });

        View.setWebViewClient(new WebViewClient(){
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
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        OnErrorReceived(error.getDescription().toString(),error.getErrorCode());
                    }
        });
        View.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
               final AlertDialog dialog = new AlertDialog.Builder(context).create();
               final WebView web = new WebView(context);
                WebSettings webSettings = web.getSettings();
                webSettings.setAllowFileAccess(true);
                webSettings.setGeolocationEnabled(true);
                webSettings.setJavaScriptEnabled(true);
                webSettings.setJavaScriptCanOpenWindowsAutomatically(SupportMultipleWindows);
                webSettings.setSupportMultipleWindows(SupportMultipleWindows);
                web.setWebViewClient(webView.getWebViewClient());
                web.setWebChromeClient(webView.getWebChromeClient());
                dialog.setButton(1, "Reload", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        web.reload();
                    }
                });
                dialog.setButton(2, "Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        web.goForward();
                    }
                });
                dialog.setButton(3, "Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
                dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if (i == KeyEvent.KEYCODE_BACK){
                            if (web.canGoBack()){
                                web.goBack();
                            }else {
                                dialog.dismiss();
                            }
                        }
                        return false;
                    }
                });
                web.setLayoutParams(new RelativeLayout.LayoutParams(-2,-2));
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setView(web);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(web);
                resultMsg.sendToTarget();
                if (SupportMultipleWindows){
                    dialog.show();
                    return true;
                }else {
                    return false;
                }
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
                }
            }
        });
        View.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int i, int i1, boolean b) {
                FindResultReceived(i,i1,b);
            }
        });
      /*  if (UserAgent.isEmpty()){
            if (DesktopMode){
                UserAgent = DESKTOP_USER_AGENT;
            }else {
                UserAgent = MOBILE_USER_AGENT;
            }
        }
        View.getSettings().setUserAgentString(UserAgent);*/
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
        String webViewString;
        WebViewInterface() {
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
}