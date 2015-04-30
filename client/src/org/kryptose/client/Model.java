package org.kryptose.client;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.kryptose.requests.Log;

public class Model {
	private MasterCredentials masterCredentials = null;
	private PasswordFile passwordFile = null;
	private ArrayList<Log> userLogs = null;
	private LocalDateTime lastModDate = null;
	private Exception lastServerException = null;
	private boolean waitingOnServer = false;

	public static enum TextForm {
		LOGIN_MASTER_USERNAME, CREATE_MASTER_USERNAME, CRED_DOMAIN, CRED_PASSWORD, 
	}
	public static enum PasswordForm {
		LOGIN_MASTER_PASSWORD, CREATE_MASTER_PASSWORD, CRED_PASSWORD
	}
	public static enum Selection {
		CRED_DOMAIN, CRED_USERNAME
	}
	public static enum ViewState {
		LOGIN, CREATE_ACCOUNT, WAITING, DISPLAYING_DOMAIN, DISPLAYING_CRED, CONFIGURING
	}
	private ViewState viewState = null;
	private Map<TextForm,String> formTexts = new EnumMap<TextForm,String>(TextForm.class);
	private Map<PasswordForm,char[]> formPasses = new EnumMap<PasswordForm,char[]>(PasswordForm.class);
	private Map<Selection,String> selections = new EnumMap<Selection,String>(Selection.class);
	
	private View view;
	
	public Model() {
		super();
	}
	
	public synchronized void registerView(View view) {
		if (this.view != null) throw new IllegalStateException("view already registered with model.");
		this.view = view;
	}

	public synchronized MasterCredentials getMasterCredentials() {
		return masterCredentials;
	}

	public synchronized void setMasterCredentials(MasterCredentials masterCredentials) {
		if (equals(this.masterCredentials, masterCredentials)) return;
		this.masterCredentials = masterCredentials;
        view.updateMasterCredentials();
	}
	
	public synchronized PasswordFile getPasswordFile() {
		return passwordFile;
	}

	public synchronized void setPasswordFile(PasswordFile passwordFile) {
		if (equals(this.passwordFile, passwordFile)) return;
		// TODO: if passwordfile is mutable, notify of changes.
		this.passwordFile = passwordFile;
        view.updatePasswordFile();
	}
	
	public synchronized ArrayList<Log> getUserLogs() {
		return userLogs;
	}

	public synchronized void setUserLogs(ArrayList<Log> userLogs) {
		if (equals(this.userLogs, userLogs)) return;
		this.userLogs = userLogs;
        view.updateLogs();
	}
	
	public synchronized LocalDateTime getLastModDate() {
		return lastModDate;
	}

	public synchronized void setLastModDate(LocalDateTime lastModDate) {
		if (equals(this.lastModDate, lastModDate)) return;
		this.lastModDate = lastModDate;
        view.updateLastMod();
	}
	
	// Hmm probably want a different format for this.
	public synchronized Exception getLastServerException() {
		return lastServerException;
	}

	public synchronized void setLastServerException(Exception lastServerException) {
		if (this.lastServerException == lastServerException) return;
		this.lastServerException = lastServerException;
        view.updateServerException();
	}
	
	public synchronized boolean isWaitingOnServer() {
		return waitingOnServer;
	}

	public synchronized void setWaitingOnServer(boolean waitingOnServer) {
		if (equals(this.waitingOnServer, waitingOnServer)) return;
		this.waitingOnServer = waitingOnServer;
        view.updateSyncStatus();
	}

	public synchronized ViewState getViewState() {
		return viewState;
	}

	public synchronized void setViewState(ViewState viewState) {
		if (equals(this.viewState, viewState)) return;
		this.viewState = viewState;
		view.updateViewState();
	}
	
	public synchronized String getFormText(TextForm form) {
		return this.formTexts.get(form);
	}
	
	public synchronized void setFormText(TextForm form, String value) {
		// Store empty as null
		if (value != null && value.length() == 0) value = null;

		// Check if value changes
		String oldValue = this.formTexts.get(form);
		if (equals(oldValue, value)) return;

		// Make change and notify view
		this.formTexts.put(form, value);
		this.view.updateTextForm(form);
	}
	
	/**
	 * Caller is responsible for destroying the password returned!
	 * @param form
	 * @return
	 */
	public synchronized char[] getFormPasswordClone(PasswordForm form) {
		char[] value = this.formPasses.get(form);
		return value == null ? null : value.clone();
	}
	
	/**
	 * Destroys the old value.
	 * @param form
	 * @param value
	 */
	public synchronized void setFormPassword(PasswordForm form, char[] value) {
		// Store empty as null
		if (value != null && value.length == 0) value = null;
		
		// Check if value changes
		char[] oldValue = this.formPasses.get(form);
		if (equals(oldValue, value)) return;
		
		// Destroy old value
		if (oldValue != null) Arrays.fill(oldValue, ' ');
		
		// Make change and notify view
 		this.formPasses.put(form, value);
		this.view.updatePasswordForm(form);
	}
	
	public synchronized String getSelection(Selection selection) {
		return this.selections.get(selection);
	}
	
	public synchronized void setSelection(Selection selection, String value) {
		// Store empty as null
		if (value != null && value.length() == 0) value = null;

		// Check if value changes
		String oldValue = this.selections.get(selection);
		if (equals(oldValue, value)) return;
		
		// Make change and notify view
		this.selections.put(selection, value);
		this.view.updateSelection(selection);
	}
	
	private static boolean equals(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}
	
}
