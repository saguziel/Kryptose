package org.kryptose.server;

import org.kryptose.requests.*;

import java.util.concurrent.Callable;

/**
 * Created by alexguziel on 3/15/15.
 */
public class HandledRequestGet implements Callable<Response> {
    private final RequestGet request;

    public HandledRequestGet(RequestGet r) {
        request = r;
    }

    public Response call() {
        User u = request.getUser();
        DataStore ds = FileSystemDataStore.getInstance();

        boolean hasBlob = ds.userHasBlob(u);
        if (hasBlob) {
            Blob b = ds.readBlob(u);
            if (b == null) {
                return new ResponseInternalServerError();
            } else {
                return new ResponseGet(b, null); // TODO logging
            }
        } else {
            return new ResponseInvalidCredentials(u);
        }
    }
}
