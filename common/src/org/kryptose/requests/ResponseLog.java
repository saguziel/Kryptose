package org.kryptose.requests;

import org.kryptose.exceptions.ServerException;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseLog extends Response {

	// TODO: is digest allowed to be null? if not, that needs to be checked,
	// both during construction and during deserialization.
	private final byte[] digest;

    public ResponseLog(byte[] digest) {
    	super();
        this.digest = digest.clone();
    }

    public ResponseLog(ServerException exception) {
    	super(exception);
    	assert exception != null;
        this.digest = null;
    }

    public byte[] getDigest() throws ServerException {
    	// Is this an exception response? Throw exception if so.
    	if (this.getException() != null) {
    		throw this.getException();
    	}
    	// Return digest.
        return digest;
    }

    public String logEntry() {
    	if (this.getException() != null) {
    		return "RESPONSE: " + this.getException().toString();
    	}
        return "RESPONSE: user log request successful\n";
    }
}
