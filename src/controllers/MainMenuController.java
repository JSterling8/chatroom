package controllers;

import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import models.JMSTopic;
import models.JMSUser;
import services.TopicService;
import views.ChatroomFrame;
import views.LoginFrame;
import views.MainMenuFrame;

public class MainMenuController {
	private static TopicService topicService = TopicService.getTopicService();
	
	private MainMenuFrame frame;
	private DefaultTableModel tableModel;
	private JMSUser user;

	public MainMenuController(MainMenuFrame frame, JMSUser user) {
		this.frame = frame;
		this.user = user;
	}

	public void handleCreateButtonPressed() {
		String topicName = JOptionPane.showInputDialog(frame, "Enter a topic name: ");
		if (StringUtils.isNotBlank(topicName)) {
			createTopic(topicName);
		}
	}
	
	public void handleJoinTopicPressed(UUID topicId) {
		// TODO Get topic from JavaSpace to verify it exists.
		JMSTopic topic = new JMSTopic();
		topic.setId(topicId);
		topic.setName("TestTopic");
		topic.setOwner(user);
		topic.setUsers(2);

		ChatroomFrame chatroomFrame = new ChatroomFrame(topic, user);
	}

	public void handleDeleteTopicPressed(int tableModelRow, UUID topicId) {
		JMSTopic topic = topicService.getTopicById(topicId);

		if (user.equals(topic.getOwner())) {
			topicService.deleteTopic(topic);

			tableModel.removeRow(tableModelRow);
		} else {
			// TODO Unauthorized exception.
		}
	}

	public DefaultTableModel generateTopicTableModel() {
		Object[] columns = { "Topic", "Owner", "User Count", "Owner ID", "Topic ID" };
		List<JMSTopic> topics = topicService.getAllTopics();
		
		Object[][] data = {};
		
		if(topics != null && topics.size() > 0){
			data = new Object[topics.size()][5];
			for(int i = 0; i < topics.size(); i++){
				data[i][0] = topics.get(i).getName();
				data[i][1] = topics.get(i).getOwner().getName();
				data[i][2] = topics.get(i).getUsers();
				data[i][3] = topics.get(i).getOwner().getId();
				data[i][4] = topics.get(i).getId();
			}
		}

		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		this.tableModel = tableModel;

		return tableModel;
	}

	public void joinTopic(JMSTopic topic) {
		// TODO implement method...
	}

	public void logout() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame.setVisible(false);
				frame.dispose();

				new LoginFrame();
			}
		});
	}
	
	private void createTopic(String name) {
		JMSTopic topic = new JMSTopic(name, user, 1);
		boolean success = true;

		try{
			topicService.createTopic(topic);
		} catch (Exception e){
			success = false;
		}
		
		if (success) {
			Object[] rowData = { topic.getName(), topic.getOwner().getName(), topic.getUsers(),
					topic.getOwner().getId(), topic.getId() };
			tableModel.addRow(rowData);
		} else {
			JOptionPane.showMessageDialog(frame, "Failed to create topic.  Topic name already exists");
		}

		joinTopic(topic);
	}
}
