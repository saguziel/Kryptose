package org.kryptose.client;

public abstract class View {

    abstract void promptUserName();
	abstract void promptPassword();
	abstract void promptCmd();
    abstract void displayPassError();
    abstract void displayKeyError();
    abstract void displayPassword(String p);
	abstract void logout();
	

}
