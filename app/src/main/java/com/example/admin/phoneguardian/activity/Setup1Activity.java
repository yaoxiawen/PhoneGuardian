package com.example.admin.phoneguardian.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import com.example.admin.phoneguardian.R;

public class Setup1Activity extends Activity {
    private GestureDetector detector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_setup1);
        //手势识别器，第2个参数为OnGestureListener类型，
        // OnGestureListener为接口，SimpleOnGestureListener为该接口的一个实现类
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //手势滑动事件判断
                if (e1.getRawX() - e2.getRawX() > 200) {
                    //下一页
                    startActivity(new Intent(Setup1Activity.this, Setup2Activity.class));
                    finish();
                    overridePendingTransition(R.anim.tranlate_nextin, R.anim.tranlate_nextout);
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    /**
     * 触摸事件监听
     * @param event 触摸事件
     * @return
     */
    public boolean onTouchEvent(MotionEvent event) {
        //由于根据触摸事件来识别手势滑动过于复杂，android有封装好的手势识别对象
        //将触摸事件委托给手势识别器处理
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 两个activity切换时的动画
     * @param v
     */
    public void next(View v) {
        startActivity(new Intent(this, Setup2Activity.class));
        finish();
        //进来的activity的动画和出去的activity的动画
        //使用补间动画的xml
        overridePendingTransition(R.anim.tranlate_nextin, R.anim.tranlate_nextout);
    }
}
