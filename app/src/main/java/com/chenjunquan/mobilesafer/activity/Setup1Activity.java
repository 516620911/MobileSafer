package com.chenjunquan.mobilesafer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;

import com.chenjunquan.mobilesafer.R;

/**
 * Created by 516620911 on 2017.10.21.
 */

public class Setup1Activity extends BaseSetupActivity {
   private GestureDetector gestureDetector;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup1);
    }



    public void showNextPage() {
        Intent intent = new Intent(this, Setup2Activity.class);
        startActivity(intent);
        finish();
        //开启平移动画
        overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
    }

    @Override
    protected void showPrePage() {

    }


}
