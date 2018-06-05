package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import java.util.ArrayList;
import java.util.List;

import utils.MD5Utils;


public class HomeActivity extends Activity {

    GridView gvHome;
    List<HomeItem> list = new ArrayList<>();
    SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_home);
        sp = getSharedPreferences("config", MODE_PRIVATE);
        HomeItem it;
        Bitmap bm;
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_safe);
        it = new HomeItem(bm, "手机防盗");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_callmsgsafe);
        it = new HomeItem(bm, "通讯卫士");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_apps);
        it = new HomeItem(bm, "软件管理");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_taskmanager);
        it = new HomeItem(bm, "进程管理");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_netmanager);
        it = new HomeItem(bm, "流量统计");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_trojan);
        it = new HomeItem(bm, "手机杀毒");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_sysoptimize);
        it = new HomeItem(bm, "缓存清理");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_tools);
        it = new HomeItem(bm, "高级工具");
        list.add(it);
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.home_settings);
        it = new HomeItem(bm, "设置中心");
        list.add(it);
        gvHome = findViewById(R.id.gv_home);
        gvHome.setAdapter(new HomeAdapter());
        gvHome.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        //手机防盗
                        //展示密码对话框
                        showPasswordDialog();
                        break;
                    case 1:
                        //通讯卫士
                        startActivity(new Intent(HomeActivity.this, CallSafeActivity.class));
                        break;
                    case 2:
                        //软件管理
                        startActivity(new Intent(HomeActivity.this, AppManagerActivity.class));
                        break;
                    case 3:
                        //进程管理
                        startActivity(new Intent(HomeActivity.this, TaskManagerActivity.class));
                        break;
                    case 4:
                        //流量统计
                        break;
                    case 5:
                        //手机杀毒
                        startActivity(new Intent(HomeActivity.this, AntiVirusActivity.class));
                        break;
                    case 6:
                        //缓存清理
                        startActivity(new Intent(HomeActivity.this, CleanCacheActivity.class));
                        break;
                    case 7:
                        //高级工具
                        startActivity(new Intent(HomeActivity.this, AToolsActivity.class));
                        break;
                    case 8:
                        //设置中心
                        startActivity(new Intent(HomeActivity.this, SettingActivity.class));
                        break;
                }
            }
        });
    }

    /**
     * 展示密码对话框
     */
    private void showPasswordDialog() {
        String savedPassword = sp.getString("password", null);
        //从本地获取密码，不为空，说明已设置过，展示密码输入对话框
        //为空，说明没有设置过，展示密码设置对话框
        if (!TextUtils.isEmpty(savedPassword)) {
            showPasswordInputDialog();
        } else {
            showPasswordSetDialog();
        }
    }

    /**
     * 密码输入对话框
     */
    private void showPasswordInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        //自定义对话框的布局
        View view = View.inflate(this, R.layout.activity_lostfindinputpassword, null);
        //对话框添加布局
        dialog.setView(view);
        dialog.show();
        final EditText etPassword = view.findViewById(R.id.et_password);
        Button btPasswordYes = view.findViewById(R.id.bt_passwordyes);
        Button btPasswordNo = view.findViewById(R.id.bt_passwordno);
        btPasswordYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = etPassword.getText().toString();
                String savedPassword = sp.getString("password", null);
                //输入框内容不为空
                if (!TextUtils.isEmpty(password)) {
                    //存储的密码是经过加密的
                    if (MD5Utils.encode(password).equals(savedPassword)) {
                        Toast.makeText(HomeActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        //隐藏对话框
                        dialog.dismiss();
                        startActivity(new Intent(HomeActivity.this,LostFindActivity.class));
                    } else {
                        Toast.makeText(HomeActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "输入框内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btPasswordNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 密码设置对话框
     */
    private void showPasswordSetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this, R.layout.activity_lostfindsetpassword, null);
        dialog.setView(view);
        dialog.show();
        final EditText etPassword = view.findViewById(R.id.et_password);
        final EditText etPasswordConfirm = view.findViewById(R.id.et_passwordconfirm);
        Button btPasswordYes = view.findViewById(R.id.bt_passwordyes);
        Button btPasswordNo = view.findViewById(R.id.bt_passwordno);
        btPasswordYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = etPassword.getText().toString();
                String passwordConfirm = etPasswordConfirm.getText().toString();
                //两个输入框内容都不为空
                if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordConfirm)) {
                    if (password.equals(passwordConfirm)) {
                        Toast.makeText(HomeActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        sp.edit().putString("password", MD5Utils.encode(password)).apply();
                        dialog.dismiss();
                        startActivity(new Intent(HomeActivity.this,LostFindActivity.class));
                    } else {
                        Toast.makeText(HomeActivity.this, "两次密码不一样", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "输入框内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        btPasswordNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    /**
     * GridView的适配器
     */
    class HomeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            ViewHolder vh;
            if (view == null) {
                v = View.inflate(HomeActivity.this, R.layout.item_home, null);
                vh = new ViewHolder();
                vh.iv = v.findViewById(R.id.iv_item);
                vh.tv = v.findViewById(R.id.tv_item);
                v.setTag(vh);
            } else {
                v = view;
                vh = (ViewHolder) v.getTag();
            }
            vh.iv.setImageBitmap(list.get(i).getBt());
            vh.tv.setText(list.get(i).getString());
            return v;
        }

        class ViewHolder {
            ImageView iv;
            TextView tv;
        }
    }
}
