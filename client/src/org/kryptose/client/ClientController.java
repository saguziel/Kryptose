package org.kryptose.client;

import org.kryptose.requests.GetRequest;
import org.kryptose.requests.ResponseGet;

public class ClientController {

    final String GET = "get";
    final String PUT = "put";

	Client model;
	
	public ClientController(Client c) {
		this.model = c;
	}
	
	public void handleRequest(String request) {
		String[] args = request.trim().toLowerCase().split("\\s+");
        if (args[0] == GET) {
            ResponseGet r = (ResponseGet)model.rh.send(new GetRequest(model.user));
        } else if (args[0] == PUT) {

        }
	}
    public void handleUserName(String userName) {
        model.setUsername(userName);
    }
	public void handlePassword(String pass) {
		
	}
	
	
}
