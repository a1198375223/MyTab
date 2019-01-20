package com.example.mytablayout.tab;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.HorizontalScrollView;

public class SlidingTabLayout extends HorizontalScrollView {
    private final int TITLE_OFFSET_DIP      = 24;                    // title偏移量（dp）
    private final int TAB_VIEW_PADDING_DIP  = 16;                    // tab的padding(dp)
    private final int TAB_VIEW_TEXT_SIZE_SP = 12;                    // tab的text大小（sp）

    private int mTitleOffset;                                        // title的偏移量
    private int mTabViewTextViewId;
    private int mTabViewLayoutId;

    private ViewPager mViewPager;                                    // 关联ViewPager
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener; // 用来回调

    private SlidingTabStrip mTabStrip;                               // 子tab

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 关闭滑动条
        setHorizontalScrollBarEnabled(false);
        // 设置是否填充该窗口
        setFillViewport(true);

        // dp->px
        mTitleOffset = (int) (TITLE_OFFSET_DIP * getResources().getDisplayMetrics().density);


        mTabStrip = new SlidingTabStrip(context);
        // 设定内容居中
        // todo 这里换成CENTER_VERTICAL会不会更好 待测试
        mTabStrip.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(mTabStrip, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
    }


    /**
     * 与ViewPager建立关联, 与setupWithViewPager()设置差不多
     */
    public void setViewPager(ViewPager viewPager) {
        // 清空tab列表
        mTabStrip.removeAllViews();

        if (null != viewPager) {
            viewPager.addOnPageChangeListener(new InternalViewPagerPageChanegeListener());
        }
    }

    /**
     * 设置ViewPager.OnPageChangeListener来保证ViewPager会被通知状态改变
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.mViewPagerPageChangeListener = listener;
    }

    private class InternalViewPagerPageChanegeListener implements ViewPager.OnPageChangeListener {
        private int mScrollState = 0; // 记录scroll的状态 0:滑动结束 1:正在滑动   2:滑动完成（到达新页面）之后会变成0

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {

        }

        @Override
        public void onPageScrollStateChanged(int i) {
            mScrollState = i;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(i);
            }
        }
    }

}
