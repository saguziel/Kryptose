package org.kryptose.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.User;

public class UserTable {
	
	private static final int DEFAULT_SALT_SIZE = 50;

	// TODO: make configurable
	private static final String USER_TABLE_FILE_NAME = "usertable.bin";
	private static final String USER_TABLE_TEMP_NAME = "usertable.bin.tmp";

	public enum Result{USER_NOT_FOUND, USER_ALREADY_EXISTS, USER_ADDED, WRONG_CREDENTIALS, AUTHENTICATION_SUCCESS, AUTH_KEY_CHANGED};
	
	private class UserRecord {
		final String username;
		byte[] salt;
		byte[] auth_key_hash;
		
		//TODO: for final
		int login_attempts;
		
		public UserRecord(String username, byte[] auth_key) {
			this.username = username;
			
			try {
				
				salt = new byte[salt_size];
		    	SecureRandom rnd;

	    		rnd = SecureRandom.getInstance("SHA1PRNG");
		    	rnd.nextBytes(salt);

		    	SecretKeyFactory factory;
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(auth_key).toCharArray(), salt, 65536, 256);
				
				//TODO handle auth_key... perhaps zero it...
				
				auth_key_hash = factory.generateSecret(spec).getEncoded();

		    	

			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new CryptoPrimitiveNotSupportedException(e);
			}
		}
		
