package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public class GetRequest extends Request {
    private final User user;

    public GetRequest(User u) {
        user = u;
    }

    public User getUser() {
        return user;
    }
}
