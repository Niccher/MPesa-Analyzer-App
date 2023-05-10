package com.niccher.mpesa_analyzer.models;

public class Mod_User_Auth {

    private String message, status, time, userid, uuid;

    public Mod_User_Auth(String message, String status, String time, String userid, String uuid) {
        this.message = message;
        this.status = status;
        this.time = time;
        this.userid = userid;
        this.uuid = uuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}