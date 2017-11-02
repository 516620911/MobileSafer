package com.chenjunquan.mobilesafer.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.engine.AddressDao;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;

/**
 * 来电状态监听与取消监听
 * Created by Administrator on 2017/10/24.
 */

public class AddressService extends Service {

    private TelephonyManager mTelephonyManager;
    private MyphoneStateListener mPhoneStateListener;
    private View mViewToast;
    private WindowManager mWM;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tv_toast.setText(mAddress);
            super.handleMessage(msg);
        }
    };
    private String mAddress;
    private TextView tv_toast;
    private int[] mDrawableIds;
    private InnerOutCallReceiver mInnerOutCallReceiver;
    private int mScreenHeight;
    private int mScreenWidth;

    @Override
    public void onCreate() {
        //第一次开启服务以后就需要去管理土司显示(归属地悬浮土司)
        //电话状态的监听
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneStateListener = new MyphoneStateListener();
        mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);

        mScreenHeight = mWM.getDefaultDisplay().getHeight();
        mScreenWidth = mWM.getDefaultDisplay().getWidth();

        //监听播出电话的广播过滤条件
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        //创建广播接收者
        mInnerOutCallReceiver = new InnerOutCallReceiver();
        registerReceiver(mInnerOutCallReceiver,intentFilter);
        super.onCreate();
    }
    class InnerOutCallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
           //获取拨出电话
            String phone = getResultData();
            //显示归属地吐司
            showToast(phone);
        }
    }
    class MyphoneStateListener extends PhoneStateListener {
        //重写电话状态发生改变触发的方法

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            //电话状态发生改变
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    //空闲状态没有任何活动 消除吐司
                    if (mWM != null && mViewToast != null) {
                        mWM.removeView(mViewToast);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //摘机状态 至少有个电话活动 拨打或者通话
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃 显示归属地吐司
                    showToast(incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void showToast(String incomingNumber) {
        //Toast.makeText(getApplicationContext(),incomingNumber,0).show();
        //展示规则

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        // params.windowAnimations = com.android.internal.R.style.Animation_Toast;
        //响铃的时候显示吐司 和电话类型一致
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.setTitle("Toast");
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //需要用手拖动 所以可以被触摸
        // | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        //指定吐司所在位置 有疑问
                        params.gravity = Gravity.LEFT + Gravity.TOP;

        mViewToast = View.inflate(this, R.layout.toast_view, null);
        tv_toast = (TextView) mViewToast.findViewById(R.id.tv_toast);
        tv_toast.setOnTouchListener(new View.OnTouchListener() {

            private int startY;
            private int startX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //距离原点的X,Y坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        //移动的距离
                        int disX = moveX - startX;
                        int disY = moveY - startY;
                        //当前控件所在屏幕左上角位置
                        params.x=params.x+disX;
                        params.y=params.y+disY;
                        //容错处理(移动之后的位置不能超过手机屏幕)
                        //左边缘不能超出屏幕
                        if(params.x<0){
                            params.x=0;
                        }
                        //上
                        if(params.y<0){
                            params.y=0;
                        }
                        //右
                        if(params.x>mScreenWidth-mViewToast.getWidth()){
                            params.x = mScreenWidth-mViewToast.getWidth();
                        }
                        //下边缘(屏幕高度-通知栏高度)
                        if(params.y>mScreenHeight-mViewToast.getHeight()-22){
                            params.y = mScreenHeight-mViewToast.getHeight()-22;
                        }


                        //告知移动吐司 按计算出来的坐标去展示
                        mWM.updateViewLayout(mViewToast,params);

                        //重置其实起始坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        //存储移动到的位置
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, params.x);
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, params.y);


                        break;

                }
                //false不响应事件
                //如果想控件既要点击事件,又要响应触摸事件 则返回值结果需要false
                //原因:事件响应传递机制
                //onClick是手指抬起时响应
                return true;
            }
        });
        //读取sp中存储吐司位置的x,y坐标值
        // params.x为吐司左上角的x的坐标
        params.x = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
        // params.y为吐司左上角的y的坐标
        params.y = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);

        //从SP中获取设置的样式并修改
        mDrawableIds = new int[]{
                R.drawable.call_locate_white,
                R.drawable.call_locate_orange,
                R.drawable.call_locate_blue,
                R.drawable.call_locate_gray,
                R.drawable.call_locate_green};
        int spToastStyle = SpUtil.getInt(getApplicationContext(), ConstantValue.TOAST_STYLE, 0);
        tv_toast.setBackgroundResource(mDrawableIds[spToastStyle]);
        mWM.addView(mViewToast, params);
        //查询来电号码归属地
        query(incomingNumber);
    }

    private void query(final String incomingNumber) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAddress = AddressDao.getAddress(incomingNumber);
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        //一定注意要取消对电话状态的监听
        if (mTelephonyManager != null && mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        }
        if(mInnerOutCallReceiver!=null){
            unregisterReceiver(mInnerOutCallReceiver);
        }
        super.onDestroy();
    }
}
