package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public class ResponseGet extends Response {

    private final Blob blob;
    private final User user;

    public ResponseGet(Blob b, User u) {
        this.blob = b;
        this.user = u;
    }

    public Blob getBlob() {
        return blob;
    }

    public User getUser() {
        return user;
    }
}
