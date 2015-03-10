package org.kryptose.server;

import org.kryptose.requests.Request;

public class Server {

	private static final Object singletonLock = new Object();
	
	private static Server server;
	
	private final Object workQueueLock = new Object();
	
	/**
	 * Main Kryptose server program.
	 * @param args
	 */
	public static void main(String[] args) {
		Server server = Server.getInstance();
		server.start();
	}
	
	private static Server getInstance() {
		if (server != null) return server;
		synchronized (singletonLock) {
			if (server != null) return server;
			server = new Server();
			return server;
		}
	}
	
	
	// INSTANCE METHODS
	
	private Server() {
		// TODO
	}
	
	public void addToWorkQueue(Request request) {
		synchronized(this.workQueueLock) {
			// TODO
		}
	}
	
	public DataStore getDataStore() {
		// TODO
		return null;
	}
	
	public Logger getLogger() {
		// TODO
		return null;
	}
	
	public void start() {
		// TODO
	}

}
