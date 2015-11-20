package views;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import controllers.LoginController;
import java.awt.Color;
import java.awt.Font;

public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1479957234936275504L;
	private LoginController controller;

	private JTextField tfUsername;
	private JPasswordField pfPassword;
	
	public LoginFrame() {
		controller = new LoginController(this);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		getContentPane().add(panel, gbc_panel);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		panel_2.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("58px"),},
			new RowSpec[] {
				FormSpecs.LINE_GAP_ROWSPEC,
				RowSpec.decode("14px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblUsername = new JLabel("Username");
		panel_2.add(lblUsername, "1, 2, 2, 1, left, top");
		
		JLabel lblPassword = new JLabel("Password");
		panel_2.add(lblPassword, "2, 4");
		
		JPanel panel_3 = new JPanel();
		panel.add(panel_3);
		panel_3.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("142px:grow"),
				ColumnSpec.decode("86px"),},
			new RowSpec[] {
				FormSpecs.LINE_GAP_ROWSPEC,
				RowSpec.decode("20px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		tfUsername = new JTextField();
		panel_3.add(tfUsername, "1, 2, 2, 1, fill, top");
		tfUsername.setColumns(10);
		
		pfPassword = new JPasswordField();
		panel_3.add(pfPassword, "1, 4, 2, 1, fill, default");
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		
		JButton btnCreate = new JButton("Create");
		btnCreate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.handleCreateButtonPressed(tfUsername.getText(), new String(pfPassword.getPassword()));
			}
		});
		panel_1.add(btnCreate);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				controller.handleLoginButtonPressed(tfUsername.getText(), new String(pfPassword.getPassword()));
			}
		});
		panel_1.add(btnLogin);
		
		JLabel lblPleaseNoteThat = new JLabel("Please note that accounts are deleted if not logged into after 30 days");
		lblPleaseNoteThat.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPleaseNoteThat.setForeground(Color.RED);
		panel.add(lblPleaseNoteThat);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(new Dimension(450, 200));
		setVisible(true);
	}

	public static void main(String[] args) throws InterruptedException {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new LoginFrame();
			}
		});
	}

}
