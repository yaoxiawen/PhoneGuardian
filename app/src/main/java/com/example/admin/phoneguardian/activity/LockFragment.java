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

public class LockFragment extends Fragment {
    private TextView tvFraglock;
    private ListView lvLock;
    private ItemAdapter adapter;
    private LinearLayout ll;
    private List<AppInfo> list;
    private AppLockdb lockdb;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ll.setVisibility(View.INVISIBLE);
            adapter = new ItemAdapter();
            lvLock.setAdapter(adapter);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_lock, null);
        tvFraglock = v.findViewById(R.id.tv_fraglock);
        lvLock = v.findViewById(R.id.lv_lock);
        ll = v.findViewById(R.id.ll);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        ll.setVisibility(View.VISIBLE);
        lockdb = new AppLockdb(getActivity());
        upList();
    }

    private void upList() {
        new Thread() {
            @Override
            public void run() {
                list = getAppInfoList();
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private List<AppInfo> getAppInfoList() {
        List<AppInfo> list = new ArrayList<>();
        PackageManager pm = getActivity().getPackageManager();
        List<PackageInfo> infos = pm.getInstalledPackages(0);
        for (PackageInfo info : infos) {
            if (lockdb.find(info.packageName)) {
                String packageName = info.packageName;
                Drawable icon = info.applicationInfo.loadIcon(pm);
                String name = info.applicationInfo.loadLabel(pm).toString();
                AppInfo appInfo = new AppInfo();
                appInfo.setPackageName(packageName);
                appInfo.setIcon(icon);
                appInfo.setName(name);
                list.add(appInfo);
            }
        }
        return list;
    }

    private class ItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            tvFraglock.setText("已加锁软件 (" + list.size() + ")");
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View v;
            final ViewHolder vh;
            if (convertView == null) {
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
            vh.ivAppIcon.setBackground(list.get(position).getIcon());
            vh.tvAppName.setText(list.get(position).getName());
            vh.ivStatus.setImageResource(R.drawable.list_button_unlock_default);
            vh.ivStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vh.ivStatus.setImageResource(R.drawable.list_button_unlock_pressed);
                    TranslateAnimation ta = new TranslateAnimation(
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, -1.0f,
                            Animation.RELATIVE_TO_SELF, 0,
                            Animation.RELATIVE_TO_SELF, 0);
                    ta.setDuration(500);
                    v.startAnimation(ta);
                    new Thread() {
                        @Override
                        public void run() {
                            SystemClock.sleep(500);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    lockdb.delete(list.get(position).getPackageName());
                                    list.remove(position);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }.start();
                }
            });
            return v;
        }

        class ViewHolder {
            ImageView ivAppIcon;
            TextView tvAppName;
            ImageView ivStatus;
        }
    }
}
