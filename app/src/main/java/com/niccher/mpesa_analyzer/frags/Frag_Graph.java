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

    LineGraphSeries<DataPoint> series_Get_from_MPESA, series_Get_from_Mshwari, series_Get_from_NCBA, series_Get_from_KCB, series_Get_from_IM, series_Get_from_Reversal,
            series_Get_Bal_MPESA, series_Get_Bal_KCB, series_Get_Bal_Mshwari, series_Loan_Limit,
            series_Sent_to_MPESA, series_Sent_Mini, series_Sent_to_Mshwari, series_Sent_to_LNM, series_Sent_Cancel,
            series_Error_Failed, series_Error_Pin, series_Error_Less, series_Error_Receiver, series_Error_Receiver_Org, series_Withdraw,
            series_Fuliza_Opt_Out, series_Fuliza_Opt_In, series_Fuliza_Limit, series_Fuliza_Loan_Paid, series_Fuliza_Mini_Statement, series_Fuliza_Loan_Taken,
            series_Similar_Transaction, series_Unknown, series_All
            ;
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
        arr_categories = new String[]{"All",
                "Get from MPESA",  "Get from KCB",  "Get from Mshwari",  "Get from NCBA",  "Get from IM",  "Get from Reversal",
                "Bal MPESA",  "Bal KCB",  "Bal Mshwari",
                "Loan Limit",
                "Sent to MPESA",  "Sent to Mshawri",  "Sent to LNM",  "Sent Mini Statement",  "Sent Canceled",  "Withdrawals",
                "Error Failed",  "Error Pin",  "Error Less",  "Error Receiver",  "Error Receiver Org",
                "Fuliza Leave",  "Fuliza Opt In",  "Fuliza Limit",  "Fuliza Loan Paid",  "Fuliza Mini Statement",  "Fuliza Loan Taken",
                "Similar Transaction",  "Unknown", };

        arr_cat_imgs = new int[]{R.mipmap.summ_money_all, R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza,
                R.mipmap.summ_money_received, R.mipmap.summ_money_sent, R.mipmap.summ_money_withdraw,
                R.mipmap.summ_money_wrong_pin, R.mipmap.summ_money_unknown, R.mipmap.summ_money_all,

                R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza, R.mipmap.summ_money_received,
                R.mipmap.summ_money_sent, R.mipmap.summ_money_withdraw, R.mipmap.summ_money_wrong_pin,
                R.mipmap.summ_money_unknown, R.mipmap.summ_money_all, R.mipmap.summ_money_balance,

                R.mipmap.summ_money_fuliza, R.mipmap.summ_money_received, R.mipmap.summ_money_sent,
                R.mipmap.summ_money_withdraw, R.mipmap.summ_money_wrong_pin, R.mipmap.summ_money_unknown,
                R.mipmap.summ_money_all, R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza,

                R.mipmap.summ_money_received, R.mipmap.summ_money_sent,R.mipmap.summ_money_withdraw,
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

        DataPoint point_Get_from_MPESA, point_Get_from_Mshwari, point_Get_from_NCBA, point_Get_from_KCB, point_Get_from_IM, point_Get_from_Reversal,
                point_Get_Bal_MPESA, point_Get_Bal_KCB, point_Get_Bal_Mshwari, point_Loan_Limit,
                point_Sent_to_MPESA, point_Sent_Mini, point_Sent_to_Mshwari, point_Sent_to_LNM, point_Sent_Cancel,
                point_Error_Failed, point_Error_Pin, point_Error_Less, point_Error_Receiver, point_Error_Receiver_Org,
                point_Withdraw,
                point_Fuliza_Opt_Out, point_Fuliza_Opt_In, point_Fuliza_Limit, point_Fuliza_Loan_Paid, point_Fuliza_Mini_Statement, point_Fuliza_Loan_Taken,
                point_Similar_Transaction, point_Unknown, point_All;

        graph_line.removeAllSeries();

        series_Get_from_MPESA = new LineGraphSeries<>();
        series_Get_from_Mshwari = new LineGraphSeries<>();
        series_Get_from_NCBA = new LineGraphSeries<>();
        series_Get_from_KCB = new LineGraphSeries<>();
        series_Get_from_IM = new LineGraphSeries<>();
        series_Get_from_Reversal = new LineGraphSeries<>();
        series_Get_Bal_MPESA = new LineGraphSeries<>();
        series_Get_Bal_KCB = new LineGraphSeries<>();
        series_Get_Bal_Mshwari = new LineGraphSeries<>();
        series_Loan_Limit = new LineGraphSeries<>();
        series_Sent_to_MPESA = new LineGraphSeries<>();
        series_Sent_Mini = new LineGraphSeries<>();
        series_Sent_to_Mshwari = new LineGraphSeries<>();
        series_Sent_to_LNM = new LineGraphSeries<>();
        series_Sent_Cancel = new LineGraphSeries<>();
        series_Error_Failed = new LineGraphSeries<>();
        series_Error_Pin = new LineGraphSeries<>();
        series_Error_Less = new LineGraphSeries<>();
        series_Error_Receiver = new LineGraphSeries<>();
        series_Error_Receiver_Org = new LineGraphSeries<>();
        series_Withdraw = new LineGraphSeries<>();
        series_Fuliza_Opt_Out = new LineGraphSeries<>();
        series_Fuliza_Opt_In = new LineGraphSeries<>();
        series_Fuliza_Limit = new LineGraphSeries<>();
        series_Fuliza_Loan_Paid = new LineGraphSeries<>();
        series_Fuliza_Mini_Statement = new LineGraphSeries<>();
        series_Fuliza_Loan_Taken = new LineGraphSeries<>();
        series_Similar_Transaction = new LineGraphSeries<>();
        series_Unknown = new LineGraphSeries<>();
        series_All = new LineGraphSeries<>();

        series_Get_from_MPESA.setColor(getResources().getColor(R.color.red));
        series_Get_Bal_MPESA.setColor(getResources().getColor(R.color.bg_green_light));
        series_Sent_to_MPESA.setColor(getResources().getColor(R.color.alice_blue));
        series_Error_Pin.setColor(getResources().getColor(R.color.yellow));
        series_Withdraw.setColor(getResources().getColor(R.color.cyan));
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

            point_Get_from_MPESA  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_from_MPESA));
            point_Get_Bal_MPESA  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Get_Bal_MPESA));
            point_Sent_to_MPESA  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Sent_to_MPESA));
            point_Withdraw  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Withdraw));
            point_All  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_All));
            point_Unknown  = new DataPoint(counter, Double.parseDouble(sumlootlist.count_Unknown));

            series_Get_from_MPESA.appendData(point_Get_from_MPESA, true, countar);
            series_Get_Bal_MPESA.appendData(point_Get_Bal_MPESA, true, countar);
            series_Sent_to_MPESA.appendData(point_Sent_to_MPESA, true, countar);
            series_Withdraw.appendData(point_Withdraw, true, countar);
            series_All.appendData(point_All, true, countar);
            series_Unknown.appendData(point_Unknown, true, countar);
        }

        series_Get_from_MPESA.setTitle("Receive from MPESA.");
        series_Get_Bal_MPESA.setTitle("Bal MPESA.");
        series_Sent_to_MPESA.setTitle("Sent to MPESA.");
        series_Withdraw.setTitle("Withdraw.");
        series_All.setTitle("All");
        series_Unknown.setTitle("Unknown");

        series_Get_from_MPESA.setAnimated(true);
        series_Get_Bal_MPESA.setAnimated(true);
        series_Sent_to_MPESA.setAnimated(true);
        series_Withdraw.setAnimated(true);
        series_All.setAnimated(true);
        series_Unknown.setAnimated(true);

        series_Get_from_MPESA.setDrawDataPoints(true);
        series_Get_Bal_MPESA.setDrawDataPoints(true);
        series_Sent_to_MPESA.setDrawDataPoints(true);
        series_Withdraw.setDrawDataPoints(true);
        series_All.setDrawDataPoints(true);
        series_Unknown.setDrawDataPoints(true);

        graph_line.addSeries(series_Get_from_MPESA);
        graph_line.addSeries(series_Get_Bal_MPESA);
        graph_line.addSeries(series_Sent_to_MPESA);
        graph_line.addSeries(series_Withdraw);
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
            Toast.makeText(getActivity(), "Category as "+sms_category_selected, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.e(kon.TAGGED, "Nothing Selected");
    }
}