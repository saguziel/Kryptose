package org.kryptose.client;
import java.util.Scanner;

public class ViewCLI extends View {

    final static int CMD = 0;
    final static int USERNAME = 1;
	
	ClientController ctrl;

	public ViewCLI(ClientController c) {
		this.ctrl = c;
	}
	
	private void awaitInput(int cmd) {
        (new InputThread(cmd)).start();
	}
	
	void promptUsername() { 
		// TODO Auto-generated method stub
		
	}

    @Override
    void promptUserName() {
        System.out.println("Enter user name");
        awaitInput(USERNAME);

    }
	
	@Override
	void promptPassword() {
		System.out.println("Enter master password");
		
	}

	@Override
	void promptCmd() {
        System.out.println("Enter command");
        awaitInput(CMD);
	}

	@Override
	void logout() {
		// TODO Auto-generated method stub
		
	}
	
	public class InputThread extends Thread {

        int cmd;
        public InputThread(int cmd){
            this.cmd = cmd;
        }

	    public void run() {
	        System.out.println("I'm awaiting input");
	        Scanner in = new Scanner( System.in );
	        String input = in.nextLine();
	        in.close();

            if (cmd == CMD)
                ctrl.handleRequest(input);
            if(cmd == USERNAME)
	            ctrl.handleUserName(input);
	    }

	}

}
