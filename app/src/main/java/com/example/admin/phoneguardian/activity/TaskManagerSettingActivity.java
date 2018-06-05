package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.admin.phoneguardian.R;

public class TaskManagerSettingActivity extends Activity {
    private SharedPreferences sp;
    private CheckBox cbShowSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskmanagersetting);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        showSystem();
    }

    private void showSystem() {
        cbShowSystem = findViewById(R.id.cb_showsystem);
        boolean showsystem = sp.getBoolean("showsystem", true);
        if (showsystem) {
            cbShowSystem.setChecked(true);
        } else {
            cbShowSystem.setChecked(false);
        }
        cbShowSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sp.edit().putBoolean("showsystem",true);
                }else{
                    sp.edit().putBoolean("showsystem",false);
                }
            }
        });
    }
}
