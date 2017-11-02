package com.chenjunquan.mobilesafer.engine;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 进度条对话框
 * 进度条
 * Created by Administrator on 2017/11/2.
 */

public class SmsBackup {

    private static int index;


    /**
     * 参数
     * 上下文环境
     * 备份文件路径
     * 回调函数(由调用者处理部分逻辑)
     */
    public static void backup(Context context, String backupPath, CallBack callBack) {
        FileOutputStream fos = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://sms/");
            cursor = contentResolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);
            File file = new File(backupPath);
            fos = new FileOutputStream(file);
            //序列化数据库读取数据放置到xml文件中
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setOutput(fos, "UTF-8");
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "smss");
            //给进度条设置最大值
            //A 如果传递进来的是对话框,指定对话框进度条的总数
            //B	如果传递进来的是进度条,指定进度条的总数
            if (callBack != null) {
                callBack.setMax(cursor.getCount());
            }
            while (cursor.moveToNext()) {
                xmlSerializer.startTag(null, "sms");

                xmlSerializer.startTag(null, "address");
                xmlSerializer.text(cursor.getString(0));
                xmlSerializer.endTag(null, "address");

                xmlSerializer.startTag(null, "date");
                xmlSerializer.text(cursor.getString(1));
                xmlSerializer.endTag(null, "date");

                xmlSerializer.startTag(null, "type");
                xmlSerializer.text(cursor.getString(2));
                xmlSerializer.endTag(null, "type");

                xmlSerializer.startTag(null, "body");
                xmlSerializer.text(cursor.getString(3));
                xmlSerializer.endTag(null, "body");

                xmlSerializer.endTag(null, "sms");
                //每循环一次 即备份完一条 进度+1
                index++;
                //稍微慢点
                Thread.sleep(500);
                //A 如果传递进来的是对话框,指定对话框进度条的当前百分比
                //B	如果传递进来的是进度条,指定进度条的当前百分比
                if (callBack != null) {
                    callBack.setProgress(index);
                }
            }
            xmlSerializer.endTag(null, "smss");
            xmlSerializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null & fos != null) {
                cursor.close();
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //回调
    //1.定义一个接口
    //2,定义接口中未实现的业务逻辑方法(短信总数设置,备份过程中短信百分比更新)
    //3.传递一个实现了此接口的类的对象(至备份短信的工具类中),接口的实现类,一定实现了上诉两个为实现方法(就决定了使用对话框,还是进度条)
    //4.获取传递进来的对象,在合适的地方(设置总数,设置百分比的地方)做方法的调用
    public interface CallBack {
        //短信总数设置为实现方法(由自己决定是用	对话框.setMax(max) 还是用	进度条.setMax(max))
        public void setMax(int max);

        //备份过程中短信百分比更新(由自己决定是用	对话框.setProgress(max) 还是用	进度条.setProgress(max))
        public void setProgress(int index);
    }

    /**
     * 老版本
     * 参数
     * 上下文环境
     * 备份文件路径
     * 进度条对话框对象
     */
    public static void backup(Context context, String backupPath, ProgressBar progressBar) {
        FileOutputStream fos = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Uri.parse("content://sms/");
            cursor = contentResolver.query(uri, new String[]{"address", "date", "type", "body"}, null, null, null);
            File file = new File(backupPath);
            fos = new FileOutputStream(file);
            //序列化数据库读取数据放置到xml文件中
            XmlSerializer xmlSerializer = Xml.newSerializer();
            xmlSerializer.setOutput(fos, "UTF-8");
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "smss");
            //给进度条设置最大值
            progressBar.setMax(cursor.getCount());
            while (cursor.moveToNext()) {
                xmlSerializer.startTag(null, "sms");

                xmlSerializer.startTag(null, "address");
                xmlSerializer.text(cursor.getString(0));
                xmlSerializer.endTag(null, "address");

                xmlSerializer.startTag(null, "date");
                xmlSerializer.text(cursor.getString(1));
                xmlSerializer.endTag(null, "date");

                xmlSerializer.startTag(null, "type");
                xmlSerializer.text(cursor.getString(2));
                xmlSerializer.endTag(null, "type");

                xmlSerializer.startTag(null, "body");
                xmlSerializer.text(cursor.getString(3));
                xmlSerializer.endTag(null, "body");

                xmlSerializer.endTag(null, "sms");
                //每循环一次 即备份完一条 进度+1
                index++;
                //稍微慢点
                Thread.sleep(500);
                progressBar.setProgress(index);
            }
            xmlSerializer.endTag(null, "smss");
            xmlSerializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null & fos != null) {
                cursor.close();
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
