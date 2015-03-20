package org.kryptose.requests;

import java.io.Serializable;

import org.kryptose.server.Connection;

/**
 * A request to send from the client to the server.
 * 
 * Includes Connection information recorded by the server when
 * the request has been received.
 * 
 * @author jnshi
 */
public abstract class Request implements Serializable {

    private final User user;
    private transient Connection connection;

	public Request(User user) {
        super();
        this.user = user;
    }

    public final User getUser() {
        // TODO: consider security implications of this being public.
        return user;
    }

    /**
     * Checks that the instance's fields satisfy specified invariants
     * and defensively copies mutable fields.
     */
    public void validateInstance() {
        if (this.user == null) throw new IllegalArgumentException("user is null");
        this.user.validateInstance();
    }

    /**
     * Returns a String used to make a log of this request.
     * @return
     */
    public abstract String logEntry();

    /**
     * Gets information about the connection from which this request originated.
     * To be called on the server.
     * @return
     */
    public Connection getConnection() {
		return connection;
	}

    /**
     * Stores information about the connection from which this request originated.
     * To be called on the server.
     * @param connection
     */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}