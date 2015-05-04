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
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kryptose.client.Model.OptionsForm;
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
import org.kryptose.exceptions.UsernameInUseException;
import org.kryptose.requests.*;

/**
 * Created by jeff on 4/27/15.
 */
public class Controller {
	
    private static final String PROPERTIES_FILE = "clientProperties.xml";

    private abstract class LongTaskRunner implements Runnable {
    	abstract boolean doRun();
		@Override
		public final void run() {
			model.setWaitingOnServer(true);
			boolean success = false; // TODO should replace this with exception?
			try {
				success = doRun();
			} catch (Throwable t) {
				logger.log(Level.SEVERE, "Uncaught error in Controller thread", t);
			} finally {
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
        FileInputStream in;
        try {
            in = new FileInputStream(PROPERTIES_FILE);
            Properties XMLProperties = new Properties();
            XMLProperties.loadFromXML(in);
            this.properties.putAll(XMLProperties);
            in.close();
        } catch (IOException e) {
        	//TODO: Unable to read the properties file. Maybe log the error?

            try {
                FileOutputStream out = new FileOutputStream(PROPERTIES_FILE);
                properties.storeToXML(out, "Client Configuration File");
                out.close();
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

    public void query(String domain, String username){
		this.pool.submit(new LongTaskRunner() {
			@Override
			boolean doRun() {
                // TODO Auto-generated method stub
				// This is likely not useful anymore.
				logger.severe("query not implemented in Controller");
				return false;
			}
		});
    }


    public void save(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			boolean doRun() {
                return doSave();
			}
		});
    }
    private boolean doSave() {
    	MasterCredentials mCred = model.getMasterCredentials();
    	PasswordFile pFile = model.getPasswordFile();

        Blob newBlob;
		try {
			newBlob = pFile.encryptBlob(mCred, model.getLastModDate());
		} catch (CryptoPrimitiveNotSupportedException | BadBlobException
				| CryptoErrorException e) {
			// TODO think about this error case
			String msg = "Error encrypting password file... could be outdated Kryptose\u2122 client or JRE.";
			logger.log(Level.SEVERE, msg, e);
			model.setLastException(new Exception(msg, e));;
			return false;
		}
		
        RequestPut req = new RequestPut(mCred.getUser(), newBlob, pFile.getOldDigest());

        ResponsePut r = this.sendRequest(req, ResponsePut.class);
        if (r == null) {
			return false;
        }
        
        model.getPasswordFile().setOldDigest(req.getBlob().getDigest());
		return true;
    }


    public void set() {
		this.pool.submit(new LongTaskRunner() {
			@Override
			boolean doRun() {
				return doSet();
			}
		});
    }
    private boolean doSet() {
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
    		// TODO: maybe allow null usernames?
    	} else if (password == null || password.length == 0) {
    		validationError = "Please enter a password.";
    	} else if (!Arrays.equals(password,confirm)) {
    		validationError = "Entered passwords do not match.";
    	}
		Utils.destroyPassword(confirm);
		
    	if (validationError != null) {
    		Utils.destroyPassword(password);
    		model.setLastException(new RecoverableException(validationError));
            return false;
    	}
    	
    	model.getPasswordFile().setVal(domain, username, new String(password));
    	
    	return doSave();
    }

    public void delete() {
		this.pool.submit(new LongTaskRunner() {
			@Override
			boolean doRun() {
				return doDelete();
			}
		});
    }
    
    private boolean doDelete() {
    	String domain = model.getFormText(TextForm.CRED_DOMAIN);
    	String username = model.getFormText(TextForm.CRED_USERNAME);
    	
    	boolean success = model.getPasswordFile().delVal(domain, username);
    	
    	if (success) {
    		// In case this fails, we should give user option to try saving again
    		success = this.doSave();
    		this.updateFormPassword(PasswordForm.CRED_PASSWORD, null);
    		this.updateFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);
    		return success;
    	}
    	else return false;
    }
    
    public void login() {
    	pool.execute(new LongTaskRunner() {
    		boolean doRun() {
    			return doLogin();
    		}
    	});
    }

