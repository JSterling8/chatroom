package controllers;

import java.awt.Frame;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import listeners.TopicAddedRemoteEventListener;
import listeners.TopicRemovedRemoteEventListener;
import models.JMSTopic;
import models.JMSTopicDeleted;
import models.JMSTopicUser;
import models.JMSUser;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
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

/**
 * Handles all of the logic of a given MainMenuFrame for a given user
 * 
 * @author Jonathan Sterling
 *
 */
public class MainMenuController {
	private static TopicService topicService = TopicService.getTopicService();

	private MainMenuFrame frame;
	private DefaultTableModel tableModel;
	private JMSUser user;
	private RemoteEventListener topicAddedStub;
	private RemoteEventListener topicRemovedStub;
	private EventRegistration topicAddedRegistration;
	private EventRegistration topicRemovedRegistration;

	public MainMenuController(MainMenuFrame frame, JMSUser user) {
		this.frame = frame;
		this.user = user;

		// Listen for topics being created and deleted.
		registerTopicAddedListener();
		registerTopicRemovedListener();
	}

	/**
	 * Handles the topic creation button being pressed.
	 */
	public void handleCreateButtonPressed() {
		// Ask the user for a topic name.
		String topicName = JOptionPane.showInputDialog(frame, "Enter a topic name: ");
		if (StringUtils.isNotBlank(topicName)) {
			// Create the topic.
			createTopic(topicName);
		}
	}

	/**
	 * Join a given topic ID
	 * 
	 * @param topicId
	 *            The UUID of the topic to join.
	 */
	public void handleJoinTopicPressed(UUID topicId) {
		JMSTopic topic = topicService.getTopicById(topicId);
		if (topic != null) {
			List<JMSTopicUser> topicUsers = topicService.getAllTopicUsers(topic);

			// Check if this user already has a chat window open for this topic
			for (JMSTopicUser topicUser : topicUsers) {
				if (topicUser.getUser().equals(user)) {
					JOptionPane.showMessageDialog(frame, "You are already in this topic");

					return;
				}
			}

			// If the topic the user wishes to join exists, and they're not
			// already in it open up a new ChatroomFrame for the topic
			new ChatroomFrame(topic, user);
		} else {
			// If the topic the user wishes to join does not exist, show an
			// error
			JOptionPane.showMessageDialog(frame,
					"Failed to Join Topic.  " + "Please refresh the topic list and try again");
		}
	}

	/**
	 * If a user tries to delete a topic, this method ensures they have the
	 * correct permissions, then removes the topic if they do.
	 * 
	 * @param tableModelRow
	 *            The row in the topics list that the user opted to delete.
	 * @param topicId
	 *            The UUID of the topic to be deleted.
	 */
	public void handleDeleteTopicPressed(int tableModelRow, UUID topicId) {
		JMSTopic topic = topicService.getTopicById(topicId);

		if (topic != null) {
			if (user.equals(topic.getOwner())) {
				// If the topic exists, and the current user owns it, delete the
				// topic.
				topicService.deleteTopic(topic);
			} else {
				// If the topic exists, and the current user does NOT own it,
				// show an error message.
				JOptionPane.showInternalMessageDialog(frame,
						"Failed to delete topic.  " + "You are not the topic owner", "Topic Deletion Failed",
						JOptionPane.ERROR_MESSAGE, null);
			}
		} else {
			// If the topic does not exist, show an error message.
			JOptionPane.showInternalMessageDialog(frame,
					"Failed to delete topic.  " + "Topic does not exist.  Please try refreshing the topic list.",
					"Topic Deletion Failed", JOptionPane.ERROR_MESSAGE, null);
		}
	}

	/**
	 * Gets all of the topics in the space and puts them into a
	 * DefaultTableModel
	 * 
	 * @return A DefaultTableModel containing all of the topics in the space.
	 */
	public DefaultTableModel generateTopicTableModel() {
		Object[] columns = { "Topic", "Owner", "Owner ID", "Topic ID" };
		List<JMSTopic> topics = topicService.getAllTopics();

		Object[][] data = {};

		// Put all of the topics into an array of arrays.
		if (topics != null && topics.size() > 0) {
			data = new Object[topics.size()][5];
			for (int i = 0; i < topics.size(); i++) {
				data[i][0] = topics.get(i).getName();
				data[i][1] = topics.get(i).getOwner().getName();
				data[i][2] = topics.get(i).getOwner().getId();
				data[i][3] = topics.get(i).getId();
			}
		}

		// Generate a table model from the array of arrays.
		tableModel = new DefaultTableModel(data, columns);

		return tableModel;
	}

	/**
	 * A simple getter for the DefaultTableModel of topics.
	 * 
	 * @return The DefaultTableModel of topics
	 */
	public DefaultTableModel getTopicTableModel() {
		return tableModel;
	}

	/**
	 * When a user clicks logout, this method closes any open topic chatrooms,
	 * closes the main menu, and opens a new login window
	 */
	public void logout() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Frame[] frames = Frame.getFrames();

				for (Frame frame : frames) {
					if(frame.isVisible()){
						frame.setVisible(false);
						frame.dispose();
					}
				}

				new LoginFrame();
			}
		});
	}

	/**
	 * Removes the topic added/deleted listeners from the space.
	 */
	public void cancelLeases() {
		try {
			topicAddedRegistration.getLease().cancel();
			topicRemovedRegistration.getLease().cancel();
		} catch (UnknownLeaseException | RemoteException | NullPointerException e) {
			System.err.println("Failed to cancel MainMenuController lease(s)");
		}
	}

	/**
	 * Gets all of the topics in the space and shows them to the user.
	 * 
	 * @param table
	 *            A JTable to show the topics in.
	 */
	public void updateTopicList(JTable table) {
		tableModel = generateTopicTableModel();
		table.setModel(tableModel);
		table.removeColumn(table.getColumnModel().getColumn(MainMenuFrame.COLUMN_INDEX_OF_TOPIC_ID));
		table.removeColumn(table.getColumnModel().getColumn(MainMenuFrame.COLUMN_INDEX_OF_TOPIC_OWNER_ID));
	}

	/**
	 * Sets up listener for topics being added to the space.
	 */
	private void registerTopicAddedListener() {
		JavaSpace05 space = SpaceService.getSpace();
		JMSTopic template = new JMSTopic();
		ArrayList<JMSTopic> templates = new ArrayList<JMSTopic>(1);
		templates.add(template);

		try {
			Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(),
					false, true);

			TopicAddedRemoteEventListener eventListener = new TopicAddedRemoteEventListener(this);
			topicAddedStub = (RemoteEventListener) myDefaultExporter.export(eventListener);

			topicAddedRegistration = space.registerForAvailabilityEvent(templates, null, true, topicAddedStub,
					Lease.FOREVER, null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to get new topic(s)");
			e.printStackTrace();
		}
	}

	/**
	 * Sets up listener for topics being removed from the space.
	 */
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

			topicRemovedRegistration = space.registerForAvailabilityEvent(templates, null, true, topicRemovedStub,
					Lease.FOREVER, null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to get new topic(s)");
			e.printStackTrace();
		}
	}

	/**
	 * Creates a topic with a given name.
	 * 
	 * @param name
	 *            The desired name of the topic to create.
	 */
	private void createTopic(String name) {
		JMSTopic topic = new JMSTopic(name, user);

		try {
			topicService.createTopic(topic);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Failed to create topic.  Topic name already exists");
		}
	}
}