		public boolean authenticate(byte[] tentative_key){
			try{
				SecretKeyFactory factory;
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(tentative_key).toCharArray(), salt, 65536, 256);
			
				//TODO: secure erase.
				DatatypeConverter.printHexBinary(auth_key_hash);
				DatatypeConverter.printHexBinary(factory.generateSecret(spec).getEncoded());
				
				return Arrays.equals(auth_key_hash, factory.generateSecret(spec).getEncoded());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new CryptoPrimitiveNotSupportedException(e);
			}

		}

		public boolean changeUserAuthKey(byte[] old_key, byte[] new_key) {
			if(!authenticate(old_key))
				return false;

			try {
				
				salt = new byte[salt_size];
		    	SecureRandom rnd;

	    		rnd = SecureRandom.getInstance("SHA1PRNG");
		    	rnd.nextBytes(salt);

		    	SecretKeyFactory factory;
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
				KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(new_key).toCharArray(), salt, 65536, 256);
				
				//TODO handle auth_key... perhaps zero it...
				
				auth_key_hash = factory.generateSecret(spec).getEncoded();

		    	return true;

			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new CryptoPrimitiveNotSupportedException(e);
			}

			
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Arrays.hashCode(auth_key_hash);
			result = prime * result + login_attempts;
			result = prime * result + Arrays.hashCode(salt);
			result = prime * result
					+ ((username == null) ? 0 : username.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserRecord other = (UserRecord) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (!Arrays.equals(auth_key_hash, other.auth_key_hash))
				return false;
			if (login_attempts != other.login_attempts)
				return false;
			if (!Arrays.equals(salt, other.salt))
				return false;
			if (username == null) {
				if (other.username != null)
					return false;
			} else if (!username.equals(other.username))
				return false;
			return true;
		}

		private UserTable getOuterType() {
			return UserTable.this;
		}

		
				
	}

	// INSTANCE VARIABLES
	
	private final Logger logger;
	private final int salt_size;
	private ConcurrentHashMap<String,UserRecord> users;
	private final Object persistLock = new Object();
	private final Object ensurePersistMonitor = new Object();
	private volatile boolean persistInProgress = false;
	

	public UserTable(Logger logger) {
		this(logger, DEFAULT_SALT_SIZE);
	}
	
	public UserTable(Logger logger, int salt_size) {
		this.logger = logger;
		this.salt_size = salt_size;
		this.users = new ConcurrentHashMap<String,UserRecord>();
//		Users.put("me", new UserRecord("me", "AAAAAAAAAAAAAAAA", "A"));
	}
	
	public boolean contains(String username){
		return users.containsKey(username);
	}
	
	public Result addUser(User user){
		if (this.contains(user.getUsername())) 
			return Result.USER_ALREADY_EXISTS;
		
		users.put(user.getUsername(),
				new UserRecord(user.getUsername(), user.getPasskey()));

		this.ensurePersistNewThread();
		
		return Result.USER_ADDED;
	}
	
	public Result auth(User user){
		if (!this.contains(user.getUsername())) 
			return Result.USER_NOT_FOUND;
		else {
			// Do auth.
			boolean success = users.get(user.getUsername()).authenticate(user.getPasskey());
			// Persist changes to disk.
			this.ensurePersistNewThread();
			// Return result.
			if (success) return Result.AUTHENTICATION_SUCCESS;
			else return Result.WRONG_CREDENTIALS;
		}
	}
	
	public Result changeAuthKey(String username, byte[] old_key, byte[] new_key){
		if (!this.contains(username)) 
			return Result.USER_NOT_FOUND;
		else {
			// Do change.
			boolean success = this.users.get(username).changeUserAuthKey(old_key,new_key);
			// Persist changes to disk.
			this.ensurePersistNewThread();
			// Return result.
			if (success) return Result.AUTH_KEY_CHANGED;
			else return Result.WRONG_CREDENTIALS;
		}

	}

	public static UserTable loadFromFile(Logger logger) throws IOException {
		try (ObjectInputStream fw = new ObjectInputStream(
				new FileInputStream(USER_TABLE_FILE_NAME))) {
			return (UserTable) fw.readObject();
		} catch (ClassNotFoundException e) {
			String errorMsg = "UserTable file incorrectly formatted. Maybe outdated file version?";
			logger.log(Level.SEVERE, errorMsg, e);
			throw new IOException(errorMsg, e);
		} catch (FileNotFoundException e) {
			String errorMsg = "No existing user table file found... reverting to no existing users.";
			logger.log(Level.WARNING, errorMsg, e);
			return new UserTable(logger);
		}
	}
	
	public void saveToFile() throws IOException {
		try (ObjectOutputStream fw = new ObjectOutputStream(
				new FileOutputStream(USER_TABLE_TEMP_NAME))) {
			fw.writeObject(this);
		}
		File finalFile = new File(USER_TABLE_FILE_NAME);
		finalFile.delete();
		new File(USER_TABLE_TEMP_NAME).renameTo(finalFile);
	}
	
	public void ensurePersistNewThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				UserTable.this.ensurePersist();
			}
		}).start();
	}
	
	public void ensurePersist() {
		// Check to see if another thread is already doing a persist.
		synchronized (ensurePersistMonitor) {
			if (persistInProgress) {
				// Queue this thread up as responsible for ensuring a persist.
				ensurePersistMonitor.notifyAll(); // relieve other threads of responsibility.
				boolean interrupted = false;
				try {
					ensurePersistMonitor.wait();
				}
				catch (InterruptedException ex) { interrupted = true; }
				// If woken while persist in progress, means this thread
				// no longer responsible for ensuring a persist.
				if (!interrupted && persistInProgress) return;
			}
			// This thread about to start a persist.
			persistInProgress = true;
		}
		// Persist the UserTable.
		synchronized (persistLock) {
			try {
				this.saveToFile();
			} catch (IOException e) {
				String errorMsg = "Error saving user table file to disk.";
				this.logger.log(Level.SEVERE, errorMsg, e);
			}
		}
		// Notify other threads that persisting is done.
		synchronized (ensurePersistMonitor) {
			persistInProgress = false;
			// Notify a thread that's responsible for a future persist.
			ensurePersistMonitor.notify();
		}
	}

	public static void main(String[] args) {
		UserTable u = new UserTable(null, 50);
		
		byte[] good_pwd = "good".getBytes();
		byte[] bad_pwd = "bad".getBytes();
		
		System.out.println(u.contains("Antonio"));
		//u.addUser("Antonio", good_pwd);
		System.out.println(u.contains("Antonio"));
		System.out.println(u.contains("AntonioAAAA"));
		
		//System.out.println(u.auth("Mario", good_pwd));		
		//System.out.println(u.auth("Antonio", good_pwd));		
		//System.out.println(u.auth("Antonio", bad_pwd));		
		
		System.out.println(u.changeAuthKey("Mario", good_pwd, good_pwd));		
		System.out.println(u.changeAuthKey("Antonio", good_pwd,bad_pwd));		
		//System.out.println(u.auth("Antonio", good_pwd));		
		//System.out.println(u.auth("Antonio", bad_pwd));
		
		
	}

}
