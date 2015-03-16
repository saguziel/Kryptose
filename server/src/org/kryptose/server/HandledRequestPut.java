package org.kryptose.server;

import org.kryptose.requests.*;

import java.util.concurrent.Callable;

/**
 * Created by alexguziel on 3/15/15.
 */
public class HandledRequestPut implements Callable<Response> {
    private final RequestPut request;

    public HandledRequestPut(RequestPut r) {
        request = r;
    }

    public Response call() {
        User u = request.getUser();
        DataStore ds = DataStore.getInstance();
        byte[] oldDigest = request.getOldDigest();
        Blob toBeWritten = request.getBlob();

        DataStore.WriteResult writeResult = ds.writeBlob(u, toBeWritten, oldDigest);
        switch (writeResult) {
            case SUCCESS:
                return new ResponsePut(u, toBeWritten);
            case USER_DOES_NOT_EXIST:
                return new ResponseInvalidCredentials(u);
            case STALE_WRITE:
                return new ResponseStaleWrite(u, oldDigest, toBeWritten.getDigest());
            case INTERNAL_ERROR:
            default:
                return new ResponseInternalServerError();
        }
    }
}
