package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.SpUtil;
import com.chenjunquan.mobilesafer.utils.StreamUtil;
import com.chenjunquan.mobilesafer.utils.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Administrator 欢迎界面 显示版本号 检测更新
 */
public class SplashActivity extends Activity {
    private TextView tv_version_name;
    private RelativeLayout rl_root;
    private int mLocalVersionCode;
    private static final String tag = "SplashActivity";
    //版本描述信息
    private String mVersionDes;
    private String mDownloadUrl;
    /**
     * 更新新版本状态码
     */
    protected static final int UPDATE_VERSION = 100;
    /**
     * 不更新直接跳转
     */
    protected static final int ENTER_HOME = 101;
    /**
     * URL地址出错或IO流错误
     */
    protected static final int IO_ERROR = 102;

    /**
     * JSON解析出错
     */
    protected static final int JSON_ERROR = 103;
    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_VERSION:
                    //弹出对话框提示用户更新
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    //跳转应用主界面
                    enterHome();
                    break;
                case IO_ERROR:
                    //URL或IO出错
                    ToastUtil.show(getApplicationContext(), "URL或IO出错");
                    //出现异常也要跳转到主页面给用户使用
                    enterHome();
                    break;
                case JSON_ERROR:
                    ToastUtil.show(getApplicationContext(), "JSON解析出错");
                    //出现异常也要跳转到主页面给用户使用
                    enterHome();
                    break;

            }
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除当前activity的title 但是每个activity都要设置太麻烦
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        //初始化UI
        initUI();
        //初始化数据
        initData();
        //初始化动画
        initAnimation();
        //初始化数据库
        initDB();
    }

    private void initDB() {
        //归属地数据库拷贝
        initAddressDB("address.db");
        //常用号码数据库拷贝
        initAddressDB("commonnum.db");
    }

    /**
     * 拷贝数据库到files文件夹下
     *
     * @param dbName
     */
    private void initAddressDB(String dbName) {
        InputStream is = null;
        FileOutputStream fos = null;
        //在files文件夹下创建同名dbName数据库File
        File files = getFilesDir();
        File dbFile = new File(files, dbName);
        if (dbFile.exists()) {
            return;
        }
        //读取第三方资产目录下的文件,使用资产管理API
        AssetManager assetManager = getAssets();
        try {
            is = assetManager.open(dbName);
            //将读取的内容写到指定文件夹的文件中
            fos = new FileOutputStream(dbFile);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                fos.write(bytes, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    }

    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(3000);
        rl_root.startAnimation(alphaAnimation);
    }

    /**
     * 弹出对话框提示用户更新
     */
    protected void showUpdateDialog() {
        //对话框是依赖于activity存在的
        new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher)
                .setTitle("有新版本啦!").setMessage(mVersionDes)
                .setPositiveButton("立即更新", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //下载apk,apk链接地址
                        downloadApk();
                    }
                }).setNegativeButton("稍后下载", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消对话框跳转到主界面
                enterHome();

            }
        }).setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                //即使用户点击取消,也需要进入主界面
                enterHome();
                dialog.dismiss();
            }
        }).show();
    }

    protected void downloadApk() {
        //判断sd卡是否可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //获取sd卡路径
            String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MoblieSafer.apk";
            //发送请求,获取apk,下载
            HttpUtils httpUtils = new HttpUtils();
            //(下载地址,下载存储位置,)
            httpUtils.download(mDownloadUrl, apkPath, new RequestCallBack<File>() {

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    Log.i(tag, "success");
                    File file = responseInfo.result;
                    //安装apk
                    installApk(file);
                }

                @Override
                public void onFailure(HttpException arg0, String arg1) {
                    Log.i(tag, "failed");
                }

                //刚开始下载
                @Override
                public void onStart() {
                    Log.i(tag, "start");
                    super.onStart();
                }

                //下载过程(总大小,当前,是否正在下载)
                @Override
                public void onLoading(long total, long current,
                                      boolean isUploading) {
                    Log.i(tag, "loading" + total + "-" + current);
                    super.onLoading(total, current, isUploading);
                }
            });
        }
    }

    protected void installApk(File file) {
        //通过源码找到apk安装的入口
        /*<action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:scheme="content" />
        <data android:scheme="file" />
        <data android:mimeType="application/vnd.android.package-archive" />*/
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        /*//文件作为数据源
		intent.setData(Uri.fromFile(file));
		//设置安装类型
		intent.setType("application/vnd.android.package-archive");*/
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        //startActivity(intent);
        //处理用户点击取消安装逻辑
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //如果回来就直接进入主界面
        enterHome();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 进入应用程序主界面
     */
    protected void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //在开启以界面后将导航界面关闭
        finish();
    }

    /**
     * 获取数据 alt+shift+J
     */
    private void initData() {
        //应用版本名称赋值
        tv_version_name.setText(getVersionName());
        //检测对比本地版本号和服务器版本  提示更新下载
        //成员变量的命名规则
        //获取本地版本号
        mLocalVersionCode = getVersionCode();
        //获取服务器版本号(发送请求 服务器 响应 json xml)
		/*json 内容
		更新版本名称 版本号
		新版本描述信息
		新版本apk下载地址*/
        //json保存格式不要带BOM
        if (SpUtil.getBoolean(getApplicationContext(), ConstantValue.OPEN_UPDATE, false)) {
            checkVersion();
        } else {
            /*主界面不要睡眠
            SystemClock.sleep(4000);
            enterHome();*/
            //延时处理信息
            //mHandler.sendMessageDelayed(msg,4000);
            //直接发送状态码
            mHandler.sendEmptyMessageDelayed(ENTER_HOME, 4000);
        }
    }

    /**
     * 检测版本号
     */
    private void checkVersion() {
		/*new Thread(new Runnable() {
			@Override
			public void run() {
				OkHttpClient 
			}
		}).start();*/
        final long startTime = System.currentTimeMillis();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();
        Request.Builder builder = new Request.Builder();
        //模拟器访问电脑tomcat 谷歌预留 10.0.2.2:8080
		/*Request request = builder.url("http://10.0.2.2:8080/version.json")
				.get().build();*/
        Request request = builder.url("http://192.168.1.102:8080/version.json")
                .get().build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            Message msg = Message.obtain();


            @Override
            public void onResponse(Call arg0, Response response)
                    throws IOException {
                //获取流对象
                InputStream is = response.body().byteStream();
                //工具类将流对象转换为字符串
                String versionJson = StreamUtil.stream2String(is);
                //json解析
                try {
                    JSONObject jsonObject = new JSONObject(versionJson);
                    String versionName = jsonObject.getString("versionName");
                    mVersionDes = jsonObject.getString("versionDes");
                    String versionCode = jsonObject.getString("versionCode");
                    mDownloadUrl = jsonObject.getString("downloadUrl");

                    Log.i(tag, versionName);
                    Log.i(tag, mVersionDes);
                    Log.i(tag, versionCode);
                    Log.i(tag, mDownloadUrl);

                    //比对版本号(服务器版本号>本地,提示更新)
                    if (mLocalVersionCode < Integer.parseInt(versionCode)) {
                        //弹出对话框,提示更新,消息机制

                        msg.what = UPDATE_VERSION;
                    } else {
                        //进入应用程序主界面
                        msg.what = ENTER_HOME;
                    }
                } catch (JSONException e) {
                    msg.what = JSON_ERROR;
                    e.printStackTrace();
                } finally {
                    //指定睡眠时间,请求网络时长超过四秒则不做处理
                    //请求小于4秒则强制让其睡眠满四秒
                    long endTime = System.currentTimeMillis();
                    if (endTime - startTime < 4000) {
                        //	SystemClock.sleep(4000 - (endTime - startTime));
                    }
                    mHandler.sendMessage(msg);
                }

            }

            @Override
            public void onFailure(Call arg0, IOException exception) {
                msg.what = IO_ERROR;
                mHandler.sendMessage(msg);
            }

        });
    }

    /**
     * 查询版本号
     *
     * @return 版本号 0=失败
     */
    private int getVersionCode() {
        //获取包的管理者对象
        PackageManager pm = getPackageManager();
        //从包的管理者对象中获取指定包名的版本信息 ,传0代表获取基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(),
                    0);
            //获取对应的版本名称
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 获取版本名称
     *
     * @return 应用版本名称 ; null=异常
     */
    private String getVersionName() {
        //获取包的管理者对象
        PackageManager pm = getPackageManager();
        //从包的管理者对象中获取指定包名的版本信息 ,传0代表获取基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(this.getPackageName(),
                    0);
            //获取对应的版本名称
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化UI方法 alt+shift+J
     */
    private void initUI() {
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);

    }

}
