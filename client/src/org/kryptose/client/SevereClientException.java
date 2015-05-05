package org.kryptose.client;

public class SevereClientException extends Exception {
	private static final long serialVersionUID = -8176846935327335757L;

	public SevereClientException() {
		super();
	}

	public SevereClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public SevereClientException(String message) {
		super(message);
	}

	public SevereClientException(Throwable cause) {
		super(cause);
	}

}
