package org.kryptose.server;

import org.kryptose.exceptions.MalformedRequestException;
import org.kryptose.requests.Request;
import org.kryptose.requests.Response;
import org.kryptose.requests.ResponseErrorReport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;

/**
 * Runs a thread that listens for requests from a client, and sends back responses when available.
 *
 */
class ClientHandler implements Runnable {

	private Server server;
	private Socket sock;
	private Connection connection;

	ClientHandler(Server server, Socket clientSocket, Connection connection) {
		this.server = server;
		this.sock = clientSocket;
		this.connection = connection;
	}

	public void run() {
		try {
			// Listen for request.
			Request request = listen();
			if (request == null) {
				speak(new ResponseErrorReport(new MalformedRequestException()));
				return;
			}
			request.setConnection(connection);

			// Process request.
			Response resp = this.server.handleRequest(request);

			// Send back the response.
			if (resp != null) {
				
				speak(resp);
			}
		}
		finally {
			try {
				if (!sock.isClosed()) sock.close();
			} catch (IOException e) {
				String errorMsg = "Error flushing/closing a connection with a client. "
						+ "\nConnection: " + sock.hashCode();
				this.server.getLogger().log(Level.INFO, errorMsg, e);
			}
		}

	}

	/**
	 * Listen for a request from the client.
	 * @return The request heard, or {@literal null} if the connection is no longer valid
	 * 		or the input is malformed.
	 */
	private Request listen() {
		ObjectInputStream in=null;

		try {
			in = new ObjectInputStream(sock.getInputStream());
			Request retVal = (Request)in.readObject();
			retVal.validateInstance();
			return retVal;
		} catch (IOException ex) {
			String errorMsg = "Error reading a client's request. Aborting.";
			this.server.getLogger().log(Level.INFO, errorMsg, ex);
			return null;
		} catch (ClassNotFoundException | ClassCastException | IllegalArgumentException e) {
			String errorMsg = "Client sent unexpected object in connection. " +
					"Might be attempt to hack server. Aborting."
					+ "\nConnection: " + sock.hashCode();;
			this.server.getLogger().log(Level.WARNING, errorMsg, e);
			return null;
		} 
	}

	/**
	 * Sends a response back to the client.
	 * @param response The response to send.
	 */
	void speak(Response response) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(response);
		} catch (IOException ex) {
			String errorMsg = "Error sending response to client request."
					+ "\nConnection: " + sock.hashCode();
			this.server.getLogger().log(Level.INFO, errorMsg, ex);
		}
	}

}
