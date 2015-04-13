package org.kryptose.server;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Hashtable;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.kryptose.requests.CryptoPrimitiveNotSupportedException;

public class UserTable {
	
	private class UserRecord{
		final String username;
		String salt;
		String auth_key_hash;
		
		//TODO: for final
		int login_attempts;
		
		public UserRecord(String username, byte[] auth_key) {
			this.username = username;
			
			try {
				
				final byte[] salt = new byte[salt_size];
		    	SecureRandom rnd;

	    		rnd = SecureRandom.getInstance("SHA1PRNG");
		    	rnd.nextBytes(salt);

		    	SecretKeyFactory factory;
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(auth_key).toCharArray(), salt, 65536, 256);
				
				//TODO handle auth_key... perhaps zero it...
				
				byte[] auth_key_hash = factory.generateSecret(spec).getEncoded();

		    	

			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new CryptoPrimitiveNotSupportedException(e);
			}
		}

				
	}
	
	int salt_size;
	private Hashtable<String,UserRecord> Users;
	
	public enum result{USER_NOT_FOUND, USER_ALREADY_EXISTS, USER_ADDED, WRONG_CREDENTIALS, AUTHENTICATION_SUCCESS};

	public boolean contains(String username){
		return Users.containsKey(username);
	}
	
	public result addUser(String username, byte[] auth_key){
		if this.contains(username) return USER_ALREADY_EXISTS;
		
		
	}
	
	
	public UserTable(int salt_size) {
		this.salt_size = salt_size;
		Users = new Hashtable<String,UserRecord>();
//		Users.put("me", new UserRecord("me", "AAAAAAAAAAAAAAAA", "A"));
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UserTable u = new UserTable();
	}

}
