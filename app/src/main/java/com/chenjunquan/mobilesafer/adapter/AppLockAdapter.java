package com.chenjunquan.mobilesafer.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.activity.AppLockActivity;
import com.chenjunquan.mobilesafer.bean.AppInfo;
import com.chenjunquan.mobilesafer.engine.AppLockDao;

import java.util.ArrayList;

/**
 * Created by 516620911 on 2017.11.04.
 */

public class AppLockAdapter extends BaseAdapter {
    //区别是否加锁的界面
    private Boolean isLock;
    private ArrayList<AppInfo> mLockList;
    private ArrayList<AppInfo> mUnLockList;
    private Context mContext;
    private TranslateAnimation mTranslateAnimation;
    public AppLockAdapter(Context context,Boolean isLock, ArrayList<AppInfo> lockList, ArrayList<AppInfo> unLockList) {
        this.isLock=isLock;
        this.mLockList=lockList;
        this.mUnLockList=unLockList;
        this.mContext=context;
        initAnimation();
    }
    private void initAnimation() {
        //参照控件
        mTranslateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mTranslateAnimation.setDuration(300);
    }
    @Override
    public int getCount() {
        if(isLock){
            AppLockActivity.tv_applock_lock.setText("已加锁应用:"+mLockList.size() + "个");
            return mLockList.size();
        }else{
            AppLockActivity.tv_applock_unlock.setText("未加锁应用:"+mUnLockList.size()+"个");
            return mUnLockList.size();
        }

    }

    @Override
    public AppInfo getItem(int position) {
        if(isLock){
            return mLockList.get(position);
        }else{
            return mUnLockList.get(position);
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null){
            convertView = View.inflate(mContext, R.layout.listview_islock_item, null);
            holder = new ViewHolder();
            holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_islock_item_icon);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_islock_item_name);
            holder.iv_lock = (ImageView) convertView.findViewById(R.id.iv_islock_item_lock);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        final AppInfo appInfo = getItem(position);
        holder.iv_icon.setImageDrawable(appInfo.getIcon());
        holder.tv_name.setText(appInfo.getName());
        if(isLock) {
            holder.iv_lock.setBackgroundResource(R.drawable.lock);
        }else{
            holder.iv_lock.setBackgroundResource(R.drawable.unlock);
        }
        final View animationView=convertView;
        holder.iv_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //动画效果(应该在动画完成后再操作listview所有需要监听动画是否完全再刷新)
                animationView.startAnimation(mTranslateAnimation);
                mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if(isLock){
                            //已加锁---->未加锁
                            //操作当前集合
                            mLockList.remove(appInfo);
                            mUnLockList.add(0,appInfo);
                            //操作数据库
                            AppLockDao mAppLockDao = AppLockDao.getInstance(mContext);
                            mAppLockDao.delete(appInfo.getPackageName());
                            //刷新适配器
                            notifyDataSetChanged();
                        }else{
                            //未加锁---->已加锁
                            //操作当前集合
                            mUnLockList.remove(appInfo);
                            mLockList.add(0,appInfo);
                            //操作数据库
                            AppLockDao mAppLockDao = AppLockDao.getInstance(mContext);
                            mAppLockDao.insert(appInfo.getPackageName());
                            //刷新适配器
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

            }
        });
        return convertView;
    }
    static class ViewHolder{
        ImageView iv_icon;
        TextView tv_name;
        ImageView iv_lock;
    }
}
