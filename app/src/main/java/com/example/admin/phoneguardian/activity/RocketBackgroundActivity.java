package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.example.admin.phoneguardian.R;


public class RocketBackgroundActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_rocketbackground);
        ImageView ivMsmoke = findViewById(R.id.iv_msmoke);
        ImageView ivTsmoke = findViewById(R.id.iv_tsmoke);
        AlphaAnimation aa = new AlphaAnimation(0,1);
        aa.setDuration(1500);
        aa.setFillAfter(true);
        ivMsmoke.startAnimation(aa);
        ivTsmoke.startAnimation(aa);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },1000);
    }
}
