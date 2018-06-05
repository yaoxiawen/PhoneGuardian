package receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 开机广播接收者，检测sim卡是否变化
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        boolean protect = sp.getBoolean("protect", false);
        if (protect) {
            String simNumber = sp.getString("simnumber", null);
            if (!TextUtils.isEmpty(simNumber)) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    String currentSinNumber = tm.getSimSerialNumber();
                    if (!simNumber.equals(currentSinNumber)) {
                        String phone = sp.getString("phone",null);
                        //发送短信
                        SmsManager sms = SmsManager.getDefault();
                        sms.sendTextMessage(phone,null,"SIM card has changed",
                                null,null);
                    }
                }
            }
        }
    }
}
