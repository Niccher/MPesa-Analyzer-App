package com.niccher.mpesa_analyzer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.niccher.mpesa_analyzer.R;

public class Info_adapter extends RecyclerView.Adapter<Info_adapter.ViewHolder> {

    List<String> itemList1;
    Context context;

    public Info_adapter(Context context) {
        this.context=context;
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
        holder.part_date.setText("fgfgfg");
    }

    @Override
    public int getItemCount() {
        return 10;//itemList1.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView part_date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            part_date=itemView.findViewById(R.id.part_txt_date);
        }
    }
}

