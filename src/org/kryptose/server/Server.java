package org.kryptose.server;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;
import org.kryptose.server.requesthandlers.RequestHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {

	// TODO: make these constants configurable.
	private static final int NUMBER_OF_THREADS = 8;
	private static final int PORT_NUMBER = 5002;
	
	private static final Object singletonLock = new Object();
	private static Server server;
	
	// INSTANCE FIELDS
	private final Object workQueueLock = new Object();
	private ExecutorService workQueue;
	
	private List<RequestHandler<? extends Request>> requestHandlers;
	private DataStore dataStore;
	private Logger logger;
	private SecureServerListener listener = new SecureServerListener(this, PORT_NUMBER);
	
	
	// STATIC METHODS
	
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
		this.requestHandlers = new ArrayList<RequestHandler<? extends Request>>();
		// TODO: populate list of RequestHandlers
		
	}
	
	/**
	 * Queue client request for processing.
	 * 
	 * @param user
	 * @param request
	 * @return
	 */
	public Future<Response> addToWorkQueue(User user, Request request) {
		Callable<Response> callable = null;
		for (RequestHandler<? extends Request> handler : this.requestHandlers) {
			if (handler.canHandleRequest(request)) {
				callable = handler.handleRequest(this, user, request);
			}
		}
		
		if (callable == null) {
			// TODO: Request can't be handled?
		}
		
		synchronized(this.workQueueLock) {
			return this.workQueue.submit(callable);
		}
	}
	
	public DataStore getDataStore() {
		// TODO Server DataStore
		return null;
	}
	
	public Logger getLogger() {
		// TODO Server Logger
		return null;
	}
	
	public void start() {
		this.workQueue = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		this.listener.start();
	}

}
