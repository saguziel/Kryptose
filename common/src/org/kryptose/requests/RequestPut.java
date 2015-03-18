package org.kryptose.requests;

public final class RequestPut extends Request {

	// TODO generate serialversionuid after fields are decided upon
	
	private final Blob blob;
    private final byte[] oldDigest;

    public RequestPut(User user, Blob blob, byte[] oldDigest) {
    	super(user);
        this.blob = blob;
        this.oldDigest = oldDigest;
        this.validateInstance();
    }

    public Blob getBlob() {
        return blob;
    }

    public byte[] getOldDigest() {
        return oldDigest;
    }

	@Override
	void validateInstance() {
		super.validateInstance();
    	if (this.blob == null) throw new IllegalArgumentException("blob is null");
    	if (this.oldDigest == null) throw new IllegalArgumentException("oldDigest is null");
    	this.blob.validateInstance();
	}

}
