package com.example.admin.phoneguardian.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;


public class SettingItemClickView extends RelativeLayout {
    TextView tv_title;
    TextView tv_desc;

    public SettingItemClickView(Context context) {
        super(context);
        InitView();
    }

    public SettingItemClickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        InitView();
    }

    public SettingItemClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitView();
    }

    private void InitView() {
        View.inflate(getContext(), R.layout.view_settingitemclick, this);
        tv_title = findViewById(R.id.tv_title);
        tv_desc = findViewById(R.id.tv_desc);
    }

    public void setTvtitle(String s) {
        tv_title.setText(s);

    }
    public void setTvdesc(String s) {
        tv_desc.setText(s);
    }
}
