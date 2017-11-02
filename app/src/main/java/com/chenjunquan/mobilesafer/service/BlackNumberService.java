package com.chenjunquan.mobilesafer.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.chenjunquan.mobilesafer.bean.BlackListInfo;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * Created by Administrator on 2017/11/2.
 */
/*
4.4及其以后系统，只能设置一个默认的SMS短信app，但短信到达，首先会通知这个app，并且只有这个app有对短信数据库的修改权限和短信的发送权限
并且短信广播，不再是有序广播，也就是App没有办法拦截这个广播，所有app都快接收到短信到达的广播通知，但是只有默认SMS短信app可以修改短信记录
 */
public class BlackNumberService extends Service {

    private InnerSmsReceiver mInnerSmsReceiver;
    private TelephonyManager mTelephonyManager;
    private MyphoneStateListener mPhoneStateListener;

    @Override
    public void onCreate() {
        super.onCreate();
        //拦截短信的过滤
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(Integer.MAX_VALUE);
        //创建SMS广播接受者
        mInnerSmsReceiver = new InnerSmsReceiver();
        registerReceiver(mInnerSmsReceiver,intentFilter);

        //电话状态的监听
        //有电话拨入,处于响铃状态,响铃状态通过代码去挂断电话(aidl,反射),拦截电话
        //挂断电话号码的方法,放置在了aidl文件中名称为endCall
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new MyphoneStateListener();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    class MyphoneStateListener extends PhoneStateListener {
        //重写电话状态发生改变触发的方法

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //电话状态发生改变
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:

                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //摘机状态 至少有个电话活动 拨打或者通话
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃 显示归属地吐司
                    //有电话拨入,处于响铃状态,响铃状态通过代码去挂断电话(aidl,反射),拦截电话
                    //挂断电话号码的方法,放置在了aidl文件中名称为endCall
                    //endCall();
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
    //**************************无法使用**************************************************
    private void endCall() {

      /* if(mode == 2 || mode == 3){
            //			ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
            //ServiceManager此类android对开发者隐藏,所以不能去直接调用其方法,需要反射调用
            try {
                //1,获取ServiceManager字节码文件
                Class<?> clazz = Class.forName("android.os.ServiceManager");
                //2,获取方法
                Method method = clazz.getMethod("getService", String.class);
                //3,反射调用此方法
                IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
                //4,调用获取aidl文件对象方法
                ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
                //5,调用在aidl中隐藏的endCall方法
                iTelephony.endCall();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

    }

    class InnerSmsReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取短信内容和电话 如果在黑名单中 拦截模式1短信或3所有 拦截短信
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            for (Object object:objects){
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) object);
                String originatingAddress = sms.getOriginatingAddress();
                String messageBody = sms.getMessageBody();
                //查询短信当前号码拦截模式
                List<BlackListInfo> BlackListInfo = DataSupport.select("mode").where("phone=?", originatingAddress).find(BlackListInfo.class);
                int mode=BlackListInfo.get(0).getMode();
                if(mode==1||mode==3){
                    //直接截断广播 阻止系统短信接受此短信
                    Log.i("mode",mode+"");
                    abortBroadcast();
                }
            }

        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mInnerSmsReceiver!=null){
            unregisterReceiver(mInnerSmsReceiver);
        }
    }
}
