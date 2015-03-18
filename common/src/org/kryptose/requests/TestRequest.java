package org.kryptose.requests;



/**
 * Each individual Request sent by the client to the Server.
 * The stub here is just for testing communication.
 * @author Antonio
 *
 */
public final class TestRequest extends Request {
	
	private static final long serialVersionUID = -4863470894643745364L;

	private static final User testUser = new User("testuser");
	
	private final String theRequest;
	
	public TestRequest(String s) {
		super(testUser);
		theRequest = s;
		this.validateInstance();
	}
	
	@Override
	public String toString(){
		return theRequest;
	}

	@Override
	void validateInstance() {
		super.validateInstance();
    	if (this.theRequest == null) throw new IllegalArgumentException("user is null");
    	if (!this.getUser().equals(testUser)) throw new IllegalStateException("user is not testUser");
	}
	
}
