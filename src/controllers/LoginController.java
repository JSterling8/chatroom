package controllers;

import javax.swing.SwingUtilities;

import models.JMSUser;
import views.LoginFrame;
import views.MainMenuFrame;

public class LoginController {
	//FIXME Keep track of all open windows and close app when all are closed.
	private LoginFrame loginFrame;

	public LoginController(LoginFrame loginFrame) {
		this.loginFrame = loginFrame;
	}

	public void handleLoginButtonPressed(String username, String password) {
		// TODO Encrypt password and check against user in JavaSpace (if one exists)

		boolean success = true;

		if (success) {
			loginFrame.setVisible(false);
			loginFrame.dispose();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JMSUser user = new JMSUser("Admin", "admin");
					new MainMenuFrame(user);
				}
			});
		} else {
			// TODO Show user error dialogue
		}
	}

	public void handleCreateButtonPressed(String username, String password) {
		// TODO Popup with a "Confirm Password" text box
		// TODO Encrypt password, create User, put in JavaSpace
		boolean success = true;

		if (success) {
			loginFrame.setVisible(false);
			loginFrame.dispose();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					JMSUser user = new JMSUser("Admin", "admin");
					new MainMenuFrame(user);
				}
			});
		} else {
			// TODO Show user error dialogue
		}
	}
}
