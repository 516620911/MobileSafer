package com.chenjunquan.mobilesafer.activity;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chenjunquan.mobilesafer.R;
import com.chenjunquan.mobilesafer.bean.BlackListInfo;

import org.litepal.crud.DataSupport;

import java.util.List;

/**
 * RecyclerView加载更多
 * Created by Administrator on 2017/10/26.
 */

public class BlackListActivity extends BaseActivity {
    private int mode;
    private TextView et_blacknum;
    private static List<BlackListInfo> mBlackListInfos;
    private BlackListAdapter mBlackListAdapter;
    private int total;//数据库中黑名单个数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentLayout(R.layout.activity_blacklist);
        initData();
        initUI();
    }

    private void initData() {
        for (int i = 0; i < 50; i++) {
            BlackListInfo blackListInfo = new BlackListInfo();
            blackListInfo.setPhone("" + i);
            blackListInfo.setMode(1);
            blackListInfo.save();
        }

        Cursor cursor = DataSupport.findBySQL("select count(*) from BlackListInfo");
        if (cursor.moveToNext()) {
            total = cursor.getInt(0);
            Log.i("total", total + "");
        }
        //mBlackListInfos = DataSupport.findAll(BlackListInfo.class);
        mBlackListInfos = DataSupport.limit(20).find(BlackListInfo.class);
    }

    private void initUI() {
        /*Button bt_add = (Button) findViewById(R.id.bt_add);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = 1;
                showDialog();
            }
        });*/
        RecyclerView rcv_blacklist = (RecyclerView) findViewById(R.id.rcv_blacklist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //linearLayoutManager.setOrientation(LinearLayout.VERTICAL);
        rcv_blacklist.setLayoutManager(linearLayoutManager);
        mBlackListAdapter = new BlackListAdapter(mBlackListInfos);
        rcv_blacklist.setAdapter(mBlackListAdapter);
        mFab.setImageResource(R.drawable.ic_launcher);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = 1;
                showDialog();
            }
        });
    }

    public class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ViewHolder> {
        //两个final int类型表示ViewType的两种类型
        private final int NORMAL_TYPE = 0;
        private final int FOOT_TYPE = 1111;
        private int current_max_count = 20;//当前最大显示数
        private Boolean isFootView = false;//是否添加了FootView
        private String footViewText = "正在加载";//FootView的内容
        private List<BlackListInfo> BlackListInfos;

        public BlackListAdapter(List<BlackListInfo> mBlackListInfos) {
            this.BlackListInfos = mBlackListInfos;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_blacklist_phone;
            TextView tv_blacklist_mode;
            ImageView iv_blacklist_delete;
            TextView tvFootView;
            ProgressBar pb_more;

            public ViewHolder(View itemView, int viewType) {
                super(itemView);
                //注意这里是view.
                if (viewType == NORMAL_TYPE) {
                    tv_blacklist_phone = (TextView) itemView.findViewById(R.id.tv_blacklist_phone);
                    tv_blacklist_mode = (TextView) itemView.findViewById(R.id.tv_blacklist_mode);
                    iv_blacklist_delete = (ImageView) itemView.findViewById(R.id.iv_blacklist_delete);
                } else if (viewType == FOOT_TYPE) {
                    pb_more = (ProgressBar) itemView.findViewById(R.id.pb_more);
                    tvFootView = (TextView) itemView.findViewById(R.id.tv_foot);
                }
            }
        }

        @Override
        public BlackListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View normal_views = View.inflate(parent.getContext(), R.layout.listview_item_blacklist, null);
            View foot_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_more, parent, false);

            if (viewType == FOOT_TYPE)
                return new ViewHolder(foot_view, FOOT_TYPE);
            final ViewHolder normal_viewHolder = new ViewHolder(normal_views, NORMAL_TYPE);
            normal_viewHolder.iv_blacklist_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = normal_viewHolder.getAdapterPosition();
                    //删除数据（删除本地list可以减少数据库访问操作）
                    String phone = normal_viewHolder.tv_blacklist_phone.getText().toString();
                    String mode = normal_viewHolder.tv_blacklist_mode.getText().toString();
                    DataSupport.deleteAll(BlackListInfo.class, "phone=?", phone);
                    mBlackListInfos.remove(position);
                    current_max_count = current_max_count - 1;
                    notifyDataSetChanged();
                    //此方法能解决条目删除的position问题?
                    notifyItemRemoved(position);
                }
            });
            return normal_viewHolder;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == current_max_count) {
                isFootView = true;
                return FOOT_TYPE;
            }
            return NORMAL_TYPE;

        }

        private String getModeString(int mode) {
            String modeString = null;
            switch (mode) {
                case 1:
                    //拦截短信
                    modeString = "拦截短信";
                    break;
                case 2:
                    //拦截电话
                    modeString = "拦截电话";
                    break;
                case 3:
                    //拦截所有
                    modeString = "拦截所有";
                    break;
                default:
                    modeString = "未知类型";
                    break;
            }
            return modeString;
        }

        @Override
        public void onBindViewHolder(BlackListAdapter.ViewHolder holder, int position) {

            //建立起ViewHolder中试图与数据的关联
            Log.d("onBindViewHolder", position + "-" + getItemViewType(position));
            //如果footview存在，并且当前位置ViewType是FOOT_TYPE
            if (isFootView && (getItemViewType(position) == FOOT_TYPE)) {
                holder.tvFootView.setText("正在加载");
                List<BlackListInfo> more = DataSupport.limit(20).offset(BlackListInfos.size()).find(BlackListInfo.class);
                Log.i("more", more.toString());
                BlackListInfos.addAll(more);
                isFootView = false;
                //最大显示条目数=当前集合大小
                current_max_count = BlackListInfos.size();
                if (position == total) {
                    holder.pb_more.setVisibility(View.GONE);
                    holder.tvFootView.setText("没有了");
                    return;
                }
                // 刷新太快 所以使用Hanlder延迟两秒
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                }, 1000);


            } else {
                BlackListInfo blackListInfo = BlackListInfos.get(position);
                if (blackListInfo != null) {
                    holder.tv_blacklist_phone.setText(blackListInfo.getPhone());
                    holder.tv_blacklist_mode.setText(getModeString(blackListInfo.getMode()));
                }
            }
        }

        @Override
        public int getItemCount() {
            Log.i("getItemCount", BlackListInfos.size() + "");
            if (BlackListInfos.size() < current_max_count) {
                return BlackListInfos.size() + 1;
            }
            return current_max_count + 1;
        }

        //创建一个方法来设置footView中的文字
