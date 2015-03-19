package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public abstract class Request implements Serializable {

    private final User user;

    public Request(User user) {
        super();
        this.user = user;
    }

    public final User getUser() {
        // TODO: consider security implications of this being public.
        return user;
    }

    /*private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Check that our invariants are satisfied
        this.validateInstance();
    }*/

    /**
     * Checks that the instance's fields satisfy specified invariants
     * and defensively copies mutable fields.
     */
    public void validateInstance() {
        if (this.user == null) throw new IllegalArgumentException("user is null");
        this.user.validateInstance();
    }

    public abstract String logEntry();

}