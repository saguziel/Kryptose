package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseStaleWrite extends Response {

    private final User user;
    private final byte[] oldDigest;
    private final byte[] newDigest;

    public ResponseStaleWrite(User u, byte[] oldDigest, byte[] newDigest) {
        this.user = u;
        this.oldDigest = oldDigest;
        this.newDigest = newDigest;
    }

    public User getUser() {
        return user;
    }

    public byte[] getOldDigest() {
        return oldDigest;
    }

    public byte[] getNewDigest() {
        return newDigest;
    }
}
