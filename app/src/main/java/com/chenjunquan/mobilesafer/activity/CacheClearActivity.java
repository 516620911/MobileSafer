package com.chenjunquan.mobilesafer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.chenjunquan.mobilesafer.utils.ToastUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public class CacheClearActivity extends BaseActivity {


    private static final int UPDATE_CACHE_APP = 100;
    private static final int CHECK_CACHE_APP = 200;
    private static final int CHECK_FINISH = 300;
    private static final int CLEAR_CACHE = 400;
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
                    TextView tv_cache_size = (TextView) view.findViewById(R.id.tv_cache_size);
                    ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);

                    final CacheInfo cacheInfo = (CacheInfo) msg.obj;
                    iv_icon.setBackgroundDrawable(cacheInfo.icon);
                    tv_item_name.setText(cacheInfo.name);
                    tv_cache_size.setText(Formatter.formatFileSize(getApplicationContext(), cacheInfo.cacheSize));

                    ll_add_text.addView(view, 0);

                    iv_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //清除单个选中应用的缓存内容(PackageMananger)

                            /*以下代码如果要执行成功则需要系统应用才可以去使用的权限 android.permission.DELETE_CACHE_FILES
                            try {
                                Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                                //2.获取调用方法对象
                                Method method = clazz.getMethod("deleteApplicationCacheFiles", String.class, IPackageDataObserver.class);
                                //3.获取对象调用方法
                                method.invoke(mPm, cacheInfo.packagename, new IPackageDataObserver.Stub() {
                                    @Override
                                    public void onRemoveCompleted(String packageName, boolean succeeded)
                                            throws RemoteException {
                                        //删除此应用缓存后,调用的方法,子线程中
                                        Log.i("onRemoveCompleted", "onRemoveCompleted.....");
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }*/
                            //源码开发课程(源码(handler机制,AsyncTask(异步请求,手机启动流程)源码))
                            //通过查看系统日志,获取开启清理缓存activity中action和data
                            Intent intent = new Intent("com.android.settings");
                            intent.setData(Uri.parse("package:"+cacheInfo.packagename));
                            startActivity(intent);
                        }
                    });
                    break;
                //正在扫描的APP显示到TextView
                case CHECK_CACHE_APP:
                    tv_name.setText((String) msg.obj);
                    break;
                case CHECK_FINISH:
                    tv_name.setText("扫描完成");
                    break;
                case CLEAR_CACHE:
                    tv_name.setText("缓存清理完成");
                    ll_add_text.removeAllViews();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentLayout(R.layout.activity_cache_clear);
        mPm = getPackageManager();
        initUI();
        initData();


    }

    private void initData() {
        new Thread(new Runnable() {
            private String mName;

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
                    //不规则递增
                    SystemClock.sleep(100 + new Random().nextInt(100));
                    index++;
                    //进度条更新
                    pb_bar.setProgress(index);

                    //将扫描的应用发送给主线程
                    Message message = Message.obtain();
                    message.what = CHECK_CACHE_APP;
                    String name = null;
                    try {
                        name = mPm.getApplicationInfo(packageName, 0).loadLabel(mPm).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    message.obj = name;
                    mHandler.sendMessage(message);

                }
                //扫描完成
                Message message = Message.obtain();
                message.what = CHECK_FINISH;
                mHandler.sendMessage(message);
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
       /* 缓存清理:
        packageManager中方法
        public abstract void freeStorageAndNotify(
        long freeStorageSize, IPackageDataObserver observer);

        参数1:申请的空间大小,如果申请无限大的空间大小的时候,则系统会释放缓存占有的空间,用于凑齐无限大大小.*/
        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Class<?> clazz = Class.forName("android.content.pm.PackageManager");
                    //2.获取调用方法对象
                    Method method = clazz.getMethod("freeStorageAndNotify", long.class, IPackageDataObserver.class);
                    //3.获取对象调用方法
                    method.invoke(mPm, Long.MAX_VALUE, new IPackageDataObserver.Stub() {
                        @Override
                        public void onRemoveCompleted(String packageName, boolean succeeded)
                                throws RemoteException {
                            //清除缓存完成后调用的方法(考虑权限)
                            ToastUtil.show(getApplicationContext(), "清理缓存");
                            Message msg = Message.obtain();
                            msg.what = CLEAR_CACHE;
                            mHandler.sendMessage(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    class CacheInfo {
        public String name;
        public Drawable icon;
        public String packagename;
        public long cacheSize;
    }
}
