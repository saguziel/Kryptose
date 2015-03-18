package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class RequestGet extends Request {
	
    public RequestGet(User u) {
        super(u);
        this.validateInstance();
    }
    
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Check that our invariants are satisfied
        this.validateInstance();
    }

	@Override
	public void validateInstance() {
		super.validateInstance();
	}
    
}
