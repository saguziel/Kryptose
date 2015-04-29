package org.kryptose.client;


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


}