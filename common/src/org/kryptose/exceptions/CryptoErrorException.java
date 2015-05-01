package org.kryptose.exceptions;

//This exception is a generic Cryptographic problem. 
// It also includes the case where a blob has been tampered with (the Authenticated Encryption verification fails).
//Suggested response: Interrupt the operation in progress, and warn the user???.

public class CryptoErrorException extends Exception {
	private static final long serialVersionUID = -2511565660933462965L;

	public CryptoErrorException() {
	}

	public CryptoErrorException(String message) {
		super(message);
	}

	public CryptoErrorException(Throwable cause) {
		super(cause);
	}

	public CryptoErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public CryptoErrorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
