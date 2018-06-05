package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

import service.AddressService;
import service.DogService;
import service.RocketService;
import service.InterceptService;
import utils.ServiceStatusUtils;


public class SettingActivity extends Activity {
    private SettingItemView sivUpdate;
    private SettingItemView sivAddress;
    private SettingItemClickView sicvAddressStyle;
    private SettingItemClickView sicvAddressLocation;
    private SharedPreferences sp;
    private WindowManager mWM;
    private View view;
    private int startX;
    private int startY;
    private int winWidth;
    private int winHeight;
    private SettingItemView sivRocket;
    private SettingItemView sivSms;
    private SettingItemView sivDog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setting);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        //获取屏幕的宽高
        winWidth = getWindowManager().getDefaultDisplay().getWidth();
        winHeight = getWindowManager().getDefaultDisplay().getHeight();
        updateView();
        addressView();
        addressStyleView();
        addressLocationView();
        rocketView();
        smsView();
        dogView();
    }

    private void dogView() {
        sivDog = findViewById(R.id.siv_dog);
        boolean serviceRunning = ServiceStatusUtils.isServiceRunning(this, "service.DogService");
        boolean dog = sp.getBoolean("dog", true);
        if (dog) {
            sivDog.setCb_status(true);
            if (!serviceRunning) {
                startService(new Intent(SettingActivity.this, DogService.class));
            }
        } else {
            sivDog.setCb_status(false);
            if (serviceRunning) {
                stopService(new Intent(SettingActivity.this, DogService.class));
            }
        }
        sivDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sivDog.ischecked()) {
                    sivDog.setCb_status(false);
                    sp.edit().putBoolean("dog", false).apply();
                    stopService(new Intent(SettingActivity.this, DogService.class));
                } else {
                    sivDog.setCb_status(true);
                    sp.edit().putBoolean("dog", true).apply();
                    startService(new Intent(SettingActivity.this, DogService.class));
                }
            }
        });

    }

    private void smsView() {
        sivSms = findViewById(R.id.siv_sms);
        boolean serviceRunning = ServiceStatusUtils.isServiceRunning(this, "service.InterceptService");
        boolean smsIntercept = sp.getBoolean("intercept", true);
        if (smsIntercept) {
            sivSms.setCb_status(true);
            if (!serviceRunning) {
                startService(new Intent(SettingActivity.this, InterceptService.class));
            }
        } else {
            sivSms.setCb_status(false);
            if (serviceRunning) {
                stopService(new Intent(SettingActivity.this, InterceptService.class));
            }
        }
        sivSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sivSms.ischecked()) {
                    sivSms.setCb_status(false);
                    sp.edit().putBoolean("intercept", false).apply();
                    stopService(new Intent(SettingActivity.this, InterceptService.class));
                } else {
                    sivSms.setCb_status(true);
                    sp.edit().putBoolean("intercept", true).apply();
                    startService(new Intent(SettingActivity.this, InterceptService.class));
                }
            }
        });
    }

    private void rocketView() {
        sivRocket = findViewById(R.id.siv_rocket);
        boolean serviceRunning = ServiceStatusUtils.isServiceRunning(this, "service.RocketService");
        boolean rocket = sp.getBoolean("rocket", false);
        if (rocket) {
            sivRocket.setCb_status(true);
            if (!serviceRunning) {
                startService(new Intent(SettingActivity.this, RocketService.class));
            }
        } else {
            sivRocket.setCb_status(false);
            if (serviceRunning) {
                stopService(new Intent(SettingActivity.this, RocketService.class));
            }
        }
        sivRocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sivRocket.ischecked()) {
                    sivRocket.setCb_status(false);
                    sp.edit().putBoolean("rocket", false).apply();
                    stopService(new Intent(SettingActivity.this, RocketService.class));
                } else {
                    sivRocket.setCb_status(true);
                    sp.edit().putBoolean("rocket", true).apply();
                    startService(new Intent(SettingActivity.this, RocketService.class));
                }
            }
        });
    }

    private void addressLocationView() {
        sicvAddressLocation = findViewById(R.id.sicv_addresslocation);
        sicvAddressLocation.setTvtitle("电话归属地显示位置");
        sicvAddressLocation.setTvdesc("设置电话归属地提示框的显示位置");
        sicvAddressLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingActivity.this, AddressLocationActivity.class));
            }
        });
    }

    private void addressStyleView() {
        sicvAddressStyle = findViewById(R.id.sicv_addressstyle);
        sicvAddressStyle.setTvtitle("电话归属地显示风格");
        final int addressStyle1 = sp.getInt("address_style", 0);
        final String[] choose = new String[]{"半透明", "活力橙", "卫士蓝", "金属灰", "苹果绿"};
        sicvAddressStyle.setTvdesc(choose[addressStyle1]);
        sicvAddressStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int addressStyle2 = sp.getInt("address_style", 0);
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setTitle("电话归属地显示风格:");
                builder.setSingleChoiceItems(choose, addressStyle2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        sp.edit().putInt("address_style", i).apply();
                        sicvAddressStyle.setTvdesc(choose[i]);
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    private void addressView() {
        sivAddress = findViewById(R.id.siv_address);
        boolean serviceRunning = ServiceStatusUtils.isServiceRunning(this, "service.AddressService");
        boolean phoneAddress = sp.getBoolean("phone_address", true);
        if (phoneAddress) {
            sivAddress.setCb_status(true);
            if (!serviceRunning) {
                startService(new Intent(SettingActivity.this, AddressService.class));
            }
        } else {
            sivAddress.setCb_status(false);
            if (serviceRunning) {
                stopService(new Intent(SettingActivity.this, AddressService.class));
            }
        }
        sivAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sivAddress.ischecked()) {
                    sivAddress.setCb_status(false);
                    sp.edit().putBoolean("phone_address", false).apply();
                    stopService(new Intent(SettingActivity.this, AddressService.class));
                } else {
                    sivAddress.setCb_status(true);
                    sp.edit().putBoolean("phone_address", true).apply();
                    startService(new Intent(SettingActivity.this, AddressService.class));
                }
            }
        });
    }

    private void updateView() {
        sivUpdate = findViewById(R.id.siv_update);
        boolean autoUpdate = sp.getBoolean("auto_update", true);
        if (autoUpdate) {
            sivUpdate.setCb_status(true);
        } else {
            sivUpdate.setCb_status(false);
        }
        sivUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sivUpdate.ischecked()) {
                    sivUpdate.setCb_status(false);
                    sp.edit().putBoolean("auto_update", false).apply();
                } else {
                    sivUpdate.setCb_status(true);
                    sp.edit().putBoolean("auto_update", true).apply();
                }
            }
        });
    }

    public void click1(View v) {
        if (Build.VERSION.SDK_INT >= 23) {
            // 动态权限申请， 是否允许创建悬浮窗
            if (Settings.canDrawOverlays(this)) {
                showToast();
            } else {
                // 跳转至设置界面获取权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(intent);
            }
        } else {
            // 如果targetSDKVersion < 6.0 直接展示窗体
            showToast();
        }
    }

    public void click2(View v) {
        if (mWM != null && view != null) {
            mWM.removeView(view);
            view = null;
        }
    }

    private void showToast() {
        //获取window管理器，在窗体上显示view，悬浮窗
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        //设定参数
        final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        mParams.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT >= 26) {
            //8.0用的
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        int x = sp.getInt("startX", 0);
        int y = sp.getInt("startY", 0);
        //设置坐标
        mParams.x = x;
        mParams.y = y;
        mParams.gravity = Gravity.LEFT + Gravity.TOP;

        int style = sp.getInt("address_style", 0);
        int[] bgs = new int[]{R.drawable.call_white, R.drawable.call_orange,
                R.drawable.call_blue, R.drawable.call_gray, R.drawable.call_green};
        //设置布局
        view = View.inflate(this, R.layout.toast_phoneaddress, null);
        view.setBackgroundResource(bgs[style]);
        TextView tvToast = view.findViewById(R.id.tv_toast);
        tvToast.setText("YYYYYYY");
        //显示
        mWM.addView(view, mParams);
        //设置触摸侦听
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //获取相对于屏幕的触摸点坐标
                        startX = (int) motionEvent.getRawX();
                        startY = (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int endX = (int) motionEvent.getRawX();
                        int endY = (int) motionEvent.getRawY();
                        int dx = endX - startX;
                        int dy = endY - startY;
                        mParams.x += dx;
                        mParams.y += dy;
                        if (mParams.x < 0) {
                            mParams.x = 0;
                        }
                        if (mParams.x > winWidth - view.getWidth()) {
                            mParams.x = winWidth - view.getWidth();
                        }
                        if (mParams.y < 0) {
                            mParams.y = 0;
                        }
                        if (mParams.y > winHeight - view.getHeight()) {
                            mParams.y = winHeight - view.getHeight();
                        }
                        //更新布局
                        mWM.updateViewLayout(view, mParams);
                        startX = endX;
                        startY = endY;
                        break;
                    case MotionEvent.ACTION_UP:
                        sp.edit().putInt("startX", mParams.x).apply();
                        sp.edit().putInt("startY", mParams.y).apply();
                        break;
                }
                return false;
            }
        });
    }
}
