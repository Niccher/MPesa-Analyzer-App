package com.niccher.mpesa_analyzer.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.helpers.Prefs;
import com.niccher.mpesa_analyzer.models.Mod_Summaries;

public class Info_adapter extends RecyclerView.Adapter<Info_adapter.ViewHolder> {

    ArrayList<Mod_Summaries> summariesList;
    Context context;
    Prefs prefs;

    public Info_adapter(ArrayList<Mod_Summaries> summariesList, Context context) {
        this.summariesList = summariesList;
        this.context = context;
    }

    @NonNull
    @Override
    public Info_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.part_posted,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Info_adapter.ViewHolder holder, final int position) {
        try{
            Long Timestamp = Long.parseLong(String.valueOf(summariesList.get(position).summary_Created));
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Timestamp * 1000L);
            String timestamp = DateFormat.format("MMM dd HH:mm:ss", cal).toString();
            holder.part_date.setText(timestamp);
        }catch (Exception ex){
            holder.part_date.setText(summariesList.get(position).summary_Created);
        }

        holder.part_sent.setText("Sent "+summariesList.get(position).summary_Sent);
        holder.part_unknown.setText("Unknown "+summariesList.get(position).summary_Unknown);
        holder.part_receive.setText("Received " +summariesList.get(position).summary_Received);
        holder.part_count.setText("Interactions "+summariesList.get(position).summary_Count);
    }

    @Override
    public int getItemCount() {
        return summariesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView part_date, part_sent, part_unknown, part_receive, part_count;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            part_date=itemView.findViewById(R.id.part_txt_date);
            part_sent=itemView.findViewById(R.id.part_txt_sent);
            part_unknown=itemView.findViewById(R.id.part_txt_unknown);
            part_receive=itemView.findViewById(R.id.part_txt_incoming);
            part_count=itemView.findViewById(R.id.part_txt_total);
        }
    }
}

