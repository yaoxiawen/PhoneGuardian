package com.example.admin.phoneguardian.activity;


import android.graphics.Bitmap;

/**
 * HomeActivity中GridView的显示内容
 */
public class HomeItem {
    Bitmap bt;
    String string;

    public HomeItem(Bitmap bt, String string) {
        this.bt = bt;
        this.string = string;
    }

    public Bitmap getBt() {
        return bt;
    }

    public String getString() {
        return string;
    }
}
