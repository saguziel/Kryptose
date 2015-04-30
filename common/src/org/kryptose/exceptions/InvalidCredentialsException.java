package org.kryptose.exceptions;

/**
 * For requests that fail to authenticate.
 * 
 * @author jshi
 */
public class InvalidCredentialsException extends RecoverableException {
	private static final long serialVersionUID = 8327451347907548033L;

	public InvalidCredentialsException() {
		super();
		this.setStackTrace(new StackTraceElement[0]);
	}

	public InvalidCredentialsException(String message) {
		super(message);
		this.setStackTrace(new StackTraceElement[0]);
	}

}
