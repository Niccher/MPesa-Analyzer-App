package com.niccher.mpesa_analyzer.frags;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_adapter;
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

public class Frag_Graph extends Fragment {

    GraphView graph_line, graph_line1;
    AppCompatActivity activity;
    TextView conn_state;
    ProgressBar conn_wait;

    JsonProcesses jsonProcesses;
    Konstants kon;
    Gson gson = null;
    ArrayList<Mod_Summaries> summariesList;

    LineGraphSeries<DataPoint> series_sent, series_received, series_unknown;
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
            supportActionBar.setTitle("More Info");
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
        setHasOptionsMenu(true);

        kon = new Konstants();

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
        conn_wait  = grapher.findViewById(R.id.conn_wait_internet);
        conn_wait.setVisibility(View.GONE);

        graph_line = (GraphView) grapher.findViewById(R.id.graph);
        graph_line1 = (GraphView) grapher.findViewById(R.id.graph1);

        graph_line.setVisibility(View.GONE);
        graph_line1.setVisibility(View.GONE);

        graph_line.setTitle("Category of SMS");
        getConnectionState();

        return grapher;
    }

    public void getConnectionState(){
        if (isConnected()){
            conn_wait.setVisibility(View.VISIBLE);
            conn_state.setVisibility(View.GONE);
            getSummaries();
        }else {
            conn_wait.setVisibility(View.GONE);
            isOffline("No internet connection at the moment.");
        }
    }

    public boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void isOffline(String mgs){
        graph_line.setVisibility(View.GONE);
        graph_line1.setVisibility(View.GONE);
        conn_state.setText(mgs);
    }

    private String get_prefs_auth(String ty){
        String id = "";
        if (ty=="auth"){
            SharedPreferences pref_auth = getActivity().getSharedPreferences(kon.shared_auth_login, Context.MODE_PRIVATE);
            id = pref_auth.getString("userid", "nullable");
        }
        if (ty=="print"){
            SharedPreferences pref_dev_id = getActivity().getSharedPreferences(kon.shared_device_id, Context.MODE_PRIVATE);
            id = pref_dev_id.getString("print_id", "nullable");
        }
        return id;
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
        parameters.put("varUsername", get_prefs_auth("auth"));
        parameters.put("varEmail", get_prefs_auth("print"));

        Call <SummaryResponse> call = jsonProcesses.getSummary(parameters);
        call.enqueue(new Callback<SummaryResponse>() {
            @Override
            public void onResponse(Call<SummaryResponse> call, Response<SummaryResponse> response) {
                conn_wait.setVisibility(View.GONE);
                if(response.isSuccessful() && response.body()!=null){
                    SummaryResponse summaryResponse = response.body();
                    summariesList = new ArrayList<>(Arrays.asList(summaryResponse.getSummarizer()));
                    //summariesAdapter = new Info_adapter(summariesList, getActivity());
                    int counter = 0;
                    int countar = summariesList.size();

                    DataPoint point_sent, point_received, point_unknown;

                    series_sent = new LineGraphSeries<>();
                    series_received = new LineGraphSeries<>();
                    series_unknown = new LineGraphSeries<>();

                    series_sent.setColor(Color.CYAN);
                    series_received.setColor(Color.RED);
                    series_unknown.setColor(Color.BLUE);

                    graph_line.setVisibility(View.VISIBLE);
                    graph_line1.setVisibility(View.VISIBLE);

                    for (Mod_Summaries sumlist: summariesList) {
                        counter++;
                        point_sent = new DataPoint(counter, Double.parseDouble(sumlist.summary_Sent));
                        point_received = new DataPoint(counter, Double.parseDouble(sumlist.summary_Received));
                        point_unknown = new DataPoint(counter, Double.parseDouble(sumlist.summary_Unknown));

                        series_sent.appendData(point_sent, true, countar);
                        series_received.appendData(point_received, true, countar);
                        series_unknown.appendData(point_unknown, true, countar);
                    }

                    series_sent.setTitle("Sent");
                    series_received.setTitle("Received");
                    series_unknown.setTitle("Unknown");

                    series_sent.setAnimated(true);
                    series_received.setAnimated(true);
                    series_unknown.setAnimated(true);

                    graph_line.getLegendRenderer().setVisible(true);
                    //graph_line.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

                    graph_line.addSeries(series_sent);
                    graph_line.addSeries(series_received);
                    graph_line.addSeries(series_unknown);

                }
            }

            @Override
            public void onFailure(Call<SummaryResponse> call, Throwable t) {
                conn_wait.setVisibility(View.GONE);
                isOffline("Unknown error has occurred, please try again");
                //Toast.makeText(getActivity(),  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
                Log.e(kon.TAGGED, "**********************: onFailure Unknown error occurred, please try again");
                Log.e(kon.TAGGED, t.getMessage());
            }
        });
    }
}