package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;

/**
 * 设置界面中的设置归属地提示框位置功能
 * ImageView双击居中 多点击事件
 * Created by Administrator on 2017/10/25.
 */

public class ToastLocationActivity extends Activity {

    private WindowManager mWM;
    private int mScreenHeight;
    private int mScreenWidth;
    //参数=连续点击数
    private long[] mHits=new long[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast_location);
        initUI();
    }

    private void initUI() {
        final ImageView iv_drag = (ImageView) findViewById(R.id.iv_drag);
        final Button bt_top = (Button) findViewById(R.id.bt_top);
        final Button bt_bottom = (Button) findViewById(R.id.bt_bottom);
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenHeight = mWM.getDefaultDisplay().getHeight();
        mScreenWidth = mWM.getDefaultDisplay().getWidth();

        int locationX = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
        int locationY = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);
        //imageView在相对布局中,所以其所在位置的规则要由相对布局提供

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        //将左上角坐标赋值给控件外边距
        layoutParams.leftMargin=locationX;
        layoutParams.topMargin=locationY;
        //将以上规则设置在iv_drag上
        iv_drag.setLayoutParams(layoutParams);
        if(locationY>mScreenHeight/2){
            bt_bottom.setVisibility(View.INVISIBLE);
            bt_top.setVisibility(View.VISIBLE);
        }else{
            bt_bottom.setVisibility(View.VISIBLE);
            bt_top.setVisibility(View.INVISIBLE);
        }
        //双击居中 点击事件监听
        iv_drag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("iv_drag","iv_drag.setOnClickListener");
                //多点击事件的系统源码
                System.arraycopy(mHits,1,mHits,0,mHits.length-1);
                mHits[mHits.length-1]= SystemClock.uptimeMillis();
                if(mHits[mHits.length-1]-mHits[0]<500){
                    //满足双击事件 居中控件
                    int left=mScreenWidth/2-iv_drag.getWidth()/2;
                    int top=mScreenHeight/2-iv_drag.getHeight()/2;
                    int right=mScreenWidth/2+iv_drag.getWidth()/2;
                    int bottom=mScreenHeight/2+iv_drag.getHeight()/2;
                    iv_drag.layout(left,top,right,bottom);
                    //存储位置
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, iv_drag.getLeft());
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, iv_drag.getTop());
                }

            }
        });
        iv_drag.setOnTouchListener(new View.OnTouchListener() {

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
                        int left = iv_drag.getLeft() + disX;//左
                        int top = iv_drag.getTop() + disY;//上

                        int right = iv_drag.getRight() + disX;//右
                        int bottom = iv_drag.getBottom() + disY;//下
                        //容错处理(移动之后的位置不能超过手机屏幕)
                        //左边缘不能超出屏幕
                        if(left<0){
                            return true;
                        }
                        //右
                        if(right>mScreenWidth){
                            return true;
                        }
                        //上
                        if(top<0){
                            return true;
                        }
                        //下边缘(屏幕高度-通知栏高度)
                        if(bottom>mScreenHeight-22){
                            return true;
                        }
                        if(top>mScreenHeight/2){
                            bt_bottom.setVisibility(View.INVISIBLE);
                            bt_top.setVisibility(View.VISIBLE);
                        }else{
                            bt_bottom.setVisibility(View.VISIBLE);
                            bt_top.setVisibility(View.INVISIBLE);
                        }

                        //告知移动控件 按计算出来的坐标去展示
                        iv_drag.layout(left, top, right, bottom);

                        //重置其实起始坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        //存储移动到的位置
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, iv_drag.getLeft());
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, iv_drag.getTop());


                        break;

                }
                //false不响应事件
                //如果想控件既要点击事件,又要响应触摸事件 则返回值结果需要false
                //原因:事件响应传递机制
                //onClick是手指抬起时响应
                return false;
            }
        });
    }

}
