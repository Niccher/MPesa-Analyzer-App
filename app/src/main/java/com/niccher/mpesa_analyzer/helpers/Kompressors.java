package com.niccher.mpesa_analyzer.helpers;

import android.util.Log;

import com.niccher.mpesa_analyzer.konstants.Konstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Kompressors {

    static Konstants kon = new Konstants();

    public static File FileKompress(String file_input_path, String file_output_path) throws IOException, FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(file_output_path);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        try {
            File fileToZip = new File(file_input_path);
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }

//            zipOut.close();
//            fis.close();
//            fos.close();
            fis.close();
            //fos.close();
            Log.e(kon.TAGGED, "FileKompress: Compressing "+file_input_path+" to "+file_output_path+" finishing" );
        } catch (IOException e) {
            Log.e(kon.TAGGED, "FileKompress: Compressing "+file_input_path+" to "+file_output_path+" error " + e.getMessage() );
            e.printStackTrace();
        } finally {
            Log.e(kon.TAGGED, "FileKompress: Compressing "+file_input_path+" to "+file_output_path+" done." );
            zipOut.close();
            //fis.close();
            fos.close();
        }

        return new File(String.valueOf(fos));
    }
}
