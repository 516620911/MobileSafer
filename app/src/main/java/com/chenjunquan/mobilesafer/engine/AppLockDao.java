package com.chenjunquan.mobilesafer.engine;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.chenjunquan.mobilesafer.db.AppLockOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用锁数据库查询
 */
public class AppLockDao {
    private  Context mContext;
    private AppLockOpenHelper appLockOpenHelper;
    //BlackNumberDao单例模式
    //1,私有化构造方法
    private AppLockDao(Context context){
        //创建数据库已经其表机构
        appLockOpenHelper = new AppLockOpenHelper(context);
        this.mContext=context;
    }
    //2,声明一个当前类的对象
    private static AppLockDao appLockDao = null;
    //3,提供一个静态方法,如果当前类的对象为空,创建一个新的
    public static AppLockDao getInstance(Context context){
        if(appLockDao == null){
            appLockDao = new AppLockDao(context);
        }
        return appLockDao;
    }

    //插入方法
    public void insert(String packagename){
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("packagename", packagename);

        db.insert("applock", null, contentValues);
        db.close();
        //内容观察者 用于告知数据库发生改变 提醒重新获取数据 以解决应用锁服务开启后 无法添加新的应用锁
        mContext.getContentResolver().notifyChange(Uri.parse("content://applock/change"),null);

    }
    //删除方法
    public void delete(String packagename){
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("packagename", packagename);

        db.delete("applock", "packagename = ?", new String[]{packagename});

        db.close();
        mContext.getContentResolver().notifyChange(Uri.parse("content://applock/change"),null);
    }
    //查询所有
    public List<String> findAll(){
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("applock", new String[]{"packagename"}, null, null, null, null, null);
        List<String> lockPackageList = new ArrayList<String>();
        while(cursor.moveToNext()){
            lockPackageList.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return lockPackageList;
    }
}
