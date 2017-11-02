package com.chenjunquan.mobilesafer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;

/**
 * Created by 516620911 on 2017.10.20.
 */

public class SettingClickView extends RelativeLayout {


    private TextView tv_des;
    private TextView tv_title;


    public SettingClickView(Context context) {
        this(context,null);
    }

    public SettingClickView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public SettingClickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //取View的时候不包括 外面的布局 所以需要手动添加 把里面的控件打气到this
        View.inflate(context, R.layout.setting_click_view,this);
        //等同
        /*View inflate = View.inflate(context, R.layout.setting_item_view, null);
        this.addView(inflate);*/
        ///让自定义view可以操作这些控件
        tv_title= (TextView) findViewById(R.id.tv_title);
        tv_des = (TextView) findViewById(R.id.tv_des);

    }
    public void setTitle(String title){
        tv_title.setText(title);
    }
    public void setDes(String Des){
        tv_des.setText(Des);
    }


}
