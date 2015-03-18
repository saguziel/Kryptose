package org.kryptose.server;

import org.kryptose.requests.Request;
import org.kryptose.requests.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Runs a thread that listens for requests from a client, and sends back responses when available.
 *
 */
class ClientHandler implements Runnable {

	private Server server;
	private Socket sock;

	ClientHandler(Server server, Socket clientSocket) {
		this.server = server;
		this.sock = clientSocket;
	}

	public void run() {
		try {
			// Listen for request.
			Request request = listen();

			// Process request.
			Response resp = this.server.handleRequest(request);

			// Send back the response.
			if (resp != null) speak(resp);
		}
		finally {
			try {
				if (!sock.isClosed()) sock.close();
			} catch (IOException e) {
				// Give up.
				// TODO: log this error?
				e.printStackTrace();
			}
		}

	}

	/**
	 * Listen for a request from the client.
	 * @return The request heard, or {@literal null} if the connection is no longer valid.
	 */
	private Request listen() {
		ObjectInputStream in=null;
		try {
			in = new ObjectInputStream(sock.getInputStream());
			return (Request)in.readObject();
		} catch (IOException ex) {
        	//TODO: This is probably an SSL Error. How do we handle it?
			ex.printStackTrace();
			return null;
		} catch (ClassNotFoundException | ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}

	/**
	 * Sends a response back to the client.
	 * @param response The response to send.
	 */
	private void speak(Response response) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(response);
		} catch (IOException ex) {
        	//TODO: This is probably an SSL Error. How do we handle it?
			ex.printStackTrace();
		}
	}

}
