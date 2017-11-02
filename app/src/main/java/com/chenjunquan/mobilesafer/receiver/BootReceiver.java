package com.chenjunquan.mobilesafer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;

/**
 * Created by Administrator on 2017/10/22.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //一旦监听到开机广播且SIM序列号发生变更就发送报警短信给指定安全手机
        //获取绑定的SIM 以及当前SIM 进行比对
        String spSimNumber = SpUtil.getString(context, ConstantValue.SIM_NUMBER, "");
        TelephonyManager telephonyManager= (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        String simSerialNumber = telephonyManager.getSimSerialNumber()+"x";
        if(!spSimNumber.equals(simSerialNumber)){
            //发送报警短信
            SmsManager smsManager = SmsManager.getDefault();
            String phone = SpUtil.getString(context, ConstantValue.CONTACT_PHONE, "");
            smsManager.sendTextMessage(phone,null,"sim change",null,null);

        }
    }
}
