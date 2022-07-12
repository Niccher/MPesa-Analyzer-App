package com.niccher.mpesa_analyzer.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.niccher.mpesa_analyzer.MainActivity;
import com.niccher.mpesa_analyzer.R;

public class Auth_Signin extends AppCompatActivity {

    Button btn_signin, btn_signup, btn_proceed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btn_signin = findViewById(R.id.btn_signIn);
        btn_signup = findViewById(R.id.btn_signUp);
        btn_proceed = findViewById(R.id.btn_sign_proceed);

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Auth_Signin.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.from_right_in, R.anim.from_left_out);
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

}
