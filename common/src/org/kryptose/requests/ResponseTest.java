package org.kryptose.requests;

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

}
