package controllers;

import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.commons.lang3.StringUtils;

import exceptions.DuplicateEntryException;
import models.JMSUser;
import net.jini.core.transaction.TransactionException;
import services.UserService;
import services.helper.PasswordEncryptionHelper;
import views.LoginFrame;
import views.MainMenuFrame;

public class LoginController {
	private static final UserService userService = UserService.getUserService();
	private LoginFrame loginFrame;

	public LoginController(LoginFrame loginFrame) {
		this.loginFrame = loginFrame;
	}

	public void handleLoginButtonPressed(String username, String password) {
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			JOptionPane.showMessageDialog(loginFrame, "Please enter a username or password to continue.");
		} else {
			JMSUser template = new JMSUser();
			template.setName(username);

			JMSUser userInSpace = userService.getUserByBaseName(template.getBaseName());

			if (userInSpace == null) {
				JOptionPane.showMessageDialog(loginFrame, "Invalid username.  Please try again.");
			} else {
				try {
					boolean passwordCorrect = PasswordEncryptionHelper.validatePassword(password.toCharArray(),
							userInSpace.getPassword());
					if (passwordCorrect) {
						loginFrame.setVisible(false);
						loginFrame.dispose();
						
						new MainMenuFrame(userInSpace);
					} else {
						JOptionPane.showMessageDialog(loginFrame, "Invalid password.  Please try again.");
					}

				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					e.printStackTrace();
					
					JOptionPane.showMessageDialog(loginFrame, "Server error!  Please try again later.");
				}
			}
		}
	}

	public void handleCreateButtonPressed(String username, String password) {
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			JOptionPane.showMessageDialog(loginFrame, "Please enter a username or password to continue.");
		} else {
			String passwordConfirmation = getPasswordConfirmation();

			if (StringUtils.isNotBlank(password) && password.equals(passwordConfirmation)) {
				try {
					String encryptedPassword = PasswordEncryptionHelper.encryptPassword(password.toCharArray());
					JMSUser user = new JMSUser();

					user.setPassword(encryptedPassword);
					user.setName(username);
					user.setId(UUID.randomUUID());

					userService.createUser(user);

					loginFrame.setVisible(false);
					loginFrame.dispose();

					new MainMenuFrame(user);
				} catch (NoSuchAlgorithmException | InvalidKeySpecException | RemoteException
						| TransactionException e) {
					System.err.println("Failed to encrypt password");
					e.printStackTrace();

					JOptionPane.showMessageDialog(loginFrame,
							"Server error!  Failed to create account.  Please try again later.");
					;
				} catch (DuplicateEntryException e) {
					JOptionPane.showMessageDialog(loginFrame, "Name already in use.  Please enter a different name.");
					;
				}
			} else {
				JOptionPane.showMessageDialog(loginFrame, "Passwords do not match, please try again.");
			}
		}
	}

	private String getPasswordConfirmation() {
		JPanel passwordConfirmationPanel = new JPanel();
		JLabel passwordConfirmationlabel = new JLabel("Please confirm your password:");
		JPasswordField passwordConfirmationField = new JPasswordField(10);

		passwordConfirmationPanel.add(passwordConfirmationlabel);
		passwordConfirmationPanel.add(passwordConfirmationField);

		String[] options = new String[] { "Confirm" };

		JOptionPane.showOptionDialog(null, passwordConfirmationPanel, "Confirm Password", JOptionPane.NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		return new String(passwordConfirmationField.getPassword());
	}
}
