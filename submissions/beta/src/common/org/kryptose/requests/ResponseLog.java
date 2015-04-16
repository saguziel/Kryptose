package org.kryptose.requests;

import java.util.ArrayList;
import org.kryptose.exceptions.*;


/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseLog extends Response {


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
