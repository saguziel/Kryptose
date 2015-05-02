package org.kryptose.client;

import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.OptionsForm;
import org.kryptose.client.Model.TextForm;


/**
 * Created by jeff on 4/27/15.
 */
public interface View {

    public void updateMasterCredentials();

    public void updateLogs();

    public void updateLastMod();

    public void updatePasswordFile();

    public void updateServerException();

    public void updateSyncStatus();

	public void updateTextForm(TextForm form);

	public void updatePasswordForm(PasswordForm form);

	public void updateSelection(OptionsForm selection);

	public void updateViewState();

	public void shutdown();


}