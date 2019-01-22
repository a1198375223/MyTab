package com.example.mytablayout;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.mytablayout.tab.SlidingTabLayout;
import com.example.mytablayout.utils.DisplayUtils;

public class MyTabActivity extends AppCompatActivity {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_tab_layout);

        SlidingTabLayout layout = (SlidingTabLayout) findViewById(R.id.my_tab_layout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        layout.setCustomTabView(R.layout.main, R.id.tab_text);
        layout.setSelectedIndicatorColors(getResources().getColor(R.color.sky_blue));
        layout.setIndicatorAnimationMode(SlidingTabLayout.ANI_MODE_TAIL);
        //layout.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER);
        //layout.setDistributeMode(SlidingTabLayout.DISTRIBUTE_MODE_TAB_IN_SECTION_CENTER);
        layout.setSelectedIndicatorThickness(2.33f);
        layout.setIndicatorWidth(DisplayUtils.getInstance(this).dip2px(20));
        layout.setIndicatorBottomMargin(DisplayUtils.getInstance(this).dip2px(6));
        layout.setSelectedTitleColor(getResources().getColorStateList(R.color.tab_text_color, null));

        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));

        layout.setViewPager(viewPager);
    }
}
