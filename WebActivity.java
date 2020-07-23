package com.sunny.CustomWebView;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.util.List;
import java.util.ArrayList;
import com.google.appinventor.components.runtime.util.JsonUtil;
public class WebActivity extends Activity{
	@Override
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        if (getIntent() != null){
            Uri uri = getIntent().getData();
            if (uri != null){
            	List<String> startValue = new ArrayList<>();
          		startValue.add(uri.toString());
          		startValue.add("1");
				PackageManager packageManager = getPackageManager();
        		Intent intent = packageManager.getLaunchIntentForPackage(getPackageName());
				intent.putExtra("APP_INVENTOR_START", JsonUtil.getJsonRepresentation(startValue));
        		startActivity(intent);
				finish();
            }
        }
    }
}
