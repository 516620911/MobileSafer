package com.chenjunquan.mobilesafer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.ScanInfo;
import com.chenjunquan.mobilesafer.engine.VirusDao;
import com.chenjunquan.mobilesafer.utils.MD5Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/11/5.
 */

public class KillVirusActivity extends BaseActivity {
    private static final int SCANING = 100;
    private static final int SCAN_FINISH = 200;
    @Bind(R.id.iv_virus_scanning)
    ImageView mVirusScanning;
    @Bind(R.id.tv_virus_name)
    TextView mTvVirusName;
    @Bind(R.id.pb_virus_bar)
    ProgressBar mPbVirusBar;
    @Bind(R.id.ll_virus_add)
    LinearLayout mLlVirusAdd;
    private int index;
    @SuppressLint("HandlerLeak")
    private Handler mHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case SCANING:
                        //显示正在扫描应用名称
                        ScanInfo info=(ScanInfo)msg.obj;
                        mTvVirusName.setText(info.name);
                        //在线性布局中添加一个正在扫描应用的TextView
                        TextView textView = new TextView(getApplicationContext());
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,17);
                        if(info.isVirus){
                            //病毒
                            textView.setTextColor(Color.RED);
                            textView.setText("发现病毒:"+info.name);
                        }else{
                            //非病毒
                            textView.setTextColor(Color.BLACK);
                            textView.setText("扫描安全:"+info.name);
                        }
                        mLlVirusAdd.addView(textView,0);
                        break;
                    case SCAN_FINISH:
                        //扫描完成停止动画
                        mTvVirusName.setText("扫描完成");
                        mVirusScanning.clearAnimation();
                        //告知用户卸载病毒应用
                        for (ScanInfo scanVirusInfo : mScanVirusInfos) {
                            Intent intent = new Intent("android.intent.action.DELETE");
                            intent.addCategory("android.intent.category.DEFAULT");
                            intent.setData(Uri.parse("package:"+scanVirusInfo.packageName));
                            startActivity(intent);
                        }
                    break;
                }
                super.handleMessage(msg);
            }
        };
    private ArrayList<ScanInfo> mScanVirusInfos;
    private List<String> mVirusList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentLayout(R.layout.activity_killvirus);
        ButterKnife.bind(this);
        initUI();
        initAnimation();
        //查找病毒
        checkVirus();
    }

    /**
     * 创建子线程扫描病毒
     * 获取所有应用程序的签名文件集合(MD5)
     * 和病毒库进行比对(判断病毒库是否包含此MD5)
     * 将扫描出来的病毒封装到集合中
     */
    private void checkVirus() {
        mVirusList = VirusDao.getVirusList();
        for (String s : mVirusList) {
            Log.i("virusList",s);
        }
        new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //获取所有应用的签名文件
                        PackageManager pm = getPackageManager();
                        //获取所有应用程序的签名文件集合
                        List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_SIGNATURES + PackageManager.GET_UNINSTALLED_PACKAGES);
                        //创建扫描结果集合
                        ArrayList<ScanInfo> scanInfos = new ArrayList<>();
                        //创建扫描到的带病毒的应用集合
                        mScanVirusInfos = new ArrayList<>();
                        //进度条最大值
                        mPbVirusBar.setMax(packageInfoList.size());
                        for (PackageInfo packageInfo : packageInfoList) {
                            ScanInfo scanInfo = new ScanInfo();
                            //获取签名文件的数组
                            Signature[] signatures = packageInfo.signatures;
                            //获取签名文件数组第一位 然后进行md5 对比病毒库中的md5
                            Signature signature = signatures[0];
                            String string = signature.toCharsString();
                            //计算md5
                            String signatureMD5 = MD5Util.encoding(string);
                            Log.i(packageInfo.packageName,signatureMD5);
                            //对比是否为病毒
                            if(mVirusList.contains(signatureMD5)){
                                //记录病毒
                                scanInfo.isVirus=true;
                                mScanVirusInfos.add(scanInfo);
                            }else{
                                scanInfo.isVirus=false;
                            }
                            //维护对象的包名和应用名
                            scanInfo.packageName=packageInfo.packageName;
                            scanInfo.name=packageInfo.applicationInfo.loadLabel(pm).toString();
                            scanInfos.add(scanInfo);
                            //扫描过程中 更新进度条
                            index++;
                            mPbVirusBar.setProgress(index);
                            //handler发送消息通知更新UI
                            Message message = Message.obtain();
                            message.what=SCANING;
                            message.obj=scanInfo;
                            mHandler.sendMessage(message);

                            //慢一点
                            SystemClock.sleep(50+ new Random().nextInt(100));
                        }
                        //扫描完成消息
                        Message message = Message.obtain();
                        message.what=SCAN_FINISH;
                        mHandler.sendMessage(message);
                    }
                }).start();



    }

    private void initAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(500);
        //指定无限循环
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        //rotateAnimation.setRepeatMode(Animation.INFINITE);
        mVirusScanning.startAnimation(rotateAnimation);

    }

    private void initUI() {

    }
}
