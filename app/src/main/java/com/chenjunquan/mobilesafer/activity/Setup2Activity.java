package com.chenjunquan.mobilesafer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;
import com.chenjunquan.mobilesafer.utils.ToastUtil;
import com.chenjunquan.mobilesafer.view.SettingItemView;

/**
 * Created by 516620911 on 2017.10.21.
 */

public class Setup2Activity extends BaseSetupActivity {
    private SettingItemView siv_sim_bound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup2);
        initUI();

    }

    private void initUI() {
        siv_sim_bound = (SettingItemView) findViewById(R.id.siv_sim_bound);
        //读取绑定状态
        String sim_number = SpUtil.getString(getApplicationContext(), ConstantValue.SIM_NUMBER, "");
        //判断序列号
        if (TextUtils.isEmpty(sim_number)) {
            siv_sim_bound.setCheck(false);
        } else {
            siv_sim_bound.setCheck(true);
        }
        siv_sim_bound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取原本的状态
                boolean check = siv_sim_bound.isCheck();
                //点击之后设置成原来的相反状态
                siv_sim_bound.setCheck(!check);
                //如果点击之后是true
                if(!check){
                    //存储SIM序列号
                    TelephonyManager manager= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String simSerialNumber = manager.getSimSerialNumber();
                    SpUtil.putString(getApplicationContext(),ConstantValue.SIM_NUMBER,simSerialNumber);
                }else{
                    //删除对应的sp节点
                    Log.i("s2",check+"");
                    SpUtil.remove(getApplicationContext(),ConstantValue.SIM_NUMBER);
                }

            }
        });
    }

    public void showPrePage() {
        Intent intent = new Intent(this, Setup1Activity.class);
        startActivity(intent);
        finish();
        //开启平移动画
        overridePendingTransition(R.anim.pre_in_anim,R.anim.pre_out_anim);
    }

    public void showNextPage() {
        String sim_number = SpUtil.getString(getApplicationContext(), ConstantValue.SIM_NUMBER, "");
        if(!TextUtils.isEmpty(sim_number)){
            Intent intent = new Intent(this, Setup3Activity.class);
            startActivity(intent);
            finish();
            //开启平移动画
            overridePendingTransition(R.anim.next_in_anim,R.anim.next_out_anim);
        }else{
            ToastUtil.show(this,"请绑定sim卡");
        }

    }
}
