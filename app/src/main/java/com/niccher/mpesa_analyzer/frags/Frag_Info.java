package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_adapter;
import com.niccher.mpesa_analyzer.adapter.Info_more_adapter;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_more_info;

import java.util.ArrayList;
import java.util.List;

public class Frag_Info extends Fragment {

    Info_more_adapter info_more_adapter;
    RecyclerView recy_info;

    public Frag_Info() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv= inflater.inflate(R.layout.frag_info, container, false);

        recy_info = solv.findViewById(R.id.info_more_Recycler);
        recy_info.setHasFixedSize(true);
        LinearLayoutManager lman = new LinearLayoutManager(solv.getContext(), LinearLayoutManager.VERTICAL,false);
        recy_info.setLayoutManager(lman);

        showInfos();

        return solv;
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void showInfos() {
        ArrayList<Mod_more_info> myMoreInfo = new ArrayList<Mod_more_info>(3);
        myMoreInfo.add(new Mod_more_info("Profile","Information about my account, Logging Out and Deleting my Account"));
        myMoreInfo.add(new Mod_more_info("App Info","App Info about the version, its requirements and necessary the permissions"));
        myMoreInfo.add(new Mod_more_info("App Credits","The Libraries and other open source resources used in creating the app"));

        info_more_adapter = new Info_more_adapter(myMoreInfo, getActivity());
        recy_info.setAdapter(info_more_adapter);
        info_more_adapter.notifyDataSetChanged();
    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(R.layout.part_sheet_profile);

        Button profile_log_out, profile_delete, profile_back;
        profile_log_out = bottomSheetDialog.findViewById(R.id.btn_profile_log_out);
        profile_delete = bottomSheetDialog.findViewById(R.id.btn_profile_delete_account);
        profile_back = bottomSheetDialog.findViewById(R.id.bs_profile_back);

        bottomSheetDialog.show();

        profile_log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Sign profile_log_out", Toast.LENGTH_SHORT).show();
            }
        });

        profile_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Sign profile_delete", Toast.LENGTH_SHORT).show();
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