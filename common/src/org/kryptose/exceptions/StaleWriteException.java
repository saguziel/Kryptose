package org.kryptose.exceptions;

/**
 * For when a blob write is rejected because of being out-of-date.
 * 
 * @author jshi
 */
public class StaleWriteException extends RecoverableException {
	private static final long serialVersionUID = 8241553780000201919L;
	
	static final String MSG = "There have been conflicting edits to the "
			+ "credentials file since the last time the server was contacted. "
			+ "Please reload the credentials from the server and try again.";

	public StaleWriteException() {
		super();
		this.setStackTrace(new StackTraceElement[0]);
	}

	public StaleWriteException(String message) {
		super(message);
		this.setStackTrace(new StackTraceElement[0]);
	}

}
