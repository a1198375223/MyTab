package com.example.mytablayout.utils;

public class CommonUtils {
    private final int FAST_DOUBLE_CLICK_INTERVAL = 500;      // 两次点击的时间间隔0.5s
    private long mLastClickTime = 0;
    private static CommonUtils sINSTANCE;

    public boolean isFastDouobleClick() {
        return isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL);
    }

    private CommonUtils() {}

    public static CommonUtils getInstance() {
        if (sINSTANCE == null) {
            synchronized (CommonUtils.class) {
                if (sINSTANCE == null) {
                    sINSTANCE = new CommonUtils();
                }
            }
        }
        return sINSTANCE;
    }


    public boolean isFastDoubleClick() {
        return isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL);
    }

    /**
     * 判断是否是快速点击, 默认点击间隔时间为0.5s
     */
    public boolean isFastDoubleClick(long time) {
        if (time <= 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        long delta = now - mLastClickTime;

        if (delta > 0 && delta < time) {
            return true;
        }
        mLastClickTime = now;
        return false;
    }
}
