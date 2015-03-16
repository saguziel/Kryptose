package org.kryptose.server;

import org.kryptose.requests.*;

import java.util.concurrent.Callable;

/**
 * Created by alexguziel on 3/15/15.
 */
public class HandledRequest implements Callable<Response> {
    Request request;

    public HandledRequest(Request r) {
        request = r;
    }

    public Response call() throws Exception {
        Callable<Response> caller;
        if (request instanceof RequestGet) {
            caller = new HandledRequestGet((RequestGet) request);
        } else if (request instanceof RequestPut) {
            caller = new HandledRequestPut((RequestPut) request);
        } else {
            return new ResponseInternalServerError();
        }
        return caller.call();
    }
}
