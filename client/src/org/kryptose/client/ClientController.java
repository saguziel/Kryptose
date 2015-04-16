package org.kryptose.client;

import org.kryptose.client.PasswordFile.BadBlobException;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.requests.*;
import org.kryptose.exceptions.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class ClientController {

    static final String LOGIN = "login";
    static final String CREATE = "create";

    static final String GET = "get";
    static final String GET_SYNTAX = "Syntax: get";
    static final String SAVE = "save";
    static final String SAVE_SYNTAX = "Syntax: save";
    static final String SET = "set";
    static final String SET_SYNTAX = "Syntax: set"; // TODO: JS I changed this, getting rid of $domain $username $password. Jeff please approve. Jeff: I approve
    static final String DEL = "del";
    static final String DEL_SYNTAX = "Syntax: del $domain $username";
    static final String QUERY = "query";
    static final String QUERY_SYNTAX = "Syntax: query $domain $username";
    static final String PRINT = "print";
    static final String LIST = "list";
    static final String PRINT_SYNTAX = "Syntax: print";
    static final String LOGS = "logs";
    static final String LOGS_SYNTAX = "Syntax: logs";
    static final String LOGOUT = "logout";
    static final String LOGOUT_SYNTAX = "Syntax: logout";
    static final String HELP = "help";
    static final String[] KEYWORDS = new String[] {GET, SAVE, SET, DEL, QUERY, PRINT, /*LOGS,*/ LOGOUT, HELP};
    static final String HELP_SYNTAX = "Syntax: help";
    Client model;
	
	public ClientController(Client c) {
		this.model = c;
	}

	public void fetch() {

        ResponseGet r;

		try {
			r = (ResponseGet) model.reqHandler.send(new RequestGet(model.user));
            // TODO: catch ClassCastException and handle it.
            if(r.getBlob() == null){
                model.newPassFile();
            } else {
                try {
                    model.setPassfile(new PasswordFile(model.user.getUsername(), r.getBlob(), model.getMasterpass()));
                    model.passfile.setOldDigest(r.getBlob().getDigest());
                } catch (PasswordFile.BadBlobException | CryptoErrorException e) {
                    model.start("Invalid login credentials");
                }
            }
		} catch (UnknownHostException e1) {
			model.continuePrompt("The host could not be found");
		} catch (IOException e1) {
			model.start("There was an SSL error, contact your local library for help");
		} catch (InvalidCredentialsException e1) {
            model.start("Invalid login credentials, please try again");
        } catch (MalformedRequestException | InternalServerErrorException e1) {
            model.continuePrompt("An error occurred, please try again");
        }

    }

    public void fetchLogs() {
        ResponseLog r;

        try {
            r = (ResponseLog) model.reqHandler.send(new RequestLog(model.user));
            // TODO: catch ClassCastException and handle it.
            model.setLogs(r.getLogs());
            model.displayLogs();
        } catch (UnknownHostException e1) {
            model.continuePrompt("The host could not be found");
        } catch (IOException e1) {
            model.start("There was an SSL error, contact your local library for help");
        } catch (MalformedRequestException | InternalServerErrorException e1) {
            model.continuePrompt("An error occurred, please try again");
        } catch (InvalidCredentialsException e1) {
            model.restartLogin();
        }
    }


    public void handleCreatepass(String pass) {
        ResponseCreateAccount r;

        model.setMasterpass(pass);

        try {
            r = (ResponseCreateAccount) model.reqHandler.send(new RequestCreateAccount(model.user));
            // TODO: catch ClassCastException and handle it.
            r.verifySuccessful();
            model.passfile = new PasswordFile(model.user.getUsername());
            model.continuePrompt("Account successfully created!");

        } catch (UnknownHostException e1) {
            model.continuePrompt("The host could not be found");
        } catch (IOException e1) {
            model.start("There was an SSL error, contact your local library for help");
        } catch (MalformedRequestException | InternalServerErrorException e1) {
            model.continuePrompt("An error occurred, please try again");
        } catch (InvalidCredentialsException e1) {
            model.restartLogin();
        } catch (UsernameInUseException e1) {
            model.start("Username in use, please try a different name");
        }
    }

    public void save() {

        try {
            Blob newBlob = model.passfile.encryptBlob(model.user.getUsername(), model.getMasterpass(), model.getLastMod());
            //TODO: use correct digest
            RequestPut req = new RequestPut(model.user, newBlob, model.passfile.getOldDigest());


            Response r = model.reqHandler.send(req);
            if (r instanceof ResponsePut) {
                model.passfile.setOldDigest(req.getBlob().getDigest());
                model.continuePrompt("Successfully saved to server");
            }

            /**
            else if (r instanceof ResponseInternalServerError) {
                model.continuePrompt("ERROR: Response not saved due to internal server error");
            } else if (r instanceof ResponseInvalidCredentials) {
                model.continuePrompt("Credentials invalid: please logout and try again");
            } else if (r instanceof ResponseGet) {
                model.continuePrompt("ERROR: Response may have not been saved. Server returned bad response");
            } else if (r instanceof ResponseStaleWrite) {
                model.continuePrompt("ERROR: Response not saved. Please run GET again before using SET");
            } else {
                model.continuePrompt("ERROR: Response may not have been saved. Server returned bad response.");
            }*/

        } catch (PasswordFile.BadBlobException | CryptoErrorException e) {
            model.badMasterPass();
        } catch (UnknownHostException e1) {
            model.continuePrompt("The host could not be found");
        } catch (IOException e1) {
            model.start("There was an SSL error, contact your local library for help");
        } catch (MalformedRequestException | InternalServerErrorException e1) {
            model.continuePrompt("An error occurred, please try again");
        } catch (InvalidCredentialsException e1) {
            model.restartLogin();
        }

    }

	public void handleRequest(String request) throws CryptoErrorException, BadBlobException {

		String[] args = request.trim().split("\\s+");
        if (args.length == 0) {
            model.continuePrompt("Command not recognized. Full list: " + Arrays.toString(ClientController.KEYWORDS));
            return;
        }
        String command = args[0].toLowerCase();

        if (command.equals(GET)) {
            if (args.length == 1) {
                fetch();
            } else {
                model.continuePrompt(GET_SYNTAX);
            }
        } else if (command.equals(QUERY)) {
            if (args.length == 2) {
//                model.getCredential(args[1], args[2]);
                model.getCredentialNum(args[1]);
            } else {
                model.continuePrompt(QUERY_SYNTAX);
            }
        } else if (command.equals(SAVE)) {
            if (args.length == 1) {
                save();
            } else {
                model.continuePrompt(SAVE_SYNTAX);
            }
        } else if (command.equals(SET)) {
            if (args.length == 1) {
//                model.setVal(args[1], args[2]);
//                save();
                model.set();
            } else {
                model.continuePrompt(SET_SYNTAX);
            }
        } else if (command.equals(DEL)) {
            if (args.length == 2) {
//                model.delVal(args[1], args[2]);
                model.delValNum(args[1]);
                save();
            } else {
                model.continuePrompt(DEL_SYNTAX);
            }
        }
//        else if (command.equals(LOGS)) {
//            if (args.length == 1) {
//                fetchLogs();
//            } else {
//                model.continuePrompt(LOGS_SYNTAX);
//            }
//        }
        else if (command.equals(LOGOUT)) {
            if (args.length == 1) {
                model.logout();
            } else {
                model.continuePrompt(LOGOUT_SYNTAX);
            }
        } else if (command.equals(PRINT) || command.equals(LIST)) {
            if (args.length == 1) {
                model.printAll();
            } else {
                model.continuePrompt(PRINT_SYNTAX);
            }
        } else if (command.equals(HELP)) {
            model.continuePrompt(
                    "Valid Commands:\n" +
                    "GET: Sets local password file to remote password file\n" +
                    GET_SYNTAX + "\n\n" +
                    "QUERY: Shows the password for $username based on the current local password file\n" +
                    QUERY_SYNTAX + "\n\n" +
                    "SAVE: Saves local password file to remote server\n" +
                    SAVE_SYNTAX + "\n\n" +
                    "SET: Sets password to $password for $username and attempts to push to remote\n" +
                    SET_SYNTAX + "\n\n" +
                    "DEL: Deletes the username password pair for $username\n" +
                    DEL_SYNTAX + "\n\n" +
                    "LOGOUT: Logs out of current account\n" +
                    LOGOUT_SYNTAX + "\n\n" +
//                    "LOGS: Displays log of all current user interactions with server\n" +
//                    LOGS_SYNTAX + "\n\n" +
                    "PRINT: Prints all usernames and password pairs\n" +
                    PRINT_SYNTAX + "\n\n" +
                    "HELP: Prints commands, their uses, and their syntax\n" +
                    HELP_SYNTAX
            );
        } else {
            model.continuePrompt("Command not recognized. Full list: " + Arrays.toString(ClientController.KEYWORDS)
                                 + "\nEnter help for list of commands, uses, and syntax");
        }
	}

    public void handleStart(String cmd) {
        cmd = cmd.trim().toLowerCase();
        if(cmd.equals(CREATE)) {
            model.startCreate();
        } else if(cmd.equals(LOGIN)) {
            model.startLogin();
        } else {
            model.start(
                    "Valid Commands:\n" +
                    "LOGIN: log in with username and password\n" +
                    "CREATE: create a new account"
            );
        }
    }

    public void handleUserName(String userName) {
        if (model.setUsername(userName)) {
            model.promptMasterpass();
        } else {
            model.start();
        }
    }

    public void handleSet(String dom, String user, String pass) {
        model.setVal(dom, user, pass);
        save();
    }

	public void handlePassword(String pass) {
        model.setMasterpass(pass);
        fetch();
		
	}

    public void handleCreateuser(String name) {
        if (model.setUsername(name)) {
            model.startSetPass();
        } else {
            model.startCreate();
        }
    }





	
}
