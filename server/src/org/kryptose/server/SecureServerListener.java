package org.kryptose.server;


import java.io.IOException;

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
	
    //TODO: remove this constructor once we are using the other one (which explicitly sets the serverKeyStore)
    public SecureServerListener(Server server, int port) {
    	this.port = port;
    	this.server = server;
    	this.serverKeyStore = "src/org/kryptose/certificates/ServerKeyStore.jks";
    	this.serverKeyStorePassword = "aaaaaa";
	}
    
    public void start() {
	    System.setProperty("javax.net.ssl.keyStore", serverKeyStore);
	    System.setProperty("javax.net.ssl.keyStorePassword", serverKeyStorePassword);
		System.setProperty("javax.net.ssl.trustStore", serverKeyStore);
		System.setProperty("javax.net.ssl.trustStorePassword", serverKeyStorePassword);

	    
	    ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
	    try {
		    this.serverListener = (SSLServerSocket) ssocketFactory.createServerSocket(port);
		    
    	    this.serverListener.setEnabledProtocols(new String[] {"TLSv1.2"});
    	    this.serverListener.setEnabledCipherSuites(new String[] {"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"});
    	    
		    
		    //TODO: remove afterwards
		    printServerSocketInfo(serverListener);
		    
            while (true) {
        	    SSLSocket clientSocket = (SSLSocket) serverListener.accept();

        	    //TODO: remove afterwards
        	    printSocketInfo(clientSocket);
                
        	    Runnable job = new ClientHandler(server, clientSocket);
        	    Thread t = new Thread(job);
                t.start();
                //System.out.println("got a connection");
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    	
    }
	
    @Override
    protected void finalize() {
    	if (this.serverListener != null && !this.serverListener.isClosed()) {
    		try {
				this.serverListener.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
	
	/**
	 * For test purposes
	 * TODO: remove.
	 * @param args
	 */
	 private static void printSocketInfo(SSLSocket s) {
	      System.out.println("Socket class: "+s.getClass());
	      System.out.println("   Remote address = "
	         +s.getInetAddress().toString());
	      System.out.println("   Remote port = "+s.getPort());
	      System.out.println("   Local socket address = "
	         +s.getLocalSocketAddress().toString());
	      System.out.println("   Local address = "
	         +s.getLocalAddress().toString());
	      System.out.println("   Local port = "+s.getLocalPort());
	      System.out.println("   Need client authentication = "
	         +s.getNeedClientAuth());
	      SSLSession ss = s.getSession();
	      System.out.println("   Cipher suite = "+ss.getCipherSuite());
	      System.out.println("   Protocol = "+ss.getProtocol());
	   }
		/**
		 * For test purposes
		 * TODO: remove.
		 * @param args
		 */
	   private static void printServerSocketInfo(SSLServerSocket s) {
	      System.out.println("Server socket class: "+s.getClass());
	      System.out.println("   Socket address = "
	         +s.getInetAddress().toString());
	      System.out.println("   Socket port = "
	         +s.getLocalPort());
	      System.out.println("   Need client authentication = "
	         +s.getNeedClientAuth());
	      System.out.println("   Want client authentication = "
	         +s.getWantClientAuth());
	      System.out.println("   Use client mode = "
	         +s.getUseClientMode());
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
*/
	
}

