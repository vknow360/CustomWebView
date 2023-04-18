package com.sunny.CustomWebView;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesActivities;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.CategoryElement;
import com.google.appinventor.components.annotations.androidmanifest.DataElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(version = 1,
        versionName = "1.1",
        description ="Helper class of CustomWebView extension to add app to browsers list<br> Developed by Sunny Gupta",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "https://res.cloudinary.com/andromedaviewflyvipul/image/upload/c_scale,h_20,w_20/v1571472765/ktvu4bapylsvnykoyhdm.png",
        helpUrl="https://github.com/vknow360/CustomWebView",
        androidMinSdk = 21)
@UsesActivities(activities = {@ActivityElement(intentFilters = {@IntentFilterElement(actionElements = {@ActionElement(name = "android.intent.action.VIEW")}, categoryElements = {@CategoryElement(name = "android.intent.category.DEFAULT"), @CategoryElement(name = "android.intent.category.BROWSABLE")}, dataElements = {@DataElement(scheme = "http"), @DataElement(scheme = "https")}), @IntentFilterElement(actionElements = {@ActionElement(name = "android.intent.action.VIEW")}, categoryElements = {@CategoryElement(name = "android.intent.category.DEFAULT"), @CategoryElement(name = "android.intent.category.BROWSABLE")}, dataElements = {@DataElement(scheme = "http"), @DataElement(scheme = "https"), @DataElement(mimeType = "text/html"), @DataElement(mimeType = "text/plain"), @DataElement(mimeType = "application/xhtml+xml")})},name=".Screen1",launchMode = "singleTask")})
@SimpleObject(external=true)
public class BrowserPromptHelper extends AndroidNonvisibleComponent implements OnNewIntentListener {
    public Activity activity;
    public BrowserPromptHelper(ComponentContainer container){
        super(container.$form());
        activity = container.$context();
        form.registerForOnNewIntent(this);
    }
    public String getUrl(Intent intent){
        Uri uri = intent.getData();
        if (uri != null && uri.toString() != null){
            return uri.toString();
        }
        return "";
    }
    @SimpleFunction(description = "Returns the url which started the current activity")
    public String GetStartUrl(){
        return getUrl(activity.getIntent());
    }

    @SimpleEvent(description = "Event raised when app gets resumed and gives the url which started this activity/screen if there is any else empty string")
    public void OnResume(String url){
        EventDispatcher.dispatchEvent(this,"OnResume",url);
    }

    @Override
    public void onNewIntent(Intent intent) {
        OnResume(getUrl(intent));
    }
}