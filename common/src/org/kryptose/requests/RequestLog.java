package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A request to get the blob stored by this user.
 * <p>
 * Created by alexguziel on 3/15/15.
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
        return String.format("REQUEST: Log from %s\n", super.getUser().getUsername());
    }
}