package org.kryptose.server;

import org.kryptose.requests.*;

import java.util.concurrent.Callable;

/**
 * Created by alexguziel on 3/15/15.
 */
public class HandledGetRequest implements Callable<Response> {
    private final GetRequest request;

    public HandledGetRequest(GetRequest r) {
        request = r;
    }

    public Response call() {
        User u = request.getUser();
        Blob b = DataStore.readBlob(u);
        if (b == null) {
            return new ResponseInvalidCredentials(u);
        } else {
            return new ResponseGet(b, u);
        }
    }
}
