package com.niccher.mpesa_analyzer.helpers;

import com.niccher.mpesa_analyzer.konstants.Konstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryptor {
    static Konstants kon = new Konstants();

    public static void encodetoFile(String keyStr, String speStr, InputStream in, OutputStream out)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        try {
            IvParameterSpec iv = new IvParameterSpec(speStr.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), kon.string_algo);

            Cipher c = Cipher.getInstance(kon.string_algo_enryptor);
            c.init(Cipher.ENCRYPT_MODE, keySpec, iv);
            out = new CipherOutputStream(out, c);
            int counter = 0;
            byte[] buffa = new byte[kon.string_read_write_block];
            while ((counter= in.read(buffa)) > 0){
                out.write(buffa,0,counter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

    public static void decodetoFile(String keyStr, String speStr, InputStream in, OutputStream out)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        try {
            IvParameterSpec iv = new IvParameterSpec(speStr.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec keySpec = new SecretKeySpec(keyStr.getBytes(StandardCharsets.UTF_8), kon.string_algo);

            Cipher c = Cipher.getInstance(kon.string_algo_enryptor);
            c.init(Cipher.DECRYPT_MODE  , keySpec, iv);
            out = new CipherOutputStream(out, c);
            int counter = 0;
            byte[] buffa = new byte[kon.string_read_write_block];
            while ((counter= in.read(buffa)) > 0){
                out.write(buffa,0,counter);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}
