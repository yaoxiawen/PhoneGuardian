package utils;

import android.content.Context;
import android.widget.Toast;

import com.example.admin.phoneguardian.activity.Setup2Activity;

public class ToastUtils {
    public static void show(Context ctx,String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }
}
