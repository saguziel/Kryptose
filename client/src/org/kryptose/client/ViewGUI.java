package org.kryptose.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.Selection;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;
import org.kryptose.exceptions.RecoverableException;

public class ViewGUI implements View {

	private class TextFieldListener implements ActionListener, FocusListener {
		private JTextField textField;
		private Action action;
		private TextForm form;
		TextFieldListener(TextForm form, Action action) {
			this.form = form;
			this.action = action;
		}
		void bindTo(JTextField textField) {
			this.textField = textField;
			this.textField.addActionListener(this);
			this.textField.addFocusListener(this);
		}
		void updateTextForm(TextForm form) {
			if (this.form == form) {
				String value = model.getFormText(form);
				this.textField.setText(value);
			}
		}
		@Override
		public void focusGained(FocusEvent ev) {
			// do nothing
		}
		@Override
		public void focusLost(FocusEvent ev) {
			model.setFormText(form, textField.getText());
		}
		@Override
		public void actionPerformed(ActionEvent ev) {
			textField.transferFocus();
			if (action != null) {
				action.actionPerformed(ev);
			}
		}
	}
	private class PasswordFieldListener implements ActionListener, FocusListener {
		private JPasswordField passwordField;
		private Action action;
		private PasswordForm form;
		PasswordFieldListener(PasswordForm form, Action action) {
			this.form = form;
			this.action = action;
		}
		void bindTo(JPasswordField passwordField) {
			this.passwordField = passwordField;
			this.passwordField.addActionListener(this);
			this.passwordField.addFocusListener(this);
		}
		void updatePasswordForm(PasswordForm form) {
			if (this.form == form) {
				char[] value = model.getFormPasswordClone(form);
				if (value == null) this.passwordField.setText("");
				else Arrays.fill(value, ' ');
			}
		}
		@Override
		public void focusGained(FocusEvent ev) {
			// do nothing
		}
		@Override
		public void focusLost(FocusEvent ev) {
			model.setFormPassword(form, passwordField.getPassword());
		}
		@Override
		public void actionPerformed(ActionEvent ev) {
			passwordField.transferFocus();
			if (action != null) {
				action.actionPerformed(ev);
			}
		}
	}
	
	private Model model;
	private Controller control;
	private Logger logger = Logger.getLogger("org.kryptose.client.ViewGUI");

	private JFrame hoverFrame;
	private JFrame loginFrame;
	
	private List<TextFieldListener> textFieldListeners = new ArrayList<TextFieldListener>();
	private List<PasswordFieldListener> passwordFieldListeners = new ArrayList<PasswordFieldListener>();
	
