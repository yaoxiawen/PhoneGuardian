package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

public class LostFindActivity extends Activity {
    TextView tvfindlostphone;
    ImageView ivProtect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        //读取本地数据，是否已经进行过设置
        Boolean configed = sp.getBoolean("configed", false);
        if (configed) {
            setContentView(R.layout.activity_lostfind);
            tvfindlostphone = findViewById(R.id.tv_lostfindphone);
            ivProtect = findViewById(R.id.iv_protect);
            String phone = sp.getString("phone", null);
            if (!TextUtils.isEmpty(phone)) {
                tvfindlostphone.setText(phone);
            }
            boolean protect = sp.getBoolean("protect", false);
            if (protect) {
                ivProtect.setImageResource(R.drawable.lock);
            } else {
                ivProtect.setImageResource(R.drawable.unlock);
            }
        } else {
            startActivity(new Intent(this, Setup1Activity.class));
            finish();
        }
    }

    public void reEnter(View v) {
        startActivity(new Intent(this, Setup1Activity.class));
        finish();
    }
}
