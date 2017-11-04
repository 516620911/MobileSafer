package com.chenjunquan.mobilesafer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by 516620911 on 2017.11.04.
 */

public class ReleaseReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        //获取释放应用的包名
        String packagename = intent.getStringExtra("packagename");

    }
}
