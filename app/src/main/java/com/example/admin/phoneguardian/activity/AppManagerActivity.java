package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bean.AppInfo;

public class AppManagerActivity extends Activity implements View.OnClickListener {
    private ListView lvApp;
    private TextView tvRom;
    private TextView tvSD;
    private LinearLayout ll;
    private ItemAdapter adapter;
    private List<AppInfo> list ;
    private List<AppInfo> systemList ;
    private List<AppInfo> userList ;
    private TextView tvStatus;
    private PopupWindow popupWindow;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ll.setVisibility(View.INVISIBLE);
            adapter = new ItemAdapter();
            lvApp.setAdapter(adapter);
        }
    };
    private AppInfo app;
    private LinearLayout llInstall;
    private LinearLayout llRun;
    private LinearLayout llInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_appmanager);
        init();
    }

    private void init() {
        lvApp = findViewById(R.id.lv_app);
        tvRom = findViewById(R.id.tv_rom);
        tvSD = findViewById(R.id.tv_sd);
        ll = findViewById(R.id.ll);
        tvStatus = findViewById(R.id.tv_status);
        //获取手机存储控件信息
        long freeSpaceRom = Environment.getDataDirectory().getFreeSpace();
        long freeSpaceSD = Environment.getExternalStorageDirectory().getFreeSpace();
        //格式转换
        tvRom.setText("内存可用：" + Formatter.formatFileSize(this, freeSpaceRom));
        tvSD.setText("SD卡可用：" + Formatter.formatFileSize(this, freeSpaceSD));
        ll.setVisibility(View.VISIBLE);
        upList();
        //ListView的状态栏，给ListView设置滚动侦听
        lvApp.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            /**
             * 滚动时调用
             * @param view 调用该方法的view
             * @param firstVisibleItem 最上面的条目
             * @param visibleItemCount 当前可见的条目数量
             * @param totalItemCount 总共的条目数量
             */
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //滚动时popup动画要隐藏
                popupWindowDismiss();
                //根据当前最上面的条目来判断状态栏要显示的内容
                if (userList != null && systemList != null) {
                    if (firstVisibleItem > userList.size()) {
                        tvStatus.setText("系统程序(" + systemList.size() + ")");
                    } else {
                        tvStatus.setText("用户程序(" + userList.size() + ")");
                    }
                }
            }
        });
        //设置点击侦听，显示popup动画
        lvApp.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //每次点击时，因为要显示新的popup动画，原先的popup动画要隐藏
                //即每次点击只能显示一个popup动画
                popupWindowDismiss();
                //根据点击的position，获取点击的条目对象
                //实际调用的为适配器里的getItem方法，因此要把该方法返回的对象写好
                Object obj = lvApp.getItemAtPosition(position);
                //对象不为空
                if (obj!=null){
                    app = (AppInfo) obj;
                    //popup动画的布局对象
                    View contentView = View.inflate(AppManagerActivity.this,
                            R.layout.item_popup,null);
                    llInstall = contentView.findViewById(R.id.ll_uninstall);
                    llRun = contentView.findViewById(R.id.ll_run);
                    llInfo = contentView.findViewById(R.id.ll_info);
                    //设置点击侦听
                    llInstall.setOnClickListener(AppManagerActivity.this);
                    llRun.setOnClickListener(AppManagerActivity.this);
                    llInfo.setOnClickListener(AppManagerActivity.this);
                    //透明动画
                    AlphaAnimation aa = new AlphaAnimation(0.5f,0.8f);
                    aa.setDuration(300);
                    //缩放动画
                    ScaleAnimation sa =  new ScaleAnimation(0.5f,1,0.5f,1,
                            Animation.RELATIVE_TO_SELF,0,Animation.RELATIVE_TO_SELF,0.5f);
                    sa.setDuration(300);
                    AnimationSet set = new AnimationSet(false);
                    set.addAnimation(aa);
                    set.addAnimation(sa);
                    //给布局对象添上动画
                    contentView.startAnimation(set);
                    //创建popupWindow，参数1，要显示的布局对象，参数2，宽，为包裹内容，参数3，高
                    popupWindow = new PopupWindow(contentView,
                            ViewGroup.LayoutParams.WRAP_CONTENT,180);
                    //因为播放动画的前提，窗体必须要有背景
                    //给popupWindow设置透明背景
                    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    //获取当前点击的item的view展示在窗体上的位置
                    int[] location = new int[2];
                    view.getLocationInWindow(location);
                    //给popupWindow设置展示位置，这个方法要最后调用，在设置背景之后
                    //参数1，方法中第1个参数，要显示在的父布局
                    //参数2，gravity
                    //参数3，4，距离x的距离和距离y的距离，x为写死，y为location[1]
                    popupWindow.showAtLocation(parent, Gravity.LEFT+Gravity.TOP,130,location[1]);
                }
            }
        });
    }

    /**
     * popup动画隐藏
     * 有4个地方要调用该方法
     * 1、为保证每次点击只显示一个popup动画，在点击侦听里要调用，先隐藏原先的popup动画，再显示新的popup动画
     * 2、滚动时要隐藏popup动画
     * 3、activity销毁时，在onDestroy方法中调用
     * 4、点击popup动画时，弹出对应的界面，此popup也要隐藏
     */
    private void popupWindowDismiss(){
        //popupWindow不为空且当前正在显示时
        if (popupWindow!=null && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow= null;
        }
    }

    /**
     * 在子线程中获取已安装的软件信息，耗时操作
     */
    private void upList() {
        new Thread() {
            @Override
            public void run() {
                list = getAppInfoList();
                userList = new ArrayList<>();
                systemList = new ArrayList<>();
                //分为用户软件和系统软件
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isUserApp()) {
                        userList.add(list.get(i));
                    } else {
                        systemList.add(list.get(i));
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * 获取已安装的软件信息
     * @return 已安装的软件信息的list
     */
    private List<AppInfo> getAppInfoList() {
        List<AppInfo> list = new ArrayList<>();
        //获取包的管理者
        PackageManager pm = getPackageManager();
        //获取安装包信息
        List<PackageInfo> infos = pm.getInstalledPackages(0);
        for (PackageInfo info : infos) {
            //获取应用程序的包名，通过安装包信息直接获得
            String packageName = info.packageName;
            //获取应用程序的图标，通过安装包信息的应用程序信息加载图标
            Drawable icon = info.applicationInfo.loadIcon(pm);
            //获取应用程序的名字，通过安装包信息的应用程序信息加载标签再转换为字符串
            String name = info.applicationInfo.loadLabel(pm).toString();
            //获取应用程序的大小，通过安装包信息的应用程序信息的资源路径，该路径文件的大小
            String dir = info.applicationInfo.sourceDir;
            File file = new File(dir);
            long size = file.length();
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(packageName);
            appInfo.setIcon(icon);
            appInfo.setName(name);
            appInfo.setSize(size);
            //安装包信息的应用程序信息的特征标志flags，是任意标志的组合
            int flags = info.applicationInfo.flags;
            //通过与特定标志做二进制的与运算，可以知道是否有该标志
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appInfo.setUserApp(true);
            } else {
                appInfo.setUserApp(false);
            }
            if ((flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
                appInfo.setRom(true);
            } else {
                appInfo.setRom(false);
            }
            list.add(appInfo);
        }
        return list;
    }

    /**
     * 点击侦听
     * @param v 被触发点击侦听，调用该方法的view，即被点击了的view
     */
    @Override
    public void onClick(View v) {
        //popup隐藏，弹出对应的界面
        popupWindowDismiss();
        Intent intent;
        switch (v.getId()){
            case R.id.ll_uninstall:
                //卸载应用
                intent = new Intent(Intent.ACTION_VIEW);
                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:"+app.getPackageName()));
                //如果卸载成功，需要刷新界面
                startActivityForResult(intent, 0);
                break;
            case R.id.ll_run:
                //运行应用
                intent =getPackageManager().getLaunchIntentForPackage(app.getPackageName());
                if (intent!= null){
                    startActivity(intent);
                }else{
                    Toast.makeText(this,"启动失败！",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.ll_info:
                //展示应用的详细信息
                intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                //intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + app.getPackageName()));
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode ==0，卸载界面的返回
        if (requestCode ==0){
            if (resultCode == Activity.RESULT_OK){
                upList();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size() + 2;
        }

        @Override
        public Object getItem(int position) {
            if (position == 0) {
                return null;
            } else if (position == userList.size() + 1) {
                return null;
            } else if (position <= userList.size()) {
                return userList.get(position - 1);
            } else {
                return systemList.get(position - 2 - userList.size());
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == 0) {
                TextView view = new TextView(AppManagerActivity.this);
                view.setText("用户程序(" + userList.size() + ")");
                view.setTextSize(20);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.GRAY);
                return view;
            } else if (position == userList.size() + 1) {
                TextView view = new TextView(AppManagerActivity.this);
                view.setText("系统程序(" + systemList.size() + ")");
                view.setTextSize(20);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.GRAY);
                return view;
            } else if (position <= userList.size()) {
                View v;
                ViewHolder vh;
                //添加的两个TextView的布局同数据显示的布局不一样
                //也就是，ListView的条目有两种布局，因此不能单纯的用convertView == null来判断
                if (convertView == null || !(convertView instanceof RelativeLayout)) {
                    v = View.inflate(AppManagerActivity.this, R.layout.item_app, null);
                    vh = new ViewHolder();
                    vh.ivAppIcon = v.findViewById(R.id.iv_appicon);
                    vh.tvAppName = v.findViewById(R.id.tv_appname);
                    vh.tvAppLocation = v.findViewById(R.id.tv_applocation);
                    vh.tvAppSize = v.findViewById(R.id.tv_appsize);
                    v.setTag(vh);
                } else {
                    v = convertView;
                    vh = (ViewHolder) v.getTag();
                }
                int newPosition = position - 1;
                vh.ivAppIcon.setBackground(userList.get(newPosition).getIcon());
                vh.tvAppName.setText(userList.get(newPosition).getName());
                vh.tvAppSize.setText(Formatter.formatFileSize(AppManagerActivity.this, userList.get(newPosition).getSize()));
                if (userList.get(newPosition).isRom()) {
                    vh.tvAppLocation.setText("手机内存");
                } else {
                    vh.tvAppLocation.setText("外部存储");
                }
                return v;
            } else {
                View v;
                ViewHolder vh;
                if (convertView == null || !(convertView instanceof RelativeLayout)) {
                    v = View.inflate(AppManagerActivity.this, R.layout.item_app, null);
                    vh = new ViewHolder();
                    vh.ivAppIcon = v.findViewById(R.id.iv_appicon);
                    vh.tvAppName = v.findViewById(R.id.tv_appname);
                    vh.tvAppLocation = v.findViewById(R.id.tv_applocation);
                    vh.tvAppSize = v.findViewById(R.id.tv_appsize);
                    v.setTag(vh);
                } else {
                    v = convertView;
                    vh = (ViewHolder) v.getTag();
                }
                int newPosition = position - 2 - userList.size();
                vh.ivAppIcon.setBackground(systemList.get(newPosition).getIcon());
                vh.tvAppName.setText(systemList.get(newPosition).getName());
                vh.tvAppSize.setText(Formatter.formatFileSize(AppManagerActivity.this, systemList.get(newPosition).getSize()));
                if (systemList.get(newPosition).isRom()) {
                    vh.tvAppLocation.setText("手机内存");
                } else {
                    vh.tvAppLocation.setText("外部存储");
                }
                return v;
            }
        }

        class ViewHolder {
            ImageView ivAppIcon;
            TextView tvAppName;
            TextView tvAppLocation;
            TextView tvAppSize;
        }
    }

    @Override
    protected void onDestroy() {
        popupWindowDismiss();
        super.onDestroy();
    }
}
