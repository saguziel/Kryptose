package org.kryptose.client;

import org.kryptose.requests.RequestGet;
import org.kryptose.requests.ResponseGet;
import org.kryptose.requests.ResponsePut;
import org.kryptose.requests.RequestPut;
import org.kryptose.requests.Blob;

public class ClientController {

    final String GET = "get";
    final String PUT = "put";
    final String LOGOUT = "logout";

	Client model;
	
	public ClientController(Client c) {
		this.model = c;
	}
	
	public void handleRequest(String request) {
		String[] args = request.trim().toLowerCase().split("\\s+");
        if (args[0].equals(GET)) {
            if(!model.hasPassFile()){
                ResponseGet r = (ResponseGet)model.reqHandler.send(new RequestGet(model.user));
                try {
                    model.setPassfile(new PasswordFile(r.getBlob(), model.user.getPassword()));
                } catch (PasswordFile.BadBlobException e) {
                    model.badMasterPass();
                }
            }
            model.getCredential(args[1]);
        } else if (args[0].equals(PUT)) {
            Blob newBlob = model.passfile.encryptBlob(model.user.getPassword());
            //TODO: use correct digest
            ResponsePut r = (ResponsePut)model.reqHandler.send(new RequestPut(model.user, newBlob, model.user.getPassword().getBytes()));
        } else if (args[0].equals(LOGOUT)) {
            model.logout();
        }
	}
    public void handleUserName(String userName) {
        model.setUsername(userName);
    }
	public void handlePassword(String pass) {
		
	}
	
	
}
