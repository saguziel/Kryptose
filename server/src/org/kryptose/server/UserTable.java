package org.kryptose.server;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.kryptose.requests.CryptoPrimitiveNotSupportedException;

public class UserTable {
	
	private class UserRecord{
		final String username;
		byte[] salt;
		byte[] auth_key_hash;
		
		//TODO: for final
		int login_attempts;
		
		public UserRecord(String username, byte[] auth_key) {
			this.username = username;
			
			try {
				
				salt = new byte[salt_size];
		    	SecureRandom rnd;

	    		rnd = SecureRandom.getInstance("SHA1PRNG");
		    	rnd.nextBytes(salt);

		    	SecretKeyFactory factory;
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(auth_key).toCharArray(), salt, 65536, 256);
				
				//TODO handle auth_key... perhaps zero it...
				
				auth_key_hash = factory.generateSecret(spec).getEncoded();

		    	

			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new CryptoPrimitiveNotSupportedException(e);
			}
		}
		
		public boolean authenticate(byte[] tentative_key){
			try{
				SecretKeyFactory factory;
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(tentative_key).toCharArray(), salt, 65536, 256);
			
				//TODO: secure erease.
				DatatypeConverter.printHexBinary(auth_key_hash);
				DatatypeConverter.printHexBinary(factory.generateSecret(spec).getEncoded());
				
				return Arrays.equals(auth_key_hash, factory.generateSecret(spec).getEncoded());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new CryptoPrimitiveNotSupportedException(e);
			}

		}

		public boolean changeUserAuthKey(byte[] old_key, byte[] new_key) {
			if(!authenticate(old_key))
				return false;

			try {
				
				salt = new byte[salt_size];
		    	SecureRandom rnd;

	    		rnd = SecureRandom.getInstance("SHA1PRNG");
		    	rnd.nextBytes(salt);

		    	SecretKeyFactory factory;
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(new_key).toCharArray(), salt, 65536, 256);
				
				//TODO handle auth_key... perhaps zero it...
				
				auth_key_hash = factory.generateSecret(spec).getEncoded();

		    	return true;

			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new CryptoPrimitiveNotSupportedException(e);
			}

			
		}

				
	}
	
	int salt_size;
	private ConcurrentHashMap<String,UserRecord> Users;
	
	public enum result{USER_NOT_FOUND, USER_ALREADY_EXISTS, USER_ADDED, WRONG_CREDENTIALS, AUTHENTICATION_SUCCESS, AUTH_KEY_CHANGED};

	public boolean contains(String username){
		return Users.containsKey(username);
	}
	
	public result addUser(String username, byte[] auth_key){
		if (this.contains(username)) 
			return result.USER_ALREADY_EXISTS;
		
		Users.put(username, new UserRecord(username, auth_key));
		return result.USER_ADDED;
	}
	
	public result auth(String username, byte[] auth_key){
		if (!this.contains(username)) 
			return result.USER_NOT_FOUND;
		else if(Users.get(username).authenticate(auth_key))
			return result.AUTHENTICATION_SUCCESS;
		else 
			return result.WRONG_CREDENTIALS;
	}
	
	public result changeAuthKey(String username, byte[] old_key, byte[] new_key){
		if (!this.contains(username)) 
			return result.USER_NOT_FOUND;
		else if(Users.get(username).changeUserAuthKey(old_key,new_key))	
			return result.AUTH_KEY_CHANGED;
		else 
			return result.WRONG_CREDENTIALS;

	}
	
	
	public UserTable(int salt_size) {
		this.salt_size = salt_size;
		Users = new ConcurrentHashMap<String,UserRecord>();
//		Users.put("me", new UserRecord("me", "AAAAAAAAAAAAAAAA", "A"));
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UserTable u = new UserTable(50);
		
		byte[] good_pwd = "good".getBytes();
		byte[] bad_pwd = "bad".getBytes();
		
		System.out.println(u.contains("Antonio"));
		u.addUser("Antonio", good_pwd);
		System.out.println(u.contains("Antonio"));
		System.out.println(u.contains("AntonioAAAA"));
		
		System.out.println(u.auth("Mario", good_pwd));		
		System.out.println(u.auth("Antonio", good_pwd));		
		System.out.println(u.auth("Antonio", bad_pwd));		
		
		System.out.println(u.changeAuthKey("Mario", good_pwd, good_pwd));		
		System.out.println(u.changeAuthKey("Antonio", good_pwd,bad_pwd));		
		System.out.println(u.auth("Antonio", good_pwd));		
		System.out.println(u.auth("Antonio", bad_pwd));
		
		
	}

}
