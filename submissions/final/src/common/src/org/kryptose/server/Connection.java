package org.kryptose.server;


import java.util.Random;
import javax.net.ssl.SSLSocket;

public class Connection {
	
	private static Random random = new Random();
	
	private int id;
	private SSLSocket sock;
	private long time;
	
	
	public Connection(SSLSocket sock) {
		super();
		this.id = random.nextInt();
		this.sock = sock;
		this.time = System.currentTimeMillis();
	}
	
	
	public int getId() {
		return id;
	}
	public long getTime() {
		return time;
	}

	public SSLSocket getSocket() {
		return this.sock;
	}
	
}
