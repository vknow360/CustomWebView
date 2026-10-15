package com.sunny.cwvutils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.OnNewIntentListener;
import com.google.appinventor.components.runtime.OnResumeListener;

@DesignerComponent(version = 13, versionName = "1.3", description = "Helper component to register your app as a system web browser and intercept external web URLs <br> Developed by Sunny Gupta", category = ComponentCategory.EXTENSION, nonVisible = true, iconName = "https://i.ibb.co/4wLNN1Hs/ktvu4bapylsvnykoyhdm-c-fill-w-20-h-20.png", helpUrl = "https://github.com/vknow360/CustomWebView", androidMinSdk = 21)
@UsesActivities(activities = { @ActivityElement(intentFilters = {
                @IntentFilterElement(actionElements = {
                                @ActionElement(name = "android.intent.action.VIEW") }, categoryElements = {
                                                @CategoryElement(name = "android.intent.category.DEFAULT"),
                                                @CategoryElement(name = "android.intent.category.BROWSABLE")
                                }, dataElements = {
                                                @DataElement(scheme = "http"),
                                                @DataElement(scheme = "https")
                                }),
                @IntentFilterElement(actionElements = {
                                @ActionElement(name = "android.intent.action.VIEW") }, categoryElements = {
                                                @CategoryElement(name = "android.intent.category.DEFAULT"),
                                                @CategoryElement(name = "android.intent.category.BROWSABLE")
                                }, dataElements = {
                                                @DataElement(mimeType = "text/plain"),
                                                @DataElement(mimeType = "application/xhtml+xml")
                                }) }, name = "com.sunny.cwvutils.BrowserActivity", exported = "true", noHistory = "true")
})
@SimpleObject(external = true)
public class BrowserPromptHelper extends AndroidNonvisibleComponent
                implements OnNewIntentListener, OnResumeListener {
        private static final String TAG = "BrowserPromptHelper";
        public static String sLastUrl = null;

        public Activity activity;

        public BrowserPromptHelper(ComponentContainer container) {
                super(container.$form());
                activity = container.$context();
                form.registerForOnNewIntent(this);
                form.registerForOnResume(this);
        }

        private String getUrl(Intent intent) {
                Uri uri = intent.getData();
                if (uri != null) {
                        return uri.toString();
                }
                return sLastUrl;
        }

        @SimpleFunction(description = "")
        public void RegisterScreen(String screenName, String startValue) {
                SharedPreferences.Editor edit = form.getSharedPreferences(TAG, 0).edit();
                String replaceAll = this.form.getClass().getName().replace(this.form.getClass().getSimpleName(),
                                screenName);
                edit.putString("scrName", replaceAll);
                edit.putString("strtvlu", startValue);
                edit.apply();
        }

        @SimpleFunction(description = "Returns the url which started the current activity")
        public String GetStartUrl() {
                return getUrl(activity.getIntent());
        }

        @SimpleEvent(description = "Event raised when app gets resumed and gives the url which started this activity/screen if there is any else empty string")
        public void OnResume(String url) {
                EventDispatcher.dispatchEvent(this, "OnResume", url);
        }

        @Override
        public void onNewIntent(Intent intent) {
                OnResume(getUrl(intent));
        }

        @Override
        public void onResume() {
                if (sLastUrl != null) {
                        OnResume(sLastUrl);
                        sLastUrl = null;
                }
        }
}