package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseGet extends Response {
	private static final long serialVersionUID = -921284173179022980L;

	private final Blob blob;

    public ResponseGet(Blob b) {
    	super();
        this.blob = b;
    }

    public Blob getBlob() {
        return blob;
    }

    public String logEntry() {
        return "RESPONSE: Get Request Successful\n";
    }

}
