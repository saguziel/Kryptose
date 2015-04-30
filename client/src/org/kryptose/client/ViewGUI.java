package org.kryptose.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
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
	
	// pixels
	private static final int GAP = 4;
	

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
		void saveForm() {
			model.setFormText(form, textField.getText());
		}
		@Override
		public void focusGained(FocusEvent ev) {
			// do nothing
		}
		@Override
		public void focusLost(FocusEvent ev) {
			this.saveForm();
		}
		@Override
		public void actionPerformed(ActionEvent ev) {
			this.saveForm();
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
		void saveForm() {
			model.setFormPassword(form, passwordField.getPassword());
		}
		@Override
		public void focusGained(FocusEvent ev) {
			// do nothing
		}
		@Override
		public void focusLost(FocusEvent ev) {
			this.saveForm();
		}
		@Override
		public void actionPerformed(ActionEvent ev) {
			this.saveForm();
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
	
	private JDialog createAccountDialog;
	
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
	
	private Action createAccountDialogAction = new AbstractAction("Create New Account") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.CREATE_ACCOUNT);
		}
	};
	private Action cancelCreateAccountAction = new AbstractAction("Cancel") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.LOGIN);
		}
	};
	
	
	private WindowListener windowCloseHandler = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent arg0) {
			control.exit();
		}
	};
	
	/**
	 * Using the GridBagLayout
	 * @param domainListener Set to null to omit domain textfield.
	 * @param usernameListener
	 * @param passwordListener
	 */
	private static void addCredentialFields(Container cont,
			TextFieldListener domainListener,
			TextFieldListener usernameListener,
			PasswordFieldListener passwordListener) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(GAP, GAP/2, GAP, GAP/2);
		
		if (domainListener != null) {
			JLabel domainLabel = new JLabel("Domain: ");
			JTextField domainField = new JTextField(18);
			domainLabel.setLabelFor(domainField);
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridwidth = GridBagConstraints.RELATIVE;
			cont.add(domainLabel,gbc);
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			cont.add(domainField,gbc);
			domainListener.bindTo(domainField);
		}
		
		JLabel usernameLabel = new JLabel("Username: ");
		JTextField usernameField = new JTextField(18);
		usernameLabel.setLabelFor(usernameField);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		cont.add(usernameLabel,gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		cont.add(usernameField,gbc);
		usernameListener.bindTo(usernameField);
		
		JLabel passwordLabel = new JLabel("Password: ");
		JPasswordField passwordField = new JPasswordField(18);
		passwordLabel.setLabelFor(passwordField);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		cont.add(passwordLabel,gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		cont.add(passwordField,gbc);
		passwordListener.bindTo(passwordField);
		
	}
	
	private JPanel createLoginPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel loginFormPanel = new JPanel(new BorderLayout());
		loginFormPanel.setBorder(BorderFactory.createEmptyBorder(
				GAP, 2*GAP, 2*GAP, GAP/2));

		// TODO: add form status validation notification thingy.
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		TextFieldListener tfl = new TextFieldListener(TextForm.LOGIN_MASTER_USERNAME, null);
		this.textFieldListeners.add(tfl);
		PasswordFieldListener pfl = new PasswordFieldListener(PasswordForm.LOGIN_MASTER_PASSWORD, logInAction);
		this.passwordFieldListeners.add(pfl);
		ViewGUI.addCredentialFields(fieldsPanel, null, tfl, pfl);
		
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
		JButton createButton = new JButton(createAccountDialogAction);
		createButtonPanel.add(createButton);
		
		createAccountPanel.add(logoLabel, BorderLayout.CENTER);
		createAccountPanel.add(createButtonPanel, BorderLayout.SOUTH);
		separatorPanel.add(createAccountPanel, BorderLayout.CENTER);
		
		panel.add(loginFormPanel, BorderLayout.WEST);
		panel.add(separatorPanel, BorderLayout.EAST);

		return panel;
	}
	
	private JPanel createCreateAccountPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		TextFieldListener tfl = new TextFieldListener(TextForm.CREATE_MASTER_USERNAME, null);
		this.textFieldListeners.add(tfl);
		PasswordFieldListener pfl = new PasswordFieldListener(PasswordForm.CREATE_MASTER_PASSWORD, logInAction);
		this.passwordFieldListeners.add(pfl);
		ViewGUI.addCredentialFields(panel, null, tfl, pfl);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(GAP, GAP/2, GAP, GAP/2);
		gbc.anchor = GridBagConstraints.EAST;
		
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JButton(this.cancelCreateAccountAction));
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(new JButton(this.createAccountAction));
		
		return panel;
	}
	
	public ViewGUI(Model model, Controller control) {
		this.model = model;
		this.control = control;
		
		this.loginFrame = new JFrame("Kryptose\u2122 Password Management System");
		this.loginFrame.addWindowListener(windowCloseHandler);
		this.loginFrame.setContentPane(createLoginPanel());
		this.loginFrame.pack();
		this.loginFrame.setResizable(false);
		this.loginFrame.setLocationRelativeTo(null);
		
		this.createAccountDialog = new JDialog(loginFrame, "New Account Creation", ModalityType.APPLICATION_MODAL);
		this.createAccountDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				for (TextFieldListener textFieldListener : textFieldListeners) {
					textFieldListener.saveForm();
				}
				for (PasswordFieldListener passwordFieldListener : passwordFieldListeners) {
					passwordFieldListener.saveForm();
				}
				String command = (String) cancelCreateAccountAction.getValue(Action.NAME);
				ActionEvent aev = new ActionEvent(createAccountDialog, ActionEvent.ACTION_PERFORMED, command);
				cancelCreateAccountAction.actionPerformed(aev);
			}
		});
		this.createAccountDialog.setContentPane(createCreateAccountPanel());
		this.createAccountDialog.pack();
		this.createAccountDialog.setResizable(false);
		this.createAccountDialog.setLocationRelativeTo(loginFrame);
		
		this.hoverFrame = new JFrame("Kryptose\u2122");
		this.hoverFrame.addWindowListener(windowCloseHandler);
		//this.hoverFrame.setOpacity(0.4f); // TODO hoverframe opacity
		//TODO this.hoverFrame.setBounds(x, y, width, height);
	}
	
	@Override
	public void updatePasswordFile() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void updateLogs() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void updateLastMod() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void updateMasterCredentials() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
		});
	}

	@Override
	public void updateServerException() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				handleServerException();
			}
		});
	}
	private void handleServerException() {
		Exception ex = model.getLastServerException();
		if (ex instanceof RecoverableException) {
			final String msg = ex.getMessage();
			final String title = "Error";
			final Window parent = this.getCurrentActiveWindow();
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				handleSyncStatus();
			}
		}); 
	}
	
	private void handleSyncStatus() {
		boolean waitingOnServer = this.model.isWaitingOnServer();
		ViewState state = this.model.getViewState();

		Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		Cursor rightCursor = waitingOnServer ? waitCursor : normalCursor;

		Action[] actionsToToggle = new Action[] {
				logInAction, createAccountAction, logOutAction,
				createAccountDialogAction, cancelCreateAccountAction
				}; 
		
		for (Action action : actionsToToggle) {
			action.setEnabled(!waitingOnServer);
		}
		
		switch (state) {
		case LOGIN:
			loginFrame.setCursor(rightCursor);
			break;
		case CREATE_ACCOUNT:
			createAccountDialog.setCursor(rightCursor);
			break;
		case WAITING:
			break;
		default:
			this.logger.log(Level.SEVERE, "Unexpected view state in model", state);
		}
		
	}

	@Override
	public void updateTextForm(TextForm form) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (TextFieldListener tfl : textFieldListeners) {
					tfl.updateTextForm(form);
				}
			}
		});
	}

	@Override
	public void updatePasswordForm(PasswordForm form) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (PasswordFieldListener pfl : passwordFieldListeners) {
					pfl.updatePasswordForm(form);
				}
			}
		});
	}

	@Override
	public void updateSelection(Selection selection) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
		});
	}
	
	private Window getCurrentActiveWindow() {
		ViewState state = this.model.getViewState();
		switch (state) {
		case LOGIN:
			return this.loginFrame;
		case CREATE_ACCOUNT:
			return this.createAccountDialog;
		case WAITING:
			return this.hoverFrame;
		default:
			this.logger.log(Level.SEVERE, "Unexpected view state in model", state);
			return null;
		}
	}

	@Override
	public void updateViewState() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				handleViewState();
			}
		});
	}
	private void handleViewState() {
		ViewState state = this.model.getViewState();
		switch (state) {
		case LOGIN:
			this.hoverFrame.setVisible(false);
			this.loginFrame.setVisible(true);
			this.createAccountDialog.setVisible(false);
			break;
		case CREATE_ACCOUNT:
			this.hoverFrame.setVisible(false);
			this.loginFrame.setVisible(true);
			this.createAccountDialog.setVisible(true);
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
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				hoverFrame.dispose();
				loginFrame.dispose();
			}
		});
	}

}
