package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseGet extends Response {

    private final Blob blob;

    public ResponseGet(Blob b) {
        this.blob = b;
    }

    public Blob getBlob() {
        return blob;
    }

    public String logEntry() {
        return "RESPONSE: Get Request Successful\n";
    }

}
