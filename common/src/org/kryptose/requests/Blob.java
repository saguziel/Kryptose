package org.kryptose.requests;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Contains all the encrypted information stored here by a single client.
 *
 * @author jshi
 */
public class Blob implements Serializable {

    // TODO: generate serial version UID, after fields are decided.

    private byte[] blob;

/*    
    //Constructor to create a Blob out of an (encrypted) file. Used only by the server (I think)
    public Blob(String filename) throws IOException{
    	blob = Files.readAllBytes(Paths.get(filename));
    }
    
    //Creates a Blob and puts in encrypted 
    public Blob(byte[] data, byte[] raw_key){
    	blob = (byte[]) data.clone();
    	
    	Cipher c = Cipher.getInstance ("AES256/GCM/NoPadding");
    	final int blockSize = c.getBlockSize();
    	byte[] ivData = new byte[blockSize];
    	final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
    	rnd.nextBytes(ivData);
    	GCMParameterSpec params = new GCMParameterSpec(blockSize * Byte.SIZE, ivData);
    	SecureRandom sr = new SecureRandom();
    	
    	//byte[] head = "Head".getBytes();
    	//sr.nextBytes(aesKey);
    	
    	SecretKeySpec sks = new SecretKeySpec(raw_key, "AES256");
    	try {
			c.init(Cipher.ENCRYPT_MODE, sks, params);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//c.updateAAD(head);
    	
    	try {
			this.blob = c.doFinal(data);
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }
*/    
    
    public byte[] getBlob(){
    	return blob.clone();
    }
    
    public void setBlob(byte[] data){
    	blob = data.clone();
    }

    public byte[] getDigest(){
    	//Only to prevent a write originated from an outdated file, so more secure algorithms are not necessary.
        try {
			return MessageDigest.getInstance("SHA").digest(blob);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			// If you do not HAVE SHA installed, suggest user to change JVM
			e.printStackTrace();
		}
        return null;
    }

    
}
