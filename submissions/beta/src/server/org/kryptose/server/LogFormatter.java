package org.kryptose.server;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Created by alexguziel on 4/14/15.
 */
public class LogFormatter extends Formatter {

    private Formatter f;
    private byte[] auth_key;
    private Path authfile;

    public LogFormatter(Formatter f, byte[] auth_key) {
        this.f = f;
        this.auth_key = auth_key.clone();
        authfile = null;
    }

    public LogFormatter(Formatter f) {
        this.f = f;
        authfile = FileSystems.getDefault().getPath("", "logPassfile");
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            auth_key = decoder.decode(Files.readAllBytes(authfile));
        } catch (IOException e) {
            System.out.println("Log file may not exist, please use LogReader to create a password for logs");
            // TODO: log error
            throw new FatalError(e);
        }
    }

    public String format(LogRecord lr) {
        String s = f.format(lr);
        StringBuffer sb = new StringBuffer();
        try {
            //create mk
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update("encrypt".getBytes(Charset.forName("UTF-8")));
            md.update(this.auth_key);
            byte[] mk = Arrays.copyOfRange(md.digest(), 0, 128 / 8);

            //encrypt entry with mk
            SecretKeySpec sks = new SecretKeySpec(mk, "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, sks);
            byte[] output = c.doFinal(s.getBytes("UTF-8"));
            Cipher c2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] IV = c.getIV();
            IvParameterSpec iv_spec = new IvParameterSpec(IV);
            c2.init(Cipher.DECRYPT_MODE, sks, iv_spec);
            c2.doFinal(output);

            //create tag with ak
            SecretKeySpec mac_key = new SecretKeySpec(this.auth_key, "AES");
            Mac m = Mac.getInstance("HmacSHA1");
            m.init(mac_key);
            byte[] tag = m.doFinal(output);
            //put results into sb
            sb.append(DatatypeConverter.printHexBinary(IV));
            sb.append("\n");
            sb.append(DatatypeConverter.printHexBinary(output));
            sb.append("\n");
            sb.append(DatatypeConverter.printHexBinary(tag));
            sb.append("\n");
            //iterate ak
            md.reset();
            md.update("iterate".getBytes(Charset.forName("UTF-8")));
            md.update(this.auth_key);
            this.auth_key = md.digest();
            this.auth_key = Arrays.copyOfRange(this.auth_key, 0, 128 / 8);
            Base64.Encoder encoder = Base64.getEncoder();
            try {
                if (authfile != null)
                    Files.write(authfile, encoder.encode(this.auth_key), StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                // TODO: log error
                throw new FatalError(e);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
        return sb.toString();
    }
}
