package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;


/**
 * Each individual Request sent by the client to the Server.
 * The stub here is just for testing communication.
 *
 * @author Antonio
 */
public final class RequestTest extends Request {
	
	private static final long serialVersionUID = -4863470894643745364L;

	private static final User testUser = new User("testuser", new byte[48]);
	
	private final String theRequest;
	
	public RequestTest(String s) {
		super(testUser);
		theRequest = s;
		this.validateInstance();
	}
	
	@Override
	public String toString(){
		return theRequest;
	}

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Check that our invariants are satisfied
        this.validateInstance();
    }

    @Override
    public void validateInstance() {
        super.validateInstance();
        if (this.theRequest == null) throw new IllegalArgumentException("user is null");
        if (!this.getUser().equals(testUser)) throw new IllegalStateException("user is not testUser");
    }

    public String logEntry() {
        return "REQUEST: Test\n";
    }
}
