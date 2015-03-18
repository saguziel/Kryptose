package org.kryptose.client;

import org.kryptose.requests.CryptoPrimitiveNotSupportedException;

public class KeyDerivator {

	public static byte[] getEncryptionKeyBytes(String pass) throws CryptoPrimitiveNotSupportedException {
		// TODO Implement an actual Key Derivation function
		// At the moment, all the encryptions are done using the key 0.
		return new byte[16];
	}

	public static byte[] getAuthenticationKeyBytes(String pass) throws CryptoPrimitiveNotSupportedException {
		// TODO Implement an actual Key Derivation function
		return pass.getBytes();
	}

}
