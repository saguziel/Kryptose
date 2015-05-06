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
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



import javax.activation.DataHandler;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;

import org.kryptose.client.Controller.setType;
import org.kryptose.Utils;
import org.kryptose.client.Model.PasswordForm;
import org.kryptose.client.Model.CredentialAddOrEditForm;
import org.kryptose.client.Model.TextForm;
import org.kryptose.client.Model.ViewState;
import org.kryptose.requests.User;


@SuppressWarnings("serial")
public class ViewGUI implements View {
	
	private static final float HOVER_DEFAULT_OPACITY = 0.4f;
	// pixels
	private static final int GAP = 4;
	private static final Action TRANSFER_FOCUS_ACTION = null; // this is supposed to be null
	private static final String NO_TOOL_TIP = null; // this is supposed to be null
	
	
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
	
	private class DragMoveListener extends MouseAdapter {
		private Window toMove;
		private Point mouseDown;
		public DragMoveListener(Window toMove) {
			super();
			this.toMove = toMove;
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (this.mouseDown == null) return;
			Point p = toMove.getLocation();
			p.x += e.getX() - mouseDown.getX();
			p.y += e.getY() - mouseDown.getY();
			toMove.setLocation(p);
		}
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				this.mouseDown = e.getPoint();
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			this.mouseDown = null;
		}
	}
	
	class OpacityAdjuster extends MouseAdapter implements MenuListener {
		@Override
		public void mouseEntered(MouseEvent e) {
			try {
				hoverFrame.setOpacity(1f);
			} catch(UnsupportedOperationException ex) {
				// do nothing.
			}
		}
		private void resetMaybe(Point p) {
			Rectangle bounds = hoverFrame.getBounds();
			if ((p == null || !bounds.contains(p))
					&& !mainMenu.isPopupMenuVisible()) {
				try {
					hoverFrame.setOpacity(HOVER_DEFAULT_OPACITY);
				} catch (UnsupportedOperationException ex){
					// do nothing.
				}
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

	private URL icon = ViewGUI.class.getResource("resources/icon.png");
	private URL logo = ViewGUI.class.getResource("resources/logo.png");
	
	private JFrame hoverFrame;
	private JFrame loginFrame;
	
	private JDialog createAccountDialog;
	private JDialog manageCredentialsDialog;
	private JDialog editCredentialDialog;
	private JDialog addCredentialDialog;
	private JDialog changeMasterPasswordDialog;
	private JDialog deleteAccountDialog;

	private JMenu mainMenu;
	private JMenu copyUsernameMenu;
	private JMenu copyPasswordMenu;
	
	private List<JCheckBox> showPasswordCheckBoxes = new ArrayList<JCheckBox>();
	
	private List<JPasswordField> unblindablePasswordFields = new ArrayList<JPasswordField>();
	
	private List<TextFieldListener> textFieldListeners = new ArrayList<TextFieldListener>();
	private List<PasswordFieldListener> passwordFieldListeners = new ArrayList<PasswordFieldListener>();
	
	
    String headers[] = {"Domain", "Username"};
	DefaultTableModel tableModel =
      new DefaultTableModel(headers,0) {
        // Make read-only
        public boolean isCellEditable(int x, int y) {
          return false;
        }
      };
      //tableModel.addRow(new Object[]{"v1", "v2"});
    JTable managedCredentialTable = new JTable(tableModel);
    
    
	
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
	private Action deleteCredentialAction = new AbstractAction("Delete Credential") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			int val = JOptionPane.showConfirmDialog(getCurrentActiveWindow(), "Delete this credential?", "Delete", JOptionPane.YES_NO_OPTION);
			if (val != JOptionPane.YES_OPTION) return;
			control.delete();
			control.requestViewState(ViewState.MANAGING);
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
	private Action cancelChangeMasterPasswordAction = new AbstractAction("Cancel") {
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
			int val = JOptionPane.showConfirmDialog(getCurrentActiveWindow(),
					"Permanently delete this Kryptose\u2122 account?",
					"Delete account", JOptionPane.YES_NO_OPTION);
			if (val != JOptionPane.YES_OPTION) {
				control.updateFormPassword(
						PasswordForm.DELETE_ACCOUNT_CONFIRM_PASSWORD,
						null);
				return;
			}
			control.deleteAccount();
		}
	};
	private Action deleteAccountDialogAction = new AbstractAction("Delete Account") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.DELETE_ACCOUNT);
		}
	};
	private Action cancelDeleteAccountAction = new AbstractAction("Cancel") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.WAITING);
		}
	};
	
	
	private Action editCredentialDialogAction = new AbstractAction("Edit Credential") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.EDITING);
		}
	};
	private Action editCredentialAction = new AbstractAction("Done") {
		@Override
		public void actionPerformed(ActionEvent ev) {			
			control.set(setType.EDIT);			
		}
	};
	private Action cancelEditingCredentialAction = new AbstractAction("Cancel") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.MANAGING);
		}
	};

	private Action addCredentialDialogAction = new AbstractAction("New Credential") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.ADDING);
		}
	};
	private Action addCredentialAction = new AbstractAction("Done") {
		@Override
		public void actionPerformed(ActionEvent ev) {	
			control.set(setType.ADD);
		}
	};
	private Action cancelAddingCredentialAction = new AbstractAction("Cancel") {
		@Override
		public void actionPerformed(ActionEvent ev) {
			control.requestViewState(ViewState.MANAGING);
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
			MouseAdapter listener = new DragMoveListener(hoverFrame) {
				@Override
				public void mousePressed(MouseEvent e) {
					if (!hoverFrame.getCursor().equals(moveCursor)) {
						reset();
					} else {
						super.mousePressed(e);
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					reset();
					hoverFrame.setCursor(normalCursor);
					super.mouseReleased(e);
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
			if (val != JOptionPane.YES_OPTION) return;
			control.exit();
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
	
	private static void addGridBelow(Container cont, Component comp) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets = new Insets(GAP, GAP/2, GAP, GAP/2);
		gbc.gridy =  GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.CENTER;
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
	
	private void addTextFieldToGrid(Container cont, TextForm form,
			String label, Action action, String toolTip) {
		JTextField textField = new JTextField(18);
		addGridWithLabel(cont, label, toolTip, textField);
		TextFieldListener tfl = new TextFieldListener(form, action);
		this.textFieldListeners.add(tfl);
		tfl.bindTo(textField);
	}

	private void addDisabledTextFieldToGrid(Container cont, TextForm form,
			String label, Action action, String toolTip) {
		JTextField textField = new JTextField(18);
		textField.setEnabled(false);
		addGridWithLabel(cont, label, toolTip, textField);
		TextFieldListener tfl = new TextFieldListener(form, action);
		this.textFieldListeners.add(tfl);
		tfl.bindTo(textField);
	}

	private JPasswordField addPasswordFieldToGrid(Container cont, PasswordForm form,
			String label, Action action, String toolTip) {
		JPasswordField passwordField = new JPasswordField(18);
				
		addGridWithLabel(cont, label, toolTip, passwordField);
		PasswordFieldListener pfl = new PasswordFieldListener(form, action);
		this.passwordFieldListeners.add(pfl);
		pfl.bindTo(passwordField);
		
		return passwordField;
	}

	
	private JPanel createLoginPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		JPanel loginFormPanel = new JPanel(new BorderLayout());
		loginFormPanel.setBorder(BorderFactory.createEmptyBorder(
				GAP, 2*GAP, 2*GAP, GAP/2));
		
		JPanel fieldsPanel = new JPanel(new GridBagLayout());
		JLabel loginLabel = new JLabel("Please enter username and password:");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5*GAP, GAP/2, 3*GAP, GAP/2);
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.WEST;
		fieldsPanel.add(loginLabel, gbc);
		
		this.addTextFieldToGrid(fieldsPanel, TextForm.LOGIN_MASTER_USERNAME,
				"Username: ", TRANSFER_FOCUS_ACTION, User.VALID_USERNAME_DOC);

		this.addPasswordFieldToGrid(fieldsPanel, PasswordForm.LOGIN_MASTER_PASSWORD,
				"Password: ", logInAction, NO_TOOL_TIP);
		
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
		
		this.addTextFieldToGrid(panel, TextForm.CREATE_MASTER_USERNAME,
				"Username: ", TRANSFER_FOCUS_ACTION, User.VALID_USERNAME_DOC);

		this.addPasswordFieldToGrid(panel, PasswordForm.CREATE_MASTER_PASSWORD,
				"Password: ", TRANSFER_FOCUS_ACTION, NO_TOOL_TIP);

		this.addPasswordFieldToGrid(panel, PasswordForm.CREATE_CONFIRM_PASSWORD,
				"Confirm Password: ", createAccountAction, NO_TOOL_TIP);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
		buttonPanel.add(new JButton(this.cancelCreateAccountAction));
		buttonPanel.add(new JButton(this.createAccountAction));
		
		addGridRight(panel, buttonPanel);
		
		return panel;
	}
	
	private JPanel createEditCredentialPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		String domainTooltip = "Name of website or application to be logged into";
		this.addDisabledTextFieldToGrid(panel, TextForm.CRED_DOMAIN, "Domain: ", TRANSFER_FOCUS_ACTION, domainTooltip);
		
		String usernameTooltip = "Username for this website or application";
		this.addDisabledTextFieldToGrid(panel, TextForm.CRED_USERNAME, "Username: ", TRANSFER_FOCUS_ACTION, usernameTooltip);

		this.unblindablePasswordFields.add(
				this.addPasswordFieldToGrid(panel, PasswordForm.CRED_PASSWORD,
						"Password: ", TRANSFER_FOCUS_ACTION, NO_TOOL_TIP)
				);
		this.unblindablePasswordFields.add(
				this.addPasswordFieldToGrid(panel, PasswordForm.CRED_CONFIRM_PASSWORD,
						"Confirm Password: ", editCredentialAction, NO_TOOL_TIP)
				);

		JCheckBox showPasswordCheckBox = new JCheckBox();
		showPasswordCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					control.updateShowPassword(true);
				else if(e.getStateChange() == ItemEvent.DESELECTED)
					control.updateShowPassword(false);
			}
		});
		this.showPasswordCheckBoxes.add(showPasswordCheckBox);
		addGridWithLabel(panel, "Show password: ", NO_TOOL_TIP, showPasswordCheckBox);
		addGridLeft(panel, new JButton(this.cancelEditingCredentialAction));
		addGridRight(panel, new JButton(this.editCredentialAction));
		addGridRight(panel, new JButton(this.deleteCredentialAction));
		
		return panel;
	}

	private JPanel createAddCredentialPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		String domainTooltip = "Name of website or application to be logged into";
		this.addTextFieldToGrid(panel, TextForm.CRED_DOMAIN, "Domain: ", TRANSFER_FOCUS_ACTION, domainTooltip);
		
		String usernameTooltip = "Username for this website or application";
		this.addTextFieldToGrid(panel, TextForm.CRED_USERNAME, "Username: ", TRANSFER_FOCUS_ACTION, usernameTooltip);
		
		this.unblindablePasswordFields.add(
				this.addPasswordFieldToGrid(panel, PasswordForm.CRED_PASSWORD,
						"Password: ", TRANSFER_FOCUS_ACTION, NO_TOOL_TIP)
				);
		this.unblindablePasswordFields.add(
				this.addPasswordFieldToGrid(panel, PasswordForm.CRED_CONFIRM_PASSWORD,
						"Confirm Password: ", addCredentialAction, NO_TOOL_TIP)
				);
		
		JCheckBox showPasswordCheckBox = new JCheckBox();
		showPasswordCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED)
					model.setShowPassword(true);
				else if(e.getStateChange() == ItemEvent.DESELECTED)
					model.setShowPassword(false);
			}
		});
		this.showPasswordCheckBoxes.add(showPasswordCheckBox);
		addGridWithLabel(panel, "Show password: ", NO_TOOL_TIP, showPasswordCheckBox);
		addGridLeft(panel, new JButton(this.cancelAddingCredentialAction));		
		addGridRight(panel, new JButton(this.addCredentialAction));
		
		return panel;
	}

	
	private JPanel createManagePanel() {
		JPanel panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		

	    // Set selection to first row
	    ListSelectionModel selectionModel =
	      this.managedCredentialTable.getSelectionModel();
	    //selectionModel.setSelectionInterval(0, 0);
	    selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    // Add to screen so scrollable
	    JScrollPane credentialScrollableTable = new JScrollPane (this.managedCredentialTable);
	    credentialScrollableTable.setSize(200,100);	    
	    panel.add(credentialScrollableTable);
	    
	    
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
		//buttonPanel.add(new JButton(this.deleteCredentialAction));
		buttonPanel.add(new JButton(this.addCredentialDialogAction));
		buttonPanel.add(new JButton(this.editCredentialDialogAction));
		buttonPanel.add(new JButton(this.doneManagingAction));
		
		panel.add(buttonPanel);
		
		

		
		return panel;
	}
	
	private JPanel createChangeMasterPasswordPanel() {
		JPanel panel = new JPanel(new GridBagLayout());

		this.addPasswordFieldToGrid(panel, PasswordForm.CHANGE_OLD_MASTER_PASSWORD,
				"Old Password: ", TRANSFER_FOCUS_ACTION, NO_TOOL_TIP);

		this.addPasswordFieldToGrid(panel, PasswordForm.CHANGE_NEW_MASTER_PASSWORD,
				"New Password: ", TRANSFER_FOCUS_ACTION, NO_TOOL_TIP);

		this.addPasswordFieldToGrid(panel, PasswordForm.CHANGE_CONFIRM_NEW_MASTER_PASSWORD,
				"Confirm New Password: ", changeMasterPasswordAction, NO_TOOL_TIP);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
		buttonPanel.add(new JButton(this.cancelChangeMasterPasswordAction));
		buttonPanel.add(new JButton(this.changeMasterPasswordAction));
		
		addGridRight(panel, buttonPanel);
		
		return panel;
	}
	
	private JPanel createDeleteAccountPanel() {
		JPanel panel = new JPanel(new GridBagLayout());

		this.addPasswordFieldToGrid(panel, PasswordForm.DELETE_ACCOUNT_CONFIRM_PASSWORD,
				"Confirm Password: ", this.deleteAccountAction, NO_TOOL_TIP);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
		buttonPanel.add(new JButton(this.cancelDeleteAccountAction));
		buttonPanel.add(new JButton(this.deleteAccountAction));
		
		addGridRight(panel, buttonPanel);
		
		return panel;
	}
	
	private JMenuBar createMenuBar() {
		this.copyUsernameMenu = new JMenu("Copy username");
		this.copyPasswordMenu = new JMenu("Copy password");
		JMenuBar menuBar = new JMenuBar();
		
		JMenu accountSettingsMenu = new JMenu("Account Settings");
		accountSettingsMenu.add(new JMenuItem(changeMasterPasswordDialogAction));
		accountSettingsMenu.add(new JMenuItem(deleteAccountDialogAction));
		
		mainMenu = new JMenu("Kryptose\u2122");
		mainMenu.add(this.copyUsernameMenu);
		mainMenu.add(this.copyPasswordMenu);
		mainMenu.add(new JMenuItem(this.managingDialogAction));
		mainMenu.add(new JMenuItem(this.reloadAction));
		mainMenu.add(accountSettingsMenu);
		mainMenu.addSeparator();
		mainMenu.add(new JMenuItem(this.moveAction));
		mainMenu.add(new JMenuItem(this.minimizeAction));
		mainMenu.add(new JMenuItem(this.logOutAction));
		mainMenu.add(new JMenuItem(this.exitAction));
		menuBar.add(mainMenu);
		
		return menuBar;
	}
	
	private JDialog createModalDialog(Window parent, String title,
			Action closeAction, JPanel content) {
		JDialog dialog = new JDialog(parent, title, ModalityType.APPLICATION_MODAL);
		dialog.addWindowListener(
				new WindowCloseHandler(closeAction, dialog));
		dialog.setContentPane(content);
		dialog.pack();
		return dialog;
	}
	
	public ViewGUI(Model model, Controller control) {
		this.model = model;
		this.control = control;
		
		this.loginFrame = new JFrame("Kryptose\u2122 Password Management System");
		this.loginFrame.addWindowListener(new WindowCloseHandler(exitAction, loginFrame));
		this.loginFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.loginFrame.setContentPane(createLoginPanel());
		this.loginFrame.setResizable(false);
		this.loginFrame.pack();
		this.loginFrame.setLocationRelativeTo(null);
		
		this.createAccountDialog = this.createModalDialog(
				this.loginFrame, "New Account Creation",
				this.cancelCreateAccountAction, this.createCreateAccountPanel()
				);
		this.createAccountDialog.setLocationRelativeTo(loginFrame);
		
		this.hoverFrame = new JFrame("Kryptose\u2122");
		this.hoverFrame.addWindowListener(new WindowCloseHandler(exitAction, hoverFrame));
		this.loginFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.hoverFrame.setJMenuBar(this.createMenuBar());
		this.hoverFrame.setUndecorated(true);
		this.hoverFrame.setAlwaysOnTop(true);
		this.hoverFrame.setResizable(false);
		try {
			this.hoverFrame.setOpacity(HOVER_DEFAULT_OPACITY);
		} catch (UnsupportedOperationException e) {
			// do nothing
		}
		this.hoverFrame.pack();
		
		OpacityAdjuster adjuster = new OpacityAdjuster();
		this.hoverFrame.addMouseListener(adjuster);
		mainMenu.addMouseListener(adjuster);
		mainMenu.addMenuListener(adjuster);
		
		this.managedCredentialTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
		        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		        DefaultTableModel dtm = (DefaultTableModel) managedCredentialTable.getModel();
		        
		        if(e.getValueIsAdjusting()) return;
		        if (lsm.isSelectionEmpty()){
		        	model.setSelectedCredential(null, null);
		        }
				int selRowIndex = lsm.getMinSelectionIndex();
		        if (lsm.isSelectedIndex(selRowIndex)) {
					model.setSelectedCredential((String) dtm.getValueAt(selRowIndex, 0), (String) dtm.getValueAt(selRowIndex, 1));
		        }else{
		        	model.setSelectedCredential(null, null);
		        }
			}
		});
				
		try {
			if (icon != null) {
				Image logoIconImage = ImageIO.read(icon);
				this.loginFrame.setIconImage(logoIconImage);
				this.hoverFrame.setIconImage(logoIconImage);
			}
		} catch (IOException e) {
			logger.log(Level.INFO, "Could not load logo icon image.", e);
		}
		
		
		this.manageCredentialsDialog = this.createModalDialog(
				this.hoverFrame, "Manage Credentials",
				this.doneManagingAction, this.createManagePanel()
				);
		this.manageCredentialsDialog.setLocationRelativeTo(null);
		
		this.editCredentialDialog = this.createModalDialog(
				this.manageCredentialsDialog, "Edit Credential",
				this.cancelEditingCredentialAction, this.createEditCredentialPanel()
				);
		this.editCredentialDialog.setLocationRelativeTo(null);
		this.editCredentialDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		this.addCredentialDialog = this.createModalDialog(
				this.manageCredentialsDialog, "Add New Credential",
				this.cancelAddingCredentialAction, this.createAddCredentialPanel()
				);
		this.addCredentialDialog.setLocationRelativeTo(null);
		this.addCredentialDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		
		this.changeMasterPasswordDialog = this.createModalDialog(
				this.hoverFrame, "Change Master Password",
				this.cancelChangeMasterPasswordAction,
				this.createChangeMasterPasswordPanel()
				);
		this.changeMasterPasswordDialog.setLocationRelativeTo(null);
		
		this.deleteAccountDialog = this.createModalDialog(
				this.hoverFrame, "Delete Kryptose\u2122 Account",
				this.cancelDeleteAccountAction, this.createDeleteAccountPanel()
				);
		this.deleteAccountDialog.setLocationRelativeTo(null);
	}
	
	@Override
	public void updatePasswordFile() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				syncCredentialMenuItems();
				syncCredentialTable();
				handleActionStatuses();
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
				// TODO maybe put this stuff in controller.
				// and maybe also avoid having inner anonymous classes
				// in inner anonymous classes in inner anonymous classes.
				
				char[] content = copyPass ? pFile.getValClone(domain, username) : (username== null ? null : username.toCharArray());
				String mime = DataFlavor.getTextPlainUnicodeFlavor().getMimeType();
				DataHandler t = new DataHandler(content, mime);
				Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
				class ClipboardWatcher implements ClipboardOwner, ActionListener {
					static final int CLIPBOARD_DURATION = 9900;
					static final String msg = "Clipboard will be cleared in %s seconds.";
					JDialog dialog = new JDialog(hoverFrame, "Clipboard in use");
					JLabel label = new JLabel(String.format(msg, 10));
					JButton button = new JButton("Clear now");
					volatile long expirationTimeMillis;
					volatile boolean wiped = false;
					synchronized void init() {
						update();
						JPanel panel = new JPanel(new BorderLayout());
						JPanel buttonPanel = new JPanel();
						panel.add(label, BorderLayout.NORTH);
						buttonPanel.add(button);
						panel.add(buttonPanel, BorderLayout.SOUTH);
						panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
						DragMoveListener dragListener = new DragMoveListener(dialog);
						panel.addMouseMotionListener(dragListener);
						panel.addMouseListener(dragListener);
						dialog.add(panel);
						dialog.setUndecorated(true);
						dialog.pack();
						dialog.setResizable(false);
						button.addActionListener(ClipboardWatcher.this);
						dialog.setVisible(true);
					}
					synchronized void update() {
						int timeLeftMillis = (int)(expirationTimeMillis - System.currentTimeMillis());
						if (timeLeftMillis <= 0) clear();
						label.setText(String.format(msg, timeLeftMillis/1000));
					}
					synchronized void start() {
						expirationTimeMillis = System.currentTimeMillis() + CLIPBOARD_DURATION;
						init();
						new Thread(new Runnable() {
							@Override
							public void run() {
								boolean keepRunning = true;
								do {
									synchronized (ClipboardWatcher.this) {
										keepRunning = !wiped;
									}
									SwingUtilities.invokeLater(() -> update());
									try { Thread.sleep(200);}
									catch (InterruptedException e) {
										keepRunning = false;
										SwingUtilities.invokeLater(() -> clear());
									}
								} while (keepRunning);
							}
						}).start();
					}
					synchronized void clear() {
						if (!wiped) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(""), null);
						setCleared();
					}
					synchronized void setCleared() {
						Utils.destroyPassword(content);
						wiped = true;
						dialog.setVisible(false);
						dialog.dispose();
					}
					@Override
					public synchronized void lostOwnership(Clipboard clipboard,
							Transferable contents) {
						setCleared();
					}
					@Override
					public synchronized void actionPerformed(ActionEvent e) {
						expirationTimeMillis = System.currentTimeMillis();
					}
				}
				ClipboardWatcher watcher = new ClipboardWatcher();
				watcher.start();
				clip.setContents(t, watcher);
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
	
	private void syncCredentialTable() {
		PasswordFile pFile = model.getPasswordFile();
		
		DefaultTableModel table =  (DefaultTableModel) managedCredentialTable.getModel();
		table.setRowCount(0);//Removes all rows
		
		
		if (pFile == null) return;
		
		for(final Credential c : pFile.credentials){
			table.addRow(new Object[]{c.getDomain(), c.getUsername()});			
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
		if (ex == null) return;
		final String msg = ex.getMessage();
		final String title = "Error";
		final Window parent = this.getCurrentActiveWindow();
		SwingUtilities.invokeLater( new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(
						parent, msg, title, JOptionPane.ERROR_MESSAGE);
			}
		});
	}
	
	private void handleActionStatuses() {
		boolean waitingOnServer = this.model.isWaitingOnServer();

		Action[] actionsToToggle = new Action[] {
				logInAction, createAccountAction, logOutAction,
				createAccountDialogAction, cancelCreateAccountAction,
				reloadAction, 
				managingDialogAction, doneManagingAction,
				
				editCredentialDialogAction, editCredentialAction,cancelEditingCredentialAction,
				
				changeMasterPasswordAction,
				cancelChangeMasterPasswordAction, changeMasterPasswordDialogAction,
				deleteAccountAction, reloadAction
				};
		for (Action action : actionsToToggle) {
			action.setEnabled(!waitingOnServer);
		}
		
		if (waitingOnServer) {
			editCredentialAction.setEnabled(false);
			addCredentialAction.setEnabled(false);
			deleteCredentialAction.setEnabled(false);
			//editCredentialDialogAction.setEnabled(false);
		} else {
			boolean setEnabled = false;
			boolean delEnabled = false;
			//boolean editEnabled = false;
			
			PasswordFile pFile = model.getPasswordFile();
			if (pFile != null) {
				String domain = model.getFormText(TextForm.CRED_DOMAIN);
				String username = model.getFormText(TextForm.CRED_USERNAME);
				char[] passwordUI = model.getFormPasswordClone(PasswordForm.CRED_PASSWORD);
				// WARNING: code currently assumes that char[] array obtained from
				// PasswordFile is something that we should destroy here once we're done with it.
				char[] passwordSaved = pFile.getValClone(domain, username);
				
				setEnabled = !Arrays.equals(passwordUI, passwordSaved);
				delEnabled = passwordSaved != null;
				
				Utils.destroyPassword(passwordUI);
				Utils.destroyPassword(passwordSaved);
			}
			addCredentialAction.setEnabled(setEnabled);
			editCredentialAction.setEnabled(setEnabled);
			deleteCredentialAction.setEnabled(delEnabled);
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
		handleActionStatuses();
		boolean waitingOnServer = this.model.isWaitingOnServer();

		Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
		Cursor appropriateCursor = waitingOnServer ? waitCursor : normalCursor;
		this.getCurrentActiveWindow().setCursor(appropriateCursor);
	}

	@Override
	public void updateTextForm(TextForm form) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (TextFieldListener tfl : textFieldListeners) {
					tfl.updateTextForm(form);
				}
/*				for (OptionsListener ol : optionsListeners) {
					ol.updateTextForm(form);
				}*/
				handleActionStatuses();
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
				handleActionStatuses();
			}
		});
	}

	@Override
	public void updateSelection(CredentialAddOrEditForm form) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