    private boolean doLogin(){
    	String username = model.getFormText(TextForm.LOGIN_MASTER_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.LOGIN_MASTER_PASSWORD);

    	// Validate inputs
    	if (!MasterCredentials.isValidUsername(username)) {
    		model.setLastException(new RecoverableException(User.VALID_USERNAME_DOC));
            return false;
    	}
    	if (!MasterCredentials.isValidPassword(password)) {
    		model.setLastException(new RecoverableException("Please enter a password."));
            return false;
    	}
    	
    	// Update master credentials
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);
    	
    	// Fetch from server
    	boolean success = this.doFetch();
    	if (success) {
    		this.doStateTransition(ViewState.WAITING);
    	} else {
    		model.getMasterCredentials().destroy();
    		model.setMasterCredentials(null);
    	}
		model.setFormPassword(PasswordForm.LOGIN_MASTER_PASSWORD, null);
		return success;
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
    	model.setLastException(null);
    	model.setUserLogs(null);
    	
    	model.setViewState(ViewState.LOGIN);
    }

    private <T extends Response> T sendRequest(Request r, Class<T> t) {
    	try {
    		Response response = (Response) this.reqHandler.send(r);
    		response.checkException();
			return t.cast(response);
		} catch (UnknownHostException e) {
			String msg = "Could not connect to Kryptose\u2122 server at: " + this.properties.getProperty("SERVER_HOSTNAME");
			Exception ex = new RecoverableException(msg);
			model.setLastException(ex);
			this.logger.log(Level.WARNING, msg, e);
		} catch (InvalidCredentialsException e) {
			model.setLastException(e);
			this.logger.log(Level.INFO, "Invalid credentials.", e);
			// TODO error handling
		} catch (IOException e) {
			if(e.getCause() instanceof NoSuchAlgorithmException){
				String msg = "Error setting up the SSL Socket. Might be due to missing certificate files or wrong password. If persists, check the settings file or reinstall the app.";
				Exception ex = new RecoverableException(msg);
				model.setLastException(ex);
				this.logger.log(Level.WARNING, msg, e);
			}else{//generic IOException
				String msg = "Error communicating with Kryptose\u2122 server.";
				Exception ex = new RecoverableException(msg);
				model.setLastException(ex);
				this.logger.log(Level.WARNING, msg, e);				
			}
				
		} catch (MalformedRequestException | ClassCastException e) {
			String msg = "Error communicating with Kryptose\u2122 server.";
			Exception ex = new RecoverableException(msg);
			model.setLastException(ex);
			this.logger.log(Level.WARNING, msg, e);
		}catch (InternalServerErrorException e) {
			model.setLastException(e);
			this.logger.log(Level.WARNING, "Internal server error.", e);
            // TODO error handling
		}
    	// TODO we should also check that the client version is compatible.
    	return null;
    }

    public void fetch() {
    	pool.execute(new LongTaskRunner() {
    		boolean doRun() {
    			return doFetch();
    		}
    	});
    }
    
    private boolean doFetch(){
    	ResponseGet r = null;
    	MasterCredentials mCred = model.getMasterCredentials();
    	RequestGet req = new RequestGet(mCred.getUser());
    	r = this.sendRequest(req, ResponseGet.class);
    	if (r == null) {
    		return false;
    	}
    	if (r.getBlob() == null){
    		PasswordFile pFile = new PasswordFile(mCred);
    		model.setPasswordFile(pFile);
    		return true;
    	}
    	try {
    		PasswordFile pFile = new PasswordFile(mCred, r.getBlob());
    		pFile.setOldDigest(r.getBlob().getDigest());
    		model.setPasswordFile(pFile);
    		return true;
    	} catch (PasswordFile.BadBlobException | CryptoErrorException e) {
    		String msg = "Error decrypting the passwords file. This could be an outdate Kryptose\u2122 client, "
    				+ "or temporary data corruption. Please try again or update the client.";
    		Exception ex = new Exception(msg, e);
    		model.setLastException(ex);
    		logger.log(Level.SEVERE, "Error decrypting blob.", e);
    		return false;
    	}
    }

