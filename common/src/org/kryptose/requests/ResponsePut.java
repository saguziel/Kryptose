package org.kryptose.requests;

import org.kryptose.exceptions.StaleWriteException;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponsePut extends Response {
    
	// TODO: is digest allowed to be null? if not, that needs to be checked,
	// both during construction and during deserialization.
	private final byte[] digest;
	
	private StaleWriteException swex = null;

    public ResponsePut(byte[] digest) {
    	super();
        this.digest = digest.clone();
    }

    public ResponsePut(StaleWriteException exception) {
    	assert exception != null;
    	this.swex = exception;
        this.digest = null;
    }

    public byte[] getDigest() throws StaleWriteException {
    	if (this.swex != null) {
    		throw this.swex;
    	}
        return digest.clone();
    }

    public String logEntry() {
    	if (this.swex != null) {
    		return "RESPONSE: " + this.swex.toString();
    	}
        return "RESPONSE: Put request successful\n";
    }
}
