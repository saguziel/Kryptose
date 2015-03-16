package org.kryptose.requests;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains all the encrypted information stored here by a single client.
 *
 * @author jshi
 */
public class Blob implements Serializable {

    // TODO: generate serial version UID, after fields are decided.

    private byte[] blob;

    public byte[] getDigest() throws NoSuchAlgorithmException {
    	//Only to prevent a write originated from an outdated file, so more secure algorithms are not necessary.
        return MessageDigest.getInstance("SHA").digest(blob);
    }
    
    //Constructor to create a Blob out of an (encrypted) file. Used only by the server (I think)
    public Blob(String filename) throws IOException{
    	blob = Files.readAllBytes(Paths.get(filename));
    }
    
    //Creates a Blob and puts in encrypted 
    public Blob(byte[] data, byte[] raw_key){
    	blob = (byte[]) data.clone();
    }
    
    public byte[] getDecryptedData(byte[] raw_key){
    	return blob.clone();
    }
    
    public byte[] getEncryptedContent(){
    	return blob.clone();
    }

    
}
