package com.niccher.mpesa_analyzer.models;

public class Mod_Loot_Summary {

    public String val_all, val_balance, val_fuliza, val_received, val_sent, val_withdraw, val_wrong_pin, val_unknown, val_created;

    public Mod_Loot_Summary(String val_all, String val_balance, String val_fuliza, String val_received, String val_sent, String val_withdraw, String val_wrong_pin, String val_unknown, String val_created) {
        this.val_all = val_all;
        this.val_balance = val_balance;
        this.val_fuliza = val_fuliza;
        this.val_received = val_received;
        this.val_sent = val_sent;
        this.val_withdraw = val_withdraw;
        this.val_wrong_pin = val_wrong_pin;
        this.val_unknown = val_unknown;
        this.val_created = val_created;
    }

    public String getVal_all() {
        return val_all;
    }

    public void setVal_all(String val_all) {
        this.val_all = val_all;
    }

    public String getVal_balance() {
        return val_balance;
    }

    public void setVal_balance(String val_balance) {
        this.val_balance = val_balance;
    }

    public String getVal_fuliza() {
        return val_fuliza;
    }

    public void setVal_fuliza(String val_fuliza) {
        this.val_fuliza = val_fuliza;
    }

    public String getVal_received() {
        return val_received;
    }

    public void setVal_received(String val_received) {
        this.val_received = val_received;
    }

    public String getVal_sent() {
        return val_sent;
    }

    public void setVal_sent(String val_sent) {
        this.val_sent = val_sent;
    }

    public String getVal_withdraw() {
        return val_withdraw;
    }

    public void setVal_withdraw(String val_withdraw) {
        this.val_withdraw = val_withdraw;
    }

    public String getVal_wrong_pin() {
        return val_wrong_pin;
    }

    public void setVal_wrong_pin(String val_wrong_pin) {
        this.val_wrong_pin = val_wrong_pin;
    }

    public String getVal_unknown() {
        return val_unknown;
    }

    public void setVal_unknown(String val_unknown) {
        this.val_unknown = val_unknown;
    }

    public String getVal_created() {
        return val_created;
    }

    public void setVal_created(String val_created) {
        this.val_created = val_created;
    }
}
