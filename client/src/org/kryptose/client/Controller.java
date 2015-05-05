package org.kryptose.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kryptose.client.Model.CredentialAddOrEditForm;
import org.kryptose.Utils;
import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;
import org.kryptose.client.PasswordFile.BadBlobException;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.exceptions.CryptoPrimitiveNotSupportedException;
import org.kryptose.exceptions.InternalServerErrorException;
import org.kryptose.exceptions.InvalidCredentialsException;
import org.kryptose.exceptions.MalformedRequestException;
import org.kryptose.exceptions.RecoverableException;
import org.kryptose.requests.*;

/**
 * Created by jeff on 4/27/15.
 */
public class Controller {
	
    private static final String PROPERTIES_FILE = "clientProperties.xml";

    private abstract class LongTaskRunner implements Runnable {
    	abstract Exception doRun();
		@Override
		public final void run() {
			model.setWaitingOnServer(true);
			Exception exception = null;
			try {
				exception = doRun();
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Uncaught error in Controller thread", t);
			} finally {
				model.setException(exception);
				model.setWaitingOnServer(false);
			}
		}
    }

    private abstract class QuickTaskRunner implements Runnable {
    	abstract void doRun();
		@Override
		public final void run() {
			try {
				doRun();
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Uncaught error in Controller thread", t);
			}
		}
    }
    
    private final ExecutorService pool;
    
	private final Model model;
	private final View view;
	private RequestHandler reqHandler;
	private Properties properties;

	private final Logger logger = Logger.getLogger(Controller.class.getCanonicalName());
	
	public Controller() {
        this.model = new Model();
		this.view = new ViewGUI(model, this);
		
		this.model.registerView(view);
		this.model.setViewState(ViewState.LOGIN);
		
		this.pool = Executors.newSingleThreadExecutor();
	}
	
