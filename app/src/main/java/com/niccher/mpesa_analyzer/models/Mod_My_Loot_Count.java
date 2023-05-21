package com.niccher.mpesa_analyzer.models;

public class Mod_My_Loot_Count {

    String msg_time;
    int msg_count, msg_status;

    public Mod_My_Loot_Count(String msg_time, int msg_count, int msg_status) {
        this.msg_time = msg_time;
        this.msg_count = msg_count;
        this.msg_status = msg_status;
    }

    public String getMsg_time() {
        return msg_time;
    }

    public void setMsg_time(String msg_time) {
        this.msg_time = msg_time;
    }

    public int getMsg_count() {
        return msg_count;
    }

    public void setMsg_count(int msg_count) {
        this.msg_count = msg_count;
    }

    public int getMsg_status() {
        return msg_status;
    }

    public void setMsg_status(int msg_status) {
        this.msg_status = msg_status;
    }
}