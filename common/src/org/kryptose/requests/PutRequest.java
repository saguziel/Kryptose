package org.kryptose.requests;

public class PutRequest extends Request {

	// TODO generate serialversionuid after fields are decided upon
	
	private final Blob blob;
    private final User user;

    public PutRequest(User user, Blob blob) {
        this.blob = blob;
        this.user = user;
    }

    public Blob getBlob() {
        return blob;
    }

    public User getUser() {
        return user;
    }

}
