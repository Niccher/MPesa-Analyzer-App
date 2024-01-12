package com.niccher.mpesa_analyzer.models;

public class Mod_Summaries {
    //public String summary_Name,summary_Type,summary_Extension,summary_Size,summary_Owner, summary_Device, summary_Created,summary_Count,summary_Received,summary_Sent,summary_Unknown;
    public String summary_Loot_Uuid, summary_Created,summary_Count,summary_Received,summary_Sent,summary_Unknown;

    public Mod_Summaries(String summary_Loot_Uuid, String summary_Created, String summary_Count, String summary_Received, String summary_Sent, String summary_Unknown) {
        this.summary_Loot_Uuid = summary_Loot_Uuid;
        this.summary_Created = summary_Created;
        this.summary_Count = summary_Count;
        this.summary_Received = summary_Received;
        this.summary_Sent = summary_Sent;
        this.summary_Unknown = summary_Unknown;
    }

    public String getSummary_Loot_Uuid() {
        return summary_Loot_Uuid;
    }

    public void setSummary_Loot_Uuid(String summary_Loot_Uuid) {
        this.summary_Loot_Uuid = summary_Loot_Uuid;
    }

    public String getSummary_Created() {
        return summary_Created;
    }

    public void setSummary_Created(String summary_Created) {
        this.summary_Created = summary_Created;
    }

    public String getSummary_Count() {
        return summary_Count;
    }

    public void setSummary_Count(String summary_Count) {
        this.summary_Count = summary_Count;
    }

    public String getSummary_Received() {
        return summary_Received;
    }

    public void setSummary_Received(String summary_Received) {
        this.summary_Received = summary_Received;
    }

    public String getSummary_Sent() {
        return summary_Sent;
    }

    public void setSummary_Sent(String summary_Sent) {
        this.summary_Sent = summary_Sent;
    }

    public String getSummary_Unknown() {
        return summary_Unknown;
    }

    public void setSummary_Unknown(String summary_Unknown) {
        this.summary_Unknown = summary_Unknown;
    }
}
