package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import utils.StreamUtils;

public class MainActivity extends Activity {

    //有新版本信息
    private static final int CODE_UPDATE = 1;
    //服务器URL格式错误
    private static final int CODE_URLERROR = 2;
    //流异常，网络原因错误
    private static final int CODE_NETERROR = 3;
    //服务器上信息解析错误
    private static final int CODE_JSONERROR = 4;
    //进入HomeActivity
    private static final int CODE_ENTERHOME = 5;

    private TextView tv_version;
    private TextView tv_progress;
    //当前应用的版本名，就是平常所说的版本号
    private String mVersionName;
    //服务器上新版本的版本名
    private String mNewVersionName;
    //当前应用的版本号
    private int mVersionCode = 0;
    //服务器上新版本的版本号
    private int mNewVersionCode = 0;
    //服务器上新版本的版本描述
    private String mNewDescription = null;
    //服务器上新版本的下载链接
    private String mNewDownloadRL;
    //数据永久性存储，本地存储
    private SharedPreferences sp;

    Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //有新版本信息，弹出更新的对话框
                case CODE_UPDATE:
                    showUpdateDialog();
                    break;
                case CODE_URLERROR:
                    Toast.makeText(MainActivity.this, "URL错误", Toast.LENGTH_SHORT).show();
                    enterHomeActivity();
                    break;
                case CODE_NETERROR:
                    Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    enterHomeActivity();
                    break;
                case CODE_JSONERROR:
                    Toast.makeText(MainActivity.this, "解析错误", Toast.LENGTH_SHORT).show();
                    enterHomeActivity();
                    break;
                case CODE_ENTERHOME:
                    enterHomeActivity();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_version = findViewById(R.id.tv_version);
        tv_progress = findViewById(R.id.tv_progress);
        //获取当前应用的版本号和版本名
        getVersion();
        tv_version.setText("版本号：" + mVersionName);

        //布局的透明动画，2秒动画
        RelativeLayout rl = findViewById(R.id.rl_splash);
        AlphaAnimation aa = new AlphaAnimation(0.3f, 1);
        aa.setDuration(2000);
        rl.startAnimation(aa);

        //整个应用需要用到的数据库的拷贝
        copyDB("address.db");
        copyDB("antivirus.db");

