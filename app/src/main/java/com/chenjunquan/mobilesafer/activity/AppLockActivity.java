package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.adapter.AppLockAdapter;
import com.chenjunquan.mobilesafer.bean.AppInfo;
import com.chenjunquan.mobilesafer.engine.AppInfoProvider;
import com.chenjunquan.mobilesafer.engine.AppLockDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 516620911 on 2017.11.04.
 */

public class AppLockActivity extends Activity implements View.OnClickListener {
    private LinearLayout llApplockUnlock;
    private TextView tvApplockLock;
    private ListView lvApplockLock;
    private LinearLayout llApplockLock;
    private TextView tvApplockUnlock;
    private ListView lvApplockUnlock;
    private ArrayList<AppInfo> mAppInfoList;
    private ArrayList<AppInfo> mLockList;
    private ArrayList<AppInfo> mUnLockList;
    private AppLockDao mAppLockDao;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //接收到消息之后填充数据适配器
            //加锁的
            AppLockAdapter lockAdapter = new AppLockAdapter(getApplicationContext(), true, mLockList, mUnLockList);
            lvApplockLock.setAdapter(lockAdapter);
            //未加锁的
            AppLockAdapter unLockAdapter = new AppLockAdapter(getApplicationContext(), false, mLockList, mUnLockList);
            lvApplockUnlock.setAdapter(unLockAdapter);
            super.handleMessage(msg);
        }
    };
    private Button bt_applock_unlock;
    private Button bt_applock_lock;
    public  static TextView tv_applock_unlock;
    public  static TextView tv_applock_lock;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applock);
        initData();
        initUI();
    }


    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取所有应用
                mAppInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                //区别已加锁和未加锁的应用
                mLockList = new ArrayList<>();
                mUnLockList = new ArrayList<>();
                //获取锁数据库的应用=加锁应用
                mAppLockDao = AppLockDao.getInstance(getApplicationContext());
                List<String> lockPackageList = mAppLockDao.findAll();
                for (AppInfo appInfo : mAppInfoList) {
                    if (lockPackageList.contains(appInfo.packageName)) {
                        mLockList.add(appInfo);
                    } else {
                        mUnLockList.add(appInfo);
                    }
                }
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    private void initUI() {
        bt_applock_unlock = (Button) findViewById(R.id.bt_applock_unlock);
        bt_applock_lock = (Button) findViewById(R.id.bt_applock_lock);
        bt_applock_unlock.setOnClickListener(this);
        bt_applock_lock.setOnClickListener(this);
        llApplockUnlock = (LinearLayout) findViewById(R.id.ll_applock_unlock);
        lvApplockLock = (ListView) findViewById(R.id.lv_applock_lock);
        llApplockLock = (LinearLayout) findViewById(R.id.ll_applock_lock);
        lvApplockUnlock = (ListView) findViewById(R.id.lv_applock_unlock);
        tv_applock_lock = (TextView)findViewById(R.id.tv_applock_lock);
        tv_applock_unlock = (TextView)findViewById(R.id.tv_applock_unlock);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_applock_unlock:
                //点击未加锁按钮 加锁布局隐藏 未加锁显示
                llApplockLock.setVisibility(View.GONE);
                llApplockUnlock.setVisibility(View.VISIBLE);
                //2.未加锁变成深色图片,已加锁变成浅色图片
                bt_applock_unlock.setBackgroundResource(R.drawable.tab_right_pressed);
                bt_applock_lock.setBackgroundResource(R.drawable.tab_left_default);
                break;
            case R.id.bt_applock_lock:
                //点击加锁按钮 未加锁布局隐藏 加锁显示
                llApplockLock.setVisibility(View.VISIBLE);
                llApplockUnlock.setVisibility(View.GONE);
                //2.未加锁变成浅色图片,已加锁变成深色图片
                bt_applock_unlock.setBackgroundResource(R.drawable.tab_left_default);
                bt_applock_lock.setBackgroundResource(R.drawable.tab_right_pressed);
                break;
        }
    }


}
