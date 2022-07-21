package com.niccher.mpesa_analyzer.helpers;

import com.niccher.mpesa_analyzer.models.Mod_Summaries;

public class SummaryResponse {
    private Mod_Summaries[] summarizer;

    public Mod_Summaries[] getSummarizer(){
        return summarizer;
    }
}
