package controllers;

import java.awt.Frame;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import listeners.TopicRemoteEventListener;
import listeners.TopicRemovedRemoteEventListener;
import models.JMSTopic;
import models.JMSTopicDeleted;
import models.JMSUser;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace05;
import services.SpaceService;
import services.TopicService;
import views.ChatroomFrame;
import views.LoginFrame;
import views.MainMenuFrame;

public class MainMenuController {
	private static TopicService topicService = TopicService.getTopicService();

	private MainMenuFrame frame;
	private DefaultTableModel tableModel;
	private JMSUser user;
	private RemoteEventListener topicAddedStub;
	private RemoteEventListener topicRemovedStub;

	public MainMenuController(MainMenuFrame frame, JMSUser user) {
		this.frame = frame;
		this.user = user;
		
		registerTopicAddedListener();
		registerTopicRemovedListener();
	}

	private void registerTopicAddedListener() {
		JavaSpace05 space = SpaceService.getSpace();
		JMSTopic template = new JMSTopic();
		ArrayList<JMSTopic> templates = new ArrayList<JMSTopic>(1);
		templates.add(template);
		
		try {
			// create the exporter
			Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(),
								false, true);
		
			// register this as a remote object
			// and get a reference to the 'stub'
			TopicRemoteEventListener eventListener = new TopicRemoteEventListener(this);
			topicAddedStub = (RemoteEventListener) myDefaultExporter.export(eventListener);
				
			space.registerForAvailabilityEvent(templates, 
					null, 
					true, 
					topicAddedStub, 
					Lease.FOREVER, // Should maybe not be forever?
					null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to get new topic(s)");
			e.printStackTrace();
		}
	}
	
	private void registerTopicRemovedListener() {
		JavaSpace05 space = SpaceService.getSpace();
		JMSTopicDeleted template = new JMSTopicDeleted();
		ArrayList<JMSTopicDeleted> templates = new ArrayList<JMSTopicDeleted>(1);
		templates.add(template);
		
		try {
			// create the exporter
			Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(),
								false, true);
		
			// register this as a remote object
			// and get a reference to the 'stub'
			TopicRemovedRemoteEventListener eventListener = new TopicRemovedRemoteEventListener(this);
			topicRemovedStub = (RemoteEventListener) myDefaultExporter.export(eventListener);
				
			space.registerForAvailabilityEvent(templates, 
					null, 
					true, 
					topicRemovedStub, 
					Lease.FOREVER, // Should maybe not be forever?
					null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to get new topic(s)");
			e.printStackTrace();
		}
	}

	public void handleCreateButtonPressed() {
		String topicName = JOptionPane.showInputDialog(frame, "Enter a topic name: ");
		if (StringUtils.isNotBlank(topicName)) {
			createTopic(topicName);
		}
	}

	public void handleJoinTopicPressed(UUID topicId) {
		JMSTopic topic = topicService.getTopicById(topicId);

		new ChatroomFrame(topic, user);
	}

	public void handleDeleteTopicPressed(int tableModelRow, UUID topicId) {
		JMSTopic topic = topicService.getTopicById(topicId);

		if (user.equals(topic.getOwner())) {
			topicService.deleteTopic(topic);
		} else {
			JOptionPane.showInternalMessageDialog(frame, "Failed to delete topic.  " + "You are not the topic owner",
					"Topic Deletion Failed", JOptionPane.ERROR_MESSAGE, null);
		}
	}
	
	public DefaultTableModel getTopicTableModel() {
		return tableModel;
	}

	public DefaultTableModel generateTopicTableModel() {
		Object[] columns = { "Topic", "Owner", "User Count", "Owner ID", "Topic ID" };
		List<JMSTopic> topics = topicService.getAllTopics();

		Object[][] data = {};

		if (topics != null && topics.size() > 0) {
			data = new Object[topics.size()][5];
			for (int i = 0; i < topics.size(); i++) {
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

	public void logout() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Frame[] frames = Frame.getFrames();

				for (Frame frame : frames) {
					frame.setVisible(false);
					frame.dispose();
				}

				new LoginFrame();
			}
		});
	}

	private void createTopic(String name) {
		JMSTopic topic = new JMSTopic(name, user, 1);

		try {
			topicService.createTopic(topic);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Failed to create topic.  Topic name already exists");
		}
	}

	public void updateTopicList(JTable table) {
		tableModel = generateTopicTableModel();
		table.setModel(tableModel);
		table.removeColumn(table.getColumnModel().getColumn(MainMenuFrame.COLUMN_INDEX_OF_TOPIC_OWNER_ID));
		table.removeColumn(table.getColumnModel().getColumn(MainMenuFrame.COLUMN_INDEX_OF_TOPIC_ID - 1));  // -1 is because index 4 becomes index 3 after the column in the line above is removed
	}
}
