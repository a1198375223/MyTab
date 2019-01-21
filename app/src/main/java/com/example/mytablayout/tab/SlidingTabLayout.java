package com.example.mytablayout.tab;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mytablayout.utils.CommonUtils;


/**
 * 首先明确我们要自定义一个TabLayout需要完成那些步骤, 并且外部需要调用那些方法来使用我们自定义的TabLayout
 * 在design包中的TabLayout中有TabView和TabStrip两个重要的东西, 这是我们必须要自定义的东西.
 * 明确外界调用的步骤：
 * 1. 首先找到我们的布局：                    mTabLayout = (SlidingTabLayout) findViewById(R.id.tab_layout);
 * 2. 设置指示器颜色,就是设置tab下划线的颜色：  mTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.color_transparent));
 * 3. 支持与ViewPager关联：                  mTabLayout.setViewPager(mViewPager);
 * 4. 加上一些布局参数的更改就差不多了
 *
 * 明确自定义步骤：
 * 1. 首先完成TabView的创建
 * 2. 完成指示器TabStrip的创建
 * 3. 提供外界与ViewPager关联的接口
 */

public class SlidingTabLayout extends HorizontalScrollView {
    private static final String TAG = "自定义->SlidingTabLayout";

    /*使用这两个属性来实现不同滑动动画*/
    public static final int ANI_MODE_NORMAL = 0;                    // 无变化平移
    public static final int ANI_MODE_TAIL = 1;                      // 带小尾巴的效果

    private final int TITLE_OFFSET_DIP      = 24;                    // title偏移量（dp）
    private final int TAB_VIEW_PADDING_DIP  = 16;                    // tab的padding(dp)
    private final int TAB_VIEW_TEXT_SIZE_SP = 12;                    // tab的text大小（sp）

    /**
     * 这几个模式就是用来实现不同的tab效果的
     */
    private final int DISTRIBUTE_MODE_AVERAGE_SEGMENTATION  = 3;     // 每个item的间隔相同
    private final int DISTRIBUTE_MODE_TAB_AS_DIVIDER        = 2;     // 如果有n个Tab，则把屏幕宽度分为n+1部分，Tab的标题的中线与每部分的分隔线重合
    private final int DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER = 1;     // 如果有n个Tab，则把屏幕宽度分为n部分，Tab的标题在每部分居中
    private final int DISTRIBUTE_MODE_NONE                  = 0;     // 不对加入TabStrip的每个Tab的LayoutParams做任何处理
    private int mDistributeMode = DISTRIBUTE_MODE_NONE;              // 默认不做处理

    private int mTitleOffset;                                        // title的偏移量
    private int mTabViewTextViewId;                                  // 存储布局文件中的TextView的id
    private int mTabViewLayoutId;                                    // 存储layout文件的id

    private int mTitleSize = 0;                                      // 存储文字的大小
    private ColorStateList mTitleTextColor;                          // 存储显示文字的颜色

    private ViewPager mViewPager;                                    // 关联ViewPager
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener; // 用来回调

    private SlidingTabStrip mTabStrip;                               // 子tab

    private CustomUiListener mCustomUiListener;                      //

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

    /*------------------------------------step 1 begin----------------------------------------*/

    /**
     * step 1:
     * -1-
     * 实现创建TabView, 一个TextView
     * 还可以自己定义布局来加载, 创建自定义的TabView -> -2-
     */
    private TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        // 设置TextView的字体大小
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        // 设置TextView的字体
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // 获取系统属性
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        textView.setBackgroundResource(outValue.resourceId);
        // 设置全为大写
        textView.setAllCaps(true);

