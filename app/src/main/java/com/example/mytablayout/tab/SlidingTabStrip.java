package com.example.mytablayout.tab;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.example.mytablayout.R;

import static android.content.ContentValues.TAG;


/**
 * 自定义指示器
 * 明确指示器中存在的内容
 * 例如：底部高度, 底部颜色, position位置, 默认的颜色, 被选中的颜色
 *
 * 步骤：
 * 1. 在构造器中进行必要的初始化
 * 2. 重写onDraw()方法, 进行绘制自定义的指示器
 */
public class SlidingTabStrip extends LinearLayout {
    private final int DEFAULT_BOTTOM_BORDER_COLOR_ALPHA     = 40;           // 默认底部边界透明值
    private final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIP   = 0;            // 默认的底部边界厚度
    private final int DEFAULT_SELECTED_INDICATOR_COLOR      = 0xFF33B5E5;   // tab被选中的指示器默认颜色
    private final float SELECTED_INDICATOR_THICKNESS_DIP    = 2.66f;        // 默认指示器的厚度（4px->4像素）
    private final int AVOID_DITHERING_THRESHOLD              = 4;            // 防止抖动的阈值

    private int mBottomBorderThickness;                                     // 底部边界的厚度
    private Paint mBottomBorderPaint;                                       // 绘制底部边界的画笔

    private float mSelectedIndicatorThickness;                              // 被选中指示器的厚度
    private Paint mSelectedIndicatorPaint;                                  // 指示器画笔

    private int mDefaultBottomBorderColor;                                  // 默认底部边界颜色
    private int mIndicatorAnimationMode = SlidingTabLayout.ANI_MODE_NORMAL; // 记录动画效果选择那个来实现
    private int mSelectedPosition = 0;                                      // 记录被选择的子view的position, 默认是0 即第一个被选中
    private float mSelectionOffset;                                         // 记录被选中的偏移量 -1->0->1 就是你手指拖动, 往左还是往右
    private int mIndicatorWidth;                                            // 记录指示器的宽度
    private float mIndicatorCornerRadius;                                   // 记录指示器的半径

    private SimpleTabColorShader mDefaultTabColorShader;                    // 默认的ColorShader
    private SlidingTabLayout.TabColorShader mCustomTabColorShader;          // 自定义的ColorShader

    private boolean mIsTabAsDividerMode;                                    // 判断tab是否是DISTRIBUTE_MODE_TAB_AS_DIVIDER分割模式
    private float mLastRight;                                               // 记录最新的right的位置
    private int mIndicatorTopMargin;                                        // 记录指示器top的margin值
    private int mIndicatorBottomMargin;                                     // 记录指示器bottom的margin值
    private GradientDrawable mIndicatorDrawable;                            // 记录指示器的图片

    // 用来返回tab对应的指示器坐标给SlidingTabStrip
    private SlidingTabLayout.ITabNameBottomPositionGetter mTabNameBottomPositionGetter;



    public SlidingTabStrip(Context context) {
        this(context, null);
    }

    public SlidingTabStrip(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false); //因为是继承ViewGroup 默认是不会经过onDraw()方法的 如果想要调用重写的onDraw()方法需要调用该方法

