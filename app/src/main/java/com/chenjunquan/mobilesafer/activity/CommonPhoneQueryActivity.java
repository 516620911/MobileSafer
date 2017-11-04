package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.adapter.CommonPhoneAdapter;
import com.chenjunquan.mobilesafer.engine.CommonPhoneDao;

import java.util.List;



public class CommonPhoneQueryActivity extends Activity {
	private ExpandableListView elv_common_number;
	private List<CommonPhoneDao.Group> mGroup;
	private CommonPhoneAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_phone);
		initUI();
		initData();
	}

	private void initData() {
		CommonPhoneDao commonPhoneDao=new CommonPhoneDao();
		mGroup = commonPhoneDao.getGroup();
		mAdapter=new CommonPhoneAdapter(getApplicationContext(),mGroup);
		elv_common_number.setAdapter(mAdapter);
		elv_common_number.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                startCall(v);
                return false;
            }
        });

	}

    private void startCall(View view) {
        TextView tv_number = (TextView) view.findViewById(R.id.tv_item_common_phone);
        //开启系统打电话界面
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:"+tv_number.getText().toString()));
        startActivity(intent);
    }

    private void initUI() {
        elv_common_number = (ExpandableListView) findViewById(R.id.elv_common_number);
    }
}