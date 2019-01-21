package com.example.mytablayout;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private String[] mTitles = new String[]{"this is one", "this is two", "this is three", "this is four"};
    private List<CommonFragment> mList;


    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        mList = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int i) {
        CommonFragment fragment = null;
        switch (i) {
            case 0:
                fragment = new CommonFragment();
                fragment.setTextView(mTitles[i], Color.RED);
                mList.add(fragment);
                break;
            case 1:
                fragment = new CommonFragment();
                fragment.setTextView(mTitles[i], Color.GRAY);
                mList.add(fragment);
                break;
            case 2:
                fragment = new CommonFragment();
                fragment.setTextView(mTitles[i], Color.YELLOW);
                mList.add(fragment);
                break;
            case 3:
                fragment = new CommonFragment();
                fragment.setTextView(mTitles[i], Color.BLUE);
                mList.add(fragment);
                break;
            default:

        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }
}
