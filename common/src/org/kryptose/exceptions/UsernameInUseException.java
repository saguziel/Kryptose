package org.kryptose.exceptions;

/**
 * Attempt to create an account with a username that is already used by an account.
 * @author jshi
 */
public class UsernameInUseException extends RecoverableException {
	
	public UsernameInUseException() {
	}

	public UsernameInUseException(String message) {
		super(message);
	}
}
