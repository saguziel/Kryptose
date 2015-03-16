package org.kryptose.requests;

import java.security.NoSuchAlgorithmException;

/**
 * Created by alexguziel on 3/15/15.
 */
public class ResponsePut extends Response {
    private final User user;
    private final byte[] digest;

    public ResponsePut(User u, Blob b) {
        user = u;
        digest = b.getDigest();
    }

    public byte[] getDigest() {
        return digest;
    }

    public User getUser() {
        return user;
    }
}
