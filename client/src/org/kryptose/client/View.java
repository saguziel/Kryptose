package org.kryptose.client;

import org.kryptose.requests.Log;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Created by jeff on 4/27/15.
 */
public interface View {

    public void updatePassFile(PasswordFile p);

    public void updateUsername(String u);

    public void updateLogs(ArrayList<Log> userlog);

    public void updateLastMod(LocalDateTime mod);

    // todo update sync status

    // todo update last error

}