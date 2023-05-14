package com.niccher.mpesa_analyzer.auth;

import android.content.Context;
import android.content.DialogInterface;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.niccher.mpesa_analyzer.BuildConfig;
import com.niccher.mpesa_analyzer.MainActivity;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.helpers.ServiceGenerator;
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

    AlertDialog.Builder builder;
    AlertDialog alertDialog ;

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

        builder = new AlertDialog.Builder(Auth_Signin.this);
        alertDialog = builder.create();

        checkPrint();

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lg_emls = lg_eml.getText().toString().trim();
                String lg_pwds = lg_pwd.getText().toString().trim();

                if (lg_emls.isEmpty() || lg_pwds.isEmpty()) {
                    Toast.makeText(Auth_Signin.this, "Both fields have to be filled", Toast.LENGTH_LONG).show();
                }else{
                    if (!Patterns.EMAIL_ADDRESS.matcher(lg_emls).matches()) {
                        Toast.makeText(Auth_Signin.this, "Email is not valid, please enter a valid email", Toast.LENGTH_LONG).show();
                    }else {
                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(kon.upload_auth_url)
                                .addConverterFactory(GsonConverterFactory.create(gson))
                                .client(ServiceGenerator.getUnsafeOkHttpClient())
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

    private void createPrint() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("device_Board", String.valueOf(Build.BOARD)+"");
        parameters.put("device_Bootloader", String.valueOf(Build.BOOTLOADER)+"");
        parameters.put("device_Brand", String.valueOf(Build.BRAND)+"");
        parameters.put("device_Device", String.valueOf(Build.DEVICE)+"");
        parameters.put("device_Display", String.valueOf(Build.DISPLAY)+"");
        parameters.put("device_Fingerprint", String.valueOf(Build.FINGERPRINT)+"");
        parameters.put("device_Hardware", String.valueOf(Build.HARDWARE)+"");
        parameters.put("device_Host", String.valueOf(Build.HOST)+"");
        parameters.put("device_Manufacturer", String.valueOf(Build.MANUFACTURER)+"");
        parameters.put("device_Model", String.valueOf(Build.MODEL)+"");
        parameters.put("device_Product", String.valueOf(Build.PRODUCT)+"");
        parameters.put("device_Tags", String.valueOf(Build.TAGS)+"");
        parameters.put("device_Type", String.valueOf(Build.TYPE)+"");
        parameters.put("device_User", String.valueOf(Build.USER)+"");
        parameters.put("device_Time", String.valueOf(Build.TIME)+"");
        parameters.put("device_Serial", String.valueOf(Build.SERIAL)+"");

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
                    .client(ServiceGenerator.getUnsafeOkHttpClient())
                    .build();

            jsonFonePrint = retrof.create(JsonFonePrint.class);
            createPrint();
        }
    }

    public void dialogLoading(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Auth_Signin.this);
        builder.setMessage("");
        builder.setTitle("");
        builder.setCancelable(false);
        builder.setNeutralButton("Okay", (DialogInterface.OnClickListener) (dialog, which) -> {
            //dialog.cancel();
        });
        builder.setPositiveButton("Okay1", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void createLogin(String new_eml, String new_pwd) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("varEmail", new_eml);
        parameters.put("varPassword", new_pwd);

        Call<Mod_User_Auth> call = jsonAuthUser.createLogin(parameters);
        //dialogLoading();

        call.enqueue(new Callback<Mod_User_Auth>() {
            @Override
            public void onResponse(Call<Mod_User_Auth> call, Response<Mod_User_Auth> response) {
                Mod_User_Auth postResponse = response.body();
                String message, status, time, userid, uuid;

                if (postResponse.getMessage().isEmpty() || postResponse.getMessage().isEmpty() || postResponse.getMessage().isEmpty()){
                }else {
                    message = postResponse.getMessage();
                    status = postResponse.getStatus();
                    time = postResponse.getTime();
                    userid = postResponse.getUserid();
                    uuid = postResponse.getUuid();

                    try {
                        if (status.equals("0") || status.equals('2')){
                            Toast.makeText(Auth_Signin.this, message, Toast.LENGTH_LONG).show();
                        }else if (status.equals("1")){
                            sharedEditor = pref_Auth.edit();
                            sharedEditor.putString("status", status);
                            sharedEditor.putString("message", message);
                            sharedEditor.putString("time", ""+time);
                            sharedEditor.putString("userid", userid);
                            sharedEditor.putString("uuid", uuid);
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
