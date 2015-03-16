package org.kryptose.requests;

public class RequestPut extends Request {

	// TODO generate serialversionuid after fields are decided upon
	
	private final Blob blob;
    private final User user;
    private final byte[] oldDigest;

    public RequestPut(User user, Blob blob, byte[] oldDigest) {
        this.blob = blob;
        this.user = user;
        this.oldDigest = oldDigest;
    }

    public Blob getBlob() {
        return blob;
    }

    public User getUser() {
        return user;
    }

    public byte[] getOldDigest() {
        return oldDigest;
    }

}
