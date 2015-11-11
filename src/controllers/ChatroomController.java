package controllers;

import java.util.Date;
import java.util.UUID;

import javax.swing.table.DefaultTableModel;

import models.JMSTopic;
import models.JMSUser;
import views.ChatroomFrame;

public class ChatroomController {
	private ChatroomFrame frame;
	private DefaultTableModel messagesTableModel;
	private DefaultTableModel usersTableModel;
	private JMSTopic topic;
	private JMSUser user;
	
	public ChatroomController(ChatroomFrame frame, JMSTopic topic, JMSUser user){
		this.frame = frame;
		this.topic = topic;
		this.user = user;
	}
	
	public DefaultTableModel generateMessagesTableModel() {
		Object[] columns = { "Date/Time", "User", "Message", "Message ID" };
		Object[][] data = { { new Date(System.currentTimeMillis()).toString(), "aUser", "This is a test message", UUID.randomUUID()} };
		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		this.messagesTableModel = tableModel;

		return tableModel;
	}
	
	public DefaultTableModel generateUsersTableModel() {
		Object[] columns = { "Users", "User ID" };
		Object[][] data = { { "aUser", UUID.randomUUID()} };
		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		this.usersTableModel = tableModel;

		return tableModel;
	}

	public void handleSubmitPressed(String text) {
		// TODO Add message to JavaSpace
		
		Object[] rowData = {new Date(System.currentTimeMillis()).toString(), user.getName(), text};
		messagesTableModel.addRow(rowData);
	}
}
