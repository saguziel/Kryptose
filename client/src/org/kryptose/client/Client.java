package org.kryptose.client;
import org.kryptose.requests.User;

public class Client {
	
	private static final Object singletonLock = new Object();
	private static Client client;

	private String masterpass;
    private String derivedFilePass;
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

    public void setVal(String dom, String newVal){
        Boolean succ = this.passfile.setVal(dom, newVal);
        if(succ){
            view.promptCmd("password for key: "+ dom + " successfully set");
        } else {
            view.promptCmd("key: "+ dom + " is not valid");
        }
    }
    public void delVal(String dom){
        Boolean succ = this.passfile.delVal(dom);
        if(succ){
            view.promptCmd("password for key: "+ dom + " successfully set");
        } else {
            view.promptCmd("there is no password associated with key: "+ dom);
        }
    }

    public void badMasterPass() {
        view.promptCmd("Invalid login credentials");
    }

    public void getCredential(String key) {
        String password = passfile.getVal(key);
        if (password == null)
            view.promptCmd("No password associated with domain");
        view.promptCmd("Password for domain " + key + " is: " + password);

    }

    public Boolean hasPassFile(){
        return passfile != null;
    }

    public void logout() {
        view.logout();
    }

    public void setUsername(String name){
        this.user = new User(name);
        System.out.println("got username "+name);
        view.promptCmd();
    }

    void setMasterpass(String pass){
        this.masterpass = pass;
    }
    String getMasterpass() {
        return masterpass;
    }
    String getFilepass() {
        return derivedFilePass;
    }


	public void start() {
        view.promptUserName();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Client client = Client.getInstance();
		ClientController ctrl = new ClientController(client);
		View view = new ViewCLI(ctrl);
		client.view = view;
		client.start();
	}

}
