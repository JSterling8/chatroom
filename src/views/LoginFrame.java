package views;

import javax.swing.JFrame;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import javax.swing.JPasswordField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;

public class LoginFrame extends JFrame {
	private JTextField tfUsername;
	private JPasswordField passwordField;
	public LoginFrame() {
		
		JLabel lblUsername = new JLabel("Username");
		
		tfUsername = new JTextField();
		lblUsername.setLabelFor(tfUsername);
		tfUsername.setColumns(15);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setLabelFor(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setColumns(15);
		getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		getContentPane().add(lblUsername);
		getContentPane().add(tfUsername);
		getContentPane().add(lblPassword);
		getContentPane().add(passwordField);
		
		JButton btnCreate = new JButton("Create");
		getContentPane().add(btnCreate);
		
		JButton btnLogin = new JButton("Login");
		getContentPane().add(btnLogin);
		
		setVisible(true);
	}
	
	public static void main(String[] args) throws InterruptedException{
		LoginFrame loginFrame = new LoginFrame();
		
		Thread.sleep(10000);
	}

}
