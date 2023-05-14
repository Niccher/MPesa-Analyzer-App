package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_adapter;
import com.niccher.mpesa_analyzer.helpers.Prefs;
import com.niccher.mpesa_analyzer.helpers.ServiceGenerator;
import com.niccher.mpesa_analyzer.helpers.SummaryResponse;
import com.niccher.mpesa_analyzer.interfaces.JsonProcesses;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_Summaries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Frag_Summary extends Fragment {
    Konstants kon;
    AppCompatActivity activity;

    TextView tv_recv, tv_sent, tv_unknown;

    JsonProcesses jsonProcesses;
    Gson gson = null;
    ArrayList<Mod_Summaries> summariesList;
    Info_adapter summariesAdapter;

    Prefs pref;

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

        tv_recv = summarizer.findViewById(R.id.cat_point_received);
        tv_sent = summarizer.findViewById(R.id.cat_point_sent);
        tv_unknown = summarizer.findViewById(R.id.cat_point_unknown);

        getReferences();
        return summarizer;
    }

    public void getReferences(){
        Bundle sent_data = this.getArguments();
        String st_created, st_sent, st_received, st_unknown;
        if(sent_data != null){
            //Toast.makeText(activity, "getReferences: " + sent_data.get("created"), Toast.LENGTH_SHORT).show();
            st_created = sent_data.get("created").toString();
            st_sent = sent_data.get("sent").toString();
            st_received = sent_data.get("received").toString();
            st_unknown = sent_data.get("unknown").toString();

            tv_recv.setText(st_received);
            tv_sent.setText(st_sent);
            tv_unknown.setText(st_unknown);
        }
    }

    private void getSummaries() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.upload_summaries)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ServiceGenerator.getUnsafeOkHttpClient())
                .build();

        jsonProcesses = retrofit.create(JsonProcesses.class);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("varUser", pref.get_prefs_auth("auth", getContext()));
        parameters.put("varDev", pref.get_prefs_auth("print", getActivity()));

        Call<SummaryResponse> call = jsonProcesses.getSummaryCalc(parameters);
        call.enqueue(new Callback<SummaryResponse>() {
            @Override
            public void onResponse(Call<SummaryResponse> call, Response<SummaryResponse> response) {
                if(response.isSuccessful() && response.body()!=null){
                }
            }

            @Override
            public void onFailure(Call<SummaryResponse> call, Throwable t) {
                //Toast.makeText(getActivity(),  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
                Log.e(kon.TAGGED, "**********************: onFailure Unknown error occurred, please try again");
                Log.e(kon.TAGGED, t.getMessage());
            }
        });
    }

    private void backtoHistory(){
        Frag_History frag_history = new Frag_History();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, frag_history);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //activity.getSupportFragmentManager().popBackStack();
                backtoHistory();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}