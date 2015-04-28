package org.kryptose.client;

import org.kryptose.requests.KeyDerivator;
import org.kryptose.requests.Log;
import org.kryptose.requests.User;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Properties;

public class Client {
	
    private static final String PROPERTIES_FILE = "clientProperties.xml";
    Properties properties;
    User user;
	OldView oldView;
    RequestHandler reqHandler;
    PasswordFile passfile;
    LocalDateTime lastmod;
//    private byte[] derivedFilePass;
    String username;
    ArrayList<Log> userlog;
    private String masterpass = "0";

    private Client() {
    	
        this.properties = new Properties();

        //SETTING DEFAULT CONFIGURATIONS (can be overriden by the Client settings file)
        properties.setProperty("SERVER_PORT_NUMBER", "5002");
        properties.setProperty("CLIENT_KEY_STORE_FILE", "ClientTrustStore.jks");
        properties.setProperty("CLIENT_KEY_STORE_PASSWORD", "aaaaaa");
        properties.setProperty("SERVER_HOSTNAME", "127.0.0.1");
        properties.setProperty("APPLICATION_SALT", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        properties.setProperty("MAX_USERNAME_LENGTH", "40");
        
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
                properties.storeToXML(out, "Client Configuration File");
                out.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                //Give up.
                e1.printStackTrace();
            }
            
        }

        KeyDerivator.setParams(properties.getProperty("APPLICATION_SALT"), Integer.parseInt(properties.getProperty("MAX_USERNAME_LENGTH")));

    	
        this.reqHandler = new RequestHandler(properties.getProperty("SERVER_HOSTNAME"),Integer.parseInt(properties.getProperty("SERVER_PORT_NUMBER")), properties.getProperty("CLIENT_KEY_STORE_FILE"), properties.getProperty("CLIENT_KEY_STORE_PASSWORD") );
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Client client = new Client();
        ClientController ctrl = new ClientController(client);
        OldView oldView = new OldViewCLI(ctrl);
        client.oldView = oldView;
        client.start();
    }

    public void setPassfile(PasswordFile pf) {
        this.passfile = pf;
        oldView.promptCmd("Password file successfully fetched");
    }

    public void setVal(String dom, String user, String pass) {
        Boolean succ = this.passfile.setVal(dom, user, pass);
        System.out.println(dom);
        System.out.println(user);
        System.out.println(pass);
        lastmod = LocalDateTime.now();
        if(succ){
            oldView.displayMessage("password for domain: " + dom + ", user: " + user + " successfully set");
        } else {
            oldView.displayMessage("password for domain: " + dom + ", user: " + user + " successfully created");
        }
    }
    public void set() {
        oldView.set();
    }

    public void getCredentialNum(String num) {
        int index = -1;
        try{
            index = Integer.parseInt(num);
        } catch (NumberFormatException e) {
            oldView.promptCmd(num + " is not valid index");
            return;
        }
        index--;
        Credential succ = this.passfile.getVal(index);
        if (succ == null)
            oldView.promptCmd("No password associated with index: " + num);
        else
            oldView.promptCmd("Password for domain:" + succ.getDomain() + ", username:" + succ.getUsername() + " is: " + succ.getPassword());
    }

    public void delValNum(String num) {
        int index = -1;
        try{
            index = Integer.parseInt(num);
        } catch (NumberFormatException e) {
            oldView.displayMessage(num + " is not valid index");
            return;
        }
        index--;
        Credential succ = this.passfile.delVal(index);
        if (succ != null) {
            oldView.displayMessage("password for domain: " + succ.getDomain() + ", username: " + succ.getUsername() + " successfully deleted");
        } else {
            oldView.displayMessage("No password associated with index: " + num);
        }
    }

    public void delVal(String dom, String user) {
        Boolean succ = this.passfile.delVal(dom, user);
        if (succ) {
            oldView.displayMessage("password for domain: " + dom + ", username: " + user + " successfully deleted");
        } else {
            oldView.displayMessage("there is no password associated with domain: " + dom + ", username :" + user);
        }
    }

    public void continuePrompt(String s) {
        oldView.promptCmd(s);
    }

    public void badMasterPass() {
        oldView.promptCmd("Invalid login credentials");
    }

    public void printAll() {
        int i = 0;
        for (Credential c : passfile.credentials) {
            System.out.println(
                    ++i + ": Domain: " + c.getDomain() +
                    " Username: " + c.getUsername() +
//                    " Password: " + c.getPassword() +
                    " Lastmod: " + c.getMod().toString());
        }
        oldView.promptCmd();
    }



    public void getCredential(String dom, String user) {
        String password = passfile.getVal(dom, user);
        if (password == null)
            oldView.promptCmd("No password associated with domain: " + dom + ", username :" + user);
        else
            oldView.promptCmd("Password for domain: " + dom + ", username :" + user + " is: " + password);
    }

    public LocalDateTime getLastMod() {
        return lastmod;
    }

    public Boolean hasPassFile() {
        return passfile != null;
    }

    public void logout() {
//        oldView.logout();
        oldView.displayMessage("Logging out!");
        this.start();
    }

    public boolean setUsername(String name) {
        if (!User.isValidUsername(name)) {
            oldView.displayMessage(User.VALID_USERNAME_DOC);
            return false;
        } else {
            this.username = name;
            oldView.displayMessage("got username " + name);
            return true;
        }
    }

    public void newPassFile() {
        this.passfile = new PasswordFile(this.user.getUsername());
        oldView.promptCmd("New password file created");
    }

    void setLogs(ArrayList<Log> a) {
        userlog = a;
        System.out.println(userlog);
    }

    void displayLogs() {
        for (Log l : userlog) {
            oldView.displayMessage(l.toString());
        }
        oldView.promptCmd();
    }

    String getMasterpass() {
        return masterpass;
    }

    public void setMasterpass(String pass) {
        this.masterpass = pass;
        byte[] derived = KeyDerivator.getAuthenticationKeyBytes(this.username, pass.toCharArray());
        this.user = new User(username, derived);
    }

    void promptMasterpass() {
        oldView.promptPassword();
    }

//    byte[] getFilepass() {
//        return derivedFilePass;
//    }

    void startLogin(){
        oldView.promptUserName();
    }

    public void start() {
        oldView.displayMessage("Welcome to Kryptose BETA VERSION. Type LOGIN or CREATE to create a new account");
        oldView.promptStart();
    }

    public void startCreate() {
        oldView.createUsername();
    }

    public void startSetPass() {
        oldView.createPass();
    }

    public void start(String s) {
        oldView.displayMessage(s);
        oldView.promptStart();
    }

    public void restartLogin() {
        oldView.displayMessage("Invalid user name or password, please try again");
        oldView.promptUserName();
    }

}
