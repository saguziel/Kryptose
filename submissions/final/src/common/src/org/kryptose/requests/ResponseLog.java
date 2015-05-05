package org.kryptose.requests;

import java.util.ArrayList;


/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseLog extends Response {
	private static final long serialVersionUID = 4477428916300635545L;
	
	private final ArrayList<Log> entries;

    public ResponseLog(ArrayList<Log> entries) {
        super();
        this.entries = entries;
    }

    public ArrayList<Log> getLogs() {
        // Return blob.
        return entries;
    }

    public String logEntry() {
        return "RESPONSE: Put Request Successful\n";
    }

}
