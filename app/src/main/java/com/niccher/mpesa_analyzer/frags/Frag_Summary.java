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

    TextView tv_gen_all, tv_gen_bal, tv_gen_fuliza, tv_gen_recv, tv_gen_sent, tv_gen_withdraw, tv_gen_wrong_pin, tv_gen_unknown, tv_gen_time, tv_gen_date;

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

        tv_gen_all = summarizer.findViewById(R.id.cat_point_gen_all);
        tv_gen_bal = summarizer.findViewById(R.id.cat_point_gen_bal);
        tv_gen_fuliza = summarizer.findViewById(R.id.cat_point_gen_fuliza);
        tv_gen_recv = summarizer.findViewById(R.id.cat_point_gen_received);
        tv_gen_sent = summarizer.findViewById(R.id.cat_point_gen_sent);
        tv_gen_withdraw = summarizer.findViewById(R.id.cat_point_gen_withdraw);
        tv_gen_wrong_pin = summarizer.findViewById(R.id.cat_point_gen_wrong_pin);
        tv_gen_unknown = summarizer.findViewById(R.id.cat_point_gen_unknown);

        tv_gen_time = summarizer.findViewById(R.id.cat_point_loot_time);
        tv_gen_date = summarizer.findViewById(R.id.cat_point_loot_date);

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
        parameters.put("varLootUuid", loot_name);

        Call<Mod_Loot_Summary> call = jsonProcesses.getSummaryCalc(parameters);
        call.enqueue(new Callback<Mod_Loot_Summary>() {
            @Override
            public void onResponse(Call<Mod_Loot_Summary> call, Response<Mod_Loot_Summary> response) {
                if(response.isSuccessful() && response.body()!=null){
                    Mod_Loot_Summary mod_loot_summary = response.body();

                    tv_gen_all.setText(mod_loot_summary.count_All);
                    tv_gen_bal.setText(mod_loot_summary.count_Get_Bal);
                    tv_gen_fuliza.setText(mod_loot_summary.count_Fuliza_Mini_Statement);
                    tv_gen_recv.setText(mod_loot_summary.count_Get_Receive);
                    tv_gen_sent.setText(mod_loot_summary.count_Sent);
                    tv_gen_withdraw.setText(mod_loot_summary.count_Withdraw);
                    tv_gen_wrong_pin.setText(mod_loot_summary.count_Error_Pin);
                    tv_gen_unknown.setText(mod_loot_summary.count_Unknown);

                    tv_gen_time.setText(mod_loot_summary.created.split(" ")[1]);
                    tv_gen_date.setText(mod_loot_summary.created.split(" ")[0]);
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