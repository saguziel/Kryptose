package org.kryptose.client;

import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;

/**
 * Created by jeff on 4/27/15.
 */
public class Controller {
	
	private Model model;
	private View view;
	
	public void start() {
		this.model = new Model();
		this.view = new ViewGUI(model, this);
		
		this.model.registerView(view);
		this.model.setViewState(ViewState.LOGIN);
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

    public void login(){
    	// TODO: controller threading?
    	// TODO: validate username and password
    	String username = model.getFormText(TextForm.MASTER_USERNAME);
    	char[] password = model.getFormPasswordClone(PasswordForm.MASTER_PASSWORD);
    	
    	MasterCredentials mCred = new MasterCredentials(username, password);
    	model.setMasterCredentials(mCred);
    	
    	model.setFormPassword(PasswordForm.MASTER_PASSWORD, null);
    	
    	// TODO: what's the difference between get and fetch?
    	this.get();
    	model.setWaitingOnServer(true);
    }

    public void logout(){
    	// TODO: controller threading?
    	MasterCredentials mCred = model.getMasterCredentials();
    	mCred.destroy();
    	model.setMasterCredentials(null);
    	
    	PasswordFile pFile = model.getPasswordFile();
    	pFile.destroy();
    	model.setPasswordFile(null);
    	
    	model.setLastModDate(null);
    	model.setWaitingOnServer(false); // TODO: waiting on server when logout?
    	model.setLastServerException(null);
    	model.setUserLogs(null);
    	
    	model.setViewState(ViewState.LOGIN);
    }

    public void fetch(){


    }

    public void fetchLogs(){


    }

    public void createAccount(MasterCredentials m){


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
		System.exit(0);
	}


















}
