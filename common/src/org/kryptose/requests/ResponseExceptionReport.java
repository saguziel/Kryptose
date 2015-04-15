package org.kryptose.requests;

import org.kryptose.exceptions.ServerException;

public class ResponseExceptionReport extends Response {

	public ResponseExceptionReport(ServerException ex) {
		super(ex);
	}
	
	@Override
	public String logEntry() {
		// TODO: this is lazy.
		return "RESPONSE: " + this.getException().toString();
	}

}
