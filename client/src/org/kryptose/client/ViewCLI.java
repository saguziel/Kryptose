package org.kryptose.client;
import java.util.Scanner;

public class ViewCLI extends View {
	
	Controller ctrl;

	public ViewCLI(Controller c) {
		this.ctrl = c;
	}
	
	private void awaitInput() {
		
	}
	
	void promptUsername() { 
		// TODO Auto-generated method stub
		
	}
	
	@Override
	void promptPassword() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void promptCmd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	void logout() {
		// TODO Auto-generated method stub
		
	}
	
	public class awaitInput implements Runnable {

	    public void run() {
	        System.out.println("I'm awaiting input");
	        Scanner in = new Scanner( System.in );
	        String cmd = in.nextLine();
	        in.close();
	        ctrl.handleRequest(cmd);
	    }

	}

}
