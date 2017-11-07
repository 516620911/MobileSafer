package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2017/11/6.
 */

public class TrafficActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取流量(2G 3G 4G 下载流量)
        long mobileRxBytes = TrafficStats.getMobileRxBytes();
        //总流量(数据网络上传+下载)
        long mobileTxBytes = TrafficStats.getMobileTxBytes();

        //下载流量总和(数据+wifi)
        long totalTxBytes = TrafficStats.getTotalTxBytes();
        //总流量(数据网络+wifi+上传+下载)
        long totalRxBytes = TrafficStats.getTotalRxBytes();
    }
}
