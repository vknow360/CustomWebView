package com.sunny.CustomWebView;

import android.content.Context;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.webkit.WebView;

public class WView extends WebView {
    private final GestureDetector gd;
    private final int id;
    private OnScrollChangeListener onScrollChangeListener;


    private static final class SwipeConstants {
        static final float MIN_SWIPE_DISTANCE = 200f;
        static final float MAX_VERTICAL_DEVIATION = 90f;
        static final float MIN_SWIPE_VELOCITY = 350f;

        static final int DIRECTION_RIGHT = 1;
        static final int DIRECTION_LEFT = 2;
    }

    public WView(final int id,Context context,final SwipeCallback callback) {
        super(context);
        this.id = id;
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

                if (deltax > SwipeConstants.MIN_SWIPE_DISTANCE && deltay < SwipeConstants.MAX_VERTICAL_DEVIATION && velo > SwipeConstants.MIN_SWIPE_VELOCITY) {
                    if (e1.getRawX() > e2.getRawX()) {
                        callback.onSwipe(id,SwipeConstants.DIRECTION_LEFT);
                    } else {
                        callback.onSwipe(id,SwipeConstants.DIRECTION_RIGHT);
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

    @Override
    public int getId() {
        return id;
    }

    // Swipe callback interface
    public interface SwipeCallback {
        /**
         * Called when a swipe gesture is detected.
         *
         * @param webViewId The ID of the WebView
         * @param direction The swipe direction (1 = right, 2 = left)
         */
        void onSwipe(int webViewId, int direction);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChangeListener != null) {
            onScrollChangeListener.onScrollChange(getContext(), l, t, oldl, oldt);
        }
    }

    public void setScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        this.onScrollChangeListener = onScrollChangeListener;
    }

    public OnScrollChangeListener getOnScrollChangeListener() {
        return onScrollChangeListener;
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *  @param v          The view whose scroll position has changed.
         * @param scrollX    Current horizontal scroll origin.
         * @param scrollY    Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(Context v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }

}