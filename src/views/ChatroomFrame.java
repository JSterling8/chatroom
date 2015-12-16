package views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import controllers.ChatroomController;
import models.JMSTopic;
import models.JMSUser;

/**
 * A chatroom window.
 * 
 * @author Jonathan Sterling
 *
 */
public class ChatroomFrame extends JFrame {
	private static final long serialVersionUID = -6904280288906125276L;

	// Contains the messages of the chatroom
	private DefaultTableModel messagesTableModel;
	// Contains an up-to-date list of all users in the chatroom
	private DefaultTableModel usersTableModel;
	private ChatroomController controller;
	private JTextField tfMessageInput;
	// The table that contains the messages
	private ColoredTable messagesTable;
	// The table that contains the users
	private JTable usersTable;
	// The currently highlighted name in the users table, if any
	private String nameSelected;

	public ChatroomFrame(JMSTopic topic, JMSUser user) {
		this.controller = new ChatroomController(this, topic, user);

		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel);
		basePanel.setLayout(null);

		messagesTableModel = controller.generateMessagesTableModel();
		messagesTable = new ColoredTable(messagesTableModel);
		messagesTable.removeColumn(messagesTable.getColumnModel().getColumn(3));
		messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Makes message table cells non editable.
		for (int i = 0; i < messagesTable.getColumnCount(); i++) {
			Class<?> columnClass = messagesTable.getColumnClass(i);
			messagesTable.setDefaultEditor(columnClass, null);
		}
		// Set column widths
		messagesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		messagesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		messagesTable.getColumnModel().getColumn(2).setPreferredWidth(800);
		// Highlight all private messages (this is called here instead of from
		// the controller because the table needed to be set up first)
		controller.highlightAllPMsInInitialTableModel();

		// The scrollpane that contains the messages table
		JScrollPane spMessages = new JScrollPane(messagesTable);
		spMessages.setBounds(10, 0, 620, 450);
		basePanel.add(spMessages);

		// The panel that will contain the users list and private messages
		// button
		JPanel usersPanel = new JPanel();
		usersPanel.setBounds(642, 0, 178, 450);
		basePanel.add(usersPanel);
		usersPanel.setLayout(null);

		// The scrollpane that will contain the users table
		JScrollPane spUsers = new JScrollPane((Component) null);
		spUsers.setBounds(10, 13, 156, 387);
		usersPanel.add(spUsers);

		// The users table and tablemodel
		usersTableModel = controller.generateUsersTableModel();
		usersTable = new JTable(usersTableModel);
		usersTable.removeColumn(usersTable.getColumnModel().getColumn(1));
		usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		usersTable.getSelectionModel().addListSelectionListener(new SharedUserListSelectionHandler());

		// Makes users table cells non editable.
		for (int i = 0; i < usersTable.getColumnCount(); i++) {
			Class<?> columnClass = usersTable.getColumnClass(i);
			usersTable.setDefaultEditor(columnClass, null);
		}

		// Add the users table to the users scrollpane
		spUsers.setColumnHeaderView(usersTable);

		// A button for sending private messages to users in the chatroom
		JButton btnSendPrivateMessage = new JButton("Send Private Message");
		btnSendPrivateMessage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				controller.setNameMessageTo(nameSelected);
				controller.handlePrivateMessageSendPressed();
			}
		});
		btnSendPrivateMessage.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnSendPrivateMessage.setBounds(10, 413, 156, 25);
		usersPanel.add(btnSendPrivateMessage);

		// A textfield for users to enter messages
		tfMessageInput = new JTextField();
		tfMessageInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					controller.handleSubmitPressed();
				}
			}
		});
		tfMessageInput.setBounds(10, 461, 692, 31);
		basePanel.add(tfMessageInput);
		tfMessageInput.setColumns(10);

		// A button for submitting messages
		JButton btnSubmitMessage = new JButton("Submit");
		btnSubmitMessage.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.handleSubmitPressed();
			}
		});
		btnSubmitMessage.setBounds(712, 461, 110, 31);
		basePanel.add(btnSubmitMessage);

		// Set the frame to dispose of itself when closed, but not to close the
		// whole application
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(850, 550));
		setTitle("Topic - " + topic.getName());
		setVisible(true);
	}

	public ColoredTable getMessagesTable() {
		return messagesTable;
	}

	public JTextField getTfMessageInput() {
		return tfMessageInput;
	}

	/**
	 * Adds to default dispose() behaviour. Notifies controller of window close
	 * and handles it accordingly
	 */
	@Override
	public void dispose() {
		controller.handleWindowClose();
	}

	/**
	 * Allows external classes to access this class's parent's dispose() method
	 */
	public void superDispose() {
		super.dispose();
	}

	/**
	 * A list selection handler for the Users List. Detects when a name has been
	 * clicked and records the index that is selected
	 */
	private class SharedUserListSelectionHandler implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();

			if (!lsm.isSelectionEmpty()) {
				int selectedRowIndex = lsm.getMinSelectionIndex();
				nameSelected = (String) usersTableModel.getValueAt(selectedRowIndex, 0);
			}
		}
	}
}
