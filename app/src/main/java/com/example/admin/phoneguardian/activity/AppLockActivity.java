package com.example.admin.phoneguardian.activity;

import android.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;
//软件锁
public class AppLockActivity extends FragmentActivity implements View.OnClickListener {
    private TextView tvUnlock;
    private TextView tvLock;
    private UnlockFragment fgUnlock;
    private LockFragment fgLock;
    private android.app.FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_app_lock);
        init();
    }

    private void init() {
        tvUnlock = findViewById(R.id.tv_unlock);
        tvLock = findViewById(R.id.tv_lock);
        //获取Fragment管理器
        fm = getFragmentManager();
        //通过Fragment管理器打开事务
        FragmentTransaction ft = fm.beginTransaction();
        //创建Fragment对象
        fgUnlock = new UnlockFragment();
        fgLock = new LockFragment();
        //通过事务把内容显示至帧布局
        ft.replace(R.id.fl, fgUnlock);
        //事务提交
        ft.commit();
        tvUnlock.setOnClickListener(this);
        tvLock.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction ft = fm.beginTransaction();
        switch (v.getId()) {
            case R.id.tv_unlock:
                tvUnlock.setBackgroundResource(R.drawable.tab_left_pressed);
                tvLock.setBackgroundResource(R.drawable.tab_right_default);
                ft.replace(R.id.fl, fgUnlock);
                break;
            case R.id.tv_lock:
                tvLock.setBackgroundResource(R.drawable.tab_right_pressed);
                tvUnlock.setBackgroundResource(R.drawable.tab_left_default);
                ft.replace(R.id.fl, fgLock);
                break;
        }
        ft.commit();
    }
}