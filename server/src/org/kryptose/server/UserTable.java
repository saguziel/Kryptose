package org.kryptose.server;

import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserTable implements Serializable {
	private static final long serialVersionUID = 6410241252645043453L;

	private static final int DEFAULT_SALT_SIZE = 50;

	// TODO: make configurable
	private static final String DEFAULT_FILENAME = "datastore/usertable.bin";
	private static final String FILENAME_BACKUP_SUFFIX = ".bak";
    private final int salt_size;
    ;
    private transient Logger logger = null;

	// INSTANCE VARIABLES
	private transient String fileName = null;
	private transient String bakFileName = null;
	private ConcurrentHashMap<String,UserRecord> users;
	private transient PersistWorkerThread persistThread = null;
	private transient RepersistRequest persistReqInProgress = null;
	private transient RepersistRequest nextPersistReq = new RepersistRequest();
	private transient Object persistReqLock = new Object();

    public UserTable(Logger logger) {
        this(logger, DEFAULT_FILENAME, DEFAULT_SALT_SIZE);
    }

    public UserTable(Logger logger, String fileName) {
        this(logger, fileName, DEFAULT_SALT_SIZE);
    }

    // TODO: make this configurable from properties
    public UserTable(Logger logger, String fileName, int salt_size) {
        this.logger = logger;
        this.fileName = fileName;
        this.bakFileName = fileName + FILENAME_BACKUP_SUFFIX;
        this.salt_size = salt_size;
        this.users = new ConcurrentHashMap<String, UserRecord>();
    }

    public static UserTable loadFromFile(Logger logger) throws IOException {
        return UserTable.loadFromFile(logger, DEFAULT_FILENAME);
    }

    public static UserTable loadFromFile(Logger logger, String fileName) throws IOException {
        try (ObjectInputStream fr = new ObjectInputStream(
                new FileInputStream(fileName))) {
            UserTable ut = (UserTable) fr.readObject();
            ut.initFromDeserialization(logger, fileName);
            return ut;
        } catch (ClassNotFoundException e) {
            String errorMsg = String.format("UserTable file at %s incorrectly formatted. Maybe outdated file version?",
                    fileName);
            logger.log(Level.SEVERE, errorMsg, e);
            throw new IOException(errorMsg, e);
        } catch (FileNotFoundException e) {
            String errorMsg = String.format("No existing user table file found at %s... reverting to empty user table.",
                    fileName);
            logger.log(Level.WARNING, errorMsg);
            return new UserTable(logger);
        }
    }

    private void initFromDeserialization(Logger logger, String fileName) {
        this.logger = logger;
        this.fileName = fileName;
        this.bakFileName = fileName + FILENAME_BACKUP_SUFFIX;
        this.nextPersistReq = new RepersistRequest();
    }

    public boolean contains(String username) {
        return users.containsKey(username);
    }

    public Result addUser(User user) {
        if (this.contains(user.getUsername()))
            return Result.USER_ALREADY_EXISTS;

        users.put(user.getUsername(),
                new UserRecord(user.getUsername(), user.getPasskey(), salt_size));

        this.ensurePersist();

        return Result.USER_ADDED;
    }

    public boolean deleteUser(User user) {
        UserRecord ur = users.remove(user.getUsername());
        if (ur == null) {
            logger.log(Level.SEVERE, "Error deleting user from user table: " + user.getUsername());
        }
        this.ensurePersist();
        return (ur != null);
    }

    public Result auth(User user) {
        if (user.getPasskey() == null) {
            return Result.WRONG_CREDENTIALS;
        }
        if (!this.contains(user.getUsername()))
            return Result.USER_NOT_FOUND;
        else {
            // Do auth.
            boolean success = users.get(user.getUsername()).authenticate(user.getPasskey());
            // Persist changes to disk.
            this.ensurePersist();
            // Return result.
            if (success) return Result.AUTHENTICATION_SUCCESS;
            else return Result.WRONG_CREDENTIALS;
        }
    }

    public Result changeAuthKey(String username, byte[] old_key, byte[] new_key) {
        if (!this.contains(username))
            return Result.USER_NOT_FOUND;
        else {
            // Do change.
            boolean success = this.users.get(username).changeUserAuthKey(old_key, new_key, salt_size);
            // Persist changes to disk.
            this.ensurePersist();
            // Return result.
            if (success) return Result.AUTH_KEY_CHANGED;
            else return Result.WRONG_CREDENTIALS;
        }

    }

    private void saveToFile() throws IOException {
        File file = new File(this.fileName);
        this.ensureDirectoryExists(file);
        // Move existing usertable file to backup location.
        if (file.exists()) {
            Files.move(Paths.get(this.fileName), Paths.get(this.bakFileName),
                    StandardCopyOption.REPLACE_EXISTING);
        }
        // Write this usertable to file.
        try (ObjectOutputStream fw = new ObjectOutputStream(
                new FileOutputStream(this.fileName))) {
            fw.writeObject(this);
        }
    }

    /**
     * Does not block.
     */
    public void ensurePersist() {
        try {
            this.ensurePersist(false);
        } catch (InterruptedException e) {
            String errorMsg = "InterruptedException where not expected. Serious problem in code.";
            this.logger.log(Level.SEVERE, errorMsg, e);
        }
    }

    public void ensurePersist(boolean block) throws InterruptedException {
        this.ensureThreadStarted();
        RepersistRequest req;
        synchronized (this.persistReqLock) {
            req = this.nextPersistReq;
        }
        req.makeRequest();
        if (Thread.currentThread().isInterrupted()) {
        	this.persistThread.interrupt();
        }
        if (block) req.waitForDone();
    }

    public void ensureThreadStarted() {
        synchronized (persistReqLock) {
            if (this.persistThread == null
                    || !this.persistThread.isAlive()) {
                this.persistThread = new PersistWorkerThread();
                this.persistThread.start();
            }
        }
    }

    /**
     * Ensure that the file's parent directory exists.
     *
     * @param file The file to ensure has a parent directory.
     */
    private void ensureDirectoryExists(File file) {
        File parentFile = file.getParentFile();
        if (parentFile == null) return;
        if (parentFile.exists()) {
            if (!parentFile.isDirectory()) {
                logger.severe("Could not create directory: " + parentFile);
            }
        } else {
            boolean success = parentFile.mkdirs();
            if (!success) {
                logger.severe("Could not create directory: " + parentFile);
            }
        }
    }

    public enum Result {USER_NOT_FOUND, USER_ALREADY_EXISTS, USER_ADDED, WRONG_CREDENTIALS, AUTHENTICATION_SUCCESS, AUTH_KEY_CHANGED}

    private static class RepersistRequest {
        private boolean requested = false;
        private Object requestMonitor = new Object();
        private boolean done = false;
        private Object doneMonitor = new Object();

        void makeRequest() {
            synchronized (requestMonitor) {
                if (!requested) {
                    requested = true;
                    requestMonitor.notify();
                }
            }
        }

        void markDone() {
            synchronized (doneMonitor) {
                done = true;
                doneMonitor.notifyAll();
            }
        }

        void waitForRequest() throws InterruptedException {
            synchronized (requestMonitor) {
                while (!requested) {
                    requestMonitor.wait();
                }
            }
        }

        void waitForDone() throws InterruptedException {
            synchronized (doneMonitor) {
                while (!done) {
                    doneMonitor.wait();
                }
            }
        }
    }

    private static class UserRecord implements Serializable {
        private static final long serialVersionUID = 8562908652006397891L;
        final String username;
        byte[] salt;
        byte[] auth_key_hash;

        //TODO: implement login attempts
        int login_attempts;

        public UserRecord(String username, byte[] auth_key, int salt_size) {
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

        public boolean authenticate(byte[] tentative_key) {
            try {
                SecretKeyFactory factory;
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                KeySpec spec = new PBEKeySpec(DatatypeConverter.printHexBinary(tentative_key).toCharArray(), salt, 65536, 256);

                // TODO: secure erase. unfortunately would require homebrewing a byte->char array converter.
                // also wouldn't be effective in Java <=8 anyway.
                DatatypeConverter.printHexBinary(auth_key_hash);
                DatatypeConverter.printHexBinary(factory.generateSecret(spec).getEncoded());

                return Arrays.equals(auth_key_hash, factory.generateSecret(spec).getEncoded());
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new CryptoPrimitiveNotSupportedException(e);
            }

        }

        public boolean changeUserAuthKey(byte[] old_key, byte[] new_key, int salt_size) {
            if (!authenticate(old_key))
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


    }

    private class PersistWorkerThread extends Thread {
        PersistWorkerThread() {
            super();
            this.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable t) {
                    String errorMsg = "Uncaught exception in Usertable save-to-file thread.";
                    logger.log(Level.SEVERE, errorMsg, t);
                    synchronized (persistReqLock) {
                        persistThread = null;
                        ensureThreadStarted();
                    }
                }
            });
        }

        public void run() {
            synchronized (persistReqLock) {
                if (nextPersistReq == null) {
                    nextPersistReq = new RepersistRequest();
                }
            }
            boolean keepRunning = true;
            while (keepRunning || nextPersistReq.requested) {
                while (!nextPersistReq.requested) {
                    try {
                        nextPersistReq.waitForRequest();
                    } catch (InterruptedException e) {
                        keepRunning = false;
                    }
                }
                synchronized (persistReqLock) {
                    persistReqInProgress = nextPersistReq;
                    nextPersistReq = new RepersistRequest();
                }
                try {
                    saveToFile();
                } catch (IOException e) {
                    String errorMsg = "Error saving file of users.";
                    logger.log(Level.SEVERE, errorMsg, e);
                }
                persistReqInProgress.markDone();
                persistReqInProgress = null;
            }
        }
    }

}
