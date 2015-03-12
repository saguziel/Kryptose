package org.kryptose.server.requesthandlers;

import java.util.concurrent.Callable;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;
import org.kryptose.server.Server;
import org.kryptose.server.User;

/**
 * Create Callable objects that enact Requests from the client.
 * 
 * Every RequestHandler instance must be stateless:
 * RequestHandlers are expected to be accessed from multiple threads concurrently.
 * 
 * @author jshi
 *
 * @param <T>
 */
public abstract class RequestHandler<T extends Request> {
	
	private final Class<T> requestType;

	protected RequestHandler(Class<T> requestType) {
		if (requestType == null) {
			throw new IllegalArgumentException("RequestHandler constructor invoked with null");
		}
		this.requestType = requestType;
	}
	
	/**
	 * 
	 * @param server
	 * @param user
	 * @param request
	 * @return
	 */
	protected abstract Callable<Response> handleRequestImpl(Server server, User user, T request);
	
	/**
	 * 
	 * @param server
	 * @param user
	 * @param request
	 * @return
	 */
	public Callable<Response> handleRequest(Server server, User user, Request request) {
		if (!canHandleRequest(request)) {
			return null; // or throw exception?
		}
		T t = requestType.cast(request);
		return this.handleRequestImpl(server, user, t);
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public boolean canHandleRequest(Request request) {
		return requestType.isInstance(request);
	}
	
}