	private Action logInAction = new AbstractAction("Log in") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.login();
		}
	};
	private Action createAccountAction = new AbstractAction("Create New Account") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.createAccount();
		}
	};
	private Action logOutAction = new AbstractAction("Log out") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.logout();
		}
	};
	
	private WindowListener windowCloseHandler = new WindowListener() {
		@Override
		public void windowActivated(WindowEvent arg0) {
			// do nothing
		}
		@Override
		public void windowClosed(WindowEvent arg0) {
			// do nothing
		}
		@Override
		public void windowClosing(WindowEvent arg0) {
			control.exit();
		}
		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// do nothing
		}
		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// do nothing
		}
		@Override
		public void windowIconified(WindowEvent arg0) {
			// do nothing
		}
		@Override
		public void windowOpened(WindowEvent arg0) {
			// do nothing
		}
	};
	
	private JPanel getLoginPanel() {
		int GAP = 4;
		
		JPanel panel = new JPanel(new BorderLayout());

		JPanel loginFormPanel = new JPanel(new BorderLayout());
		loginFormPanel.setBorder(BorderFactory.createEmptyBorder(
				GAP, 2*GAP, 2*GAP, GAP/2));

		// TODO: add form status validation notification thingy.
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		JLabel usernameLabel = new JLabel("Username: ");
		JTextField usernameField = new JTextField(18);
		usernameLabel.setLabelFor(usernameField);
		JLabel passwordLabel = new JLabel("Password: ");
		JPasswordField passwordField = new JPasswordField(18);
		passwordLabel.setLabelFor(passwordField);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(GAP, GAP/2, GAP, GAP/2);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		fieldsPanel.add(usernameLabel,gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		fieldsPanel.add(usernameField,gbc);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		fieldsPanel.add(passwordLabel,gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		fieldsPanel.add(passwordField,gbc);
		
		TextFieldListener tfl = new TextFieldListener(TextForm.MASTER_USERNAME, null);
		this.textFieldListeners.add(tfl);
		tfl.bindTo(usernameField);
		PasswordFieldListener pfl = new PasswordFieldListener(PasswordForm.MASTER_PASSWORD, logInAction);
		this.passwordFieldListeners.add(pfl);
		pfl.bindTo(passwordField);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton(logInAction);
		buttonPanel.add(button);
		
		loginFormPanel.add(fieldsPanel, BorderLayout.CENTER);
		loginFormPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		JPanel separatorPanel = new JPanel(new BorderLayout());
		separatorPanel.setBorder(BorderFactory.createEmptyBorder(
				GAP, GAP, GAP, GAP));
		separatorPanel.add(new JSeparator(JSeparator.VERTICAL),
				BorderLayout.WEST);
		
		JPanel createAccountPanel = new JPanel(new BorderLayout());
		createAccountPanel.setBorder(BorderFactory.createEmptyBorder(
				0, GAP, GAP, GAP));
		
		URL logoURL = Client.class.getResource("resources/logo.png");
		JLabel logoLabel = new JLabel();
		if (logoURL != null) logoLabel.setIcon(new ImageIcon(logoURL));
		
		JPanel createButtonPanel = new JPanel(new FlowLayout());
		JButton createButton = new JButton(createAccountAction);
		createButtonPanel.add(createButton);
		
		createAccountPanel.add(logoLabel, BorderLayout.CENTER);
		createAccountPanel.add(createButtonPanel, BorderLayout.SOUTH);
		separatorPanel.add(createAccountPanel, BorderLayout.CENTER);
		
		panel.add(loginFormPanel, BorderLayout.WEST);
		panel.add(separatorPanel, BorderLayout.EAST);

		return panel;
	}
	
	public ViewGUI(Model model, Controller control) {
		this.model = model;
		this.control = control;
		
		this.loginFrame = new JFrame("Kryptose\u2122 Password Management System");
		this.loginFrame.addWindowListener(windowCloseHandler);
		this.loginFrame.setContentPane(getLoginPanel());
		this.loginFrame.pack();
		this.loginFrame.setResizable(false);
		
		this.hoverFrame = new JFrame("Kryptose\u2122");
		this.hoverFrame.addWindowListener(windowCloseHandler);
		//this.hoverFrame.setOpacity(0.4f); // TODO hoverframe opacity
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
		Exception ex = model.getLastServerException();
		if (ex instanceof RecoverableException) {
			final String msg = ex.getMessage();
			final String title = "Error";
			final Component parent = model.getViewState() == ViewState.LOGIN
					? loginFrame : hoverFrame;
			SwingUtilities.invokeLater( new Runnable() {
				public void run() {
					JOptionPane.showMessageDialog(
							parent, msg, title, JOptionPane.ERROR_MESSAGE);
				}
			});
		} else {
			// TODO
		}
	}

	@Override
	public void updateSyncStatus() {
		
		boolean waitingOnServer = this.model.isWaitingOnServer();
		ViewState state = this.model.getViewState();

		Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		Cursor rightCursor = waitingOnServer ? waitCursor : normalCursor;

		logInAction.setEnabled(!waitingOnServer);
		createAccountAction.setEnabled(!waitingOnServer);
		
		switch (state) {
		case LOGIN:
			loginFrame.setCursor(rightCursor);
			break;
		case WAITING:
			break;
		default:
			this.logger.log(Level.SEVERE, "Unexpected view state in model", state);
		}
		
	}

	@Override
	public void updateTextForm(TextForm form) {
		for (TextFieldListener tfl : this.textFieldListeners) {
			tfl.updateTextForm(form);
		}
	}

	@Override
	public void updatePasswordForm(PasswordForm form) {
		for (PasswordFieldListener pfl : this.passwordFieldListeners) {
			pfl.updatePasswordForm(form);
		}
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
			this.hoverFrame.setVisible(false);
			this.loginFrame.setVisible(true);
			break;
		case WAITING:
			this.hoverFrame.setVisible(true);
			this.loginFrame.setVisible(false);
			break;
		default:
			this.logger.log(Level.SEVERE, "Unexpected view state in model", state);
		}
	}

	@Override
	public void shutdown() {
		this.hoverFrame.dispose();
		this.loginFrame.dispose();
	}

}
