package com.sunny.CustomWebView;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.FrameLayout;

import java.util.Arrays;
import java.util.List;

public class CustomWebChromeClient extends WebChromeClient {
    private final String TAG = "CustomWebChromeClient";
    private final CustomWebView customWebView;

    private final Activity activity;
    private final Context context;
    private final WebView webView;

    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;
    CustomWebChromeClient(CustomWebView customWebView){
        this.customWebView = customWebView;
        this.activity = customWebView.getActivity();
        this.context = customWebView.getContext();
        this.webView = customWebView.getWebView();
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (!customWebView.PromptForPermission()) {
            callback.invoke(origin, true, true);
        } else {
            customWebView.setGeolocationCallback(callback, origin);
            customWebView.OnGeolocationRequested(origin);
        }
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
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
        customWebView.OnShowCustomView();
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
        customWebView.OnHideCustomView();
    }

    @Override
    public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
        customWebView.setFilePathCallback(filePathCallback);
        customWebView.FileUploadNeeded(view.getId(), fileChooserParams.getAcceptTypes()[0], fileChooserParams.isCaptureEnabled());
        return customWebView.FileAccess();
    }

    @Override
    public boolean onCreateWindow(WebView view,final boolean isDialog,final boolean isUserGesture, Message resultMsg) {
        if (customWebView.SupportMultipleWindows()) {
            final int i = view.getId();
            customWebView.setResultObject(resultMsg);
            if (customWebView.nWM.equalsIgnoreCase("LEGACY")) {
                final WebView mWebView = new WebView(context);
                mWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        customWebView.OnNewWindowRequest(i, url, isDialog, !isUserGesture);
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
                customWebView.OnNewWindowRequest(i, str, isDialog, isUserGesture);
            }
        }
        return customWebView.SupportMultipleWindows();
    }

    @Override
    public void onCloseWindow(WebView window) {
        customWebView.OnCloseWindowRequest(window.getId());
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        customWebView.OnProgressChanged(view.getId(), newProgress);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        customWebView.OnConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.sourceId(), consoleMessage.messageLevel().toString());
        return true;
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        if (!customWebView.PromptForPermission()) {
            request.grant(request.getResources());
        } else {
            customWebView.setPermissionRequestCallback(request);
            String[] strings = request.getResources();
            List<String> permissions = Arrays.asList(strings);
            customWebView.OnPermissionRequest(permissions);
        }
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        customWebView.setJsPromptResult(result);
        customWebView.OnJsPrompt(view.getId(), url, message, defaultValue);
        return customWebView.EnableJS();
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        customWebView.OnJsAlert(view.getId(), url, message);
        customWebView.setJsAlertResult(result);
        return customWebView.EnableJS();
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        customWebView.setJsResult(result);
        customWebView.OnJsConfirm(view.getId(), url, message);
        return customWebView.EnableJS();
    }
    
}
