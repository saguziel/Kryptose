package org.kryptose.server;

import org.kryptose.requests.Blob;
import org.kryptose.requests.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.Request;
import org.kryptose.requests.RequestGet;
import org.kryptose.requests.RequestPut;
import org.kryptose.requests.Response;
import org.kryptose.requests.ResponseGet;
import org.kryptose.requests.ResponseInternalServerError;
import org.kryptose.requests.ResponseInvalidCredentials;
import org.kryptose.requests.ResponsePut;
import org.kryptose.requests.ResponseStaleWrite;
import org.kryptose.requests.RequestTest;
import org.kryptose.requests.ResponseTest;
import org.kryptose.requests.User;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Server {

    private static final String PROPERTIES_FILE = "serverProperties.xml";
    // INSTANCE FIELDS
    Properties properties;

    private DataStore dataStore;
    private Logger logger;
    private SecureServerListener listener;
    
    private Map<User, Object> userLocks = Collections.synchronizedMap(new HashMap<User,Object>());


    // STATIC METHODS

    /**
     * Main Kryptose server program.
     *
     * @param args
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }


    // INSTANCE METHODS

    private Server() {
    }
        
    /**
     * Handle the request. Acquire locks and authenticate user, then dispatch.
     *
     * @param request, not null
     * @return
     */
    public Response handleRequest(Request request) {
    	try {
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
    	finally {
    		this.userLocks.remove(request.getUser());
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
        } else if (request instanceof RequestTest) {
        	return this.handleRequestTest((RequestTest) request);
        } else {
            return new ResponseInternalServerError();
        }
    }

    private Response handleRequestGet(RequestGet request) {
        User u = request.getUser();
        DataStore ds = FileSystemDataStore.getInstance();

        boolean hasBlob = ds.userHasBlob(u);
        if (hasBlob) {
            Blob b = ds.readBlob(u);
            if (b == null) {
                return new ResponseInternalServerError();
            } else {
                return new ResponseGet(b, null); // TODO logging
            }
        } else {
            return new ResponseInvalidCredentials(u);
        }
    }

    private Response handleRequestPut(RequestPut request) {
        User u = request.getUser();
        DataStore ds = FileSystemDataStore.getInstance(); // TODO: make this better
        byte[] oldDigest = request.getOldDigest();
        Blob toBeWritten = request.getBlob();

        DataStore.WriteResult writeResult = ds.writeBlob(u, toBeWritten, oldDigest);
        switch (writeResult) {
            case SUCCESS:
            	try {
            		return new ResponsePut(u, toBeWritten.getDigest());
            	} catch (CryptoPrimitiveNotSupportedException e) {
            		// TODO Auto-generated catch block
            		e.printStackTrace();
            		return new ResponseInternalServerError();
            	}
            case STALE_WRITE:
            	try {
            		return new ResponseStaleWrite(u, oldDigest, toBeWritten.getDigest());
            	} catch (CryptoPrimitiveNotSupportedException e) {
            		// TODO Auto-generated catch block
            		e.printStackTrace();
            		return new ResponseInternalServerError();
            	}
            case USER_DOES_NOT_EXIST: // we should have authenticated by now.
            case INTERNAL_ERROR:
            default:
                return new ResponseInternalServerError();
        }
    }
    
    private Response handleRequestTest(RequestTest request) {
    	return new ResponseTest(request.toString());
    }
    
    public DataStore getDataStore() {
        // TODO Server DataStore
        return null;
    }

    public Logger getLogger() {
        // TODO Server Logger
        return null;
    }

    public void start() {
        this.properties = new Properties();

        //SETTING DEFAULT CONFIGURATIONS (can be overriden by the Server settings file
        // TODO: do not silently set defaults. if something went wrong when reading the configuration file,
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
            this.properties.putAll(XMLProperties);
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

        // TODO catch parsing errors and give informative feedback if properties file is invalid.
        int portNumber = Integer.parseInt(properties.getProperty("PORT_NUMBER"));
        String keyStoreFile = properties.getProperty("SERVER_KEY_STORE_FILE");
        String keyStorePass = properties.getProperty("SERVER_KEY_STORE_PASSWORD");
        
        this.listener = new SecureServerListener(this, portNumber, keyStoreFile, keyStorePass);
        this.listener.start();
    }

}
