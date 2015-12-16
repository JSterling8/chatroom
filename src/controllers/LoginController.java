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

/**
 * Handles all logic behind a given LoginFrame
 * 
 * @author Jonathan Sterling
 *
 */
public class LoginController {
	private static final UserService userService = UserService.getUserService();

	private LoginFrame loginFrame;

	public LoginController(LoginFrame loginFrame) {
		this.loginFrame = loginFrame;
	}

	/**
	 * Handles all business logic when the login button is pressed.
	 * 
	 * @param username
	 *            The username the user entered before clicking login, if any
	 * @param password
	 *            The password the user entered before clicking login, if any
	 */
	public void handleLoginButtonPressed(String username, String password) {
		// Check that the user has entered a username and password. If they
		// haven't, instruct them to do so.
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			JOptionPane.showMessageDialog(loginFrame, "Please enter a username or password to continue.");
		} else {
			// See if the username entered exists in the space.
			JMSUser template = new JMSUser();
			template.setName(username);

			JMSUser userInSpace = userService.getUserByBaseName(template.getBaseName());

			if (userInSpace == null) {
				// If the username does not exist in the space, inform the user.
				JOptionPane.showMessageDialog(loginFrame, "Invalid username.  Please try again.");
			} else {
				try {
					// If the username does exist, check that the password
					// entered is correct.
					boolean passwordCorrect = PasswordEncryptionHelper.validatePassword(password.toCharArray(),
							userInSpace.getPassword());
					if (passwordCorrect) {
						// If logged in successfully, dispose of the login
						// window and open the main menu
						loginFrame.setVisible(false);
						loginFrame.dispose();

						new MainMenuFrame(userInSpace);
					} else {
						// If login failed, inform the user.
						JOptionPane.showMessageDialog(loginFrame, "Invalid password.  Please try again.");
					}
				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					e.printStackTrace();

					// If something goes wrong whilst checking the password
					// (highly unlikely), inform the user that a server error
					// occurred and that they should try again
					JOptionPane.showMessageDialog(loginFrame, "Server error!  Please try again later.");
				}
			}
		}
	}

	/**
	 * Handles all business logic when the create button is pressed.
	 * 
	 * @param username
	 *            The username the user entered before clicking create, if any
	 * @param password
	 *            The password the user entered before clicking create, if any
	 */
	public void handleCreateButtonPressed(String username, String password) {
		// Assert that usernames are 1-12 characters, and passwords are 5-50
		// characters
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			JOptionPane.showMessageDialog(loginFrame, "Please enter a non-blank username and password to continue.");
		} else if (username.length() > 12) {
			JOptionPane.showMessageDialog(loginFrame, "Username must be 1 - 12 characters long.");
		} else if (password.length() < 5 || password.length() > 50) {

			JOptionPane.showMessageDialog(loginFrame, "Password must be 5 - 50 characters long.");
		} else {
			// Get the user to re-enter their password to ensure it is what they
			// expect.
			String passwordConfirmation = getPasswordConfirmation();

			if (StringUtils.isNotBlank(password) && password.equals(passwordConfirmation)) {
				// If the passwords match, encrypt the password and save the
				// user to the space.
				try {
					String encryptedPassword = PasswordEncryptionHelper.encryptPassword(password.toCharArray());
					JMSUser user = new JMSUser();

					user.setPassword(encryptedPassword);
					user.setName(username);
					user.setId(UUID.randomUUID());

					if (user.getBaseName().length() == 0) {
						// This is because the base name of "$$$$$" is the same
						// as the base name of "@@@@@" or any other all special
						// character name
						// Special-character-only names are useful for testing
						JOptionPane.showMessageDialog(loginFrame, "Name must have at least one alphanumeric character");
					} else {
						userService.createUser(user);

						// Log the newly created user in by disposing of the
						// login
						// frame and showing the main menu
						loginFrame.setVisible(false);
						loginFrame.dispose();

						new MainMenuFrame(user);
					}
				} catch (NoSuchAlgorithmException | InvalidKeySpecException | RemoteException
						| TransactionException e) {
					// If the password encryption failed, throw an error, print
					// a stack trace, and inform the user that their account
					// creation failed.
					System.err.println("Failed to encrypt password");
					e.printStackTrace();

					JOptionPane.showMessageDialog(loginFrame,
							"Server error!  Failed to create account.  Please try again later.");
					;
				} catch (DuplicateEntryException e) {
					// If the username already exists in the space, inform the
					// user.
					JOptionPane.showMessageDialog(loginFrame, "Name already in use.  Please enter a different name.");
					;
				}
			} else {
				// If the user enters non-matching passwords, inform them so
				// they can try again
				JOptionPane.showMessageDialog(loginFrame, "Passwords do not match, please try again.");
			}
		}
	}

	/**
	 * Pops up a password confirmation dialog for the user to re-enter their
	 * password.
	 * 
	 * @return The re-entered password.
	 */
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
