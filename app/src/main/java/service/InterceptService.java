package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import db.AddressDB;
import db.BlackNumberdb;

public class InterceptService extends Service {
    private SmsInterceptReceiver receiver;
    private BlackNumberdb bndb;
    private TelephonyManager tm;
    private MyPhoneStateListener listener;
    //来电记录
    private Uri uri = Uri.parse("content://call_log/calls");

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bndb = new BlackNumberdb(this);
        receiver = new SmsInterceptReceiver();
        //电话拦截
        //获取电话管理器
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        listener = new MyPhoneStateListener();
        //监听电话状态
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        //短信拦截，短信的广播接收者，代码注册
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(receiver, filter);
    }

    /**
     * 短信拦截，创建短信的广播接收者
     */
    class SmsInterceptReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            for (Object obj : objects) {
                SmsMessage message = SmsMessage.createFromPdu((byte[]) obj);
                String number = message.getOriginatingAddress();
                String mode = bndb.findMode(number);
                //短信拦截，mode为2或3
                if ("2".equals(mode) || "3".equals(mode)) {
                    //拦截广播
                    abortBroadcast();
                }
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                //电话状态为响铃
                case TelephonyManager.CALL_STATE_RINGING:
                    String mode = bndb.findMode(incomingNumber);
                    //电话拦截，mode为1或3
                    if ("1".equals(mode) || "3".equals(mode)) {
                        //挂断电话
                        endCall();
                        //删除来电记录
                        deleteRecord(incomingNumber);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /**
     * 删除来电记录
     * 利用内容观察者和内容提供者
     *
     * @param incomingNumber 来电号码
     */
    private void deleteRecord(String incomingNumber) {
        //利用内容观察者去监听日志的改变
        //注册内容观察者，监听来电记录
        getContentResolver().registerContentObserver(uri, true,
                new MyContentObserver(new Handler(), incomingNumber));
    }

    /**
     * 自定义的内容观察者
     */
    private class MyContentObserver extends ContentObserver {
        private String incomingNumber;

        //将来电号码传入进来
        public MyContentObserver(Handler handler, String incomingNumber) {
            super(handler);
            this.incomingNumber = incomingNumber;
        }

        /**
         * 观察到日志改变
         * @param selfChange
         */
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //删除来电记录中数据
            getContentResolver().delete(uri, "number=?", new String[]{incomingNumber});
            //取消注册内容观察者
            getContentResolver().unregisterContentObserver(this);
        }
    }

    /**
     * 挂断电话，利用反射技术
     */
    private void endCall() {
        try {
            //通过类加载器加载ServiceManager，得到字节码对象，注意要写全名
            Class clazz = getClassLoader().loadClass("android.os.ServiceManager");
            //通过反射得到当前的方法
            //参数：方法名，该方法的参数类型
            Method method = clazz.getDeclaredMethod("getService", String.class);
            //调用方法得到远程服务代理类
            //参数：调用上一步中getService方法的对象，由于getService方法为静态方法，所以传入null
            //参数：传入上一步中getService方法的参数对象，对应String.class，TELEPHONY_SERVICE就是一个字符串
            IBinder iBinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);
            //远程服务代理对象iBinder需要一个aidl文件文件去生成方法去管理服务
            ITelephony telephony = ITelephony.Stub.asInterface(iBinder);
            telephony.endCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        unregisterReceiver(receiver);
        receiver = null;
        super.onDestroy();
    }
}
