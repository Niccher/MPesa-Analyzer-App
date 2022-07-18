package com.niccher.mpesa_analyzer.helpers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.niccher.mpesa_analyzer.konstants.Konstants;

import java.text.SimpleDateFormat;

public class Prefs {

    Konstants kon;

    SharedPreferences pref_sms = null;

    SharedPreferences.Editor sharedEditor = null;

    public void get_FileType(String fileName, String timeAt, Context cntt){
        kon = new Konstants();

        if (fileName.startsWith("sms_All_",0)){
            pref_sms = cntt.getSharedPreferences(kon.string_upload_sms, MODE_PRIVATE);
            sharedEditor = pref_sms.edit();
            sharedEditor.putString("last_upload_name", fileName);
            sharedEditor.putString("last_upload_time", timeAt);
            sharedEditor.apply();
            Log.e(kon.TAGGED, "get_FileType: as SMS" );
        }

    }

    public String get_TimeStamp(String category, Context cnt){
        kon = new Konstants();
        String timestamp = "";
        SharedPreferences pref_read = null;
        String value = "";

        if (category.startsWith("SMS",0)){
            pref_read = cnt.getSharedPreferences(kon.string_upload_sms, MODE_PRIVATE);
            value = pref_read.getString(kon.shared_last_time, "0000000");
        }

        try{
            Long Timestamp = Long.parseLong(value);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss");//E Day_name
            timestamp = String.valueOf(sdf.format(Timestamp));
        }catch (Exception es){
            timestamp = "Not set";
        }

        return timestamp;
    }

}
