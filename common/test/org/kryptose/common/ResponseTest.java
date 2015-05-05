package org.kryptose.common;

import org.kryptose.requests.Response;

public final class ResponseTest extends Response {
	private static final long serialVersionUID = -1804481405619059996L;

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
