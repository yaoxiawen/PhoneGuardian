package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

import bean.BlackNumberInfo;

//黑名单数据库
public class BlackNumberdb {
    private BlackNumberOpenHelper helper;

    public BlackNumberdb(Context context) {
        helper = new BlackNumberOpenHelper(context);
    }

    public long add(String number, String mode) {
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("number", number);
        cv.put("mode", mode);
        long i = db.insert("number", null, cv);
        db.close();
        return i;
    }

    public int delete(String number) {
        SQLiteDatabase db = helper.getReadableDatabase();
        int i = db.delete("number", "number = ?", new String[]{number});
        db.close();
        return i;
    }

    public int update(String number, String newMode) {
        SQLiteDatabase db = helper.getReadableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("mode", newMode);
        int i = db.update("number", cv, "number=?", new String[]{number});
        db.close();
        return i;
    }

    public boolean findNumber(String number) {
        boolean result = false;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cs = db.query("number", new String[]{"number"}, "number=?", new String[]{number}, null, null, null);
        if (cs.moveToNext()) {
            result = true;
        }
        cs.close();
        db.close();
        return result;
    }

    public String findMode(String number) {
        String mode = null;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cs = db.query("number", new String[]{"mode"}, "number=?", new String[]{number}, null, null, null);
        if (cs.moveToNext()) {
            mode = cs.getString(0);
        }
        cs.close();
        db.close();
        return mode;
    }

    public List<BlackNumberInfo> findAll() {
        List<BlackNumberInfo> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cs = db.query("number", new String[]{"number", "mode"}, null, null, null, null, null);
        while (cs.moveToNext()) {
            BlackNumberInfo bn = new BlackNumberInfo();
            bn.setNumber(cs.getString(0));
            bn.setMode(cs.getString(1));
            list.add(bn);
        }
        cs.close();
        db.close();
        return list;
    }

    //分批加载数据
    public List<BlackNumberInfo> findPart(int startIndex, int maxCount) {
        List<BlackNumberInfo> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        //为使数据倒序显示，新添加的数据显示在最上方，查询时降序查询
        Cursor cs = db.query("number", new String[]{"number", "mode"}, null, null,
                null, null, "_id desc", startIndex + "," + maxCount);
        while (cs.moveToNext()) {
            BlackNumberInfo bn = new BlackNumberInfo();
            bn.setNumber(cs.getString(0));
            bn.setMode(cs.getString(1));
            list.add(bn);
        }
        cs.close();
        db.close();
        return list;
    }

    public int getTotalCount() {
        int count = 0;
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.rawQuery("select count(*) from number", null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        database.close();
        return count;
    }
}
