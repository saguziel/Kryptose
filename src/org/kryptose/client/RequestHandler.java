package org.kryptose.client;

import org.kryptose.server.Request;
import java.io.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class RequestHandler {

	static String serverHostname;
	static int serverPort;
	
    SSLSocketFactory sslsocketfactory; 
    SSLSocket sock; 

	ObjectInputStream in;
	ObjectOutputStream out;
//	Socket sock;
	
	static String getServerHostname() {
		return serverHostname;
	}
	static void setServerHostname(String serverHostname) {
		RequestHandler.serverHostname = serverHostname;
	}
	static int getServerPort() {
		return serverPort;
	}
	static void setServerPort(int serverPort) {
		RequestHandler.serverPort = serverPort;
	}
	
	RequestHandler(){
		serverHostname = "127.0.0.1";
		serverPort = 5002;
//		System.setProperty("javax.net.debug", "all");
		System.setProperty("javax.net.ssl.trustStore", "src/org/kryptose/certificates/ClientTrustStore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "aaaaaa");

		
		sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	Request send(Request req){
        try {
        	
//            sock = new Socket(serverHostname, serverPort);
    	    sock = (SSLSocket) sslsocketfactory.createSocket(serverHostname, serverPort);
    	    
//    	    sock.startHandshake();
    	    
            out = new ObjectOutputStream(sock.getOutputStream());                

            out.writeObject(req);
            
            System.out.println("Request sent: " + req.toString());
            
            Request resp;
			try {
	            in = new ObjectInputStream(sock.getInputStream());

				resp = (Request) in.readObject();
	            System.out.println("Response received: " + resp.toString());
	            
	            sock.close();
	      
	            return resp;
			
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
            
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }

		return new Request("SENDING FAILED");
		
	}
	
	public static void main(String[] args) {
		RequestHandler handler = new RequestHandler();
		System.out.println("Client is running");
		handler.send(new Request("-My first request-")).toString();
		handler.send(new Request("-My second request-")).toString();

	}


}
