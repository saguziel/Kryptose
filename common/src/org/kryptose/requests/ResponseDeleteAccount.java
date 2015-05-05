package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseDeleteAccount extends Response {
	private static final long serialVersionUID = 9029411847420961975L;

	private boolean successful;

    public ResponseDeleteAccount(boolean successful) {
        super();
        this.successful = successful;
    }

    public boolean verifySuccessful() {
        return successful;
    }

    public String logEntry() {
        if (!successful) {
            return "RESPONSE: " + "Delete account failed";
        }
        return "RESPONSE: Delete Account Request Successful\n";
    }

}
