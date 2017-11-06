package com.chenjunquan.mobilesafer.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/23.
 */

public class VirusDao {
    //指定数据库路径
    public static String dbPath = "data/data/com.chenjunquan.mobilesafer/files/antivirus.db";
    //查询数据库中病毒的md5
    public static List<String> getVirusList(){
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query("datable", new String[]{"md5"}, null, null, null, null, null);
        ArrayList<String> virusList = new ArrayList<>();
        while(cursor.moveToNext()){
            virusList.add(cursor.getString(0));
          //  Log.i("VirusDao",cursor.getString(0));
        }
        cursor.close();
        db.close();
        return virusList;
    }
}
