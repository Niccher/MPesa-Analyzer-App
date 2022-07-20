package com.niccher.mpesa_analyzer.models;

public class Mod_Summaries {
    public String summary_Name,summary_Type,summary_Extension,summary_Size,summary_Owner, summary_Device;
    public String summary_Created,summary_Count,summary_Received,summary_Sent,summary_Unknown;

    public Mod_Summaries(String summary_Name, String summary_Type, String summary_Extension, String summary_Size, String summary_Owner, String summary_Device, String summary_Created, String summary_Count, String summary_Received, String summary_Sent, String summary_Unknown) {
        this.summary_Name = summary_Name;
        this.summary_Type = summary_Type;
        this.summary_Extension = summary_Extension;
        this.summary_Size = summary_Size;
        this.summary_Owner = summary_Owner;
        this.summary_Device = summary_Device;
        this.summary_Created = summary_Created;
        this.summary_Count = summary_Count;
        this.summary_Received = summary_Received;
        this.summary_Sent = summary_Sent;
        this.summary_Unknown = summary_Unknown;
    }

    public String getSummary_Name() {
        return summary_Name;
    }

    public void setSummary_Name(String summary_Name) {
        this.summary_Name = summary_Name;
    }

    public String getSummary_Type() {
        return summary_Type;
    }

    public void setSummary_Type(String summary_Type) {
        this.summary_Type = summary_Type;
    }

    public String getSummary_Extension() {
        return summary_Extension;
    }

    public void setSummary_Extension(String summary_Extension) {
        this.summary_Extension = summary_Extension;
    }

    public String getSummary_Size() {
        return summary_Size;
    }

    public void setSummary_Size(String summary_Size) {
        this.summary_Size = summary_Size;
    }

    public String getSummary_Owner() {
        return summary_Owner;
    }

    public void setSummary_Owner(String summary_Owner) {
        this.summary_Owner = summary_Owner;
    }

    public String getSummary_Device() {
        return summary_Device;
    }

    public void setSummary_Device(String summary_Device) {
        this.summary_Device = summary_Device;
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
