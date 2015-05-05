package org.kryptose.client;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
	
	private static Logger kryptoseClientMainLogger = Logger.getLogger("org.kryptose.client");

	public static void main(String[] args) {
		
		// For debugging purposes.
		if (args.length >= 1 && args[0].equals("--debug")) {

			
			Handler handler = new ConsoleHandler();
			handler.setLevel(Level.FINE);
			
			kryptoseClientMainLogger.setLevel(Level.FINE);
			kryptoseClientMainLogger.addHandler(handler);
		}
		
		new Controller().start();
	}

}
