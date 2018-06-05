package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.admin.phoneguardian.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取手机联系人
 */
public class ContactActivity extends Activity {
    private ArrayList<Map<String, String>> al;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact);
        al = getContact();
        ListView lvContact = findViewById(R.id.lv_contact);
        //配置适配器，用SimpleAdapter
        lvContact.setAdapter(new SimpleAdapter(this, al, R.layout.activity_contactitem,
                new String[]{"name", "phone"}, new int[]{R.id.tv_idname, R.id.tv_idphone}));
        //点击侦听，将点击的数据传递回去
        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String phone = al.get(i).get("phone");
                Intent intent = new Intent();
                intent.putExtra("phone", phone);
                //返回数据，同时返回结果码
                setResult(Activity.RESULT_OK, intent);
                //关闭当前activity，才能返回数据
                finish();
            }
        });
    }

    /**
     * 读取手机联系人
     * @return ArrayList 联系人列表
     */
    public ArrayList getContact() {
        ArrayList<Map<String, String>> al = new ArrayList<>();
        //通过内容观察者对联系人进行查询
        ContentResolver cr = getContentResolver();
        Cursor csContactId = cr.query(Uri.parse("content://com.android.contacts/raw_contacts"),
                new String[]{"contact_id"}, null, null, null);
        if (csContactId != null) {
            while (csContactId.moveToNext()) {
                String contatcId = csContactId.getString(0);
                Cursor csData = cr.query(Uri.parse("content://com.android.contacts/data"),
                        new String[]{"data1", "mimetype"}, "raw_contact_id=?", new String[]{contatcId}, null);
                if (csData!=null) {
                    HashMap<String, String> map = new HashMap<>();
                    while (csData.moveToNext()) {
                        String data1 = csData.getString(0);
                        String mimetype = csData.getString(1);
                        if (data1!=null) {
                            if ("vnd.android.cursor.item/name".equals(mimetype)) {
                                map.put("name", data1);
                            }
                            if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                                map.put("phone", data1);
                            }
                        }
                    }
                    if (map.get("name")!=null) {
                        al.add(map);
                    }
                }
                csData.close();
            }
        }
        csContactId.close();
        return al;
    }
}

