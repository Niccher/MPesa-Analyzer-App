package com.niccher.mpesa_analyzer.models;

public class Mod_Fone_Id {
    public String print_id, status, message, time;

    public Mod_Fone_Id(String print_id, String status, String message, String time) {
        this.print_id = print_id;
        this.status = status;
        this.message = message;
        this.time = time;
    }

    public String getPrint_id() {
        return print_id;
    }

    public void setPrint_id(String print_id) {
        this.print_id = print_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
