package com.chenjunquan.mobilesafer.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by 516620911 on 2017.10.21.
 */

public class MD5Util {
    public static String encoding(String str){
        try {
            //str=str+"加盐";
            //指定加密算法类型
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            //将需要加密的字符串转换成Byte[],然后进行随机哈希
            byte[] bytes = md5.digest(str.getBytes());
            //拼接字符串
            StringBuffer sb=new StringBuffer();
            //此时byte[]才16位,要循环遍历 生成32位
            for (byte b:bytes){
                int i=b&0xff;
                //int的i需要转换成16
                String hexString=Integer.toHexString(i);
                if(hexString.length()<2){
                    hexString="0"+hexString;
                }
                sb.append(hexString);
            }
            System.out.println(sb.toString());
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }


}
