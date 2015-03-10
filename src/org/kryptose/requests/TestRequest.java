package org.kryptose.requests;



/**
 * Each individual Request sent by the client to the Server.
 * The stub here is just for testing communication.
 * @author Antonio
 *
 */
public class TestRequest extends Request {

	private static final long serialVersionUID = 7705643898653821811L;
	
	private final String theRequest;
	
	public TestRequest(String s) {
		super();
		theRequest = s;
	}
	
	@Override
	public String toString(){
		return theRequest;
	}

	@Override
	public void run() {
		// Do nothing.
	}
	
	
}
