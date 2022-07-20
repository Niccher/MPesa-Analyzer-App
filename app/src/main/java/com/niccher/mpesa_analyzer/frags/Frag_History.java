package com.niccher.mpesa_analyzer.frags;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.niccher.mpesa_analyzer.BuildConfig;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_adapter;
import com.niccher.mpesa_analyzer.interfaces.JsonProcesses;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_Summaries;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private RecyclerView recyclerView;

    JsonProcesses jsonProcesses;
    Konstants kon;
    Gson gson = null;
    ArrayList<Mod_Summaries> summariesList = new ArrayList<>();
    Info_adapter summariesAdapter;

    public Frag_History() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        kon = new Konstants();

        gson = new GsonBuilder()
                .setLenient()
                .create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_history, container, false);

        recyclerView = view.findViewById(R.id.recy_history);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        summariesAdapter = new Info_adapter(summariesList, getActivity());
        recyclerView.setAdapter(summariesAdapter);
        getSummaries();
        return view;
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.upload_summaries)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getUnsafeOkHttpClient())
                .build();

        jsonProcesses = retrofit.create(JsonProcesses.class);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("varUsername", get_prefs_auth("auth"));
        parameters.put("varEmail", get_prefs_auth("print"));

        Call<Mod_Summaries> call = jsonProcesses.getSummary(parameters);
        call.enqueue(new Callback<Mod_Summaries>() {
            @Override
            public void onResponse(Call<Mod_Summaries> call, Response<Mod_Summaries> response) {
                if(response.isSuccessful() && response.body()!=null){
                    summariesList.addAll(Collections.singletonList(response.body()));
                    summariesAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<Mod_Summaries> call, Throwable t) {
                Toast.makeText(getActivity(),  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
                Log.e(kon.TAGGED, "**********************: onFailure Unknown error occurred, please try again");
                Log.e(kon.TAGGED, t.getMessage());
            }
        });
    }
}