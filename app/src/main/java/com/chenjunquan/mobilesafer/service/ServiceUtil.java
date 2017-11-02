package com.chenjunquan.mobilesafer.service;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * Created by Administrator on 2017/10/24.
 */

public class ServiceUtil {
    /**
     * 判断服务是否正在运行
     * @param serviceName
     * @return 服务是否开启
     */
    public static boolean isRunning(Context context,String serviceName){
        //获取activityMananger管理者对象,可以获取当前手机正在运行的所有服务
        ActivityManager mAM= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取手机中正在运行的服务
        List<ActivityManager.RunningServiceInfo> runningServices = mAM.getRunningServices(100);
        for (ActivityManager.RunningServiceInfo runningServiceInfo:runningServices
             ) {
            if(serviceName.equals(runningServiceInfo.service.getClassName())){
                return true;
            }
        }
        return false;
    }
}
