package com.niccher.mpesa_analyzer.models;

import android.util.Log;

public class Mod_Loot_Summary {

    public String count_Get_from_MPESA, count_Get_from_KCB, count_Get_from_Mshwari, count_Get_from_NCBA, count_Get_from_IM, count_Get_from_Reversal;
    public String count_Get_Bal_MPESA, count_Get_Bal_KCB, count_Get_Bal_Mshwari, count_Loan_Limit;
    public String count_Sent_to_MPESA, count_Sent_to_Mshwari, count_Sent_to_LNM, count_Sent_Mini, count_Sent_Cancel;
    public String count_Withdraw;
    public String count_Error_Failed, count_Error_Pin, count_Error_Less, count_Error_Receiver, count_Error_Receiver_Org;
    public String count_Fuliza_Opt_Out, count_Fuliza_Opt_In, count_Fuliza_Limit, count_Fuliza_Loan_Paid, count_Fuliza_Mini_Statement, count_Fuliza_Loan_Taken;
    public String count_Similar_Transaction, count_All, count_Unknown, loot_Created, loot_Uuid;

    public Mod_Loot_Summary(String count_Get_from_MPESA, String count_Get_from_KCB, String count_Get_from_Mshwari, String count_Get_from_NCBA, String count_Get_from_IM, String count_Get_from_Reversal, String count_Get_Bal_MPESA, String count_Get_Bal_KCB, String count_Get_Bal_Mshwari, String count_Loan_Limit, String count_Sent_to_MPESA, String count_Sent_to_Mshwari, String count_Sent_to_LNM, String count_Sent_Mini, String count_Sent_Cancel, String count_Withdraw, String count_Error_Failed, String count_Error_Pin, String count_Error_Less, String count_Error_Receiver, String count_Error_Receiver_Org, String count_Fuliza_Opt_Out, String count_Fuliza_Opt_In, String count_Fuliza_Limit, String count_Fuliza_Loan_Paid, String count_Fuliza_Mini_Statement, String count_Fuliza_Loan_Taken, String count_Similar_Transaction, String count_All, String count_Unknown, String loot_Created, String loot_Uuid) {
        this.count_Get_from_MPESA = count_Get_from_MPESA;
        this.count_Get_from_KCB = count_Get_from_KCB;
        this.count_Get_from_Mshwari = count_Get_from_Mshwari;
        this.count_Get_from_NCBA = count_Get_from_NCBA;
        this.count_Get_from_IM = count_Get_from_IM;
        this.count_Get_from_Reversal = count_Get_from_Reversal;
        this.count_Get_Bal_MPESA = count_Get_Bal_MPESA;
        this.count_Get_Bal_KCB = count_Get_Bal_KCB;
        this.count_Get_Bal_Mshwari = count_Get_Bal_Mshwari;
        this.count_Loan_Limit = count_Loan_Limit;
        this.count_Sent_to_MPESA = count_Sent_to_MPESA;
        this.count_Sent_to_Mshwari = count_Sent_to_Mshwari;
        this.count_Sent_to_LNM = count_Sent_to_LNM;
        this.count_Sent_Mini = count_Sent_Mini;
        this.count_Sent_Cancel = count_Sent_Cancel;
        this.count_Withdraw = count_Withdraw;
        this.count_Error_Failed = count_Error_Failed;
        this.count_Error_Pin = count_Error_Pin;
        this.count_Error_Less = count_Error_Less;
        this.count_Error_Receiver = count_Error_Receiver;
        this.count_Error_Receiver_Org = count_Error_Receiver_Org;
        this.count_Fuliza_Opt_Out = count_Fuliza_Opt_Out;
        this.count_Fuliza_Opt_In = count_Fuliza_Opt_In;
        this.count_Fuliza_Limit = count_Fuliza_Limit;
        this.count_Fuliza_Loan_Paid = count_Fuliza_Loan_Paid;
        this.count_Fuliza_Mini_Statement = count_Fuliza_Mini_Statement;
        this.count_Fuliza_Loan_Taken = count_Fuliza_Loan_Taken;
        this.count_Similar_Transaction = count_Similar_Transaction;
        this.count_All = count_All;
        this.count_Unknown = count_Unknown;
        this.loot_Created = loot_Created;
        this.loot_Uuid = loot_Uuid;
    }

    public String getCount_Get_from_MPESA() {
        return count_Get_from_MPESA;
    }

    public void setCount_Get_from_MPESA(String count_Get_from_MPESA) {
        this.count_Get_from_MPESA = count_Get_from_MPESA;
    }

    public String getCount_Get_from_KCB() {
        return count_Get_from_KCB;
    }

    public void setCount_Get_from_KCB(String count_Get_from_KCB) {
        this.count_Get_from_KCB = count_Get_from_KCB;
    }

