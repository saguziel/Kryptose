package org.kryptose.requests;

import org.kryptose.exceptions.ServerException;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseCreateAccount extends Response {


    public ResponseCreateAccount() {
    	super();
    }

    public ResponseCreateAccount(ServerException exception) {
    	super(exception);
    }

    public String logEntry() {
        return "RESPONSE: Get Request Successful\n";
    }

}
