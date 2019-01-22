package com.example.mytablayout.tab;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mytablayout.utils.CommonUtils;
import com.example.mytablayout.utils.DisplayUtils;


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

    private final int TITLE_OFFSET_DIP      = 24;                   // title偏移量（dp）
    private final int TAB_VIEW_PADDING_DIP  = 16;                   // tab的padding(dp)
    private final int TAB_VIEW_TEXT_SIZE_SP = 12;                   // tab的text大小（sp）
    private final float DEFAULT_NORMAL_TEXT_SIZE = 16.33f;          // 默认的正常文本大小
    private final float DEFAULT_SELECTED_TEXT_SIZE = 22.67f;        // 默认被选中文本的大小

    /**
     * 这几个模式就是用来实现不同的tab效果的
     */
    public static final int DISTRIBUTE_MODE_AVERAGE_SEGMENTATION  = 3;     // 每个item的间隔相同
    public static final int DISTRIBUTE_MODE_TAB_AS_DIVIDER        = 2;     // 如果有n个Tab，则把屏幕宽度分为n+1部分，Tab的标题的中线与每部分的分隔线重合
    public static final int DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER = 1;     // 如果有n个Tab，则把屏幕宽度分为n部分，Tab的标题在每部分居中
    public static final int DISTRIBUTE_MODE_NONE                  = 0;     // 不对加入TabStrip的每个Tab的LayoutParams做任何处理
    public  int mDistributeMode = DISTRIBUTE_MODE_NONE;              // 默认不做处理

    private int mTitleOffset;                                        // title的偏移量
    private int mTabViewTextViewId;                                  // 存储布局文件中的TextView的id
    private int mTabViewLayoutId;                                    // 存储layout文件的id

    private float mNormalTitleSize = 0;                              // 存储文字的大小
    private float mSelectedTitleSize = 0;                            // 存储文字被选中的大小
    private ColorStateList mTitleTextColor;                          // 存储显示文字的颜色

    private ViewPager mViewPager;                                    // 关联ViewPager
    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener; // 用来回调

    private SlidingTabStrip mTabStrip;                               // 子tab

    private CustomUiListener mCustomUiListener;                      // 自定义ui listener, 外部灵活控制显示和隐藏title的一部分

    private int mLastPosition = 0;                                   // 记录最新的position位置
    private int mCurrentPosition = 0;                                // 记录当前坐标的位置

    private int mDefaultSelected = 0;

    // 记录ContentDescriptions
    private SparseArray<String> mContentDescriptions = new SparseArray<>();

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
        mTabStrip.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(mTabStrip, ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.MATCH_PARENT);

        setTextChangeSize(DEFAULT_NORMAL_TEXT_SIZE, DEFAULT_SELECTED_TEXT_SIZE);
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

        // 存储所有item的width值总和来处理平均分配模式
        int totalItemWidth = 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            if (isTabAsDividerMode() && i == 0) {
                // 添加了一个开始分割线
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

            // 通过外部对tabView进行必要的处理
            if (tabView != null && mCustomUiListener != null) {
                mCustomUiListener.onCustomTitle(tabView, i);
            }

            // 如果加载的是默认布局, 把值赋值给tabTitleView
            if (tabTitleView == null && tabView instanceof TextView) {
                tabTitleView = (TextView) tabView;
            }

            // 通过不同的分割模式来处理tabView
            // 如果设置了分割模式则对每一个tabView就进行参数变化
            // todo 感觉没啥用啊 待测试删除是否会显示有影响
//            if (tabView != null && mDistributeMode > DISTRIBUTE_MODE_NONE) {
//                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
//                distributeTab(lp);
//            }

            // 设置tabTitleView的字体大小和颜色
            if (tabTitleView != null) {
                if (i == mViewPager.getCurrentItem()) {
                    tabTitleView.setTextSize(mSelectedTitleSize);
                    tabTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                } else {
                    tabTitleView.setTextSize(mNormalTitleSize);
                    tabTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                }

                if (mTitleTextColor != null) {
                    tabTitleView.setTextColor(mTitleTextColor);
                }
                // 为tabTitleView设置文本
                tabTitleView.setText(adapter.getPageTitle(i));
                //Log.d(TAG, "populateTabStrip: title : " + adapter.getPageTitle(i));
            }

            if (tabView != null) {
                // 设置tabView的点击事件
                tabView.setOnClickListener(listener);

                // 为tabView添加ContentDescription
                String desc = mContentDescriptions.get(i, null);
                if (desc != null) {
                    tabView.setContentDescription(desc);
                }

                if (mDistributeMode == DISTRIBUTE_MODE_AVERAGE_SEGMENTATION) {
                    tabView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                    totalItemWidth = totalItemWidth + tabView.getMeasuredWidth();
                }

                mTabStrip.addView(tabView);
                // 设置联动, 如果ViewPager的当前position与tabView的position相同, 将该tabView设置成被选中状态
                if (i == mViewPager.getCurrentItem()) {
                    tabView.setSelected(true);
                }
            }

            // 添加了一个结束分割线
            if (isTabAsDividerMode() && i == adapter.getCount() - 1) {
                addPaddingViewForCenterMode();
            }
        } // end for

        if (mDistributeMode == DISTRIBUTE_MODE_AVERAGE_SEGMENTATION && adapter.getCount() > 1) {
            int layoutWidth = DisplayUtils.getInstance(getContext()).getPhoneWidth();

            if (getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
                layoutWidth = layoutWidth - layoutParams.leftMargin - layoutParams.rightMargin;
            } else if (getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) getLayoutParams();
                layoutWidth = layoutWidth - layoutParams.leftMargin - layoutParams.rightMargin;
            } else if (getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) getLayoutParams();
                layoutWidth = layoutWidth - layoutParams.leftMargin - layoutParams.rightMargin;
            }

            int average_segmentation = (layoutWidth - totalItemWidth) / (adapter.getCount() - 1);

            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (i > 0) {
                    View child = mTabStrip.getChildAt(i);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) child.getLayoutParams();
                    layoutParams.leftMargin = average_segmentation;
                    child.setLayoutParams(layoutParams);
                }
            }
        }
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
     * 添加一个view作为分割线吧
     */
    private void addPaddingViewForCenterMode() {
        View paddingView = new View(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        mTabStrip.addView(paddingView, layoutParams);
    }

    /**
     * 定义一个CustomUiListener来监听 让外部灵活控制显示和隐藏title的一部分
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

    /**
     * 设置自定义的UI Listener
     */
    public void setCustomUiListener(CustomUiListener listener) {
        this.mCustomUiListener = listener;
    }

    /**
     * 设置自定义加载布局的id, 和内置的TextView id
     */
    public void setCustomTabView(int layoutId, int textViewId) {
        mTabViewLayoutId = layoutId;
        mTabViewTextViewId = textViewId;
    }

    /**
     * 设置Tab分割模式
     */
    public void setDistributeMode(int distributeMode) {
        mDistributeMode = distributeMode;
        mTabStrip.setTabAsDividerMode(isTabAsDividerMode());
    }

    /**
     * 设置ColorList
     */
    public void setSelectedTitleColor(ColorStateList list) {
        this.mTitleTextColor = list;
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

    /**
     * 定义一个接口来返回tab名称底部坐标, 即返回底部的位置
     */
    public interface ITabNameBottomPositionGetter {
        int getTabNameBottomPosition(View selectedTitle);
    }

    /**
     * 为指示器设置BottomMargin值
     */
    public void setIndicatorBottomMargin(int indicatorBottomMargin) {
        mTabStrip.setIndicatorBottomMargin(indicatorBottomMargin);
    }

    /**
     * 为指示器设置TopMartin值
     */
    public void setIndicatorTopMargin(int indicatorTopMargin, ITabNameBottomPositionGetter positionGetter) {
        mTabStrip.setIndicatorTopMargin(indicatorTopMargin, positionGetter);
    }

    /**
     * 为指示器设置width值
     */
    public void setIndicatorWidth(int indicatorWidth) {
        mTabStrip.setIndicatorWidth(indicatorWidth);
    }

    /**
     * 为指示器设置厚度
     */
    public void setSelectedIndicatorThickness(float mSelectedIndicatorThickness) {
        mTabStrip.setSelectedIndicatorThickness(mSelectedIndicatorThickness);
    }

    /**
     * 为指示器设置圆角半径
     */
    public void setIndicatorCornerRadius(float indicatorCornerRadius) {
        mTabStrip.setIndicatorCornerRadius(indicatorCornerRadius);
    }

    /**
     * 为指示器设置颜色
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * 设置自定义的tab着色器
     */
    public void setCustomTabColorShader(TabColorShader tabColorShader) {
        mTabStrip.setCustomTabColorShader(tabColorShader);
    }

    /**
     * 设置指示器的动画模式
     */
    public void setIndicatorAnimationMode(int mode) {
        mTabStrip.setIndicatorAnimationMode(mode);
    }

    /*------------------------------------step 2 done-----------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*------------------------------------step 3 begin----------------------------------------*/
    /**
     * 设置与外界ViewPager的关联
     */

    /**
     * 与ViewPager建立关联, 与setupWithViewPager()设置差不多
     */
    public void setViewPager(ViewPager viewPager) {
        // 清空tab列表
        mTabStrip.removeAllViews();

        if (null != viewPager) {
            mViewPager = viewPager;
            viewPager.addOnPageChangeListener(new InternalViewPagerPageChangeListener());
            // 进行填充
            populateTabStrip();
        }
    }

    private class InternalViewPagerPageChangeListener implements ViewPager.OnPageChangeListener {
        private int mScrollState = 0; // 记录scroll的状态
        // 0:滑动结束->SCROLL_STATE_IDLE
        // 1:正在滑动->SCROLL_STATE_DRAGGING
        // 2:滑动完成（到达新页面）之后会变成0->SCROLL_STATE_SETTLING

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //Log.d(TAG, "onPageScrolled: ");
            int tabStripChildCount = getTabStripTabCount();
            // 如果索引错误, 不进行处理
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            // 把滑动也通知指示器
            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(getTabStripChildIndex(position));
            int extraOffset = (selectedTitle != null) ? (int) (positionOffset * selectedTitle.getWidth()) : 0;

            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                // 通知指示器滑动结束
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }

            // 改变TextView文本
            mCurrentPosition = position;
            changeText();

            int tabStripPosition = getTabStripChildIndex(position);

            // 改变指示器的状态
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                mTabStrip.getChildAt(i).setSelected(tabStripPosition == i);
            }

            Log.d(TAG, "onPageSelected: position = " + position + " tabStripPosition : "  + tabStripPosition);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            Log.d(TAG, "onPageScrollStateChanged: " + state);
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    }

    /**
     *  获取Tab的数量,
     */
    private int getTabStripTabCount() {
        int childCount = mTabStrip.getChildCount(); // 获取Tab的数量
        if (isTabAsDividerMode()) {
            childCount -= 2;
        }
        return childCount;
    }

    /**
     * 获取viewPager对应的指示器position
     */
    private int getTabStripChildIndex(int viewPagerIndex) {
        if (isTabAsDividerMode()) {
            viewPagerIndex++;
        }
        return viewPagerIndex;
    }

    /**
     * 定义一个方法来滑动到指定position的tab
     */
    private void scrollToTab(int viewPagerIndex, int positionOffset) {
        int tabStripChildCount = getTabStripTabCount();
        // 如果索引出错则不处理
        if (tabStripChildCount == 0 || viewPagerIndex < 0 || viewPagerIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(getTabStripChildIndex(viewPagerIndex));
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;
            if (viewPagerIndex > 0 || positionOffset > 0) {
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);

            //改为居中对齐，如果要换成原先的居左对齐方式的话把上面的注释去掉
//            int targetScrollX = (selectedChild.getLeft() + selectedChild.getRight() - getWidth()) / 2 + positionOffset;
//            scrollTo(targetScrollX, 0);
        }
    }

    /**
     * 设置文本的变化
     */
    private void changeText() {
        if (mCurrentPosition != mLastPosition) {
            TextView currentText = getTabTextView(getTabStripChildIndex(mCurrentPosition) + 1);
            if (currentText != null) {
                currentText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSelectedTitleSize);
                currentText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }

            TextView lastText = getTabTextView(getTabStripChildIndex(mLastPosition) + 1);
            if (lastText != null) {
                lastText.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                lastText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mNormalTitleSize);
            }
        }
        mLastPosition = mCurrentPosition;
    }

    private TextView getTabTextView(int stripIndex) {
        View tab = mTabStrip.getChildAt(getTabIndex(stripIndex));
        if (tab instanceof TextView) {
            return (TextView) tab;
        }

        if (tab != null) {
            return tab.findViewById(mTabViewTextViewId);
        }
        return null;
    }


    private int getTabIndex(int stripIndex) {
        if (isTabAsDividerMode()) {
            stripIndex--;
        }
        return stripIndex;
    }


    /**
     * 设置文本的大小 默认小文本16.33sp, 大文本22.67sp
     */
    public void setTextChangeSize(float normalTextSize, float selectedTextSize) {
        this.mSelectedTitleSize = selectedTextSize;
        this.mNormalTitleSize = normalTextSize;
    }

    /**
     * 设置ViewPager.OnPageChangeListener来保证ViewPager会被通知状态改变
     */
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.mViewPagerPageChangeListener = listener;
    }

    /*------------------------------------step 3 done-----------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/
    /*-----------------------------------other methods----------------------------------------*/

    /**
     * 重新绘制一次SlidingTabLayout
     */
    public void notifyDataChange() {
        mTabStrip.removeAllViews();
        populateTabStrip();
    }

    /**
     * 初始ViewPager的位置
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }


}
