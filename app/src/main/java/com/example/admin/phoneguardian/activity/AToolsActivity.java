package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import utils.SmsUtils;

public class AToolsActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_atools);
    }

    /**
     * 号码归属地查询
     * @param v
     */
    public void numberAddressQuery(View v) {
        startActivity(new Intent(this, AddressActivity.class));
    }

    /**
     * 短信备份，注意接口和回调
     * @param v
     */
    public void smsBackup(View v) {
        //ProgressDialog，带进度条的对话框
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("备份短信中：");
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.show();
        //开启子线程进行短信备份
        new Thread() {
            @Override
            public void run() {
                //判断SD卡状态
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    //SD卡ok
                    boolean result = SmsUtils.backup(AToolsActivity.this, new SmsUtils.SmsBackupCallBack() {
                        //回调得到数据
                        @Override
                        public void beforeSmsBackup(int total) {
                            pd.setMax(total);
                        }

                        @Override
                        public void onSmsBackup(int progress) {
                            pd.setMessage("已备份短信" + progress + "条");//没有
                            pd.setProgress(progress);
                        }
                    });
                    if (result) {
                        pd.dismiss();
                        //子线程无法更新UI，在子线程弹吐司用Looper
                        //Looper消息轮询器，手动去取消息
                        Looper.prepare();
                        Toast.makeText(AToolsActivity.this, "短信备份成功", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        Toast.makeText(AToolsActivity.this, "短信备份失败", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    Looper.prepare();
                    Toast.makeText(AToolsActivity.this, "SD卡不可用", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }.start();
    }

    /**
     * 软件锁
     * @param v
     */
    public void appLock(View v) {
        startActivity(new Intent(this, AppLockActivity.class));
    }
}
