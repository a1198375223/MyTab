package com.example.mytablayout.tab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import android.R;


public class SlidingTabStrip extends LinearLayout {
    private final int DEFAULT_BOTTOM_BORDER_COLOR_ALPHA     = 40;           // 默认底部边界透明值
    private final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIP   = 0;            // 默认的底部边界厚度
    private final int DEFAULT_SELECTED_INDICATOR_COLOR      = 0xFF33B5E5;   // tab被选中的指示器默认颜色
    private final float SELECTD_INDICATOR_THICKNESS_DIP     = 2.66f;        // 默认指示器的厚度（4px->4像素）

    private int mBottomBorderThickness;                                     // 底部边界的厚度
    private Paint mBottomBorderPaint;                                       // 绘制底部边界的画笔

    private float mSelectedIndicatorThickness;                              // 被选中指示器的厚度
    private Paint mSelectedIndicatorPaint;                                  // 指示器画笔

    private int mDefaultBottomBorderColor;                                  // 默认底部边界颜色

    private int mSelectedPosition = 0;                                      // 记录被选择的子view的position, 默认是0 即第一个被选中


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
        context.getTheme().resolveAttribute(R.attr.colorForeground, outValue, true);

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
        mSelectedIndicatorThickness = (int) (SELECTD_INDICATOR_THICKNESS_DIP * density);

        mSelectedIndicatorPaint = new Paint();
    }


    /**
     * 在onDraw绘制我们的tab
     */
    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);

        int height = getHeight(); // 获取高度
        int childCount = getChildCount(); // 获取子view的数量

        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition); // 获取被选中的view
            if (null != selectedTitle) {
                float left = selectedTitle.getLeft(); // 获取子view的left
                float right = selectedTitle.getRight(); // 获取子view的right
                float leftMargin = 0; // 设置leftMargin

                // todo



            }

        }
    }

    /**
     * 设置颜色的透明度,将color设置成透明度为alpha
     */
    private int setColorAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }


}
