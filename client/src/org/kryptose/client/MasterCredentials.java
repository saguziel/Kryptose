package org.kryptose.client;

import javax.security.auth.Destroyable;

import org.kryptose.requests.KeyDerivator;
import org.kryptose.requests.User;

public class MasterCredentials implements Destroyable {
	private final String username;
	private final char[] password;
	private final byte[] authKey;
	private final byte[] cryptKey;
	private final User user;
	
	public MasterCredentials(String username, char[] password) {
		super();
		if (!isValidUsername(username)) {
			throw new IllegalArgumentException("Not a valid username: " + username);
		}
		if (!isValidPassword(password)) {
			throw new IllegalArgumentException("Not a valid password.");
		}
		this.username = username;
		this.password = password.clone();
		Utils.destroyPassword(password);
		
        this.authKey = KeyDerivator.getAuthenticationKeyBytes(this.username, password);
        this.cryptKey = KeyDerivator.getEncryptionKeyBytes(username, password);
		this.user = new User(getUsername(), getAuthKey());
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
	@Override
	public void destroy() {
		Utils.destroyPassword(password);
		Utils.destroyPasskey(authKey);
		Utils.destroyPasskey(cryptKey);
		this.user.destroy();
	}
	
	public static boolean isValidUsername(String username) {
		return User.isValidUsername(username);
	}
	
	public static boolean isValidPassword(char[] password) {
		return password != null && password.length > 0;
	}

	public User getUser() {
		return this.user;
	}
}
