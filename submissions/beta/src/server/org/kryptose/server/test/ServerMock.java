package org.kryptose.server.test;

import java.util.logging.Logger;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;
import org.kryptose.server.DataStore;
import org.kryptose.server.Server;

public class ServerMock extends Server {
	
	Logger logger;
	DataStoreMock datastore = new DataStoreMock();

	public ServerMock(Logger logger) {
		this.logger = logger;
	}

	@Override
	public Response handleRequest(Request request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataStore getDataStore() {
		return this.datastore;
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public void start() {
		throw new IllegalAccessError();
	}

}
