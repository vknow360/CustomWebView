package com.sunny.CustomWebView;

import android.content.Context;
import android.webkit.WebView;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
public class WView extends WebView {
    GestureDetector gd;
    public WView(int id,Context context,SwipeCallback callback) {
        super(context);
        SimpleOnGestureListener onGestureListener = new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return super.onDoubleTapEvent(e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return super.onDown(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float deltax, deltay, velo;
                deltax = Math.abs(e1.getRawX() - e2.getRawX());
                deltay = Math.abs(e1.getRawY() - e2.getRawY());
                velo = Math.abs(velocityX);

                if (deltax > 200 && deltay < 90 && velo > 350) {
                    if (e1.getRawX() > e2.getRawX()) {
                        callback.onSwipe(id,2);
                    } else if (e1.getRawX() < e2.getRawX()) {
                        callback.onSwipe(id,1);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public void onShowPress(MotionEvent e) {
                super.onShowPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return super.onSingleTapUp(e);
            }
        };
        gd = new GestureDetector(context, onGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (gd.onTouchEvent(event) || super.onTouchEvent(event));
    }
    public interface SwipeCallback{
        void onSwipe(int i,int i1);
    }
}
