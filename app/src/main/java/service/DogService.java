package service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.example.admin.phoneguardian.activity.SetPwdActivity;

import db.AppLockdb;

public class DogService extends Service {
    private ActivityManager am;
    private AppLockdb lockdb;
    private boolean flag = false;
    private Intent watchIntent;
    private StopReceiver receiver;
    private String stopProtectPackName;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        lockdb = new AppLockdb(this);
        //动态注册广播接收者
        receiver = new StopReceiver();
        IntentFilter filter = new IntentFilter();
        //添加action
        filter.addAction("com.example.admin.phoneguardian.stopprotect");
        registerReceiver(receiver, filter);
        new Thread() {
            @Override
            public void run() {
                flag = true;
                while (flag) {
                    ActivityManager.RunningTaskInfo taskInfo = am.getRunningTasks(1).get(0);
                    String packName = taskInfo.topActivity.getPackageName();
                    if (lockdb.find(packName)) {
                        if (!packName.equals(stopProtectPackName)) {
                            //从服务中开启activty，要加flags，FLAG_ACTIVITY_NEW_TASK
                            watchIntent = new Intent(DogService.this, SetPwdActivity.class);
                            watchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            watchIntent.putExtra("packname", packName);
                            startActivity(watchIntent);
                        }
                    }
                }
            }
        }.start();
    }

    class StopReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.example.admin.phoneguardian.stopprotect")) {
                //停止对当前应用的软件锁
                stopProtectPackName = intent.getStringExtra("packname");
            }
        }
    }

    @Override
    public void onDestroy() {
        //服务关闭，关闭软件锁，取消注册广播接收者
        flag = false;
        unregisterReceiver(receiver);
        receiver = null;
        super.onDestroy();
    }
}