    public String getCount_Get_from_Mshwari() {
        return count_Get_from_Mshwari;
    }

    public void setCount_Get_from_Mshwari(String count_Get_from_Mshwari) {
        this.count_Get_from_Mshwari = count_Get_from_Mshwari;
    }

    public String getCount_Get_from_NCBA() {
        return count_Get_from_NCBA;
    }

    public void setCount_Get_from_NCBA(String count_Get_from_NCBA) {
        this.count_Get_from_NCBA = count_Get_from_NCBA;
    }

    public String getCount_Get_from_IM() {
        return count_Get_from_IM;
    }

    public void setCount_Get_from_IM(String count_Get_from_IM) {
        this.count_Get_from_IM = count_Get_from_IM;
    }

    public String getCount_Get_from_Reversal() {
        return count_Get_from_Reversal;
    }

    public void setCount_Get_from_Reversal(String count_Get_from_Reversal) {
        this.count_Get_from_Reversal = count_Get_from_Reversal;
    }

    public String getCount_Get_Bal_MPESA() {
        return count_Get_Bal_MPESA;
    }

    public void setCount_Get_Bal_MPESA(String count_Get_Bal_MPESA) {
        this.count_Get_Bal_MPESA = count_Get_Bal_MPESA;
    }

    public String getCount_Get_Bal_KCB() {
        return count_Get_Bal_KCB;
    }

    public void setCount_Get_Bal_KCB(String count_Get_Bal_KCB) {
        this.count_Get_Bal_KCB = count_Get_Bal_KCB;
    }

    public String getCount_Get_Bal_Mshwari() {
        return count_Get_Bal_Mshwari;
    }

    public void setCount_Get_Bal_Mshwari(String count_Get_Bal_Mshwari) {
        this.count_Get_Bal_Mshwari = count_Get_Bal_Mshwari;
    }

    public String getCount_Loan_Limit() {
        return count_Loan_Limit;
    }

    public void setCount_Loan_Limit(String count_Loan_Limit) {
        this.count_Loan_Limit = count_Loan_Limit;
    }

    public String getCount_Sent_to_MPESA() {
        return count_Sent_to_MPESA;
    }

    public void setCount_Sent_to_MPESA(String count_Sent_to_MPESA) {
        this.count_Sent_to_MPESA = count_Sent_to_MPESA;
    }

    public String getCount_Sent_to_Mshwari() {
        return count_Sent_to_Mshwari;
    }

    public void setCount_Sent_to_Mshwari(String count_Sent_to_Mshwari) {
        this.count_Sent_to_Mshwari = count_Sent_to_Mshwari;
    }

    public String getCount_Sent_to_LNM() {
        return count_Sent_to_LNM;
    }

    public void setCount_Sent_to_LNM(String count_Sent_to_LNM) {
        this.count_Sent_to_LNM = count_Sent_to_LNM;
    }

    public String getCount_Sent_Mini() {
        return count_Sent_Mini;
    }

    public void setCount_Sent_Mini(String count_Sent_Mini) {
        this.count_Sent_Mini = count_Sent_Mini;
    }

    public String getCount_Sent_Cancel() {
        return count_Sent_Cancel;
    }

    public void setCount_Sent_Cancel(String count_Sent_Cancel) {
        this.count_Sent_Cancel = count_Sent_Cancel;
    }

    public String getCount_Withdraw() {
        return count_Withdraw;
    }

    public void setCount_Withdraw(String count_Withdraw) {
        this.count_Withdraw = count_Withdraw;
    }

    public String getCount_Error_Failed() {
        return count_Error_Failed;
    }

    public void setCount_Error_Failed(String count_Error_Failed) {
        this.count_Error_Failed = count_Error_Failed;
    }

    public String getCount_Error_Pin() {
        return count_Error_Pin;
    }

    public void setCount_Error_Pin(String count_Error_Pin) {
        this.count_Error_Pin = count_Error_Pin;
    }

    public String getCount_Error_Less() {
        return count_Error_Less;
    }

    public void setCount_Error_Less(String count_Error_Less) {
        this.count_Error_Less = count_Error_Less;
    }

    public String getCount_Error_Receiver() {
        return count_Error_Receiver;
    }

    public void setCount_Error_Receiver(String count_Error_Receiver) {
        this.count_Error_Receiver = count_Error_Receiver;
    }

    public String getCount_Error_Receiver_Org() {
        return count_Error_Receiver_Org;
    }

    public void setCount_Error_Receiver_Org(String count_Error_Receiver_Org) {
        this.count_Error_Receiver_Org = count_Error_Receiver_Org;
    }

    public String getCount_Fuliza_Opt_Out() {
        return count_Fuliza_Opt_Out;
    }

