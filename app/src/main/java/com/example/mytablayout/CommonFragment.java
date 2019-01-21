package com.example.mytablayout;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.support.constraint.Constraints.TAG;

public class CommonFragment extends Fragment {
    private TextView mTextView;
    private String mTitle;
    private int mColor;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        mTextView = (TextView) view.findViewById(R.id.text);
        mTextView.setText(mTitle);
        mTextView.setTextColor(mColor);
        return view;
    }

    public void setTextView(String text, int color) {
        this.mTitle = text;
        this.mColor = color;
    }
}