        //数据永久性存储，是否开启自动更新
        sp = getSharedPreferences("config", MODE_PRIVATE);
        boolean autoUpdate = sp.getBoolean("auto_update", true);
        if (autoUpdate) {
            //开启自动更新，去服务器上获取新版本的信息
            getNewVersion();
        } else {
            //没有开启自动更新，延迟2秒发送消息，保证透明动画的完成
            //消息为进入HomeActivity
            mhandler.sendEmptyMessageDelayed(CODE_ENTERHOME, 2000);
        }

    }

    /**
     * 有新版本信息，弹出更新的对话框
     */
    private void showUpdateDialog() {
        //创建确定取消对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("最新版本：" + mNewVersionName);
        builder.setMessage("最新版本描述: " + mNewDescription);
        //点击确定，进行新版本的下载
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //要下载进度可见
                tv_progress.setVisibility(View.VISIBLE);
                //新版本apk的存储位置
                String target = Environment.getExternalStorageDirectory() + "/update.apk";
                //使用第三方jar包进行下载apk
                HttpUtils utils = new HttpUtils();
                utils.download(mNewDownloadRL, target, new RequestCallBack<File>() {
                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {
                        super.onLoading(total, current, isUploading);
                        tv_progress.setText("下载进度：" + current * 100 / total + "%");
                    }

                    /**
                     * apk下载成功之后，跳转到系统的安装界面
                     * @param responseInfo
                     */
                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setDataAndType(Uri.fromFile(responseInfo.result),
                                "application/vnd.android.package-archive");
                        //返回时传递数据，在系统的安装界面，用户有可能点了取消
                        startActivityForResult(intent, 0);
                    }

                    @Override
                    public void onFailure(HttpException e, String s) {

                    }
                });
            }
        });
        /**
         * 点击取消，进入HomeActivity
         */
        builder.setNegativeButton("取消更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                enterHomeActivity();
            }
        });
        /**
         *更新对话框出现时，用户既没有点确定更新，也没有点取消更新，
         * 而是点了返回键，不能是将对话框取消，然后继续停留在该界面，而是应该进入HomeActivity
         * setOnCancelListener，点了返回键的监听
         */
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                enterHomeActivity();
            }
        });
        builder.show();
    }

    /**
     * 在系统的安装界面，用户有可能点了取消，返回时传递数据，进入HomeActivity
     *
     * @param requestCode 请求码，是0，由于该activity页面只有一个跳转界面返回数据的，
     *                    就不需要根据请求码进行判断了
     * @param resultCode  结果码，由于是系统的安装界面，只有用户点了取消，才会返回，
     *                    就不需要根据结果码进行结果的判断了
     * @param data        返回时携带的数据
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        enterHomeActivity();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 进入HomeActivity
     */
    private void enterHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //要把当前的activty finish
        finish();
    }

    /**
     * 去服务器上获取新版本的信息
     */
    private void getNewVersion() {
        //记录时间
        final long startTime = System.currentTimeMillis();
        new Thread() {
            Message msg = Message.obtain();
            HttpURLConnection conn = null;

            @Override
            public void run() {
                try {
                    //连接服务器
                    URL url = new URL("http://");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.connect();
                    //响应码200，连接成功
                    if (conn.getResponseCode() == 200) {
                        InputStream is = conn.getInputStream();
                        //将字节输入流获取的字节数据转换为字符串
                        String result = StreamUtils.readFromStream(is);
                        JSONObject jo = new JSONObject(result);
                        mNewVersionName = jo.getString("VersionName");
                        mNewVersionCode = jo.getInt("VersionCode");
                        mNewDescription = jo.getString("Description");
                        mNewDownloadRL = jo.getString("DownloadRL");
                        if (mNewVersionCode > mVersionCode) {
                            //有新版本信息
                            msg.what = CODE_UPDATE;
                        } else {
                            //没有更新的版本，进入HomeActivity
                            msg.what = CODE_ENTERHOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    //服务器URL格式错误
                    msg.what = CODE_URLERROR;
                    e.printStackTrace();
                } catch (IOException e) {
                    //流异常，网络原因错误
                    msg.what = CODE_NETERROR;
                    e.printStackTrace();
                } catch (JSONException e) {
                    //服务器上信息解析错误
                    msg.what = CODE_JSONERROR;
                    e.printStackTrace();
                } finally {
                    //为保证透明动画的完成，要有至少2秒的时间
                    long endTime = System.currentTimeMillis();
                    long timeUsed = endTime - startTime;
                    if (timeUsed < 2000) {
                        //不够2秒，睡到2秒
                        //SystemClock.sleep为Thread.sleep加上try的封装
                        SystemClock.sleep(2000 - timeUsed);
                    }
                    mhandler.sendMessage(msg);
                    //获取完信息，关闭服务器连接
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }.start();
    }

    /**
     * 获取当前应用的版本号和版本名
     */
    private void getVersion() {
        PackageManager pm = getPackageManager();
        PackageInfo pi;
        try {
            pi = pm.getPackageInfo(getPackageName(), 0);
            mVersionCode = pi.versionCode;
            mVersionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 整个应用需要用到的数据库的拷贝
     *
     * @param name 要拷贝的数据库名字
     */
    private void copyDB(String name) {
        //要拷贝到的位置
        String path = "data/data/" + "com.example.admin.phoneguardian" + "/" + name;
        //不要每次打开应用都进行拷贝，只要拷贝一次就可
        if (!new File(path).exists()) {
            FileOutputStream out = null;
            InputStream in = null;
            try {
                out = new FileOutputStream(path);
                //数据库放在assets文件夹下
                in = getAssets().open(name);
                int len;
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) != -1) {
                    out.write(buf, 0, len);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //拷贝完，分别对输出流和输入流进行关闭
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
