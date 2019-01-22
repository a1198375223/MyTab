package com.example.mytablayout;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class TabActivity extends AppCompatActivity {

    private static final String TAG = "玩转Tab: MainActivity:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_layout);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        viewPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Toast.makeText(TabActivity.this, "onTabSelected: " + tab.getPosition(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onTabSelected: " + tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Toast.makeText(TabActivity.this, "onTabUnselected: "+ tab.getPosition(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onTabUnselected: "+ tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Toast.makeText(TabActivity.this, "onTabReselected: "+ tab.getPosition(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onTabReselected: " + tab.getPosition());
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                Log.d(TAG, "viewPager onPageScrolled: " + i + " -----------> " + v);
            }

            @Override
            public void onPageSelected(int i) {
                Log.d(TAG, "viewPager onPageSelected: " + i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.d(TAG, "viewPager onPageScrollStateChanged: " + i);
            }
        });
    }
}
