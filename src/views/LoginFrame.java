package views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import controllers.LoginController;

/**
 * The account login/creation window.
 * 
 * @author Jonathan Sterling
 *
 */
public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1479957234936275504L;

	private LoginController controller;
	private JTextField tfUsername;
	private JPasswordField pfPassword;

	public LoginFrame() {
		// Create the controller for this frame
		controller = new LoginController(this);

		// Set window's base layout
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		// Create a base panel for all other elements to go onto
		JPanel basePanel = new JPanel();
		GridBagConstraints constraintsPanel = new GridBagConstraints();
		constraintsPanel.fill = GridBagConstraints.BOTH;
		constraintsPanel.gridx = 0;
		constraintsPanel.gridy = 0;
		getContentPane().add(basePanel, constraintsPanel);
		basePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		// Create a panel for all of the labels
		JPanel labelsPanel = new JPanel();
		basePanel.add(labelsPanel);
		labelsPanel.setLayout(
				new FormLayout(new ColumnSpec[] { FormSpecs.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("58px"), },
						new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, RowSpec.decode("14px"),
								FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		// Create the labels and add them to the labels panel
		JLabel lblUsername = new JLabel("Username");
		labelsPanel.add(lblUsername, "1, 2, 2, 1, left, top");
		JLabel lblPassword = new JLabel("Password");
		labelsPanel.add(lblPassword, "2, 4");

		// Create a panel for user inputs
		JPanel inputsPanel = new JPanel();
		basePanel.add(inputsPanel);
		inputsPanel.setLayout(
				new FormLayout(new ColumnSpec[] { ColumnSpec.decode("142px:grow"), ColumnSpec.decode("86px"), },
						new RowSpec[] { FormSpecs.LINE_GAP_ROWSPEC, RowSpec.decode("20px"),
								FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

		// Create user inputs and put them on the inputs panel
		tfUsername = new JTextField();
		inputsPanel.add(tfUsername, "1, 2, 2, 1, fill, top");
		tfUsername.setColumns(10);
		pfPassword = new JPasswordField();
		inputsPanel.add(pfPassword, "1, 4, 2, 1, fill, default");

		// Create a panel for all of the buttons to go on
		JPanel buttonsPanel = new JPanel();
		basePanel.add(buttonsPanel);

		// Create the buttons and put them on the buttons panel
		JButton btnCreate = new JButton("Create");
		btnCreate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.handleCreateButtonPressed(tfUsername.getText(), new String(pfPassword.getPassword()));
			}
		});
		buttonsPanel.add(btnCreate);

		JButton btnLogin = new JButton("Login");
		btnLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				controller.handleLoginButtonPressed(tfUsername.getText(), new String(pfPassword.getPassword()));
			}
		});
		buttonsPanel.add(btnLogin);

		// Create a warnin label about account deletion and add it to the
		// bottom of the base panel
		JLabel lblPleaseNoteThat = new JLabel("Please note that accounts are deleted if not logged into after 90 days");
		lblPleaseNoteThat.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPleaseNoteThat.setForeground(Color.RED);
		basePanel.add(lblPleaseNoteThat);

		// Set some window defaults...
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(new Dimension(450, 200));
		setTitle("Login/Register");
		setVisible(true);
	}
}