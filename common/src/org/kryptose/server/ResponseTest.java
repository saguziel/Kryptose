package org.kryptose.server;

import org.kryptose.requests.Response;

public final class ResponseTest extends Response {
	private final String theResponse;
	
	public ResponseTest(String s) {
		super();
		theResponse = s;
	}
	
	@Override
	public String toString(){
		return theResponse;
	}

    public String logEntry() {
        return "RESPONSE: test\n";
    }
}
