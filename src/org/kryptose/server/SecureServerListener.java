package org.kryptose.server;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;



class SecureServerListener{
	
    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        ObjectOutputStream out;
        Socket sock;
        
        public ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                in = new ObjectInputStream(sock.getInputStream());
                //TO-DO: this is a blocking call (if the stream is empty). Provide a timeout for threads, or someone can just spawn new threads
                //and exhaust resources
                out = new ObjectOutputStream(sock.getOutputStream());                
            } catch (Exception ex) { ex.printStackTrace(); }
        }
        
        public void run() {
            Object o1;
            System.out.println("Connection thread running");
            try {
                o1 = in.readObject();
                
                System.out.println("Received one request: " + o1.toString());
                out.writeObject(new Request("Server got and accepted the request " + o1.toString()));
                //TO-DO: What do we do with the object received? I would think of generating an event "RequestReceived"
                //so that other classes could listen to/wait for it connect to it.

                in.close();
                out.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

	
	SecureServerListener(int port){
//	    ServerSocketFactory ssocketFactory = SSLServerSocketFactory.getDefault();
	    try {
//		    ServerSocket serverListener = ssocketFactory.createServerSocket(port);
	    	ServerSocket serverListener = new ServerSocket(port);
            while(true) {
        	    Socket clientSocket = serverListener.accept();
                Runnable job = new ClientHandler(clientSocket);
        	    Thread t = new Thread(job);
//                System.out.println("Thread created");
                t.start();
                System.out.println("got a connection");
            }
//            serverListener.close();
        } catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public static void main(String[] args) {
		System.out.println("Server is running");
		SecureServerListener listener = new SecureServerListener(5002);
	}

	
}

