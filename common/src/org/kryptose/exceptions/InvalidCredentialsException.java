package org.kryptose.exceptions;

/**
 * For requests that fail to authenticate.
 * 
 * @author jshi
 */
public class InvalidCredentialsException extends RecoverableException {

	public InvalidCredentialsException() {
	}

	public InvalidCredentialsException(String message) {
		super(message);
	}

}
