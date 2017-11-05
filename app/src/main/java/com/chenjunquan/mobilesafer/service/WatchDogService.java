package com.chenjunquan.mobilesafer.service;

import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chenjunquan.mobilesafer.activity.ValidationActivity;
import com.chenjunquan.mobilesafer.engine.AppLockDao;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 监视应用开启的服务
 * 通过任务栈监听当前开启的应用
 * 所有加了程序锁的应用只有一个应用通过验证后能不被拦截
 * Created by 516620911 on 2017.11.04.
 */

public class WatchDogService extends Service {
    private boolean isWatch = true;
    private AppLockDao mAppLockDao;
    private List<String> mPackagenameList;
    private ActivityManager mActivityManager;
    private UsageStatsManager mUsageStatsManager;
    private String mReleaseApp="";
    private AppReleaseReceiver mAppReleaseReceiver;
    private MyContentObserver mMyContentObserver;

    @Override
    //维护一个死循环 监听开启的应用 并选择是否拦截
    public void onCreate() {
        mAppLockDao = AppLockDao.getInstance(getApplicationContext());
        //UsageStatsManager权限
        //魅族和小米手机不能通过UsageStatsManager获取应用使用情况
        /*对于每一个应用来说，系统会记录以下信息：
            应用最后一次被用的时间
            对应存储的4个级别，应用在前台的总共时间
            时间戳：一个组件一天之内改变状态的时刻（从前台到后台，或从后台到前台），这个组件可以通过包名或activity的名字来唯一标示。
            时间戳：设备配置信息改变的时刻，如：横竖屏切换。*/
        getPermission();
        watch();
        //注册广播接收者,接收可释放的(已验证)应用包名
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("android.intent.action.RELEASE");
        mAppReleaseReceiver = new AppReleaseReceiver();
        registerReceiver(mAppReleaseReceiver,intentFilter);
        //应用锁问题3
        //注册一个内容观察者 以解决 服务开启后无法增加应用的问题(数据库发生改变)
        mMyContentObserver = new MyContentObserver(new Handler());
        getContentResolver().registerContentObserver(Uri.parse("content://applock/change"),true, mMyContentObserver);
        super.onCreate();
    }

    //获取权限
    private void getPermission(){
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
    }

    //获取到栈顶应用程序的包名
    public String getTopActivty() {
        String topPackageName = null;
        //android5.0以上获取方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            long time = System.currentTimeMillis();
            /*intervalType 时间间隔的类型，5种，对应上面的4个，还有一个，后面再说
                beginTime 开始的时间
                endTime 结束的时间*/
            //十秒前到现在的进程运行情况
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
            if (stats != null) {

                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    //按最近使用时间排序
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    //取出最后一个即为最近使用(毫秒值最大)
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    Log.e("TopPackage Name", topPackageName);
                }
            }

        }
        //android5.0以下获取方式
        else {
            List<ActivityManager.RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo taskInfo = tasks.get(0);
            topPackageName = taskInfo.topActivity.getPackageName();
        }
        return topPackageName;
    }


    private void watch() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mPackagenameList = mAppLockDao.findAll();
                while (isWatch) {
                    String packageName = getTopActivty();
                    if(packageName==null) continue;
                    //过时
                  /*  //通过任务栈监听当前开启的应用
                    mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    //获取正在开启应用的任务栈的集合
                    List<ActivityManager.RunningTaskInfo> runningTasks = mActivityManager.getRunningTasks(10);
                    //集合的第一个为最新开启的应用
                    ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
                    //获取栈顶的activity的包 名
                    String packageName = runningTaskInfo.topActivity.getPackageName();*/
                    //判断是否需要拦截
                    if (mPackagenameList.contains(packageName)) {
                        //应用锁问题2判断是否放弃拦截(已通过验证)
                        if(!mReleaseApp.equals(packageName)) {
                            //拦截则跳转到验证界面(启动模式为SingleInstance销毁可以返回到原来的应用)
                            Intent intent = new Intent(getApplicationContext(), ValidationActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("packagename", packageName);
                            startActivity(intent);
                        }
                    }
                    //考虑性能问题 不要太过占用CUP 时间片
                    SystemClock.sleep(500);
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        isWatch = false;
        if(mAppReleaseReceiver!=null) {
            unregisterReceiver(mAppReleaseReceiver);
        }
        if(mMyContentObserver!=null){
            getContentResolver().unregisterContentObserver(mMyContentObserver);
        }
        super.onDestroy();
    }
    class AppReleaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取释放应用的包名
            mReleaseApp = intent.getStringExtra("packagename");

        }
    }

    private class MyContentObserver extends ContentObserver{
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //数据发生改变重新查询数据库
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mPackagenameList = mAppLockDao.findAll();
                }
            }).start();
            super.onChange(selfChange);

        }

    }
}
