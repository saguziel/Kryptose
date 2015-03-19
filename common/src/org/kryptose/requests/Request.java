package org.kryptose.requests;

import java.io.Serializable;

import org.kryptose.server.Connection;

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

    public abstract String logEntry();

    public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

}