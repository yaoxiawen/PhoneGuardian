package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.phoneguardian.R;

import java.util.ArrayList;
import java.util.List;

import bean.BlackNumberInfo;
import db.BlackNumberdb;

public class CallSafeActivity extends Activity {
    private ListView lvBlackNumber;
    private BlackNumberdb bndb;
    private List<BlackNumberInfo> list = new ArrayList<>();
    private LinearLayout ll;
    private ItemAdapter adapter;
    private int startIndex = 0;
    private int maxCount = 20;
    private boolean isLoading = false;
    private int totalNumber;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            isLoading = false;
            ll.setVisibility(View.INVISIBLE);
            if (adapter == null) {
                adapter = new ItemAdapter();
                lvBlackNumber.setAdapter(adapter);
            } else {
                adapter.notifyDataSetChanged();
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_callsafe);
        init();
    }

    private void init() {
        lvBlackNumber = findViewById(R.id.lv_blacknumber);
        ll = findViewById(R.id.ll);
        ll.setVisibility(View.VISIBLE);
        //创建黑名单数据库
        bndb = new BlackNumberdb(this);
        totalNumber = bndb.getTotalCount();
        if (totalNumber == 0) {
            ll.setVisibility(View.INVISIBLE);
            return;
        }
        //最开始先加载一批
        updateList();
        //分批加载数据，设置滚动侦听
        lvBlackNumber.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                switch (i) {
                    //滚动停止
                    case SCROLL_STATE_IDLE:
                        //最后一条item的位置
                        int position = lvBlackNumber.getLastVisiblePosition();
                        //目前list的长度
                        int total = list.size();
                        if (position == (total - 1) && !isLoading) {
                            //防止用户一直在滑造成的重复加载
                            isLoading = true;
                            startIndex += maxCount;
                            if (startIndex >= totalNumber) {
                                Toast.makeText(CallSafeActivity.this, "全部数据已加载完",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ll.setVisibility(View.VISIBLE);
                            updateList();
                        }
                        break;
                    //惯性滑动
                    case SCROLL_STATE_FLING:
                        break;
                    //触摸滚动
                    case SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        });
    }


    private void updateList() {
        new Thread() {
            @Override
            public void run() {
                if (list == null) {
                    list = bndb.findPart(startIndex, maxCount);
                } else {
                    list.addAll(bndb.findPart(startIndex, maxCount));
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private class ItemAdapter extends BaseAdapter {
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
                v = View.inflate(CallSafeActivity.this, R.layout.item_blacknumber, null);
                vh = new ViewHolder();
                vh.tv_number = v.findViewById(R.id.tv_number);
                vh.tv_mode = v.findViewById(R.id.tv_mode);
                vh.iv_delete = v.findViewById(R.id.iv_delete);
                v.setTag(vh);
            } else {
                v = view;
                vh = (ViewHolder) v.getTag();
            }
            vh.tv_number.setText(list.get(i).getNumber());

            if ("1".equals(list.get(i).getMode())) {
                vh.tv_mode.setText("拦截电话");
            } else if ("2".equals(list.get(i).getMode())) {
                vh.tv_mode.setText("拦截短信");
            } else if ("3".equals(list.get(i).getMode())) {
                vh.tv_mode.setText("拦截电话+短信");
            }
            final BlackNumberInfo info = list.get(i);
            final String number = info.getNumber();
            //删除黑名单数据
            vh.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bndb.delete(number);
                    list.remove(info);
                    totalNumber = bndb.getTotalCount();
                    adapter.notifyDataSetChanged();
                }
            });
            return v;
        }

        class ViewHolder {
            TextView tv_number;
            TextView tv_mode;
            ImageView iv_delete;
        }
    }

    /**
     * 添加黑名单数据
     * @param v
     */
    public void add(View v) {
        //显示对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        //布局
        View viewDialog = View.inflate(this, R.layout.dialog_addblacknumber, null);
        dialog.setView(viewDialog);
        dialog.show();
        final EditText etBlackNumber = viewDialog.findViewById(R.id.et_blacknumber);
        //单选组合框
        final RadioGroup rg = viewDialog.findViewById(R.id.rg);
        Button btBlackNumberYes = viewDialog.findViewById(R.id.bt_blacknumberyes);
        Button btBlackNumberNo = viewDialog.findViewById(R.id.bt_blacknumberno);
        btBlackNumberYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = etBlackNumber.getText().toString().trim();
                if (!TextUtils.isEmpty(num)) {
                    //获取选中的按钮的id
                    int id = rg.getCheckedRadioButtonId();
                    String mode = "3";
                    switch (id) {
                        case R.id.rb_phone:
                            mode = "1";
                            break;
                        case R.id.rb_sms:
                            mode = "2";
                            break;
                        case R.id.rb_all:
                            mode = "3";
                            break;
                    }
                    bndb.add(num, mode);
                    BlackNumberInfo info = new BlackNumberInfo();
                    info.setNumber(num);
                    info.setMode(mode);
                    //新添加的数据显示在list的最上方
                    list.add(0, info);
                    totalNumber = bndb.getTotalCount();
                    handler.sendEmptyMessage(0);
                    dialog.dismiss();
                } else {
                    Toast.makeText(CallSafeActivity.this, "输入号码为空", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        btBlackNumberNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
