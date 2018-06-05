package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

import db.AddressDB;

public class AddressService extends Service {
    private TelephonyManager tm;
    private MyPhoneStateListener listener;
    private OutCallReceiver receiver;
    private WindowManager mWM;
    private View view;
    private SharedPreferences sp;
    private int startX;
    private int startY;
    private int winWidth;
    private int winHeight;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences("config", MODE_PRIVATE);
        //获取电话管理器
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        //监听电话状态
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        //监听去电的广播接收者，代码注册
        receiver = new OutCallReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(receiver, filter);
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                //电话状态为响铃，获取电话归属地，并展示
                case TelephonyManager.CALL_STATE_RINGING:
                    String address = AddressDB.getAddress(incomingNumber);
                    showToast(address);
                    break;
                //电话状态为空闲，取消展示
                case TelephonyManager.CALL_STATE_IDLE:
                    if (mWM != null && view != null) {
                        mWM.removeView(view);
                        view = null;
                    }
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /**
     * 去电的广播接收者
     */
    class OutCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //从广播中获取号码数据
            String number = getResultData();
            String address = AddressDB.getAddress(number);
            showToast(address);
        }
    }

    /**
     * 展示电话归属地的悬浮窗
     * @param address 归属地
     */
    private void showToast(String address) {
        //试验在SettingActivity中
        //获取window管理器，在窗体上显示view，悬浮窗
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        //获取屏幕的宽高
        winWidth = mWM.getDefaultDisplay().getWidth();
        winHeight = mWM.getDefaultDisplay().getHeight();
        //设定参数
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        if (Build.VERSION.SDK_INT >= 26) {
            //8.0用的
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        int x = sp.getInt("startX", 0);
        int y = sp.getInt("startY", 0);
        //设置坐标
        params.x = x;
        params.y = y;
        params.gravity = Gravity.LEFT + Gravity.TOP;

        int style = sp.getInt("address_style", 0);
        int[] bgs = new int[]{R.drawable.call_white, R.drawable.call_orange,
                R.drawable.call_blue, R.drawable.call_gray, R.drawable.call_green};
        //设置布局
        view = View.inflate(this, R.layout.toast_phoneaddress, null);
        view.setBackgroundResource(bgs[style]);
        TextView tvToast = view.findViewById(R.id.tv_toast);
        tvToast.setText(address);
        //显示
        mWM.addView(view, params);
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
                        params.x += dx;
                        params.y += dy;
                        if (params.x < 0) {
                            params.x = 0;
                        }
                        if (params.x > winWidth - view.getWidth()) {
                            params.x = winWidth - view.getWidth();
                        }
                        if (params.y < 0) {
                            params.y = 0;
                        }
                        if (params.y > winHeight - view.getHeight()) {
                            params.y = winHeight - view.getHeight();
                        }
                        //更新布局
                        mWM.updateViewLayout(view, params);
                        startX = endX;
                        startY = endY;
                        break;
                    case MotionEvent.ACTION_UP:
                        sp.edit().putInt("startX", params.x).apply();
                        sp.edit().putInt("startY", params.y).apply();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        //取消来电的监听
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        //取消注册去电的广播接收者
        unregisterReceiver(receiver);
        receiver = null;
        super.onDestroy();
    }
}
