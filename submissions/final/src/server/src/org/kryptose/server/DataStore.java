package org.kryptose.server;

import org.kryptose.requests.Blob;
import org.kryptose.requests.Log;
import org.kryptose.requests.User;

import java.util.ArrayList;

public interface DataStore {

	/**
	 * 
	 * @param user
	 * @return
	 */
	public abstract boolean userHasBlob(User user);

    /**
     * Attempts to write a blob.
     * 
     * Fails and returns WriteResult.STALE_WRITE if oldDigest does not
     * match the digest of the previously stored blob.
     * 
     * @param user The user whose blob to write.
     * @param blob The blob to write for the user.
     * @param oldDigest The digest of the blob we're overwriting, or null if no
     * 		previous blob should have been set.
     * @return The outcome of the write.
     */
	public abstract WriteResult writeBlob(User user, Blob blob, byte[] oldDigest);

	/**
	 * Read the blob associated with the given user.
	 * 
	 * @param user The user whose blob to read.
	 * @return The blob read, or null on read error.
	 */
	public abstract Blob readBlob(User user);

    public abstract boolean deleteBlob(User user);

    /**
     * Write a user-specific log entry.
     *
     * @param user       The user whose log to write.
     * @param maxEntries - maximum number of entries to return, -1 if no limit, returns maxEntries most recent entries
     * @return The logs in question
     */
    public abstract ArrayList<Log> readUserLogs(User user, int maxEntries);

	/**
	 * Write a user-specific log entry.
	 * 
	 * @param user The user whose log to write.
	 * @param log The log to write.
	 * @return The outcome of the write.
	 */
	public abstract WriteResult writeUserLog(User user, Log log);

	
    public enum WriteResult {
        SUCCESS,
        USER_DOES_NOT_EXIST,
        STALE_WRITE,
        INTERNAL_ERROR
    }

}