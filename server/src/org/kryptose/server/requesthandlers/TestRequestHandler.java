package org.kryptose.server.requesthandlers;

import org.kryptose.requests.Response;
import org.kryptose.requests.TestRequest;
import org.kryptose.requests.TestResponse;
import org.kryptose.requests.User;
import org.kryptose.server.Server;

import java.util.concurrent.Callable;

public class TestRequestHandler extends RequestHandler<TestRequest> {


    public TestRequestHandler() {
        super(TestRequest.class);
    }

    @Override
    protected Callable<Response> handleRequestImpl(Server server, User user,
                                                   final TestRequest request) {
        return new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                TestResponse resp = new TestResponse("This is a response to the request " + request.toString());
                System.out.println("I am handling the request " + request.toString());
                return resp;
            }

        };
    }

}