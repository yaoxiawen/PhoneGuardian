package com.example.admin.phoneguardian.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import utils.ToastUtils;

public class Setup2Activity extends Activity {
    private SettingItemView siv;
    private SharedPreferences sp;
    private GestureDetector detector;
    private TelephonyManager tm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup2);
        siv = findViewById(R.id.siv_sim);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        boolean sim = sp.getBoolean("sim", false);
        if (sim) {
            siv.setCb_status(true);
        } else {
            siv.setCb_status(false);
        }
        //自定义控件的点击侦听
        siv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (siv.ischecked()) {
                    siv.setCb_status(false);
                    sp.edit().putBoolean("sim", false).apply();
                } else {
                    siv.setCb_status(true);
                    sp.edit().putBoolean("sim", true).apply();
                    //获取电话管理器
                    tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    //动态获取权限
                    if (ActivityCompat.checkSelfPermission(Setup2Activity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(Setup2Activity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                    } else {
                        //获取sim卡序列号
                        String sinNumber = tm.getSimSerialNumber();
                        sp.edit().putString("simnumber", sinNumber).apply();
                    }

                }
            }
        });
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getRawX() - e1.getRawX() > 200) {
                    //返回上一页
                    startActivity(new Intent(Setup2Activity.this, Setup1Activity.class));
                    finish();
                    overridePendingTransition(R.anim.tranlate_previousin, R.anim.tranlate_previousout);
                    return true;
                }
                if (e1.getRawX() - e2.getRawX() > 200) {
                    //下一页
                    boolean sim = sp.getBoolean("sim", false);
                    if (sim) {
                        startActivity(new Intent(Setup2Activity.this, Setup3Activity.class));
                        finish();
                        overridePendingTransition(R.anim.tranlate_nextin, R.anim.tranlate_nextout);
                        return true;
                    } else {
                        ToastUtils.show(Setup2Activity.this, "SIM卡未绑定");
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    /**
     * 请求权限回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        String sinNumber = tm.getSimSerialNumber();
                        sp.edit().putString("simnumber", sinNumber).apply();
                    }
                } else {
                    ToastUtils.show(this, "" + "权限" + permissions[i] + "申请失败");
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void next(View v) {
        boolean sim = sp.getBoolean("sim", false);
        if (sim) {
            startActivity(new Intent(this, Setup3Activity.class));
            finish();
            overridePendingTransition(R.anim.tranlate_nextin, R.anim.tranlate_nextout);
        } else {
            ToastUtils.show(Setup2Activity.this, "SIM卡未绑定");
        }
    }

    public void previous(View v) {
        startActivity(new Intent(this, Setup1Activity.class));
        finish();
        overridePendingTransition(R.anim.tranlate_previousin, R.anim.tranlate_previousout);
    }
}
