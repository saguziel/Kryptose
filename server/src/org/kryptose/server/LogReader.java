package org.kryptose.server;

import org.kryptose.requests.KeyDerivator;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by alexguziel on 4/14/15.
 */
public class LogReader {
    private byte[] auth_key;
    private byte[] current_key;
    private int key_iteration;


    public LogReader(byte[] auth_key) {
        this.auth_key = auth_key;
        this.current_key = auth_key;
        this.key_iteration = 0;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter CREATE to create password file or READ to read logs");
        String choice = in.readLine();
        switch (choice) {
            case "CREATE":
                System.out.println("Enter password");
                KeyDerivator.setParams("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", 0);
                byte[] authkey = Arrays.copyOf(KeyDerivator.getAuthenticationKeyBytes("", password.toCharArray()), 128);
                Path p = FileSystems.getDefault().getPath("", "logPassfile")
        }
        String password = in.readLine();

        KeyDerivator.setParams("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", 0);
        byte[] authkey = Arrays.copyOf(KeyDerivator.getAuthenticationKeyBytes("", password.toCharArray()), 128);



    }

    public String[] decrypt(String[] entries) {

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        SecretKeySpec sks;
        Cipher c;
        Base64.Decoder decoder = Base64.getDecoder();

        String[] results = new String[entries.length];

        for (int i = 0; i < entries.length; i++) {
            String[] split_s = entries[i].split("\n");
            if (split_s.length < 3) {
                continue;
            }
            byte[][] things = new byte[3][];
            for (int j = 0; j < split_s.length; j++) {
                things[j] = DatatypeConverter.parseHexBinary(split_s[j]);
            }


            md.reset();
            md.update("encrypt".getBytes(Charset.forName("UTF-8")));
            md.update(this.auth_key);
            byte[] mk = Arrays.copyOfRange(md.digest(), 0, 128 / 8);
            sks = new SecretKeySpec(mk, "AES");
            byte[] output = null;
            byte[] tag = null;
            try {
                //need to add IV
                c = Cipher.getInstance("AES/CBC/PKCS5Padding");
                IvParameterSpec ivspec = new IvParameterSpec(things[0]);
                c.init(Cipher.DECRYPT_MODE, sks, ivspec);
                output = c.doFinal(things[1]);

                SecretKeySpec mac_key = new SecretKeySpec(this.auth_key, "AES");
                Mac m = Mac.getInstance("HmacSHA1");
                m.init(mac_key);
                tag = m.doFinal(things[1]);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                    InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
                System.out.println(e.getMessage());
            }
            if (Arrays.equals(tag, things[2])) {
                results[i] = new String(output, Charset.forName("UTF-8"));
            } else {
                System.out.println("Tag mismatch");
            }


            md.reset();
            md.update("iterate".getBytes(Charset.forName("UTF-8")));
            md.update(this.auth_key);
            this.auth_key = md.digest();
            this.auth_key = Arrays.copyOfRange(this.auth_key, 0, 128 / 8);

        }
        return results;
    }

    public void destroy() {
        auth_key = null;
        current_key = null;
        key_iteration = -1;
    }

}
