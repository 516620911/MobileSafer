package com.chenjunquan.mobilesafer.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.chenjunquan.mobilesafer.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Administrator on 2017/11/2.
 */

public class AppInfoProvider {
    /**
     * 返回应用信息列表(图标 包名 名称 安装环境)
     * @param context 上下文
     */
    public static ArrayList<AppInfo> getAppInfoList(Context context){
        //获取包的管理者对象
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
        ArrayList<AppInfo> appInfoslist = new ArrayList<>();
        for (PackageInfo packageInfo:installedPackages){
            AppInfo appInfo=new AppInfo();
            //获取应用的包名
            appInfo.setPackageName(packageInfo.packageName);
            //应用名称
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            appInfo.setName(applicationInfo.loadLabel(pm).toString());
            //图标
            appInfo.setIcon(applicationInfo.loadIcon(pm));
            //判断是否为系统应用
            if((applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
                //系统应用
               // Log.i("applicationInfo",applicationInfo.flags+"-"+ApplicationInfo.FLAG_SYSTEM);
                appInfo.setSystem(true);
            }else{
                appInfo.setSystem(false);
            }
            //判断是否为sd卡应用
            if((applicationInfo.flags&ApplicationInfo.FLAG_EXTERNAL_STORAGE)==ApplicationInfo.FLAG_EXTERNAL_STORAGE){
                //系统应用
               // Log.i("applicationInfo",applicationInfo.flags+"-"+ApplicationInfo.FLAG_SYSTEM);
                appInfo.setSdCard(true);
            }else{
                appInfo.setSdCard(false);
            }
            appInfoslist.add(appInfo);
        }
        return appInfoslist;
    }
}
