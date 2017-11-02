package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;

import com.chenjunquan.mobilesafer.R;

/**
 * Created by Administrator on 2017/11/2.
 */

public class AppMangerActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        //初始化头部数据
        initTitle();

    }

    private void initTitle() {
        //储存内存路径
        String path = Environment.getDataDirectory().getAbsolutePath();
        //sd卡路径
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        getAvailSpace(path);
        getAvailSpace(sdPath);

    }

    private void getAvailSpace(String path) {
        //
        StatFs statFs = new StatFs(path);

    }
}
