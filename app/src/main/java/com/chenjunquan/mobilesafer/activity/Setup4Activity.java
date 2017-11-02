package com.chenjunquan.mobilesafer.activity;

/**
 * Created by 516620911 on 2017.10.21.
 */

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.receiver.MyDeviceAdminReceiver;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;
import com.chenjunquan.mobilesafer.utils.ToastUtil;

/**
 * Created by 516620911 on 2017.10.21.
 */

public class Setup4Activity extends BaseSetupActivity {
    private CheckBox cb_box;
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup4);

        initUI();
    }

    private void initUI() {
        cb_box = (CheckBox) findViewById(R.id.cb_box);
        //是否选中状态回显
        boolean open_security = SpUtil.getBoolean(this, ConstantValue.OPEN_SECURITY, false);
        //根据状态修改checkbox后续文字显示
        cb_box.setChecked(open_security);
        if (open_security) {
            cb_box.setText("安全设置已开启");
        } else {
            cb_box.setText("安全设置已关闭");
        }

        cb_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //点击后的checkbox状态

                //切换后状态的存储,
                SpUtil.putBoolean(getApplicationContext(), ConstantValue.OPEN_SECURITY, b);
                openDevicePolicyManager();
                if (b) {
                    cb_box.setText("安全设置已开启");
                } else {
                    cb_box.setText("安全设置已关闭");
                }
            }
        });


    }

    private void openDevicePolicyManager() {
        //开启激活设备管理器的activity
        Log.i("openDevicePolicyManager","openDevicePolicyManager");
        //设备管理器组件
        mDPM =(DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
        //组件
        mDeviceAdmin = new ComponentName(this,MyDeviceAdminReceiver.class);
        // Launch the activity to have the user enable our admin.
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "手机卫士设备管理器");
        startActivity(intent);
    }

    public void showNextPage() {
        boolean open_security = SpUtil.getBoolean(getApplicationContext(), ConstantValue.OPEN_SECURITY, false);
        if (open_security) {
            Intent intent = new Intent(this, SetupOverActivity.class);
            startActivity(intent);
            finish();
            SpUtil.putBoolean(this, ConstantValue.SETUP_OVER, true);
            //开启平移动画
            overridePendingTransition(R.anim.next_in_anim,R.anim.next_out_anim);
        }else{
            ToastUtil.show(getApplicationContext(),"请开启手机防盗功能");
        }
    }

    public void showPrePage() {
        Intent intent = new Intent(this, Setup3Activity.class);
        startActivity(intent);
        finish();
        //开启平移动画
        overridePendingTransition(R.anim.pre_in_anim,R.anim.pre_out_anim);
    }
}
