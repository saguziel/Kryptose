package org.kryptose.server;


import java.io.IOException;
import java.util.logging.Level;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;


class SecureServerListener{
	
	private final int port;
	private final Server server;
	private final String serverKeyStore;
	private final String serverKeyStorePassword;

	private SSLServerSocket serverListener;
	
    public SecureServerListener(Server server, int port, String serverKeyStore, String serverKeyStorePassword) {
    	this.port = port;
    	this.server = server;
    	this.serverKeyStore = serverKeyStore;
    	this.serverKeyStorePassword = serverKeyStorePassword;
	}
	
    private void init() {
	    System.setProperty("javax.net.ssl.keyStore", serverKeyStore);
	    System.setProperty("javax.net.ssl.keyStorePassword", serverKeyStorePassword);
		System.setProperty("javax.net.ssl.trustStore", serverKeyStore);
		System.setProperty("javax.net.ssl.trustStorePassword", serverKeyStorePassword);
		
	    ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();

	    try {
		    this.serverListener = (SSLServerSocket) ssocketFactory.createServerSocket(port);
		    
    	    this.serverListener.setEnabledProtocols(new String[] {"TLSv1.2"});

    	    this.serverListener.setEnabledCipherSuites(new String[] {
    	    		"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
    	    		"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"
    	    		});
		    
    	    this.server.getLogger().log(Level.CONFIG, printServerSocketInfo(this.serverListener));
		    
        } catch (IOException ex) {
    		String errorMsg = "Error setting up socket to listen to incoming connections... exiting program.";
			this.server.getLogger().log(Level.SEVERE, errorMsg, ex);
			//throw new RuntimeException(); // TODO need a better way to do this.
			System.exit(2); // TODO need a better way to do this?
        }
    	
    }
    
    private void listenForClient() {
    	SSLSocket clientSocket;
    	
    	// Wait for and accept connection.
    	try {
    		clientSocket = (SSLSocket) serverListener.accept();
    	} catch (IOException ex) {
    		String errorMsg = "Error accepting an incoming connection; ignoring.";
			this.server.getLogger().log(Level.INFO, errorMsg, ex);
			return; // abort current connection attempt.
		}

    	Connection connection = new Connection(clientSocket);
    	// Log connection.
		this.server.getLogger().log(Level.CONFIG, printSocketInfo(connection));

		// Handle connection.
		Runnable job = new ClientHandler(server, clientSocket, connection);
		Thread t = new Thread(job);
		t.start();
    }
    
    private void run() {
    	while (true) {
    		this.listenForClient();
    		
    		// Something presumably wants this thread to stop.
    		if (Thread.interrupted()) {
                break;
    		}
    		
    		// Can't do anything once the serversocket is closed.
    		if (serverListener.isClosed()) {
                break;
    		}
    	}
    }
    
    /**
     * This method does not return until the server is shut down.
     */
    public void start() {

    	try {
    		// Set initial properties
    		this.init();

    		// Listen for incoming connections.
    		this.run();
    	}
    	finally {
        	if (this.serverListener != null && !this.serverListener.isClosed()) {
        		try {
    				this.serverListener.close();
    			} catch (IOException e) {
    	    		String errorMsg = "Error closing server socket.";
    				this.server.getLogger().log(Level.WARNING, errorMsg, e);
    			}
        	}
    	}
    }
	
    @Override
    protected void finalize() {
    	if (this.serverListener != null && !this.serverListener.isClosed()) {
    		try {
				this.serverListener.close();
			} catch (IOException e) {
	    		String errorMsg = "Error closing server socket.";
				this.server.getLogger().log(Level.WARNING, errorMsg, e);
			}
    	}
    }

    
	
	/**
	 * For test purposes
	 * TODO: remove.
	 * @param args
	 */
	 private static String printSocketInfo(Connection connection) {
		 SSLSocket s = connection.getSocket();
		  StringBuilder builder = new StringBuilder();

	      builder.append("Accepting an incoming connection: " + connection.getId());
	      builder.append("\tTime: " + connection.getTime());
	      builder.append("\n");
	      builder.append("\n");
		  builder.append("Socket class: " + s.getClass());
	      builder.append("\n");
		  builder.append("\tRemote address = " + s.getInetAddress());
	      builder.append("\n");
		  builder.append("\tRemote port = " + s.getPort());
	      builder.append("\n");
		  builder.append("\tLocal socket address = " + s.getLocalSocketAddress());
	      builder.append("\n");
		  builder.append("\tLocal address = " + s.getLocalAddress());
	      builder.append("\n");
		  builder.append("\tLocal port = "+s.getLocalPort());
	      builder.append("\n");
		  builder.append("\tNeed client authentication = " +s.getNeedClientAuth());
	      builder.append("\n");
		  
	      SSLSession ss = s.getSession();
	      builder.append("\tCipher suite = "+ss.getCipherSuite());
	      builder.append("\n");
	      builder.append("\tProtocol = "+ss.getProtocol());
	      builder.append("\n");
	      builder.append("\n");
	      
	      return builder.toString();
	   }
	 
		/**
		 * @param s
		 */
	   private static String printServerSocketInfo(SSLServerSocket s) {
		  StringBuilder builder = new StringBuilder();

	      builder.append("Listening for incoming connections...");
	      builder.append("\n");
	      builder.append("Server socket class: "+s.getClass());
	      builder.append("\n");
	      builder.append("\tSocket address = "  +s.getInetAddress());
	      builder.append("\n");
	      builder.append("\tSocket port = " +s.getLocalPort());
	      builder.append("\n");
	      builder.append("\tNeed client authentication = " +s.getNeedClientAuth());
	      builder.append("\n");
	      builder.append("\tWant client authentication = " +s.getWantClientAuth());
	      builder.append("\n");
	      builder.append("\tUse client mode = " +s.getUseClientMode());
	      builder.append("\n");
	      builder.append("\n");
	      
	      return builder.toString();
	   } 
/*

	   //TODO: remove afterwards. For testing only
	   public static void main(String[] args) {
		   		System.setProperty("javax.net.debug", "all");
		   		System.out.println("Server is runningAA");
		   		SecureServerListener listener = new SecureServerListener(5002);
		   		listener.start();
		   		System.out.println("Server is runningAA");
		   		
		   	}

	   
	    //TODO: remove afterwards. For testing only
	    public SecureServerListener(int port) {
	    	this.port = port;
	    	this.server = null;
	    	this.serverKeyStore = "src/org/kryptose/certificates/ServerKeyStore.jks";
	    	this.serverKeyStorePassword = "aaaaaa";
		}


	//TODO: remove this constructor once we are using the other one (which explicitly sets the serverKeyStore)
    public SecureServerListener(Server server, int port) {
    	this.port = port;
    	this.server = server;
    	this.serverKeyStore = "src/org/kryptose/certificates/ServerKeyStore.jks";
    	this.serverKeyStorePassword = "aaaaaa";
	}

*/
	
}

