package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class RequestGet extends Request {
	
    public RequestGet(User u) {
        super(u);
        this.validateInstance();
    }

	@Override
	void validateInstance() {
		super.validateInstance();
	}
    
}
