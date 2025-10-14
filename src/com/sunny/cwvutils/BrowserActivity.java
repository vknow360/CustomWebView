package com.sunny.cwvutils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import com.google.appinventor.components.runtime.util.JsonUtil;
import org.json.JSONException;

public class BrowserActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent launch = getLaunchIntent();
        if(launch != null){
            startActivity(launch);
        }
        finish();
    }

    private Intent getLaunchIntent(){
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("BrowserPromptHelper", 0);
            String mScreen = sharedPreferences.getString("scrName", "");
            Intent intent = mScreen.isEmpty() ? getPackageManager().getLaunchIntentForPackage(getPackageName()) : new Intent();
            if (!mScreen.isEmpty()){
                intent.setClassName(this,mScreen);
            }
            intent.putExtra("APP_INVENTOR_START", JsonUtil.getJsonRepresentation(sharedPreferences.getString("strtvlu", "")));
            intent.setPackage(null);
            intent.setData(getIntent().getData());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            return intent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
