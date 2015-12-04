package views;

import java.awt.Dimension;
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
import java.awt.Component;
import java.awt.Font;

public class ChatroomFrame extends JFrame {
	private static final long serialVersionUID = -6904280288906125276L;
	
	private DefaultTableModel messagesTableModel;
	private DefaultTableModel usersTableModel;
	private ChatroomController controller;
	private JTextField tfMessageInput;
	private ColoredTable messagesTable;
	private String nameSelected;
	private JTable usersTable;

	public ChatroomFrame(JMSTopic topic, JMSUser user) {
		// FIXME Create a "Message Owner" button.
		this.controller = new ChatroomController(this, topic, user);

		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);

		messagesTableModel = controller.generateMessagesTableModel();
		messagesTable = new ColoredTable(messagesTableModel);
		messagesTable.removeColumn(messagesTable.getColumnModel().getColumn(3));
		messagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Makes cells non editable.
		for (int i = 0; i < messagesTable.getColumnCount(); i++) {
			Class<?> columnClass = messagesTable.getColumnClass(i);
			messagesTable.setDefaultEditor(columnClass, null);
		}
		messagesTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		messagesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		messagesTable.getColumnModel().getColumn(2).setPreferredWidth(800);
		controller.highlightAllPMsInInitialTableModel();

		JScrollPane spMessages = new JScrollPane(messagesTable);
		spMessages.setBounds(10, 0, 620, 450);
		panel.add(spMessages);

		JPanel usersPanel = new JPanel();
		usersPanel.setBounds(642, 0, 178, 450);
		panel.add(usersPanel);
		usersPanel.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane((Component) null);
		scrollPane.setBounds(10, 13, 156, 387);
		usersPanel.add(scrollPane);
		
		usersTableModel = controller.generateUsersTableModel();
		usersTable = new JTable(usersTableModel);
		usersTable.removeColumn(usersTable.getColumnModel().getColumn(1));
		usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		usersTable.getSelectionModel().addListSelectionListener(new SharedUserListSelectionHandler());
		
		// Makes cells non editable.
		for (int i = 0; i < usersTable.getColumnCount(); i++) {
			Class<?> columnClass = usersTable.getColumnClass(i);
			usersTable.setDefaultEditor(columnClass, null);
		}
		
		scrollPane.setColumnHeaderView(usersTable);
		


		
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

		tfMessageInput = new JTextField();
		tfMessageInput.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					controller.handleSubmitPressed(null);
				}
			}
		});
		tfMessageInput.setBounds(10, 461, 692, 31);
		panel.add(tfMessageInput);
		tfMessageInput.setColumns(10);

		JButton btnSubmit = new JButton("Submit");
		btnSubmit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				controller.handleSubmitPressed(null);
			}
		});
		btnSubmit.setBounds(712, 461, 110, 31);
		panel.add(btnSubmit);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(850, 550));
		setTitle(topic.getName());
		setVisible(true);
	}

	public ColoredTable getMessagesTable() {
		return messagesTable;
	}

	public JTextField getTfMessageInput() {
		return tfMessageInput;
	}

	@Override
	public void dispose() {
		super.dispose();

		controller.handleWindowClose();
	}

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
