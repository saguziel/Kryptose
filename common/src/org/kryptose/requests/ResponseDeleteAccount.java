package org.kryptose.requests;

/**
 * Created by alexguziel on 3/15/15.
 */
public final class ResponseDeleteAccount extends Response {

    private boolean successful;

    public ResponseDeleteAccount(boolean successful) {
        super();
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