/*				for (OptionsListener ol : optionsListeners) {
					ol.updateOptionsForm(form);
				}*/
				handleActionStatuses();
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
		case EDITING:
			return this.editCredentialDialog;
		case ADDING:
			return this.addCredentialDialog;
		case CHANGE_MASTER_PASSWORD:
			return this.changeMasterPasswordDialog;
		case DELETE_ACCOUNT:
			return this.deleteAccountDialog;
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
		// Warning: when a modal dialog is set to visible, the thread processing this
		// method will block until the dialog is dismissed.
		// Thus setVisible(true) on a modal dialog MUST be the last method called.
		
		handleSyncStatus();
		
		Window[] windows = new Window[] {
				createAccountDialog,
				manageCredentialsDialog, 
				editCredentialDialog,
				addCredentialDialog,
				changeMasterPasswordDialog, deleteAccountDialog,
				loginFrame, hoverFrame,
		};
		Window activeWindow = this.getCurrentActiveWindow();
		for (Window window : windows) {
			if (!window.isAncestorOf(activeWindow) && window != activeWindow) {
				window.setVisible(false);
			}
		}
		for (Window window : windows) {
			if (window.isAncestorOf(activeWindow) && window instanceof JFrame) {
				window.setVisible(true);
			}
		}
		activeWindow.setVisible(true);
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

	@Override
	public void updateShowPasswords() {
		boolean b = this.model.getShowPassword();
		
		char c = b ? (char) 0 : '*';
		for(JPasswordField p : unblindablePasswordFields){
				p.setEchoChar(c);
		}
		
		for (JCheckBox box : this.showPasswordCheckBoxes) {
			box.setSelected(b);
		}
	}

}
