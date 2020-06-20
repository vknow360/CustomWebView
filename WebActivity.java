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
							PackageManager packageManager = getPackageManager();
        			Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
							intent.putExtra("APP_INVENTOR_START",'"'+uri.toString()+'"');
        			startActivity(intent);
							finish();
            }
        }
    }
}
