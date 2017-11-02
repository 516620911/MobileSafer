package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.Contact;
import com.chenjunquan.mobilesafer.utils.QueryContactsUtil;

import java.util.List;


/**
 * Created by 516620911 on 2017.10.21.
 */

public class ContactListActivity extends Activity {
    private ListView lv_contact;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mAdapter = new ContactAdapter();
            if (msg.what == 1)
                lv_contact.setAdapter(mAdapter);
            super.handleMessage(msg);

        }
    };
    private List<Contact> mContacts;
    private ContactAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactlist);
        initUI();
        initDate();

    }

    private void initDate() {
        //读取系统联系人可能是个耗时操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                mContacts = QueryContactsUtil.queryContacts(getApplicationContext());
                Message message = Message.obtain();

                mHandler.sendEmptyMessage(1);
            }
        }).start();
    }

    private void initUI() {
        lv_contact = (ListView) findViewById(R.id.lv_contact);
        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*Contact selectContact = (Contact) parent.getSelectedItem();
                String phone = selectContact.getPhone();*/
                Contact selectContact= (Contact) mAdapter.getItem(position);
                String phone = selectContact.getPhone();
                //将数据传递回去
                Intent intent = new Intent();
                intent.putExtra("phone",phone);
                setResult(0,intent);
                finish();
            }
        });
    }



    class ContactAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return mContacts.size();
        }

        @Override
        public Object getItem(int position) {
            return mContacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                view = View.inflate(getApplicationContext(), R.layout.listview_item_contact, null);
            } else {
                view=convertView;
            }

            TextView  tvName = (TextView) view.findViewById(R.id.tv_name);
            TextView  tvPhone = (TextView) view.findViewById(R.id.tv_phone);
            Contact contact = mContacts.get(position);

            tvName.setText(contact.getName());
            tvPhone.setText(contact.getPhone());
            return view;
        }
    }
}
