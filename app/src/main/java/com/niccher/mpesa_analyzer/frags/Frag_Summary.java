package com.niccher.mpesa_analyzer.frags;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    TextView tv_rec_all, tv_rec_bank, tv_rec_mshwari, tv_rec_ncba, tv_rec_im, tv_rec_reversal;
    TextView tv_bal_all, tv_bal_mpesa, tv_bal_mshwari, tv_bal_kcb;
    TextView tv_fuliza_all, tv_fuliza_opt_in, tv_fuliza_opt_out, tv_fuliza_limit, tv_fuliza_loan, tv_fuliza_mini;
    TextView tv_error_all, tv_error_pin, tv_error_less, tv_error_merchant, tv_error_receiver, tv_error_org, tv_error_failed;

    LinearLayout layout_interactions;

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

        tv_rec_all = summarizer.findViewById(R.id.cat_point_rec_all);
        tv_rec_bank = summarizer.findViewById(R.id.cat_point_rec_bal);
        tv_rec_mshwari = summarizer.findViewById(R.id.cat_point_rec_mshwari);
        tv_rec_ncba = summarizer.findViewById(R.id.cat_point_rec_ncba);
        tv_rec_im = summarizer.findViewById(R.id.cat_point_rec_im);
        tv_rec_reversal = summarizer.findViewById(R.id.cat_point_rec_reversal);

        tv_bal_all = summarizer.findViewById(R.id.cat_point_bal_all);
        tv_bal_mpesa = summarizer.findViewById(R.id.cat_point_bal_mpesa);
        tv_bal_mshwari = summarizer.findViewById(R.id.cat_point_bal_mshwari);
        tv_bal_kcb = summarizer.findViewById(R.id.cat_point_bal_kcb);

        tv_fuliza_all = summarizer.findViewById(R.id.cat_point_fuliza_all);
        tv_fuliza_opt_in = summarizer.findViewById(R.id.cat_point_fuliza_opt_in);
        tv_fuliza_opt_out = summarizer.findViewById(R.id.cat_point_fuliza_opt_out);
        tv_fuliza_limit = summarizer.findViewById(R.id.cat_point_fuliza_limit);
        tv_fuliza_loan = summarizer.findViewById(R.id.cat_point_fuliza_loan);
        tv_fuliza_mini = summarizer.findViewById(R.id.cat_point_fuliza_mini);

        tv_error_all = summarizer.findViewById(R.id.cat_point_error_all);
        tv_error_pin = summarizer.findViewById(R.id.cat_point_error_pin);
        tv_error_less = summarizer.findViewById(R.id.cat_point_error_less);
        tv_error_merchant = summarizer.findViewById(R.id.cat_point_error_merchant);
        tv_error_receiver = summarizer.findViewById(R.id.cat_point_error_receiver);
        tv_error_org = summarizer.findViewById(R.id.cat_point_error_org);
        tv_error_failed = summarizer.findViewById(R.id.cat_point_error_failed);

        tv_gen_time = summarizer.findViewById(R.id.cat_point_loot_time);
        tv_gen_date = summarizer.findViewById(R.id.cat_point_loot_date);

        layout_interactions = summarizer.findViewById(R.id.summary_interactions_layout);

        getConnectionState();

        return summarizer;
    }

    @Override
    public void onResume() {
        super.onResume();
        getConnectionState();
    }

    public void getConnectionState(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()){
            getReferences();
        }else {
            layout_interactions.setVisibility(View.GONE);
            Toast.makeText(activity, "No internet connection at the moment", Toast.LENGTH_LONG).show();
        }
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

                    int all_rec = 0 , all_bal = 0, all_fuliza = 0, all_error = 0;

                    tv_gen_all.setText(mod_loot_summary.count_All);
                    tv_gen_bal.setText(mod_loot_summary.count_Get_Bal);
                    tv_gen_fuliza.setText(mod_loot_summary.count_Fuliza_Mini_Statement);
                    tv_gen_recv.setText(mod_loot_summary.count_Get_Receive);
                    tv_gen_sent.setText(mod_loot_summary.count_Sent);
                    tv_gen_withdraw.setText(mod_loot_summary.count_Withdraw);
                    tv_gen_wrong_pin.setText(mod_loot_summary.count_Error_Pin);
                    tv_gen_unknown.setText(mod_loot_summary.count_Unknown);

                    all_rec = Integer.parseInt(mod_loot_summary.count_Get_Receive) + Integer.parseInt(mod_loot_summary.count_Get_Bank) +
                            Integer.parseInt(mod_loot_summary.count_Get_from_NCBA) + Integer.parseInt(mod_loot_summary.count_Get_Mshwari) +
                            Integer.parseInt(mod_loot_summary.count_Get_from_IM) + Integer.parseInt(mod_loot_summary.count_Get_Reversal);
                    tv_rec_all.setText(String.valueOf(all_rec));
                    tv_rec_bank.setText(mod_loot_summary.count_Get_Receive);//Mpesa
                    tv_rec_mshwari.setText(mod_loot_summary.count_Get_Mshwari);
                    tv_rec_ncba.setText(mod_loot_summary.count_Get_from_NCBA);
                    tv_rec_im.setText(mod_loot_summary.count_Get_from_IM);
                    tv_rec_reversal.setText(mod_loot_summary.count_Get_Reversal);

                    all_bal = Integer.parseInt(mod_loot_summary.count_Get_Bal_KCB) + Integer.parseInt(mod_loot_summary.count_Get_Bal_Mshwari) +
                            Integer.parseInt(mod_loot_summary.count_Get_Bal);
                    tv_bal_all.setText( String.valueOf(all_bal));
                    tv_bal_mpesa.setText(mod_loot_summary.count_Get_Bal);
                    tv_bal_mshwari.setText(mod_loot_summary.count_Get_Bal_Mshwari);
                    tv_bal_kcb.setText(mod_loot_summary.count_Get_Bal_KCB);

                    all_fuliza = Integer.parseInt(mod_loot_summary.count_Fuliza_Opt_In) +
                            Integer.parseInt(mod_loot_summary.count_Fuliza_Limit) + Integer.parseInt(mod_loot_summary.count_Fuliza_Leave) +
                            Integer.parseInt(mod_loot_summary.count_Fuliza_Loan_Taken) + Integer.parseInt(mod_loot_summary.count_Fuliza_Mini_Statement);
                    tv_fuliza_all.setText(String.valueOf(all_fuliza));
                    tv_fuliza_opt_in.setText(mod_loot_summary.count_Fuliza_Opt_In);
                    tv_fuliza_opt_out.setText(mod_loot_summary.count_Fuliza_Leave);
                    tv_fuliza_limit.setText(mod_loot_summary.count_Fuliza_Limit);
                    tv_fuliza_loan.setText(mod_loot_summary.count_Fuliza_Loan_Taken);
                    tv_fuliza_mini.setText(mod_loot_summary.count_Fuliza_Mini_Statement);

                    all_fuliza = Integer.parseInt(mod_loot_summary.count_Fuliza_Opt_In) +
                            Integer.parseInt(mod_loot_summary.count_Fuliza_Limit) + Integer.parseInt(mod_loot_summary.count_Fuliza_Leave) +
                            Integer.parseInt(mod_loot_summary.count_Fuliza_Loan_Taken) + Integer.parseInt(mod_loot_summary.count_Fuliza_Mini_Statement);
                    tv_fuliza_all.setText(String.valueOf(all_fuliza));
                    tv_fuliza_opt_in.setText(mod_loot_summary.count_Fuliza_Opt_In);
                    tv_fuliza_opt_out.setText(mod_loot_summary.count_Fuliza_Leave);
                    tv_fuliza_limit.setText(mod_loot_summary.count_Fuliza_Limit);
                    tv_fuliza_loan.setText(mod_loot_summary.count_Fuliza_Loan_Taken);
                    tv_fuliza_mini.setText(mod_loot_summary.count_Fuliza_Mini_Statement);

                    all_error = Integer.parseInt(mod_loot_summary.count_Error_Pin) + Integer.parseInt(mod_loot_summary.count_Error_Less) +
                            Integer.parseInt(mod_loot_summary.count_Error_Receiver) + Integer.parseInt(mod_loot_summary.count_Error_Pay_Merchant) +
                            Integer.parseInt(mod_loot_summary.count_Error_Receiver_Org) + Integer.parseInt(mod_loot_summary.count_Error_Failed);
                    tv_error_all.setText(String.valueOf(all_error));
                    tv_error_pin.setText(mod_loot_summary.count_Error_Pin);
                    tv_error_less.setText(mod_loot_summary.count_Error_Less);
                    tv_error_merchant.setText(mod_loot_summary.count_Error_Pay_Merchant);
                    tv_error_receiver.setText(mod_loot_summary.count_Error_Receiver);
                    tv_error_org.setText(mod_loot_summary.count_Error_Receiver_Org);
                    tv_error_failed.setText(mod_loot_summary.count_Error_Failed);

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