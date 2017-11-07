package com.chenjunquan.mobilesafer.adapter;

import android.content.Context;
import android.os.Build;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.ProcessInfo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/3.
 */

public class ProcessAdapter  extends BaseAdapter{
    private ArrayList<ProcessInfo> mUserProcessList;
    private ArrayList<ProcessInfo> mSystemProcessList;
    private Context mContext;
    public ProcessAdapter(Context context,ArrayList<ProcessInfo> userProcessList, ArrayList<ProcessInfo> systemProcessList){
            this.mSystemProcessList=systemProcessList;
            this.mUserProcessList=userProcessList;
            this.mContext=context;
    }
    @Override
    public int getCount() {
        return mSystemProcessList.size()+mUserProcessList.size()+2;
    }

    @Override
    public ProcessInfo getItem(int position) {
        if(position==0||position==mUserProcessList.size()+1){
            return null;
        }else{
            if(position<mUserProcessList.size()+1){
                return mUserProcessList.get(position-1);
            }else{
                return mSystemProcessList.get(position-mUserProcessList.size()-2);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0||position==mUserProcessList.size()+1){
            //表头
            return 0;
        }else{
            return 1;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        //两种类型条目
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemViewType = getItemViewType(position);
        if(itemViewType==0){
            //表头
            ViewTitleHolder holder=null;
            if(convertView==null){
                convertView = View.inflate(mContext, R.layout.listview_app_item_title, null);
                holder = new ViewTitleHolder();
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                convertView.setTag(holder);
            }else{
                holder= (ViewTitleHolder) convertView.getTag();
            }
            if(position==0){
                //用户进程
                holder.tv_title.setText("用户进程("+mUserProcessList.size()+")");
            }else{
                holder.tv_title.setText("系统进程("+mSystemProcessList.size()+")");
            }
        }else{
            ViewHolder holder = null;
            if(convertView==null){
                convertView = View.inflate(mContext, R.layout.listview_process_item, null);
                holder = new ViewHolder();
                holder.iv_icon = (ImageView)convertView.findViewById(R.id.iv_icon);
                holder.tv_name = (TextView)convertView.findViewById(R.id.tv_name);
                holder.tv_memory_info = (TextView) convertView.findViewById(R.id.tv_memory_info);
                holder.cb_box = (CheckBox) convertView.findViewById(R.id.cb_box);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            //可以根据position获取不同类型的进程信息
            ProcessInfo processInfo = getItem(position);
            holder.tv_name.setText(processInfo.getName());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.iv_icon.setBackground(processInfo.getIcon());
            }else{
                holder.iv_icon.setBackgroundDrawable(processInfo.getIcon());
            }
            //占用内存(把Byte转成MB)
            String strSize = Formatter.formatFileSize(mContext, processInfo.memeSize);
            holder.tv_memory_info.setText(strSize);
            //由于当前不能被选中 所以屏蔽当前线程的checkbox
            if(processInfo.packageName.equals(mContext.getPackageName())){
                holder.cb_box.setVisibility(View.GONE);
            }else{
                holder.cb_box.setVisibility(View.VISIBLE);
            }
            holder.cb_box.setChecked(processInfo.isCheck);
        }
        return convertView;

    }
    static class ViewHolder{
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_memory_info;
        CheckBox cb_box;
    }

    static class ViewTitleHolder{
        TextView tv_title;
    }
}
