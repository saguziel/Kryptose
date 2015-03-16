package org.kryptose.client;
import org.kryptose.requests.Blob;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
    // returns true iff value associated w/ dom successfully deleted
    public Boolean delVal(String dom){
        int toRem = -1;
        for(int i = 0; i<credentials.size(); i++){
            if(credentials.get(i).getDomain().equals(dom)){
                toRem = i;
                break;
            }
        }
        if(toRem >= 0){
            credentials.remove(toRem);
            return true;
        }
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
    
    private static Blob rawBlobCreate(byte[] raw_data, byte[] raw_key){
    	Blob b = new Blob();
    	
		try {
	    	Cipher c;
			c = Cipher.getInstance("AES/GCM/NoPadding");
	    	final int blockSize = c.getBlockSize();
	    	
	    	final byte[] ivData = new byte[blockSize];
	    	final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
	    	rnd.nextBytes(ivData);
	    	
	    	GCMParameterSpec params = new GCMParameterSpec(blockSize * Byte.SIZE, ivData);
	    	
	    	SecretKeySpec sks = new SecretKeySpec(raw_key, "AES");
	    	c.init(Cipher.ENCRYPT_MODE, sks, params);

	    	//byte[] head = "Head".getBytes();
	    	//c.updateAAD(head);
	    	
	    	b.setBlob(c.doFinal(raw_data),ivData);		
		
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return b;
    }
    
    private static byte[] rawBlobDecrypt(Blob b, byte[] raw_key){

		try {
	    	Cipher c;
			c = Cipher.getInstance("AES/GCM/NoPadding");
	    	final int blockSize = c.getBlockSize();
	    	
	    	GCMParameterSpec params = new GCMParameterSpec(blockSize * Byte.SIZE, b.getIv());
	    	
	    	SecretKeySpec sks = new SecretKeySpec(raw_key, "AES");
	    	c.init(Cipher.DECRYPT_MODE, sks, params);

	    	//byte[] head = "Head".getBytes();
	    	//c.updateAAD(head);
	    	
	    	return c.doFinal(b.getEncBytes());		

		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return null;
    }
    
    
    //TODO: for testing only. Remove LATER BY ANTONIO. If it generates errors, just comment it.
    public static void main(String[] args){
    	byte[] myRawKey = new byte[16];
//    	Arrays.fill(myRawKey, (byte) 0);
    	
    	Blob b = rawBlobCreate("EncryptionTestAA".getBytes(), myRawKey);
    	System.out.println("Decrypted: " + new String(rawBlobDecrypt(b, myRawKey)));
    	
    	byte[] raw_ciphertext = b.getEncBytes();
    	byte[] iv = b.getIv();
    	
    	raw_ciphertext[2] = (byte) 0;
    	
    	Blob b_tampered = new Blob();
    	b_tampered.setBlob(raw_ciphertext, iv);
    	System.out.println("Decrypted: " + new String(rawBlobDecrypt(b_tampered, myRawKey)));
    		
    }

}
