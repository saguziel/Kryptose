package org.kryptose.client;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;
import org.kryptose.exceptions.CryptoErrorException;
import org.kryptose.exceptions.InternalServerErrorException;
import org.kryptose.exceptions.InvalidCredentialsException;
import org.kryptose.exceptions.MalformedRequestException;
import org.kryptose.exceptions.RecoverableException;
import org.kryptose.exceptions.UsernameInUseException;
import org.kryptose.requests.KeyDerivator;
import org.kryptose.requests.Request;
import org.kryptose.requests.RequestCreateAccount;
import org.kryptose.requests.RequestGet;
import org.kryptose.requests.Response;
import org.kryptose.requests.ResponseCreateAccount;
import org.kryptose.requests.ResponseGet;
import org.kryptose.requests.User;

/**
 * Created by jeff on 4/27/15.
 */
public class Controller {
	
    private static final String PROPERTIES_FILE = "clientProperties.xml";
	
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

        //SETTING DEFAULT CONFIGURATIONS (can be overriden by the Client settings file)
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

    public void get(){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }


    public void query(String domain, String username){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }


    public void save(){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }


    public void set(String domain, String username){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }


    public void delete(String domain, String username){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }
    
    public void login() {
    	model.setWaitingOnServer(true);
    	pool.execute(new Runnable() {
    		public void run() {
    			doLogin();
    		}
    	});
    }

    private void doLogin(){
    	String username = model.getFormText(TextForm.LOGIN_MASTER_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.LOGIN_MASTER_PASSWORD);

    	// Validate inputs
    	if (!MasterCredentials.isValidUsername(username)) {
    		model.setLastServerException(new RecoverableException(User.VALID_USERNAME_DOC));
            this.processResponse(false);
    		return;
    	}
    	if (!MasterCredentials.isValidPassword(password)) {
    		model.setLastServerException(new RecoverableException("Please enter a password."));
            this.processResponse(false);
    		return;
    	}
    	
    	// Update master credentials
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);
    	
    	// Clear password field
    	//model.setFormPassword(PasswordForm.LOGIN_MASTER_PASSWORD, null);
    	
