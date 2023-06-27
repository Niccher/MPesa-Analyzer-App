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
import android.widget.ArrayAdapter;
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
import com.niccher.mpesa_analyzer.models.Mod_Sms_Cat_Spinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    ArrayList<Mod_Sms_Cat_Spinner> sms_cat_spin_list;
    List<Mod_Sms_Cat_Spinner> list_smscat = new ArrayList<>();
    Info_Sms_Spinner_adapter info_sms_spinner_adapter;

    String[] arr_categories;
    int[] arr_cat_imgs;

    LineGraphSeries<DataPoint> series_all, series_balance, series_fuliza, series_received, series_sent, series_withdraw, series_wrongpin, series_unknown;
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
        //graph_line1 = (GraphView) grapher.findViewById(R.id.graph1);

        graph_line.setVisibility(View.GONE);
        //graph_line1.setVisibility(View.GONE);

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
        arr_categories = new String[]{"All", "Balance", "Fuliza", "Received", "Sent", "Withdraw", "Wrong Pin", "Unknown"};
        arr_cat_imgs = new int[]{R.mipmap.summ_money_all, R.mipmap.summ_money_balance, R.mipmap.summ_money_fuliza,
                R.mipmap.summ_money_received, R.mipmap.summ_money_sent, R.mipmap.summ_money_withdraw,
                R.mipmap.summ_money_wrong_pin, R.mipmap.summ_money_unknown};

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
                    SummaryLootResponse summaryLootResponse = response.body();
                    summaryLootList = new ArrayList<Mod_Loot_Summary>(Arrays.asList(summaryLootResponse.getLootSummarizer()));

                    int counter = 0;
                    int countar = summaryLootList.size();

                    DataPoint point_all, point_balance, point_fuliza, point_received, point_sent, point_withdraw, point_wrongpin, point_unknown;

                    series_all = new LineGraphSeries<>();
                    series_balance = new LineGraphSeries<>();
                    series_fuliza = new LineGraphSeries<>();
                    series_received = new LineGraphSeries<>();
                    series_sent = new LineGraphSeries<>();
                    series_withdraw = new LineGraphSeries<>();
                    series_wrongpin = new LineGraphSeries<>();
                    series_unknown = new LineGraphSeries<>();

                    series_all.setColor(getResources().getColor(R.color.dark_gray));
                    series_balance.setColor(getResources().getColor(R.color.light_pink));
                    series_fuliza.setColor(getResources().getColor(R.color.yellow));
                    series_received.setColor(getResources().getColor(R.color.pink));
                    series_sent.setColor(getResources().getColor(R.color.green));
                    series_withdraw.setColor(getResources().getColor(R.color.blue));
                    series_wrongpin.setColor(getResources().getColor(R.color.beige));
                    series_unknown.setColor(getResources().getColor(R.color.red));

                    graph_line.setVisibility(View.VISIBLE);
                    //graph_line1.setVisibility(View.VISIBLE);

                    graph_line.getViewport().setScalable(true);
                    graph_line.getViewport().setScrollable(true);
                    graph_line.getViewport().setScalableY(true);
                    graph_line.getViewport().setScrollableY(true);

                    graph_line.getLegendRenderer().setVisible(true);

                    for (Mod_Loot_Summary sumlootlist : summaryLootList) {
                        counter++;

                        point_all = new DataPoint(counter, Double.parseDouble(sumlootlist.val_all));
                        point_balance = new DataPoint(counter, Double.parseDouble(sumlootlist.val_balance));
                        point_fuliza = new DataPoint(counter, Double.parseDouble(sumlootlist.val_fuliza));
                        point_received = new DataPoint(counter, Double.parseDouble(sumlootlist.val_received));
                        point_sent = new DataPoint(counter, Double.parseDouble(sumlootlist.val_sent));
                        point_withdraw = new DataPoint(counter, Double.parseDouble(sumlootlist.val_withdraw));
                        point_wrongpin = new DataPoint(counter, Double.parseDouble(sumlootlist.val_wrong_pin));
                        point_unknown = new DataPoint(counter, Double.parseDouble(sumlootlist.val_unknown));

                        series_all.appendData(point_all, true, countar);
                        series_balance.appendData(point_balance, true, countar);
                        series_fuliza.appendData(point_fuliza, true, countar);
                        series_received.appendData(point_received, true, countar);
                        series_sent.appendData(point_sent, true, countar);
                        series_withdraw.appendData(point_withdraw, true, countar);
                        series_wrongpin.appendData(point_wrongpin, true, countar);
                        series_unknown.appendData(point_unknown, true, countar);
                    }

                    series_all.setTitle("All");
                    series_balance.setTitle("Balances");
                    series_fuliza.setTitle("Fuliza");
                    series_received.setTitle("Received");
                    series_sent.setTitle("Sent");
                    series_withdraw.setTitle("Withdrawn");
                    series_wrongpin.setTitle("Wrong Pin");
                    series_received.setTitle("Received");
                    series_unknown.setTitle("Unknown");

                    series_all.setAnimated(true);
                    series_balance.setAnimated(true);
                    series_fuliza.setAnimated(true);
                    series_received.setAnimated(true);
                    series_sent.setAnimated(true);
                    series_withdraw.setAnimated(true);
                    series_wrongpin.setAnimated(true);
                    series_unknown.setAnimated(true);

                    series_all.setDrawDataPoints(true);
                    series_balance.setDrawDataPoints(true);
                    series_fuliza.setDrawDataPoints(true);
                    series_received.setDrawDataPoints(true);
                    series_sent.setDrawDataPoints(true);
                    series_withdraw.setDrawDataPoints(true);
                    series_wrongpin.setDrawDataPoints(true);
                    series_unknown.setDrawDataPoints(true);

                    graph_line.addSeries(series_all);
                    graph_line.addSeries(series_balance);
                    graph_line.addSeries(series_fuliza);
                    graph_line.addSeries(series_received);
                    graph_line.addSeries(series_sent);
                    graph_line.addSeries(series_withdraw);
                    graph_line.addSeries(series_wrongpin);
                    graph_line.addSeries(series_unknown);

                    graph_line.getViewport().setMinX(1);
                    graph_line.getViewport().setMaxX(countar);

                    //graph.getViewport().setYAxisBoundsManual(true);
                    graph_line.getViewport().setXAxisBoundsManual(true);

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(getActivity(), arr_categories[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.e(kon.TAGGED, "Nothing Selected");
    }
}