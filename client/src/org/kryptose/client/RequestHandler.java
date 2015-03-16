package org.kryptose.client;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;
import org.kryptose.requests.TestRequest;
import org.kryptose.requests.TestResponse;

import java.io.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


// TODO: rename this class.
public class RequestHandler {

	static String serverHostname;
	static int serverPort;
	static String clientTrustStore; 
	static String clientTrustStorePassword;
	
    SSLSocketFactory sslsocketfactory; 
    SSLSocket sock; 

	ObjectInputStream in;
	ObjectOutputStream out;
	
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

		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		
		clientTrustStore = "src/org/kryptose/certificates/ClientTrustStore.jks";
		clientTrustStorePassword= "aaaaaa"; 
		System.setProperty("javax.net.debug", "all");
		System.setProperty("javax.net.ssl.trustStore", clientTrustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", clientTrustStorePassword);
		
		sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	RequestHandler(String serverHostname, int serverPort, String clientTrustStore, String clientTrustStorePassword){
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		
		this.clientTrustStore = clientTrustStore;
		this.clientTrustStorePassword= clientTrustStorePassword; 
		System.setProperty("javax.net.debug", "all");
		System.setProperty("javax.net.ssl.trustStore", clientTrustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", clientTrustStorePassword);
		
		sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	
	Response send(Request req){
        try {
        	
    	    sock = (SSLSocket) sslsocketfactory.createSocket(serverHostname, serverPort);
    	    
    	    sock.setEnabledProtocols(new String[] {"TLSv1.2"});
//    	    sock.setEnabledCipherSuites();
    	    
            out = new ObjectOutputStream(sock.getOutputStream());                

            out.writeObject(req);
            
            System.out.println("Request sent: " + req.toString());
            
            Response resp;
			try {
	            in = new ObjectInputStream(sock.getInputStream());

				resp = (Response) in.readObject();
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

		return new TestResponse("SENDING FAILED");
		
	}
	
	public static void main(String[] args) {
		RequestHandler handler = new RequestHandler();
		System.out.println("Client is running");
		handler.send(new TestRequest("-My first request-")).toString();
		handler.send(new TestRequest("-My second request-")).toString();

	}


}
