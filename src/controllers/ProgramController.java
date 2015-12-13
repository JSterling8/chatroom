package controllers;

import javax.swing.SwingUtilities;

import views.LoginFrame;

public class ProgramController {
	public static void main(String[] args) throws InterruptedException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new LoginFrame();
			}
		});
	}
}
