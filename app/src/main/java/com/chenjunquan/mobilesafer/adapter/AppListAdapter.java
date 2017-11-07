package com.chenjunquan.mobilesafer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.AppInfo;

import java.util.List;

/**
 * 应用列表适配器
 * Created by Administrator on 2017/11/7.
 */

public class AppListAdapter extends BaseAdapter {

    private Context mContext;
    private List<AppInfo> mSystemList;
    private List<AppInfo> mCustomerList;

    public AppListAdapter(Context context,List<AppInfo> systemList,List<AppInfo> customerList){
        this.mContext=context;
        this.mSystemList=systemList;
        this.mCustomerList=customerList;
    }
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
                convertView = View.inflate(mContext, R.layout.listview_app_item_title, null);
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
                convertView = View.inflate(mContext, R.layout.listview_app_item, null);
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
    static class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_path;
    }

    static class ViewTitleHolder {
        TextView tv_title;
    }
}
