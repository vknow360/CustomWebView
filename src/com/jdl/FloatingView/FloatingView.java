package com.jdl.FloatingView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

import static android.content.Context.WINDOW_SERVICE;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.ActivityResultListener;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.OnDestroyListener;

@DesignerComponent(version = 1, description = "Floating View <br> Developed by Jarlisson", iconName = "img.png", helpUrl = "https://github.com/jarlisson2/FloatingViewAIX") // //
public class FloatingView extends AndroidNonvisibleComponent implements ActivityResultListener, OnDestroyListener {
    public Activity activity;
    public Context context;
    public ComponentContainer container;
    private static final int REQUEST_CODE_DRAW_OVERLAY_PERMISSION = 5;

    private static boolean mIsFloatViewShowing = false;
    private boolean mFloatViewTouchConsumedByMove = false;
    private boolean clickable = false;
    private int mFloatViewLastX;
    private int mFloatViewLastY;
    private int mFloatViewFirstX;
    private int mFloatViewFirstY;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;
    private RelativeLayout rl;
    private ViewGroup viewParent;
    private int indexChild;
    private View viewHV;

    private int requestCode = 0;

    public FloatingView(ComponentContainer container) {
        super(container.$form());
        context = container.$context();
        this.container = container;
        activity = (Activity) context;
        form.registerForOnDestroy(this);

    }

    @SimpleFunction()
    public void MoveAppToFront(){
        PackageManager packageManager = form.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(form.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setPackage(null);
        context.startActivity(intent);
    }

    @SimpleFunction(description = "Initializes the component you want to float.")
    public void SetupView(AndroidViewComponent viewComponent, boolean clickable /*, int positionX, int positionY */) {
        viewHV = viewComponent.getView();
        this.clickable = clickable;
//        floatViewHV(positionX, positionY);
        floatViewHV();
    }

    @SimpleFunction(description = "Displays the floating component.")
    public void ShowFloatingView() {
        if (checkDrawOverlayPermission(true)) {
            showFloatView();
        }
    }


    @SimpleFunction(description = "Hides the floating component.")
    public void DismissViewFloating() {
        dismissFloatView();
    }

    @SimpleFunction(description = "Checks whether the overlay permission is active.")
    public boolean CheckDrawOverlayPermission() {
        return checkDrawOverlayPermission(false);
    }

    @SimpleFunction(description = "Redirects to application settings to allow overlay permission.")
    public void RequestDrawOverlayPermission() {
        checkDrawOverlayPermission(true);
    }

    @SimpleFunction(description = "Gets the X coordinate that the floating view is in.")
    public int GetPositionX() {
        return params.x;
    }

    @SimpleFunction(description = "Gets the Y coordinate that the floating view is in.")
    public int GetPositionY() {
        return params.y;
    }

    @SimpleFunction(description = "Moves the floating view to the indicated coordinates.")
    public void SetPosition(int x, int y) {
        params.x = x;
        params.y = y;
        if (mWindowManager != null) {
            //PositionMoved(params.x, params.y);
            mWindowManager.updateViewLayout(rl, params);
        }
    }

    @SimpleProperty(description = "Adjusts whether the floating view is clickable")
    public void SetClickable(final boolean clickable) {
        this.clickable = clickable;
    }

    @SimpleProperty(description = "Checks whether the floating view is clickable.")
    public boolean GetClickable() {
        return clickable;
    }

    @SimpleProperty(description = "Checks if the floating is present on the screen.")
    public boolean GetFloatingViewVisible() {
        return mIsFloatViewShowing;
    }

    @SimpleEvent(description = "Executes after clicking on the floating component.")
    public void ClickView() {
        EventDispatcher.dispatchEvent(this, "ClickView");
    }

    @SimpleFunction(description = "Returns the floating window to the screen.")
    public void RestoreFloatingView() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rl != null) {
                    if (mIsFloatViewShowing && mWindowManager != null)
                        mWindowManager.removeView(rl);
                    mIsFloatViewShowing = false;
                    rl.removeView(viewHV);
                    View view = viewHV instanceof ViewGroup ? ((ViewGroup) viewHV).getChildAt(0) : (View) viewHV;
                    view.setOnClickListener(null);
                    view.setOnTouchListener(null);
                    viewParent.addView(viewHV, indexChild);
                    rl = null;
                }
            }
        });
    }

    @SimpleFunction(description = "Prompts to focus on the floating window.")
    public void RequestFocusFloatingView() {
        if (mIsFloatViewShowing) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mWindowManager != null) {
                        params.flags=WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                        mWindowManager.updateViewLayout(rl, params);
                    }
                }
            });
        }
    }

    @SimpleFunction(description = "Loses focus on the floating window.")
    public void LoseFocusFloatingView() {
        if (mIsFloatViewShowing) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mWindowManager != null) {
                        rl.clearFocus();
                        params.flags= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
                        mWindowManager.updateViewLayout(rl, params);
                    }
                }
            });
        }
    }

    @Override
    public void resultReturned(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
                showFloatView();
            }
        }
    }

    private boolean checkDrawOverlayPermission(boolean request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            if (request) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                if (requestCode == 0)
                    requestCode = form.registerForActivityResult(this);
                container.$context().startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY_PERMISSION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void showFloatView() {
        if (!mIsFloatViewShowing) {
            mIsFloatViewShowing = true;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!activity.isFinishing()) {
                        mWindowManager = (WindowManager) activity.getSystemService(WINDOW_SERVICE);
                        if (mWindowManager != null) {
                            rl.clearFocus();
                            mWindowManager.addView(rl, params);
                        }
                    }
                }
            });
        }

    }

    private void dismissFloatView() {
        if (mIsFloatViewShowing) {
            mIsFloatViewShowing = false;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mWindowManager != null) {
                        mWindowManager.removeViewImmediate(rl);
                    }
                }
            });
        }
    }

    private void floatViewHV(/* int positionX, int positionY */) {
        dismissFloatView();
        rl = new RelativeLayout(context);
        params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        //params.gravity = Gravity.TOP | Gravity.LEFT;
        params.gravity = Gravity.BOTTOM;
        //params.x = positionX;
        //params.y = positionY;

        View view = viewHV instanceof ViewGroup ? ((ViewGroup) viewHV).getChildAt(0) : (View) viewHV;
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickView();
            }
        });
        view.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int totalDeltaX = mFloatViewLastX - mFloatViewFirstX;
                int totalDeltaY = mFloatViewLastY - mFloatViewFirstY;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        mFloatViewLastX = (int) event.getRawX();
                        mFloatViewLastY = (int) event.getRawY();
                        mFloatViewFirstX = mFloatViewLastX;
                        mFloatViewFirstY = mFloatViewLastY;
                        mFloatViewTouchConsumedByMove = !clickable;
                        break;
                    case MotionEvent.ACTION_UP:
                        mFloatViewTouchConsumedByMove = !clickable;
                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        int deltaX = (int) event.getRawX() - mFloatViewLastX;
