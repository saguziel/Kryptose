package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Created by jeff on 4/14/15.
 */
public final class RequestLog extends Request {

    public RequestLog(User u) {
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


    @Override
    public String logEntry() {
        return String.format("REQUEST: Get log from %s\n", super.getUser().getUsername());
    }
}
