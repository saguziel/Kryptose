package org.kryptose.client;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.junit.internal.ArrayComparisonFailure;
import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;

public class KeyDerivator {
	
	static byte[] appSalt;
	
	static int usernameMaxLength;
	
	static final char[] appName = "Kryptose:".toCharArray();
	
//	private char[] password;
	
	public static void setParams(String enc_app_salt, int username_max_length){
		appSalt =  DatatypeConverter.parseHexBinary(enc_app_salt);
		usernameMaxLength = username_max_length;
	}
	
	
	
/*	
	public void zeroPassword() {
		for(int i = 0; i<password.length;i++)
			password[i] = 0;
	}

	public void setPassword(char[] pwd) {
		this.password = pwd.clone();
		for(int i = 0; i<pwd.length;i++)
			pwd[i] = 0;
	}
*/
	
	
	public static byte[] getAuthenticationKeyBytes(String username, char[] password) throws CryptoPrimitiveNotSupportedException {
			return Arrays.copyOfRange(computeRawKey(username, password), 0, 32);	
	}

	public static byte[] getEncryptionKeyBytes(String username, char[] password) throws CryptoPrimitiveNotSupportedException {
		return Arrays.copyOfRange(computeRawKey(username, password), 32, 48);	
	}

	private static byte[] computeRawKey(String username, char[] password){
		try{
			char[] s = new char[appName.length + usernameMaxLength + password.length];
			
			Arrays.fill(s, '#');
			System.arraycopy(appName, 0, s, 0, appName.length);
			System.arraycopy(username.toCharArray(), 0, s, appName.length, username.length());
			System.arraycopy( password, 0, s, usernameMaxLength + appName.length, password.length);
	
			SecretKeyFactory factory;
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(s, appSalt, 65536, 256+128);
						
			return factory.generateSecret(spec).getEncoded();
		}catch(InvalidKeySpecException | NoSuchAlgorithmException e){
			e.printStackTrace();
			throw new CryptoPrimitiveNotSupportedException(e);
		}
		
		
	}


/*
	public static byte[] getEncryptionKeyBytes(String username, char[] password) throws CryptoPrimitiveNotSupportedException {
		//TODO Securely erease intermediate data. How?
		
		try{
			char[] s = new char[appName.length + usernameMaxLength + password.length];
			
			Arrays.fill(s, '#');
			System.arraycopy(appName, 0, s, 0, appName.length);
			System.arraycopy(username.toCharArray(), 0, s, appName.length, username.length());
			System.arraycopy( password, 0, s, usernameMaxLength + appName.length, password.length);

			SecretKeyFactory factory;
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(s, appSalt, 65536, 512);
		
		byte[] raw_key = factory.generateSecret(spec).getEncoded();
		byte[] enc_key = Arrays.copyOfRange(raw_key, 32, 48);
		
		//spec = new PBEKeySpec(DatatypeConverter.printHexBinary(enc_key).toCharArray(), user_salt, 65536, 128);
		//return factory.generateSecret(spec).getEncoded();
		return enc_key;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoPrimitiveNotSupportedException(e);
		}
	}
*/
	
	public static void main(String[] args){
	
		try{
/*		
		return auth_key;

		
		// Encrypt the message. 
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secret);
		AlgorithmParameters params = cipher.getParameters();
		byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		byte[] ciphertext = cipher.doFinal("Hello, World!".getBytes("UTF-8"));
		Now send the ciphertext and the iv to the recipient. The recipient generates a SecretKey in exactly the same way, using the same salt and password. Then initialize the cipher with the key and the initialization vector.

		// Decrypt the message, given derived key and initialization vector. 
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
		String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
		System.out.println(plaintext);
*/
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
