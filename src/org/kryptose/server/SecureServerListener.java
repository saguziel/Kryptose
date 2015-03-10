package org.kryptose.server;

import java.io.*;

import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.kryptose.requests.TestRequest;



class SecureServerListener{
	
    private static class ClientHandler implements Runnable {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket sock;
        
        ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                in = new ObjectInputStream(sock.getInputStream());
                // TODO: this is a blocking call (if the stream is empty). Provide a timeout for threads, or someone can just spawn new threads
                // and exhaust resources
                out = new ObjectOutputStream(sock.getOutputStream());                
            } catch (Exception ex) {
            	// TODO
            	ex.printStackTrace();
            }
        }
        
        public void run() {
            Object o1;
            System.out.println("Connection thread running");
            try {
                o1 = in.readObject();
                
                System.out.println("Received one request: " + o1.toString());
                out.writeObject(new TestRequest("Server got and accepted the request " + o1.toString()));
                // TODO: What do we do with the object received? I would think of generating an event "RequestReceived"
                // so that other classes could listen to/wait for it connect to it.

                in.close();
                out.close();
            } catch (Exception ex) {
            	// TODO
            	ex.printStackTrace();
            }
        }
    }

	
	SecureServerListener(int port) {
//		System.setProperty("javax.net.ssl.trustStore", "src/org/kryptose/certificates/ClientKeyStore.jks");
//		System.setProperty("javax.net.ssl.trustStorePassword", "aaaaaa");
		// TODO: make this configurable.
	    System.setProperty("javax.net.ssl.keyStore", "src/org/kryptose/certificates/ServerKeyStore.jks");
	    System.setProperty("javax.net.ssl.keyStorePassword", "aaaaaa");
	    
	    ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
	    try {
		    SSLServerSocket serverListener = (SSLServerSocket) ssocketFactory.createServerSocket(port);
		    
		    printServerSocketInfo(serverListener);
		    
//	    	ServerSocket serverListener = new ServerSocket(port);
            while(true) {
        	    SSLSocket clientSocket = (SSLSocket) serverListener.accept();
        	    printSocketInfo(clientSocket);
                Runnable job = new ClientHandler(clientSocket);
        	    Thread t = new Thread(job);
//                System.out.println("Thread created");
                t.start();
                System.out.println("got a connection");
            }
//            serverListener.close();
        } catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public static void main(String[] args) {
//		System.setProperty("javax.net.debug", "all");
		System.out.println("Server is running");
		SecureServerListener listener = new SecureServerListener(5002);
	}

//For debug purposes only (code was copied from a website)	
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

