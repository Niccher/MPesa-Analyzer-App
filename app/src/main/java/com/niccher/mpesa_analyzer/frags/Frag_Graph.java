package com.niccher.mpesa_analyzer.frags;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_Sms_Spinner_adapter;
import com.niccher.mpesa_analyzer.helpers.Prefs;
import com.niccher.mpesa_analyzer.helpers.ServiceGenerator;
import com.niccher.mpesa_analyzer.helpers.SummaryLootResponse;
import com.niccher.mpesa_analyzer.interfaces.JsonProcesses;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_Loot_Summary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Frag_Graph extends Fragment implements AdapterView.OnItemSelectedListener {

    GraphView graph_line, graph_line1;
    AppCompatActivity activity;
    TextView conn_state;
    ProgressBar conn_wait;

    JsonProcesses jsonProcesses;
    Konstants kon;
    Prefs pref;
    Gson gson = null;
    ArrayList<Mod_Loot_Summary> summaryLootList;

    Spinner spin_cat_sms;
    String sms_category_selected;
    SummaryLootResponse stored_SummaryLootResponse;

    String[] arr_categories;
    int[] arr_cat_imgs;

    LineGraphSeries<DataPoint> series_Get_Receive,series_Get_Bank,series_Get_Mshwari,series_Get_from_NCBA,series_Get_from_IM,series_Get_Bal,
            series_Get_Bal_KCB,series_Get_Bal_Mshwari,series_Get_Reversal,series_Loan_Limit,series_Sent,series_Sent_Mini,series_Sent_Mshwari,
            series_Sent_Cancel,series_Error_Failed,series_Error_Pay_Merchant,series_Error_Pin,series_Error_Less,series_Error_Receiver
            ,series_Error_Receiver_Org,series_Withdraw,series_Fuliza_Leave,series_Fuliza_Opt_In,series_Fuliza_Limit,series_Fuliza_Mini_Statement,
            series_Fuliza_Loan_Taken,series_Similar_Transaction,series_All,series_Unknown;
    PointsGraphSeries<DataPoint> point_sent, point_received, point_unknown;

    public Frag_Graph() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (AppCompatActivity) getActivity();
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Graph View");
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
        setHasOptionsMenu(true);

        kon = new Konstants();
        pref = new Prefs();

        gson = new GsonBuilder()
                .setLenient()
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View grapher = inflater.inflate(R.layout.frag_graph, container, false);

        conn_state = grapher.findViewById(R.id.conn_no_internet);
        conn_wait = grapher.findViewById(R.id.conn_wait_internet);
        conn_wait.setVisibility(View.GONE);

        graph_line = grapher.findViewById(R.id.graph);

        graph_line.setVisibility(View.GONE);

        spin_cat_sms = grapher.findViewById(R.id.spinner_sms_category);
        initSpinners(grapher);

        graph_line.setTitle("Category of SMS");
        getConnectionState();

        return grapher;
    }

    public void getConnectionState() {
        if (isConnected()) {
            conn_wait.setVisibility(View.VISIBLE);
            conn_state.setVisibility(View.GONE);
            getSummaries();
        } else {
            conn_wait.setVisibility(View.GONE);
            isOffline("No internet connection at the moment.");
        }
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void isOffline(String mgs) {
        graph_line.setVisibility(View.GONE);
        //graph_line1.setVisibility(View.GONE);
        conn_state.setText(mgs);
    }

    private void initSpinners(View this_view) {
        arr_categories = new String[]{"All", "Get Receive","Get Bank","Get Mshwari","Get from NCBA","Get from IM",
                "Get Bal","Get Bal KCB","Get Bal Mshwari","Get Reversal","Loan Limit","Sent",
                "Sent Mini","Sent Mshwari","Sent Cancel","Error Failed","Error Pay Merchant",
                "Error Pin","Error Less","Error Receiver","Error Receiver Org","Withdraw",
                "Fuliza Leave","Fuliza Opt In","Fuliza Limit","Fuliza Mini Statement",
                "Fuliza Loan Taken","Similar Transaction","Unknown"};

        arr_cat_imgs = new int[]{R.mipmap.summ_money_all, R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza,
                R.mipmap.summ_money_received, R.mipmap.summ_money_sent, R.mipmap.summ_money_withdraw,
                R.mipmap.summ_money_wrong_pin, R.mipmap.summ_money_unknown,

                R.mipmap.summ_money_all, R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza,
                R.mipmap.summ_money_received, R.mipmap.summ_money_sent, R.mipmap.summ_money_withdraw,
                R.mipmap.summ_money_wrong_pin, R.mipmap.summ_money_unknown,
                R.mipmap.summ_money_all, R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza,

                R.mipmap.summ_money_received, R.mipmap.summ_money_sent, R.mipmap.summ_money_withdraw,
                R.mipmap.summ_money_wrong_pin, R.mipmap.summ_money_unknown,
                R.mipmap.summ_money_all, R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza,

                R.mipmap.summ_money_received, R.mipmap.summ_money_sent
        };

        Info_Sms_Spinner_adapter info_sms_spinner_adapter = new Info_Sms_Spinner_adapter(getActivity(), arr_cat_imgs, arr_categories);
        spin_cat_sms.setAdapter(info_sms_spinner_adapter);
        spin_cat_sms.setOnItemSelectedListener(this);
    }

    private void getSummaries() {
        conn_wait.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.upload_summaries)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ServiceGenerator.getUnsafeOkHttpClient())
                .build();

        jsonProcesses = retrofit.create(JsonProcesses.class);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("varUser", pref.get_prefs_auth("auth", getActivity()));
        parameters.put("varDev", pref.get_prefs_auth("print", getActivity()));

        Call<SummaryLootResponse> call = jsonProcesses.getLootCountCategories(parameters);
        call.enqueue(new Callback<SummaryLootResponse>() {
            @Override
            public void onResponse(Call<SummaryLootResponse> call, Response<SummaryLootResponse> response) {
                conn_wait.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    //{"loot_summarizer":
                    try {
                        SummaryLootResponse summaryLootResponse = response.body();
                        stored_SummaryLootResponse = summaryLootResponse;
                        graph_all_summaries(summaryLootResponse);
                    }catch (Exception ex){
                        Log.e(kon.TAGGED, "response.isSuccessful "+ ex.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<SummaryLootResponse> call, Throwable t) {
                conn_wait.setVisibility(View.GONE);
                isOffline("Unknown error has occurred, please try again");
                //Toast.makeText(getActivity(),  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
                Log.e(kon.TAGGED, "**********************: onFailure Unknown error occurred, please try again");
                Log.e(kon.TAGGED, t.getMessage());
            }
        });
    }

    private void graph_all_summaries(SummaryLootResponse summaryLootResponse) {
        Frag_Graph.this.summaryLootList = new ArrayList<Mod_Loot_Summary>(Arrays.asList(summaryLootResponse.getLootSummarizer()));

        int counter = 0;
        int countar = Frag_Graph.this.summaryLootList.size();

        DataPoint point_Get_Receive,point_Get_Bank,point_Get_Mshwari,point_Get_from_NCBA,point_Get_from_IM,point_Get_Bal,point_Get_Bal_KCB,
                point_Get_Bal_Mshwari,point_Get_Reversal,point_Loan_Limit,point_Sent,point_Sent_Mini,point_Sent_Mshwari,
                point_Sent_Cancel,point_Error_Failed,point_Error_Pay_Merchant,point_Error_Pin,point_Error_Less,point_Error_Receiver,
                point_Error_Receiver_Org,point_Withdraw,point_Fuliza_Leave,point_Fuliza_Opt_In,point_Fuliza_Limit,point_Fuliza_Mini_Statement,
                point_Fuliza_Loan_Taken,point_Similar_Transaction,point_All,point_Unknown;

        graph_line.removeAllSeries();

        series_Get_Receive = new LineGraphSeries<>();
        series_Get_Bank = new LineGraphSeries<>();
        series_Get_Mshwari = new LineGraphSeries<>();
        series_Get_from_NCBA = new LineGraphSeries<>();
        series_Get_from_IM = new LineGraphSeries<>();
        series_Get_Bal = new LineGraphSeries<>();
        series_Get_Bal_KCB = new LineGraphSeries<>();
        series_Get_Bal_Mshwari = new LineGraphSeries<>();
        series_Get_Reversal = new LineGraphSeries<>();
        series_Loan_Limit = new LineGraphSeries<>();
        series_Sent = new LineGraphSeries<>();
        series_Sent_Mini = new LineGraphSeries<>();
        series_Sent_Mshwari = new LineGraphSeries<>();
        series_Sent_Cancel = new LineGraphSeries<>();
        series_Error_Failed = new LineGraphSeries<>();
        series_Error_Pay_Merchant = new LineGraphSeries<>();
        series_Error_Pin = new LineGraphSeries<>();
        series_Error_Less = new LineGraphSeries<>();
        series_Error_Receiver = new LineGraphSeries<>();
        series_Error_Receiver_Org = new LineGraphSeries<>();
        series_Withdraw = new LineGraphSeries<>();
        series_Fuliza_Leave = new LineGraphSeries<>();
        series_Fuliza_Opt_In = new LineGraphSeries<>();
        series_Fuliza_Limit = new LineGraphSeries<>();
        series_Fuliza_Mini_Statement = new LineGraphSeries<>();
        series_Fuliza_Loan_Taken = new LineGraphSeries<>();
        series_Similar_Transaction = new LineGraphSeries<>();
        series_All = new LineGraphSeries<>();
        series_Unknown = new LineGraphSeries<>();

        series_Get_Receive.setColor(getResources().getColor(R.color.red));
//        series_Get_Bank.setColor(getResources().getColor(R.color.dark_gray));
//        series_Get_Mshwari.setColor(getResources().getColor(R.color.dark_gray));
//        series_Get_from_NCBA.setColor(getResources().getColor(R.color.dark_gray));
//        series_Get_from_IM.setColor(getResources().getColor(R.color.dark_gray));
        series_Get_Bal.setColor(getResources().getColor(R.color.bg_green_light));
//        series_Get_Bal_KCB.setColor(getResources().getColor(R.color.dark_gray));
//        series_Get_Bal_Mshwari.setColor(getResources().getColor(R.color.dark_gray));
//        series_Get_Reversal.setColor(getResources().getColor(R.color.dark_gray));
//        series_Loan_Limit.setColor(getResources().getColor(R.color.dark_gray));
        series_Sent.setColor(getResources().getColor(R.color.alice_blue));
//        series_Sent_Mini.setColor(getResources().getColor(R.color.dark_gray));
//        series_Sent_Mshwari.setColor(getResources().getColor(R.color.dark_gray));
//        series_Sent_Cancel.setColor(getResources().getColor(R.color.dark_gray));
//        series_Error_Failed.setColor(getResources().getColor(R.color.dark_gray));
//        series_Error_Pay_Merchant.setColor(getResources().getColor(R.color.dark_gray));
        series_Error_Pin.setColor(getResources().getColor(R.color.yellow));
//        series_Error_Less.setColor(getResources().getColor(R.color.dark_gray));
//        series_Error_Receiver.setColor(getResources().getColor(R.color.dark_gray));
//        series_Error_Receiver_Org.setColor(getResources().getColor(R.color.dark_gray));
        series_Withdraw.setColor(getResources().getColor(R.color.cyan));
//        series_Fuliza_Leave.setColor(getResources().getColor(R.color.bisque));
//        series_Fuliza_Opt_In.setColor(getResources().getColor(R.color.bg_green_light));
//        series_Fuliza_Limit.setColor(getResources().getColor(R.color.aqua));
//        series_Fuliza_Mini_Statement.setColor(getResources().getColor(R.color.alice_blue));
//        series_Fuliza_Loan_Taken.setColor(getResources().getColor(R.color.pink));
//        series_Similar_Transaction.setColor(getResources().getColor(R.color.red));
        series_All.setColor(getResources().getColor(R.color.deep_pink));
        series_Unknown.setColor(getResources().getColor(R.color.orchid));

        graph_line.setVisibility(View.VISIBLE);

        graph_line.getViewport().setScalable(true);
        graph_line.getViewport().setScrollable(true);
        graph_line.getViewport().setScalableY(true);
        graph_line.getViewport().setScrollableY(true);

        graph_line.getLegendRenderer().setVisible(true);

        for (Mod_Loot_Summary sumlootlist : Frag_Graph.this.summaryLootList) {
            counter++;

            point_Get_Receive  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Receive));
//            point_Get_Bank  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Bank));
//            point_Get_Mshwari  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Mshwari));
//            point_Get_from_NCBA  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_from_NCBA));
//            point_Get_from_IM  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_from_IM));
            point_Get_Bal  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Bal));
