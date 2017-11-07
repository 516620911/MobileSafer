package com.chenjunquan.mobilesafer.activity;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.chenjunquan.mobilesafer.R;

/**
 * 全局左侧导航栏基类
 * 1.新建一个基类让所有activity继承
 * 2.基类的布局:导航控件为根,包裹一个FrameLayout让其它活动填充(提供一个方法)
 * 3.释放滑动事件 导航图标显示图片原色
 * 4.添加toolbar,设置导航按钮
 * 5.设置点击事件
 */
public class BaseActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private DrawerLayout drawer;
    private RelativeLayout left;
    private RelativeLayout right;
    private FrameLayout content;
    public  FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initUI();

    }

    private void initUI() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //导航图标显示图片原色
        navigationView.setItemIconTintList(null);
        //设置Toolbar为Actionbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        //给导航设置点击事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //id为菜单条目的id
                switch (item.getItemId()) {
/*                    case R.id.nav_PhoneBak:
                        //手机防盗
                        //showDialog();
                        break;*/
                    case R.id.nav_BlackList:
                        //跳转通信卫士界面
                        startActivity(new Intent(getApplicationContext(), BlackListActivity.class));
                        break;
                    case R.id.nav_AppManager:
                        //跳转到软件管理界面
                        startActivity(new Intent(getApplicationContext(), AppManagerActivity.class));
                        break;
                    case R.id.nav_ProcessManager:
                        //跳转到进程管理界面
                        startActivity(new Intent(getApplicationContext(), ProcessManagerActivity.class));
                        break;
                    case R.id.nav_Traffic:
                        //跳转到流量统计界面
                        //startActivity(new Intent(getApplicationContext(), TrafficActivity.class));
                        break;
                    case R.id.nav_KillVirus:
                        //跳转到杀毒界面
                        startActivity(new Intent(getApplicationContext(), KillVirusActivity.class));
                        break;
                    case R.id.nav_CacheClear:
                        //跳转到清理缓存界面
                        //startActivity(new Intent(getApplicationContext(),BaseCacheClearActivity.class));
                        startActivity(new Intent(getApplicationContext(), CacheClearActivity.class));
                        break;
                    case R.id.nav_ATool:
                        //跳转到高级工具列表界面
                        startActivity(new Intent(getApplicationContext(), AToolActivity.class));
                        //关闭滑动菜单
                        drawer.closeDrawers();
                        break;
                    case R.id.nav_Setting:
                        //跳转到设置列表界面
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });
        //给悬浮按钮设置点击事件
        mFab = findViewById(R.id.fab_home);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到主页
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                break;
        }
        return true;
    }

    //提供一个公共方法给子类布局填充内容
    public void initContentLayout(@LayoutRes int layoutResID) {
        //中间内容
        content = (FrameLayout) findViewById(R.id.content_drawer_layout);
        View view = getLayoutInflater().inflate(layoutResID, null);
        content.addView(view);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
