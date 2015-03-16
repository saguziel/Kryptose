package org.kryptose.client;
import org.kryptose.requests.Blob;

import java.util.ArrayList;

/**
 * Created by jeff on 3/15/15.
 */
public class PasswordFile {

    ArrayList<Credential> credentials;
    String username;

    public PasswordFile(String user, Blob b, String masterpass) throws BadBlobException {
        credentials = decryptBlob(masterpass, b);
        this.username = user;
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
    // returns true if value overwritten, false if new val inserted
    public Boolean setVal(String dom, String newVal){
        for(Credential c : credentials) {
            if (c.getDomain() == dom) {
                c.setPassword(newVal);
                return true;
            }
        }
        credentials.add(new Credential(username, newVal, dom));
        return false;
    }
    public Boolean delVal(String dom){
        int toRem = -1;
        for(int i = 0; i<credentials.size(); i++){
            if(credentials.get(i).getDomain().equals(dom)){
                
            }
        }
        
        //TODO: fixme
        return false;
    }
    public ArrayList<Credential> toList(){
        return credentials;
    }

    public class BadBlobException extends Exception {
        public BadBlobException(String message) {
            super(message);
        }
    }
    
    private Blob rawBlobCreate(byte[] raw_data, byte[] raw_key){
    	Blob b = new Blob();
    	b.setBlob(raw_data);
    	return b;
    }
    
    private byte[] rawBlobDecrypt(Blob b, byte[] raw_key){
    	return b.getBlob();
    }
    

}
