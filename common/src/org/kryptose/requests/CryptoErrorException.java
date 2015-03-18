package org.kryptose.requests;

//This exception is a generic Cryptographic problem. 
// It also includes the case where a blob has been tampered with (the Authenticated Encryption verification fails).
//Suggested response: Interrupt the operation in progress, and warn the user???.

public class CryptoErrorException extends Exception {

	public CryptoErrorException() {
		// TODO Auto-generated constructor stub
	}

	public CryptoErrorException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CryptoErrorException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public CryptoErrorException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CryptoErrorException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
