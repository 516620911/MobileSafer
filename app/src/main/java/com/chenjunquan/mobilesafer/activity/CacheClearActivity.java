package com.chenjunquan.mobilesafer.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;

import java.lang.reflect.Method;
import java.util.List;

public class CacheClearActivity extends Activity {


    private static final int UPDATE_CACHE_APP = 100;
    private Button bt_clear;
    private ProgressBar pb_bar;
    private TextView tv_name;
    private LinearLayout ll_add_text;
    private PackageManager mPm;
    private int mIndex = 0;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CACHE_APP:
                    //在线性布局中添加有缓存应用的条目

                    View view = View.inflate(getApplicationContext(), R.layout.cache_item_view, null);

                    ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
                    TextView tv_item_name = (TextView) view.findViewById(R.id.tv_name);
                    TextView tv_cache_size = (TextView)view.findViewById(R.id.tv_cache_size);
                    ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);

                    final CacheInfo cacheInfo = (CacheInfo) msg.obj;
                    iv_icon.setBackgroundDrawable(cacheInfo.icon);
                    tv_item_name.setText(cacheInfo.name);
                    tv_cache_size.setText(Formatter.formatFileSize(getApplicationContext(), cacheInfo.cacheSize));

                    ll_add_text.addView(view, 0);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_clear);
        mPm = getPackageManager();
        initUI();
        initData();


    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.获取包管理者对象

                mPm = getPackageManager();

                //2.获取安装在手机上的所有的应用
                List<PackageInfo> installedPackages = mPm.getInstalledPackages(0);
                //3.给进度条设置最大值(手机中所有应用的总数)
                pb_bar.setMax(installedPackages.size());
                //4.遍历每一个应用,获取有缓存的应用信息(应用名称,图标,缓存大小,包名)
                for (PackageInfo packageInfo : installedPackages) {
                    //包名作为获取缓存信息的条件
                    String packageName = packageInfo.packageName;
                    //获取指定应用缓存大小
                    getPackageCache(packageName);
                    SystemClock.sleep(100);
                }
            }
        }).start();
    }

    /**
     * 系统源码
     * 获取应用缓存
     * 1.以"清除缓存"为关键字搜索setting应用
     * 2.搜索到"清除缓存"所在的string文件,以string中name中的字符串作为关键继续搜索,搜索使用此字符串的布局文件
     * 3.以布局文件名称作为关键字,搜索使用此布局文件的src中java类
     * 4.在java类中找到给布局文件中缓存大小控件赋值的过程
     * 通过包名获取应用缓存信息
     *
     * @param packageName
     */
    private void getPackageCache(String packageName) {
        //观察者对象
        IPackageStatsObserver.Stub mStatsObserver = new IPackageStatsObserver.Stub() {
            //回调函数
            @Override
            public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                //子线程
                //获取指定包名缓存大小
                long cacheSize = pStats.cacheSize;
                //
                //判断缓存大小是否大于零
                if (cacheSize > 0) {
                    //更新UI增加textview
                    Message msg = Message.obtain();
                    msg.what = UPDATE_CACHE_APP;
                    CacheInfo cacheInfo = null;
                    try {
                        //封装有缓存应用的javabean
                        cacheInfo = new CacheInfo();
                        cacheInfo.cacheSize = cacheSize;
                        cacheInfo.packagename = pStats.packageName;
                        cacheInfo.name = mPm.getApplicationInfo(pStats.packageName, 0).loadLabel(mPm).toString();
                        cacheInfo.icon = mPm.getApplicationInfo(pStats.packageName, 0).loadIcon(mPm);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    msg.obj = cacheInfo;
                    mHandler.sendMessage(msg);
                }
            }
        };
        // 获取mStatsObserver对象,然后调用onGetStatsCompleted()方法,调用此方法时可以获取,缓存大小

        // 参数1:获取缓存应用的包名,参数2:aidl文件指向类,对应的对象
        // 问题:PackageManager中getPackageSizeInfo方法隐藏方法不能被调用,需要反射
        // mPm.getPackageSizeInfo("com.android.browser", mStatsObserver);
        try {
            Class<?> clazz = Class.forName("android.content.pm.PackageManager");
            //2.获取调用方法对象
            Method method = clazz.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
            //3.获取对象调用方法
            method.invoke(mPm, packageName, mStatsObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        bt_clear = (Button) findViewById(R.id.bt_clear);
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);
    }

    class CacheInfo {
        public String name;
        public Drawable icon;
        public String packagename;
        public long cacheSize;
    }
}
