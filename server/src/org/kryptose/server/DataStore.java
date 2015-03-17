package org.kryptose.server;

import org.kryptose.requests.Blob;
import org.kryptose.requests.User;

public interface DataStore {

	public abstract boolean userHasBlob(User user);

	public abstract WriteResult writeBlob(User user, Blob blob, byte[] oldDigest);

	public abstract Blob readBlob(User user);

	public abstract WriteResult writeUserLog(User user, Log log);

	public abstract WriteResult writeGlobalLog(Log log);


    public enum WriteResult {
        SUCCESS,
        USER_DOES_NOT_EXIST,
        STALE_WRITE,
        INTERNAL_ERROR
    }

}