//            point_Get_Bal_KCB  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Bal_KCB));
//            point_Get_Bal_Mshwari  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Bal_Mshwari));
//            point_Get_Reversal  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Reversal));
//            point_Loan_Limit  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Loan_Limit));
            point_Sent  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Sent));
//            point_Sent_Mini  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Sent_Mini));
//            point_Sent_Mshwari  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Sent_Mshwari));
//            point_Sent_Cancel  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Sent_Cancel));
//            point_Error_Failed  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Error_Failed));
//            point_Error_Pay_Merchant  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Error_Pay_Merchant));
//            point_Error_Pin  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Error_Pin));
//            point_Error_Less  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Error_Less));
//            point_Error_Receiver  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Error_Receiver));
//            point_Error_Receiver_Org  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Error_Receiver_Org));
            point_Withdraw  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Withdraw));
//            point_Fuliza_Leave  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Fuliza_Leave));
//            point_Fuliza_Opt_In  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Fuliza_Opt_In));
//            point_Fuliza_Limit  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Fuliza_Limit));
//            point_Fuliza_Mini_Statement  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Fuliza_Mini_Statement));
//            point_Fuliza_Loan_Taken  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Fuliza_Loan_Taken));
//            point_Similar_Transaction  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Similar_Transaction));
            point_All  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_All));
            point_Unknown  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Unknown));

            //series_Get_Receive, series_Get_Bal, series_Sent, series_Error_Pin, series_Withdraw, series_All, series_Unknown;

            series_Get_Receive.appendData(point_Get_Receive, true, countar);
