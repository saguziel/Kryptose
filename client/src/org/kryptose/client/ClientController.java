package org.kryptose.client;

import org.kryptose.requests.RequestGet;
import org.kryptose.requests.ResponseGet;
import org.kryptose.requests.ResponsePut;

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
            if(!model.hasPassFile()){
                ResponseGet r = (ResponseGet)model.reqHandler.send(new RequestGet(model.user));
                model.setPassfile(new PasswordFile(r.getBlob(), model.user.getPassword()));
            }
            model.getCredential(args[1]);
        } else if (args[0] == PUT) {
//            ResponsePut r = (ResponseGet)model.rh.send(new RequestPut(model.user));
        }
	}
    public void handleUserName(String userName) {
        model.setUsername(userName);
    }
	public void handlePassword(String pass) {
		
	}
	
	
}
