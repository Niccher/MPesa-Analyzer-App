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
import com.niccher.mpesa_analyzer.models.Mod_Loot_Delete;
import com.niccher.mpesa_analyzer.models.Mod_Loot_Summary;
import com.niccher.mpesa_analyzer.models.Mod_Summaries;

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

    TextView tv_sent_all, tv_sent_mpesa, tv_sent_mshwari, tv_sent_lnm, tv_sent_mini, tv_sent_cancel;
    TextView tv_rec_all, tv_rec_bank, tv_rec_mshwari, tv_rec_ncba, tv_rec_im, tv_rec_kcb, tv_rec_reversal;
    TextView tv_bal_all, tv_bal_mpesa, tv_bal_mshwari, tv_bal_kcb;
    TextView tv_fuliza_all, tv_fuliza_opt_in, tv_fuliza_opt_out, tv_fuliza_limit, tv_fuliza_loan, tv_fuliza_mini;
    TextView tv_error_all, tv_error_pin, tv_error_less, tv_error_merchant, tv_error_receiver, tv_error_org, tv_error_failed;

    LinearLayout lin_lay_gen_all, lin_lay_gen_bal, lin_lay_gen_fuliza, lin_lay_gen_recv, lin_lay_gen_sent, lin_lay_gen_withdraw, lin_lay_gen_wrong_pin, lin_lay_gen_unknown, lin_lay_gen_time, lin_lay_gen_date;
    LinearLayout lin_lay_rec_all, lin_lay_rec_bank, lin_lay_rec_mshwari, lin_lay_rec_ncba, lin_lay_rec_im, lin_lay_rec_reversal;
    LinearLayout lin_lay_bal_all, lin_lay_bal_mpesa, lin_lay_bal_mshwari, lin_lay_bal_kcb;
    LinearLayout lin_lay_fuliza_all, lin_lay_fuliza_opt_in, lin_lay_fuliza_opt_out, lin_lay_fuliza_limit, lin_lay_fuliza_loan, lin_lay_fuliza_mini;
    LinearLayout lin_lay_error_all, lin_lay_error_pin, lin_lay_error_less, lin_lay_error_merchant, lin_lay_error_receiver, lin_lay_error_org, lin_lay_error_failed;
    LinearLayout lin_loot_delete;

    String lood_uuid;

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

        tv_sent_all = summarizer.findViewById(R.id.cat_point_sent_all);
        tv_sent_mpesa = summarizer.findViewById(R.id.cat_point_sent_mpesa);
        tv_sent_mshwari = summarizer.findViewById(R.id.cat_point_sent_mshwari);
        tv_sent_lnm = summarizer.findViewById(R.id.cat_point_sent_lnm);
        tv_sent_mini = summarizer.findViewById(R.id.cat_point_sent_mini);
        tv_sent_cancel = summarizer.findViewById(R.id.cat_point_sent_cancel);

        tv_rec_all = summarizer.findViewById(R.id.cat_point_rec_all);
        tv_rec_bank = summarizer.findViewById(R.id.cat_point_rec_bal);
        tv_rec_mshwari = summarizer.findViewById(R.id.cat_point_rec_mshwari);
        tv_rec_ncba = summarizer.findViewById(R.id.cat_point_rec_ncba);
        tv_rec_im = summarizer.findViewById(R.id.cat_point_rec_im);
        tv_rec_reversal = summarizer.findViewById(R.id.cat_point_rec_reversal);
        tv_rec_kcb = summarizer.findViewById(R.id.cat_point_rec_kcb);

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

        lin_lay_gen_all = summarizer.findViewById(R.id.summary_gen_All);
        lin_lay_gen_bal = summarizer.findViewById(R.id.summary_Gen_Balance);
        lin_lay_gen_fuliza = summarizer.findViewById(R.id.summary_Gen_Fuliza);
        lin_lay_gen_recv = summarizer.findViewById(R.id.summary_Gen_Received);
        lin_lay_gen_sent = summarizer.findViewById(R.id.summary_Gen_Sent);
        lin_lay_gen_withdraw = summarizer.findViewById(R.id.summary_Gen_Withdraw);
        lin_lay_gen_wrong_pin = summarizer.findViewById(R.id.summary_Gen_Wrong_Pin);
        lin_lay_gen_unknown = summarizer.findViewById(R.id.summary_Gen_Unknown);

        lin_lay_rec_all = summarizer.findViewById(R.id.summary_Received_All);
        lin_lay_rec_bank = summarizer.findViewById(R.id.summary_Rec_Mpesa);
        lin_lay_rec_mshwari = summarizer.findViewById(R.id.summary_Rec_Mshwari);
        lin_lay_rec_ncba = summarizer.findViewById(R.id.summary_Rec_NCBA);
        lin_lay_rec_im = summarizer.findViewById(R.id.summary_Rec_IM);
        lin_lay_rec_reversal = summarizer.findViewById(R.id.summary_Rec_Reversal);

        lin_lay_bal_all = summarizer.findViewById(R.id.summary_ball_All);
        lin_lay_bal_mpesa = summarizer.findViewById(R.id.summary_Bal_mpesa);
        lin_lay_bal_mshwari = summarizer.findViewById(R.id.summary_Bal_mshwari);
        lin_lay_bal_kcb = summarizer.findViewById(R.id.summary_Bal_KCB);

        lin_lay_fuliza_all = summarizer.findViewById(R.id.summary_Fuliza_All);
        lin_lay_fuliza_opt_in = summarizer.findViewById(R.id.summary_Fuliza_Opt_In);
        lin_lay_fuliza_opt_out = summarizer.findViewById(R.id.summary_Fuliza_Opt_Out);
        lin_lay_fuliza_limit = summarizer.findViewById(R.id.summary_Fuliza_Limit);
        lin_lay_fuliza_loan = summarizer.findViewById(R.id.summary_Fuliza_Loan);
        lin_lay_fuliza_mini = summarizer.findViewById(R.id.summary_Fuliza_Mini);

        lin_lay_error_all = summarizer.findViewById(R.id.summary_Error_All);
        lin_lay_error_pin = summarizer.findViewById(R.id.summary_Error_Pin);
        lin_lay_error_less = summarizer.findViewById(R.id.summary_Error_Less);
        lin_lay_error_merchant = summarizer.findViewById(R.id.summary_Error_Merchant);
        lin_lay_error_receiver = summarizer.findViewById(R.id.summary_Error_Receiver);
        lin_lay_error_org = summarizer.findViewById(R.id.summary_Error_Org);
        lin_lay_error_failed = summarizer.findViewById(R.id.summary_Error_Failed);

        lin_loot_delete = summarizer.findViewById(R.id.summary_Loot_Delete);

        layout_interactions = summarizer.findViewById(R.id.summary_interactions_layout);

        lood_uuid = "";

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

    public void getClickedAction(String count_Loot_Uuid){
        lin_lay_gen_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_all");
            }
        });
        lin_lay_gen_bal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_bal");
            }
        });
        lin_lay_gen_fuliza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_fuliza");
            }
        });
        lin_lay_gen_recv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_recv");
            }
        });
        lin_lay_gen_sent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_sent");
            }
        });
        lin_lay_gen_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_withdraw");
            }
        });
        lin_lay_gen_wrong_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_wrong_pin");
            }
        });
        lin_lay_gen_unknown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_gen_unknown");
            }
        });

        lin_lay_rec_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_rec_all");
            }
        });
        lin_lay_rec_bank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Received from Mpesa");
            }
        });
        lin_lay_rec_mshwari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Received from Mshwari");
            }
        });
        lin_lay_rec_ncba.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Received from NCBA");
            }
        });
        lin_lay_rec_im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Received from IM");
            }
        });
        lin_lay_rec_reversal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Received from Reversal");
            }
        });

        lin_lay_bal_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_bal_all");
            }
        });
        lin_lay_bal_mpesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Get MPESA Balance");
            }
        });
        lin_lay_bal_mshwari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Get MShwari Balance");
            }
        });
        lin_lay_bal_kcb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Get KCB Balance");
            }
        });

        lin_lay_fuliza_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_fuliza_all");
            }
        });
        lin_lay_fuliza_opt_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Fuliza Opt In");
            }
        });
        lin_lay_fuliza_opt_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Fuliza Leave");
            }
        });
        lin_lay_fuliza_limit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Fuliza Limit");
            }
        });
        lin_lay_fuliza_loan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Fuliza Loan Taken");
            }
        });
        lin_lay_fuliza_mini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Fuliza Mini Statement");
            }
        });

        lin_lay_error_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("lin_lay_error_all");
            }
        });
        lin_lay_error_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Wrong Pin");
            }
        });
        lin_lay_error_less.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Insufficient funds");
            }
        });
        lin_lay_error_merchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Wrong Merchant");
            }
        });
        lin_lay_error_receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Receiver not in Service");
            }
        });
        lin_lay_error_org.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Org not in Service");
            }
        });
        lin_lay_error_failed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Transaction Cancelled");
            }
        });
        lin_loot_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSmsListingFor("Delete Loot");
            }
        });
    }

    public void getSmsListingFor(String category){
        if (category != null | !category.isEmpty()){
            Toast.makeText(activity, "Category as "+ category, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(activity, "Null Category", Toast.LENGTH_SHORT).show();
        }
    }

    private void getSummaries(String loot_name) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.link_process)
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

                    int all_rec = 0, all_sent = 0, all_bal = 0, all_fuliza = 0, all_error = 0;

                    tv_gen_all.setText(mod_loot_summary.count_All);
                    tv_gen_bal.setText(mod_loot_summary.count_Get_Bal_MPESA);
                    tv_gen_fuliza.setText(mod_loot_summary.count_Fuliza_Mini_Statement);
                    tv_gen_recv.setText(mod_loot_summary.count_Get_from_MPESA);
                    tv_gen_sent.setText(mod_loot_summary.count_Sent_to_MPESA);
                    tv_gen_withdraw.setText(mod_loot_summary.count_Withdraw);
                    tv_gen_wrong_pin.setText(mod_loot_summary.count_Error_Pin);
                    tv_gen_unknown.setText(mod_loot_summary.count_Unknown);

                    all_sent = Integer.parseInt(mod_loot_summary.count_Sent_to_MPESA) + Integer.parseInt(mod_loot_summary.count_Sent_to_Mshwari) +
                            Integer.parseInt(mod_loot_summary.count_Sent_to_LNM) + Integer.parseInt(mod_loot_summary.count_Sent_Mini) +
                            Integer.parseInt(mod_loot_summary.count_Sent_Cancel);
                    tv_sent_all.setText(String.valueOf(all_sent));
                    tv_sent_mpesa.setText(mod_loot_summary.count_Sent_to_MPESA);
                    tv_sent_mshwari.setText(mod_loot_summary.count_Sent_to_Mshwari);
                    tv_sent_lnm.setText(mod_loot_summary.count_Sent_to_LNM);
                    tv_sent_mini.setText(mod_loot_summary.count_Sent_Mini);
                    tv_sent_cancel.setText(mod_loot_summary.count_Sent_Cancel);

                    all_rec = Integer.parseInt(mod_loot_summary.count_Get_from_MPESA) + Integer.parseInt(mod_loot_summary.count_Get_from_KCB) +
                            Integer.parseInt(mod_loot_summary.count_Get_from_NCBA) + Integer.parseInt(mod_loot_summary.count_Get_from_Mshwari) +
                            Integer.parseInt(mod_loot_summary.count_Get_from_IM) + Integer.parseInt(mod_loot_summary.count_Get_from_Reversal);
                    tv_rec_all.setText(String.valueOf(all_rec));
                    tv_rec_bank.setText(mod_loot_summary.count_Get_from_MPESA);//Mpesa
                    tv_rec_mshwari.setText(mod_loot_summary.count_Get_from_Mshwari);
                    tv_rec_ncba.setText(mod_loot_summary.count_Get_from_NCBA);
                    tv_rec_im.setText(mod_loot_summary.count_Get_from_IM);
                    tv_rec_kcb.setText(mod_loot_summary.count_Get_from_KCB);
                    tv_rec_reversal.setText(mod_loot_summary.count_Get_from_Reversal);

                    all_bal = Integer.parseInt(mod_loot_summary.count_Get_Bal_KCB) + Integer.parseInt(mod_loot_summary.count_Get_Bal_Mshwari) +
                            Integer.parseInt(mod_loot_summary.count_Get_Bal_MPESA);
                    tv_bal_all.setText( String.valueOf(all_bal));
                    tv_bal_mpesa.setText(mod_loot_summary.count_Get_Bal_MPESA);
                    tv_bal_mshwari.setText(mod_loot_summary.count_Get_Bal_Mshwari);
                    tv_bal_kcb.setText(mod_loot_summary.count_Get_Bal_KCB);

                    all_fuliza = Integer.parseInt(mod_loot_summary.count_Fuliza_Opt_In) +
                            Integer.parseInt(mod_loot_summary.count_Fuliza_Limit) + Integer.parseInt(mod_loot_summary.count_Fuliza_Opt_Out) +
                            Integer.parseInt(mod_loot_summary.count_Fuliza_Loan_Taken) + Integer.parseInt(mod_loot_summary.count_Fuliza_Mini_Statement);
                    tv_fuliza_all.setText(String.valueOf(all_fuliza));
                    tv_fuliza_opt_in.setText(mod_loot_summary.count_Fuliza_Opt_In);
                    tv_fuliza_opt_out.setText(mod_loot_summary.count_Fuliza_Opt_Out);
                    tv_fuliza_limit.setText(mod_loot_summary.count_Fuliza_Limit);
                    tv_fuliza_loan.setText(mod_loot_summary.count_Fuliza_Loan_Taken);
                    tv_fuliza_mini.setText(mod_loot_summary.count_Fuliza_Mini_Statement);

                    all_error = Integer.parseInt(mod_loot_summary.count_Error_Pin) + Integer.parseInt(mod_loot_summary.count_Error_Less) +
                            Integer.parseInt(mod_loot_summary.count_Error_Receiver) +
                            Integer.parseInt(mod_loot_summary.count_Error_Receiver_Org) + Integer.parseInt(mod_loot_summary.count_Error_Failed);
                    tv_error_all.setText(String.valueOf(all_error));
                    tv_error_pin.setText(mod_loot_summary.count_Error_Pin);
                    tv_error_less.setText(mod_loot_summary.count_Error_Less);
                    tv_error_merchant.setText("0");
                    tv_error_receiver.setText(mod_loot_summary.count_Error_Receiver);
                    tv_error_org.setText(mod_loot_summary.count_Error_Receiver_Org);
                    tv_error_failed.setText(mod_loot_summary.count_Error_Failed);

                    tv_gen_time.setText(mod_loot_summary.loot_Created.split(" ")[1]);
                    tv_gen_date.setText(mod_loot_summary.loot_Created.split(" ")[0]);

                    getClickedAction(mod_loot_summary.loot_Uuid);
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

    private void setLootToDelete(String loot_uuid) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.link_process)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ServiceGenerator.getUnsafeOkHttpClient())
                .build();

        jsonProcesses = retrofit.create(JsonProcesses.class);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("varUser", pref.get_prefs_auth("auth", getContext()));
        parameters.put("varDev", pref.get_prefs_auth("print", getActivity()));
        parameters.put("varLootUuid", loot_uuid);

        Call<Mod_Loot_Delete> call = jsonProcesses.getLootDelete(parameters);
        call.enqueue(new Callback<Mod_Loot_Delete>() {
            @Override
            public void onResponse(Call<Mod_Loot_Delete> call, Response<Mod_Loot_Delete> response) {
                if(response.isSuccessful() && response.body()!=null){
                    Mod_Loot_Delete mod_summary = response.body();

                    if (mod_summary.getSummary_Status() == "1"){

                    } else if (mod_summary.getSummary_Status() == "2"){

                    }
                }
            }

            @Override
            public void onFailure(Call<Mod_Loot_Delete> call, Throwable t) {
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