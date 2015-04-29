package org.kryptose.exceptions;

/**
 * Any error sent to the client that's resolvable by an error message and a "please try again".
 * 
 * @author jshi
 */
public class RecoverableException extends Exception {
	private static final long serialVersionUID = 4384928253759960805L;

	public RecoverableException() {
	}

	public RecoverableException(String message) {
		super(message);
	}

}
