package org.kryptose.client;

import org.kryptose.exceptions.InternalServerErrorException;
import org.kryptose.exceptions.InvalidCredentialsException;
import org.kryptose.exceptions.MalformedRequestException;
import org.kryptose.requests.Request;
import org.kryptose.requests.Response;

import java.io.*;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class RequestHandler {

	String serverHostname;
	int serverPort;
	String clientTrustStore; 
	String clientTrustStorePassword;
	
    SSLSocketFactory sslsocketfactory; 
    SSLSocket sock; 

	ObjectInputStream in;
	ObjectOutputStream out;

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

	
	Response send(Request req) throws UnknownHostException, IOException, MalformedRequestException, InvalidCredentialsException, InternalServerErrorException {
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
            
            //System.out.println("Request sent: " + req.toString());
           
            in = new ObjectInputStream(sock.getInputStream());

            Response resp;
			resp = (Response) in.readObject();
            // TODO: catch ClassCastException and handle it.
			
			// See if Response was an Exception response, throw if so.
			resp.checkException();

			//TODO: remove later (testing only).
			//System.out.println("Response received: " + resp.toString());
      
	        sock.close();
            return resp;
            
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
        	//e.printStackTrace();
			sock.close();
			throw new IOException(e);
		}catch(MalformedRequestException| InvalidCredentialsException| InternalServerErrorException e){
			sock.close();
			throw e;
		}
               
        		
	}
	
/*
	//TODO: Constructor for testing only (has common parameters built in). Remove later.
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
		
	public static void main(String[] args) {
		RequestHandler handler = new RequestHandler();
		System.out.println("Client is running");
		handler.send(new RequestTest("-My first request-")).toString();
		handler.send(new RequestTest("-My second request-")).toString();

	}
	
*/

}
