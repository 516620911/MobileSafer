package com.chenjunquan.mobilesafer.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class StreamUtil {

	/**
	 * @param is
	 * @return 流转换的字符串 null=异常
	 */
	public static String stream2String(InputStream is) {
		//读取过程中 将读取内容存入缓存中 然后一次性转换字符串返回
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//读流操作
		int len = 0;
		byte[] buffer = new byte[1024];
		try {
			while ((len = is.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			return bos.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;

	}

}
