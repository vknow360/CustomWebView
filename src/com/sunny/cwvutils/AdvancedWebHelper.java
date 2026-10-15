package com.sunny.cwvutils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.webkit.WebView;

import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.ServiceWorkerControllerCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;

import java.util.concurrent.Executor;

@DesignerComponent(version = 1,
        versionName = "1.0",
        description = "Advanced helper for WebView features like Dark Mode, Service Workers, and Proxy settings. <br> Developed by Sunny Gupta",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://i.ibb.co/4wLNN1Hs/ktvu4bapylsvnykoyhdm-c-fill-w-20-h-20.png",
        helpUrl = "https://github.com/vknow360/CustomWebView",
        androidMinSdk = 21)
@SimpleObject(external = true)
@UsesLibraries(libraries = "androidx-webkit-1.17.1.jar")
public class AdvancedWebHelper extends AndroidNonvisibleComponent {

    private final Activity activity;

    public AdvancedWebHelper(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
    }

    private WebView getWebViewFromObject(Object webViewOrComponent) {
        if (webViewOrComponent instanceof WebView) {
            return (WebView) webViewOrComponent;
        } else if (webViewOrComponent instanceof com.sunny.CustomWebView.CustomWebView) {
            return (WebView) ((com.sunny.CustomWebView.CustomWebView) webViewOrComponent).getWebView();
        } else if (webViewOrComponent instanceof AndroidViewComponent) {
            View view = ((AndroidViewComponent) webViewOrComponent).getView();
            if (view instanceof WebView) {
                return (WebView) view;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @SimpleFunction(description = "Enable or disable dark mode for a specific WebView or WebViewer")
    public void SetDarkMode(Object webViewOrComponent, boolean enable) {
        WebView webView = getWebViewFromObject(webViewOrComponent);
        if (webView != null) {
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
    }

    @SimpleProperty(description = "Sets the cache mode for process-wide Service Worker.")
    public void ServiceWorkerCacheMode(int mode) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_CACHE_MODE)) {
            ServiceWorkerControllerCompat.getInstance().getServiceWorkerWebSettings().setCacheMode(mode);
        }
    }

    @SimpleProperty(description = "Sets whether process-wide Service Worker can access local files.")
    public void ServiceWorkerAllowFileAccess(boolean allow) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_FILE_ACCESS)) {
            ServiceWorkerControllerCompat.getInstance().getServiceWorkerWebSettings().setAllowFileAccess(allow);
        }
    }

    @SimpleProperty(description = "Sets whether process-wide Service Worker can access content.")
    public void ServiceWorkerAllowContentAccess(boolean allow) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_CONTENT_ACCESS)) {
            ServiceWorkerControllerCompat.getInstance().getServiceWorkerWebSettings().setAllowContentAccess(allow);
        }
    }

    @SimpleProperty(description = "Sets whether process-wide Service Worker should not load resources from the network.")
    public void ServiceWorkerBlockNetworkLoads(boolean block) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BLOCK_NETWORK_LOADS)) {
            ServiceWorkerControllerCompat.getInstance().getServiceWorkerWebSettings().setBlockNetworkLoads(block);
        }
    }

    @SimpleFunction(description = "Checks whether Proxy Override is supported on this device.")
    public boolean IsProxySupported() {
        return WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE);
    }

    @SimpleFunction(description = "Sets a network proxy for WebView traffic (e.g. host:port, http://host:port, socks5://host:port). " +
            "bypassList can contain hostnames or IPs to exclude.")
    public void SetProxy(String proxyUrl, YailList bypassList) {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            ProxyError("Proxy override is not supported on this device.");
            return;
        }

        if (proxyUrl == null || proxyUrl.trim().isEmpty()) {
            ClearProxy();
            return;
        }

        ProxyConfig.Builder builder = new ProxyConfig.Builder();
        builder.addProxyRule(proxyUrl.trim());

        if (bypassList != null) {
            Object[] items = bypassList.toArray();
            for (int i = 0; i < items.length; i++) {
                String rule = items[i].toString().trim();
                if (!rule.isEmpty()) {
                    builder.addBypassRule(rule);
                }
            }
        }

        ProxyConfig proxyConfig = builder.build();
        Executor mainExecutor = ExecutorCompat.getMainExecutor(activity);

        ProxyController.getInstance().setProxyOverride(
                proxyConfig,
                mainExecutor,
                new Runnable() {
                    @Override
                    public void run() {
                        ProxyChanged(true);
                    }
                }
        );
    }

    @SimpleFunction(description = "Clears any active proxy override and reverts WebView to direct connection.")
    public void ClearProxy() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            ProxyError("Proxy override is not supported on this device.");
            return;
        }

        Executor mainExecutor = ExecutorCompat.getMainExecutor(activity);

        ProxyController.getInstance().clearProxyOverride(
                mainExecutor,
                new Runnable() {
                    @Override
                    public void run() {
                        ProxyChanged(false);
                    }
                }
        );
    }

    @SimpleEvent(description = "Event raised after proxy configuration changes. isEnabled is true if proxy is active.")
    public void ProxyChanged(boolean isEnabled) {
        EventDispatcher.dispatchEvent(this, "ProxyChanged", isEnabled);
    }

    @SimpleEvent(description = "Event raised when setting a proxy fails or is unsupported.")
    public void ProxyError(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "ProxyError", errorMessage);
    }
}
