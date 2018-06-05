package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

import db.AddressDB;

public class AddressActivity extends Activity {
    private EditText etAddress;
    private TextView tvAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_address);
        etAddress = findViewById(R.id.et_address);
        tvAddress = findViewById(R.id.tv_address);
        //EditText设置文本改变监听
        etAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String result = AddressDB.getAddress(editable.toString().trim());
                tvAddress.setText(result);
            }
        });
    }

    public void query(View v) {
        String num = etAddress.getText().toString().trim();
        if (!TextUtils.isEmpty(num)) {
            String result = AddressDB.getAddress(num);
            tvAddress.setText(result);
        }else{
            //获取震动器
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(500);
        }
    }
}
