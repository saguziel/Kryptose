package org.kryptose.server;

import org.kryptose.requests.Blob;
import org.kryptose.requests.User;

public class DataStore {

    //TODO: consider behavior before user has a file with anything in it, probably write empty file
	// > Why not just specify that the default blob is an empty one of length 0? 

    private static DataStore instance = null;

    private DataStore() {

    }

    public static synchronized DataStore getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new DataStore();
            return instance;
        }
    }

    public boolean userHasBlob(User user) {
        return false;
    }

    public WriteResult writeBlob(User user, Blob blob, byte[] oldDigest) {
        // TODO
        // attempts to write a blob
        // digest should be null if this is the first write for this user
        // return 0 if written successfully
        //
        // should fail if digest does not match digest of the previously stored blob
        // this is to check for merge conflicts
        return WriteResult.INTERNAL_ERROR;
    }

    public Blob readBlob(User user) {
        // reads the user's blob, return null if nonexistent or error
        return null;
	}
    
    public WriteResult writeUserLog(User user, Log log) {
    	return WriteResult.INTERNAL_ERROR;
    }
    
    public WriteResult writeGlobalLog(Log log) {
    	return WriteResult.INTERNAL_ERROR;
    }

    public enum WriteResult {
        SUCCESS,
        USER_DOES_NOT_EXIST,
        STALE_WRITE,
        INTERNAL_ERROR
    }

}
