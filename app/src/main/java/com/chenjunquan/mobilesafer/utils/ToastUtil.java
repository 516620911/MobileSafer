package com.chenjunquan.mobilesafer.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author Administrator
 * Toast工具类
 */
public class ToastUtil {
	
	/**
	 * @param context
	 * @param msg
	 * Toast.makeText(context, msg, 0).show();
	 */
	public static void show(Context context,String msg){
		Toast.makeText(context, msg, 0).show();
	}
}
