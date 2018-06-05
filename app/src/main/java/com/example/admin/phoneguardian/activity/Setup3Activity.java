package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.example.admin.phoneguardian.R;

import utils.ToastUtils;

public class Setup3Activity extends Activity {
    private GestureDetector detector;
    private EditText etContact;
    private SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup3);
        etContact = findViewById(R.id.et_contact);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        String phone = sp.getString("phone", null);
        if (!TextUtils.isEmpty(phone)) {
            etContact.setText(phone);
        }
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e2.getRawX() - e1.getRawX() > 200) {
                    //返回上一页
                    startActivity(new Intent(Setup3Activity.this, Setup2Activity.class));
                    finish();
                    overridePendingTransition(R.anim.tranlate_previousin, R.anim.tranlate_previousout);
                    return true;
                }
                if (e1.getRawX() - e2.getRawX() > 200) {
                    //下一页
                    String phone = etContact.getText().toString().trim();
                    if (!TextUtils.isEmpty(phone)) {
                        sp.edit().putString("phone", phone).apply();
                        startActivity(new Intent(Setup3Activity.this, Setup4Activity.class));
                        finish();
                        overridePendingTransition(R.anim.tranlate_nextin, R.anim.tranlate_nextout);
                        return true;
                    } else {
                        ToastUtils.show(Setup3Activity.this, "安全号码不能为空");
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void next(View v) {
        //trim()去除字符串中的空格
        String phone = etContact.getText().toString().trim();
        if (!TextUtils.isEmpty(phone)) {
            //当要跳转到下一个页面时，才读取数据并保存
            sp.edit().putString("phone", phone).apply();
            startActivity(new Intent(this, Setup4Activity.class));
            finish();
            overridePendingTransition(R.anim.tranlate_nextin, R.anim.tranlate_nextout);
        } else {
            ToastUtils.show(Setup3Activity.this, "安全号码不能为空");
        }
    }

    public void previous(View v) {
        startActivity(new Intent(this, Setup2Activity.class));
        finish();
        overridePendingTransition(R.anim.tranlate_previousin, R.anim.tranlate_previousout);
    }

    public void selectContact(View v) {
        //获取手机联系人
        Intent intent = new Intent(this, ContactActivity.class);
        //请求码为1，返回时要传递数据
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //请求码为1，结果码为Activity.RESULT_OK，用户才是选择了联系人的，将结果显示
        //当用户是按了返回键返回时，什么都不执行，就不会出现空指针异常
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            String phone = data.getStringExtra("phone");
            etContact.setText(phone);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
