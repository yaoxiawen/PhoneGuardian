package com.example.admin.phoneguardian.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.phoneguardian.R;

/**
 * 自定义控件，自定义布局，自定义属性，自定义方法
 */
public class SettingItemView extends RelativeLayout {
    //自定义布局三个组件
    TextView tv_title;
    TextView tv_desc;
    CheckBox cb_status;
    //三个自定义属性
    String title;
    String desc_on;
    String desc_off;

    /**
     * 构造方法1，有style样式时调用此构造方法
     * @param context
     */
    public SettingItemView(Context context) {
        super(context);
        InitView();
    }

    /**
     * 构造方法2，有自定义属性时调用此构造方法
     * @param context
     * @param attrs 获取自定义属性的值
     */
    public SettingItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //自定义的属性，values文件夹下attrs.xml
        //命名空间是在使用该自定义控件的布局文件中所使用的命名空间
        //通过构造方法中的参数获取自定义属性的值
        title = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto","title");
        desc_on = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto","desc_on");
        desc_off = attrs.getAttributeValue("http://schemas.android.com/apk/res-auto","desc_off");
        InitView();
    }

    /**
     * 构造方法3，用代码new对象时调用此构造方法
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitView();
    }

    /**
     * 初始化工作，加载布局，拿到组件id，组件内容赋值
     * 在每一个构造方法中都调用一遍
     */
    private void InitView() {
        //加载布局，注意getContext()，且左边不用写
        View.inflate(getContext(), R.layout.view_settingitem, this);
        tv_title = findViewById(R.id.tv_title);
        tv_desc = findViewById(R.id.tv_desc);
        cb_status = findViewById(R.id.cb_status);
        setTvtitle(title);
    }

    /**
     * 自定义方法，使用代码对自定义控件中的组件赋值或者获取自定义控件中的组件的信息
     */
    public void setTvtitle(String s) {
        tv_title.setText(s);

    }
    public void setTvdesc(String s) {
        tv_desc.setText(s);
    }
    public void setCb_status(boolean check){
        cb_status.setChecked(check);
        if(check){
            setTvdesc(desc_on);
        }else {
            setTvdesc(desc_off);
        }
    }
    public boolean ischecked(){
        return cb_status.isChecked();
    }
}
