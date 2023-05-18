package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_more_adapter;
import com.niccher.mpesa_analyzer.models.Mod_more_info;

import java.util.ArrayList;

public class Frag_Info extends Fragment {

    Info_more_adapter info_more_adapter;
    RecyclerView recy_info;

    AppCompatActivity activity;

    public Frag_Info() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv = inflater.inflate(R.layout.frag_info, container, false);

        recy_info = solv.findViewById(R.id.info_more_Recycler);
        recy_info.setHasFixedSize(true);
        LinearLayoutManager lman = new LinearLayoutManager(solv.getContext(), LinearLayoutManager.VERTICAL, false);
        recy_info.setLayoutManager(lman);

        showInfos();

        return solv;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("More Info");
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
        setHasOptionsMenu(true);
    }

    private void showInfos() {
        ArrayList<Mod_more_info> myMoreInfo = new ArrayList<Mod_more_info>(3);
        myMoreInfo.add(new Mod_more_info("Profile", "Information about my account, Logging Out and Deleting my Account"));
        myMoreInfo.add(new Mod_more_info("App Info", "App Info about the version, its requirements and necessary the permissions"));
        myMoreInfo.add(new Mod_more_info("App Credits", "The Libraries and other open source resources used in creating the app"));

        info_more_adapter = new Info_more_adapter(myMoreInfo, getActivity());
        recy_info.setAdapter(info_more_adapter);
        info_more_adapter.notifyDataSetChanged();
    }
}