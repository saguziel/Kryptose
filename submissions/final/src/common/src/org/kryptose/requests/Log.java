package org.kryptose.requests;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Log implements Serializable {

	private static final long serialVersionUID = 2220976602447785L;

	String message;
    User user;
    Request request;
    Response response;
    ZonedDateTime time;
    int connectionId;

    public Log(User u, Request req, Response res) {
        time = ZonedDateTime.now(ZoneId.of("UTC"));
        request = req;
        response = res;
        user = u;
        if (req.getConnection() != null) {
        	connectionId = req.getConnection().getId();
        }
        message = String.format("%s%nUsername: %s%nConnection ID:%s%n%s%s%n",
        		time, user.getUsername(), connectionId, request.logEntry(), response.logEntry());
    }

    public String toString() {
        return message;
    }

}
