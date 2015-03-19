package org.kryptose.server;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;
import org.kryptose.requests.User;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Log implements Serializable {
	//TODO: So far the only thing you can log are requests.
	// We need a broader way to log events (like ssl connections, ip addresses)

    String message;
    User user;
    Request request;
    Response response;
    ZonedDateTime time;

    public Log(User u, Request req, Response res) {
        time = ZonedDateTime.now(ZoneId.of("UTC"));
        request = req;
        response = res;
        user = u;
        message = String.format("%s\nUsername: %s\n%s%s\n", time, user.getUsername(), request.logEntry(), response.logEntry());
    }

    public String toString() {
        return message;
    }

}
