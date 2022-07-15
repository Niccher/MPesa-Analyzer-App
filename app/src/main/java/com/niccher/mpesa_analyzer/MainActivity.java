package com.niccher.mpesa_analyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;

import com.niccher.mpesa_analyzer.frags.Frag_Graph;
import com.niccher.mpesa_analyzer.frags.Frag_History;
import com.niccher.mpesa_analyzer.frags.Frag_Home;
import com.niccher.mpesa_analyzer.frags.Frag_Info;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    MeowBottomNavigation meow;
    int perm_sms = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meow = findViewById(R.id.meow_nav);

        meow.add(new MeowBottomNavigation.Model(1, R.drawable.ic_home));
        meow.add(new MeowBottomNavigation.Model(2, R.drawable.ic_graph));
        meow.add(new MeowBottomNavigation.Model(3, R.drawable.ic_history));
        meow.add(new MeowBottomNavigation.Model(4, R.drawable.ic_info));

        meow.show(1, true);
        changeFragment(new Frag_Home());

        meow.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                switch (model.getId()){
                    case 1:
                        changeFragment(new Frag_Home());
                        break;
                    case 2:
                        changeFragment(new Frag_Graph());
                        break;
                    case 3:
                        changeFragment(new Frag_History());
                        break;
                    case 4:
                        changeFragment(new Frag_Info());
                        break;
                }
                return null;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reqPermission(Manifest.permission.READ_SMS, perm_sms);
    }

    @Override
    protected void onStart() {
        super.onStart();
        reqPermission(Manifest.permission.READ_SMS, perm_sms);
    }

    public void changeFragment(Fragment new_frag){
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.frame, new_frag);
        trans.commit();
    }

    public void reqPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == perm_sms) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
            else {
                Toast.makeText(MainActivity.this, "Permissions regarding SMS needs to be granted for the app to work as designed", Toast.LENGTH_LONG).show();
            }
        }
    }
}