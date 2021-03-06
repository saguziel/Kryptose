package org.kryptose.exceptions;

/**
 * For when a blob write is rejected because of being out-of-date.
 * 
 * @author jshi
 */
public class StaleWriteException extends Exception {

	public StaleWriteException() {
	}

	public StaleWriteException(String message) {
		super(message);
	}

}
