package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;

public final class RequestPut extends Request {

    // TODO generate serialversionuid after fields are decided upon

    private final Blob blob;
    private byte[] oldDigest;

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

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Check that our invariants are satisfied
        this.validateInstance();
    }

	@Override
	public void validateInstance() {
		super.validateInstance();
    	if (this.blob == null) throw new IllegalArgumentException("blob is null");
    	if (this.oldDigest != null) {
    		this.oldDigest = oldDigest.clone(); // Defensive copying.
    	}
    	this.blob.validateInstance();
	}

    @Override
    public String logEntry() {
        return String.format("REQUEST: Put from %s\n", super.getUser().getUsername());
    }
}
