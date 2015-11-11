package controllers;

import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import models.JMSTopic;
import models.JMSUser;
import views.MainMenuFrame;

public class MainMenuController {
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

	public void createTopic(String name) {
		JMSTopic topic = new JMSTopic(name, user, 1);

		// TODO Add topic to JavaSpace...
		boolean success = true;

		if (success) {
			Object[] rowData = { topic.getName(), topic.getOwner().getName(), topic.getUsers(),
					topic.getOwner().getId() };
			tableModel.addRow(rowData);
		} else {
			JOptionPane.showMessageDialog(frame, "Failed to create topic.  Topic name already exists");
		}

		// TODO Open topic window.
	}

	public void deleteTopic(int tableModelRow, UUID topicId) {
		// TODO Get topic from JavaSpace to verify it exists.
		JMSTopic topic = new JMSTopic();
		topic.setId(topicId);
		// FIXME - This currently bypasses the authentication in the if check
		// below. MUST get Topic from JavaSpace with a template that has the
		// topicId
		topic.setOwner(user);
		topic.setName(user.getName());

		if (user.equals(topic.getOwner())) {
			// TODO Remove topic from JavaSpace

			tableModel.removeRow(tableModelRow);
		} else {
			// TODO Unauthorized exception.
		}
	}

	public DefaultTableModel generateTableModelWithTestData() {
		Object[] columns = { "Topic", "Owner", "User Count", "Owner ID", "Topic ID" };
		Object[][] data = { { "Topic1", "Owner1", "4", 44, 44 }, { "Topic2", "Owner2", "6", 44, 44 } };
		DefaultTableModel tableModel = new DefaultTableModel(data, columns);

		this.tableModel = tableModel;

		return tableModel;
	}

	public void joinTopic(JMSTopic topic) {
		// TODO implement method...
	}

	public void logout() {
		// TODO implement method...
	}
}
