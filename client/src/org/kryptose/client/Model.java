package org.kryptose.client;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.kryptose.requests.Log;

public class Model {
	private MasterCredentials masterCredentials = null;
	private PasswordFile passwordFile = null;
	private ArrayList<Log> userLogs = null;
	private LocalDateTime lastModDate = null;
	private Exception lastServerException = null;
	private boolean waitingOnServer = false;
	
	private View view;
	
	public Model(View view) {
		super();
		this.view = view;
	}

	public synchronized MasterCredentials getMasterCredentials() {
		return masterCredentials;
	}

	public synchronized void setMasterCredentials(
			MasterCredentials masterCredentials) {
		this.masterCredentials = masterCredentials;
        view.updateMasterCredentials();
	}
	
	public synchronized PasswordFile getPasswordFile() {
		return passwordFile;
	}

	public synchronized void setPasswordFile(PasswordFile passwordFile) {
		this.passwordFile = passwordFile;
        view.updatePasswordFile();
	}
	
	public synchronized ArrayList<Log> getUserLogs() {
		return userLogs;
	}

	public synchronized void setUserLogs(ArrayList<Log> userLogs) {
		this.userLogs = userLogs;
        view.updateLogs();
	}
	
	public synchronized LocalDateTime getLastModDate() {
		return lastModDate;
	}

	public synchronized void setLastModDate(LocalDateTime lastModDate) {
		this.lastModDate = lastModDate;
        view.updateLastMod();
	}
	
	// Hmm probably want a different format for this.
	public synchronized Exception getLastServerException() {
		return lastServerException;
	}

	public synchronized void setLastServerException(Exception lastServerException) {
		this.lastServerException = lastServerException;
        view.updateServerException();
	}
	
	public synchronized boolean isWaitingOnServer() {
		return waitingOnServer;
	}

	public synchronized void setWaitingOnServer(boolean waitingOnServer) {
		this.waitingOnServer = waitingOnServer;
        view.updateSyncStatus();
	}
	
	
}
