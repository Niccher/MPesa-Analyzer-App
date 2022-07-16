package com.niccher.mpesa_analyzer.models;

public class Mod_Fone_Id {
    public String print_id, status, message;

    public Mod_Fone_Id(String print_id, String status, String message) {
        this.print_id = print_id;
        this.status = status;
        this.message = message;
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
}
