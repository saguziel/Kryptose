package org.kryptose.server;

import java.util.concurrent.Callable;

import org.kryptose.requests.Response;
import org.kryptose.requests.TestRequest;
import org.kryptose.requests.TestResponse;

public class HandledRequestTest implements Callable<Response> {
    private final TestRequest request;

    public HandledRequestTest(TestRequest r) {
        request = r;
    }

    @Override
    public Response call() {
    	return new TestResponse(request.toString());
    }

}
