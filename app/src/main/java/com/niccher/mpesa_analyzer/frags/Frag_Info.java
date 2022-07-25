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
import com.niccher.mpesa_analyzer.konstants.Konstants;

import java.util.ArrayList;
import java.util.List;

public class Frag_Info extends Fragment {
    Konstants kon;

    public Frag_Info() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv= inflater.inflate(R.layout.frag_info, container, false);

        kon = new Konstants();

        final String[] option_points = {"Profile", "App Info", "App Credits"};
        final String[] option_points_description = {"Information about my account," +
                "Logging Out and Deleting m Account",
                "App Info about the version, its requirements and necessary the permissions",
                "The Libraries and other open source resources used in creating the app"};

        ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(), R.layout.part_info_item, option_points);
        //ArrayAdapter adapter_description = new ArrayAdapter<String>(getActivity(), R.layout.part_info_item, option_points_description);

        ListView option_title = (ListView) solv.findViewById(R.id.account_list);
        //ListView option_description = (ListView) solv.findViewById(R.id.account_list);

        option_title.setAdapter(adapter);
        //option_description.setAdapter(adapter_description);

        option_title.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (option_points[position] == "Profile"){
                    showBottomSheetDialog();
                }

                if (option_points[position] == "App Credits"){
                    Toast.makeText(getActivity(), "App Credits", Toast.LENGTH_SHORT).show();
                }

                if (option_points[position] == "App Info"){
                    Toast.makeText(getActivity(), "App Info", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return solv;
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    private void showBottomSheetDialog() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(R.layout.part_sheet_profile);

        Button profile_log_out, profile_delete, profile_back;
        profile_log_out = bottomSheetDialog.findViewById(R.id.bs_profile_log_out);
        profile_delete = bottomSheetDialog.findViewById(R.id.bs_profile_delete_account);
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