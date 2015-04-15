package org.kryptose.exceptions;

/**
 * Sent in a response to represent any internal server error.
 * 
 * That's any error in the server where we don't know what to tell the client,
 * but something is seriously wrong. I mean SERIOUSLY wrong.
 * 
 * Like, we probably shouldn't tell them to try again because of how wrong
 * things are.
 * 
 * @author jshi
 */
public class InternalServerErrorException extends Exception {
	
	public InternalServerErrorException() {
		super();
	}

	public InternalServerErrorException(String message) {
		super(message);
	}
	
	// We don't want to send the client details about the internal server error.
	/*public InternalServerErrorException(Throwable cause) {
		super(cause);
	}

	public InternalServerErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public InternalServerErrorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}*/

}
