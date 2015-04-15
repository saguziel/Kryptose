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
	
	
	public static void setParams(String enc_app_salt, int username_max_length){
		appSalt =  DatatypeConverter.parseHexBinary(enc_app_salt);
		usernameMaxLength = username_max_length;
	}

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


	
}
