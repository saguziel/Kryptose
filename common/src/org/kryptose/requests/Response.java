package org.kryptose.requests;

import java.io.Serializable;


/**
 * A response to the client after the client sends a Request.
 * 
 * @author jnshi
 * @see Request
 */
public abstract class Response implements Serializable {
    public abstract String logEntry();

}
