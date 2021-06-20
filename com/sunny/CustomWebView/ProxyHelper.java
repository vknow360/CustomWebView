package com.sunny.CustomWebView;

import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.util.ArrayMap;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

@DesignerComponent(version = 1,
        versionName = "1.0",
        description ="Helper class of CustomWebView extension for altering proxy<br> Developed by Sunny Gupta",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png",
        helpUrl="https://github.com/vknow360/CustomWebView",
        androidMinSdk = 21)
@SimpleObject(external=true)
public class ProxyHelper extends AndroidNonvisibleComponent {
    public Context context;
    public ProxyHelper(ComponentContainer container){
        super(container.$form());
        context = container.$context();
    }
    @SimpleFunction()
    public boolean SetProxy(String host,int port){
        return setProxy(host,port);
    }
    @SimpleFunction()
    public boolean RemoveProxy(){
        return revertProxy();
    }
    @SimpleEvent()
    public void GotError(String errorMessage){
        EventDispatcher.dispatchEvent(this,"GotError",errorMessage);
    }
    /*
        credits to https://www.programmersought.com/article/4447606409/
    */
    private boolean setProxy(String host, int port) {
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");
        try {
            Class applictionCls = Class.forName(context.getApplicationInfo().className);
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(context);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        onReceiveMethod.invoke(rec, context, intent);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            GotError(e.getMessage()!=null?e.getMessage():e.toString());
        }
        return false;
    }
    private boolean revertProxy() {
        Properties properties = System.getProperties();
        properties.remove("http.proxyHost");
        properties.remove("http.proxyPort");
        properties.remove("https.proxyHost");
        properties.remove("https.proxyPort");
        try {
            Class applictionCls = Class.forName(context.getApplicationInfo().className);
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(context);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);
                        onReceiveMethod.invoke(rec, context, intent);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            GotError(e.getMessage()!=null?e.getMessage():e.toString());
        }
        return false;
    }
}
