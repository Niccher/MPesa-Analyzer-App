package com.niccher.mpesa_analyzer.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.frags.Frag_Summary;
import com.niccher.mpesa_analyzer.models.Mod_Summaries;
import com.niccher.mpesa_analyzer.models.Mod_more_info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Info_more_adapter extends RecyclerView.Adapter<Info_more_adapter.ViewHolder> {

    ArrayList<Mod_more_info> info_list;
    Context context;

    BottomSheetDialog bottomSheetDialog;
    Button profile_log_out, profile_delete, profile_back;

    public Info_more_adapter(ArrayList<Mod_more_info> info_list, Context context) {
        this.info_list = info_list;
        this.context = context;
    }

    @NonNull
    @Override
    public Info_more_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.part_info_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);

        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.part_sheet_profile);

        profile_log_out = bottomSheetDialog.findViewById(R.id.bs_profile_log_out);
        profile_delete = bottomSheetDialog.findViewById(R.id.bs_profile_delete_account);
        profile_back = bottomSheetDialog.findViewById(R.id.bs_profile_back);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Info_more_adapter.ViewHolder holder, final int position) {
        holder.part_name.setText(info_list.get(position).getName_title());
        holder.part_desc.setText(info_list.get(position).getName_desc());

        holder.part_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info_list.get(position).getName_title().equals("Profile")){

                    bottomSheetDialog.show();

                    profile_log_out.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context, "Sign profile_log_out", Toast.LENGTH_SHORT).show();
                        }
                    });

                    profile_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context, "Sign profile_delete", Toast.LENGTH_SHORT).show();
                        }
                    });

                    profile_back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            bottomSheetDialog.cancel();
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return info_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView part_name, part_desc;
        ConstraintLayout part_body;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            part_name = itemView.findViewById(R.id.acc_item);
            part_desc = itemView.findViewById(R.id.acc_item_description);
            part_body = itemView.findViewById(R.id.acc_body);
        }
    }
}

