package org.kryptose.client;
import org.kryptose.requests.User;

public class Client {
	
	private static final Object singletonLock = new Object();
	private static Client client;

//    private String username;
//	private String masterpass;
    User user;
	View view;
    RequestHandler reqHandler;
    PasswordFile passfile;

    public Client() {
        this.reqHandler = new RequestHandler();
    }

	private static Client getInstance() {
        if (client != null) return client;
        synchronized (singletonLock) {
            if (client != null) return client;
            client = new Client();
            return client;
        }
    }

    public void setPassfile(PasswordFile pf){
        this.passfile = pf;
    }

    public void getCredential(String key) {
        String password = passfile.getVal(key);
        if (password == null)
            view.displayKeyError();
        view.displayPassword(password);

    }

    public Boolean hasPassFile(){
        return passfile != null;
    }


    public void setUsername(String name){
//        username = name;
        this.user = new User(name);
        view.promptCmd();
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
		ClientController ctrl = new ClientController(client);
		View view = new ViewCLI(ctrl);
		client.view = view;
		client.start();
	}

}
