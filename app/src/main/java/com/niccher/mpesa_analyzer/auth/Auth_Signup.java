package com.niccher.mpesa_analyzer.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.niccher.mpesa_analyzer.BuildConfig;
import com.niccher.mpesa_analyzer.MainActivity;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.interfaces.JsonAuthUser;
import com.niccher.mpesa_analyzer.interfaces.JsonFonePrint;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_User_Auth;

import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.GregorianCalendar;
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

public class Auth_Signup extends AppCompatActivity {

    Button btn_signup, btn_proceed;
    TextView btn_signin;
    EditText reg_name, reg_eml, reg_pwd;

    private JsonAuthUser jsonAuthUser;
    Konstants kon;

    Gson gson = null;

    SharedPreferences pref_Auth = null;
    SharedPreferences.Editor sharedEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        btn_signup = findViewById(R.id.btn_sign_register);
        btn_signin = findViewById(R.id.id_back_login);
        btn_proceed = findViewById(R.id.btn_sign_proceed);

        reg_name = findViewById(R.id.ed_name);
        reg_eml = findViewById(R.id.ed_email);
        reg_pwd = findViewById(R.id.ed_pwd);

        kon = new Konstants();

        pref_Auth = getSharedPreferences(kon.shared_auth_token, Context.MODE_PRIVATE);

        gson = new GsonBuilder()
                .setLenient()
                .create();

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reg_names = reg_name.getText().toString().trim();
                String reg_emls = reg_eml.getText().toString().trim();
                String reg_pwds = reg_pwd.getText().toString().trim();

                if (reg_names.isEmpty()) {
                    Toast.makeText(Auth_Signup.this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                }
                if (reg_pwds.isEmpty()) {
                    Toast.makeText(Auth_Signup.this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                }

                if (reg_emls.isEmpty()) {
                    Toast.makeText(Auth_Signup.this, "Email field cannot be empty", Toast.LENGTH_SHORT).show();
                }else{
                    if (!Patterns.EMAIL_ADDRESS.matcher(reg_emls).matches() && !reg_names.isEmpty() && !reg_pwds.isEmpty()) {
                        Toast.makeText(Auth_Signup.this, "Email is not valid, please enter a valid email", Toast.LENGTH_SHORT).show();
                    }

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(kon.upload_auth_url)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .client(getUnsafeOkHttpClient())
                            .build();

                    jsonAuthUser = retrofit.create(JsonAuthUser.class);
                    createUser(reg_names,reg_emls,reg_pwds);

                    Log.e(kon.TAGGED, "onClick: Email as " + reg_emls);
                    Log.e(kon.TAGGED, "onClick: Name as " + reg_names);
                    Log.e(kon.TAGGED, "onClick: Passwd as " + reg_pwds);
                }
            }
        });

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Auth_Signup.this, Auth_Signin.class);
                startActivity(intent);
                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);
            }
        });

        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Auth_Signup.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);
            }
        });

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

    private void createUser(String new_name, String new_eml, String new_pwd) {
        sharedEditor = pref_Auth.edit();

        Map<String, String> parameters = new HashMap<>();
        parameters.put("varUsername", new_name);
        parameters.put("varEmail", new_eml);
        parameters.put("varPassword", new_pwd);

        Call<Mod_User_Auth> call = jsonAuthUser.createRegister(parameters);

        call.enqueue(new Callback<Mod_User_Auth>() {
            @Override
            public void onResponse(Call<Mod_User_Auth> call, Response<Mod_User_Auth> response) {

                Mod_User_Auth postResponse = response.body();
                String message, status, time, userid;

                if (postResponse.getMessage().isEmpty() || postResponse.getMessage().isEmpty() || postResponse.getMessage().isEmpty()){

                }else {
                    message = postResponse.getMessage();
                    status = postResponse.getStatus();
                    time = postResponse.getTime();
                    userid = postResponse.getUserid();

                    try {
                        if (status.equals("0") || status.equals('2')){
                            Toast.makeText(Auth_Signup.this, message, Toast.LENGTH_LONG).show();
                        }else {
                            sharedEditor.putString("status", status);
                            sharedEditor.putString("message", message);
                            sharedEditor.putString("time", time);
                            sharedEditor.putString("userid", userid);
                            sharedEditor.apply();

                            Intent to_home = new Intent(Auth_Signup.this, MainActivity.class);
                            to_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);
                            startActivity(to_home);
                            Auth_Signup.this.finish();
                        }
                    }catch (Exception ex){
                        Toast.makeText(Auth_Signup.this,  ex.getMessage()+"\nUnknown error occurred", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Mod_User_Auth> call, Throwable t) {
                Toast.makeText(Auth_Signup.this,  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
            }
        });
    }
}
