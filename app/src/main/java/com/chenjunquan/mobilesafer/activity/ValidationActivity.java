package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.utils.ToastUtil;

/**
 * 本界面为程序锁验证界面
 * Created by 51662on 2017.11.04.
 */

public class ValidationActivity extends Activity{

    private String mPackagename;
    TextView tv_validation_name;
    ImageView  iv_validation_icon;
    EditText et_validation_psd;
    Button bt_validation_submit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPackagename = getIntent().getStringExtra("packagename");
        setContentView(R.layout.activity_validation);
        initUI();
        initData();
    }

    private void initData() {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(mPackagename, 0);
            iv_validation_icon.setBackgroundDrawable(applicationInfo.loadIcon(packageManager));
            tv_validation_name.setText(applicationInfo.loadLabel(packageManager).toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bt_validation_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String psd = et_validation_psd.getText().toString();
                if(!TextUtils.isEmpty(psd)){
                    if(psd.equals("123")){
                        //密码正确进入应用后发送一个广播告知监听服务去除此应用的拦截
                        Intent intent=new Intent("android.intent.action.RELEASE");
                        intent.putExtra("packagename",mPackagename);
                        sendBroadcast(intent);
                        finish();
                    }else{
                        ToastUtil.show(getApplicationContext(),"密码错误");

                    }
                }else{
                    ToastUtil.show(getApplicationContext(),"请输入密码");
                }
            }
        });
    }

    private void initUI() {
         tv_validation_name= findViewById(R.id.tv_validation_name);
         iv_validation_icon=(ImageView)findViewById(R.id.iv_validation_icon);
         et_validation_psd = (EditText)findViewById(R.id.et_validation_psd);
         bt_validation_submit = (Button)findViewById(R.id.bt_validation_submit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //应用锁问题点回退无限验证界面问题
        //通过隐式意图跳转到桌面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
