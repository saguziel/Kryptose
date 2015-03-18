package org.kryptose.client;

import org.kryptose.client.PasswordFile.BadBlobException;
import org.kryptose.requests.CryptoErrorException;
import org.kryptose.requests.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.RequestGet;
import org.kryptose.requests.ResponseGet;
import org.kryptose.requests.ResponsePut;
import org.kryptose.requests.RequestPut;
import org.kryptose.requests.Blob;

public class ClientController {

    final String GET = "get";
    final String PUT = "save";
    final String SET = "set";
    final String DEL = "del";
    final String LOGOUT = "logout";

	Client model;
	
	public ClientController(Client c) {
		this.model = c;
	}
	
	public void handleRequest(String request) throws CryptoErrorException, BadBlobException {
		String[] args = request.trim().toLowerCase().split("\\s+");
        if (args[0].equals(GET)) {
            if(!model.hasPassFile()){
                ResponseGet r = (ResponseGet)model.reqHandler.send(new RequestGet(model.user));
                try {
                    model.setPassfile(new PasswordFile(model.user.getUsername(), r.getBlob(), model.getFilepass()));
                } catch (PasswordFile.BadBlobException e) {
                    model.badMasterPass();
                }
            }
            model.getCredential(args[1]);
        } else if (args[0].equals(PUT)) {
            Blob newBlob = model.passfile.encryptBlob(model.getFilepass());
            //TODO: use correct digest
            ResponsePut r = (ResponsePut)model.reqHandler.send(new RequestPut(model.user, newBlob, "".getBytes()));
        } else if (args[0].equals(SET)) {
            model.setVal(args[1], args[2]);
        } else if (args[0].equals(DEL)) {
            model.delVal(args[1]);
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
