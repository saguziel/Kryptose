package org.kryptose.requests;

import java.io.Serializable;

import org.kryptose.exceptions.InternalServerErrorException;
import org.kryptose.exceptions.InvalidCredentialsException;
import org.kryptose.exceptions.MalformedRequestException;


/**
 * A response to the client after the client sends a Request.
 * 
 * @author jnshi
 * @see Request
 */
public abstract class Response implements Serializable {
	private static final long serialVersionUID = -2677279346126015739L;

	public Response() {
	}


	public abstract String logEntry();
    
    public void checkException() throws InternalServerErrorException,
    									MalformedRequestException,
    									InvalidCredentialsException {
    }

}
