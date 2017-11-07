package com.chenjunquan.mobilesafer.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.service.AddressService;
import com.chenjunquan.mobilesafer.service.BlackNumberService;
import com.chenjunquan.mobilesafer.service.ServiceUtil;
import com.chenjunquan.mobilesafer.service.WatchDogService;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;
import com.chenjunquan.mobilesafer.view.SettingClickView;
import com.chenjunquan.mobilesafer.view.SettingItemView;

/**
 * Created by 516620911 on 2017.10.20.
 */

public class SettingActivity extends BaseActivity {

    private String[] mToastStyle;
    private int mToast_style;
    private SettingClickView scv_toast_style;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentLayout(R.layout.activity_setting);
        initUpdate();
        initAddress();
        initToastStyle();
        initToastLocation();
        initBlacknumber();
        initApplock();
    }

    private void initApplock() {
        final SettingItemView siv_applock = (SettingItemView) findViewById(R.id.siv_applock);
        boolean isRunning = ServiceUtil.isRunning(this, "com.chenjunquan.mobilesafer.service.WatchDogService");
        siv_applock.setCheck(isRunning);
        siv_applock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_applock.isCheck();
                siv_applock.setCheck(!isCheck);
                if(!isCheck){
                    startService(new Intent(getApplicationContext(), WatchDogService.class));
                }else{
                    stopService(new Intent(getApplicationContext(), WatchDogService.class));
                }
            }
        });
    }

    /**
     * 开启黑名单服务
     */
    private void initBlacknumber() {
        final SettingItemView siv_blacknumber = (SettingItemView) findViewById(R.id.siv_blacknumber);
        boolean isRunning = ServiceUtil.isRunning(this, "com.chenjunquan.mobilesafer.service.BlackNumberService");
        siv_blacknumber.setCheck(isRunning);
        siv_blacknumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_blacknumber.isCheck();
                siv_blacknumber.setCheck(!isCheck);
                if(!isCheck){
                    startService(new Intent(getApplicationContext(), BlackNumberService.class));
                }else{
                    stopService(new Intent(getApplicationContext(), BlackNumberService.class));
                }
            }
        });

    }

    private void initToastLocation() {

        SettingClickView scv_toast_location = (SettingClickView) findViewById(R.id.scv_toast_location);
        scv_toast_location.setTitle("归属地提示框的位置");
        scv_toast_location.setDes("设置归属地提示框的位置");
        scv_toast_location.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ToastLocationActivity.class));
            }
        });

    }

    private void initToastStyle() {
        scv_toast_style = (SettingClickView) findViewById(R.id.scv_toast_style);
        scv_toast_style.setTitle("设置归属地显示风格");
        //创建描述文字所在的String数组
        mToastStyle = new String[]{"透明", "橙色", "蓝色", "灰色", "绿色"};
        //SP获取当前存储的吐司样式
        mToast_style = SpUtil.getInt(getApplicationContext(), ConstantValue.TOAST_STYLE, 0);
        //获取文字描述
        scv_toast_style.setDes(mToastStyle[mToast_style]);
        //监听点击时间弹出对话框
        scv_toast_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出一个选择样式的单选dialog
                showToastStyleDialog();
            }
        });
    }

    private void showToastStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择归属地样式").setIcon(R.drawable.ic_launcher);
        //参数(选项数组,默认选中条目,点击事件)
        builder.setSingleChoiceItems(mToastStyle, mToast_style, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SpUtil.putInt(getApplicationContext(),ConstantValue.TOAST_STYLE,which);
                //关闭对话框
                dialog.dismiss();
                //将选中的样式显示到TextView中
                scv_toast_style.setDes(mToastStyle[which]);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 是否显示电话归属地方法
     * 是否开启来电归属地显示的判断条件
     * 不能用SP 需要和服务开启的状态绑定
     */
    private void initAddress() {
        final SettingItemView siv_address= (SettingItemView) findViewById(R.id.siv_address);
        //判断来电归属地服务是否开启(完整包名)
        boolean isRunning = ServiceUtil.isRunning(this, "com.chenjunquan.mobilesafer.service.AddressService");
        siv_address.setCheck(isRunning);
        siv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isCheck = siv_address.isCheck();
                siv_address.setCheck(!isCheck);
                if(!isCheck){
                    startService(new Intent(getApplicationContext(),AddressService.class));
                }else{
                    stopService(new Intent(getApplicationContext(),AddressService.class));
                }
            }
        });
    }

    private void initUpdate() {
        final SettingItemView siv_update = (SettingItemView) findViewById(R.id.siv_update);
        boolean open_update=SpUtil.getBoolean(getApplicationContext(), ConstantValue.OPEN_UPDATE, false);
        siv_update.setCheck(open_update);
        siv_update.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //点击就会取当前状态的相反状态
                        boolean check = siv_update.isCheck();
                        siv_update.setCheck(!check);
                        SpUtil.putBoolean(getApplicationContext(), ConstantValue.OPEN_UPDATE, !check);
                    }
                }
        );
    }
    //卸载应用
    public void uninstall(View view){
        Intent intent = new Intent("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:"+getPackageName()));
        startActivity(intent);
    }
}
