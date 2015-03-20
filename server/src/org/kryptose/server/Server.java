package org.kryptose.server;

import org.kryptose.requests.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final String PROPERTIES_FILE = "serverProperties.xml";
    
    private static final String LOGGER_NAME = "org.kryptose.server";
    // TODO make configurable?
    private static final int LOG_FILE_COUNT = 20;
    private static final int LOG_FILE_SIZE = 40 * 1024; // bytes
    private static final String LOG_FILE_NAME = "datastore/kryptose.%g.%u.log";
    
    // INSTANCE FIELDS
    Properties properties;

    private DataStore dataStore = new FileSystemDataStore();
    private Logger logger = Logger.getLogger(LOGGER_NAME);
    private SecureServerListener listener;
    
    private Map<User, Object> userLocks = Collections.synchronizedMap(new WeakHashMap<User,Object>());


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
     * Handle the request. Acquire locks and authenticate user, then dispatch.
     *
     * @param request, not null
     * @return
     */
    public Response handleRequest(Request request) {

    	Object userLock;

    	synchronized (this.userLocks) {
    		userLock = this.userLocks.get(request.getUser());
    		if (userLock == null) {
    			userLock = new Object();
    			this.userLocks.put(request.getUser(), userLock);
    		}
    	}

    	synchronized (userLock) {
    		// TODO: user authentication.
    		return this.handleRequestWithLocksAcquired(request);
    	}
    }
    
    /**
     * Dispatch.
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
        } else {
        	//TODO: Maybe include some specific description like
        	// "Unrecognized request type"
            return new ResponseInternalServerError();
        }
    }

    private Response handleRequestGet(RequestGet request) {

        Response response;
        User u = request.getUser();

        boolean hasBlob = this.dataStore.userHasBlob(u);

        if (hasBlob) {
            Blob b = this.dataStore.readBlob(u);

            if (b == null) {
                response = new ResponseInternalServerError();
            } else {
                response = new ResponseGet(b, null); // TODO logging
            }
        } else {
        	// User has not yet stored a blob.
            response = new ResponseGet(null, null);
        }

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
                    response = new ResponsePut(u, toBeWritten.getDigest());
                } catch (CryptoPrimitiveNotSupportedException e) {
                    // TODO Auto-generated catch block
            		e.printStackTrace();
                    response = new ResponseInternalServerError();
                }
                break;
            case STALE_WRITE:
            	try {
                    response = new ResponseStaleWrite(u, oldDigest, toBeWritten.getDigest());
                } catch (CryptoPrimitiveNotSupportedException e) {
                    // TODO Auto-generated catch block
            		e.printStackTrace();
                    response = new ResponseInternalServerError();
                }
                break;
            case USER_DOES_NOT_EXIST: // we should have authenticated by now.
            case INTERNAL_ERROR:
            default:
                response = new ResponseInternalServerError();
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
    
    // TODO: I copied and pasted this without taking note of preconditions/postconditions.
    private Properties readProperties() {
        Properties properties = new Properties();

        //SETTING DEFAULT CONFIGURATIONS (can be overriden by the Server settings file
        // TODO: do not silently set defaults. if something went wrong when reading
        // the configuration file,
        // the admins need to know about it.
        properties.setProperty("NUMBER_OF_THREADS", "8");
        properties.setProperty("PORT_NUMBER", "5002");

        properties.setProperty("SERVER_KEY_STORE_FILE", "src/org/kryptose/certificates/ServerKeyStore.jks");
        properties.setProperty("SERVER_KEY_STORE_PASSWORD", "aaaaaa");


        FileInputStream in;

        try {
            in = new FileInputStream(PROPERTIES_FILE);
            Properties XMLProperties = new Properties();
            XMLProperties.loadFromXML(in);
            properties.putAll(XMLProperties);
            in.close();
        } catch (IOException e) {
        	//TODO: Unable to read the properties file. Maybe log the error?

            try {
                FileOutputStream out = new FileOutputStream(PROPERTIES_FILE);
                properties.storeToXML(out, "Server Configuration File");
                out.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                //Give up.
                e1.printStackTrace();
            }
        }
        
        return properties;
    	
    }

    // consumes thread.
    public void start() {

    	// Read properties.
    	this.properties = this.readProperties();
    	
    	// Initialize logger.
    	try {
			this.logger.setLevel(Level.ALL);
			
			Handler fileHandler = new FileHandler(LOG_FILE_NAME, LOG_FILE_SIZE, LOG_FILE_COUNT);
			fileHandler.setLevel(Level.CONFIG);
			this.logger.addHandler(fileHandler);
			
			// TODO this logger for debug purposes. remove or configure.
			Handler debug = new ConsoleHandler();
			debug.setLevel(Level.ALL);
			this.logger.addHandler(debug);
			
    	} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Set uncaught exception handler
    	Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				String errorMsg = "Unexpected error in main server thread/incoming connection handler.";
				System.err.println(errorMsg);
				e.printStackTrace();
				Server.this.getLogger().log(Level.SEVERE, errorMsg, e);
			}
    	});
    	
        // TODO catch parsing errors and give informative feedback if properties file is invalid.
        int portNumber = Integer.parseInt(properties.getProperty("PORT_NUMBER"));
        String keyStoreFile = properties.getProperty("SERVER_KEY_STORE_FILE");
        String keyStorePass = properties.getProperty("SERVER_KEY_STORE_PASSWORD");
        
        // Start incoming connection listener.
        this.listener = new SecureServerListener(this, portNumber, keyStoreFile, keyStorePass);
        this.listener.start();

    }

}
