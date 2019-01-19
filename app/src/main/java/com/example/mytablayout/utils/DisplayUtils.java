package com.example.mytablayout.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayUtils {
    private float mDensity;
    private int mScreenWidth;
    private int mScreenHeight;

    private void initMeterics(Context context) {
        DisplayMetrics metrics = context.getApplicationContext().getResources().getDisplayMetrics();
        if (metrics != null) {
            mScreenHeight = Math.max(metrics.heightPixels, metrics.widthPixels);
            mScreenWidth = Math.min(metrics.heightPixels, metrics.widthPixels);
            mDensity = metrics.density;
        }
    }


    /**
     * from dp to px
     */
    public int dip2px(float dpValue) {
        return (int) (dpValue * mDensity + 0.5f);
    }


    /**
     * from px to dp
     */
    public int px2dip(float pxValue) {
        return (int) (pxValue / mDensity + 0.5f);
    }


    public int getPhoneWidth() {
        return mScreenWidth;
    }
}
