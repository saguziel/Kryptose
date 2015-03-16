package org.kryptose.client;

public class Client {
	
	private static final Object singletonLock = new Object();
	private static Client client;
	
	private String masterpass;
	View view;

	private static Client getInstance() {
		if (client != null) return client;
		synchronized (singletonLock) {
			if (client != null) return client;
			client = new Client();
			return client;
		}
	}
	
	public void start() {
        view.promptUserName();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

        System.out.println("f");

		Client client = Client.getInstance();
		Controller ctrl = new ClientController(client);
		View view = new ViewCLI(ctrl);
		client.view = view;
		client.start();
	}

}
