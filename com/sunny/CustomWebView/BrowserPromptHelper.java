package com.sunny.CustomWebView;
import android.app.Activity;
import android.net.Uri;
import com.google.appinventor.components.annotations.androidmanifest.*;
import com.google.appinventor.components.runtime.*;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
@DesignerComponent(version = 1,
        versionName = "1.1",
        description ="Helper class of CustomWebView extension to add app to browsers list<br> Developed by Sunny Gupta",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png",
        helpUrl="https://github.com/vknow360/CustomWebView",
        androidMinSdk = 21)
@UsesActivities(activities = {@ActivityElement(intentFilters = {@IntentFilterElement(actionElements = {@ActionElement(name = "android.intent.action.VIEW")}, categoryElements = {@CategoryElement(name = "android.intent.category.DEFAULT"), @CategoryElement(name = "android.intent.category.BROWSABLE")}, dataElements = {@DataElement(scheme = "http"), @DataElement(scheme = "https")}), @IntentFilterElement(actionElements = {@ActionElement(name = "android.intent.action.VIEW")}, categoryElements = {@CategoryElement(name = "android.intent.category.DEFAULT"), @CategoryElement(name = "android.intent.category.BROWSABLE")}, dataElements = {@DataElement(scheme = "http"), @DataElement(scheme = "https"), @DataElement(mimeType = "text/html"), @DataElement(mimeType = "text/plain"), @DataElement(mimeType = "application/xhtml+xml")})},name="appinventor.ai_vknow360.CustomWebView.Screen1",launchMode = "singleTask")})
@SimpleObject(external=true)
public class BrowserPromptHelper extends AndroidNonvisibleComponent implements OnResumeListener{
    public Activity activity;
    public BrowserPromptHelper(ComponentContainer container){
        super(container.$form());
        activity = container.$context();
    }
    public String getUrl(){
        Uri uri = activity.getIntent().getData();
        if (uri != null && uri.toString() != null){
            return uri.toString();
        }
        return "";
    }
    @SimpleFunction(description = "Returns the url which started the current activity")
    public String GetStartUrl(){
        return getUrl();
    }

    @Override
    public void onResume() {
        OnResume(getUrl());
    }
    @SimpleEvent(description = "Event raised when app gets resumed and gives the url which started this activity/screen if there is any else empty string")
    public void OnResume(String url){
        EventDispatcher.dispatchEvent(this,"OnResume",url);
    }
}
