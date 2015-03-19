package org.kryptose.client;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.kryptose.requests.User;
import java.util.Date;
import java.time.LocalDateTime;

public class Client {
	
    private static final String PROPERTIES_FILE = "clientProperties.xml";
    Properties properties;
    

	private String masterpass = "0";
    private String derivedFilePass = "0";
    User user;
	View view;


    RequestHandler reqHandler;
    PasswordFile passfile;
    LocalDateTime lastmod;

    private Client() {
    	
        this.properties = new Properties();

        //SETTING DEFAULT CONFIGURATIONS (can be overriden by the Client settings file)
        properties.setProperty("SERVER_PORT_NUMBER", "5002");
        properties.setProperty("CLIENT_KEY_STORE_FILE", "src/org/kryptose/certificates/ClientTrustStore.jks");
        properties.setProperty("CLIENT_KEY_STORE_PASSWORD", "aaaaaa");
        properties.setProperty("SERVER_HOSTNAME", "127.0.0.1");
        
        //LOADIG CUSTOM CONFIGURATION FROM FILE.
        FileInputStream in;
        try {
            in = new FileInputStream(PROPERTIES_FILE);
            Properties XMLProperties = new Properties();
            XMLProperties.loadFromXML(in);
            this.properties.putAll(XMLProperties);
            in.close();
        } catch (IOException e) {
        	//TODO: Unable to read the properties file. Maybe log the error?

            try {
                FileOutputStream out = new FileOutputStream(PROPERTIES_FILE);
                properties.storeToXML(out, "Server Configuration File");
                out.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                //Give up.
                e1.printStackTrace();
            }
        }

    	
        this.reqHandler = new RequestHandler(properties.getProperty("SERVER_HOSTNAME"),Integer.parseInt(properties.getProperty("SERVER_PORT_NUMBER")), properties.getProperty("CLIENT_KEY_STORE_FILE"), properties.getProperty("CLIENT_KEY_STORE_PASSWORD") );
    }
    
    public void setPassfile(PasswordFile pf){
        this.passfile = pf;
        view.promptCmd("Password file successfully fetched");
    }

    public void setVal(String dom, String newVal){
        Boolean succ = this.passfile.setVal(dom, newVal);
        lastmod = LocalDateTime.now();
        if(succ){
            view.displayMessage("password for key: " + dom + " successfully set");
        } else {
            view.displayMessage("password for key: " + dom + " successfully created");
        }
    }

    public void delVal(String dom){
        Boolean succ = this.passfile.delVal(dom);
        if(succ){
            view.displayMessage("password for key: " + dom + " successfully set");
        } else {
            view.displayMessage("there is no password associated with key: "+ dom);
        }
    }

    public void continuePrompt(String s) {
        view.promptCmd(s);
    }

    public void badMasterPass() {
        view.promptCmd("Invalid login credentials");
    }

    public void printAll() {
        for(Credential c : passfile.credentials) {
            System.out.println("Domain: "+c.getDomain()+" Password: "+c.getPassword());
        }
        view.promptCmd();
    }

    public void getCredential(String key) {
        String password = passfile.getVal(key);
        if (password == null)
            view.promptCmd("No password associated with key: "+key);
        else
            view.promptCmd("Password for domain " + key + " is: " + password);
    }

    public LocalDateTime getLastMod() {
        return lastmod;
    }

    public Boolean hasPassFile(){
        return passfile != null;
    }

    public void logout() {
        view.logout();
    }

    public void setUsername(String name){
        this.user = new User(name, new byte[48]); // TODO: set passkey
        view.displayMessage("got username " +name);
    }

    public void newPassFile(){
        this.passfile = new PasswordFile(this.user.getUsername());
        view.promptCmd("New password file created");
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
		Client client = new Client();
		ClientController ctrl = new ClientController(client);
		View view = new ViewCLI(ctrl);
		client.view = view;
		client.start();
	}



}
