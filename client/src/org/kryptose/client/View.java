package org.kryptose.client;

import org.kryptose.requests.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Created by jeff on 4/27/15.
 */
public interface View {

    public void updatePassFile();

    public void updateUsername();

    public void updateLogs();

    public void updateLastMod();

    // todo update sync status

    // todo update last error

}