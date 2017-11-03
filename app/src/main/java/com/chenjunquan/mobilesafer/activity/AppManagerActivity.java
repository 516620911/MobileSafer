package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.AppInfo;
import com.chenjunquan.mobilesafer.engine.AppInfoProvider;
import com.chenjunquan.mobilesafer.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AppManagerActivity extends Activity implements View.OnClickListener {
    private List<AppInfo> mAppInfoList;
    private ListView lv_app_list;
    private MyAdapter mAdapter;
    private AppInfo mAppInfo;
    private List<AppInfo> mSystemList;
    private List<AppInfo> mCustomerList;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            mAdapter = new MyAdapter();
            lv_app_list.setAdapter(mAdapter);
            //刚开始加载的时候 有用户应用且控件存在则初始化为用户应用+数量
            if (tv_des != null && mCustomerList != null) {
                tv_des.setText("用户应用(" + mCustomerList.size() + ")");
            }
        }

        ;
    };

    private TextView tv_des;
    private PopupWindow mPopupWindow;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_uninstall:
                if (mAppInfo.isSystem()) {
                    //点击事件记录的被点击条目的应用信息
                    ToastUtil.show(getApplicationContext(), "系统应用无法卸载");
                } else {
                    //卸载界面
                    Intent intent = new Intent("android.intent.action.DELETE");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse("package:" + mAppInfo.getPackageName()));
                    startActivity(intent);

                }
                break;
            case R.id.tv_start:
                //从桌面启动应用
                PackageManager packageManager = getPackageManager();
                //获取包的启动意图
                Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(mAppInfo.getPackageName());
                //判断是否有启动的intent(比如系统应用)
                if (launchIntentForPackage != null) {
                    startActivity(launchIntentForPackage);
                } else {
                    ToastUtil.show(getApplicationContext(), "无法打开此应用");
                }
                break;
            case R.id.tv_share:
                //发送一条分享短信
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.putExtra(Intent.EXTRA_TEXT, "分享一个应用,应用名称为" + mAppInfo.getName());
                startActivity(intent);
                break;
        }
        //点击事件完成后取消弹窗
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
        }
    }

    class MyAdapter extends BaseAdapter {

        //获取数据适配器中条目类型的总数,修改成两种(纯文本,图片+文字)
        //有两种条目类型  一种是表头 一种是应用信息的条目
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        //指定索引指向的条目类型,条目类型状态码指定(0(复用系统),1)
        //两种情况 第一种是用户应用开头 第二种是系统应用开头
        @Override
        public int getItemViewType(int position) {
            //在显示两种不同应用类型之前应该先显示表头
            if (position == 0 || position == mCustomerList.size() + 1) {
                //返回0,代表纯文本条目的状态码
                return 0;
            } else {
                //返回1,代表图片+文本条目状态码
                return 1;
            }
        }

        //listView中添加两个描述条目
        //条目总数 多了两个表头
        @Override
        public int getCount() {
            return mCustomerList.size() + mSystemList.size() + 2;
        }

        // 返回条目对应的应用信息对象
        @Override
        public AppInfo getItem(int position) {
            //表头不返回
            if (position == 0 || position == mCustomerList.size() + 1) {
                return null;
            } else {
                //返回用户应用对应的条目
                if (position < mCustomerList.size() + 1) {
                    return mCustomerList.get(position - 1);
                } else {
                    //返回系统应用对应条目的对象
                    return mSystemList.get(position - mCustomerList.size() - 2);
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);

            if (type == 0) {
                //展示灰色纯文本条目
                ViewTitleHolder holder = null;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.listview_app_item_title, null);
                    holder = new ViewTitleHolder();
                    holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewTitleHolder) convertView.getTag();
                }
                if (position == 0) {
                    holder.tv_title.setText("用户应用(" + mCustomerList.size() + ")");
                } else {
                    holder.tv_title.setText("系统应用(" + mSystemList.size() + ")");
                }
                return convertView;
            } else {
                //展示图片+文字条目
                ViewHolder holder = null;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.listview_app_item, null);
                    holder = new ViewHolder();
                    holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
                    holder.tv_name = (TextView) convertView.findViewById(R.id.tv_app_name);
                    holder.tv_path = (TextView) convertView.findViewById(R.id.tv_app_path);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.iv_icon.setBackgroundDrawable(getItem(position).icon);
                holder.tv_name.setText(getItem(position).name);
                if (getItem(position).isSdCard) {
                    holder.tv_path.setText("sd卡应用");
                } else {
                    holder.tv_path.setText("手机应用");
                }
                return convertView;
            }
        }
    }

    static class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_path;
    }

    static class ViewTitleHolder {
        TextView tv_title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);

        initTitle();
        initList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //antivity获取焦点的时候再获取数据刷新
        //因为卸载之后回来后会再重新获取焦点
        //第一次进入activity也会获取焦点 所以把获取数据的方法放在这里
        initAppInfoList();
    }

    private void initAppInfoList() {
        new Thread() {
            public void run() {
                mAppInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                mSystemList = new ArrayList<AppInfo>();
                mCustomerList = new ArrayList<AppInfo>();
                //将系统和用户应用分别放入两个集合
                for (AppInfo appInfo : mAppInfoList) {
                    if (appInfo.isSystem) {
                        //系统应用
                        mSystemList.add(appInfo);
                    } else {
                        //用户应用
                        mCustomerList.add(appInfo);
                    }
                }
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initList() {
        lv_app_list = (ListView) findViewById(R.id.lv_app_list);
        //一个覆盖在ListView之上的TextView 通过监听判断当前第一个条目的类型 动态更改此TextView
        tv_des = (TextView) findViewById(R.id.tv_app_des);

        //ListView滚动监听
        lv_app_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            //滚动过程中调用方法
            //AbsListView中view就是listView对象
            //firstVisibleItem第一个可见条目索引值
            //visibleItemCount当前一个屏幕的可见条目数
            //totalItemCount总共条目总数
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                //非空判断
                if (mCustomerList != null && mSystemList != null) {
                    //第一个可视条目大于用户应用总数+1
                    if (firstVisibleItem >= mCustomerList.size() + 1) {
                        //滚动到了系统条目
                        tv_des.setText("系统应用(" + mSystemList.size() + ")");
                    } else {
                        //滚动到了用户应用条目
                        tv_des.setText("用户应用(" + mCustomerList.size() + ")");
                    }
                }

            }
        });
        lv_app_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            //参数view是点击条目的对象
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || position == mCustomerList.size() + 1) {
                    return;
                } else {
                    //返回用户应用对应的条目
                    if (position < mCustomerList.size() + 1) {
                        mAppInfo = mCustomerList.get(position - 1);
                    } else {
                        //返回系统应用对应条目的对象
                        mAppInfo = mSystemList.get(position - mCustomerList.size() - 2);
                    }
                    showPopupWindow(view);
                }
            }
        });
    }

    private void showPopupWindow(View view) {
        View popupView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.popupwindow_layout, null);
        TextView tv_uninstall = (TextView) popupView.findViewById(R.id.tv_uninstall);
        TextView tv_start = (TextView) popupView.findViewById(R.id.tv_start);
        TextView tv_share = (TextView) popupView.findViewById(R.id.tv_share);

        tv_uninstall.setOnClickListener(this);
        tv_start.setOnClickListener(this);
        tv_share.setOnClickListener(this);
        //设置淡入淡出动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(300);
        alphaAnimation.setFillAfter(true);

        //缩放动画(x,y由0到1从无到有,缩放的中心位置)
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(300);
        scaleAnimation.setFillAfter(true);
        //插补器(数学函数)
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);
        //只能用这个开启
        popupView.startAnimation(animationSet);
        //创建窗体对象 指定宽高
        mPopupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        //设置背景为了响应回退 但是布局有背景了 这里设置成透明
        //ColorDrawable空参构成表示透明
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        //挂载窗体(在哪个控件下面,偏移量)
        mPopupWindow.showAsDropDown(view, 500, -view.getHeight() * 5 / 4);
    }

    private void initTitle() {
        //1,获取磁盘(内存,区分于手机运行内存)可用大小,磁盘路径
        String path = Environment.getDataDirectory().getAbsolutePath();
        //2,获取sd卡可用大小,sd卡路径
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //3,获取以上两个路径下文件夹的可用大小
        String memoryAvailSpace = Formatter.formatFileSize(this, getAvailSpace(path));
        String sdMemoryAvailSpace = Formatter.formatFileSize(this, getAvailSpace(sdPath));

        TextView tv_memory = (TextView) findViewById(R.id.tv_memory);
        TextView tv_sd_memory = (TextView) findViewById(R.id.tv_sd_memory);

        tv_memory.setText("磁盘可用:" + memoryAvailSpace);
        tv_sd_memory.setText("sd卡可用:" + sdMemoryAvailSpace);
    }

    //int代表多少个G

    /**
     * 返回值结果单位为byte = 8bit,最大结果为2147483647 bytes
     *
     * @param path
     * @return 返回指定路径可用区域的byte类型值
     */
    private long getAvailSpace(String path) {
        //获取可用磁盘大小类
        StatFs statFs = new StatFs(path);
        //获取可用区块的个数
        long count = statFs.getAvailableBlocks();
        //获取区块的大小
        long size = statFs.getBlockSize();
        //区块大小*可用区块个数 == 可用空间大小
        return count * size;
        //		Integer.MAX_VALUE	代表int类型数据的最大大小
        //		0x7FFFFFFF
        //
        //		2147483647bytes/1024 =  2096128 KB
        //		2096128KB/1024 = 2047	MB
        //		2047MB = 2G
    }
}
