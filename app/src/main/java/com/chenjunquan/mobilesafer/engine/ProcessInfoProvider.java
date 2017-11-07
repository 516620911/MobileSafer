package com.chenjunquan.mobilesafer.engine;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.ProcessInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2017/11/3.
 * 似乎谷歌最终关闭了所有获得当前前台应用程序包的门。
 * 从5.1开始就限制了getRunningAppProcesses方法 它现在返回您自己的应用程序包的列表
 * https://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag/32366476#32366476
 * <p>
 * 获取60s内运行的应用
 * 获取应用的pid
 * 此方法无法获取进程pid
 * 改用getRunningServices方法替代
 * 获取已安装的应用
 * 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
 * 两者取交集(包名)
 * 缺点如果该应用没有服务正在运行则无法判定
 */

public class ProcessInfoProvider {
    private static final String TAG = "ProcessInfoProvider";

    public static List<ProcessInfo> queryAllRunningAppInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        // 查询所有已经安装的应用程序
        List<ApplicationInfo> listAppcations = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations,
                new ApplicationInfo.DisplayNameComparator(pm));// 排序
        // 保存所有正在运行的包名 以及它所在的进程信息
        Map<String, ActivityManager.RunningServiceInfo> pgkProcessAppMap = new HashMap<String, ActivityManager.RunningServiceInfo>();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 通过调用ActivityManager的getRunningAppProcesses()方法获得系统里所有正在运行的进程
        List<ActivityManager.RunningServiceInfo> appProcessList = mActivityManager
                .getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo appProcess : appProcessList) {
            int pid = appProcess.pid; // pid
            String processName = appProcess.process; // 进程名
            //Log.d("TAG", "processName: " + processName + "  pid: " + pid);
            String pkgNameList = appProcess.service.getPackageName(); // 获得运行在该进程里的所有应用程序包
            //Log.d("TAG", "包名：：：："+pkgNameList);
            // 输出所有应用程序的包名
            pgkProcessAppMap.put(pkgNameList, appProcess);
        }
        // 保存所有正在运行的应用程序信息
        List<ProcessInfo> runningAppInfos = new ArrayList<ProcessInfo>(); // 保存过滤查到的AppInfo
        for (ApplicationInfo applicationInfo : listAppcations) {
            // 如果该包名存在 则构造一个RunningAppInfo对象
            if (pgkProcessAppMap.containsKey(applicationInfo.packageName)) {
                // 获得该packageName的 pid 和 processName
                ProcessInfo processInfo = new ProcessInfo();
                int pid = pgkProcessAppMap.get(applicationInfo.packageName).pid;
                String processName = pgkProcessAppMap.get(applicationInfo.packageName).process;
                //获取已使用内存的大小：
                Debug.MemoryInfo[] processMemoryInfo = mActivityManager.getProcessMemoryInfo(new int[]{pid});
                //单位KB(*1024变成byte好转化)
                processInfo.memeSize = processMemoryInfo[0].getTotalPrivateDirty() * 1024;
                //获取应用的名称
                processInfo.name = applicationInfo.loadLabel(pm).toString();
                processInfo.icon = applicationInfo.loadIcon(pm);
                processInfo.appPid = pid;
                processInfo.packageName = processName;
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                    processInfo.isSystem = true;
                } else {
                    processInfo.isSystem = false;
                }
                Log.i(TAG, processInfo.toString());
                runningAppInfos.add(processInfo);
            }
        }
        return runningAppInfos;
    }

    /**
     * 杀进程方法
     *
     * @param ctx         上下文环境
     * @param processInfo 杀死进程所在的javabean的对象
     */
    public static void killProcess(Context ctx, ProcessInfo processInfo) {
        //1,获取activityManager
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        //2,杀死指定包名进程(权限)
        am.killBackgroundProcesses(processInfo.packageName);
    }

    /**
     * 6.0版本获取相应的进程信息
     *
     * @param context
     * @return
     */
    public static List<ProcessInfo> getProcess6Info(Context context) {
        ArrayList<ProcessInfo> processInfoList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager m = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (m != null) {
                long now = System.currentTimeMillis();
                //获取60秒之内的应用数据
                List<UsageStats> stats = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);
                ActivityManager systemService = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                Log.i(TAG, "Running app number in last 60 seconds : " + stats.size());
                //取得最近运行的一个app，即当前运行的app
                if ((stats != null) && (!stats.isEmpty())) {
                    for (int i = 0; i < stats.size(); i++) {
                      /* if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                           j = i;
                       }*/
                        int i1 = stats.get(i).describeContents();
                        String processName = stats.get(i).getPackageName();
                        Log.i(TAG, "top running app is : " + processName);
                        PackageManager PM = context.getPackageManager();
                        ProcessInfo processInfo = new ProcessInfo();
                        try {
                            processInfo.packageName = processName;
                            ApplicationInfo applicationInfo = PM.getApplicationInfo(processInfo.getPackageName(), 0);
                            int uidForName = applicationInfo.uid;
                            processInfo.appPid = uidForName;
                            //int uidForName = android.os.Process.getUidForName(processName);
                            android.os.Process.myPid();
                            Log.i(TAG, uidForName + "");
                            /***
                             * 此方法未能成功获取进程的内存信息
                             */
                            Debug.MemoryInfo[] processMemoryInfo = systemService.getProcessMemoryInfo(new int[]{3963});
                            //获取已使用的大小：
                            processInfo.memeSize = processMemoryInfo[0].getTotalPrivateDirty() * 1024;
                            //获取应用的名称
                            processInfo.name = applicationInfo.loadLabel(PM).toString();
                            processInfo.icon = applicationInfo.loadIcon(PM);

                            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                                processInfo.isSystem = true;
                            } else {
                                processInfo.isSystem = false;
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            processInfo.name = processInfo.packageName;
                            processInfo.icon = context.getResources().getDrawable(R.drawable.ic_launcher);
                            processInfo.isSystem = true;
                            e.printStackTrace();
                        }
                        processInfoList.add(processInfo);
                    }
                }
            }
        }

        return processInfoList;
    }


    //获取进程总数
    public static int getProcessCount(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //获取正在运行的进程集合
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        return runningAppProcesses.size();
    }

    /**
     * @param context
     * @return 可用内存大小byte
     */
    public static long getAvailSpace(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //构建可用内存对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //给memoryInfo对象赋值
        am.getMemoryInfo(memoryInfo);
        //获取可用内存大小(byte)
        return memoryInfo.availMem;
    }

    public static long getTotalSpace(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //构建可用内存对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //给memoryInfo对象赋值
        am.getMemoryInfo(memoryInfo);
        //获取总内存大小(byte)
        return memoryInfo.totalMem;
    }

    public static long getTotalSpaceOld(Context context) {
        //内存大小写入文件中,读取proc/meminfo文件,读取第一行,获取数字字符,转换成bytes返回
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader("proc/meminfo");
            bufferedReader = new BufferedReader(fileReader);
            String lineOne = bufferedReader.readLine();
            //将字符串转换成字符的数组
            char[] charArray = lineOne.toCharArray();
            //循环遍历每一个字符,如果此字符的ASCII码在0到9的区域内,说明此字符有效
            StringBuffer stringBuffer = new StringBuffer();
            for (char c : charArray) {
                if (c >= '0' && c <= '9') {
                    stringBuffer.append(c);
                }
            }
            return Long.parseLong(stringBuffer.toString()) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null && bufferedReader != null) {
                    fileReader.close();
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

 /*   public static ArrayList<ProcessInfo> getProcessInfo(Context context) {
        *//*AppChecker appChecker = new AppChecker();
        String packageName = appChecker.getForegroundApp(context);
        Log.i("packageName",packageName);
        return null;*//*
        return getProcessInfoDeprecated(context);
    }*/

    /**
     * 过时
     *
     * @return 返回当前正在运行的进程相关信息的集合
     */
    @Deprecated
    public static ArrayList<ProcessInfo> getProcessInfoDeprecated(Context context) {
        ArrayList<ProcessInfo> processInfoList = new ArrayList<>();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = context.getPackageManager();
        //获取正在运行的进程集合
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        //循环遍历,获取进程相关信息封装到集合中(名称,图标,使用内存,包名,是否为系统进程)
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            ProcessInfo processInfo = new ProcessInfo();
            //获取进程名称=进程包名
            processInfo.packageName = info.processName;
            //获取进程占用内存大小(传递一个进程对应pid数组,可以一次获取多个)
            Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{info.pid});
            //数组中第0个为当前进程内存信息对象
            Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
            //获取已使用的内存大小转为byte
            processInfo.memeSize = memoryInfo.getTotalPrivateDirty() * 1024;
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(processInfo.packageName, 0);
                //获取应用名称
                processInfo.name = applicationInfo.loadLabel(packageManager).toString();
                //获取应用图标
                processInfo.icon = applicationInfo.loadIcon(packageManager);
                //判断是否为系统进程
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                    //系统应用
                    processInfo.isSystem = true;
                } else {
                    processInfo.isSystem = false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                //不是可执行应用
                processInfo.name = info.processName;
                processInfo.icon = context.getResources().getDrawable(R.drawable.ic_launcher);
                processInfo.isSystem = true;
                e.printStackTrace();
            }
            processInfoList.add(processInfo);
        }
        return processInfoList;
    }

}
