package org.kryptose.server;

import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;
import org.kryptose.exceptions.InternalServerErrorException;
import org.kryptose.exceptions.InvalidCredentialsException;
import org.kryptose.exceptions.MalformedRequestException;
import org.kryptose.exceptions.StaleWriteException;
import org.kryptose.exceptions.UsernameInUseException;
import org.kryptose.requests.*;
import org.kryptose.server.UserTable.Result;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;
import java.util.logging.*;

public class Server {

    private static final String PROPERTIES_FILE = "serverProperties.xml";
    
    private static final String LOGGER_NAME = "org.kryptose.server";
    private Logger logger = Logger.getLogger(LOGGER_NAME);
    private DataStore dataStore = new FileSystemDataStore(logger);
    // TODO make configurable?
    private static final int LOG_FILE_COUNT = 20;
    private static final int LOG_FILE_SIZE = 40 * 1024; // bytes
    private static final String LOG_FILE_NAME = "datastore/kryptose.%g.%u.log";
    
    // INSTANCE FIELDS
    private Properties properties;
    private SecureServerListener listener;
 
    // An object for each user, to lock on, to make sure each user only has one
    // request being processed at a time.
    private Map<User, Object> userLocks =
    		Collections.synchronizedMap(new WeakHashMap<User,Object>());

    private UserTable userTable = null;

    // INSTANCE METHODS

    private Server() {

    }

    /**
     * Main Kryptose server program.
     *
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
        
    /**
     * Handle the request.
     * 
     * Acquire locks and authenticate user, then dispatch to request-specific handlers.
     *
     * @param request not null
     * @return The Response to the request.
     */
    public Response handleRequest(Request request) {
    	Object userLock;

    	// Obtain lock to ensure each User only has one request
    	// being processed at a time.
    	synchronized (this.userLocks) {
    		userLock = this.userLocks.get(request.getUser());
    		if (userLock == null) {
    			userLock = new Object();
    			this.userLocks.put(request.getUser(), userLock);
    		}
    	}

    	synchronized (userLock) {
    		if (request instanceof RequestCreateAccount) {
    			// Skip authentication
    	    	return this.handleRequestWithLocksAcquired(request);
    		} else {
    			// Authenticate user.
    			Result result = this.userTable.auth(request.getUser());
    			// If authentication failed, respond with exception.
    			if (result == Result.WRONG_CREDENTIALS ||
    					result == Result.USER_NOT_FOUND) {
    				InvalidCredentialsException ex = new InvalidCredentialsException();
    				return new ResponseErrorReport(ex);
    			}
    			// Authentication succeeded; continue.
    			if (result == Result.AUTHENTICATION_SUCCESS) {
    	    		return this.handleRequestWithLocksAcquired(request);
    			}
    			// result check fell through, log error.
    			String errorMsg = "Unexpected UserTable.Result in authentication.";
    			this.getLogger().log(Level.SEVERE, errorMsg, result);
    			return new ResponseErrorReport(new InternalServerErrorException());
    		}
    	}
    }
    
    /**
     * Dispatch request to request-specific handlers.
     * @param request
     * @return
     */
    private Response handleRequestWithLocksAcquired(Request request) {
        if (request instanceof RequestGet) {
            return this.handleRequestGet((RequestGet) request);
        } else if (request instanceof RequestPut) {
            return this.handleRequestPut((RequestPut)request);
        } else if (request instanceof RequestTest) { //TODO: remove later, testing only.
            return this.handleRequestTest((RequestTest) request);
        } else if (request instanceof RequestLog) {
            return this.handleRequestLog((RequestLog) request);
        } else if (request instanceof RequestCreateAccount) {
            return this.handleRequestCreateAccount((RequestCreateAccount) request);
        } else {
        	// TODO: make sure this is included on server logs.
            return new ResponseErrorReport(new MalformedRequestException());
        }
    }

    private Response handleRequestCreateAccount(RequestCreateAccount request) {
        Response response;
        User u = request.getUser();

        Result result = this.userTable.addUser(u);
        if (result == Result.USER_ADDED) {
        	response = new ResponseCreateAccount(); // TODO userauditlog
        } else if (result == Result.USER_ALREADY_EXISTS) {
        	response = new ResponseCreateAccount(new UsernameInUseException());
        } else {
        	response = new ResponseErrorReport(new InternalServerErrorException());
        	String errorMsg = "Unexpected result from UserTable addUSer.";
        	this.getLogger().log(Level.SEVERE, errorMsg, result);
        }

        this.dataStore.writeUserLog(u, new Log(u, request, response));
        return response;
	}

	private Response handleRequestGet(RequestGet request) {
        Response response;
        User u = request.getUser();

        boolean hasBlob = this.dataStore.userHasBlob(u);

        if (hasBlob) {
            Blob b = this.dataStore.readBlob(u);

            if (b != null) {
                response = new ResponseGet(b); // TODO userauditlog
            } else {
                response = new ResponseErrorReport(new InternalServerErrorException());
                String errorMsg = "Blob was null, but hasBlob was true. User: " + u.getUsername();
                this.getLogger().log(Level.SEVERE, errorMsg);
            }
        } else {
        	// User has not yet stored a blob.
            response = new ResponseGet((Blob)null); // TODO userauditlog
            // TODO is this an exceptional response? Do we need to create an exception?
            // ^ probably not.
        }

        this.dataStore.writeUserLog(u, new Log(u, request, response));
        return response;
    }

