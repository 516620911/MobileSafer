package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;

/**
 * Created by 516620911 on 2017.10.21.
 */

public class SetupOverActivity extends Activity{

    private TextView tv_safe_number;
    private TextView mTv_safe_number;
    private TextView tv_reset_setup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //密码正确,且导航设置完成,则停留在设置完成界面

        boolean setup_over = SpUtil.getBoolean(this, ConstantValue.SETUP_OVER, false);
        if(setup_over){
            setContentView(R.layout.activity_setup_over);
            initUI();
        }else{
            //导航设置未完成 跳转到导航设置界面
            Intent intent = new Intent(this, Setup1Activity.class);
            startActivity(intent);
            finish();
        }
    }

    private void initUI() {
        //安全手机设置
        tv_safe_number = (TextView) findViewById(R.id.tv_safe_number);
        String phone = SpUtil.getString(this, ConstantValue.CONTACT_PHONE, "");
        tv_safe_number.setText(phone);
        //给
        tv_reset_setup = (TextView) findViewById(R.id.tv_reset_setup);
        tv_reset_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Setup1Activity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
