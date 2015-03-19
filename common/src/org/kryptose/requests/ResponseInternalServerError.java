package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseInternalServerError extends Response {

    public String logEntry() {
    	//TODO: Maybe extend internal server error to include a more specific message in addition to this generic one.
    	// Such a message could be sent to the client as well.
        return "RESPONSE: Internal Server Error\n";
    }
}