    	// Fetch from server
    	this.doFetch();
    }
    
    public void logout() {
    	this.pool.submit(new Runnable() {
			@Override
			public void run() {
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
    	model.setLastServerException(null);
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
			model.setLastServerException(ex);
			this.logger.log(Level.WARNING, msg, e);
		} catch (InvalidCredentialsException e) {
			model.setLastServerException(e);
			this.logger.log(Level.INFO, "Invalid credentials.", e);
			// TODO error handling
		} catch (MalformedRequestException | IOException | ClassCastException e) {
			String msg = "Error communicating with Kryptose\u2122 server.";
			Exception ex = new RecoverableException(msg);
			model.setLastServerException(ex);
			this.logger.log(Level.WARNING, msg, e);
		} catch (InternalServerErrorException e) {
			model.setLastServerException(e);
			this.logger.log(Level.WARNING, "Internal server error.", e);
            // TODO error handling
		}
    	// TODO we should also check that the client version is compatible.
    	return null;
    }

    public void fetch() {
    	pool.execute(new Runnable() {
    		public void run() {
    			doFetch();
    		}
    	});
    }
    
    private void doFetch(){
    	model.setWaitingOnServer(true);
    	
        ResponseGet r = null;
        boolean success = false;
        try {
        	MasterCredentials mCred = model.getMasterCredentials();
        	RequestGet req = new RequestGet(mCred.getUser());
			r = this.sendRequest(req, ResponseGet.class);
			if (r == null) {
				success = false;
				return;
			}
            if (r.getBlob() == null){
                PasswordFile pFile = new PasswordFile(mCred.getUsername());
                model.setPasswordFile(pFile);
            	success = true;
            	return;
            }
            try {
            	PasswordFile pFile = new PasswordFile(mCred, r.getBlob());
            	pFile.setOldDigest(r.getBlob().getDigest());
            	model.setPasswordFile(pFile);
            	success = true;
            	return;
            } catch (PasswordFile.BadBlobException | CryptoErrorException e) {
            	String msg = "Error decrypting the passwords file. This could be an outdate Kryptose\u2122 client, "
            			+ "or temporary data corruption. Please try again or update the client.";
            	Exception ex = new Exception(msg, e);
            	model.setLastServerException(ex);
            	logger.log(Level.SEVERE, "Error decrypting blob.", e);
            	success = false;
            	return;
            }
        } finally {
        	this.processResponse(success);
        }
    }

    public void fetchLogs(){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }

    public void createAccount(){
    	pool.execute(new Runnable() {
    		public void run() {
    			doCreateAccount();
    		}
    	});
    }
    
    private void doCreateAccount() {
    	model.setWaitingOnServer(true);
    	
    	String username = model.getFormText(TextForm.CREATE_MASTER_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.CREATE_MASTER_PASSWORD);
    	
    	// Validate inputs
    	if (!MasterCredentials.isValidUsername(username)) {
    		model.setLastServerException(new RecoverableException(User.VALID_USERNAME_DOC));
            this.processResponse(false);
    		return;
    	}
    	if (!MasterCredentials.isValidPassword(password)) {
    		model.setLastServerException(new RecoverableException("Please enter a password."));
            this.processResponse(false);
    		return;
    	}
    	
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);

        try {
        	RequestCreateAccount req = new RequestCreateAccount(mCred.getUser());
        	ResponseCreateAccount r = this.sendRequest(req, ResponseCreateAccount.class);
        	if (r == null) {
        		this.processResponse(false);
        		return;
        	}
            r.verifySuccessful();
            model.setPasswordFile(new PasswordFile(mCred.getUsername()));
            this.processResponse(true);
        } catch (UsernameInUseException e) {
        	model.setLastServerException(e);
        	this.logger.log(Level.INFO, "Failed to create new account.", e);
            this.processResponse(false);
        }
    	
    }

    public void deleteAccount(){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }

    public void changeMasterpass(){
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
                // TODO Auto-generated method stub
			}
		});
    }
    
    public void updateFormText(TextForm form, String value) {
    	// TODO validate value?
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
				model.setFormText(form, value);
			}
		});
    }

	public void exit() {
		// TODO confirm exit.
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
				doLogout();
				pool.shutdown();
				view.shutdown();
			}
		});
	}

	private void processResponse(boolean success) {
		ViewState viewState = model.getViewState();
		
		model.setWaitingOnServer(false);
		
		if (viewState == ViewState.LOGIN) {
			if (success == true) this.doStateTransition(ViewState.WAITING);
			else model.setFormPassword(PasswordForm.LOGIN_MASTER_PASSWORD, null);
			return;
		}
		
		if (viewState == ViewState.CREATE_ACCOUNT) {
			if (success == true) this.doStateTransition(ViewState.WAITING);
			return;
		}
		// TODO process other requests
	}

	public void requestViewState(final ViewState viewState) {
		this.pool.submit(new Runnable() {
			@Override
			public void run() {
				doRequestViewState(viewState);
			}
		});
	}
	private void doRequestViewState(ViewState viewState) {
		ViewState oldState = this.model.getViewState();
		
		if (oldState == ViewState.LOGIN
				&& viewState == ViewState.CREATE_ACCOUNT) {
			this.doStateTransition(viewState);
			
		} else if (oldState == ViewState.CREATE_ACCOUNT
				&& viewState == ViewState.LOGIN) {
			this.doStateTransition(viewState);
			
		} else if (oldState == ViewState.WAITING
				&& viewState == ViewState.MANAGING) {
			this.doStateTransition(viewState);
			
		} else if (oldState == ViewState.MANAGING
				&& viewState == ViewState.WAITING) {
			this.doStateTransition(viewState);
			
		} else {
			logger.log(Level.SEVERE, "Bad view state transition from " + oldState + " to " + viewState);
		}
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

		this.model.setViewState(viewState);
	}




	
	public static void main(String[] args) {
		new Controller().start();
	}


}
