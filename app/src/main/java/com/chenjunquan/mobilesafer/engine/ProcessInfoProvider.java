package com.chenjunquan.mobilesafer.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Debug;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.ProcessInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/3.
 * 似乎谷歌最终关闭了所有获得当前前台应用程序包的门。
 * 从5.1开始就限制了getRunningAppProcesses方法 它现在返回您自己的应用程序包的列表
 * https://stackoverflow.com/questions/30619349/android-5-1-1-and-above-getrunningappprocesses-returns-my-application-packag/32366476#32366476
 *
 */

public class ProcessInfoProvider {
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

    public static ArrayList<ProcessInfo> getProcessInfo(Context context) {
        /*AppChecker appChecker = new AppChecker();
        String packageName = appChecker.getForegroundApp(context);
        Log.i("packageName",packageName);
        return null;*/
        return getProcessInfoDeprecated(context);
    }
    /**
     * 过时
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
            ProcessInfo processInfo=new ProcessInfo();
            //获取进程名称=进程包名
            processInfo.packageName=info.processName;
            //获取进程占用内存大小(传递一个进程对应pid数组,可以一次获取多个)
            Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{info.pid});
            //数组中第0个为当前进程内存信息对象
            Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
            //获取已使用的内存大小转为byte
            processInfo.memSize = memoryInfo.getTotalPrivateDirty() * 1024;
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(processInfo.packageName, 0);
                //获取应用名称
                processInfo.name = applicationInfo.loadLabel(packageManager).toString();
                //获取应用图标
                processInfo.icon=applicationInfo.loadIcon(packageManager);
                //判断是否为系统进程
                if((applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==ApplicationInfo.FLAG_SYSTEM){
                    //系统应用
                    processInfo.isSystem=true;
                }else{
                    processInfo.isSystem=false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                //不是可执行应用
                processInfo.name=info.processName;
                processInfo.icon=context.getResources().getDrawable(R.drawable.ic_launcher);
                processInfo.isSystem=true;
                e.printStackTrace();
            }
            processInfoList.add(processInfo);
        }
        return processInfoList;
    }

}
