package com.niccher.mpesa_analyzer.helpers;

import com.niccher.mpesa_analyzer.models.Mod_Loot_Summary;

public class SummaryLootResponse {
    private Mod_Loot_Summary[] loot_summarizer;

    public Mod_Loot_Summary[] getLootSummarizer(){
        return loot_summarizer;
    }
}
