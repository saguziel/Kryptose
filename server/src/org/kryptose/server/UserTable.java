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

import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.User;

public class UserTable {
	
	private static final int DEFAULT_SALT_SIZE = 50;



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
	
	private final int salt_size;
	private ConcurrentHashMap<String,UserRecord> users;
	
	public enum Result{USER_NOT_FOUND, USER_ALREADY_EXISTS, USER_ADDED, WRONG_CREDENTIALS, AUTHENTICATION_SUCCESS, AUTH_KEY_CHANGED};


	public UserTable() {
		this(DEFAULT_SALT_SIZE);
	}
	
	public UserTable(int salt_size) {
		this.salt_size = salt_size;
		this.users = new ConcurrentHashMap<String,UserRecord>();
//		Users.put("me", new UserRecord("me", "AAAAAAAAAAAAAAAA", "A"));
	}
	
	public boolean contains(String username){
		return users.containsKey(username);
	}
	
	public Result addUser(User user){
		if (this.contains(user.getUsername())) 
			return Result.USER_ALREADY_EXISTS;
		
		users.put(user.getUsername(),
				new UserRecord(user.getUsername(), user.getPasskey()));
		return Result.USER_ADDED;
	}
	
	public Result auth(User user){
		if (!this.contains(user.getUsername())) 
			return Result.USER_NOT_FOUND;
		else if(users.get(user.getUsername()).authenticate(user.getPasskey()))
			return Result.AUTHENTICATION_SUCCESS;
		else 
			return Result.WRONG_CREDENTIALS;
	}
	
	public Result changeAuthKey(String username, byte[] old_key, byte[] new_key){
		if (!this.contains(username)) 
			return Result.USER_NOT_FOUND;
		else if(users.get(username).changeUserAuthKey(old_key,new_key))	
			return Result.AUTH_KEY_CHANGED;
		else 
			return Result.WRONG_CREDENTIALS;

	}

	public static UserTable loadFromFile(String filename) {
		throw new RuntimeException("User Table not yet implemented");
		// TODO: do this.
	}
	
	public void saveToFile(String filename) {
		throw new RuntimeException("User Table not yet implemented");
		// TODO: do this.
	}
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		UserTable u = new UserTable(50);
		
		byte[] good_pwd = "good".getBytes();
		byte[] bad_pwd = "bad".getBytes();
		
		System.out.println(u.contains("Antonio"));
		//u.addUser("Antonio", good_pwd);
		System.out.println(u.contains("Antonio"));
		System.out.println(u.contains("AntonioAAAA"));
		
		//System.out.println(u.auth("Mario", good_pwd));		
		//System.out.println(u.auth("Antonio", good_pwd));		
		//System.out.println(u.auth("Antonio", bad_pwd));		
		
		System.out.println(u.changeAuthKey("Mario", good_pwd, good_pwd));		
		System.out.println(u.changeAuthKey("Antonio", good_pwd,bad_pwd));		
		//System.out.println(u.auth("Antonio", good_pwd));		
		//System.out.println(u.auth("Antonio", bad_pwd));
		
		
	}

}
