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
import org.kryptose.requests.KeyDerivator;
import org.kryptose.requests.RequestGet;
import org.kryptose.requests.Response;
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


    }


    public void query(String domain, String username){



    }


    public void save(){


    }


    public void set(String domain, String username){


    }


    public void delete(String domain, String username){


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
    	// TODO: controller threading?
    	// TODO: validate username and password
    	String username = model.getFormText(TextForm.MASTER_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.MASTER_PASSWORD);
    	
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);
    	
    	model.setFormPassword(PasswordForm.MASTER_PASSWORD, null);
    	
    	this.doFetch();
    }

    public void logout(){
    	// TODO: controller threading?
    	MasterCredentials mCred = model.getMasterCredentials();
    	if (mCred != null) mCred.destroy();
    	model.setMasterCredentials(null);
    	
    	PasswordFile pFile = model.getPasswordFile();
    	if (pFile != null) pFile.destroy();
    	model.setPasswordFile(null);
    	
    	model.setLastModDate(null);
    	model.setWaitingOnServer(false); // TODO: waiting on server when logout?
    	model.setLastServerException(null);
    	model.setUserLogs(null);
    	
    	model.setViewState(ViewState.LOGIN);
    }

    public void fetch() {
    	model.setWaitingOnServer(true);
    	pool.execute(new Runnable() {
    		public void run() {
    			doFetch();
    		}
    	});
    }

    private void doFetch(){
        ResponseGet r = null;
        boolean success = false;

		try {
        	MasterCredentials mCred = model.getMasterCredentials();
			User user = new User(mCred.getUsername(), mCred.getAuthKey());
			r = (ResponseGet) reqHandler.send(new RequestGet(user));
            // TODO: catch ClassCastException and handle it.
            if(r.getBlob() == null){
                PasswordFile pFile = new PasswordFile(mCred.getUsername());
                model.setPasswordFile(pFile);
                success = true;
            } else {
                try {
                	PasswordFile pFile = new PasswordFile(mCred, r.getBlob());
                	pFile.setOldDigest(r.getBlob().getDigest());
                    model.setPasswordFile(pFile);
                    success = true;
                } catch (PasswordFile.BadBlobException | CryptoErrorException e) {
                    // TODO error handling
                	logger.log(Level.SEVERE, "Error decrypting blob.", e);
                }
            }
		} catch (UnknownHostException e1) {
			String msg = "Could not connect to Kryptose\u2122 server at: " + this.properties.getProperty("SERVER_HOSTNAME");
			Exception ex = new RecoverableException(msg);
			model.setLastServerException(ex);
			this.logger.log(Level.WARNING, msg, e1);
		} catch (IOException e1) {
			String msg = "Error communicating with Kryptose\u2122 server.";
			Exception ex = new RecoverableException(msg);
			model.setLastServerException(ex);
			this.logger.log(Level.WARNING, msg, e1);
		} catch (InvalidCredentialsException e1) {
			model.setLastServerException(e1);
			this.logger.log(Level.INFO, "Invalid credentials.", e1);
        } catch (MalformedRequestException e1) {
			model.setLastServerException(e1);
			this.logger.log(Level.WARNING, "", e1);
            // TODO error handling
        } catch (InternalServerErrorException e1) {
			model.setLastServerException(e1);
			this.logger.log(Level.WARNING, "", e1);
            // TODO error handling
        } finally {
        	// TODO make sure processResponse works well with error handling.
        	processResponse(success);
        }
		
    }

    public void fetchLogs(){


    }

    public void createAccount(){
    	model.setWaitingOnServer(true);
    	pool.execute(new Runnable() {
    		public void run() {
    			doCreateAccount();
    		}
    	});
    }
    
    private void doCreateAccount() {
    	
    }

    public void deleteAccount(){


    }

    public void changeMasterpass(){


    }
    
    public void updateFormText(TextForm form, String value) {
    	// TODO validate value.
    	this.model.setFormText(form, value);
    }

	public void exit() {
		// TODO confirm exit.
		logout();
		this.pool.shutdown();
		this.view.shutdown();
	}

	private void processResponse(boolean success) {
		ViewState viewState = model.getViewState();
		
		model.setWaitingOnServer(false);
		
		if (viewState == ViewState.LOGIN && success == true) {
			model.setViewState(ViewState.WAITING);
		}
		// TODO process other requests
	}
	
	public static void main(String[] args) {
		new Controller().start();
	}
















}
