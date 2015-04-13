package org.kryptose.requests;

import org.kryptose.exceptions.MalformedRequestException;

public class ResponseMalformedRequest extends Response {

	public ResponseMalformedRequest() {
		super(new MalformedRequestException());
	}

	@Override
	public String logEntry() {
		// TODO: this is lazy.
		return "RESPONSE: " + this.getException().toString();
	}

}
