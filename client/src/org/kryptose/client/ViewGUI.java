package org.kryptose.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.Selection;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;
import org.kryptose.exceptions.RecoverableException;
import org.kryptose.requests.User;

public class ViewGUI implements View {
	
	private static final float HOVER_DEFAULT_OPACITY = 0.4f;
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
			if (action != null) {
				this.saveForm();
				action.actionPerformed(ev);
			} else {
				textField.transferFocus();
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
			if (action != null) {
				this.saveForm();
				action.actionPerformed(ev);
			} else {
				passwordField.transferFocus();
			}
		}
	}
	
	private final class WindowCloseHandler extends WindowAdapter {
		private Action action;
		private Window source;
		WindowCloseHandler(Action action, Window source) {
			this.action = action;
			this.source = source;
		}
		@Override
		public void windowClosing(WindowEvent ev) {
			for (TextFieldListener textFieldListener : textFieldListeners) {
				textFieldListener.saveForm();
			}
			for (PasswordFieldListener passwordFieldListener : passwordFieldListeners) {
				passwordFieldListener.saveForm();
			}
			String command = (String) action.getValue(Action.NAME);
			ActionEvent aev = new ActionEvent(source, ActionEvent.ACTION_PERFORMED, command);
			action.actionPerformed(aev);
		}
	}
	
	private Model model;
	private Controller control;
	private Logger logger = Logger.getLogger("org.kryptose.client.ViewGUI");

	private URL logo = ViewGUI.class.getResource("resources/logo.png");
	
	private JFrame hoverFrame;
	private JFrame loginFrame;
	
	private JDialog createAccountDialog;
	private JDialog manageCredentialsDialog;

	private JMenu mainMenu;
	private JMenu copyPasswordMenu;
	
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
	private Action managingDialogAction = new AbstractAction("Manage Credentials") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.MANAGING);
		}
	};
	private Action doneManagingAction = new AbstractAction("Done") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.WAITING);
		}
	};
	
	private Action minimizeAction = new AbstractAction("Minimize") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			hoverFrame.setExtendedState(JFrame.ICONIFIED);
		}
	};
	private Action moveAction = new AbstractAction("Move") {
		final Cursor moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
		final Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		@Override
		public void actionPerformed(ActionEvent ev) {
			MouseAdapter listener = new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (!hoverFrame.getCursor().equals(moveCursor)) {
						reset();
					}
				}
				@Override
				public void mouseDragged(MouseEvent e) {
					Point p = hoverFrame.getLocation();
					p.x += e.getX();
					p.y += e.getY();
					hoverFrame.setLocation(p);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					reset();
					hoverFrame.setCursor(normalCursor);
				}
				private void reset() {
					hoverFrame.removeMouseListener(this);
					mainMenu.removeMouseListener(this);
					hoverFrame.removeMouseMotionListener(this);
					mainMenu.removeMouseMotionListener(this);
					mainMenu.setEnabled(true);
				}
			};
			hoverFrame.addMouseListener(listener);
			mainMenu.addMouseListener(listener);
			hoverFrame.addMouseMotionListener(listener);
			mainMenu.addMouseMotionListener(listener);
			mainMenu.setEnabled(false);
			hoverFrame.setCursor(moveCursor);
		}
	};
	private Action exitAction = new AbstractAction("Exit") {
		@Override
		public void actionPerformed(ActionEvent ev) {
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
		usernameLabel.setToolTipText(User.VALID_USERNAME_DOC);
		usernameField.setToolTipText(User.VALID_USERNAME_DOC);
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

		// TODO: add form status validation notification thingy?
		// TODO: following label is currently not used and should be removed.
		JLabel loginValidationLabel = new JLabel("");
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		JLabel loginLabel = new JLabel("Please enter username and password:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5*GAP, GAP/2, 3*GAP, GAP/2);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.WEST;
		fieldsPanel.add(loginLabel, gbc);
		TextFieldListener tfl = new TextFieldListener(TextForm.LOGIN_MASTER_USERNAME, null);
		this.textFieldListeners.add(tfl);
		PasswordFieldListener pfl = new PasswordFieldListener(PasswordForm.LOGIN_MASTER_PASSWORD, logInAction);
		this.passwordFieldListeners.add(pfl);
		ViewGUI.addCredentialFields(fieldsPanel, null, tfl, pfl);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton button = new JButton(logInAction);
		buttonPanel.add(button);
		
		loginFormPanel.add(loginValidationLabel, BorderLayout.NORTH);
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
		
		JLabel logoLabel = new JLabel();
		if (logo != null) logoLabel.setIcon(new ImageIcon(logo));
		
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

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
		buttonPanel.add(new JButton(this.cancelCreateAccountAction));
		buttonPanel.add(new JButton(this.createAccountAction));
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(buttonPanel, gbc);
		
		return panel;
	}
	
	public ViewGUI(Model model, Controller control) {
		this.model = model;
		this.control = control;
		
		this.loginFrame = new JFrame("Kryptose\u2122 Password Management System");
		this.loginFrame.addWindowListener(new WindowCloseHandler(exitAction, loginFrame));
		this.loginFrame.setContentPane(createLoginPanel());
		this.loginFrame.pack();
		this.loginFrame.setResizable(false);
		this.loginFrame.setLocationRelativeTo(null);
		
		this.createAccountDialog = new JDialog(loginFrame, "New Account Creation", ModalityType.APPLICATION_MODAL);
		this.createAccountDialog.addWindowListener(
				new WindowCloseHandler(cancelCreateAccountAction, createAccountDialog));
		this.createAccountDialog.setContentPane(createCreateAccountPanel());
		this.createAccountDialog.pack();
		this.createAccountDialog.setResizable(false);
		this.createAccountDialog.setLocationRelativeTo(loginFrame);
		
		this.hoverFrame = new JFrame("Kryptose\u2122");
		this.hoverFrame.addWindowListener(new WindowCloseHandler(exitAction, hoverFrame));
		
		this.copyPasswordMenu = new JMenu("Copy password");
		
		JMenuBar menuBar = new JMenuBar();
		mainMenu = new JMenu("Kryptose");
		this.hoverFrame.setUndecorated(true);
		this.hoverFrame.setAlwaysOnTop(true);
		mainMenu.add(this.copyPasswordMenu);
		mainMenu.add(new JMenuItem(this.managingDialogAction));
		mainMenu.addSeparator();
		mainMenu.add(new JMenuItem(this.minimizeAction));
		mainMenu.add(new JMenuItem(this.moveAction));
		mainMenu.add(new JMenuItem(this.logOutAction));
		mainMenu.add(new JMenuItem(this.exitAction));
		menuBar.add(mainMenu);
		this.hoverFrame.setJMenuBar(menuBar);
		
		this.hoverFrame.pack();
		this.hoverFrame.setResizable(false);
		this.hoverFrame.setOpacity(HOVER_DEFAULT_OPACITY);
		
		class OpacityAdjuster extends MouseAdapter implements MenuListener {
			@Override
			public void mouseEntered(MouseEvent e) {
				hoverFrame.setOpacity(1f);
			}
			private void resetMaybe(Point p) {
				Rectangle bounds = hoverFrame.getBounds();
				if ((p == null || !bounds.contains(p))
						&& !mainMenu.isPopupMenuVisible()) {
					hoverFrame.setOpacity(HOVER_DEFAULT_OPACITY);
				}
			}
			@Override
			public void mouseExited(MouseEvent e) {
				this.resetMaybe(e.getPoint());
			}
			@Override
			public void menuCanceled(MenuEvent e) {
				this.resetMaybe(hoverFrame.getMousePosition(true));
			}
			@Override
			public void menuDeselected(MenuEvent e) {
				this.resetMaybe(hoverFrame.getMousePosition(true));
			}
			@Override
			public void menuSelected(MenuEvent e) {
				// do nothing
			}
		};
		OpacityAdjuster adjuster = new OpacityAdjuster();
		this.hoverFrame.addMouseListener(adjuster);
		mainMenu.addMouseListener(adjuster);
		mainMenu.addMenuListener(adjuster);
		
		try {
			if (logo != null) {
				Image logoIconImage = ImageIO.read(logo);
				this.loginFrame.setIconImage(logoIconImage);
				this.hoverFrame.setIconImage(logoIconImage);
			}
		} catch (IOException e) {
			logger.log(Level.INFO, "Could not load logo image.", e);
		}
		
		this.manageCredentialsDialog = new JDialog(hoverFrame, "Manage Credentials", ModalityType.APPLICATION_MODAL);
		this.manageCredentialsDialog.addWindowListener(
				new WindowCloseHandler(doneManagingAction, manageCredentialsDialog));
		// TODO this.manageCredentialsDialog.setContentPane();
		this.manageCredentialsDialog.pack();
		this.manageCredentialsDialog.setResizable(false);
		this.manageCredentialsDialog.setLocationRelativeTo(null);
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
		case MANAGING:
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
		case MANAGING:
			return this.manageCredentialsDialog;
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
			this.manageCredentialsDialog.setVisible(false);
			break;
		case MANAGING:
			this.hoverFrame.setVisible(true);
			this.loginFrame.setVisible(false);
			this.manageCredentialsDialog.setVisible(true);
			break;
		default:
			this.logger.log(Level.SEVERE, "Unexpected view state in model", state);
		}
		handleSyncStatus();
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