    public void setCount_Fuliza_Opt_Out(String count_Fuliza_Opt_Out) {
        this.count_Fuliza_Opt_Out = count_Fuliza_Opt_Out;
    }

    public String getCount_Fuliza_Opt_In() {
        return count_Fuliza_Opt_In;
    }

    public void setCount_Fuliza_Opt_In(String count_Fuliza_Opt_In) {
        this.count_Fuliza_Opt_In = count_Fuliza_Opt_In;
    }

    public String getCount_Fuliza_Limit() {
        return count_Fuliza_Limit;
    }

    public void setCount_Fuliza_Limit(String count_Fuliza_Limit) {
        this.count_Fuliza_Limit = count_Fuliza_Limit;
    }

    public String getCount_Fuliza_Loan_Paid() {
        return count_Fuliza_Loan_Paid;
    }

    public void setCount_Fuliza_Loan_Paid(String count_Fuliza_Loan_Paid) {
        this.count_Fuliza_Loan_Paid = count_Fuliza_Loan_Paid;
    }

    public String getCount_Fuliza_Mini_Statement() {
        return count_Fuliza_Mini_Statement;
    }

    public void setCount_Fuliza_Mini_Statement(String count_Fuliza_Mini_Statement) {
        this.count_Fuliza_Mini_Statement = count_Fuliza_Mini_Statement;
    }

    public String getCount_Fuliza_Loan_Taken() {
        return count_Fuliza_Loan_Taken;
    }

    public void setCount_Fuliza_Loan_Taken(String count_Fuliza_Loan_Taken) {
        this.count_Fuliza_Loan_Taken = count_Fuliza_Loan_Taken;
    }

    public String getCount_Similar_Transaction() {
        return count_Similar_Transaction;
    }

    public void setCount_Similar_Transaction(String count_Similar_Transaction) {
        this.count_Similar_Transaction = count_Similar_Transaction;
    }

    public String getCount_All() {
        return count_All;
    }

    public void setCount_All(String count_All) {
        this.count_All = count_All;
    }

    public String getCount_Unknown() {
        return count_Unknown;
    }

    public void setCount_Unknown(String count_Unknown) {
        this.count_Unknown = count_Unknown;
    }

    public String getLoot_Created() {
        return loot_Created;
    }

    public void setLoot_Created(String loot_Created) {
        this.loot_Created = loot_Created;
    }

    public String getLoot_Uuid() {
        return loot_Uuid;
    }

    public void setLoot_Uuid(String loot_Uuid) {
        this.loot_Uuid = loot_Uuid;
    }

    public String setVal_sel(String val_sel) {//Values from drop-down list.
        Log.e("Mod_Loot_Summary", "setVal_sel: " + val_sel );
        switch (val_sel) {
            case "Get from MPESA":
                return count_Get_from_MPESA;
            case "Get from KCB":
                return count_Get_from_KCB;
            case "Get from Mshwari":
                return count_Get_from_Mshwari;
            case "Get from NCBA":
                return count_Get_from_NCBA;
            case "Get from IM":
                return count_Get_from_IM;
            case "Get from Reversal":
                return count_Get_from_Reversal;

            case "Bal MPESA":
                return count_Get_Bal_MPESA;
            case "Bal KCB":
                return count_Get_Bal_KCB;
            case "Bal Mshwari":
                return count_Get_Bal_Mshwari;

            case "Loan Limit":
                return count_Loan_Limit;

            case "Sent to MPESA":
                return count_Sent_to_MPESA;
            case "Sent to Mshawri":
                return count_Sent_to_Mshwari;
            case "Sent to LNM":
                return count_Sent_to_LNM;
            case "Sent Mini Statement":
                return count_Sent_Mini;
            case "Sent Canceled":
                return count_Sent_Cancel;
            case "Withdrawals":
                return count_Withdraw;

            case "Error Failed":
                return count_Error_Failed;
            case "Error Pin":
                return count_Error_Pin;
            case "Error Less":
                return count_Error_Less;
            case "Error Receiver":
                return count_Error_Receiver;
            case "Error Receiver Org":
                return count_Error_Receiver_Org;

            case "Fuliza Leave":
                return count_Fuliza_Opt_Out;
            case "Fuliza Opt In":
                return count_Fuliza_Opt_In;
            case "Fuliza Limit":
                return count_Fuliza_Limit;
            case "Fuliza Loan Paid":
                return count_Fuliza_Loan_Paid;
            case "Fuliza Mini Statement":
                return count_Fuliza_Mini_Statement;
            case "Fuliza Loan Taken":
                return count_Fuliza_Loan_Taken;

            case "Similar Transaction":
                return count_Similar_Transaction;
            case "Unknown":
                return count_Unknown;
            default:
                return count_All;
        }
    }
}
