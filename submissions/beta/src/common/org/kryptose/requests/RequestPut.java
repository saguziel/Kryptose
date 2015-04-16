package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A request to store a new blob for a user.
 */
public final class RequestPut extends Request {

    // TODO generate serialversionuid after fields are decided upon

    private final Blob blob;
    private byte[] oldDigest;

    /**
     * Constructs a new Request to write a blob for this user.
     * @param user The user whose blob to write.
     * @param blob The blob to write.
     * @param oldDigest The digest of the old blob to overwrite.
     * 		Used by server to detect stale write requests.
     * @see Blob#getDigest()
     */
    public RequestPut(User user, Blob blob, byte[] oldDigest) {
        super(user);
        this.blob = blob;
        this.oldDigest = oldDigest == null ? null : oldDigest.clone();
        this.validateInstance();
    }

    /**
     * The blob to overwrite by this request.
     * @return
     */
    public Blob getBlob() {
        return blob;
    }

    /**
     * The digest of the blob to be overwritten by this request.
     * @see Blob#getDigest()
     */
    public byte[] getOldDigest() {
        return oldDigest == null ? null : oldDigest.clone();
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
