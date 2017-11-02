package com.chenjunquan.mobilesafer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;

/**
 * Created by 516620911 on 2017.10.20.
 */

public class SettingItemView extends RelativeLayout {
    public static  final String NAMESPACE="http://schemas.android.com/apk/res/com.chenjunquan.mobilesafer";
    private CheckBox mCk_box;
    private TextView mTv_des;
    private String tag = "SettingItemView";
    private String mDestitle;
    private String mDesoff;
    private String mDeson;

    public SettingItemView(Context context) {
        this(context,null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //取View的时候不包括 外面的布局 所以需要手动添加 把里面的控件打气到this
        View.inflate(context, R.layout.setting_item_view,this);
        //等同
        /*View inflate = View.inflate(context, R.layout.setting_item_view, null);
        this.addView(inflate);*/
        ///让自定义view可以操作这些控件
        TextView tv_title= (TextView) findViewById(R.id.tv_title);
        mTv_des = (TextView) findViewById(R.id.tv_des);
        mCk_box = (CheckBox) findViewById(R.id.ck_box);
        //获取自定义属性和原生属性的操作
        initAttrs(attrs);

        tv_title.setText(mDestitle);

        /*boolean ck_update = SpUtil.getBoolean(context, "open_update", false);
        if(ck_update) {
            mCk_box.setChecked(ck_update);
            mTv_des.setText("自动更新已开启");
        }*/
    }

    private void initAttrs(AttributeSet attrs) {
        /*Log.i(tag,attrs.getAttributeCount()+"");
        for (int i=0;i<attrs.getAttributeCount();i++){
            Log.i(tag,attrs.getAttributeName(i)+"");
            Log.i(tag,attrs.getAttributeValue(i)+"");

        }*/
        //获取属性值
        mDestitle = attrs.getAttributeValue(NAMESPACE, "destitle");
        mDesoff = attrs.getAttributeValue(NAMESPACE, "desoff");
        mDeson = attrs.getAttributeValue(NAMESPACE, "deson");
    }

    /**
     * 判断是否开启的方法
     * @return 返回当前SIV中的checkbox是否选中状态
     */
    public boolean isCheck(){
        return mCk_box.isChecked();
    }

    /**
     * @param isCheck 是否作为开启的变量 由点击过程中去做传递
     */
    public void setCheck(boolean isCheck){
        mCk_box.setChecked(isCheck);
        if(isCheck)
            mTv_des.setText(mDeson);
        else
            mTv_des.setText(mDesoff);
    }
}
