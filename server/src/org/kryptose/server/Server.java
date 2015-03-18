package org.kryptose.server;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {

    private static final String PROPERTIES_FILE = "serverProperties.xml";
    // INSTANCE FIELDS
    private final Object workQueueLock = new Object();
    Properties properties;
    private ExecutorService workQueue;

    private DataStore dataStore;
    private Logger logger;
    private SecureServerListener listener;


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
        this.listener = new SecureServerListener(this, Integer.parseInt(properties.getProperty("PORT_NUMBER")));
    }
    
    /**
     * Queue client request for processing.
     *
     * @param user
     * @param request
     * @return
     */
    public Future<Response> addToWorkQueue(Request request) {
        synchronized (this.workQueueLock) {
            return this.workQueue.submit(new HandledRequest(request));
        }
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
        
        this.workQueue = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("NUMBER_OF_THREADS")));
        this.listener.start();
    }

}
