package com.niccher.mpesa_analyzer.models;

public class Mod_Fone_Id {
    public String pd_id, status, message;

    public Mod_Fone_Id(String pd_id, String status, String message) {
        this.pd_id = pd_id;
        this.status = status;
        this.message = message;
    }

    public String getPd_id() {
        return pd_id;
    }

    public void setPd_id(String pd_id) {
        this.pd_id = pd_id;
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
}
