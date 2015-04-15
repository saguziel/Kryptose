package org.kryptose.server;

/**
 * Exceptions that should cause the error to halt.
 * @author jshi
 */
public class FatalError extends RuntimeException {

	private static final long serialVersionUID = 6646718244134638801L;

	public FatalError() {
		super();
	}

	public FatalError(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FatalError(String message, Throwable cause) {
		super(message, cause);
	}

	public FatalError(String message) {
		super(message);
	}

	public FatalError(Throwable cause) {
		super(cause);
	}

}
