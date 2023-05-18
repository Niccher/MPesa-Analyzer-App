package com.niccher.mpesa_analyzer.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_more_info;

import java.util.ArrayList;

public class Info_more_adapter extends RecyclerView.Adapter<Info_more_adapter.ViewHolder> {

    ArrayList<Mod_more_info> info_list;
    Context context;
    Konstants kon;

    BottomSheetDialog bottomSheetDialog;
    CardView card_log_out, card_delete_account, card_back;
    TextView my_name, my_email, my_logged_time;

    public Info_more_adapter(ArrayList<Mod_more_info> info_list, Context context) {
        this.info_list = info_list;
        this.context = context;
    }

    public String get_prefs_user_data(String field) {
        kon = new Konstants();
        String data = null, data1 = null;
        SharedPreferences pref_dev_id = this.context.getSharedPreferences(kon.shared_auth_login, Context.MODE_PRIVATE);
        data =  pref_dev_id.getString(field, "nullable");
        data1 = String.valueOf(data.charAt(0)).toUpperCase() + data.substring(1, data.length());
        return data1;
    }

    @NonNull
    @Override
    public Info_more_adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.part_info_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(R.layout.part_sheet_profile);

        card_log_out = bottomSheetDialog.findViewById(R.id.card_logout);
        card_delete_account = bottomSheetDialog.findViewById(R.id.card_del);
        card_back = bottomSheetDialog.findViewById(R.id.card_back);

        my_name = bottomSheetDialog.findViewById(R.id.user_profile_name);
        my_email = bottomSheetDialog.findViewById(R.id.user_profile_email);
        my_logged_time = bottomSheetDialog.findViewById(R.id.user_profile_time);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Info_more_adapter.ViewHolder holder, final int position) {
        holder.part_name.setText(info_list.get(position).getName_title());
        holder.part_desc.setText(info_list.get(position).getName_desc());

        my_name.setText(get_prefs_user_data("user_email"));
        my_email.setText(get_prefs_user_data("user_name"));
        my_logged_time.setText("Logged in at  "+  get_prefs_user_data("time"));

        holder.part_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info_list.get(position).getName_title().equals("Profile")) {

                    bottomSheetDialog.show();

                    card_log_out.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context, "Sign profile_log_out", Toast.LENGTH_SHORT).show();
                        }
                    });

                    card_delete_account.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(context, "Sign profile_delete", Toast.LENGTH_SHORT).show();
                        }
                    });

                    card_back.setOnClickListener(new View.OnClickListener() {
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

