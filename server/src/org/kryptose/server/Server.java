package org.kryptose.server;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {

    private static final String PROPERTIES_FILE = "src/org/kryptose/server/serverProperties.xml";
    private static final Object singletonLock = new Object();
    private static Server server;
    // INSTANCE FIELDS
    private final Object workQueueLock = new Object();
    Properties properties;
    private ExecutorService workQueue;

    private DataStore dataStore;
    private Logger logger;
    private SecureServerListener listener;


    // STATIC METHODS

    private Server() {

        this.properties = new Properties();
        FileInputStream in;
        try {
            in = new FileInputStream(PROPERTIES_FILE);
            this.properties.loadFromXML(in);
            in.close();
        } catch (IOException e) {
            properties.setProperty("NUMBER_OF_THREADS", "8");
            properties.setProperty("PORT_NUMBER", "5002");

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

        listener = new SecureServerListener(this, Integer.parseInt(properties.getProperty("PORT_NUMBER")));
    }

    /**
     * Main Kryptose server program.
     *
     * @param args
     */
    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.start();
    }


    // INSTANCE METHODS

    private static Server getInstance() {
        if (server != null) return server;
        synchronized (singletonLock) {
            if (server != null) return server;
            server = new Server();
            return server;
        }
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
        this.workQueue = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("NUMBER_OF_THREADS")));
        this.listener.start();
    }

}
