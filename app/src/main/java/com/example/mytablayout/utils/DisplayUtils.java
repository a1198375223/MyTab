package com.example.mytablayout.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayUtils {
    private float mDensity;
    private int mScreenWidth;
    private int mScreenHeight;

    private static DisplayUtils sINSTANCE;

    public static DisplayUtils getInstance(Context context) {
        if (sINSTANCE == null) {
            synchronized (DisplayUtils.class) {
                if (sINSTANCE == null) {
                    sINSTANCE = new DisplayUtils(context);
                }
            }
        }
        return sINSTANCE;
    }

    private DisplayUtils(Context context) {
        initMetrics(context);
    }

    private void initMetrics(Context context) {
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

    public int getPhoneHeight() {
        return mScreenHeight;
    }
}
