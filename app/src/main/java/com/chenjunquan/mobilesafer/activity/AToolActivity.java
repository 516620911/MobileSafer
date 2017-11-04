package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.engine.SmsBackup;

import java.io.File;

public class AToolActivity extends Activity {

    private TextView tv_query_phone_address,tv_sms_backup,tv_common_phone;
    private ProgressBar pb_bar;
    private TextView tv_applock;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atool);
		
		//电话归属地查询方法
		initPhoneAddress();
		//备份短信
		initSmsBackup();
		//常用电话
        initCommonPhone();
        //程序锁
        initAppLock();
	}

    private void initAppLock() {
        tv_applock = (TextView) findViewById(R.id.tv_applock);
        tv_applock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AppLockActivity.class));
            }
        });
    }

    private void initCommonPhone() {
        tv_common_phone = (TextView) findViewById(R.id.tv_common_phone);
        tv_common_phone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CommonPhoneQueryActivity.class));
            }
        });
    }

    private void initSmsBackup() {
        tv_sms_backup = (TextView) findViewById(R.id.tv_sms_backup);
        //底下的进度条
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        tv_sms_backup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showSmsBackUpDialog();
            }
        });
    }

    private void showSmsBackUpDialog() {
	    //对话框进度条
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("短信备份");
        progressDialog.setIcon(R.drawable.ic_launcher);
        //进度条水平显示
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SmsBackup.xml";
                //用回调函数确定是哪种对话框
                SmsBackup.backup(getApplicationContext(), path, new SmsBackup.CallBack() {
                    @Override
                    public void setMax(int max) {
                        progressDialog.setMax(max);
                        pb_bar.setMax(max);
                    }

                    @Override
                    public void setProgress(int index) {
                        progressDialog.setProgress(index);
                        pb_bar.setProgress(index);
                    }
                });
            progressDialog.dismiss();
            }
        }).start();

    }


    private void initPhoneAddress() {
		tv_query_phone_address = (TextView) findViewById(R.id.tv_query_phone_address);
		tv_query_phone_address.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), QueryAddressActivity.class));
			}
		});
	}


}