        // 获取系统资源 此时import的是android.R;而不是xxx.xxx.xxx(包名).R
        // 当导入的是（包名.R）的时候,可以引用较少（我也不知道要怎么描述）的资源
        // 参数说明：第一个参数为需要寻找的attr资源id
        //          第二个参数被填入属性提供的值,可以通过getValue来获取,
        //          第三个参数:true返回的outValue是TYPE_REFERENCE, false返回的是TYPE_ATTRIBUTE
        // 如果需要解析的对象是String可以使用
        // outValue.toString();
        // outValue.coerceToString();
        // 来获取
        //
        // 如果是其他的数据,可以使用
        // outValue.resourceId;
        // outValue.data;
        // 来获取
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);

        int themeForegroundColor = outValue.data;

        // 初始化底部边界的颜色
        mDefaultBottomBorderColor = setColorAlpha(themeForegroundColor, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

        // 获取手机设备的参数
        // dpi -> 单位尺寸的像素点
        // dm.widthPixels; -> 设备的绝对宽度（px）
        // dm.heightPixels; -> 设备的绝对高度（px）
        // dm.xdpi; -> x方向上的dpi
        // dm.ydpi; -> y方向上的dpi
        // dm.density; -> 屏幕密度
        // dm.densityDpi; -> 屏幕密度dpi
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;

        // 将底部边界厚度单位从dp->px
        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIP * density);

        // 初始化底部边界画笔
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(mDefaultBottomBorderColor);

        // 将指示器厚度单位从dp->px
        mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIP * density);
        mSelectedIndicatorPaint = new Paint();

        // 初始化TabColorShader
        mDefaultTabColorShader = new SimpleTabColorShader();
        mDefaultTabColorShader.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR);

        // 初始化指示器角的半径
        mIndicatorCornerRadius = getResources().getDimension(R.dimen.indicator_corner_radius);
    }

    /**
     * 设置颜色的透明度,将color设置成透明度为alpha
     */
    private int setColorAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * 这个类主要就是用来返回给SlidingTabLayout对应position的color
     */
    private class SimpleTabColorShader implements SlidingTabLayout.TabColorShader {
        private int[] mIndicatorColors;

        @Override
        public int getIndicatorColor(int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        // 设置color数组
        void setIndicatorColors(int... colors) {
            mIndicatorColors = colors;
        }
    }



    /**
     * 在onDraw绘制我们的tab
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        int height = getHeight(); // 获取高度
        int childCount = getChildCount(); // 获取子view的数量
        SlidingTabLayout.TabColorShader tabColorShader = mDefaultTabColorShader != null ? mDefaultTabColorShader : mCustomTabColorShader;

        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition); // 获取被选中的view
            if (null != selectedTitle) {
                float left = selectedTitle.getLeft(); // 获取子view的left
                float right = selectedTitle.getRight(); // 获取子view的right
                float leftMargin = 0; // 设置leftMargin

                //
                if (mIndicatorAnimationMode == SlidingTabLayout.ANI_MODE_TAIL && mIndicatorWidth > 0) {
                    leftMargin = (right - left - mIndicatorWidth) / 2.0f;
                }

                // 得到当前tab的指示器颜色
                int color = tabColorShader.getIndicatorColor(getTabIndex(mSelectedPosition));

                // 如果现在处于滑动状态, 获取即将进入的tab的指示器的颜色
                // 往右边滑动
                if (mSelectionOffset > 0 && mSelectedPosition < (getChildCount() - 1)) {
                    int nextColor = tabColorShader.getIndicatorColor(getTabIndex(mSelectedPosition + 1));
                    if (color != nextColor) {
                        color = blendColors(nextColor, color, mSelectionOffset);
                    }

                    // 获取即将进入的tab的View
                    View nextTitle = getChildAt(mSelectedPosition + 1);

                    // 处理动画效果
                    switch (mIndicatorAnimationMode) {

                        case SlidingTabLayout.ANI_MODE_NORMAL:  // 无特殊动画, 直接平移
                            left = (int) (mSelectionOffset * nextTitle.getLeft() + (1.0f - mSelectionOffset) * left);
                            right = (int) (mSelectionOffset * nextTitle.getRight() + (1.0f - mSelectionOffset) * right);
                            break;
                        case SlidingTabLayout.ANI_MODE_TAIL:    // 带动画效果 todo 测试一下什么效果, 我感觉是先拉伸然后逐渐变小
                            int moveDimen = (nextTitle.getWidth() + selectedTitle.getWidth()) / 2;
                            left += Math.pow(mSelectionOffset, 2) * moveDimen;
                            right += Math.sqrt(mSelectionOffset) * moveDimen;
                            break;
                        default:
                    }

                    mLastRight = right;
                }

                if (mIndicatorAnimationMode == SlidingTabLayout.ANI_MODE_NORMAL && mIndicatorWidth > 0) {
                    leftMargin = (right - left - mIndicatorWidth) / 2.0f;
                }

                //设置被选中指示器的画笔颜色
                mSelectedIndicatorPaint.setColor(color);

                if (mIndicatorTopMargin > 0) {
                    //画圆角矩形
                    int tabTitleBottom = mTabNameBottomPositionGetter.getTabNameBottomPosition(selectedTitle);
                    drawRoundRect((int) (left + leftMargin), tabTitleBottom + mIndicatorTopMargin,
                            (int) (right - leftMargin), (int) (tabTitleBottom + mIndicatorTopMargin + mSelectedIndicatorThickness),
                            color, canvas);

                } else {
                    drawRoundRect((int) (left + leftMargin), (int) (height - mIndicatorBottomMargin - mSelectedIndicatorThickness),
                            (int) (right - leftMargin), height - mIndicatorBottomMargin,
                            color, canvas);
                }
            } // end if
        }
        canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);
    }

    /**
     * 返回tab的position
     */
    private int getTabIndex(int childIndex) {
        if (mIsTabAsDividerMode) {
            childIndex--;
        }
        return childIndex;
    }

    /**
     * 按照比例(ratio)融合2个颜色
     * 1.0 return color1    0.5 平均融合2颜色   0 return color2
     */
    private int blendColors(int color1, int color2, float ratio) {
        float inverseRatio = 1.0f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRatio);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRatio);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRatio);
        return Color.rgb((int)r, (int)g, (int)b);
    }

    /**
     * 定义一个方法来绘制圆角矩形
     */
    private void drawRoundRect(int left, int top, int right, int bottom, int color, Canvas canvas) {
        if (mIndicatorDrawable == null) {
            mIndicatorDrawable = (GradientDrawable) getResources().getDrawable(R.drawable.tab_strip_drawable);
        }
        mIndicatorDrawable.setBounds(left, top, right, bottom);
        mIndicatorDrawable.setColor(color);
        mIndicatorDrawable.setCornerRadius(mIndicatorCornerRadius);
        mIndicatorDrawable.draw(canvas);
    }



    /*----------------------------------------------------------------------------------------*/
    /*-----------------------------------属性的set方法-----------------------------------------*/

    /**
     * 设置IndicatorBottomMargin
     */
    public void setIndicatorBottomMargin(int indicatorBottomMargin) {
        mIndicatorBottomMargin = indicatorBottomMargin;
        mIndicatorTopMargin = 0;
        mTabNameBottomPositionGetter = null;
        invalidate();
    }


    /**
     * 设置IndicatorTopMargin
     */
    public void setIndicatorTopMargin(int indicatorTopMargin, SlidingTabLayout.ITabNameBottomPositionGetter positionGetter) {
        mIndicatorTopMargin = indicatorTopMargin;
        mTabNameBottomPositionGetter = positionGetter;
        mIndicatorBottomMargin = 0;
        invalidate();
    }

    /**
     * 设置IndicatorWidth
     */
    public void setIndicatorWidth(int indicatorWidth) {
        mIndicatorWidth = indicatorWidth;
        invalidate();
    }

    /**
     * 设置SelectedIndicatorThickness
     */
    public void setSelectedIndicatorThickness(float selectedIndicatorThickness) {
        this.mSelectedIndicatorThickness = selectedIndicatorThickness;
    }

    /**
     * 设置IndicatorCornerRadius
     */
    public void setIndicatorCornerRadius(float indicatorCornerRadius) {
        this.mIndicatorCornerRadius = indicatorCornerRadius;
    }

    /**
     * 设置TabAsDividerMode
     */
    public void setTabAsDividerMode(boolean isTabAsDividerMode) {
        mIsTabAsDividerMode = isTabAsDividerMode;
    }

    /**
     * 设置IndicatorAnimationMode
     */
    public void setIndicatorAnimationMode(int mode) {
        this.mIndicatorAnimationMode = mode;
    }

    /**
     * 设置CustomTabColorShader
     */
    public void setCustomTabColorShader(SlidingTabLayout.TabColorShader customTabColorShader) {
        this.mCustomTabColorShader = customTabColorShader;
        invalidate();
    }

    /**
     * 为DefaultTabColorShader设置颜色
     */
    public void setSelectedIndicatorColors(int... colors) {
        mCustomTabColorShader = null;
        mDefaultTabColorShader.setIndicatorColors(colors);
        invalidate();
    }

    /*---------------------------------------end----------------------------------------------*/
    /*----------------------------------------------------------------------------------------*/


    /**
     * 最后定义一个方法来监听绑定的ViewPager的页面是否发生了变化
     */
    public void onViewPagerPageChanged(final int position, float positionOffset) {

        //Log.d(TAG, "onViewPagerPageChanged: offset = " + positionOffset);
        mSelectedPosition = getChildIndex(position);

        float right = -1;
        // 获取选中View的right
        if (getChildCount() > 0) {
            View view = getChildAt(mSelectedPosition);
            if (null != view) {
                right = view.getRight();
            }
        }

//        if (positionOffset == 0) { //防止一下抖动
//            if (mSelectionOffset > 0 && right != -1 && Math.abs(mLastRight - right) >= AVOID_DITHERING_THRESHOLD) {
//                //Log.d(TAG, "onAnimationUpdate: 触发的offset = " + mSelectionOffset + " final right : " + right + " now right : " + mLastRight);
//                final boolean goLeft = mLastRight > right;
//                ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
//                animator.setDuration(5000);
//                final float finalRight = right;
//                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        float currentValue = (float) animation.getAnimatedValue();
//                        if (goLeft) {
//                            mSelectionOffset = mSelectionOffset * currentValue;
//                        } else {
//                            mSelectionOffset = mSelectionOffset + (1 - mSelectionOffset) * (1 - currentValue);
//                        }
//                        //Log.d(TAG, "onAnimationUpdate: position = " + position + " offset = " + mSelectionOffset);
//                        invalidate();
//                    }
//                });
//                animator.setInterpolator(new AccelerateInterpolator());
//                animator.start();
//                return;
//            }
//        }
        mSelectionOffset = positionOffset;
        invalidate();
    }

    private int getChildIndex(int tabIndex) {
        if (mIsTabAsDividerMode) {
            tabIndex++;
        }
        return tabIndex;
    }
}