//                        int deltaY = (int) event.getRawY() - mFloatViewLastY;
//                        mFloatViewLastX = (int) event.getRawX();
//                        mFloatViewLastY = (int) event.getRawY();
//                        if (Math.abs(totalDeltaX) >= 5 || Math.abs(totalDeltaY) >= 5) {
//                            if (event.getPointerCount() == 1) {
//                                params.x += deltaX;
//                                params.y += deltaY;
//                                mFloatViewTouchConsumedByMove = true;
//                                if (mWindowManager != null) {
//                                    //PositionMoved(params.x, params.y);
//                                    mWindowManager.updateViewLayout(rl, params);
//                                }
//                            } else {
//                                mFloatViewTouchConsumedByMove = false;
//                            }
//                        } else {
//                            mFloatViewTouchConsumedByMove = false;
//                        }
//                        break;
                    default:
                        break;
                }
                return mFloatViewTouchConsumedByMove;
            }
        });
        if (viewHV.getParent() != null) {
            viewParent = (ViewGroup) viewHV.getParent();
            indexChild = viewParent.indexOfChild(viewHV);
            ((ViewGroup) viewHV.getParent()).removeView(viewHV);
        }
        rl.addView(viewHV);

    }

    @Override
    public void onDestroy() {
        if (mWindowManager != null) {
            mWindowManager.removeViewImmediate(rl);
        }
    }
}