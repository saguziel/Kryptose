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
	private SSLServerSocket serverListener;
	
	
    public SecureServerListener(Server server, int port) {
    	this.port = port;
    	this.server = server;
	}
    
    public void start() {
//		System.setProperty("javax.net.ssl.trustStore", "src/org/kryptose/certificates/ClientKeyStore.jks");
//		System.setProperty("javax.net.ssl.trustStorePassword", "aaaaaa");
		// TODO: make this configurable.
	    System.setProperty("javax.net.ssl.keyStore", "src/org/kryptose/certificates/ServerKeyStore.jks");
	    System.setProperty("javax.net.ssl.keyStorePassword", "aaaaaa");
	    
	    ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
	    try {
		    this.serverListener = (SSLServerSocket) ssocketFactory.createServerSocket(port);
		    
		    printServerSocketInfo(serverListener);
		    
//	    	ServerSocket serverListener = new ServerSocket(port);
            while (true) {
        	    SSLSocket clientSocket = (SSLSocket) serverListener.accept();
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
    
	
//For debug purposes only (code was copied from a website)	
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
	
}

