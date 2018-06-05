package utils;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Xml;
import org.xmlpull.v1.XmlSerializer;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 短信工具
 */
public class SmsUtils {
    //暴露接口
    //通过接口将逻辑代码中的数据暴露到外面取
    public interface SmsBackupCallBack{
        void beforeSmsBackup(int total);
        void onSmsBackup(int progress);
    }

    /**
     * 短信备份
     * @param context
     * @param smsBackupCallBack 暴露的接口
     * @return
     */
    public static boolean backup(Context context, SmsBackupCallBack smsBackupCallBack) {
        try {
            //把查询到的短信生成xml文件存储，使用xml序列化器
            XmlSerializer serializer = Xml.newSerializer();
            File file = new File(Environment.getExternalStorageDirectory(),"backup.xml");
            FileOutputStream os = new FileOutputStream(file);
            serializer.setOutput(os,"utf-8");
            serializer.startDocument("utf-8",true);
            serializer.startTag(null,"smss");
            //通过内容观察者对短信进行查询
            Uri uri = Uri.parse("content://sms/");
            ContentResolver cr = context.getContentResolver();
            Cursor cs = cr.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);
            //通过接口将数据暴露出去
            smsBackupCallBack.beforeSmsBackup(cs.getCount());
            //设置根节点上面的属性
            serializer.attribute(null,"size",Integer.toString(cs.getCount()));
            int progress = 0;
            while (cs.moveToNext()) {
                serializer.startTag(null,"sms");
                serializer.startTag(null,"address");
                serializer.text(cs.getString(0));
                serializer.endTag(null,"address");
                serializer.startTag(null,"date");
                serializer.text(cs.getString(1));
                serializer.endTag(null,"date");
                serializer.startTag(null,"type");
                serializer.text(cs.getString(2));
                serializer.endTag(null,"type");
                serializer.startTag(null,"body");
                serializer.text(cs.getString(3));
                serializer.endTag(null,"body");
                serializer.endTag(null,"sms");
                progress++;
                //通过接口将数据暴露出去
                smsBackupCallBack.onSmsBackup(progress);
            }
            serializer.endTag(null,"smss");
            serializer.endDocument();
            cs.close();
            os.flush();
            os.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
