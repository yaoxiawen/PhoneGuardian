package receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import com.example.admin.phoneguardian.R;

import service.LocationService;

/**
 * 短信广播接收者
 */
public class SMSReceiver extends BroadcastReceiver {
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;

    @Override
    public void onReceive(Context context, Intent intent) {
        //从广播从获取短信数据
        Object[] objects = (Object[]) intent.getExtras().get("pdus");
        for (Object obj : objects) {
            SmsMessage message = SmsMessage.createFromPdu((byte[]) obj);
            //短信号码
            String address = message.getOriginatingAddress();
            //短信内容
            String body = message.getMessageBody();
            if ("#*alarm*#".equals(body)) {
                //多媒体播放器，获取音频资源
                MediaPlayer player = MediaPlayer.create(context, R.raw.kid);
                //将音量设为最大，即使静音也会响，像闹钟
                //左声道和右声道
                player.setVolume(1f, 1f);
                //单曲循环
                player.setLooping(true);
                player.start();
                //广播拦截
                abortBroadcast();
            } else if ("#*location*#".equals(body)) {
                //开启定位服务
                context.startService(new Intent(context, LocationService.class));
                SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
                String location = sp.getString("location", "null");
                String phone = sp.getString("phone", null);
                //发送短信
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phone, null, location,
                        null, null);
                //广播拦截
                abortBroadcast();
            } else if ("#*lockscreen*#".equals(body)) {
                //获取设备管理器
                mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                mDeviceAdmin = new ComponentName(context, LockReceiver.class);
                //要进行判断，是否是激活状态
                if (mDPM.isAdminActive(mDeviceAdmin)) {
                    //强制锁屏
                    mDPM.lockNow();
                    //重置密码，传空串能取消掉密码
                    mDPM.resetPassword("333333",0);
                }
                //广播拦截
                abortBroadcast();
            } else if ("#*wipedata*#".equals(body)) {
                mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                mDeviceAdmin = new ComponentName(context, LockReceiver.class);
                if (mDPM.isAdminActive(mDeviceAdmin)) {
                    //清除数据
                    mDPM.wipeData(0);
                }
                //广播拦截
                abortBroadcast();
            }
        }
    }

}
