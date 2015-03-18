package org.kryptose.requests;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains all the encrypted information stored here by a single client.
 *
 * @author jshi
 */
public final class Blob implements Serializable {

    // TODO: generate serial version UID, after fields are decided.

    private final byte[] encBytes;
    private final byte[] iv;

/*    
 * 	TODO: remove after server code is finalized.
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
    
    
    public Blob(byte[] data, byte[] iv){
    	this.encBytes = data.clone();
    	this.iv = iv.clone();
    }
    
    public byte[] getEncBytes(){
    	return encBytes.clone();
    }
    
    public byte[] getIv(){
    	return iv.clone();
    }

    public byte[] getDigest() throws CryptoPrimitiveNotSupportedException{
    	//Only to prevent a write originated from an outdated file, so more secure algorithms are not necessary.
        try {
        	MessageDigest md = MessageDigest.getInstance("SHA");
        	md.update(iv);
        	md.update(encBytes);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new CryptoPrimitiveNotSupportedException();
		}
        
    }

	void validateInstance() {
    	if (this.iv == null) throw new IllegalArgumentException("iv is null");
    	if (this.encBytes == null) throw new IllegalArgumentException("encBytes is null");
	}

    
}
