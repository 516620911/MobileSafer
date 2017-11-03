package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.adapter.ProcessAdapter;
import com.chenjunquan.mobilesafer.bean.ProcessInfo;
import com.chenjunquan.mobilesafer.engine.ProcessInfoProvider;

import java.util.ArrayList;

public class ProcessManagerActivity extends Activity implements View.OnClickListener {

    private TextView tv_process_count;
    private TextView tv_memory_info;
    private TextView tv_des;
    private ListView lv_process_list;
    private Button bt_select_all;
    private Button bt_select_reverse;
    private Button bt_clear;
    private Button bt_setting;
    private int mProcessCount;
    private String mStrTotalSpace;
    private ArrayList<ProcessInfo> mProcessInfoList;
    private ArrayList<ProcessInfo> mUserProcessList;
    private ArrayList<ProcessInfo> mSystemProcessList;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ProcessAdapter processAdapter = new ProcessAdapter(getApplicationContext(), mUserProcessList, mSystemProcessList);
            lv_process_list.setAdapter(processAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_manager);
        initUI();
        //初始化进程数量和内存大小
        initTitleData();
        ProcessInfoProvider.getProcessInfo(getApplicationContext());
    }

    /* void requestUsageStatsPermission() {
         if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                 && !hasUsageStatsPermission(this)) {
             startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
         }
     }

     @TargetApi(Build.VERSION_CODES.KITKAT)
     boolean hasUsageStatsPermission(Context context) {
         AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
         int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                 android.os.Process.myUid(), context.getPackageName());
         boolean granted = mode == AppOpsManager.MODE_ALLOWED;
         return granted;
     }*/
    @Override
    protected void onResume() {
        super.onResume();
        initProcessInfoList();
    }

    private void initProcessInfoList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mProcessInfoList = ProcessInfoProvider.getProcessInfo(getApplicationContext());
                mSystemProcessList = new ArrayList<>();
                mUserProcessList = new ArrayList<>();
                for (ProcessInfo processInfo : mProcessInfoList) {
                    Log.i("processInfo", processInfo.toString());
                    if (processInfo.isSystem) {
                        mSystemProcessList.add(processInfo);
                    } else {
                        mUserProcessList.add(processInfo);
                    }
                }
                Log.i("mSystemProcessList", mSystemProcessList.toString());
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    private void initTitleData() {
        mProcessCount = ProcessInfoProvider.getProcessCount(getApplicationContext());
        tv_process_count.setText("进程总数:" + mProcessCount);

        //获取可用内存大小 格式化
        String strAvailSpace = Formatter.formatFileSize(getApplicationContext(), ProcessInfoProvider.getAvailSpace(getApplicationContext()));
        //获取总内存大小 格式化
        mStrTotalSpace = Formatter.formatFileSize(getApplicationContext(), ProcessInfoProvider.getTotalSpace(getApplicationContext()));

        tv_memory_info.setText("剩余/总共:" + strAvailSpace + "/" + mStrTotalSpace);
    }

    private void initUI() {
        tv_process_count = (TextView) findViewById(R.id.tv_process_count);
        tv_memory_info = (TextView) findViewById(R.id.tv_memory_info);
        tv_des = (TextView) findViewById(R.id.tv_des);
        lv_process_list = (ListView) findViewById(R.id.lv_process);
        bt_select_all = (Button) findViewById(R.id.bt_process_all);
        bt_select_reverse = (Button) findViewById(R.id.bt_process_reverse);
        bt_clear = (Button) findViewById(R.id.bt_process_clear);
        bt_setting = (Button) findViewById(R.id.bt_process_setting);
        bt_select_all.setOnClickListener(this);
        bt_select_reverse.setOnClickListener(this);
        bt_clear.setOnClickListener(this);
        bt_setting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bt_process_all:
                selectAll();
                break;
            case R.id.bt_process_reverse:
                selectReverse();
                break;
            case R.id.bt_process_clear:
                clearAll();
                break;
            case R.id.bt_process_setting:

                break;
        }

    }

    private void clearAll() {

    }

    private void selectReverse() {

    }

    private void selectAll() {
    }

}