//            series_Get_Bank.appendData(point_Get_Bank, true, countar);
//            series_Get_Mshwari.appendData(point_Get_Mshwari, true, countar);
//            series_Get_from_NCBA.appendData(point_Get_from_NCBA, true, countar);
//            series_Get_from_IM.appendData(point_Get_from_IM, true, countar);
            series_Get_Bal.appendData(point_Get_Bal, true, countar);
//            series_Get_Bal_KCB.appendData(point_Get_Bal_KCB, true, countar);
//            series_Get_Bal_Mshwari.appendData(point_Get_Bal_Mshwari, true, countar);
//            series_Get_Reversal.appendData(point_Get_Reversal, true, countar);
//            series_Loan_Limit.appendData(point_Loan_Limit, true, countar);
            series_Sent.appendData(point_Sent, true, countar);
//            series_Sent_Mini.appendData(point_Sent_Mini, true, countar);
//            series_Sent_Mshwari.appendData(point_Sent_Mshwari, true, countar);
//            series_Sent_Cancel.appendData(point_Sent_Cancel, true, countar);
//            series_Error_Failed.appendData(point_Error_Failed, true, countar);
//            series_Error_Pay_Merchant.appendData(point_Error_Pay_Merchant, true, countar);
//            series_Error_Pin.appendData(point_Error_Pin, true, countar);
//            series_Error_Less.appendData(point_Error_Less, true, countar);
//            series_Error_Receiver.appendData(point_Error_Receiver, true, countar);
//            series_Error_Receiver_Org.appendData(point_Error_Receiver_Org, true, countar);
            series_Withdraw.appendData(point_Withdraw, true, countar);