    private ResponseLog handleRequestLog(RequestLog request) {
        ResponseLog response;
        User u = request.getUser();
        ArrayList<Log> logs = this.dataStore.readUserLogs(u, -1);
        response = new ResponseLog(logs);
        this.dataStore.writeUserLog(u, new Log(u, request, response));

        return response;
    }

    private Response handleRequestPut(RequestPut request) {
        Response response;
        User u = request.getUser();
        byte[] oldDigest = request.getOldDigest();
        Blob toBeWritten = request.getBlob();

        DataStore.WriteResult writeResult = this.dataStore.writeBlob(u, toBeWritten, oldDigest);

        switch (writeResult) {
            case SUCCESS:
            	try {
                    response = new ResponsePut(toBeWritten.getDigest());
                } catch (CryptoPrimitiveNotSupportedException e) {
                	String errorMsg = "Java Runtime does not support required cryptography operations.";
                	this.getLogger().log(Level.SEVERE, errorMsg, e);
                    response = new ResponseErrorReport(new InternalServerErrorException());
                }
                break;
            case STALE_WRITE:
                response = new ResponsePut(new StaleWriteException());
                break;
            case INTERNAL_ERROR:
                response = new ResponseErrorReport(new InternalServerErrorException());
                // Assume logging has already been done.
                break;
            case USER_DOES_NOT_EXIST: // we should have authenticated by now.
            default:
                response = new ResponseErrorReport(new InternalServerErrorException());
            	String errorMsg = "Switch-case fall-through in handleRequestPut.";
            	this.getLogger().log(Level.SEVERE, errorMsg, Thread.currentThread().getStackTrace());
                break;
        }
        this.dataStore.writeUserLog(u, new Log(u, request, response));
        return response;
    }
    
    
    //TODO: remove later (testing only)
    private Response handleRequestTest(RequestTest request) {
    	return new ResponseTest(request.toString());
    }
    
    public DataStore getDataStore() {
        return this.dataStore;
    }

    public Logger getLogger() {
        return this.logger;
    }
    
    private Properties readProperties() {
        Properties properties = new Properties();

        //SETTING DEFAULT CONFIGURATIONS (can be overriden by the Server settings file
        properties.setProperty("NUMBER_OF_THREADS", "8");
        properties.setProperty("PORT_NUMBER", "5002");
        properties.setProperty("SERVER_KEY_STORE_FILE", "ServerKeyStore.jks");
        properties.setProperty("SERVER_KEY_STORE_PASSWORD", "aaaaaa");
        

        try (FileInputStream in = new FileInputStream(PROPERTIES_FILE);) {
        	// TODO validate and fail fast.
            Properties XMLProperties = new Properties();
            XMLProperties.loadFromXML(in);
            properties.putAll(XMLProperties);
        } catch (IOException e) {
        	String msg = "Could not read server configuration file: " + PROPERTIES_FILE
        			+ "\n Writing default configuration file.";
        	this.getLogger().log(Level.WARNING, msg, e);

            try {
                FileOutputStream out = new FileOutputStream(PROPERTIES_FILE);
                properties.storeToXML(out, "Server Configuration File");
                out.close();
            } catch (IOException e1) {
            	String errorMsg = "Could not write server configuration file: " + PROPERTIES_FILE;
            	this.getLogger().log(Level.WARNING, errorMsg, e1);
            }
        }
        
        return properties;
    	
    }
    
    private void initLogger() {
		this.logger.setLevel(Level.ALL);
		
		// TODO this log Handler is for debug purposes. remove or configure.
		Handler debug = new ConsoleHandler();
		debug.setLevel(Level.ALL);
		this.logger.addHandler(debug);

		// Handler to write logs to file.
		Handler fileHandler = null;
    	try {
			fileHandler = new FileHandler(LOG_FILE_NAME, LOG_FILE_SIZE, LOG_FILE_COUNT);
    	} catch (SecurityException e) {
    		String msg = "Insuffient permissions to write to log files: " + LOG_FILE_NAME;
    		logger.log(Level.SEVERE, msg, e);
		} catch (IOException e) {
    		String msg = "Could not write to log files: " + LOG_FILE_NAME;
    		logger.log(Level.SEVERE, msg, e);
		}
    	if (fileHandler != null) {
    		fileHandler.setLevel(Level.CONFIG);
    		this.logger.addHandler(fileHandler);
    	}
    }
    
    private void initExceptionHandler() {
    	// Set uncaught exception handler
    	Thread.currentThread().setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				String errorMsg = "Unexpected error in main server thread/incoming connection handler.";
				Server.this.getLogger().log(Level.SEVERE, errorMsg, e);
			}
    	});
    }

    /**
     * Start running this server application. Consumes the thread.
     */
    public void start() {
    	// Initialize logger.
    	this.initLogger();

    	// Set uncaught exception handler
    	this.initExceptionHandler();

    	// Read properties.
    	this.properties = this.readProperties();
    	
    	// Read user table.
    	try {
			this.userTable = UserTable.loadFromFile(this.logger);
		} catch (IOException e) {
			throw new FatalError("Error reading user table from file.", e);
		}
    	
        // TODO catch parsing errors and give informative feedback if properties file is invalid.
        int portNumber = Integer.parseInt(properties.getProperty("PORT_NUMBER"));
        String keyStoreFile = properties.getProperty("SERVER_KEY_STORE_FILE");
        String keyStorePass = properties.getProperty("SERVER_KEY_STORE_PASSWORD");
        
        // Start incoming connection listener.
        this.listener = new SecureServerListener(this, portNumber, keyStoreFile, keyStorePass);
        this.listener.start();

    }

}
