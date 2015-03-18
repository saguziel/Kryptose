package org.kryptose.requests;

public final class TestResponse extends Response {
	private final String theResponse;
	
	public TestResponse(String s) {
		super();
		theResponse = s;
	}
	
	@Override
	public String toString(){
		return theResponse;
	}

}
