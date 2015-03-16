package org.kryptose.server;

import org.kryptose.requests.Blob;
import org.kryptose.requests.User;

public class DataStore {

    private static DataStore instance = null;

    private DataStore() {

    }

    public static void writeBlob(User user, Blob blob) {
        // TODO
        // should probably do some sort of error handling
    }

    public static Blob readBlob(User user) {
        // TODO
        return null;
	}

    public static synchronized DataStore getInstance() {
        if (instance != null) {
            return instance;
        } else {
            instance = new DataStore();
            return instance;
        }
    }

}
