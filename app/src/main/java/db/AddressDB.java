package db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 号码归属地数据库
 */
public class AddressDB {
    /**
     * 号码归属地查询
     * @param num 号码
     * @return 归属地
     */
    public static String getAddress(String num) {
        String path = "data/data/com.example.admin.phoneguardian/address.db";
        String result = "未知号码";
        //用正则匹配手机号码
        if (num.matches("^1[3-8]\\d{9}$")) {
            //打开已有的数据库，打开方式只读
            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            Cursor cs1 = db.query("data1", new String[]{"outkey"}, "id=?",
                    new String[]{num.substring(0, 7)}, null, null, null, null);
            if (cs1.moveToNext()) {
                String outkey = cs1.getString(cs1.getColumnIndex("outkey"));
                Cursor cs2 = db.query("data2", new String[]{"location"}, "id=?",
                        new String[]{outkey}, null, null, null, null);
                if (cs2.moveToNext()) {
                    result = cs2.getString(cs2.getColumnIndex("location"));
                }
                cs2.close();
            }
            cs1.close();
            db.close();
        }
        return result;
    }
}
