package org.kryptose.exceptions;

//This is probably a problem of the jvm, which does not support the crypto we wanna use.
//Suggested response: quit the program, and ask user to update the jvm.
public class CryptoPrimitiveNotSupportedException extends RuntimeException {
	private static final long serialVersionUID = -9094506097718681573L;

	public CryptoPrimitiveNotSupportedException() {
	}

	public CryptoPrimitiveNotSupportedException(String message) {
		super(message);
	}

	public CryptoPrimitiveNotSupportedException(Throwable cause) {
		super(cause);
	}

	public CryptoPrimitiveNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

	public CryptoPrimitiveNotSupportedException(String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
