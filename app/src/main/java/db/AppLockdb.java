package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppLockdb {
    private AppLockOpenHelper helper;

    public AppLockdb(Context context) {
        helper = new AppLockOpenHelper(context);
    }

    public long add(String packagename) {
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("packagename", packagename);
        long i = db.insert("applock", null, cv);
        db.close();
        return i;
    }

    public int delete(String packagename) {
        SQLiteDatabase db = helper.getReadableDatabase();
        int i = db.delete("applock", "packagename = ?", new String[]{packagename});
        db.close();
        return i;
    }

    public boolean find(String packagename) {
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cs = db.query("applock", new String[]{"packagename"}, "packagename=?", new String[]{packagename}, null, null, null);
        if (cs.moveToNext()) {
            result = true;
        }
        cs.close();
        db.close();
        return result;
    }
}
