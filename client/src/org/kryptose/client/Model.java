package org.kryptose.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kryptose.requests.Log;

public class Model implements PropertyChangeListener {
	private Logger logger = Logger.getLogger(Model.class.getCanonicalName());
	
	private MasterCredentials masterCredentials = null;
	private PasswordFile passwordFile = null;
	private ArrayList<Log> userLogs = null;
	private LocalDateTime lastModDate = null;
	private Exception lastServerException = null;
	private boolean waitingOnServer = false;

	public static enum TextForm {
		LOGIN_MASTER_USERNAME, CREATE_MASTER_USERNAME,
		CRED_DOMAIN, CRED_USERNAME
	}
	public static enum PasswordForm {
		LOGIN_MASTER_PASSWORD,
		CREATE_MASTER_PASSWORD, CREATE_CONFIRM_PASSWORD,
		CRED_PASSWORD, CRED_CONFIRM_PASSWORD,
		CHANGE_OLD_MASTER_PASSWORD, CHANGE_NEW_MASTER_PASSWORD, CHANGE_CONFIRM_NEW_MASTER_PASSWORD,
		DELETE_ACCOUNT_CONFIRM_PASSWORD
	}
	public static enum OptionsForm {
		CRED_DOMAIN, CRED_USERNAME
	}
	public static enum ViewState {
		LOGIN, CREATE_ACCOUNT, WAITING, MANAGING, CHANGE_MASTER_PASSWORD, DELETE_ACCOUNT
	}
	private ViewState viewState = null;
	private Map<TextForm,String> formTexts = new EnumMap<TextForm,String>(TextForm.class);
	private Map<PasswordForm,char[]> formPasses = new EnumMap<PasswordForm,char[]>(PasswordForm.class);
	private Map<OptionsForm,String[]> formOptions = new EnumMap<OptionsForm,String[]>(OptionsForm.class);
	
	private View view;
	
	public Model() {
		super();
		this.logger.setLevel(Level.FINE);
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
		logger.fine("Master credentials changed.");
	}
	
	public synchronized PasswordFile getPasswordFile() {
		return passwordFile;
	}

	public synchronized void setPasswordFile(PasswordFile passwordFile) {
		if (this.passwordFile == passwordFile) return;
		this.passwordFile = passwordFile;
		
		if (passwordFile != null) passwordFile.addChangeListener(this);
        view.updatePasswordFile();
		logger.fine("Password file changed.");
	}
	
	public synchronized ArrayList<Log> getUserLogs() {
		return userLogs;
	}

	public synchronized void setUserLogs(ArrayList<Log> userLogs) {
		if (equals(this.userLogs, userLogs)) return;
		this.userLogs = userLogs;
        view.updateLogs();
		logger.fine("User logs changed.");
	}
	
	public synchronized LocalDateTime getLastModDate() {
		return lastModDate;
	}

	public synchronized void setLastModDate(LocalDateTime lastModDate) {
		if (equals(this.lastModDate, lastModDate)) return;
		this.lastModDate = lastModDate;
        view.updateLastMod();
		logger.fine("LastModDate changed: " + lastModDate);
	}
	
	// Hmm probably want a different format for this.
	public synchronized Exception getLastServerException() {
		return lastServerException;
	}

	public synchronized void setLastException(Exception lastServerException) {
		if (this.lastServerException == lastServerException) return;
		this.lastServerException = lastServerException;
        view.updateServerException();
		logger.fine("Last exception changed: " + lastServerException);
	}
	
	public synchronized boolean isWaitingOnServer() {
		return waitingOnServer;
	}

	public synchronized void setWaitingOnServer(boolean waitingOnServer) {
		if (equals(this.waitingOnServer, waitingOnServer)) return;
		this.waitingOnServer = waitingOnServer;
        view.updateSyncStatus();
		logger.fine("Waiting status changed: " + waitingOnServer);
	}

	public synchronized ViewState getViewState() {
		return viewState;
	}

	public synchronized void setViewState(ViewState viewState) {
		if (equals(this.viewState, viewState)) return;
		this.viewState = viewState;
		view.updateViewState();
		logger.fine("View state changed: " + viewState);
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
		logger.fine("Form value updated: " + form + " => " + value);
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
	 * Destroys the old value. Will destroy the copy that it is given.
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
		Utils.destroyPassword(oldValue);
		
		// Make change and notify view
 		this.formPasses.put(form, value);
		this.view.updatePasswordForm(form);
		logger.fine("Form value updated: " + form);
	}
	
	public synchronized String[] getFormOptions(OptionsForm options) {
		return this.formOptions.get(options);
	}
	
	public synchronized void setFormOptions(OptionsForm options, String[] values) {
		// Store empty as null
		if (values != null && values.length == 0) values = null;

		// Check if value changes
		String[] oldValue = this.formOptions.get(options);
		if (Arrays.equals(oldValue, values)) return;
		
		// Make change and notify view
		this.formOptions.put(options, values);
		this.view.updateSelection(options);
		logger.fine("Form value updated: " + options + " => " + Arrays.toString(values));
	}
	
	private static boolean equals(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == this.passwordFile) {
			logger.fine("Password file updated.");
			this.view.updatePasswordFile();
		}
	}

}
