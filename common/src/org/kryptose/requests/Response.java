package org.kryptose.requests;

import java.io.Serializable;

import org.kryptose.exceptions.ServerException;


/**
 * A response to the client after the client sends a Request.
 * 
 * @author jnshi
 * @see Request
 */
public abstract class Response implements Serializable {
	
	private final ServerException exception;
	
	public Response() {
		this.exception = null;
	}
	
	public Response(ServerException exception) {
		this.exception = exception;
	}
	
    public abstract String logEntry();
    
    public ServerException getException() {
    	return this.exception;
    }
    
    public void checkException() throws ServerException {
    	if (this.getException() != null) {
    		throw this.getException();
    	}
    }

}
