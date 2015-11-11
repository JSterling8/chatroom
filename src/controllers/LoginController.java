package controllers;

import javax.swing.SwingUtilities;

import models.JMSUser;
import views.LoginFrame;
import views.MainMenuFrame;

public class LoginController {
	private LoginFrame loginFrame;

	public LoginController(LoginFrame loginFrame) {
		this.loginFrame = loginFrame;
	}

	public void handleLoginButtonPressed(String username, String password) {
		// TODO Encrypt password and check against list of users in JavaSpace

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
