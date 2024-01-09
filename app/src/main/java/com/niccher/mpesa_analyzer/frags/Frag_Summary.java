package com.niccher.mpesa_analyzer.frags;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.helpers.Prefs;
import com.niccher.mpesa_analyzer.helpers.ServiceGenerator;
import com.niccher.mpesa_analyzer.interfaces.JsonProcesses;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_Loot_Summary;

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

    TextView tv_all, tv_bal, tv_fuliza, tv_recv, tv_sent, tv_withdraw, tv_wrong_pin, tv_unknown, tv_time, tv_date;

    JsonProcesses jsonProcesses;
    Gson gson = null;

    Prefs pref;

    public Frag_Summary() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        kon = new Konstants();
        pref = new Prefs();

        gson = new GsonBuilder()
                .setLenient()
                .create();

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

        tv_all = summarizer.findViewById(R.id.cat_point_all);
        tv_bal = summarizer.findViewById(R.id.cat_point_bal);
        tv_fuliza = summarizer.findViewById(R.id.cat_point_fuliza);
        tv_recv = summarizer.findViewById(R.id.cat_point_received);
        tv_sent = summarizer.findViewById(R.id.cat_point_sent);
        tv_withdraw = summarizer.findViewById(R.id.cat_point_withdraw);
        tv_wrong_pin = summarizer.findViewById(R.id.cat_point_wrong_pin);
        tv_unknown = summarizer.findViewById(R.id.cat_point_unknown);

        tv_time = summarizer.findViewById(R.id.cat_point_loot_time);
        tv_date = summarizer.findViewById(R.id.cat_point_loot_date);

        getReferences();

        return summarizer;
    }

    public void getReferences(){
        Bundle sent_data = this.getArguments();
        String st_name;
        if(sent_data != null){
            st_name = sent_data.get("summary_loot_name").toString();

            getSummaries(st_name);
        }
    }

    private void getSummaries(String loot_name) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.upload_summaries)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ServiceGenerator.getUnsafeOkHttpClient())
                .build();

        jsonProcesses = retrofit.create(JsonProcesses.class);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("varUser", pref.get_prefs_auth("auth", getContext()));
        parameters.put("varDev", pref.get_prefs_auth("print", getActivity()));
        parameters.put("varLootName", loot_name);

        Call<Mod_Loot_Summary> call = jsonProcesses.getSummaryCalc(parameters);
        call.enqueue(new Callback<Mod_Loot_Summary>() {
            @Override
            public void onResponse(Call<Mod_Loot_Summary> call, Response<Mod_Loot_Summary> response) {
                if(response.isSuccessful() && response.body()!=null){
                    Mod_Loot_Summary mod_loot_summary = response.body();

                    tv_all.setText(mod_loot_summary.count_All);
                    tv_bal.setText(mod_loot_summary.count_Get_Bal);
                    tv_fuliza.setText(mod_loot_summary.count_Fuliza_Mini_Statement);
                    tv_recv.setText(mod_loot_summary.count_Get_Receive);
                    tv_sent.setText(mod_loot_summary.count_Sent);
                    tv_withdraw.setText(mod_loot_summary.count_Withdraw);
                    tv_wrong_pin.setText(mod_loot_summary.count_Error_Pin);
                    tv_unknown.setText(mod_loot_summary.count_Unknown);

                    tv_time.setText(mod_loot_summary.created.split(" ")[1]);
                    tv_date.setText(mod_loot_summary.created.split(" ")[0]);
                }
            }

            @Override
            public void onFailure(Call<Mod_Loot_Summary> call, Throwable t) {
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