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

    @Override
    public int getId() {
        return id;
    }

    public interface SwipeCallback{
        void onSwipe(int i,int i1);
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