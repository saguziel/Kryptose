package org.kryptose.exceptions;

/**
 * Represents any error message sent by the server to the client.
 * @author jshi
 */
public class ServerException extends Exception {

	public ServerException() {
		this.setStackTrace(null);
	}

	public ServerException(String message) {
		super(message);
		this.setStackTrace(null);
	}

	// Do not reveal stack traces to client.
	/*
	public ServerException(ServerException cause) {
		super(cause);
	}

	public ServerException(String message, ServerException cause) {
		super(message, cause);
	}

	public ServerException(String message, ServerException cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}*/

}
