package com.chenjunquan.mobilesafer.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsMessage;
import android.util.Log;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.service.LocationService;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;
import com.chenjunquan.mobilesafer.utils.ToastUtil;

/**
 * Created by Administrator on 2017/10/22.
 */

public class SmsReceiver extends BroadcastReceiver {
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdmin;

    @Override
    public void onReceive(Context context, Intent intent) {
        //是否开启防盗保护
        boolean open_security = SpUtil.getBoolean(context, ConstantValue.OPEN_SECURITY, false);
        //获取短信内容
        if (open_security) {
            Object[] pdus = (Object[]) intent.getExtras().get("pdus");
            //循环遍历短信
            for (Object o : pdus) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) o);
                String fromPhone = smsMessage.getOriginatingAddress();
                String messageBody = smsMessage.getMessageBody();
                Log.i("sms", messageBody);
                //判断是否包含指定字符
                if (messageBody.contains("#*alarm*#")) {
                    //播放音乐
                    /*prepare()和prepareAsync() 提供了同步和异步两种方式设置播放器进入prepare状态，
                    需要注意的是，如果MediaPlayer实例是由create方法创建的，那么第一次启动播放前不需要再调用prepare（）了，
                    因为create方法里已经调用过了。*/
                    MediaPlayer player = MediaPlayer.create(context, R.raw.tears);
                    player.setLooping(true);
                    player.start();

                } else if (messageBody.contains("#*location*#")) {
                    //开启获取位置的服务
                    context.startService(new Intent(context, LocationService.class));
                } else if (messageBody.contains("#*lockscreen*#")) {
                    //设备管理器组件
                    mDPM =(DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    //组件
                    mDeviceAdmin = new ComponentName(context,MyDeviceAdminReceiver.class);
                    //判断本应用是否在设备管理器激活
                    if (mDPM.isAdminActive(mDeviceAdmin)){
                        mDPM.lockNow();
                    }else{
                        ToastUtil.show(context,"请开启相关设备管理器");
                    }
                } else if (messageBody.contains("#*wipedata*#")) {
                    //设备管理器组件
                    mDPM =(DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    //组件
                    mDeviceAdmin = new ComponentName(context,MyDeviceAdminReceiver.class);
                    //判断本应用是否在设备管理器激活
                    if (mDPM.isAdminActive(mDeviceAdmin)){
                        //mDPM.resetPassword("1",0);
                        mDPM.wipeData(0);
                    }else{
                        ToastUtil.show(context,"请开启相关设备管理器");
                    }
                }
            }
        }
    }
}
