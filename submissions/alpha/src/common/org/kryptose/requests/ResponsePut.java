package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponsePut extends Response {
    private final User user;
    private final byte[] digest;

    public ResponsePut(User u, byte[] digest) {
        this.user = u;
        this.digest = digest;
    }

    public byte[] getDigest() {
        return digest;
    }

    public User getUser() {
        return user;
    }

    public String logEntry() {
        return "RESPONSE: Put request successful\n";
    }
}
