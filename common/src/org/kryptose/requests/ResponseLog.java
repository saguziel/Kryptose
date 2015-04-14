package org.kryptose.requests;
import java.util.ArrayList;


/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseLog extends Response {


    private final ArrayList<Log> entries;

    public ResponseLog(ArrayList<Log> entries) {
        super();
        this.entries = entries;
    }

    public ResponseLog(ServerException exception) {
        super(exception);
        this.entries = null;
    }

    public ArrayList<Log> getLogs() throws ServerException {
        // Is this an exception response? Throw exception if so.
        if (this.getException() != null) {
            throw this.getException();
        }
        // Return blob.
        return entries;
    }

    public String logEntry() {
        return "RESPONSE: Put Request Successful\n";
    }

}
