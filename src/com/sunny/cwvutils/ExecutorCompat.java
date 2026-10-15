package com.sunny.cwvutils;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

public class ExecutorCompat {

    public static Executor getMainExecutor(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return context.getMainExecutor();
        } else {
            return new MainExecutorCompat(context);
        }
    }

    private static class MainExecutorCompat implements Executor {
        private final Handler mainHandler;

        public MainExecutorCompat(Context context) {
            this.mainHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void execute(Runnable command) {
            mainHandler.post(command);
        }
    }
}
