package org.kryptose.client;
import org.kryptose.requests.Blob;

import java.util.ArrayList;

/**
 * Created by jeff on 3/15/15.
 */
public class PasswordFile {

    ArrayList<Credential> credentials;

    public PasswordFile(Blob b, String masterpass) {
        credentials = decryptBlob(masterpass, b);
    }

    public ArrayList<Credential> decryptBlob(String key, Blob b){
        return null;
    }
    public Blob encryptBlob(String key, ArrayList<Credential> l){
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

    public class BadBlobException extends Exception {
        public BadBlobException(String message) {
            super(message);
        }
    }


}
