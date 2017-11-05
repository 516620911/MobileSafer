package com.chenjunquan.mobilesafer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.engine.CommonPhoneDao;

import java.util.List;

/**
 * 本界面为常用电话数据适配器
 * BaseExpandableListAdapter的实现
 * Created by 516620911 on 2017.11.04.
 */

public class CommonPhoneAdapter extends BaseExpandableListAdapter{
    private List<CommonPhoneDao.Group> mGroup;
    private Context mContext;
    public CommonPhoneAdapter(Context context,List<CommonPhoneDao.Group> group){
            this.mGroup=group;
            this.mContext=context;
    }
    @Override
    public int getGroupCount() {
        return mGroup.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroup.get(groupPosition).childList.size();
    }

    @Override
    public CommonPhoneDao.Group getGroup(int groupPosition) {
        return mGroup.get(groupPosition);
    }

    @Override
    public CommonPhoneDao.Child getChild(int groupPosition, int childPosition) {
        return mGroup.get(groupPosition).childList.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        TextView textView = new TextView(mContext);
        textView.setText("    "+getGroup(groupPosition).name);
        textView.setTextColor(Color.GREEN);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        return textView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View view;
        if(convertView==null) {
             view = View.inflate(mContext, R.layout.elv_child_item, null);

        }else{
            view=convertView;
        }
        TextView tv_name = (TextView) view.findViewById(R.id.tv_item_common_name);
        TextView tv_number = (TextView) view.findViewById(R.id.tv_item_common_phone);

        tv_name.setText(getChild(groupPosition, childPosition).name);
        tv_number.setText(getChild(groupPosition, childPosition).number);
        return view;
    }
    //孩子节点是否响应点击事件
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