	private void loadProperties() {

        this.properties = new Properties();

        //SETTING DEFAULT CONFIGURATIONS (can be overridden by the Client settings file)
        properties.setProperty("SERVER_PORT_NUMBER", "5002");
        properties.setProperty("CLIENT_KEY_STORE_FILE", "ClientTrustStore.jks");
        properties.setProperty("CLIENT_KEY_STORE_PASSWORD", "aaaaaa");
        properties.setProperty("SERVER_HOSTNAME", "127.0.0.1");
        properties.setProperty("APPLICATION_SALT", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.setProperty("MAX_USERNAME_LENGTH", "40");
        
        //LOADIG CUSTOM CONFIGURATION FROM FILE.
        try (FileInputStream in = new FileInputStream(PROPERTIES_FILE)) {
            
            Properties XMLProperties = new Properties();
            XMLProperties.loadFromXML(in);
            this.properties.putAll(XMLProperties);
            in.close();
        } catch (IOException e) {
        	//TODO: Unable to read the properties file. Maybe log the error?

            try (FileOutputStream out = new FileOutputStream(PROPERTIES_FILE)) {
                properties.storeToXML(out, "Client Configuration File");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                //Give up.
                e1.printStackTrace();
            }
            
        }
    	
	}
	
	public void start() {
		this.loadProperties();
		
        KeyDerivator.setParams(properties.getProperty("APPLICATION_SALT"), Integer.parseInt(properties.getProperty("MAX_USERNAME_LENGTH")));
        this.reqHandler = new RequestHandler(properties.getProperty("SERVER_HOSTNAME"),Integer.parseInt(properties.getProperty("SERVER_PORT_NUMBER")), properties.getProperty("CLIENT_KEY_STORE_FILE"), properties.getProperty("CLIENT_KEY_STORE_PASSWORD") );
		
	}


    public void save(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			Exception doRun() {
                return doSave();
			}
		});
    }
    private Exception doSave() {
    	MasterCredentials mCred = model.getMasterCredentials();
    	PasswordFile pFile = model.getPasswordFile();

        Blob newBlob;
		try {
			newBlob = pFile.encryptBlob(mCred, model.getLastModDate());
		} catch (CryptoPrimitiveNotSupportedException | BadBlobException
				| CryptoErrorException e) {
			String msg = "Error encrypting password file... could be outdated Kryptose\u2122 client or JRE.";
			logger.log(Level.SEVERE, msg, e);
			return new Exception(msg, e);
		}
		
        RequestPut req = new RequestPut(mCred.getUser(), newBlob, pFile.getOldDigest());

        try {
			ResponsePut r = this.sendRequest(req, ResponsePut.class);
			byte[] digest = r.getDigest();
	        model.getPasswordFile().setOldDigest(digest);
			return null;
		} catch (RecoverableException | SevereClientException e) {
			return e;
		}
    }

    public static enum setType{ADD, EDIT};
    
    public void set(setType s) {
		this.pool.submit(new LongTaskRunner() {
			@Override
			Exception doRun() {
				return doSet(s);
			}
		});
    }
    
    private Exception doSet(setType s) {
    	String domain = model.getFormText(TextForm.CRED_DOMAIN);
    	String username = model.getFormText(TextForm.CRED_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.CRED_PASSWORD);
    	char[] confirm = model.getFormPasswordClone(PasswordForm.CRED_CONFIRM_PASSWORD);
    	
    	String validationError = null;
    	
    	// Validate inputs
    	if (domain == null || domain.length() == 0) {
    		validationError = "Please enter a domain.";
    	} else if (username == null || username.length() == 0) {
    		validationError = "Please enter a username.";
    	} else if (password == null || password.length == 0) {
    		validationError = "Please enter a password.";
    	} else if (!Arrays.equals(password,confirm)) {
    		validationError = "Entered passwords do not match.";
    	}
		Utils.destroyPassword(confirm);

		if(validationError == null && s == setType.ADD && model.getPasswordFile().getVal(domain, username)!= null)
			validationError = "A credential for the same domain and username already exists. Please edit or delete it.";		

		if(validationError == null && s == setType.EDIT && model.getPasswordFile().getVal(domain, username)== null)
			validationError = "You tried to edit a credential, but it seems the credential you want to edit does not exist. This is a bug. Please contact Kryptose to have it fixed.";				
		
    	if (validationError != null) {
    		Utils.destroyPassword(password);
    		return new RecoverableException(validationError);
    	}
    	

    	
    	model.getPasswordFile().setVal(domain, username, password);
    	
    	Exception ex = doSave();
    	
    	if(ex == null){
    		this.requestViewState(ViewState.MANAGING);
    	}
    	
    	return ex;
    }

    public void delete() {
		this.pool.submit(new LongTaskRunner() {
			@Override
			Exception doRun() {
				return doDelete();
			}
		});
    }
    
    private Exception doDelete() {
    	String domain = model.getFormText(TextForm.CRED_DOMAIN);
    	String username = model.getFormText(TextForm.CRED_USERNAME);
    	
    	boolean success = model.getPasswordFile().delVal(domain, username);
    	
    	if (!success) {
    		String msg = "Attempted to delete a credential set that does not exist.";
    		RecoverableException ex = new RecoverableException(msg);
    		logger.log(Level.INFO, msg, ex);
    		return ex;
    	}
    	
    	Exception exception = this.doSave();
    	this.updateFormPassword(PasswordForm.CRED_PASSWORD, null);
    	this.updateFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);
    	
    	if (exception != null) {
    		Exception ex2 = this.doFetch();
    		
    		if (ex2 != null) {
    			String msg = exception.getMessage()
    					+ "\n Furthermore, was unable to revert the change to the local password file. "
    					+ "Please exit the application, since it is now in an inconsistent state.";
    			SevereClientException scex = new SevereClientException(msg, ex2);
    			logger.log(Level.SEVERE, msg, scex);
    			return scex;
    		}
    	}
    	
    	return exception;
    }
    
    public void login() {
    	pool.execute(new LongTaskRunner() {
    		Exception doRun() {
    			return doLogin();
    		}
    	});
    }

    private Exception doLogin(){
    	String username = model.getFormText(TextForm.LOGIN_MASTER_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.LOGIN_MASTER_PASSWORD);

    	// Validate inputs
    	if (!MasterCredentials.isValidUsername(username)) {
    		return new RecoverableException(User.VALID_USERNAME_DOC);
    	}
    	if (!MasterCredentials.isValidPassword(password)) {
    		return new RecoverableException("Please enter a password.");
    	}
    	
    	// Update master credentials
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);
    	
    	// Fetch from server
    	Exception ex = this.doFetch();
    	if (ex == null) {
    		this.doStateTransition(ViewState.WAITING);
    	} else {
    		model.getMasterCredentials().destroy();
    		model.setMasterCredentials(null);
    		model.setFormPassword(PasswordForm.LOGIN_MASTER_PASSWORD, null);
    		return ex;
    	}
		model.setFormPassword(PasswordForm.LOGIN_MASTER_PASSWORD, null);
		return null;
    }
    
    public void logout() {
    	this.pool.submit(new QuickTaskRunner() {
			@Override
			void doRun() {
				doLogout();
			}
    	});
    }

    private void doLogout() {
    	MasterCredentials mCred = model.getMasterCredentials();
    	if (mCred != null) mCred.destroy();
    	model.setMasterCredentials(null);
    	
    	PasswordFile pFile = model.getPasswordFile();
    	if (pFile != null) pFile.destroy();
    	model.setPasswordFile(null);
    	
    	model.setLastModDate(null);
    	model.setWaitingOnServer(false);
    	model.setException(null);
    	model.setUserLogs(null);
    	
    	model.setViewState(ViewState.LOGIN);
    }

    private <T extends Response> T sendRequest(Request r, Class<T> t) throws RecoverableException, SevereClientException {
    	try {
    		Response response = (Response) this.reqHandler.send(r);
    		response.checkException();
			return t.cast(response);
		} catch (UnknownHostException e) {
			String msg = "Could not connect to Kryptose\u2122 server at: " + this.properties.getProperty("SERVER_HOSTNAME");
			RecoverableException ex = new RecoverableException(msg);
			this.logger.log(Level.WARNING, msg, e);
			throw ex;
		} catch (InvalidCredentialsException e) {
			this.logger.log(Level.INFO, "Invalid credentials.", e);
			throw e;
		} catch (IOException e) {
			if (e.getCause() instanceof NoSuchAlgorithmException) {
				String msg = "Error setting up the SSL Socket. Might be due to missing certificate files or wrong password. If persists, check the settings file or reinstall the app.";
				RecoverableException ex = new RecoverableException(msg);
				this.logger.log(Level.WARNING, msg, e);
				throw ex;
			} else {//generic IOException
				String msg = "Error communicating with Kryptose\u2122 server.";
				RecoverableException ex = new RecoverableException(msg);
				this.logger.log(Level.WARNING, msg, e);	
				throw ex;
			}
				
		} catch (MalformedRequestException | ClassCastException e) {
			String msg = "Error communicating with Kryptose\u2122 server.";
			RecoverableException ex = new RecoverableException(msg);
			this.logger.log(Level.WARNING, msg, e);
			throw ex;
		}catch (InternalServerErrorException e) {
			this.logger.log(Level.WARNING, "Internal server error.", e);
            throw new SevereClientException("An unknown error occured on the server.", e);
		}
    	// TODO we should also check that the client version is compatible.
    }

    public void fetch() {
    	pool.execute(new LongTaskRunner() {
    		Exception doRun() {
    			return doFetch();
    		}
    	});
    }
    
    private Exception doFetch(){
    	ResponseGet r = null;
    	MasterCredentials mCred = model.getMasterCredentials();
    	RequestGet req = new RequestGet(mCred.getUser());
    	try {
			r = this.sendRequest(req, ResponseGet.class);
		} catch (RecoverableException | SevereClientException e1) {
			return e1;
		}
    	if (r.getBlob() == null){
    		PasswordFile pFile = new PasswordFile(mCred);
    		model.setPasswordFile(pFile);
    		return null;
    	}
    	try {
    		PasswordFile pFile = new PasswordFile(mCred, r.getBlob());
    		pFile.setOldDigest(r.getBlob().getDigest());
    		model.setPasswordFile(pFile);
    		return null;
    	} catch (PasswordFile.BadBlobException | CryptoErrorException e) {
    		String msg = "Error decrypting the passwords file. This could be an outdate Kryptose\u2122 client, "
    				+ "or temporary data corruption. Please try again or update the client.";
    		Exception ex = new SevereClientException(msg, e);
    		logger.log(Level.SEVERE, "Error decrypting blob.", e);
    		return ex;
    	}
    }

    public void fetchLogs(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			Exception doRun() {
                // TODO Auto-generated method stub
				String msg = "fetchLogs not implemented in Controller";
				SevereClientException ex = new SevereClientException(msg);
				return ex;
			}
		});
    }

    public void createAccount(){
    	pool.execute(new LongTaskRunner() {
    		Exception doRun() {
    			return doCreateAccount();
    		}
    	});
    }
    
    private Exception doCreateAccount() {
    	String username = model.getFormText(TextForm.CREATE_MASTER_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.CREATE_MASTER_PASSWORD);
    	char[] confirm = model.getFormPasswordClone(PasswordForm.CREATE_CONFIRM_PASSWORD);
    	
    	String validationError = null;
    	
    	// Validate inputs
    	if (!MasterCredentials.isValidUsername(username)) {
    		validationError = User.VALID_USERNAME_DOC;
    	} else if (!MasterCredentials.isValidPassword(password)) {
    		validationError = "Please enter a password.";
    	} else if (!Arrays.equals(password, confirm)) {
    		validationError = "Entered passwords do not match.";
    	}
		Utils.destroyPassword(confirm);
		
    	if (validationError != null) {
    		Utils.destroyPassword(password);
    		return new RecoverableException(validationError);
    	}
    	
    	// mCred now has responsibility for destroying password.
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);

        try {
        	RequestCreateAccount req = new RequestCreateAccount(mCred.getUser());
        	ResponseCreateAccount r = this.sendRequest(req, ResponseCreateAccount.class);
            r.verifySuccessful();
            model.setPasswordFile(new PasswordFile(mCred));
			this.doStateTransition(ViewState.WAITING);
            return null;
        } catch (RecoverableException | SevereClientException e) {
        	this.logger.log(Level.INFO, "Failed to create new account.", e);
            return e;
        }
    	
    }

    public void deleteAccount(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			Exception doRun() {
				return doDeleteAccount();
			}
		});
    }
    
    private Exception doDeleteAccount() {
		MasterCredentials mCred = model.getMasterCredentials();
		char[] passwordConfirm = model.getFormPasswordClone(PasswordForm.DELETE_ACCOUNT_CONFIRM_PASSWORD);

        if (!Arrays.equals(mCred.getPassword(), passwordConfirm)) {
            return new RecoverableException("Password incorrect.");
        }

        RequestDeleteAccount req = new RequestDeleteAccount(model.getMasterCredentials().getUser());
		ResponseDeleteAccount r;
		try {
			r = this.sendRequest(req, ResponseDeleteAccount.class);
		} catch (RecoverableException | SevereClientException e) {
			return e;
		}
        if (!r.verifySuccessful()) {
            return new RecoverableException("Error: account delete failed unexpectedly");
        }
    	this.doLogout();
        logger.severe("logging out");
    	return null;
    }

    public void changeMasterPassword(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			Exception doRun() {
                return doChangeMasterPassword();
			}
		});
    }

    private Exception doChangeMasterPassword() {
		
		MasterCredentials mCred = model.getMasterCredentials();
		char[] oldPasswordConfirm = model.getFormPasswordClone(PasswordForm.CHANGE_OLD_MASTER_PASSWORD);
		char[] newPassword = model.getFormPasswordClone(PasswordForm.CHANGE_NEW_MASTER_PASSWORD);
		char[] newPasswordConfirm = model.getFormPasswordClone(PasswordForm.CHANGE_CONFIRM_NEW_MASTER_PASSWORD);

        if(!Arrays.equals(oldPasswordConfirm, mCred.getPassword())){
            return new RecoverableException("Wrong old password");
        }
        if(!Arrays.equals(newPassword, newPasswordConfirm)){
            return new RecoverableException("New passwords do not match");
        }

        PasswordFile pFile = model.getPasswordFile();
        MasterCredentials newMCred = new MasterCredentials(mCred.getUsername(), newPassword);

        Blob newBlob;
        try {
            newBlob = pFile.encryptBlob(newMCred, model.getLastModDate());
        } catch (BadBlobException | CryptoErrorException e) {
            this.logger.log(Level.INFO, "Failed to change master password.", e);
            return e;
        }

        RequestChangePassword req = new RequestChangePassword(mCred.getUser(), newMCred.getAuthKey(), newBlob, (pFile==null) ? null : pFile.getOldDigest());
        try {
        	ResponseChangePassword r = this.sendRequest(req, ResponseChangePassword.class);
			r.getDigest();
		} catch (RecoverableException | SevereClientException e) {
	        this.doStateTransition(ViewState.WAITING);
			return e;
		}

        model.setMasterCredentials(newMCred);
        model.setPasswordFile(new PasswordFile(newMCred));
        this.doStateTransition(ViewState.WAITING);

		return null;
    }
    
    public void updateFormText(TextForm form, String value) {
    	if (!this.pool.isShutdown())
    		this.pool.submit(new QuickTaskRunner() {
			@Override
			void doRun() {
		    	// TODO validate value? (probably too much work)
				model.setFormText(form, value);
				if (form == TextForm.CRED_DOMAIN || form == TextForm.CRED_USERNAME) {
					refreshCredentialTable();
				}
			}
		});
    }

	public void updateFormPassword(PasswordForm form, char[] password) {
		if (!this.pool.isShutdown())
			this.pool.submit(new QuickTaskRunner() {
			@Override
			void doRun() {
				model.setFormPassword(form, password);
			}
		});
	}

	public void exit() {
		this.pool.submit(new QuickTaskRunner() {
			@Override
			void doRun() {
				doLogout();
				view.shutdown();
				pool.shutdown();
			}
		});
	}

	public void requestViewState(final ViewState viewState) {
		this.pool.submit(new QuickTaskRunner() {
			@Override
			void doRun() {
				doRequestViewState(viewState);
			}
		});
	}
	private void doRequestViewState(ViewState viewState) {
		ViewState oldState = this.model.getViewState();
		
		ViewState[][] allowedTransitions = new ViewState[][] {
			new ViewState[] { ViewState.LOGIN, ViewState.CREATE_ACCOUNT},
			new ViewState[] { ViewState.CREATE_ACCOUNT, ViewState.LOGIN},
			new ViewState[] { ViewState.WAITING, ViewState.MANAGING},
			new ViewState[] { ViewState.MANAGING, ViewState.WAITING},
			new ViewState[] { ViewState.WAITING, ViewState.CHANGE_MASTER_PASSWORD},
			new ViewState[] { ViewState.CHANGE_MASTER_PASSWORD, ViewState.WAITING},
			new ViewState[] { ViewState.WAITING, ViewState.DELETE_ACCOUNT},
			new ViewState[] { ViewState.DELETE_ACCOUNT, ViewState.WAITING},
			
			//new ViewState[] { ViewState.MANAGING, ViewState.EDITING},    //HANDLED SEPARATELY
			new ViewState[] { ViewState.EDITING, ViewState.MANAGING},
			new ViewState[] { ViewState.MANAGING, ViewState.ADDING},
			new ViewState[] { ViewState.ADDING, ViewState.MANAGING},
						
		};
		
		for (ViewState[] transition : allowedTransitions) {
			if (oldState == transition[0] && viewState == transition[1]) {
				this.doStateTransition(viewState);
				return;
			}
		}
		
		if(oldState == viewState.MANAGING && viewState == viewState.EDITING){
			if(!model.getPasswordFile().existsCredential(model.selectedDomain, model.selectedUser)){
				//TODO Jonathan
				//model.setLastException(new RecoverableException("Please select a Credential before editing"));
				return;
			}else{
				this.doStateTransition(viewState);
				return;
			}

		}
			
		logger.log(Level.SEVERE, "Bad view state transition from " + oldState + " to " + viewState);
	}

	private void doStateTransition(ViewState viewState) {
		ViewState oldState = this.model.getViewState();
		
        if ((oldState == ViewState.LOGIN || oldState == ViewState.CREATE_ACCOUNT)
				&& viewState == ViewState.WAITING) {
			this.model.setFormText(TextForm.LOGIN_MASTER_USERNAME, null);
			this.model.setFormPassword(PasswordForm.LOGIN_MASTER_PASSWORD, null);
		}
		
		if (oldState == ViewState.CREATE_ACCOUNT) {
			this.model.setFormText(TextForm.CREATE_MASTER_USERNAME, null);
			this.model.setFormPassword(PasswordForm.CREATE_MASTER_PASSWORD, null);
		}
		
		if (viewState == ViewState.MANAGING) {
			this.refreshCredentialTable();
		}
		
		if (oldState == ViewState.CHANGE_MASTER_PASSWORD) {
			this.model.setFormPassword(PasswordForm.CHANGE_OLD_MASTER_PASSWORD, null);
			this.model.setFormPassword(PasswordForm.CHANGE_NEW_MASTER_PASSWORD, null);
			this.model.setFormPassword(PasswordForm.CHANGE_CONFIRM_NEW_MASTER_PASSWORD, null);
		}
		
		if (oldState == ViewState.DELETE_ACCOUNT) {
			this.model.setFormPassword(PasswordForm.DELETE_ACCOUNT_CONFIRM_PASSWORD, null);
		}
		
		if (viewState == viewState.ADDING){
			this.model.setFormText(TextForm.CRED_DOMAIN, null);
			this.model.setFormText(TextForm.CRED_USERNAME, null);
			this.model.setFormPassword(PasswordForm.CRED_PASSWORD, null);
			this.model.setFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);
		}

		if (oldState == viewState.MANAGING && viewState == viewState.EDITING){

			this.model.setFormText(TextForm.CRED_DOMAIN, model.selectedDomain);
			this.model.setFormText(TextForm.CRED_USERNAME, model.selectedUser);			
			
			if(model.getPasswordFile().getVal(model.selectedDomain, model.selectedUser) == null){
				this.model.setFormPassword(PasswordForm.CRED_PASSWORD, null);
			}else{
				this.model.setFormPassword(PasswordForm.CRED_PASSWORD, model.getPasswordFile().getVal(model.selectedDomain, model.selectedUser));
			}
			
			this.model.setFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);
		}
		
		
		if(!(oldState == viewState.MANAGING && viewState == viewState.EDITING)){
			model.setFormPassword(PasswordForm.CRED_PASSWORD, null);
        	model.setFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);        
        	for(PasswordForm e : PasswordForm.values())
        		model.setFormPassword(e, null);
			}
        model.setFormPassword(PasswordForm.CREATE_CONFIRM_PASSWORD, null);
        