//            series_Fuliza_Leave.appendData(point_Fuliza_Leave, true, countar);
//            series_Fuliza_Opt_In.appendData(point_Fuliza_Opt_In, true, countar);
//            series_Fuliza_Limit.appendData(point_Fuliza_Limit, true, countar);
//            series_Fuliza_Mini_Statement.appendData(point_Fuliza_Mini_Statement, true, countar);
//            series_Fuliza_Loan_Taken.appendData(point_Fuliza_Loan_Taken, true, countar);
//            series_Similar_Transaction.appendData(point_Similar_Transaction, true, countar);
            series_All.appendData(point_All, true, countar);
            series_Unknown.appendData(point_Unknown, true, countar);
        }

        series_Get_Receive.setTitle("Get_Receive.");
//        series_Get_Bank.setTitle("Get_Bank.");
//        series_Get_Mshwari.setTitle("Get_Mshwari.");
//        series_Get_from_NCBA.setTitle("Get_from_NCBA.");
//        series_Get_from_IM.setTitle("Get_from_IM.");
        series_Get_Bal.setTitle("Get_Bal.");
//        series_Get_Bal_KCB.setTitle("Get_Bal_KCB.");
//        series_Get_Bal_Mshwari.setTitle("Get_Bal_Mshwari.");
//        series_Get_Reversal.setTitle("Get_Reversal.");
//        series_Loan_Limit.setTitle("Loan_Limit.");
        series_Sent.setTitle("Sent.");
