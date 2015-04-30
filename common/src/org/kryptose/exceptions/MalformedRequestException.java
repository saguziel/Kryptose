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
	private static final long serialVersionUID = 6166848162362646330L;

	public MalformedRequestException() {
		super();
		this.setStackTrace(new StackTraceElement[0]);
	}

	public MalformedRequestException(String message) {
		super(message);
		this.setStackTrace(new StackTraceElement[0]);
	}

}
