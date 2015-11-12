package controllers;

import java.util.Date;
import java.util.UUID;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import models.JMSTopic;
import models.JMSUser;
import views.ChatroomFrame;

public class ChatroomController {
	//TODO Spam prevention
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
		
		messagesTableModel = new DefaultTableModel(data, columns);
		
		return messagesTableModel;
	}
	
	public DefaultTableModel generateUsersTableModel() {
		Object[] columns = { "Users", "User ID" };
		Object[][] data = { { "aUser", UUID.randomUUID()} };
		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		this.usersTableModel = tableModel;

		return tableModel;
	}

	public void handleSubmitPressed() {
		JTextField tfMessageInput = frame.getTfMessageInput();
		String text = tfMessageInput.getText();
		
		if(StringUtils.isNotBlank(text)){
			// TODO Add message to JavaSpace
			
			Object[] rowData = {new Date(System.currentTimeMillis()).toString(), user.getName(), text};
			messagesTableModel.addRow(rowData);
			
			scrollToBottomOfMessages();
			tfMessageInput.setText(null);
		}
	}

	private void scrollToBottomOfMessages() {
		JTable messagesTable = frame.getMessagesTable();
		messagesTable.scrollRectToVisible(messagesTable.getCellRect(messagesTable.getRowCount() - 1, 0, true));
	}
}