//        series_Sent_Mini.setTitle("Sent_Mini.");
//        series_Sent_Mshwari.setTitle("Sent_Mshwari.");
//        series_Sent_Cancel.setTitle("Sent_Cancel.");
//        series_Error_Failed.setTitle("Error_Failed.");
//        series_Error_Pay_Merchant.setTitle("Error_Pay_Merchant.");
//        series_Error_Pin.setTitle("Error_Pin.");
//        series_Error_Less.setTitle("Error_Less.");
//        series_Error_Receiver.setTitle("Error_Receiver.");
//        series_Error_Receiver_Org.setTitle("Error_Receiver_Org.");
        series_Withdraw.setTitle("Withdraw.");
//        series_Fuliza_Leave.setTitle("Fuliza_Leave.");
//        series_Fuliza_Opt_In.setTitle("Fuliza_Opt_In.");
//        series_Fuliza_Limit.setTitle("Fuliza_Limit.");
//        series_Fuliza_Mini_Statement.setTitle("Fuliza_Mini_Statement.");
//        series_Fuliza_Loan_Taken.setTitle("Fuliza_Loan_Taken.");
//        series_Similar_Transaction.setTitle("Similar_Transaction.");
        series_All.setTitle("All.");
        series_Unknown.setTitle("Unknown.");

        series_Get_Receive.setAnimated(true);
//        series_Get_Bank.setAnimated(true);
//        series_Get_Mshwari.setAnimated(true);
//        series_Get_from_NCBA.setAnimated(true);
//        series_Get_from_IM.setAnimated(true);
        series_Get_Bal.setAnimated(true);
//        series_Get_Bal_KCB.setAnimated(true);
//        series_Get_Bal_Mshwari.setAnimated(true);
//        series_Get_Reversal.setAnimated(true);
//        series_Loan_Limit.setAnimated(true);
        series_Sent.setAnimated(true);
//        series_Sent_Mini.setAnimated(true);
//        series_Sent_Mshwari.setAnimated(true);
//        series_Sent_Cancel.setAnimated(true);
//        series_Error_Failed.setAnimated(true);
//        series_Error_Pay_Merchant.setAnimated(true);
//        series_Error_Pin.setAnimated(true);
//        series_Error_Less.setAnimated(true);
//        series_Error_Receiver.setAnimated(true);
//        series_Error_Receiver_Org.setAnimated(true);
        series_Withdraw.setAnimated(true);
//        series_Fuliza_Leave.setAnimated(true);
//        series_Fuliza_Opt_In.setAnimated(true);
//        series_Fuliza_Limit.setAnimated(true);
//        series_Fuliza_Mini_Statement.setAnimated(true);
//        series_Fuliza_Loan_Taken.setAnimated(true);
//        series_Similar_Transaction.setAnimated(true);
        series_All.setAnimated(true);
        series_Unknown.setAnimated(true);

        series_Get_Receive.setDrawDataPoints(true);
//        series_Get_Bank.setDrawDataPoints(true);
//        series_Get_Mshwari.setDrawDataPoints(true);
//        series_Get_from_NCBA.setDrawDataPoints(true);
//        series_Get_from_IM.setDrawDataPoints(true);
        series_Get_Bal.setDrawDataPoints(true);
//        series_Get_Bal_KCB.setDrawDataPoints(true);
//        series_Get_Bal_Mshwari.setDrawDataPoints(true);
//        series_Get_Reversal.setDrawDataPoints(true);
//        series_Loan_Limit.setDrawDataPoints(true);
        series_Sent.setDrawDataPoints(true);
//        series_Sent_Mini.setDrawDataPoints(true);
//        series_Sent_Mshwari.setDrawDataPoints(true);
//        series_Sent_Cancel.setDrawDataPoints(true);
//        series_Error_Failed.setDrawDataPoints(true);
//        series_Error_Pay_Merchant.setDrawDataPoints(true);
//        series_Error_Pin.setDrawDataPoints(true);
//        series_Error_Less.setDrawDataPoints(true);
//        series_Error_Receiver.setDrawDataPoints(true);
//        series_Error_Receiver_Org.setDrawDataPoints(true);
        series_Withdraw.setDrawDataPoints(true);
