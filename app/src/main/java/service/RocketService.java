package service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.admin.phoneguardian.R;
import com.example.admin.phoneguardian.activity.RocketBackgroundActivity;

public class RocketService extends Service {
    private WindowManager mWM;
    private View view;
    private int startX;
    private int startY;
    private int winWidth;
    private int winHeight;
    private WindowManager.LayoutParams params;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        winWidth = mWM.getDefaultDisplay().getWidth();
        winHeight = mWM.getDefaultDisplay().getHeight();
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.LEFT + Gravity.TOP;
        if (Build.VERSION.SDK_INT >= 26) {
            //8.0用的
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        view = View.inflate(this, R.layout.rocket, null);
        ImageView ivRocket = view.findViewById(R.id.iv_rocket);
        ivRocket.setBackgroundResource(R.drawable.rocket);
        AnimationDrawable ad = (AnimationDrawable) ivRocket.getBackground();
        ad.start();
        mWM.addView(view, params);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
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
                        mWM.updateViewLayout(view, params);
                        startX = endX;
                        startY = endY;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (params.x > (winWidth / 4 - view.getWidth() / 2)
                                && params.x < 3 * winWidth / 4
                                && params.y > winHeight - 2 * view.getHeight()) {
                            sendRocket();
                            //从服务里开启activity，要设flags，Intent.FLAG_ACTIVITY_NEW_TASK
                            //开启的activity最好启动模式最好为singleInstance
                            Intent intent = new Intent(RocketService.this, RocketBackgroundActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        break;
                }
                return false;
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                int y = msg.arg1;
                params.y = y;
                mWM.updateViewLayout(view, params);
            } else if (msg.what == 2) {
                params.x = 0;
                params.y = 0;
                mWM.updateViewLayout(view, params);
            }
        }
    };

    private void sendRocket() {
        params.x = winWidth / 2 - view.getWidth() / 2;
        mWM.updateViewLayout(view, params);
        new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10; i++) {
                    SystemClock.sleep(100);
                    int y = (winHeight - view.getHeight()) - (winHeight - view.getHeight()) / 8 * i;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = y;
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
                SystemClock.sleep(200);
                Message msg = handler.obtainMessage();
                msg.what = 2;
                handler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        if (mWM != null && view != null) {
            mWM.removeView(view);
            view = null;
        }
        super.onDestroy();
    }
}
