package org.kryptose.requests;

//This is probably a problem of the jvm, which does not support the crypto we wanna use.
//Suggested response: quit the program, and ask user to update the jvm.
public class CryptoPrimitiveNotSupportedException extends Exception {

	public CryptoPrimitiveNotSupportedException() {
		// TODO Auto-generated constructor stub
	}

	public CryptoPrimitiveNotSupportedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CryptoPrimitiveNotSupportedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public CryptoPrimitiveNotSupportedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CryptoPrimitiveNotSupportedException(String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
