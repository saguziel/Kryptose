
package org.kryptose.client;




public abstract class View {

    abstract void promptUserName();

	abstract void promptPassword();

    abstract void promptCmd();

	abstract void promptCmd(String s);

	abstract void logout();

    abstract void promptStart();

    abstract void displayMessage(String s);

    abstract void createUsername();

    abstract void createPass();

    abstract void set();

}
