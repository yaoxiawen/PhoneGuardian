package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import java.util.ArrayList;
import java.util.List;

import bean.TaskInfo;

public class TaskManagerActivity extends Activity {
    private ListView lvTask;
    private TextView tvTasknum;
    private TextView tvRam;
    private LinearLayout ll;
    private ItemAdapter adapter;
    private List<TaskInfo> list = new ArrayList<>();
    private List<TaskInfo> systemList = new ArrayList<>();
    private List<TaskInfo> userList = new ArrayList<>();
    private TextView tvStatus;
    private CheckBox cbTask;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ll.setVisibility(View.INVISIBLE);
            if (adapter == null) {
                adapter = new ItemAdapter();
                lvTask.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    };
    private ActivityManager am;
    private int runningProcessCount;
    private long availRam;
    private long totalRam;
    private SharedPreferences sp;
    private boolean showsystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_taskmanager);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        showsystem = sp.getBoolean("showsystem", true);
        init();
    }

    private void init() {
        lvTask = findViewById(R.id.lv_task);
        tvTasknum = findViewById(R.id.tv_tasknum);
        tvRam = findViewById(R.id.tv_ram);
        ll = findViewById(R.id.ll);
        tvStatus = findViewById(R.id.tv_status);
        //获取活动管理器(任务管理器)
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //得到正在运行的app进程的数量
        runningProcessCount = am.getRunningAppProcesses().size();
        //获取内存信息
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(info);
        //剩余内存
        availRam = info.availMem;
        //总内存
        totalRam = info.totalMem;
        tvTasknum.setText("运行中进程" + runningProcessCount + "个");
        //要格式化
        tvRam.setText("剩余/总内存：" + Formatter.formatFileSize(this, availRam)
                + "/" + Formatter.formatFileSize(this, totalRam));
        ll.setVisibility(View.VISIBLE);
        upList();
        //设置滚动侦听，给ListView配置状态栏信息
        lvTask.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (userList != null && systemList != null) {
                    if (firstVisibleItem > userList.size()) {
                        tvStatus.setText("系统进程(" + systemList.size() + ")");
                    } else {
                        tvStatus.setText("用户进程(" + userList.size() + ")");
                    }
                }
            }
        });
        lvTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = lvTask.getItemAtPosition(position);
                if (obj != null) {
                    TaskInfo taskInfo = (TaskInfo) obj;
                    if (taskInfo.getPackageName().equals(TaskManagerActivity.this.getPackageName())) {
                        return;
                    }
                    if (taskInfo.isChecked()) {
                        taskInfo.setChecked(false);
                        adapter.notifyDataSetChanged();
                    } else {
                        taskInfo.setChecked(true);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void upList() {
        new Thread() {
            @Override
            public void run() {
                list = getTaskInfoList();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isUserTask()) {
                        userList.add(list.get(i));
                    } else {
                        systemList.add(list.get(i));
                    }
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private List<TaskInfo> getTaskInfoList() {
        List<TaskInfo> list = new ArrayList<>();
        //获取活动管理器(任务管理器)
        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //获取包管理器
        PackageManager pm = getPackageManager();
        //获取正在运行的app进程
        //android 5.1之后，没用
        List<ActivityManager.RunningAppProcessInfo> infos = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            try {
                Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(new int[]{info.pid});
                int memory = memoryInfo[0].getTotalPrivateDirty();
                long size = memory * 1024L;
                String packageName = info.processName;
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                Drawable icon = packageInfo.applicationInfo.loadIcon(pm);
                String name = packageInfo.applicationInfo.loadLabel(pm).toString();
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setPackageName(packageName);
                taskInfo.setIcon(icon);
                taskInfo.setName(name);
                taskInfo.setSize(size);
                int flags = packageInfo.applicationInfo.flags;
                if ((flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    taskInfo.setUserTask(true);
                } else {
                    taskInfo.setUserTask(false);
                }
                list.add(taskInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    private class ItemAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (showsystem) {
                return list.size() + 2;
            } else {
                return userList.size() + 1;
            }
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
                TextView view = new TextView(TaskManagerActivity.this);
                view.setText("用户进程(" + userList.size() + ")");
                view.setTextSize(20);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.GRAY);
                return view;
            } else if (position == userList.size() + 1) {
                TextView view = new TextView(TaskManagerActivity.this);
                view.setText("系统进程(" + systemList.size() + ")");
                view.setTextSize(20);
                view.setTextColor(Color.WHITE);
                view.setBackgroundColor(Color.GRAY);
                return view;
            } else if (position <= userList.size()) {
                View v;
                ViewHolder vh;
                if (convertView == null || !(convertView instanceof RelativeLayout)) {
                    v = View.inflate(TaskManagerActivity.this, R.layout.item_task, null);
                    vh = new ViewHolder();
                    vh.ivTaskIcon = v.findViewById(R.id.iv_taskicon);
                    vh.tvTaskName = v.findViewById(R.id.tv_taskname);
                    vh.tvRamSize = v.findViewById(R.id.tv_ramsize);
                    vh.cbTask = v.findViewById(R.id.cb_task);
                    v.setTag(vh);
                } else {
                    v = convertView;
                    vh = (ViewHolder) v.getTag();
                }
                int newPosition = position - 1;
                vh.cbTask.setChecked(userList.get(newPosition).isChecked());
                vh.ivTaskIcon.setBackground(userList.get(newPosition).getIcon());
                vh.tvTaskName.setText(userList.get(newPosition).getName());
                vh.tvRamSize.setText(Formatter.formatFileSize(TaskManagerActivity.this, userList.get(newPosition).getSize()));
                if (userList.get(newPosition).getPackageName().equals(TaskManagerActivity.this.getPackageName())) {
                    vh.cbTask.setVisibility(View.INVISIBLE);
                } else {
                    vh.cbTask.setVisibility(View.VISIBLE);
                }
                return v;
            } else {
                View v;
                ViewHolder vh;
                if (convertView == null || !(convertView instanceof RelativeLayout)) {
                    v = View.inflate(TaskManagerActivity.this, R.layout.item_task, null);
                    vh = new ViewHolder();
                    vh.ivTaskIcon = v.findViewById(R.id.iv_taskicon);
                    vh.tvTaskName = v.findViewById(R.id.tv_taskname);
                    vh.tvRamSize = v.findViewById(R.id.tv_ramsize);
                    vh.cbTask = v.findViewById(R.id.cb_task);
                    v.setTag(vh);
                } else {
                    v = convertView;
                    vh = (ViewHolder) v.getTag();
                }
                int newPosition = position - 2 - userList.size();
                vh.cbTask.setChecked(systemList.get(newPosition).isChecked());
                vh.ivTaskIcon.setBackground(systemList.get(newPosition).getIcon());
                vh.tvTaskName.setText(systemList.get(newPosition).getName());
                vh.tvRamSize.setText(Formatter.formatFileSize(TaskManagerActivity.this, systemList.get(newPosition).getSize()));
                if (systemList.get(newPosition).getPackageName().equals(TaskManagerActivity.this.getPackageName())) {
                    vh.cbTask.setVisibility(View.INVISIBLE);
                } else {
                    vh.cbTask.setVisibility(View.VISIBLE);
                }
                return v;
            }
        }

        class ViewHolder {
            ImageView ivTaskIcon;
            TextView tvTaskName;
            TextView tvRamSize;
            CheckBox cbTask;
        }
    }

    public void selectAll(View v) {
        for (TaskInfo info : userList) {
            if (info.getPackageName().equals(TaskManagerActivity.this.getPackageName())) {
                continue;
            }
            info.setChecked(true);
        }
        if (!showsystem) {
            for (TaskInfo info : systemList) {
                info.setChecked(true);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void unSelect(View v) {
        for (TaskInfo info : userList) {
            if (info.getPackageName().equals(TaskManagerActivity.this.getPackageName())) {
                continue;
            }
            info.setChecked(false);
        }
        if (!showsystem) {
            for (TaskInfo info : systemList) {
                info.setChecked(false);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void killProcess(View v) {
        int total = 0;
        long saveMem = 0;
        List<TaskInfo> killedTaskInfo = new ArrayList<TaskInfo>();
        for (TaskInfo info : userList) {
            //杀进程
            am.killBackgroundProcesses(info.getPackageName());
            total++;
            saveMem += info.getSize();
            //并发问题
            //userList.remove(info);
            killedTaskInfo.add(info);
        }
        if (showsystem) {
            for (TaskInfo info : systemList) {
                am.killBackgroundProcesses(info.getPackageName());
                total++;
                saveMem += info.getSize();
                //并发问题
                //systemList.remove(info);
                killedTaskInfo.add(info);
            }
        }
        for (TaskInfo info : killedTaskInfo) {
            if (info.isUserTask()) {
                userList.remove(info);
            } else {
                systemList.remove(info);
            }
        }
        runningProcessCount -= total;
        tvTasknum.setText("运行中进程" + runningProcessCount + "个");
        availRam += saveMem;
        tvRam.setText("剩余/总内存：" + Formatter.formatFileSize(this, availRam)
                + "/" + Formatter.formatFileSize(this, totalRam));
        Toast.makeText(this, "杀死了" + total + "个进程，释放了" +
                Formatter.formatFileSize(this, saveMem), Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

    public void enterSetting(View v) {
        startActivityForResult(new Intent(TaskManagerActivity.this, TaskManagerSettingActivity.class),0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==0){
            adapter.notifyDataSetChanged();
        }
    }
}
