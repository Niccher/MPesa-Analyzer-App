package com.niccher.mpesa_analyzer.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.niccher.mpesa_analyzer.MainActivity;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.auth.Auth_Signin;
import com.niccher.mpesa_analyzer.auth.Auth_Signup;
import com.niccher.mpesa_analyzer.konstants.Konstants;

public class Splash extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private int progressStatus = 0;
    Konstants kon;

    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mProgressBar = (ProgressBar) findViewById (R.id.progress_bar);

        kon = new Konstants();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startloading();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startloading();
    }

    private void startloading() {
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 4;
                    handler.post(new Runnable() {
                        public void run() {
                            mProgressBar.setProgress(progressStatus);
                            if (progressStatus == 100){
                                Intent to_validate = new Intent(Splash.this, Auth_Signin.class);
                                Intent to_home = new Intent(Splash.this, MainActivity.class);
                                String state = checkValidity();
                                if (Integer.parseInt(state) == 1 ){
                                    to_home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(to_home);
                                    Splash.this.finish();
                                }else {
                                    to_validate.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(to_validate);
                                    Splash.this.finish();
                                }
                            }
                        }
                    });
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    public String checkValidity(){
        SharedPreferences sharedPreferences = getSharedPreferences(kon.shared_auth_login, Context.MODE_PRIVATE);
        String status = sharedPreferences.getString("status","3");
        return status;
    }

}

