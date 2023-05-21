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

    public Prefs() {
        kon = new Konstants();
    }

    public void get_FileType(String fileName, String timeAt, Context cntt){
        kon = new Konstants();

        if (fileName.startsWith("sms_All_",0)){
            pref_sms = cntt.getSharedPreferences(kon.shared_last_time, MODE_PRIVATE);
            sharedEditor = pref_sms.edit();
            sharedEditor.putString("last_upload_name", fileName);
            sharedEditor.putString("last_upload_time", timeAt);
            sharedEditor.apply();
            Log.e(kon.TAGGED, "get_FileType: as SMS" );
        }

    }

    public String get_TimeStamp(Context cnt){
        kon = new Konstants();
        String timestamp = "";
        SharedPreferences pref_read = null;
        String value = "";

        pref_read = cnt.getSharedPreferences(kon.shared_last_time, MODE_PRIVATE);
        value = pref_read.getString(kon.shared_last_time, "0000000");

        try{
            Long Timestamp = Long.parseLong(value);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd HH:mm:ss");//E Day_name
            timestamp = String.valueOf(sdf.format(Timestamp));
        }catch (Exception es){
            timestamp = "Not set";
        }

        return timestamp;
    }

    public String get_prefs_auth(String ty, Context cntt){
        kon = new Konstants();
        String id = "";
        if (ty=="auth"){
            SharedPreferences pref_auth = cntt.getSharedPreferences(kon.shared_auth_login, Context.MODE_PRIVATE);
            //id = pref_auth.getString("userid", "nullable");
            id = pref_auth.getString("uuid", "nullable");
        }
        if (ty=="print"){
            SharedPreferences pref_dev_id = cntt.getSharedPreferences(kon.shared_device_id, Context.MODE_PRIVATE);
            id = pref_dev_id.getString("print_id", "nullable");
        }
        if (ty=="loot_count"){
            SharedPreferences pref_loot_count = cntt.getSharedPreferences(kon.shared_loot_count, Context.MODE_PRIVATE);
            id = String.valueOf(pref_loot_count.getInt("loots", 0));
        }
        return id;
    }

}
