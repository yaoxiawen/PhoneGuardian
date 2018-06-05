package db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class VirusDB {
    public static String isVirus(String md5) {
        String path = "data/data/com.example.admin.phoneguardian/antivirus.db";
        String result = null;
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cs = db.query("datable", new String[]{"desc"}, "md5=?",
                new String[]{md5}, null, null, null, null);
        while (cs.moveToNext()) {
            result = cs.getString(0);
        }
        cs.close();
        db.close();
        return result;
    }

}
