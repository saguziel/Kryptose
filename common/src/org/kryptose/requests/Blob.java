package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;

/**
 * Represents all the encrypted information stored by a single client on the server.
 * Also includes initialization vector used in encryption.
 *
 * Instances of this class are immutable (as are all other objects in org.kryptose.requests).
 *
 * @author jshi
 */
public final class Blob implements Serializable {
	private static final long serialVersionUID = -304040678350247434L;
	
	// Encrypted content.
    private byte[] encBytes;
    // Initialization vector for encryption.
    private byte[] iv;
    
    
    public Blob(byte[] encBytes, byte[] iv){ 
    	this.encBytes = encBytes.clone();
    	this.iv = iv.clone();

    }
        
    public byte[] getEncBytes(){
    	return encBytes.clone();
    }
    
    public byte[] getIv(){
    	return iv.clone();
    }

    /**
     * Gets a digest of the encrypted blob.
     * @return A SHA-1 message digest for the encrypted blob.
     * @throws CryptoPrimitiveNotSupportedException
     */
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
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Check that our invariants are satisfied
        this.validateInstance();
    }

    // Check object invariants and do defensive copying.
	void validateInstance() {
    	if (this.iv == null) throw new IllegalArgumentException("iv is null");
    	if (this.encBytes == null) throw new IllegalArgumentException("encBytes is null");
    	
    	this.iv = iv.clone();
    	this.encBytes = encBytes.clone();

	}

	public String toString() {
		//For debugging purposes
		return Arrays.toString(encBytes) + "\n" + Arrays.toString(iv);
	}
}
