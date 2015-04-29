package org.kryptose.client;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.Selection;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;

public class ViewGUI implements View {

	private Model model;
	private Controller control;
	private Logger logger;

	private JFrame hoverFrame;
	private JFrame loginFrame;
	
	private Action logInAction = new AbstractAction("Log in") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.login();
		}
	};
	private Action logOutAction = new AbstractAction("Log out") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.logout();
		}
	};
	
	private WindowStateListener windowCloseHandler = new WindowStateListener() {
		@Override
		public void windowStateChanged(WindowEvent ev) {
			if (ev.getNewState() == WindowEvent.WINDOW_CLOSING) {
				ViewGUI.this.control.exit();
			}
		}
	};
	
	public ViewGUI(Model model, Controller control) {
		this.model = model;
		this.control = control;
		
		this.loginFrame = new JFrame("Kryptose\u2122 Password Management System");
		this.loginFrame.addWindowStateListener(windowCloseHandler);
		
		this.hoverFrame = new JFrame("Kryptose\u2122");
		this.hoverFrame.addWindowStateListener(windowCloseHandler);
		this.hoverFrame.setOpacity(0.4f); // TODO hoverframe opacity
		//TODO this.hoverFrame.setBounds(x, y, width, height);
	}
	
	@Override
	public void updatePasswordFile() {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateLogs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateLastMod() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMasterCredentials() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateServerException() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateSyncStatus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTextForm(TextForm form) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePasswordForm(PasswordForm form) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateSelection(Selection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateViewState() {
		ViewState state = this.model.getViewState();
		switch (state) {
		case LOGIN:
			// TODO
			break;
		default:
			this.logger.log(Level.SEVERE, "Unexpected view state in model", state);
		}
	}

}
