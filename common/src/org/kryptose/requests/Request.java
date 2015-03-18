package org.kryptose.requests;

import java.io.Serializable;

public abstract class Request implements Serializable {

	private final User user;

	public Request(User user) {
		super();
		this.user = user;
		this.validateInstance();
	}

	public final User getUser() {
		// TODO: consider security implications of this being public.
	    return user;
	}
    
    void validateInstance() {
    	if (this.user == null) throw new IllegalArgumentException("user is null");
    	this.user.validateInstance();
    }

}