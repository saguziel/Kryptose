package org.kryptose.client;
import org.kryptose.requests.Blob;

import java.util.ArrayList;

/**
 * Created by jeff on 3/15/15.
 */
public class PasswordFile {

    ArrayList<Credential> credentials;

    public PasswordFile(Blob b, String masterpass) throws BadBlobException {
        credentials = decryptBlob(masterpass, b);
    }

    public ArrayList<Credential> decryptBlob(String passwd, Blob b) throws BadBlobException {
        return null;
    }
    public Blob encryptBlob(String passwd){
        return null;
    }

    public String getVal(String key){
        for(Credential c : credentials){
            if(c.getDomain() == key){
                return c.getPassword();
            }
        }
        return null;
    }
    public ArrayList<Credential> toList(){
        return credentials;
    }

    public class BadBlobException extends Exception {
        public BadBlobException(String message) {
            super(message);
        }
    }


}
