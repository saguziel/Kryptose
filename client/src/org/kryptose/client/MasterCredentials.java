package org.kryptose.client;

import java.util.Arrays;

import javax.security.auth.Destroyable;

public class MasterCredentials implements Destroyable {
	private String username;
	private char[] password;
	
	public MasterCredentials(String username, char[] password) {
		super();
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	// exposes internal state, but that's okay
	public char[] getPassword() {
		return password;
	}

	/**
	 * WARNING: As of java 1.8, does not actually effectively clear
	 *  the password from memory.
	 */
	public void destroy() {
		Arrays.fill(password, ' ');
	}
}