//        series_Fuliza_Leave.setDrawDataPoints(true);
//        series_Fuliza_Opt_In.setDrawDataPoints(true);
//        series_Fuliza_Limit.setDrawDataPoints(true);
//        series_Fuliza_Mini_Statement.setDrawDataPoints(true);
//        series_Fuliza_Loan_Taken.setDrawDataPoints(true);
//        series_Similar_Transaction.setDrawDataPoints(true);
        series_All.setDrawDataPoints(true);
        series_Unknown.setDrawDataPoints(true);

        graph_line.addSeries(series_Get_Receive);
//        graph_line.addSeries(series_Get_Bank);
//        graph_line.addSeries(series_Get_Mshwari);
//        graph_line.addSeries(series_Get_from_NCBA);
//        graph_line.addSeries(series_Get_from_IM);
        graph_line.addSeries(series_Get_Bal);
//        graph_line.addSeries(series_Get_Bal_KCB);
//        graph_line.addSeries(series_Get_Bal_Mshwari);
//        graph_line.addSeries(series_Get_Reversal);
//        graph_line.addSeries(series_Loan_Limit);
        graph_line.addSeries(series_Sent);
//        graph_line.addSeries(series_Sent_Mini);
//        graph_line.addSeries(series_Sent_Mshwari);
//        graph_line.addSeries(series_Sent_Cancel);
//        graph_line.addSeries(series_Error_Failed);
//        graph_line.addSeries(series_Error_Pay_Merchant);
//        graph_line.addSeries(series_Error_Pin);
//        graph_line.addSeries(series_Error_Less);
//        graph_line.addSeries(series_Error_Receiver);
//        graph_line.addSeries(series_Error_Receiver_Org);
        graph_line.addSeries(series_Withdraw);
//        graph_line.addSeries(series_Fuliza_Leave);
//        graph_line.addSeries(series_Fuliza_Opt_In);
//        graph_line.addSeries(series_Fuliza_Limit);
//        graph_line.addSeries(series_Fuliza_Mini_Statement);
//        graph_line.addSeries(series_Fuliza_Loan_Taken);
//        graph_line.addSeries(series_Similar_Transaction);
        graph_line.addSeries(series_All);
        graph_line.addSeries(series_Unknown);

        graph_line.getViewport().setMinX(1);
        graph_line.getViewport().setMaxX(countar);
        graph_line.getViewport().setXAxisBoundsManual(true);
    }

    private void graph_one_summaries(SummaryLootResponse summaryLootResponse, String sms_category) {
        Frag_Graph.this.summaryLootList = new ArrayList<Mod_Loot_Summary>(Arrays.asList(summaryLootResponse.getLootSummarizer()));

        int counter = 0;
        int countar = Frag_Graph.this.summaryLootList.size();

        DataPoint point_all;

        graph_line.removeAllSeries();

        series_All = new LineGraphSeries<>();

        series_All.setColor(getResources().getColor(R.color.red));
        graph_line.setVisibility(View.VISIBLE);

        graph_line.getViewport().setScalable(true);
        graph_line.getViewport().setScrollable(true);
        graph_line.getViewport().setScalableY(true);
        graph_line.getViewport().setScrollableY(true);
        graph_line.getLegendRenderer().setVisible(true);

        for (Mod_Loot_Summary sumlootlist : Frag_Graph.this.summaryLootList) {
            counter++;
            Double double_sel_string = Double.valueOf(sumlootlist.setVal_sel(sms_category));
            point_all = new DataPoint(counter, double_sel_string);
            series_All.appendData(point_all, true, countar);
        }

        series_All.setTitle(sms_category);
        series_All.setAnimated(true);
        series_All.setDrawDataPoints(true);

        graph_line.addSeries(series_All);

        graph_line.getViewport().setMinX(1);
        graph_line.getViewport().setMaxX(countar);
        graph_line.getViewport().setXAxisBoundsManual(true);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        sms_category_selected = arr_categories[position];
        if (stored_SummaryLootResponse == null) {
            //Toast.makeText(getActivity(), "Unknown error please try again", Toast.LENGTH_LONG).show();
        } else {
            if (sms_category_selected == "All") {
                graph_all_summaries(stored_SummaryLootResponse);
            } else {
                graph_one_summaries(stored_SummaryLootResponse, sms_category_selected);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.e(kon.TAGGED, "Nothing Selected");
    }
}