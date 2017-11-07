package com.chenjunquan.mobilesafer.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.chenjunquan.mobilesafer.bean.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系人查询工具类
 * 通过ContentProvider查询联系人
 * 根据表的结构,需要先从raw_contacts表查询联系人contact_id
 * 再到另一张表data查询其它类型的数据
 * Created by 516620911 on 2017.10.21.
 */

public class QueryContactsUtil {
    public static List<Contact> queryContacts(Context context){
        //[0]创建一个集合

        List<Contact>  contactLists = new ArrayList<Contact>();
        //[1]先查询row_contacts表 的contact_id列 我们就知道一共有几条联系人
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        Uri dataUri = Uri.parse("content://com.android.contacts/data");
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"contact_id"}, null, null, null);
        while(cursor!=null&&cursor.moveToNext()){
            String contact_id = cursor.getString(0);
            Contact contact=new Contact();
            contact.setId(contact_id);
            Cursor dataCursor = context.getContentResolver().query(dataUri, new String[]{"data1","mimetype"}, "raw_contact_id=?", new String[]{contact_id}, null);
            while(dataCursor!=null&&dataCursor.moveToNext()){
                String data1 = dataCursor.getString(0);
                String mimetype = dataCursor.getString(1);
                //[3]根据mimetype 区分data1列的数据类型
                if ("vnd.android.cursor.item/name".equals(mimetype)) {
                    contact.setName(data1);
                }else if ("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
                    contact.setPhone(data1);
                }else if ("vnd.android.cursor.item/email_v2".equals(mimetype)) {
                    contact.setEmail(data1);
                }
            }
           // Log.i("con",contact.toString());
            contactLists.add(contact);
        }

        return contactLists;
    }
}
