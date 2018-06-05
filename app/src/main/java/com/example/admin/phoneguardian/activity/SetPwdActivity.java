package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import utils.MD5Utils;

public class SetPwdActivity extends Activity implements View.OnClickListener {
    private EditText etPassword;
    private String packname;
    private Button bt0;
    private Button bt1;
    private Button bt2;
    private Button bt3;
    private Button bt4;
    private Button bt5;
    private Button bt6;
    private Button bt7;
    private Button bt8;
    private Button bt9;
    private Button btClear;
    private Button btDelete;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setpwd);
        init();

    }

    private void init() {
        //获取跳转过来时传递过来的数据，应用的包名
        Intent intent = getIntent();
        packname = intent.getStringExtra("packname");
        etPassword = findViewById(R.id.et_password);
        //EditText隐藏键盘
        etPassword.setInputType(InputType.TYPE_NULL);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        bt0 = findViewById(R.id.bt_0);
        bt1 = findViewById(R.id.bt_1);
        bt2 = findViewById(R.id.bt_2);
        bt3 = findViewById(R.id.bt_3);
        bt4 = findViewById(R.id.bt_4);
        bt5 = findViewById(R.id.bt_5);
        bt6 = findViewById(R.id.bt_6);
        bt7 = findViewById(R.id.bt_7);
        bt8 = findViewById(R.id.bt_8);
        bt9 = findViewById(R.id.bt_9);
        btClear = findViewById(R.id.bt_clear);
        btDelete = findViewById(R.id.bt_delete);
        bt0.setOnClickListener(this);
        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);
        bt3.setOnClickListener(this);
        bt4.setOnClickListener(this);
        bt5.setOnClickListener(this);
        bt6.setOnClickListener(this);
        bt7.setOnClickListener(this);
        bt8.setOnClickListener(this);
        bt9.setOnClickListener(this);
        btClear.setOnClickListener(this);
        btDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String str;
        switch (v.getId()) {
            case R.id.bt_0:
                str = etPassword.getText().toString();
                etPassword.setText(str + "0");
                break;
            case R.id.bt_1:
                str = etPassword.getText().toString();
                etPassword.setText(str + "1");
                break;
            case R.id.bt_2:
                str = etPassword.getText().toString();
                etPassword.setText(str + "2");
                break;
            case R.id.bt_3:
                str = etPassword.getText().toString();
                etPassword.setText(str + "3");
                break;
            case R.id.bt_4:
                str = etPassword.getText().toString();
                etPassword.setText(str + "4");
                break;
            case R.id.bt_5:
                str = etPassword.getText().toString();
                etPassword.setText(str + "5");
                break;
            case R.id.bt_6:
                str = etPassword.getText().toString();
                etPassword.setText(str + "6");
                break;
            case R.id.bt_7:
                str = etPassword.getText().toString();
                etPassword.setText(str + "7");
                break;
            case R.id.bt_8:
                str = etPassword.getText().toString();
                etPassword.setText(str + "8");
                break;
            case R.id.bt_9:
                str = etPassword.getText().toString();
                etPassword.setText(str + "9");
                break;
            case R.id.bt_clear:
                etPassword.setText("");
                break;
            case R.id.bt_delete:
                str = etPassword.getText().toString();
                if (str.length() == 0) {
                    return;
                }
                etPassword.setText(str.substring(0, str.length() - 1));
                break;
        }
    }

    public void enter(View v) {
        String password = etPassword.getText().toString().trim();
        String savedPassword = sp.getString("password", null);
        if (!TextUtils.isEmpty(password)) {
            if (MD5Utils.encode(password).equals(savedPassword)) {
                //密码正确，关闭当前界面
                finish();
                //发送广播，停止软件锁
                Intent intent = new Intent();
                //自定义发送广播的action
                intent.setAction("com.example.admin.phoneguardian.stopprotect");
                //传递数据
                intent.putExtra("packname",packname);
                sendBroadcast(intent);
            } else {
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "输入框内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * 用户按返回键时调用该方法
     */
    @Override
    public void onBackPressed() {
        //按返回键时，进入手机桌面
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
    }


}