//        model.selectedDomain = null;
//        model.selectedUser = null;
        
		this.model.setViewState(viewState);
	}
	
	private void refreshCredentialTable() {
		PasswordFile pFile = model.getPasswordFile();
		
		String domain = model.getFormText(TextForm.CRED_DOMAIN);
		String username = model.getFormText(TextForm.CRED_USERNAME);
		
		String[] domainOptions = pFile.getDomains();
		String[] usernameOptions = pFile.getUsernames(domain);
		// WARNING: current code assumes that the char[] obtained from pFile.getVal
		// is a new copy, and hence destroys the copy obtained.
		char[] passString = pFile.getVal(domain, username);
		
		String[] domainOptionsWithNull = new String[domainOptions.length + 1];
		System.arraycopy(domainOptions, 0, domainOptionsWithNull, 1, domainOptions.length);
		String[] usernameOptionsWithNull = new String[usernameOptions.length + 1]; 
		System.arraycopy(usernameOptions, 0, usernameOptionsWithNull, 1, usernameOptions.length);

		model.setFormPassword(PasswordForm.CRED_PASSWORD, null);
		model.setFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);
		model.setFormOptions(CredentialAddOrEditForm.CRED_DOMAIN, domainOptionsWithNull);
		model.setFormOptions(CredentialAddOrEditForm.CRED_USERNAME,
				usernameOptions.length <= 1 ? usernameOptions : usernameOptionsWithNull);
		
	}


}
