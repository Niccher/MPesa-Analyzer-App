package com.niccher.mpesa_analyzer.frags;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.niccher.mpesa_analyzer.BuildConfig;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_adapter;
import com.niccher.mpesa_analyzer.helpers.SummaryResponse;
import com.niccher.mpesa_analyzer.interfaces.JsonProcesses;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_Summaries;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Frag_History extends Fragment {

    RecyclerView recyclerView;
    TextView conn_state;
    ProgressBar conn_wait;

    JsonProcesses jsonProcesses;
    Konstants kon;
    Gson gson = null;
    ArrayList<Mod_Summaries> summariesList;
    Info_adapter summariesAdapter;

    AppCompatActivity activity;

    public Frag_History() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kon = new Konstants();

        gson = new GsonBuilder()
                .setLenient()
                .create();

        activity = (AppCompatActivity) getActivity();
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Upload History");
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_history, container, false);

        recyclerView = view.findViewById(R.id.recy_history);
        conn_state = view.findViewById(R.id.conn_no_internet);
        conn_wait  = view.findViewById(R.id.conn_wait_internet);
        conn_wait.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        if (isConnected()){
            conn_wait.setVisibility(View.VISIBLE);
            conn_state.setVisibility(View.GONE);
            getSummaries();
        }else {
            conn_wait.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            isOffline();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isConnected()){
            conn_wait.setVisibility(View.VISIBLE);
            conn_state.setVisibility(View.GONE);
            getSummaries();
        }else {
            conn_wait.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            isOffline();
        }
    }

    public boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void isOffline(){
        Toast.makeText(activity, "No Internet", Toast.LENGTH_LONG).show();

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

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            if(BuildConfig.DEBUG) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            }else{
                logging.setLevel(HttpLoggingInterceptor.Level.NONE);
            }

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory);
            builder.addInterceptor(logging);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void getSummaries() {

        conn_wait.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.upload_summaries)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getUnsafeOkHttpClient())
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
                    summariesAdapter = new Info_adapter(summariesList, getActivity());
                    recyclerView.setAdapter(summariesAdapter);
                    summariesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<SummaryResponse> call, Throwable t) {
                conn_wait.setVisibility(View.GONE);
                Toast.makeText(getActivity(),  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
                Log.e(kon.TAGGED, "**********************: onFailure Unknown error occurred, please try again");
                Log.e(kon.TAGGED, t.getMessage());
            }
        });
    }
}