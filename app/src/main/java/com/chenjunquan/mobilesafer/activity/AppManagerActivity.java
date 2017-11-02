package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.AppInfo;
import com.chenjunquan.mobilesafer.engine.AppInfoProvider;

import java.util.ArrayList;
import java.util.List;

public class AppManagerActivity extends Activity {
    private List<AppInfo> mAppInfoList;

    private ListView lv_app_list;
    private MyAdapter mAdapter;

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

    private void initList() {
        lv_app_list = (ListView) findViewById(R.id.lv_app_list);
        //一个覆盖在ListView之上的TextView 通过监听判断当前第一个条目的类型 动态更改此TextView
        tv_des = (TextView) findViewById(R.id.tv_app_des);

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

            ;
        }.start();
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
				if(mCustomerList!=null && mSystemList!=null){
				    //第一个可视条目大于用户应用总数+1
					if(firstVisibleItem>=mCustomerList.size()+1){
						//滚动到了系统条目
						tv_des.setText("系统应用("+mSystemList.size()+")");
					}else{
						//滚动到了用户应用条目
						tv_des.setText("用户应用("+mCustomerList.size()+")");
					}
				}
				
			}
		});
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
