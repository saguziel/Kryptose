package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public class ResponseGet extends Response {

    private final Blob blob;
//    private final UserAuditLog log;

    public ResponseGet(Blob b, UserAuditLog log) {
        this.blob = b;
        this.log = log;
    }

    public Blob getBlob() {
        return blob;
    }

}
