package controllers;

import java.util.UUID;

import javax.swing.table.DefaultTableModel;

import views.ChatroomFrame;

public class ChatroomController {
	private ChatroomFrame frame;
	private DefaultTableModel messagesTableModel;
	
	public ChatroomController(ChatroomFrame frame){
		this.frame = frame;
	}
	
	public DefaultTableModel generateMessagesTableModel() {
		Object[] columns = { "Date/Time", "User", "Message", "Message ID" };
		Object[][] data = { { "Nov 11 2015", "aUser", "This is a test message", UUID.randomUUID()}, { "Topic2", "Owner2", "6", 44, 44 } };
		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		this.messagesTableModel = tableModel;

		return tableModel;
	}
	
	public DefaultTableModel generateUsersTableModel() {
		Object[] columns = { "Users", "User ID" };
		Object[][] data = { { "aUser", UUID.randomUUID()} };
		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		this.messagesTableModel = tableModel;

		return tableModel;
	}
}
