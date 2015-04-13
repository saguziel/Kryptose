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

import org.kryptose.requests.CryptoPrimitiveNotSupportedException;

public class KeyDerivator {
	
	static byte[] appSalt;
	
//	private char[] password;
	
	public static void setAppSalt(String enc_app_salt){
		appSalt =  DatatypeConverter.parseHexBinary(enc_app_salt);
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
	
	
	public static byte[] getAuthenticationKeyBytes(char[] password) throws CryptoPrimitiveNotSupportedException {
		//TODO Securely erease intermediate data. How?
		
		try{
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password, appSalt, 65536, 512);
		
		byte[] raw_key = factory.generateSecret(spec).getEncoded();
		byte[] auth_key = Arrays.copyOfRange(raw_key, 0, 32);

		
		return auth_key;
		}catch(Exception e){
			//TODO Exception Handling
			e.printStackTrace();
		}
		return null;
	}



	public static byte[] getEncryptionKeyBytes(char[] password, byte[] user_salt) throws CryptoPrimitiveNotSupportedException {
		//TODO Securely erease intermediate data. How?
		
		try{
		SecretKeyFactory factory;
			factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password, appSalt, 65536, 512);
		
		byte[] raw_key = factory.generateSecret(spec).getEncoded();
		byte[] enc_key = Arrays.copyOfRange(raw_key, 32, 64);
		
		spec = new PBEKeySpec(DatatypeConverter.printHexBinary(enc_key).toCharArray(), user_salt, 65536, 128);
		
				
		return factory.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new CryptoPrimitiveNotSupportedException(e);
		}
	}

	
	public static void main(String[] args){
		String password = "TestPassword";
		
		byte[] salt = new byte[16];
		try{
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 512);
		
		byte[] raw_key = factory.generateSecret(spec).getEncoded();
		byte[] auth_key = Arrays.copyOfRange(raw_key, 0, 256);
		
		
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
