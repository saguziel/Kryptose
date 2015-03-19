package org.kryptose.client;

import org.kryptose.client.PasswordFile.BadBlobException;
import org.kryptose.requests.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

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

	public void fetch() {

        ResponseGet r;

		try {
			r = (ResponseGet) model.reqHandler.send(new RequestGet(model.user));
            if(r.getBlob() == null){
                model.newPassFile();
            } else {
                try {
                    model.setPassfile(new PasswordFile(model.user.getUsername(), r.getBlob(), model.getFilepass()));
                    model.passfile.setOldDigest(r.getBlob().getDigest());
                } catch (PasswordFile.BadBlobException | CryptoErrorException e) {
                    model.badMasterPass();
                }
            }
		} catch (UnknownHostException e1) {
			model.continuePrompt("The host could not be found");
		} catch (IOException e1) {
			model.continuePrompt("There was an SSL error, contact your local library for help");
		}

    }

    public void save() {

        try {
            Blob newBlob = model.passfile.encryptBlob(model.getFilepass(), model.getLastMod());
            //TODO: use correct digest
            RequestPut req = new RequestPut(model.user, newBlob, model.passfile.getOldDigest());


            @SuppressWarnings("unused")
            Response r = model.reqHandler.send(req);
            if (r instanceof ResponsePut) {
                model.passfile.setOldDigest(req.getBlob().getDigest());
                model.continuePrompt("Successfully saved to server");
            } else if (r instanceof ResponseInternalServerError) {
                model.continuePrompt("ERROR: Response not saved due to internal server error");
            } else if (r instanceof ResponseInvalidCredentials) {
                model.continuePrompt("Credentials invalid: please logout and try again");
            } else if (r instanceof ResponseGet) {
                model.continuePrompt("ERROR: Response may have not been saved. Server returned bad response");
            } else if (r instanceof ResponseStaleWrite) {
                model.continuePrompt("ERROR: Response not saved. Please run GET again before using SET");
            } else {
                model.continuePrompt("ERROR: Response may not have been saved. Server returned bad response.");
            }

        } catch (PasswordFile.BadBlobException | CryptoErrorException e) {
            model.badMasterPass();
        } catch (UnknownHostException e1) {
            model.continuePrompt("The host could not be found");
        } catch (IOException e1) {
            model.continuePrompt("There was an SSL error, contact your local library for help");
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
            save();
        } else if (args[0].equals(SET)) {
            model.setVal(args[1], args[2]);
            save();
        } else if (args[0].equals(DEL)) {
            model.delVal(args[1]);
            save();
        } else if (args[0].equals(LOGOUT)) {
            model.logout();
        } else if (args[0].equals(PRINT)) {
            model.printAll();
        } else {
            model.continuePrompt("Command not recognized. Full list: " + Arrays.toString(ClientController.KEYWORDS));
        }
	}

    public void handleUserName(String userName) {
        if (model.setUsername(userName)) {
            fetch();
        } else {
            model.start();
        }
    }

	public void handlePassword(String pass) {
		
	}



	
}
