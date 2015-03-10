package org.kryptose.requests;

import java.io.Serializable;

import org.kryptose.server.Server;
import org.kryptose.server.User;

public abstract class Request implements Serializable, Runnable {

	private transient Server server;
	private transient User user;
	
	public void init(Server server, User user) {
		this.server = server;
		this.user = user;
	}
	
	protected Server getServer() {
		if (server != null) return server;
		throw new IllegalStateException("Request object run before initialized.");
	}
	
	protected User getUser() {
		if (user != null) return user;
		throw new IllegalStateException("Request object run before initialized.");
	}
	
}