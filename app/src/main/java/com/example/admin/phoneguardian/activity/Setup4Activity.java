package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.admin.phoneguardian.R;

public class Setup4Activity extends Activity {
    SharedPreferences sp;
    CheckBox cbConfiged;
    private GestureDetector detector;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup4);
        sp = getSharedPreferences("config",MODE_PRIVATE);
        cbConfiged = findViewById(R.id.cb_configed);
        boolean protect = sp.getBoolean("protect",false);
        if (protect){
            cbConfiged.setText("你已经开启防盗保护");
            cbConfiged.setChecked(true);
        }else{
            cbConfiged.setText("你没有开启防盗保护");
            cbConfiged.setChecked(false);
        }
        //CheckBox的点击事件，不是点击侦听，是setOnCheckedChangeListener
        cbConfiged.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    cbConfiged.setText("你已经开启防盗保护");
                    sp.edit().putBoolean("protect", true).apply();
                }else{
                    cbConfiged.setText("你没有开启防盗保护");
                    sp.edit().putBoolean("protect", false).apply();
                }
            }
        });
        detector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getRawX()-e1.getRawX()>200){
                    //返回上一页
                    startActivity(new Intent(Setup4Activity.this, Setup3Activity.class));
                    finish();
                    overridePendingTransition(R.anim.tranlate_previousin, R.anim.tranlate_previousout);
                    return true;
                }
                if (e1.getRawX()-e2.getRawX()>200){
                    //下一页
                    startActivity(new Intent(Setup4Activity.this, LostFindActivity.class));
                    finish();
                    overridePendingTransition(R.anim.tranlate_nextin, R.anim.tranlate_nextout);
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    public void next(View v){
        sp.edit().putBoolean("configed",true).apply();
        startActivity(new Intent(this,LostFindActivity.class));
        finish();
        overridePendingTransition(R.anim.tranlate_nextin,R.anim.tranlate_nextout);
    }
    public void previous(View v){
        startActivity(new Intent(this,Setup3Activity.class));
        finish();
        overridePendingTransition(R.anim.tranlate_previousin,R.anim.tranlate_previousout);
    }
}
