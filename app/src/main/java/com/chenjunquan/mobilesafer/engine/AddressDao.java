package com.chenjunquan.mobilesafer.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 2017/10/23.
 */

public class AddressDao {
    //指定数据库路径
    public static String dbPath = "data/data/com.chenjunquan.mobilesafer/files/address.db";
    private static String mAddress;

    /**
     * 打开数据库连接进行访问
     *
     * @param phone
     * @return 归属地
     */
    //
    public static String getAddress(String phone) {
        mAddress = "未知号码";
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$";
        //打开数据库只读
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        if (phone.matches(regex)) {
            //截取手机前七位
            phone = phone.substring(0, 7);

            Cursor cursor = db.query("data1", new String[]{"outkey"}, "id=?", new String[]{phone}, null, null, null);
            //查到即可
            if (cursor.moveToNext()) {
                String outkey = cursor.getString(0);
                Cursor cursor2 = db.query("data2", new String[]{"location"}, "id=?", new String[]{outkey}, null, null, null);
                if (cursor2.moveToNext()) {
                    //获取查询到的电话归属地
                    mAddress = cursor2.getString(0);
                    return mAddress;
                }
            }
            return "未知号码";
        } else {
            int length = phone.length();
            switch (length) {
                case 3://119 110 120 114
                    mAddress = "报警电话";
                    break;
                case 4://119 110 120 114
                    mAddress = "模拟器";
                    break;
                case 5://10086 99555
                    mAddress = "服务电话";
                    break;
                case 7:
                    mAddress = "本地电话";
                    break;
                case 8:
                    mAddress = "本地电话";
                    break;
                case 11:
                    //(3+8) 区号+座机号码(外地),查询data2
                    String area = phone.substring(1, 3);
                    Cursor cursor = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area}, null, null, null);
                    if (cursor.moveToNext()) {
                        mAddress = cursor.getString(0);
                    } else {
                        mAddress = "未知号码";
                    }
                    break;
                case 12:
                    //(4+8) 区号(0791(江西南昌))+座机号码(外地),查询data2
                    String area1 = phone.substring(1, 4);
                    Cursor cursor1 = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area1}, null, null, null);
                    if (cursor1.moveToNext()) {
                        mAddress = cursor1.getString(0);
                    } else {
                        mAddress = "未知号码";
                    }
                    break;
            }
            return mAddress;
        }

    }
}
