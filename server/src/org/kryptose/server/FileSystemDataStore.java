package org.kryptose.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.kryptose.requests.Blob;
import org.kryptose.requests.User;

public class FileSystemDataStore implements DataStore {

    //TODO: consider behavior before user has a file with anything in it, probably write empty file
	// > Why not just specify that the default blob is an empty one of length 0? 

    private static DataStore instance = null;
    
    private ConcurrentMap<String,Object> locks;

    private FileSystemDataStore() {
    	this.locks = new ConcurrentHashMap<String, Object>();
    }

    public static synchronized DataStore getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new FileSystemDataStore();
            return instance;
        }
    }

    /* (non-Javadoc)
	 * @see org.kryptose.server.IDataStore#userHasBlob(org.kryptose.requests.User)
	 */
    @Override
	public boolean userHasBlob(User user) {
        return false;
    }

    /* (non-Javadoc)
	 * @see org.kryptose.server.IDataStore#writeBlob(org.kryptose.requests.User, org.kryptose.requests.Blob, byte[])
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see org.kryptose.server.IDataStore#readBlob(org.kryptose.requests.User)
	 */
    @Override
	public Blob readBlob(User user) {
        // reads the user's blob, return null if nonexistent or error
        return null;
	}
    
    /* (non-Javadoc)
	 * @see org.kryptose.server.IDataStore#writeUserLog(org.kryptose.requests.User, org.kryptose.server.Log)
	 */
    @Override
	public WriteResult writeUserLog(User user, Log log) {
    	return WriteResult.INTERNAL_ERROR;
    }
    
    /* (non-Javadoc)
	 * @see org.kryptose.server.IDataStore#writeGlobalLog(org.kryptose.server.Log)
	 */
    @Override
	public WriteResult writeGlobalLog(Log log) {
    	return WriteResult.INTERNAL_ERROR;
    }

}