    public void fetchLogs(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			boolean doRun() {
                // TODO Auto-generated method stub
				logger.severe("fetchLogs not implemented in Controller");
				return false;
			}
		});
    }

    public void createAccount(){
    	pool.execute(new LongTaskRunner() {
    		boolean doRun() {
    			return doCreateAccount();
    		}
    	});
    }
    
    private boolean doCreateAccount() {
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
    		model.setLastException(new RecoverableException(validationError));
            return false;
    	}
    	
    	// mCred now has responsibility for destroying password.
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);

        try {
        	RequestCreateAccount req = new RequestCreateAccount(mCred.getUser());
        	ResponseCreateAccount r = this.sendRequest(req, ResponseCreateAccount.class);
        	if (r == null) {
        		return false;
        	}
            r.verifySuccessful();
            model.setPasswordFile(new PasswordFile(mCred));
			this.doStateTransition(ViewState.WAITING);
            return true;
        } catch (UsernameInUseException e) {
        	model.setLastException(e);
        	this.logger.log(Level.INFO, "Failed to create new account.", e);
            return false;
        }
    	
    }

    public void deleteAccount(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			boolean doRun() {
				return doDeleteAccount();
			}
		});
    }
    
    private boolean doDeleteAccount() {
//		logger.severe("deleteAccount not implemented in Controller");
//        System.out.println("deleting master password");
		MasterCredentials mCred = model.getMasterCredentials();
		char[] passwordConfirm = model.getFormPasswordClone(PasswordForm.DELETE_ACCOUNT_CONFIRM_PASSWORD);

        if (!Arrays.equals(mCred.getPassword(), passwordConfirm)) {
//            System.out.println("first");
            model.setLastException(new RecoverableException("Password incorrect."));
            return false;
        }

		// stuff happens here.
        RequestDeleteAccount req = new RequestDeleteAccount(model.getMasterCredentials().getUser());
		ResponseDeleteAccount r = this.sendRequest(req, ResponseDeleteAccount.class);
        if (r == null){
//            System.out.println("second");
            model.setLastException(new RecoverableException("Error: server unavailable"));
            return false;
        }
        if (!r.verifySuccessful()) {
//            System.out.println("third");
            model.setLastException(new RecoverableException("Error: account delete failed unexpectedly"));
            return false;
        }
    	this.doLogout();
        logger.severe("logging out");
//        System.out.println("logging out");
    	return true;
    }

    public void changeMasterPassword(){
		this.pool.submit(new LongTaskRunner() {
			@Override
			boolean doRun() {
                return doChangeMasterPassword();
			}
		});
    }

    private boolean doChangeMasterPassword() {
		logger.severe("changeMasterPassword not implemented in Controller");
		
		MasterCredentials mCred = model.getMasterCredentials();
		char[] oldPasswordConfirm = model.getFormPasswordClone(PasswordForm.CHANGE_OLD_MASTER_PASSWORD);
		char[] newPassword = model.getFormPasswordClone(PasswordForm.CHANGE_NEW_MASTER_PASSWORD);
		char[] newPasswordConfirm = model.getFormPasswordClone(PasswordForm.CHANGE_CONFIRM_NEW_MASTER_PASSWORD);

        if(!Arrays.equals(oldPasswordConfirm, mCred.getPassword())){
            model.setLastException(new RecoverableException("Wrong old password"));
            return false;
        }
        if(!Arrays.equals(newPassword, newPasswordConfirm)){
            model.setLastException(new RecoverableException("New passwords do not match"));
            return false;
        }

        PasswordFile pFile = model.getPasswordFile();
        MasterCredentials newMCred = new MasterCredentials(mCred.getUsername(), newPassword);

        Blob newBlob;
        try {
            newBlob = pFile.encryptBlob(newMCred, model.getLastModDate());
        } catch (BadBlobException | CryptoErrorException e) {
            model.setLastException(e);
            this.logger.log(Level.INFO, "Failed to change master password.", e);
            return false;
        }

        RequestChangePassword req = new RequestChangePassword(mCred.getUser(), newMCred.getAuthKey(), newBlob, pFile.getOldDigest());
        ResponseChangePassword r = this.sendRequest(req, ResponseChangePassword.class);

        model.setMasterCredentials(newMCred);
        model.setPasswordFile(new PasswordFile(newMCred));
        this.doStateTransition(ViewState.WAITING);

		return true;
    }
    
    public void updateFormText(TextForm form, String value) {
		this.pool.submit(new QuickTaskRunner() {
			@Override
			void doRun() {
		    	// TODO validate value? (probably too much work)
				model.setFormText(form, value);
				if (form == TextForm.CRED_DOMAIN || form == TextForm.CRED_USERNAME) {
					refreshCredOptions();
				}
			}
		});
    }

	public void updateFormPassword(PasswordForm form, char[] password) {
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
		};
		
		for (ViewState[] transition : allowedTransitions) {
			if (oldState == transition[0] && viewState == transition[1]) {
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
			this.refreshCredOptions();
		}
		
		if (oldState == ViewState.CHANGE_MASTER_PASSWORD) {
			this.model.setFormPassword(PasswordForm.CHANGE_OLD_MASTER_PASSWORD, null);
			this.model.setFormPassword(PasswordForm.CHANGE_NEW_MASTER_PASSWORD, null);
			this.model.setFormPassword(PasswordForm.CHANGE_CONFIRM_NEW_MASTER_PASSWORD, null);
		}
		
		if (oldState == ViewState.DELETE_ACCOUNT) {
			this.model.setFormPassword(PasswordForm.DELETE_ACCOUNT_CONFIRM_PASSWORD, null);
		}
//        model.setFormOptions(OptionsForm.CRED_DOMAIN, null);
//        model.setFormOptions(OptionsForm.CRED_USERNAME, null);
        model.setFormPassword(PasswordForm.CRED_PASSWORD, null);
        model.setFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);
        model.setFormPassword(PasswordForm.CREATE_CONFIRM_PASSWORD, null);
        for(PasswordForm e : PasswordForm.values())
            model.setFormPassword(e, null);

		this.model.setViewState(viewState);
	}
	
	private void refreshCredOptions() {
		PasswordFile pFile = model.getPasswordFile();
		
		String domain = model.getFormText(TextForm.CRED_DOMAIN);
		String username = model.getFormText(TextForm.CRED_USERNAME);
		
		String[] domainOptions = pFile.getDomains();
		String[] usernameOptions = pFile.getUsernames(domain);
		// WARNING: current code assumes that the char[] obtained from pFile.getVal
		// is a new copy, and hence destroys the copy obtained.
		String passString = pFile.getVal(domain, username);
		char[] password = passString == null ? null : passString.toCharArray();
		
		String[] domainOptionsWithNull = new String[domainOptions.length + 1];
		System.arraycopy(domainOptions, 0, domainOptionsWithNull, 1, domainOptions.length);
		String[] usernameOptionsWithNull = new String[usernameOptions.length + 1]; 
		System.arraycopy(usernameOptions, 0, usernameOptionsWithNull, 1, usernameOptions.length);

		model.setFormPassword(PasswordForm.CRED_PASSWORD, null);
		model.setFormPassword(PasswordForm.CRED_CONFIRM_PASSWORD, null);
		model.setFormOptions(OptionsForm.CRED_DOMAIN, domainOptionsWithNull);
		model.setFormOptions(OptionsForm.CRED_USERNAME,
				usernameOptions.length <= 1 ? usernameOptions : usernameOptionsWithNull);
		
	}




	
	public static void main(String[] args) {
		// For debugging purposes.
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.FINE);
		Logger clientLogger = Logger.getLogger("org.kryptose.client");
		clientLogger.setLevel(Level.FINE);
		clientLogger.addHandler(handler);
		new Controller().start();
	}


}
