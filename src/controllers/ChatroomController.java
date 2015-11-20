package controllers;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import models.JMSMessage;
import models.JMSTopic;
import models.JMSTopicUser;
import models.JMSUser;
import services.MessageService;
import services.TopicService;
import views.ChatroomFrame;

public class ChatroomController {
	//TODO Spam prevention
	private ChatroomFrame frame;
	private DefaultTableModel messagesTableModel;
	private DefaultTableModel usersTableModel;
	private MessageService messageService;
	private TopicService topicService;
	private JMSTopic topic;
	private JMSUser user;
	
	public ChatroomController(ChatroomFrame frame, JMSTopic topic, JMSUser user){
		this.frame = frame;
		this.topic = topic;
		this.user = user;
				
		this.messageService = MessageService.getMessageService();
		this.topicService = TopicService.getTopicService();
		
		markUserAsInTopic();
	}
	
	public DefaultTableModel generateMessagesTableModel() {
		Object[] columns = { "Date/Time", "User", "Message", "Message ID" };
		List<JMSMessage> messages = messageService.getAllMessagesForTopic(topic);
		
		Object[][] data = {};
		
		if(messages != null && messages.size() > 0){
			data = new Object[messages.size()][4];
			for(int i = 0; i < messages.size(); i++){
				data[i][0] = messages.get(i).getSentDate();
				data[i][1] = messages.get(i).getFrom().getName();
				data[i][2] = messages.get(i).getMessage();
				data[i][3] = messages.get(i).getId();
			}
		}

		messagesTableModel = new DefaultTableModel(data, columns);

		return messagesTableModel;
	}
	
	public DefaultTableModel generateUsersTableModel() {
		Object[] columns = { "Users", "User ID" };
		List<JMSTopicUser> users = topicService.getAllTopicUsers(topic);

		Object[][] data = {};
		
		if(users != null && users.size() > 0){
			data = new Object[users.size()][2];
			for(int i = 0; i < users.size(); i++){
				data[i][0] = users.get(i).getUser().getName();
				data[i][1] = users.get(i).getUser().getId();
			}
		}
		
		usersTableModel = new DefaultTableModel(data, columns);

		return usersTableModel;
	}

	public void handleSubmitPressed() {
		JTextField tfMessageInput = frame.getTfMessageInput();
		String text = tfMessageInput.getText();
		
		if(StringUtils.isNotBlank(text)){
			boolean successfullyAddedToSpace = false;
			try {
				messageService.createPublicMessage(new JMSMessage(topic, new Date(), user, null, UUID.randomUUID(), text));
				successfullyAddedToSpace = true;
			} catch (Exception e) {
				// TODO Finer error catching.
				// No real need to print stack trace at the moment.  This could happen for legitimate reasons.
				// TODO Topic deleted notification?
			}
			
			if(successfullyAddedToSpace){
				Object[] rowData = {new Date(System.currentTimeMillis()).toString(), user.getName(), text};
				messagesTableModel.addRow(rowData);
			
				scrollToBottomOfMessages();
				tfMessageInput.setText(null);
			} else {
				JOptionPane.showMessageDialog(frame, "Failed to send message to server.  Perhaps the owner has deleted the topic?");
			}
		}
	}

	private void scrollToBottomOfMessages() {
		JTable messagesTable = frame.getMessagesTable();
		messagesTable.scrollRectToVisible(messagesTable.getCellRect(messagesTable.getRowCount() - 1, 0, true));
	}

	public void markUserAsInTopic() {
		topicService.addTopicUser(topic, user);
	}

	public void handleWindowClose() {
		topicService.removeTopicUser(topic, user);
	}
}
