package com.niccher.mpesa_analyzer.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.niccher.mpesa_analyzer.models.Mod_Fone_Id;
import com.niccher.mpesa_analyzer.models.Mod_User_Auth;
import com.niccher.mpesa_analyzer.splash.Splash;

import java.security.cert.CertificateException;
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

public class Auth_Signin extends AppCompatActivity {

    Button btn_signin, btn_signup, btn_proceed;
    EditText lg_eml, lg_pwd;

    private JsonFonePrint jsonFonePrint;
    private JsonAuthUser jsonAuthUser;
    Konstants kon;

    Gson gson = null;

    SharedPreferences pref_Auth = null;
    SharedPreferences pref_Device = null;
    SharedPreferences.Editor sharedEditor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btn_signin = findViewById(R.id.btn_signIn);
        btn_signup = findViewById(R.id.btn_signUp);
        btn_proceed = findViewById(R.id.btn_sign_proceed);

        lg_eml = findViewById(R.id.id_email_EditText);
        lg_pwd = findViewById(R.id.id_password_EditText);

        kon = new Konstants();

        pref_Auth = getSharedPreferences(kon.shared_auth_login, Context.MODE_PRIVATE);
        pref_Device = getSharedPreferences(kon.shared_device_id, Context.MODE_PRIVATE);

        gson = new GsonBuilder()
                .setLenient()
                .create();

        checkPrint();

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lg_emls = lg_eml.getText().toString().trim();
                String lg_pwds = lg_pwd.getText().toString().trim();

                if (lg_emls.isEmpty() || lg_pwds.isEmpty()) {
                    Toast.makeText(Auth_Signin.this, "Both fields have to be filled", Toast.LENGTH_SHORT).show();
                }else{
                    if (!Patterns.EMAIL_ADDRESS.matcher(lg_emls).matches()) {
                        Toast.makeText(Auth_Signin.this, "Email is not valid, please enter a valid email", Toast.LENGTH_SHORT).show();
                    }else {
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(kon.upload_auth_url)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .client(getUnsafeOkHttpClient())
                                .build();

                        jsonAuthUser = retrofit.create(JsonAuthUser.class);
                        createLogin(lg_emls, lg_pwds);
                    }
                }
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Auth_Signin.this, Auth_Signup.class);
                startActivity(intent);
                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);
            }
        });

        btn_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Auth_Signin.this, MainActivity.class);
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

    private void createPrint() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_Board", String.valueOf(Build.BOARD)+"");
        parameters.put("p_Bootloader", String.valueOf(Build.BOOTLOADER)+"");
        parameters.put("p_Brand", String.valueOf(Build.BRAND)+"");
        parameters.put("p_Device", String.valueOf(Build.DEVICE)+"");
        parameters.put("p_Display", String.valueOf(Build.DISPLAY)+"");
        parameters.put("p_Fingerprint", String.valueOf(Build.FINGERPRINT)+"");
        parameters.put("p_Hardware", String.valueOf(Build.HARDWARE)+"");
        parameters.put("p_Host", String.valueOf(Build.HOST)+"");
        parameters.put("p_Manufacturer", String.valueOf(Build.MANUFACTURER)+"");
        parameters.put("p_Model", String.valueOf(Build.MODEL)+"");
        parameters.put("p_Product", String.valueOf(Build.PRODUCT)+"");
        parameters.put("p_Tags", String.valueOf(Build.TAGS)+"");
        parameters.put("p_Type", String.valueOf(Build.TYPE)+"");
        parameters.put("p_User", String.valueOf(Build.USER)+"");
        parameters.put("p_Time", String.valueOf(Build.TIME)+"");

        Call<Mod_Fone_Id> call = jsonFonePrint.createPrint(parameters);

        call.enqueue(new Callback<Mod_Fone_Id>() {
            @Override
            public void onResponse(Call<Mod_Fone_Id> call, Response<Mod_Fone_Id> response) {
                Mod_Fone_Id postResponse = response.body();

                sharedEditor = pref_Device.edit();
                sharedEditor.putString("status", postResponse.getStatus());
                sharedEditor.putString("time", postResponse.getTime());
                sharedEditor.putString("message", postResponse.getMessage());
                sharedEditor.putString("print_id", postResponse.getPrint_id());
                sharedEditor.apply();
            }

            @Override
            public void onFailure(Call<Mod_Fone_Id> call, Throwable t) {
                Log.e(kon.TAGGED, "Mod_Fone_Print onFailure: " + t.getMessage());
                Toast.makeText(Auth_Signin.this,  t.getMessage()+"\nMod_Fone_Print\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void checkPrint(){
        SharedPreferences sharedPreferences = getSharedPreferences(kon.shared_device_id, Context.MODE_PRIVATE);
        String state = sharedPreferences.getString("status","3");
        if (Integer.parseInt(state) == 1){
        }else {
            Retrofit retrof = new Retrofit.Builder()
                    .baseUrl(kon.upload_print)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(getUnsafeOkHttpClient())
                    .build();

            jsonFonePrint = retrof.create(JsonFonePrint.class);
            createPrint();
        }
    }

    private void createLogin(String new_eml, String new_pwd) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("varEmail", new_eml);
        parameters.put("varPassword", new_pwd);

        Call<Mod_User_Auth> call = jsonAuthUser.createLogin(parameters);

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
                            Toast.makeText(Auth_Signin.this, message, Toast.LENGTH_LONG).show();
                        }else if (status.equals("1")){
                            sharedEditor = pref_Auth.edit();
                            sharedEditor.putString("status", status);
                            sharedEditor.putString("message", message);
                            sharedEditor.putString("time", ""+time);
                            sharedEditor.putString("userid", userid);
                            sharedEditor.apply();

                            Intent to_home = new Intent(Auth_Signin.this, MainActivity.class);
                            to_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);
                            startActivity(to_home);
                            Auth_Signin.this.finish();
                        }
                    }catch (Exception ex){
                        Toast.makeText(Auth_Signin.this,  ex.getMessage()+"\nUnknown error occurred", Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void onFailure(Call<Mod_User_Auth> call, Throwable t) {
                Toast.makeText(Auth_Signin.this,  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

}
