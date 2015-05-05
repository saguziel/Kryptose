package org.kryptose.requests;

import org.kryptose.exceptions.StaleWriteException;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseChangePassword extends Response {
	private static final long serialVersionUID = -492016377863076776L;

	private final byte[] digest;
    private StaleWriteException swex = null;

    public ResponseChangePassword(byte[] digest) {
        super();
        this.digest = digest == null ? null : digest.clone();
    }

    public ResponseChangePassword(StaleWriteException exception) {
        assert exception != null;
        this.swex = exception;
        this.digest = null;
    }

    public byte[] getDigest() throws StaleWriteException {
        if (this.swex != null) {
            throw this.swex;
        }
        return digest == null ? null : digest.clone();
    }

    public String logEntry() {
        if (this.swex != null) {
            return "RESPONSE: " + this.swex.toString();
        }
        return "RESPONSE: Change password request successful\n";
    }
}
