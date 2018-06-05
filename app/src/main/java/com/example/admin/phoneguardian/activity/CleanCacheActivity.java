package com.example.admin.phoneguardian.activity;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CleanCacheActivity extends AppCompatActivity {
    private static final int SCANING = 0;
    private static final int SHOW_SCAN_INFO = 1;
    private static final int SCAN_FINISH = 2;
    private ListView lvCache;
    private TextView tvCache;
    private LinearLayout ll;
    private List<CacheInfo> list;
    private PackageManager pm;
    private ItemAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ll.setVisibility(View.INVISIBLE);
            adapter = new ItemAdapter();
            lvCache.setAdapter(adapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_clean_cache);
        init();
    }

    private void init() {
        lvCache = findViewById(R.id.lv_cache);
        tvCache = findViewById(R.id.tv_cache);
        ll = findViewById(R.id.ll);
        list = new ArrayList<>();
        ll.setVisibility(View.VISIBLE);
        upList();
    }

    private void upList() {
        new Thread() {
            @Override
            public void run() {
                pm = getPackageManager();
                List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
                for (PackageInfo packInfo : packInfos) {
                    String packName = packInfo.packageName;
                    try {
                        //利用发射技术获取缓存
                        //通过类加载器加载，获取字节码对象，
                        //通过反射从字节码对象获取到当前的方法
                        //参数：方法名，该方法的参数类型
                        Method method = PackageManager.class.getMethod(
                                "getPackageSizeInfo", String.class,
                                IPackageStatsObserver.class);
                        //调用方法
                        //参数：调用上一步中getPackageSizeInfo方法的对象，由pm调用getPackageSizeInfo方法
                        //参数：传入上一步中getPackageSizeInfo方法的参数对象，
                        // 对应String.class，packName，对应IPackageStatsObserver.class，new MyObserver()
                        method.invoke(pm, packName, new MyObserver());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    class MyObserver extends IPackageStatsObserver.Stub {
        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
            //得到缓存大小
            long size = pStats.cacheSize;
            if (size > 0) {
                try {
                    CacheInfo cacheInfo = new CacheInfo();
                    //得到包名
                    cacheInfo.name = pStats.packageName;
                    //得到缓存大小
                    cacheInfo.size = size;
                    //通过包名，由pm得到PackageInfo，再得到图标
                    cacheInfo.icon = pm.getPackageInfo(pStats.packageName, 0).
                            applicationInfo.loadIcon(pm);
                    list.add(cacheInfo);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class CacheInfo {
        Drawable icon;
        String name;
        long size;
    }

    class ItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            tvCache.setText("缓存软件 (" + list.size() + ")");
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            ViewHolder vh;
            if (convertView == null) {
                v = View.inflate(CleanCacheActivity.this, R.layout.item_cache, null);
                vh = new ViewHolder();
                vh.ivCacheIcon = v.findViewById(R.id.iv_cacheicon);
                vh.tvCacheName = v.findViewById(R.id.tv_cachename);
                vh.tvCacheSize = v.findViewById(R.id.tv_cachesize);
                v.setTag(vh);
            } else {
                v = convertView;
                vh = (ViewHolder) v.getTag();
            }
            vh.ivCacheIcon.setBackground(list.get(position).icon);
            vh.tvCacheName.setText(list.get(position).name);
            vh.tvCacheSize.setText(Formatter.formatFileSize(CleanCacheActivity.this, list.get(position).size));
            return v;
        }

        class ViewHolder {
            ImageView ivCacheIcon;
            TextView tvCacheName;
            TextView tvCacheSize;
        }
    }

}
