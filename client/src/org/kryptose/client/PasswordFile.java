package org.kryptose.client;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.Blob;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by jeff on 3/15/15.
 */
public class PasswordFile {

    ArrayList<Credential> credentials;
    LocalDateTime timestamp;

    String username;
    byte[] oldDigest;

    public PasswordFile(String username, Blob b, String pass) throws BadBlobException, CryptoPrimitiveNotSupportedException, CryptoErrorException {
        decryptBlob(b, username, pass);
        this.username = username;
    }

    public PasswordFile(String user) {
        this.username = user;
        this.timestamp = LocalDateTime.now();
        this.credentials = new ArrayList<Credential>();
    }

    public byte[] getOldDigest(){
    	if (oldDigest == null) return null;
        return oldDigest.clone();
    }

    public void setOldDigest(byte[] digest){
    	if (digest == null) {
    		this.oldDigest = null;
    	} else {
    		this.oldDigest = digest.clone();
    	}
    }

    public void decryptBlob(Blob b, String username, String pass) throws BadBlobException, CryptoPrimitiveNotSupportedException, CryptoErrorException {
    	byte[] raw_key = KeyDerivator.getEncryptionKeyBytes(username, pass.toCharArray());
        
    	
        byte[] decrypted = rawBlobDecrypt(b, raw_key);
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(decrypted);
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            credentials = (ArrayList<Credential>) objStream.readObject();
            timestamp = (LocalDateTime) objStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new BadBlobException("Bad blob");
        }
    }

    //TODO: use correct timestamp and iv
    public Blob encryptBlob(String username, String pass, LocalDateTime lastmod) throws BadBlobException, CryptoPrimitiveNotSupportedException, CryptoErrorException {
        
    	/*
    	byte[] salt = new byte[64];
    	SecureRandom rnd;
		try {
			rnd = SecureRandom.getInstance("SHA1PRNG");
			rnd.nextBytes(salt);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			throw new CryptoPrimitiveNotSupportedException(e1);
		}
    	*/

    	
    	byte[] raw_key = KeyDerivator.getEncryptionKeyBytes(username, pass.toCharArray());

        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(credentials);
            objStream.writeObject(lastmod);
            objStream.flush();
            byte[] bytes = byteStream.toByteArray();
            objStream.close();

            return rawBlobCreate(bytes, raw_key);

        } catch (IOException e) {
            e.printStackTrace();
            throw new BadBlobException("Bad blob");
        }
    }

    public String getVal(String key){
        for(Credential c : credentials){
            if(c.getDomain().equals(key)){
                return c.getPassword();
            }
        }
        return null;
    }

    // returns true if value overwritten, false if new val inserted
    public Boolean setVal(String dom, String user, String pass){
        for(Credential c : credentials) {
            if (c.getDomain().equals(dom) && c.getUsername().equals(user)) {
                c.setPassword(pass);
                return true;
            }
        }
        credentials.add(new Credential(user, pass, dom));
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
    
    private static Blob rawBlobCreate(byte[] raw_data, byte[] raw_key) throws CryptoPrimitiveNotSupportedException, CryptoErrorException{
    	Blob b;
    	
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
	    	
	    	b = new Blob(c.doFinal(raw_data),ivData);		
		
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new CryptoPrimitiveNotSupportedException(e);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			throw new CryptoErrorException(e);
		}
    	
    	return b;
    }
    
    private static byte[] rawBlobDecrypt(Blob b, byte[] raw_key) throws CryptoPrimitiveNotSupportedException, CryptoErrorException{

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
			throw new CryptoPrimitiveNotSupportedException(e);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			//Note: this can be due to a tampered blob or malicious attack
			throw new CryptoErrorException(e);
		}
    }
    

}
