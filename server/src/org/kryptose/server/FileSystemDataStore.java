package org.kryptose.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.kryptose.requests.Blob;
import org.kryptose.requests.User;

/**
 * This DataStore assumes for each User, only one thread is accessing their files at a time.
 * @author jshi
 */
public class FileSystemDataStore implements DataStore {

	// TODO: make datastore filenames configurable?
	private static final String DATASTORE_PREFIX = "datastore/";
	private static final String USER_BLOB_PREFIX = DATASTORE_PREFIX + "blobs/";
	private static final String USER_LOG_PREFIX = DATASTORE_PREFIX + "userlogs/";
	private static final File SYSTEM_LOG_FILE = new File(DATASTORE_PREFIX + "kryptose.log");

    FileSystemDataStore() {

    }

    private static File getUserBlobFile(User user) {
    	assert (User.isValidUsername(user.getUsername()));
    	return new File(USER_BLOB_PREFIX + user.getUsername() + ".blob");
    }
    
    private static File getUserLogFile(User user) {
    	assert (User.isValidUsername(user.getUsername()));
    	return new File(USER_LOG_PREFIX + user.getUsername() + ".log");
    }
    
    private static File getSystemLogFile() {
    	return SYSTEM_LOG_FILE;
    }
    
    @Override
	public boolean userHasBlob(User user) {
        return getUserBlobFile(user).exists();
    }

    /**
     * 
        // attempts to write a blob
        // digest should be null if this is the first write for this user
        // return 0 if written successfully
        //
        // should fail if digest does not match digest of the previously stored blob
        // this is to check for merge conflicts
     */
    @Override
	public WriteResult writeBlob(User user, Blob blob, byte[] oldDigest) {
    	/*boolean hasBlob = this.userHasBlob(user);
    	if (oldDigest == null && hasBlob) {
    		return WriteResult.STALE_WRITE;
    	}
    	if (oldDigest != null && !hasBlob) {
    		// TODO not quite the right error condition.
    		return WriteResult.USER_DOES_NOT_EXIST;
    	}
    	if (oldDigest != null && hasBlob) {
    		if (!oldDigest.equals(this.readBlob(user).getDigest())) {
    			return WriteResult.STALE_WRITE;
    		}
    	} */ //TODO
    	
    	// Actually do the write.
    	File file = getUserBlobFile(user);

    	//TODO: Is this necessary? I think FileOutputStream creates the file if it doesn't exists.
		ensureExists(file);

    	try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));) {
			oos.writeObject(blob);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return WriteResult.INTERNAL_ERROR;
		}
    	
        return WriteResult.SUCCESS;
    }

    @Override
	public Blob readBlob(User user) {

    	// Actually do the read.
    	File file = getUserBlobFile(user);

    	try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));) {
			return (Blob) ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("If you see this error message, the developers are incapable.");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("If you see this error message, the developers are incapable.");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return WriteResult.INTERNAL_ERROR;
		}

        return WriteResult.SUCCESS;
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
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return WriteResult.INTERNAL_ERROR;
    		}
    		return WriteResult.SUCCESS;
    	}
    }
    
    private void ensureExists(File file) {

    	//TODO: We do not check for mkdirs' return value. If it is 0, the operation failed.
    	//The error will be caught later (createNewFile will return IOEXCEPTION, but this is unhandled.
    	file.getParentFile().mkdirs();

		try {
			file.createNewFile();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

}
