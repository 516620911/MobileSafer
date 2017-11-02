package com.chenjunquan.mobilesafer.bean;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/10/26.
 */

public class BlackListInfo extends DataSupport{
    private int id;
    private String phone;
    private int mode;

    @Override
    public String toString() {
        return "BlackListInfo{" +
                "id=" + id +
                ", phone='" + phone + '\'' +
                ", mode=" + mode +
                '}';
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
