package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

public class AddressLocationActivity extends Activity {
    private TextView tvTop;
    private TextView tvBottom;
    private ImageView ivAddressLocation;
    private SharedPreferences sp;
    private int startX;
    private int startY;
    private int winWidth;
    private int winHeight;
    private long[] mHits;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_addresslocation);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        //获取屏幕的宽高
        winWidth = getWindowManager().getDefaultDisplay().getWidth();
        winHeight = getWindowManager().getDefaultDisplay().getHeight();
        tvTop = findViewById(R.id.tv_top);
        tvBottom = findViewById(R.id.tv_bottom);
        ivAddressLocation = findViewById(R.id.iv_addresslocation);
        int x = sp.getInt("startX", 0);
        int y = sp.getInt("startY", 0);
        if (y > winHeight / 2) {
            tvTop.setVisibility(View.VISIBLE);
            tvBottom.setVisibility(View.INVISIBLE);
        } else {
            tvTop.setVisibility(View.INVISIBLE);
            tvBottom.setVisibility(View.VISIBLE);
        }
        //给ImageView初始化坐标
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivAddressLocation.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        ivAddressLocation.setLayoutParams(params);
        //设置触摸侦听
        ivAddressLocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) motionEvent.getRawX();
                        startY = (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int endX = (int) motionEvent.getRawX();
                        int endY = (int) motionEvent.getRawY();
                        int dx = endX - startX;
                        int dy = endY - startY;
                        //左右上下的坐标
                        int l = ivAddressLocation.getLeft() + dx;
                        int r = ivAddressLocation.getRight() + dx;
                        int t = ivAddressLocation.getTop() + dy;
                        int b = ivAddressLocation.getBottom() + dy;
                        if (l < 0 || r > winWidth || t < 0 || b > winHeight - 30) {
                            break;
                        }
                        if (t > winHeight / 2) {
                            tvTop.setVisibility(View.VISIBLE);
                            tvBottom.setVisibility(View.INVISIBLE);
                        } else {
                            tvTop.setVisibility(View.INVISIBLE);
                            tvBottom.setVisibility(View.VISIBLE);
                        }
                        //设置左右上下的坐标
                        ivAddressLocation.layout(l, t, r, b);
                        startX = endX;
                        startY = endY;
                        break;
                    case MotionEvent.ACTION_UP:
                        sp.edit().putInt("startX", ivAddressLocation.getLeft()).apply();
                        sp.edit().putInt("startY", ivAddressLocation.getTop()).apply();
                        break;
                }
                //触摸侦听要返回false，才能再设置点击侦听
                //false表示事件会传递
                return false;
            }
        });

        //多击事件
        mHits = new long[2];//双击事件
        //设置点击侦听
        ivAddressLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.arraycopy(mHits,1,mHits,0,mHits.length-1);
                mHits[mHits.length-1] = SystemClock.uptimeMillis();
                if (mHits[0]>=(SystemClock.uptimeMillis()-500)){
                    ivAddressLocation.layout(winWidth/2-ivAddressLocation.getWidth()/2,
                            winHeight/2-ivAddressLocation.getHeight()/2,
                            winWidth/2+ivAddressLocation.getWidth()/2,
                            winHeight/2+ivAddressLocation.getHeight()/2);
                    sp.edit().putInt("startX", ivAddressLocation.getLeft()).apply();
                    sp.edit().putInt("startY", ivAddressLocation.getTop()).apply();
                }
            }
        });
    }
}
