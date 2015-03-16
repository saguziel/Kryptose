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
		serverPort = 5003;

		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		
		clientTrustStore = "src/org/kryptose/certificates/ClientTrustStore.jks";
		clientTrustStorePassword= "aaaaaa"; 
		//System.setProperty("javax.net.debug", "all");
		System.setProperty("javax.net.ssl.trustStore", clientTrustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", clientTrustStorePassword);
		
		sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	RequestHandler(String serverHostname, int serverPort, String clientTrustStore, String clientTrustStorePassword){
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		
		this.clientTrustStore = clientTrustStore;
		this.clientTrustStorePassword= clientTrustStorePassword; 
		//System.setProperty("javax.net.debug", "all");
		System.setProperty("javax.net.ssl.trustStore", clientTrustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", clientTrustStorePassword);
		
		sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	
	Response send(Request req){
        try {
        	
    	    sock = (SSLSocket) sslsocketfactory.createSocket(serverHostname, serverPort);
    	    
    	    sock.setEnabledProtocols(new String[] {"TLSv1.2"});

    	    //TODO: maybe delete this afterwards
    	    //Enable this line to test what happens if client and server have no TLS Version in common
    	    //sock.setEnabledProtocols(new String[] {"TLSv1.1"});
    	    
    	    sock.setEnabledCipherSuites(new String[] {"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"});
    	    
    	    //TODO: maybe delete this afterwards
    	    //Enable this line to test what happens if client and server have no CipherSuites in common
    	    //sock.setEnabledCipherSuites(new String[] {"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA"});
   	    
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