        // 为TextView设置padding
        int padding = (int) (TAB_VIEW_PADDING_DIP * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);
        return textView;
    }

    /**
     * -2-
     * 完成填充方法, 加载自定义布局或者加载默认的布局
     */
    private void populateTabStrip() {
        PagerAdapter adapter = mViewPager.getAdapter();
        OnClickListener listener = new TabClickListener();

        // 记录所有tab的总宽度
        int totalItemWidth = 0;


        for (int i = 0; i < adapter.getCount(); i++) {
            if (isTabAsDividerMode() && i == 0) {
                //todo 感觉是添加了一个占位View
                addPaddingViewForCenterMode();
            }


            View tabView = null;
            TextView tabTitleView = null;

            // 加载自定义布局
            if (mTabViewLayoutId != 0) {
                // 加载布局, 加载tab的title
                tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip, false);
                tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
            }

            // 加载默认布局
            if (tabView == null) {
                tabView = createDefaultTabView(getContext());
            }

            // todo
            if (tabView != null && mCustomUiListener != null) {
                mCustomUiListener.onCustomTitle(tabView, i);
            }

            // 如果加载的是默认布局, 把值赋值给tabTitleView
            if (tabTitleView == null && tabView instanceof TextView) {
                tabTitleView = (TextView) tabView;
            }

            // 通过不同的分割模式来处理tabView
            // todo 为什么这样设置值
            if (tabView != null && mDistributeMode > DISTRIBUTE_MODE_NONE) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                distributeTab(lp);
            }

            // 设置tabTitleView的字体大小和颜色
            if (tabTitleView != null) {
                if (mTitleSize > 0) {
                    tabTitleView.setTextSize(mTitleSize);
                }
                if (mTitleTextColor != null) {
                    tabTitleView.setTextColor(mTitleTextColor);
                }
                // 为tabTitleView设置文本
                tabTitleView.setText(adapter.getPageTitle(i));
            }

            if (tabView != null) {
                // 设置tabView的点击事件
                tabView.setOnClickListener(listener);

                // 设置联动, 如果ViewPager的当前position与tabView的position相同, 将该tabView设置成被选中状态
                if (i == mViewPager.getCurrentItem()) {
                    tabView.setSelected(true);
                }
            }

            //todo 感觉是添加了一个占位View
            if (isTabAsDividerMode() && i == adapter.getCount() - 1) {
                addPaddingViewForCenterMode();
            }
        } // end for
    }

    /**
     * 设置TabView的OnClickListener
     * 如果被点击了, 改变ViewPager的位置, 实现联动效果
     */
    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View tabView) {
            if (CommonUtils.getInstance().isFastDoubleClick()) {
                return;
            }
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (tabView == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(getViewPagerPosition(i));
                    return;
                }
            }
        }
    }

    /**
     * 通过指示器的位置来获取ViewPager的位置
     * 因为指示器有不同的模式, 所以要作处理
     */
    private int getViewPagerPosition(int tabStripIndex) {
        // 如果指示器是DISTRIBUTE_MODE_TAB_AS_DIVIDER模式, TabView的position比ViewPager的position大1
        if (isTabAsDividerMode()) {
            tabStripIndex--;
        }
        return tabStripIndex;
    }

    /**
     * 判断Tab分割模式是否是DISTRIBUTE_MODE_TAB_AS_DIVIDER模式
     */
    private boolean isTabAsDividerMode(){
        return mDistributeMode == DISTRIBUTE_MODE_TAB_AS_DIVIDER;
    }


    /**
     * 添加一个view作为？
     * todo 我暂时想的是为view设置一个背景来看一下是什么
     */
    private void addPaddingViewForCenterMode() {
        View paddingView = new View(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        mTabStrip.addView(paddingView, layoutParams);
    }

    /**
     * 定义一个CustomUiListener来监听 todo
     */
    public interface CustomUiListener {
        void onCustomTitle(View titleView, int position);
    }

    /**
     * 以不同的tab的mode来处理tab
     */
    private void distributeTab(LinearLayout.LayoutParams lp) {
        switch (mDistributeMode) {
            case DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER:
                lp.weight = 0;
                lp.height = 1;
                break;
            case DISTRIBUTE_MODE_TAB_AS_DIVIDER:
                lp.weight = 0;
                lp.height = 2;
                break;
        }
    }

    /*------------------------------------step 1 done-----------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*------------------------------------step 2 begin----------------------------------------*/

    /**
     * 在外部创建了一个SlideTabStrip来作为我们的指示器
     */

    /**
     * 定义一个接口来返回对应position的颜色
     */
    public interface TabColorShader {
        int getIndicatorColor(int position);
    }


    /*------------------------------------step 2 done-----------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*------------------------------------step 3 begin----------------------------------------*/


    /*------------------------------------step 3 done-----------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*-----------------------------------other methods----------------------------------------*/
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

    /**
     *  获取TabStrip的数量
     */
    private int getTabStripTabCount() {
        int childCount = mTabStrip.getChildCount(); // 获取TabStrip的数量
        return childCount;
    }


    /**
     * 设置Tab分割模式
     */
    public void setDistributeMode(int distributeMode) {
        mDistributeMode = distributeMode;

    }



    private void scrollToTab(int viewPagerrTabIndex, int positionOffset) {

    }


    private class InternalViewPagerPageChanegeListener implements ViewPager.OnPageChangeListener {
        private int mScrollState = 0; // 记录scroll的状态
                                      // 0:滑动结束->SCROLL_STATE_IDLE
                                      // 1:正在滑动->SCROLL_STATE_DRAGGING
                                      // 2:滑动完成（到达新页面）之后会变成0->SCROLL_STATE_SETTLING

        @Override
        public void onPageScrolled(int i, float v, int i1) {

            if (mViewPagerPageChangeListener != null) {
                Log.d(TAG, "onPageScrolled: ");
                mViewPagerPageChangeListener.onPageScrolled(i, v, i1);
            }

        }

        @Override
        public void onPageSelected(int i) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {

            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(i);
            }
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
