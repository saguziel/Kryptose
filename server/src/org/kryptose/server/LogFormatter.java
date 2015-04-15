package org.kryptose.server;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by alexguziel on 4/14/15.
 */
public class LogFormatter extends Formatter {

    private Formatter f;
    private byte[] auth_key;

    public LogFormatter(Formatter f, byte[] auth_key) {
        this.f = f;
        this.auth_key = auth_key;
    }

    public String format(LogRecord lr) {
        String s = f.format(lr);
        StringBuffer sb = new StringBuffer();
        try {
            //create mk
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update("encrypt".getBytes(Charset.forName("UTF-8")));
            md.update(this.auth_key);
            byte[] mk = md.digest();

            //encrypt entry with mk
            SecretKeySpec sks = new SecretKeySpec(mk, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, sks);
            c.update(s.getBytes(Charset.forName("UTF-8")));
            byte[] output = c.doFinal();

            //create tag with ak
            SecretKeySpec mac_key = new SecretKeySpec(this.auth_key, "AES");
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(mac_key);
            m.update(output);
            byte[] tag = m.doFinal();

            //put results into sb
            sb.append(c.getIV());
            sb.append("\n");
            sb.append(output);
            sb.append("\n");
            sb.append(tag);
            sb.append("\n");

            //iterate ak
            md.reset();
            md.update("iterate".getBytes(Charset.forName("UTF-8")));
            md.update(this.auth_key);
            this.auth_key = md.digest();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("shit");
        }
        return sb.toString();
    }
}
