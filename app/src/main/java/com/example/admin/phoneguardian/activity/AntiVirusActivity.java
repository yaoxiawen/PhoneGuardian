package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import db.VirusDB;
import utils.MD5Utils;
//杀毒
public class AntiVirusActivity extends Activity {
    private ImageView ivScan;
    private ProgressBar pbScan;
    private TextView tvScan;
    private TextView tvNumber;
    private LinearLayout llVirus;
    private List<ScanInfo> virusInfos = new ArrayList<>();
    private static final int SCANING = 0;
    private static final int SCAN_FINISH = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCANING:
                    ScanInfo scanInfo = (ScanInfo) msg.obj;
                    tvScan.setText("正在进行快速扫描");
                    tvNumber.setText("已扫描" + msg.arg1 + "个软件，发现病毒" + msg.arg2 + "个");
                    TextView textView = new TextView(AntiVirusActivity.this);
                    if (scanInfo.isVirus) {
                        textView.setText("发现病毒:" + scanInfo.appName);
                        textView.setTextColor(Color.RED);
                    } else {
                        textView.setText("扫描安全:" + scanInfo.appName);
                        textView.setTextColor(Color.BLACK);
                    }
                    //线性布局添加组件，第二个参数表示组件加在什么位置
                    llVirus.addView(textView, 0);
                    break;
                case SCAN_FINISH:
                    tvScan.setText("扫描完毕");
                    ivScan.clearAnimation();
                    if (virusInfos.size() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AntiVirusActivity.this);
                        builder.setTitle("警告!!");
                        builder.setMessage("发现病毒：" + virusInfos.size() + "个，手机已经十分危险，赶快杀毒!!!");
                        builder.setNegativeButton("下次再说", null);
                        builder.setPositiveButton("立即清除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (ScanInfo scanInfo : virusInfos) {
                                    Intent intent = new Intent(Intent.ACTION_DELETE);
                                    intent.setData(Uri.parse("package:" + scanInfo.packageName));
                                    startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    } else {
                        Toast.makeText(getApplicationContext(), "你的手机很安全了，继续加油哦！", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_antivirus);
        init();
    }

    private void init() {
        ivScan = findViewById(R.id.iv_scan);
        pbScan = findViewById(R.id.pb_scan);
        tvScan = findViewById(R.id.tv_scan);
        tvNumber = findViewById(R.id.tv_number);
        llVirus = findViewById(R.id.ll_virus);
        rotateAnim();
        new Thread() {
            @Override
            public void run() {
                PackageManager pm = getPackageManager();
                //获取已安装的包信息，传入参数不是0
                //获取的信息除了安装的，还包括卸载的但没有卸载干净的（保留有数据的应用），
                // 传入参数PackageManager.GET_UNINSTALLED_PACKAGES
                //此外，还需要应用的签名信息，传入参数PackageManager.GET_SIGNATURES
                List<PackageInfo> infos = pm.getInstalledPackages(
                        PackageManager.GET_UNINSTALLED_PACKAGES +
                                PackageManager.GET_SIGNATURES);
                pbScan.setMax(infos.size());
                int progress = 0;
                int virus = 0;
                Random random = new Random();
                for (PackageInfo info : infos) {
                    ScanInfo scanInfo = new ScanInfo();
                    scanInfo.appName = info.applicationInfo.loadLabel(pm).toString();
                    scanInfo.packageName = info.packageName;
                    String md5 = MD5Utils.encode(info.signatures[0].toCharsString());
                    String result = VirusDB.isVirus(md5);
                    scanInfo.desc = result;
                    if (result != null) {
                        scanInfo.isVirus = true;
                        virus++;
                        virusInfos.add(scanInfo);
                    } else {
                        scanInfo.isVirus = false;
                    }
                    progress++;
                    Message message = Message.obtain();
                    message.what = SCANING;
                    message.obj = scanInfo;
                    message.arg1 = progress;
                    message.arg2 = virus;
                    handler.sendMessage(message);
                    pbScan.setProgress(progress);
                    SystemClock.sleep(50 + random.nextInt(50));
                }
                //扫描完成
                Message message = Message.obtain();
                message.what = SCAN_FINISH;
                handler.sendMessage(message);
            }
        }.start();
    }

    class ScanInfo {
        boolean isVirus;
        String desc;
        String appName;
        String packageName;
    }


    private void rotateAnim() {
        RotateAnimation ra = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(1000);
        //无限循环，会卡顿，没办法就是这样的
        ra.setRepeatCount(Animation.INFINITE);
        ivScan.startAnimation(ra);
    }
}
