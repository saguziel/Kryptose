package org.kryptose.exceptions;

/**
 * Attempt to create an account with a username that is already used by an account.
 * @author jshi
 */
public class UsernameInUseException extends RecoverableException {
	private static final long serialVersionUID = 8852692548941445753L;

	public UsernameInUseException() {
	}

	public UsernameInUseException(String message) {
		super(message);
	}
}
