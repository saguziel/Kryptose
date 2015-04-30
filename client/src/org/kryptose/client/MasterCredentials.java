package org.kryptose.client;

import java.util.Arrays;

import javax.security.auth.Destroyable;

import org.kryptose.requests.KeyDerivator;

public class MasterCredentials implements Destroyable {
	private final String username;
	private final char[] password;
	private final byte[] authKey;
	private final byte[] cryptKey;
	
	public MasterCredentials(String username, char[] password) {
		super();
		this.username = username;
		this.password = password;
        this.authKey = KeyDerivator.getAuthenticationKeyBytes(this.username, password);
        this.cryptKey = KeyDerivator.getEncryptionKeyBytes(username, password);
	}

	public String getUsername() {
		return username;
	}

	// exposes internal state, but that's okay
	public char[] getPassword() {
		return password;
	}

	public byte[] getAuthKey() {
		return authKey;
	}

	public byte[] getCryptKey() {
		return cryptKey;
	}

	/**
	 * WARNING: As of java 1.8, does not actually effectively clear
	 *  the password from memory.
	 */
	public void destroy() {
		Arrays.fill(password, ' ');
		Arrays.fill(authKey, (byte)0);
		Arrays.fill(cryptKey, (byte)0);
	}
}
