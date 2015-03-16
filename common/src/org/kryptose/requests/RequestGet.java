package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public class RequestGet extends Request {
    private final User user;

    public RequestGet(User u) {
        user = u;
    }

    public User getUser() {
        return user;
    }
}
