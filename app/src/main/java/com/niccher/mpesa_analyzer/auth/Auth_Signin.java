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

        pref_Auth = getSharedPreferences(kon.shared_auth_token, Context.MODE_PRIVATE);
        pref_Device = getSharedPreferences(kon.shared_device_id, Context.MODE_PRIVATE);

        gson = new GsonBuilder()
                .setLenient()
                .create();

        Tag_Device();

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
                        createPost();
                    }
                }

                /*Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(kon.upload_auth_url)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .client(getUnsafeOkHttpClient())
                        .build();

                jsonAuthUser = retrofit.create(JsonAuthUser.class);
                createPost();*/
                Log.e(kon.TAGGED, "onClick: Email as " + lg_emls);
                Log.e(kon.TAGGED, "onClick: Passwd as " + lg_pwds);
                /*Intent intent = new Intent(Auth_Signin.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);*/
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
        String pr_board = String.valueOf(Build.BOARD);
        String pr_bootloader = String.valueOf(Build.BOOTLOADER );
        String pr_brand = String.valueOf(Build.BRAND );
        String pr_device = String.valueOf(Build.DEVICE );
        String pr_display = String.valueOf(Build.DISPLAY );
        String pr_finger = String.valueOf(Build.FINGERPRINT );
        String pr_hardware = String.valueOf(Build.HARDWARE );
        String pr_host = String.valueOf(Build.HOST );
        String pr_manufacturer = String.valueOf(Build.MANUFACTURER );
        String pr_model = String.valueOf(Build.MODEL );
        String pr_product = String.valueOf(Build.PRODUCT );
        String pr_tags = String.valueOf(Build.TAGS );
        String pr_type = String.valueOf(Build.TYPE );
        String pr_user = String.valueOf(Build.USER );
        String pr_time = String.valueOf(Build.TIME );

        Map<String, String> parameters = new HashMap<>();
        parameters.put("p_Board", pr_board);
        parameters.put("p_Bootloader", pr_bootloader);
        parameters.put("p_Brand", pr_brand);
        parameters.put("p_Device", pr_device);
        parameters.put("p_Display", pr_display);
        parameters.put("p_Fingerprint", pr_finger);
        parameters.put("p_Hardware", pr_hardware);
        parameters.put("p_Host", pr_host);
        parameters.put("p_Manufacturer", pr_manufacturer);
        parameters.put("p_Model", pr_model);
        parameters.put("p_Product", pr_product);
        parameters.put("p_Tags", pr_tags);
        parameters.put("p_Type", pr_type);
        parameters.put("p_User", pr_user);
        parameters.put("p_Time", pr_time);

        Call<Mod_Fone_Id> call = jsonFonePrint.createPrint(parameters);

        call.enqueue(new Callback<Mod_Fone_Id>() {
            @Override
            public void onResponse(Call<Mod_Fone_Id> call, Response<Mod_Fone_Id> response) {
                Mod_Fone_Id postResponse = response.body();
                String p_id = postResponse.getPd_id();
                String p_status = postResponse.getStatus();
                String p_message = postResponse.getMessage();

                sharedEditor = pref_Device.edit();
                sharedEditor.putString("print_id", postResponse.getPd_id());
                sharedEditor.apply();

                Log.e(kon.TAGGED, "Mod_Fone_Print Assigned Print_id: " + p_id);
                Log.e(kon.TAGGED, "Mod_Fone_Print Assigned p_status: " + p_status);
                Log.e(kon.TAGGED, "Mod_Fone_Print Assigned p_message: " + p_message);
            }

            @Override
            public void onFailure(Call<Mod_Fone_Id> call, Throwable t) {
                Log.e(kon.TAGGED, "Mod_Fone_Print onFailure: " + t.getMessage());
                Toast.makeText(Auth_Signin.this,  t.getMessage()+"\nMod_Fone_Print\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void Tag_Device(){
        Retrofit retrof = new Retrofit.Builder()
                .baseUrl(kon.upload_print)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getUnsafeOkHttpClient())
                .build();

        jsonFonePrint = retrof.create(JsonFonePrint.class);
        createPrint();
    }

}
