package org.kryptose.exceptions;

/**
 * Any error sent to the client that's resolvable by an error message and a "please try again".
 * 
 * @author jshi
 */
public class RecoverableException extends Exception {

	public RecoverableException() {
	}

	public RecoverableException(String message) {
		super(message);
	}

}
