package org.kryptose.client;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;

import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.OptionsForm;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;
import org.kryptose.exceptions.RecoverableException;
import org.kryptose.requests.User;

public class ViewGUI implements View {
	
	private static final float HOVER_DEFAULT_OPACITY = 0.4f;
	// pixels
	private static final int GAP = 4;
	
	
	private abstract class FormListener implements ActionListener, FocusListener {
		private Action action;
		FormListener(Action action) {
			this.action = action;
		}
		abstract void saveForm();
		abstract Component getComponent();
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
			if (this.action != null) {
				this.saveForm();
				this.action.actionPerformed(ev);
			} else {
				this.getComponent().transferFocus();
			}
		}
	}
	private class TextFieldListener extends FormListener {
		private JTextField textField;
		private TextForm form;
		TextFieldListener(TextForm form, Action action) {
			super(action);
			this.form = form;
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
		void saveForm() {
			control.updateFormText(form, textField.getText());
		}
		@Override
		Component getComponent() {
			return this.textField;
		}
	}
	private class PasswordFieldListener extends FormListener {
		private JPasswordField passwordField;
		private PasswordForm form;
		PasswordFieldListener(PasswordForm form, Action action) {
			super(action);
			this.form = form;
		}
		void bindTo(JPasswordField passwordField) {
			this.passwordField = passwordField;
			this.passwordField.addActionListener(this);
			this.passwordField.addFocusListener(this);
		}
		void updatePasswordForm(PasswordForm form) {
			if (this.form == form) {
				char[] value = model.getFormPasswordClone(form);
				if (value == null) {
					this.passwordField.setText("");
					return;
				}
				// pull shenanigans to be able to "destroy" passwords
				class PasswordContent extends GapContent {
					private static final long serialVersionUID = 4924364071282953867L;
					void setToChars(char[] value) {
						this.replace(0, 0, value, value.length);
					}
				}
				PasswordContent content = new PasswordContent();
				this.passwordField.setDocument(new PlainDocument(content));
				content.setToChars(value);
				Utils.destroyPassword(value);
			}
		}
		@Override
		void saveForm() {
			control.updateFormPassword(form, passwordField.getPassword());
		}
		@Override
		Component getComponent() {
			return this.passwordField;
		}
	}
	private class OptionsListener extends FormListener {
		private JComboBox<String> comboBox;
		private TextForm textForm;
		private OptionsForm optionsForm;
		OptionsListener(TextForm textForm, OptionsForm optionsForm, Action action) {
			super(action);
			this.textForm = textForm;
			this.optionsForm = optionsForm;
		}
		void bindTo(JComboBox<String> comboBox) {
			this.comboBox = comboBox;
			// TODO figure out how this all works.
			this.comboBox.addActionListener(this);
			this.comboBox.addFocusListener(this);
			this.comboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						saveForm();
						comboBox.getEditor().getEditorComponent().transferFocus();
					}
				}				
			});
		}
		void updateTextForm(TextForm form) {
			if (this.textForm == form) {
				String value = model.getFormText(form);
				this.comboBox.setSelectedItem(value);
			}
		}
		void updateOptionsForm(OptionsForm form) {
			if (this.optionsForm == form) {
				String[] values = model.getFormOptions(form);
				this.comboBox.removeAllItems();
				if (values != null) {
					for (String item : values) this.comboBox.addItem(item);
				}
			}
		}
		@Override
		void saveForm() {
			control.updateFormText(textForm, (String)comboBox.getSelectedItem());
		}
		@Override
		Component getComponent() {
			return this.comboBox;
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
	
	private Model model;
	private Controller control;
	private Logger logger = Logger.getLogger("org.kryptose.client.ViewGUI");

	private URL logo = ViewGUI.class.getResource("resources/logo.png");
	
	private JFrame hoverFrame;
	private JFrame loginFrame;
	
	private JDialog createAccountDialog;
	private JDialog manageCredentialsDialog;

	private JMenu mainMenu;
	private JMenu copyUsernameMenu;
	private JMenu copyPasswordMenu;
	
	private List<TextFieldListener> textFieldListeners = new ArrayList<TextFieldListener>();
	private List<PasswordFieldListener> passwordFieldListeners = new ArrayList<PasswordFieldListener>();
	private List<OptionsListener> optionsListeners = new ArrayList<OptionsListener>();
	
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
	private Action setCredentialAction = new AbstractAction("Save Credential") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.set();
		}
	};
	private Action reloadAction = new AbstractAction("Reload Credentials") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.fetch();
		}
	};
	private Action logOutAction = new AbstractAction("Log out") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			int val = JOptionPane.showConfirmDialog(getCurrentActiveWindow(), "Log out?", "Log out", JOptionPane.YES_NO_OPTION);
			if (val == JOptionPane.YES_OPTION) {
				control.logout();
			}
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

	private Action changeMasterPasswordDialogAction = new AbstractAction("Change Master Password") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.CHANGE_MASTER_PASSWORD);
		}
	};
	private Action changeMasterPasswordCancelAction = new AbstractAction("Cancel") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.WAITING);
		}
	};
	private Action changeMasterPasswordAction = new AbstractAction("Change Password") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.changeMasterPassword();
		}
	};
	private Action deleteAccountAction = new AbstractAction("Delete Account") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			// TODO: should authenticate again before allowing this to happen.
			int val = JOptionPane.showConfirmDialog(getCurrentActiveWindow(), "Permanently delete this Kryptose\u2122 account?", "Delete account", JOptionPane.YES_NO_OPTION);
			if (val != JOptionPane.YES_OPTION) return;
			int val2 = JOptionPane.showConfirmDialog(getCurrentActiveWindow(), "Really delete?", "Delete account", JOptionPane.YES_NO_OPTION);
			if (val2 != JOptionPane.YES_OPTION) return;
			control.deleteAccount();
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
			int val = JOptionPane.showConfirmDialog(getCurrentActiveWindow(), "Exit Kryptose\u2122?", "Exit", JOptionPane.YES_NO_OPTION);
			if (val == JOptionPane.YES_OPTION) {
				control.exit();
			}
		}
	};
	
	private static void addGridLeft(Container cont, Component comp) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(GAP, GAP/2, GAP, GAP/2);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = GridBagConstraints.RELATIVE;
		cont.add(comp,gbc);
	}
	
	private static void addGridWithLabel(Container cont, String labelText, String tooltip, JComponent comp) {
		JLabel label = new JLabel(labelText);
		label.setLabelFor(comp);
		label.setToolTipText(tooltip);
		comp.setToolTipText(tooltip);
		addGridLeft(cont, label);
		addGridRight(cont, comp);
	}
	
	private static void addGridRight(Container cont, Component comp) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		if (comp instanceof JComboBox) {
			// this is terrible hax but it works
			gbc.fill = GridBagConstraints.HORIZONTAL;
		}
		gbc.insets = new Insets(GAP, GAP/2, GAP, GAP/2);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		cont.add(comp,gbc);
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
		
		JTextField usernameField = new JTextField(18);
		addGridWithLabel(fieldsPanel, "Username: ", User.VALID_USERNAME_DOC, usernameField);
		TextFieldListener tfl = new TextFieldListener(TextForm.LOGIN_MASTER_USERNAME, null);
		this.textFieldListeners.add(tfl);
		tfl.bindTo(usernameField);

		JPasswordField passwordField = new JPasswordField(18);
		addGridWithLabel(fieldsPanel, "Password: ", null, passwordField);
		PasswordFieldListener pfl = new PasswordFieldListener(PasswordForm.LOGIN_MASTER_PASSWORD, logInAction);
		this.passwordFieldListeners.add(pfl);
		pfl.bindTo(passwordField);
		
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
		
		JTextField usernameField = new JTextField(18);
		addGridWithLabel(panel, "Username: ", User.VALID_USERNAME_DOC, usernameField);
		TextFieldListener tfl = new TextFieldListener(TextForm.CREATE_MASTER_USERNAME, null);
		this.textFieldListeners.add(tfl);
		tfl.bindTo(usernameField);

		JPasswordField passwordField = new JPasswordField(18);
		addGridWithLabel(panel, "Password: ", null, passwordField);
		PasswordFieldListener pfl = new PasswordFieldListener(PasswordForm.CREATE_MASTER_PASSWORD, null);
		this.passwordFieldListeners.add(pfl);
		pfl.bindTo(passwordField);

		JPasswordField confirmPassword = new JPasswordField(18);
		addGridWithLabel(panel, "Confirm Password: ", null, confirmPassword);
		PasswordFieldListener cpfl = new PasswordFieldListener(PasswordForm.CREATE_CONFIRM_PASSWORD, createAccountAction);
		this.passwordFieldListeners.add(cpfl);
		cpfl.bindTo(confirmPassword);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
		buttonPanel.add(new JButton(this.cancelCreateAccountAction));
		buttonPanel.add(new JButton(this.createAccountAction));
		
		addGridRight(panel, buttonPanel);
		
		return panel;
	}
	
	private JPanel createManagePanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		JComboBox<String> domainBox = new JComboBox<String>();
		domainBox.setEditable(true);
		String domainTooltip = "Name of website or application to be logged into";
		addGridWithLabel(panel, "Domain: ", domainTooltip, domainBox);
		OptionsListener dol = new OptionsListener(TextForm.CRED_DOMAIN, OptionsForm.CRED_DOMAIN, null);
		this.optionsListeners.add(dol);
		dol.bindTo(domainBox);

		JComboBox<String> usernameBox = new JComboBox<String>();
		usernameBox.setEditable(true);
		String usernameTooltip = "Username for this website or application";
		addGridWithLabel(panel, "Username: ", usernameTooltip, usernameBox);
		OptionsListener uol = new OptionsListener(TextForm.CRED_USERNAME, OptionsForm.CRED_USERNAME, null);
		this.optionsListeners.add(uol);
		uol.bindTo(usernameBox);

		JPasswordField passwordField = new JPasswordField(18);
		addGridWithLabel(panel, "Password: ", null, passwordField);
		PasswordFieldListener pfl = new PasswordFieldListener(PasswordForm.CRED_PASSWORD, null);
		this.passwordFieldListeners.add(pfl);
		pfl.bindTo(passwordField);

		JPasswordField confirmPassword = new JPasswordField(18);
		addGridWithLabel(panel, "Confirm Password: ", null, confirmPassword);
		PasswordFieldListener cpfl = new PasswordFieldListener(PasswordForm.CRED_CONFIRM_PASSWORD, setCredentialAction);
		this.passwordFieldListeners.add(cpfl);
		cpfl.bindTo(confirmPassword);
		
		// TODO option to display password on screen in plaintext.

		addGridRight(panel, new JButton(this.setCredentialAction));
		addGridRight(panel, new JButton(this.doneManagingAction));
		
		return panel;
	}
	
	private JMenuBar createMenuBar() {
		this.copyUsernameMenu = new JMenu("Copy username");
		this.copyPasswordMenu = new JMenu("Copy password");
		JMenuBar menuBar = new JMenuBar();
		
		JMenu accountSettingsMenu = new JMenu("Account Settings");
		accountSettingsMenu.add(new JMenuItem(changeMasterPasswordDialogAction));
		accountSettingsMenu.add(new JMenuItem(deleteAccountAction));
		
		mainMenu = new JMenu("Kryptose\u2122");
		mainMenu.add(this.copyUsernameMenu);
		mainMenu.add(this.copyPasswordMenu);;
		mainMenu.add(new JMenuItem(this.reloadAction));
		mainMenu.add(new JMenuItem(this.managingDialogAction));
		mainMenu.add(accountSettingsMenu);
		mainMenu.addSeparator();
		mainMenu.add(new JMenuItem(this.minimizeAction));
		mainMenu.add(new JMenuItem(this.moveAction));
		mainMenu.add(new JMenuItem(this.logOutAction));
		mainMenu.add(new JMenuItem(this.exitAction));
		menuBar.add(mainMenu);
		
		return menuBar;
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
		
		this.hoverFrame.setJMenuBar(this.createMenuBar());
		this.hoverFrame.setUndecorated(true);
		this.hoverFrame.setAlwaysOnTop(true);
		this.hoverFrame.pack();
		this.hoverFrame.setResizable(false);
		this.hoverFrame.setOpacity(HOVER_DEFAULT_OPACITY);
		
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
		this.manageCredentialsDialog.setContentPane(this.createManagePanel());
		this.manageCredentialsDialog.pack();
		this.manageCredentialsDialog.setResizable(false);
		this.manageCredentialsDialog.setLocationRelativeTo(null);
	}
	
	@Override
	public void updatePasswordFile() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				syncCredentialMenuItems();
			}
		});
	}
	
	private void syncCredentialMenuItems() {
		PasswordFile pFile = model.getPasswordFile();
		this.copyUsernameMenu.removeAll();
		this.copyPasswordMenu.removeAll();

		if (pFile == null) return;
		
		class CopyAction extends AbstractAction {
			String domain, username;
			boolean copyPass;
			CopyAction(String domain, String username, String title, boolean copyPass) {
				super(title);
				this.domain = domain;
				this.username = username;
				this.copyPass = copyPass;
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				String content = copyPass ? pFile.getVal(domain, username) : username;
				// TODO: not sure if this is the right way to set mime types etc
				String mime = DataFlavor.getTextPlainUnicodeFlavor().getMimeType();
				DataHandler t = new DataHandler(content, mime);
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				clip.setContents(t, null); // TODO ClipboardOwner
			}
		}
		
		for (final String domain : pFile.getDomains()) {
			String[] usernames = pFile.getUsernames(domain);
			if (usernames.length == 1) {
				String username = usernames[0];
				Action copyUsernameAction = new CopyAction(domain, username, domain, false);
				this.copyUsernameMenu.add(new JMenuItem(copyUsernameAction));
				Action copyPasswordAction = new CopyAction(domain, username, domain, true);
				this.copyPasswordMenu.add(new JMenuItem(copyPasswordAction));
			} else {
				JMenu usernameMenu = new JMenu(domain);
				JMenu domainMenu = new JMenu(domain);
				for (String username : usernames) {
					Action copyUsernameAction = new CopyAction(domain, username, username, false);
					usernameMenu.add(new JMenuItem(copyUsernameAction));
					Action copyPasswordAction = new CopyAction(domain, username, username, true);
					domainMenu.add(new JMenuItem(copyPasswordAction));
				}
				this.copyUsernameMenu.add(usernameMenu);
				this.copyPasswordMenu.add(domainMenu);
			}
		}
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
				// do nothing.
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
				createAccountDialogAction, cancelCreateAccountAction,
				reloadAction, managingDialogAction, doneManagingAction,
				setCredentialAction, changeMasterPasswordAction,
				changeMasterPasswordCancelAction, changeMasterPasswordDialogAction,
				deleteAccountAction, reloadAction
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
			createAccountDialog.setCursor(rightCursor);
			break;
		case MANAGING:
			createAccountDialog.setCursor(rightCursor);
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
				for (OptionsListener ol : optionsListeners) {
					ol.updateTextForm(form);
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
	public void updateSelection(OptionsForm form) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (OptionsListener ol : optionsListeners) {
					ol.updateOptionsForm(form);
				}
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
