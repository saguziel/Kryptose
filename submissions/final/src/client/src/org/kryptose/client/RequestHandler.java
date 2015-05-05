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
	
    SSLSocketFactory sslsocketfactory; 
    SSLSocket sock; 

	ObjectInputStream in;
	ObjectOutputStream out;

	RequestHandler(String serverHostname, int serverPort, String clientTrustStore, String clientTrustStorePassword){
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		
		//System.setProperty("javax.net.debug", "all");
		// TODO: it is really sketchy that this constructor sets global state.
		System.setProperty("javax.net.ssl.trustStore", clientTrustStore);
		System.setProperty("javax.net.ssl.trustStorePassword", clientTrustStorePassword);
		
		sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	
	Response send(Request req) throws UnknownHostException, IOException, MalformedRequestException, InvalidCredentialsException, InternalServerErrorException {
        try {

        	sock = (SSLSocket) sslsocketfactory.createSocket(serverHostname, serverPort);

    	    sock.setEnabledProtocols(new String[] {"TLSv1.2"});
    	    
    	    sock.setEnabledCipherSuites(new String[] {"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"});
    	    
            out = new ObjectOutputStream(sock.getOutputStream());                
            
            out.writeObject(req);
            
            //System.out.println("Request sent: " + req.toString());
           
            in = new ObjectInputStream(sock.getInputStream());

            Response resp;
			resp = (Response) in.readObject();
			
			// See if Response was an Exception response, throw if so.
			resp.checkException();
      
	        sock.close();
            return resp;
            
        } catch (ClassNotFoundException | ClassCastException e) {
			// TODO make sure that user is notified about possibility of outdated client.
			sock.close();
			throw new IOException(e);
		} catch(MalformedRequestException| InvalidCredentialsException| InternalServerErrorException e) {
			sock.close();
			throw e;
		}
               
        		
	}

}
