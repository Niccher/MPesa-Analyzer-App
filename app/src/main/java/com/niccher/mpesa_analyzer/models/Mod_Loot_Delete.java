package com.niccher.mpesa_analyzer.models;

public class Mod_Loot_Delete {
    public String summary_Loot_Uuid, summary_Status, summary_Time;

    public Mod_Loot_Delete(String summary_Loot_Uuid, String summary_Status, String summary_Time) {
        this.summary_Loot_Uuid = summary_Loot_Uuid;
        this.summary_Status = summary_Status;
        this.summary_Time = summary_Time;
    }

    public String getSummary_Loot_Uuid() {
        return summary_Loot_Uuid;
    }

    public void setSummary_Loot_Uuid(String summary_Loot_Uuid) {
        this.summary_Loot_Uuid = summary_Loot_Uuid;
    }

    public String getSummary_Status() {
        return summary_Status;
    }

    public void setSummary_Status(String summary_Status) {
        this.summary_Status = summary_Status;
    }

    public String getSummary_Time() {
        return summary_Time;
    }

    public void setSummary_Time(String summary_Time) {
        this.summary_Time = summary_Time;
    }
}
