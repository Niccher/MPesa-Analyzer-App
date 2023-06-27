package com.niccher.mpesa_analyzer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.niccher.mpesa_analyzer.R;

public class Info_Sms_Spinner_adapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;

    int[] sms_cat_img;
    String[] sms_cat_name;

    public Info_Sms_Spinner_adapter(Context context, int[] sms_cat_img, String[] sms_cat_name) {
        this.context = context;
        inflater = (LayoutInflater.from(context));
        this.sms_cat_img = sms_cat_img;
        this.sms_cat_name = sms_cat_name;
    }

    @Override
    public int getCount() {
        return sms_cat_name.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.part_sms_category_spinner, null);

        TextView part_sms_cat;
        ImageView part_sms_img;

        part_sms_cat = convertView.findViewById(R.id.part_spinner_txt);
        part_sms_img = convertView.findViewById(R.id.part_spinner_img);

        part_sms_img.setImageResource(sms_cat_img[position]);
        part_sms_cat.setText(sms_cat_name[position]);

        return convertView;
    }
}

