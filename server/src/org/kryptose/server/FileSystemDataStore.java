package org.kryptose.server;

import org.kryptose.requests.Blob;
import org.kryptose.requests.Log;
import org.kryptose.requests.User;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A DataStore which stores each user's blob in a separate file on disk.
 * This DataStore assumes for each User, only one thread is accessing their files at a time.
 * @author jshi
 */
public class FileSystemDataStore implements DataStore {

	// TODO: make datastore filenames configurable?
	private static final String DATASTORE_PREFIX = "datastore/";
	private static final String USER_BLOB_PREFIX = DATASTORE_PREFIX + "blobs/";
	private static final String USER_LOG_PREFIX = DATASTORE_PREFIX + "userlogs/";
	private static final File SYSTEM_LOG_FILE = new File(DATASTORE_PREFIX + "kryptose.log");


    private Logger logger;

    FileSystemDataStore(Logger logger) {
    	this.logger = logger;
    }

    /**
     * Get the file where we store this user's blob.
     * @param user User, whose username determines the file location.
     * @return
     */
    private static File getUserBlobFile(User user) {
    	assert (User.isValidUsername(user.getUsername()));
    	return new File(USER_BLOB_PREFIX + user.getUsername() + ".blob");
    }

    /**
     * Get the file where we store this user's logs.
     * @param user User, whose username determines the file location.
     * @return
     */
    private static File getUserLogFile(User user) {
    	assert (User.isValidUsername(user.getUsername()));
    	return new File(USER_LOG_PREFIX + user.getUsername() + ".log");
    }
    
    /**
     * Get the file location of the system log. 
     * @return
     */
    private static File getSystemLogFile() {
    	return SYSTEM_LOG_FILE;
    }
    
    @Override
	public boolean userHasBlob(User user) {
        return getUserBlobFile(user).exists();
    }

    @Override
	public WriteResult writeBlob(User user, Blob blob, byte[] oldDigest) {
    	boolean hasBlob = this.userHasBlob(user);

    	if (oldDigest == null && hasBlob) {
    		return WriteResult.STALE_WRITE;
    	}
    	if (oldDigest != null && !hasBlob) {
    		// TODO not quite the right error condition.
    		return WriteResult.USER_DOES_NOT_EXIST;
    	}
    	if (oldDigest != null && hasBlob && !Arrays.equals(oldDigest, (this.readBlob(user).getDigest()))) {
            return WriteResult.STALE_WRITE;
    	}
    	
    	// Actually do the write.
    	File file = getUserBlobFile(user);

    	// Necessary to make sure directory exists.
		ensureExists(file);

    	try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));) {
			oos.writeObject(blob);
		} catch (IOException e) {
			String errorMsg = "Error writing user blob.";
			logger.log(Level.SEVERE, errorMsg, e);
			return WriteResult.INTERNAL_ERROR;
		}
    	
        return WriteResult.SUCCESS;
    }

    @Override
	public Blob readBlob(User user) {
    	File file = getUserBlobFile(user);

    	// Actually do the read.
    	try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));) {
			return (Blob) ois.readObject();
		} catch (IOException e) {
			String errorMsg = "Error reading user blob.";
			logger.log(Level.SEVERE, errorMsg, e);
			return null;
		} catch (ClassCastException | ClassNotFoundException e) {
			String errorMsg = "Error reading user blob.";
			logger.log(Level.SEVERE, errorMsg, e);
			return null;
		}
	}
    
    @Override
	public WriteResult writeUserLog(User user, Log log) {

    	File file = getUserLogFile(user);
		ensureExists(file);
    	//try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file, true));) {
		//	oos.writeObject(log);

    	try (FileWriter fw = new FileWriter(file, true);) {
			fw.write(log.toString());
		} catch (IOException e) {
			String errorMsg = "Error reading writing user log.";
			logger.log(Level.SEVERE, errorMsg, e);
			return WriteResult.INTERNAL_ERROR;
		}

        return WriteResult.SUCCESS;
    }

    @Override
    public ArrayList<Log> readUserLogs(User user, int maxEntries) {
        return null;
    }
    
    @Override
    public WriteResult writeSystemLog(Log log) {

    	File file = getSystemLogFile();

    	synchronized(SYSTEM_LOG_FILE) {
    		ensureExists(file);
    		//try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file, true));) {
    		//	oos.writeObject(log);
        	try (FileWriter fw = new FileWriter(file, true);) {
    			fw.write(log.toString());
    		} catch (IOException e) {
    			// TODO: hmm. this is somewhat circular.
    			String errorMsg = "Error writing system log: " + SYSTEM_LOG_FILE;
    			logger.log(Level.SEVERE, errorMsg, e);
    			return WriteResult.INTERNAL_ERROR;
    		}
    		return WriteResult.SUCCESS;
    	}
    }

    @Override
    public boolean deleteBlob(User u) {
        File file = getUserBlobFile(u);

        if (file.delete()) {
            return true;
        } else {
            String errorMsg = "Error deleting account: " + u.getUsername();
            logger.log(Level.SEVERE, errorMsg);
            return false;
        }
    }

    /**
     * Ensure that the file has been created, and that all its parent directories exist.
     * @param file The file to ensure has been created.
     */
    private void ensureExists(File file) {
    	File parentFile = file.getParentFile();
    	if (parentFile != null && parentFile.exists()) {
    		if (!parentFile.isDirectory()) {
    			logger.severe("Could not create directory: " + parentFile);
    		}
    	} else if (parentFile != null) {
    		boolean success = parentFile.mkdirs();
    		if (!success) {
    			logger.severe("Could not create directory: " + parentFile);
    		}
    	}

		try {
			file.createNewFile();
		} catch (IOException e1) {
			String errorMsg = "Error creating file: " + file;
			logger.log(Level.SEVERE, errorMsg, e1);
		}
    }

}
