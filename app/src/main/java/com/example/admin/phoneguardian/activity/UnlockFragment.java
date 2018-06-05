package com.example.admin.phoneguardian.activity;

import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

import java.util.ArrayList;
import java.util.List;

import bean.AppInfo;
import db.AppLockdb;

public class UnlockFragment extends Fragment {
    private TextView tvFragunlock;
    private ListView lvUnlock;
    private ItemAdapter adapter;
    private LinearLayout ll;
    private List<AppInfo> list;
    private List<AppInfo> systemList;
    private List<AppInfo> userList;
    private AppLockdb lockdb;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ll.setVisibility(View.INVISIBLE);
            adapter = new ItemAdapter();
            lvUnlock.setAdapter(adapter);
        }
    };

    /**
     * 在onCreateView方法里写初始化的代码
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_unlock, null);
        tvFragunlock = v.findViewById(R.id.tv_fragunlock);
        lvUnlock = v.findViewById(R.id.lv_unlock);
        ll = v.findViewById(R.id.ll);
        return v;
    }

    /**
     * 在onStart方法里写逻辑代码
     * 每一次屏幕可见时，该方法都会被调用一次，就能加载一次数据
     */
    @Override
    public void onStart() {
        super.onStart();
        ll.setVisibility(View.VISIBLE);
        //Fragment里面获取上下文用getActivity()方法
        lockdb = new AppLockdb(getActivity());
        upList();
    }

    private void upList() {
        new Thread() {
            @Override
            public void run() {
                list = getAppInfoList();
                userList = new ArrayList<>();
                systemList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    if (!lockdb.find(list.get(i).getPackageName())) {
                        if (list.get(i).isUserApp()) {
                            userList.add(list.get(i));
                        } else {
                            systemList.add(list.get(i));
                        }
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private List<AppInfo> getAppInfoList() {
        List<AppInfo> list = new ArrayList<>();
        PackageManager pm = getActivity().getPackageManager();
        List<PackageInfo> infos = pm.getInstalledPackages(0);
        for (PackageInfo info : infos) {
            String packageName = info.packageName;
            Drawable icon = info.applicationInfo.loadIcon(pm);
            String name = info.applicationInfo.loadLabel(pm).toString();
            AppInfo appInfo = new AppInfo();
            appInfo.setPackageName(packageName);
            appInfo.setIcon(icon);
            appInfo.setName(name);
            int flags = info.applicationInfo.flags;
            if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appInfo.setUserApp(true);
            } else {
                appInfo.setUserApp(false);
            }
            list.add(appInfo);
        }
        return list;
    }

    private class ItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            int count = userList.size() + systemList.size();
            tvFragunlock.setText("未加锁软件 (" + count + ")");
            return count + 2;
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
                TextView view = new TextView(getActivity());
                view.setText("用户软件(" + userList.size() + ")");
                view.setTextSize(20);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.GRAY);
                return view;
            } else if (position == userList.size() + 1) {
                TextView view = new TextView(getActivity());
                view.setText("系统软件(" + systemList.size() + ")");
                view.setTextSize(20);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.GRAY);
                return view;
            } else if (position <= userList.size()) {
                View v;
                final ViewHolder vh;
                if (convertView == null || !(convertView instanceof RelativeLayout)) {
                    v = View.inflate(getActivity(), R.layout.item_applock, null);
                    vh = new ViewHolder();
                    vh.ivAppIcon = v.findViewById(R.id.iv_appicon);
                    vh.tvAppName = v.findViewById(R.id.tv_appname);
                    vh.ivStatus = v.findViewById(R.id.iv_status);
                    v.setTag(vh);
                } else {
                    v = convertView;
                    vh = (ViewHolder) v.getTag();
                }
                final int newPosition = position - 1;
                vh.ivAppIcon.setBackground(userList.get(newPosition).getIcon());
                vh.tvAppName.setText(userList.get(newPosition).getName());
                vh.ivStatus.setImageResource(R.drawable.list_button_lock_default);
                //点击侦听，给应用加锁
                vh.ivStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vh.ivStatus.setImageResource(R.drawable.list_button_lock_pressed);
                        //平移动画
                        TranslateAnimation ta = new TranslateAnimation(
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 1.0f,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0);
                        ta.setDuration(500);
                        v.startAnimation(ta);
                        //让动画播放完之后，再去执行更新ListView的代码
                        //开个线程，让线程睡，动画播放完之后，再运行UI线程getActivity().runOnUiThread()
                        new Thread(){
                            @Override
                            public void run() {
                                SystemClock.sleep(500);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        userList.remove(newPosition);
                                        lockdb.add(userList.get(newPosition).getPackageName());
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }.start();
                    }
                });
                return v;
            } else {
                View v;
                final ViewHolder vh;
                if (convertView == null || !(convertView instanceof RelativeLayout)) {
                    v = View.inflate(getActivity(), R.layout.item_applock, null);
                    vh = new ViewHolder();
                    vh.ivAppIcon = v.findViewById(R.id.iv_appicon);
                    vh.tvAppName = v.findViewById(R.id.tv_appname);
                    vh.ivStatus = v.findViewById(R.id.iv_status);
                    v.setTag(vh);
                } else {
                    v = convertView;
                    vh = (ViewHolder) v.getTag();
                }
                final int newPosition = position - 2 - userList.size();
                vh.ivAppIcon.setBackground(systemList.get(newPosition).getIcon());
                vh.tvAppName.setText(systemList.get(newPosition).getName());
                vh.ivStatus.setImageResource(R.drawable.list_button_lock_default);
                vh.ivStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vh.ivStatus.setImageResource(R.drawable.list_button_lock_pressed);
                        TranslateAnimation ta = new TranslateAnimation(
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 1.0f,
                                Animation.RELATIVE_TO_SELF, 0,
                                Animation.RELATIVE_TO_SELF, 0);
                        ta.setDuration(500);
                        v.startAnimation(ta);
                        new Thread(){
                            @Override
                            public void run() {
                                SystemClock.sleep(500);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        lockdb.add(systemList.get(newPosition).getPackageName());
                                        systemList.remove(newPosition);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }.start();
                    }
                });
                return v;
            }
        }

        class ViewHolder {
            ImageView ivAppIcon;
            TextView tvAppName;
            ImageView ivStatus;
        }
    }
}
