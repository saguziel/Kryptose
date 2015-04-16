package org.kryptose.exceptions;

/**
 * For Requests received by the server that the server cannot parse.
 * Is RecoverableException because it's possible that it's just a corrupted bit.
 * 
 * Suggested user-facing message:
 *   Server could not parse the request. Please try again,
 *   or make sure that the Kryptose\u2122 client is up-to-date.
 * @author jshi
 */
public class MalformedRequestException extends RecoverableException {

	public MalformedRequestException() {
		super();
	}

	public MalformedRequestException(String message) {
		super(message);
	}

}
