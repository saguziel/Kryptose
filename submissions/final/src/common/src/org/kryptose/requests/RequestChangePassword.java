package org.kryptose.requests;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * A request to get the blob stored by this user.
 * <p>
 * Created by alexguziel on 3/15/15.
 */
public final class RequestChangePassword extends Request {
	private static final long serialVersionUID = 7721398455729573266L;
	
	private byte[] newAuthkey;
    private Blob newBlob;
    private byte[] oldDigest;

    public RequestChangePassword(User u, byte[] newAuthkey, Blob newBlob, byte[] oldDigest) {
        super(u);
        this.newAuthkey = newAuthkey;
        this.newBlob = newBlob;
        this.oldDigest = (oldDigest == null) ? null : oldDigest.clone();
        this.validateInstance();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        // Check that our invariants are satisfied
        this.validateInstance();
    }

    public byte[] getNewAuthkey() {
        return newAuthkey;
    }

    public Blob getNewBlob() {
        return newBlob;
    }

    public byte[] getOldDigest() {
        return (oldDigest == null) ? null : oldDigest.clone();
    }

    @Override
    public void validateInstance() {
        if (newAuthkey == null) {
            throw new IllegalArgumentException("New auth key cannot be null");
        }
        super.validateInstance();
    }


    @Override
    public String logEntry() {
        return String.format("REQUEST: Change password from %s%n%n", super.getUser().getUsername());
    }
}
