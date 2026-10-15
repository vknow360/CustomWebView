package com.sunny.cwvutils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONObject;

public class BrowserActivity extends Activity {
    private static final String TAG = "BrowserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        try {
            Intent launch = getLaunchIntent();
            if (launch != null) {
                Log.d(TAG, "Starting activity with intent: " + launch.toString());
                startActivity(launch);
            } else {
                Log.e(TAG, "Launch intent is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
        }
        finish();
    }

    private Intent getLaunchIntent() {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("BrowserPromptHelper", 0);
            String mScreen = sharedPreferences.getString("scrName", "");
            String startValue = sharedPreferences.getString("strtvlu", "");

            Log.d(TAG, "mScreen: '" + mScreen + "'");
            Log.d(TAG, "startValue: '" + startValue + "'");

            Intent intent = mScreen.isEmpty() ? getPackageManager().getLaunchIntentForPackage(getPackageName()) : new Intent();
            if (intent == null) {
                Log.e(TAG, "getLaunchIntentForPackage returned null!");
                return null;
            }

            if (!mScreen.isEmpty()) {
                intent.setClassName(this, mScreen);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.setPackage(null);

            if (getIntent().getData() != null) {
                BrowserPromptHelper.sLastUrl = getIntent().getData().toString();
            }

            intent.putExtra("APP_INVENTOR_START", JSONObject.quote(startValue));

            Log.d(TAG, "Final intent data: " + getIntent().getData());
            return intent;
        } catch (Exception e) {
            Log.e(TAG, "Error in getLaunchIntent: " + e.getMessage(), e);
        }
        return null;
    }
}
