package com.chenjunquan.mobilesafer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;
import com.chenjunquan.mobilesafer.utils.ToastUtil;

/**
 * Created by 516620911 on 2017.10.21.
 */

public class Setup3Activity extends BaseSetupActivity{

    private EditText et_phone_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup3);
        initUI();
    }

    private void initUI() {
        et_phone_number = (EditText) findViewById(R.id.et_phone_number);
        String phone = SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_PHONE, "");
        if(phone!=null){
            et_phone_number.setText(phone);
        }
        Button btn_select_contact= (Button) findViewById(R.id.btn_select_contact);
        btn_select_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //跳转到选择联系人的界面
                Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
                startActivityForResult(intent,0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0&&resultCode==0){
            String phone = data.getStringExtra("phone");
            //将特殊字符过滤掉
            phone = phone.replace("-", "").replace(" ","").trim();
            if(phone!=null)
            et_phone_number.setText(phone);

        }
    }

    public void showNextPage(){
        //
        String phone = et_phone_number.getText().toString();
        SpUtil.putString(getApplicationContext(), ConstantValue.CONTACT_PHONE,phone);
       if(!TextUtils.isEmpty(phone)) {

           Intent intent = new Intent(this, Setup4Activity.class);
           startActivity(intent);
           finish();
           //开启平移动画
           overridePendingTransition(R.anim.next_in_anim,R.anim.next_out_anim);
       }else
           ToastUtil.show(getApplicationContext(),"请输入电话号码");
    }
    public void showPrePage(){
        Intent intent = new Intent(this, Setup2Activity.class);
        startActivity(intent);
        finish();
        //开启平移动画
        overridePendingTransition(R.anim.pre_in_anim,R.anim.pre_out_anim);
    }
}
