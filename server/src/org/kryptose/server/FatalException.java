package org.kryptose.server;

/**
 * Exceptions that should cause the error to halt.
 * @author jshi
 */
public class FatalException extends RuntimeException {

	private static final long serialVersionUID = 6646718244134638801L;

	public FatalException() {
		super();
	}

	public FatalException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FatalException(String message, Throwable cause) {
		super(message, cause);
	}

	public FatalException(String message) {
		super(message);
	}

	public FatalException(Throwable cause) {
		super(cause);
	}

}
