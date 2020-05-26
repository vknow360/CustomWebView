package com.sunny.CustomWebView;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
public class WebActivity extends Activity{
	@Override
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        if (getIntent() != null){
            Uri uri = getIntent().getData();
            if (uri != null){
				CustomWebView.url = uri.toString();
				//String url = '"' + uri.toString() + '"';
                Intent launch = new Intent();
				launch.setClassName(getApplicationContext(),getPackageName()+".Screen1");
				//launch.putExtra("APP_INVENTOR_START",url);
				startActivity(launch);
				finish();
            }
        }
    }
}