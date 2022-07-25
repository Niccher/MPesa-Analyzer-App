package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.konstants.Konstants;

public class Frag_Summary extends Fragment {
    Konstants kon;
    AppCompatActivity activity;

    public Frag_Summary() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Summary Info");
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View summarizer =  inflater.inflate(R.layout.frag_summary, container, false);
        kon = new Konstants();
        getReferences();
        return summarizer;
    }

    public void getReferences(){
        Bundle sent_data = this.getArguments();
        if(sent_data != null){
            Toast.makeText(activity, "getReferences: " + sent_data.get("created"), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                activity.getSupportFragmentManager().popBackStack();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}