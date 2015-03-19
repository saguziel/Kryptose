package org.kryptose.client;

import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.kryptose.client.PasswordFile.BadBlobException;
import org.kryptose.requests.CryptoErrorException;
import org.kryptose.requests.CryptoPrimitiveNotSupportedException;
import org.kryptose.requests.RequestGet;
import org.kryptose.requests.ResponseGet;
import org.kryptose.requests.ResponsePut;
import org.kryptose.requests.RequestPut;
import org.kryptose.requests.Blob;

public class ClientController {

    static final String GET = "get";
    static final String SAVE = "save";
    static final String SET = "set";
    static final String DEL = "del";
    static final String QUERY = "query";
    static final String PRINT = "print";
    static final String LOGOUT = "logout";
    static final String[] KEYWORDS = new String[] {GET, SAVE, SET, DEL, QUERY, PRINT, LOGOUT};

	Client model;
	
	public ClientController(Client c) {
		this.model = c;
	}
	public void fetch() throws CryptoErrorException, BadBlobException {
        ResponseGet r = (ResponseGet) model.reqHandler.send(new RequestGet(model.user));
        if(r.getBlob() == null){
            model.newPassFile();
        } else {
            try {
                model.setPassfile(new PasswordFile(model.user.getUsername(), r.getBlob(), model.getFilepass()));
            } catch (PasswordFile.BadBlobException e) {
                model.badMasterPass();
            }
        }
    }

	public void handleRequest(String request) throws CryptoErrorException, BadBlobException {
		String[] args = request.trim().toLowerCase().split("\\s+");
        if (args[0].equals(GET)) {
            fetch();
        } else if (args[0].equals(QUERY)) {
            if(!model.hasPassFile()) {
                model.continuePrompt("Please run get first");
            } else {
                model.getCredential(args[1]);
            }
        } else if (args[0].equals(SAVE)) {
            try {
                Blob newBlob = model.passfile.encryptBlob(model.getFilepass(), model.getLastMod());
                //TODO: use correct digest
                RequestPut req = new RequestPut(model.user, newBlob, "".getBytes());
                @SuppressWarnings("unused")
				ResponsePut r = (ResponsePut)model.reqHandler.send(req);
                model.continuePrompt("Successfully saved to server");
            } catch (PasswordFile.BadBlobException e) {
                model.badMasterPass();
            }
        } else if (args[0].equals(SET)) {
            model.setVal(args[1], args[2]);
        } else if (args[0].equals(DEL)) {
            model.delVal(args[1]);
        } else if (args[0].equals(LOGOUT)) {
            model.logout();
        } else if (args[0].equals(PRINT)) {
            model.printAll();
        } else {
            model.continuePrompt("Command not recognized. Full list: " + Arrays.toString(ClientController.KEYWORDS));
        }
	}
    public void handleUserName(String userName) {
        model.setUsername(userName);
    }
	public void handlePassword(String pass) {
		
	}
	
	
}
