package com.sunny.CustomWebView;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
public class WebActivity extends Activity{
	@Override
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        if (getIntent() != null){
            Uri uri = getIntent().getData();
            if (uri != null){
				CustomWebView.url = uri.toString();
				//String url = '"' + uri.toString() + '"';
				PackageManager packageManager = getPackageManager();
                //Intent launch = new Intent();
				//launch.setClassName((Context)this, getPackageName() + "." + "Screen1");
				//launch.putExtra("APP_INVENTOR_START",url);
				startActivity(packageManager.getLaunchIntentForPackage(getPackageName()));
				finish();
            }
        }
    }
}