/*        public void setFootViewText(String footViewText) {
            isFootView = true;
            this.footViewText = footViewText;
        }*/
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog alertDialog = builder.create();
        View view = View.inflate(getApplicationContext(), R.layout.dialog_add_blacklist, null);
        alertDialog.setView(view);
        alertDialog.show();
        et_blacknum = (TextView) view.findViewById(R.id.et_blacknum);
        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);
        RadioGroup rg_group = (RadioGroup) view.findViewById(R.id.rg_group);
        rg_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                switch (checkedId) {
                    case R.id.rb_sms1:
                        //拦截短信
                        mode = 1;
                        break;
                    case R.id.rb_phone:
                        //拦截电话
                        mode = 2;
                        break;
                    case R.id.rb_all:
                        //拦截所有
                        mode = 3;
                        break;
                }

            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phone = et_blacknum.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    BlackListInfo blackListInfo = new BlackListInfo();
                    blackListInfo.setMode(mode);
                    blackListInfo.setPhone(phone);
                    blackListInfo.save();
                    mBlackListInfos.add(blackListInfo);
                    mBlackListAdapter.notifyDataSetChanged();
                }
                alertDialog.dismiss();
            }
        });

    }

    /**
     * 每次查询20条
     *
     * @param index
     */
    public List<BlackListInfo> find(int index) {
        List<BlackListInfo> blackListInfos = DataSupport.limit(20).offset(index).find(BlackListInfo.class);
        return blackListInfos;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "onDestroy");
        DataSupport.deleteAll(BlackListInfo.class);
    }
}
