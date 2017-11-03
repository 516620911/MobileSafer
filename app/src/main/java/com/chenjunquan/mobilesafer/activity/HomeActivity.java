package com.chenjunquan.mobilesafer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.utils.ConstantValue;
import com.chenjunquan.mobilesafer.utils.MD5Util;
import com.chenjunquan.mobilesafer.utils.SpUtil;
import com.chenjunquan.mobilesafer.utils.ToastUtil;

public class HomeActivity extends Activity {
    private GridView gv_home;
    private String[] mTitleStrs;
    private int[] mDrawableIds;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initUI();
        initData();
        
    }

    private void initData() {
        mTitleStrs = new String[]{
                "手机防盗", "通信卫士", "软件管理", "进程管理", "流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心"
        };

        mDrawableIds = new int[]{
                R.drawable.home_safe, R.drawable.home_callmsgsafe,
                R.drawable.home_apps, R.drawable.home_taskmanager,
                R.drawable.home_netmanager, R.drawable.home_trojan,
                R.drawable.home_sysoptimize, R.drawable.home_tools, R.drawable.home_settings
        };
        //九宫格控件设置数据适配器
        gv_home.setAdapter(new MyAdapter());
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        showDialog();
                        break;
                    case 1:
                        //跳转到黑名单列表界面
                        startActivity(new Intent(getApplicationContext(),BlackListActivity.class));
                        break;
                    case 2:
                        //跳转到黑名单列表界面
                        startActivity(new Intent(getApplicationContext(),AppManagerActivity.class));
                        break;
                    case 3:
                        //跳转到黑名单列表界面
                        startActivity(new Intent(getApplicationContext(),ProcessManagerActivity.class));
                        break;
                    case 7:
                        //跳转到高级工具列表界面
                        startActivity(new Intent(getApplicationContext(),AToolActivity.class));
                        break;
                    case 8:
                        //跳转到设置列表界面
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    private void showDialog() {
        String psd = SpUtil.getString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, "");
        if (TextUtils.isEmpty(psd)) {
            //初始设置密码对话框
            showSetPsdDialog();
        } else {
            //确认密码
            showConfirmPsdDialog();
        }
    }


    /**
     * 初始设置密码对话框
     */
    private void showSetPsdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //自定义对话框
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this, R.layout.dialog_set_psd, null);
        //dialog.setView(view);
        //适配老版本 内边距
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_set_psd = (EditText) view.findViewById(R.id.et_set_psd);
                EditText et_confirm_psd = (EditText) view.findViewById(R.id.et_confirm_psd);

                String confirmPsd = et_confirm_psd.getText().toString();
                String psd = et_set_psd.getText().toString();
                if (!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(confirmPsd)) {
                    if (psd.equals(confirmPsd)) {
                        //进去防盗模块
                        //Intent intent = new Intent(HomeActivity.this, TestActivity.class);
                        Intent intent = new Intent(HomeActivity.this, SetupOverActivity.class);
                        startActivity(intent);
                        //点击返回需要隐藏对话框
                        dialog.dismiss();
                        SpUtil.putString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, MD5Util.encoding(psd));
                    } else {
                        ToastUtil.show(getApplicationContext(), "两次密码不一致");
                    }
                } else {
                    //提示用户密码输入为空
                    ToastUtil.show(getApplicationContext(), "密码输入为空");
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 已有密码输入确认
     */
    private void showConfirmPsdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //自定义对话框
        final AlertDialog dialog = builder.create();
        final View view = View.inflate(this, R.layout.dialog_confirm_psd, null);
        dialog.setView(view);
        dialog.show();

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_confirm_psd = (EditText) view.findViewById(R.id.et_confirm_psd);
                String confirmPsd = MD5Util.encoding(et_confirm_psd.getText().toString());
                if (!TextUtils.isEmpty(confirmPsd)) {
                    String psd = SpUtil.getString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, "");
                    if (psd.equals(confirmPsd)) {
                        //进去防盗模块
                       // Intent intent = new Intent(HomeActivity.this, TestActivity.class);
                        Intent intent = new Intent(HomeActivity.this, SetupOverActivity.class);
                        startActivity(intent);
                        //点击返回需要隐藏对话框
                        dialog.dismiss();
                    } else {
                        ToastUtil.show(getApplicationContext(), "密码错误");
                    }
                } else {
                    //提示用户密码输入为空
                    ToastUtil.show(getApplicationContext(), "密码输入为空");
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void initUI() {
        gv_home = (GridView) findViewById(R.id.gv_home);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTitleStrs.length;
        }

        @Override
        public Object getItem(int i) {
            return mTitleStrs[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View view = View.inflate(getApplicationContext(), R.layout.gridview_item, null);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            tv_title.setText(mTitleStrs[i]);
            iv_icon.setImageResource(mDrawableIds[i]);
            return view;
        }
    }

}
