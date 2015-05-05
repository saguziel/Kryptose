package org.kryptose.requests;

import org.kryptose.exceptions.UsernameInUseException;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseCreateAccount extends Response {
	private static final long serialVersionUID = -1189422938339598496L;

	private UsernameInUseException uiuex = null;
	
    public ResponseCreateAccount() {
    	super();
    }

    public ResponseCreateAccount(UsernameInUseException exception) {
    	super();
    	this.uiuex = exception;
    }
    
    public void verifySuccessful() throws UsernameInUseException {
    	if (this.uiuex != null) throw this.uiuex;
    }

    public String logEntry() {
    	if (this.uiuex != null) {
    		return "RESPONSE: " + this.uiuex.toString();
    	}
        return "RESPONSE: Get Request Successful\n";
    }

}
