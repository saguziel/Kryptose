/**
 * Each individual Request sent by the client to the Server.
 * The stub here is just for testing communication.
 */
package org.kryptose.server;

import java.io.Serializable;


/**
 * @author Antonio
 *
 */
public class Request implements Serializable{

	/**
	 * @param args
	 */
	private String theRequest;
	
	public Request(String s){
		theRequest = s;
	}
	
	public String toString(){
		return new String(theRequest);
	}
	
	
}
