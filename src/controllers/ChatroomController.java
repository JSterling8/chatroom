package controllers;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.StringUtils;

import exceptions.ResourceNotFoundException;
import listeners.MessageRemoteEventListener;
import listeners.TopicRemovedRemoteEventListener;
import listeners.TopicUserAddedRemoteEventListener;
import listeners.TopicUserRemovedRemoteEventListener;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSTopicDeleted;
import models.JMSTopicUser;
import models.JMSTopicUserRemoved;
import models.JMSUser;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.export.Exporter;
import net.jini.jeri.BasicILFactory;
import net.jini.jeri.BasicJeriExporter;
import net.jini.jeri.tcp.TcpServerEndpoint;
import net.jini.space.JavaSpace05;
import services.MessageService;
import services.SpaceService;
import services.TopicService;
import services.UserService;
import views.ChatroomFrame;
import views.ColoredTable;

/**
 * Controls a given ChatroomFrame for a given user and topic.
 * 
 * @author Jonathan Sterling
 *
 */
public class ChatroomController implements Serializable {
	private static final long serialVersionUID = 523026449422229593L;
	private static final UserService userService = UserService.getUserService();

	private ChatroomFrame frame;
	private DefaultTableModel messagesTableModel;
	private DefaultTableModel usersTableModel;
	private MessageService messageService;
	private TopicService topicService;
	private JMSTopic topic;
	private JMSUser user;
	private String nameSendingMessageTo;
	private List<Integer> rowsToHighlight = new ArrayList<Integer>();
	private RemoteEventListener messageReceivedStub;
	private RemoteEventListener usersAddedStub;
	private RemoteEventListener usersRemovedStub;
	private RemoteEventListener topicRemovedStub;
	private EventRegistration topicRemovedRegistration;
	private EventRegistration messageReceivedRegistration;
	private EventRegistration userAddedRegistration;
	private EventRegistration userRemovedRegistration;

	public ChatroomController(ChatroomFrame frame, JMSTopic topic, JMSUser user) {
		this.frame = frame;
		this.topic = topic;
		this.user = user;

		this.messageService = MessageService.getMessageService();
		this.topicService = TopicService.getTopicService();

		markUserAsInTopic();

		// Listen for incoming messages, users joining/leaving, and the topic
		// being deleted.
		registerMessageListener();
		registerUserAddedListener();
		registerUserRemovedListener();
		registerTopicRemovedListener();
	}

	/**
	 * Gets all messages for its topic and puts them into a DefaultTableModel.
	 * 
	 * @return A DefaultTableModel containing all of a Chatroom's messages.s
	 */
	public DefaultTableModel generateMessagesTableModel() {
		Object[] columns = { "Time Sent", "User", "Message", "Message ID" };
		List<JMSMessage> messages = messageService.getAllMessagesForUserInTopic(topic, user);

		for (int i = 0; i < messages.size(); i++) {
			JMSMessage message = messages.get(i);

			// If the message has a non-null "to" field, then it is a private
			// message and should be highlighted.
			if (message.getTo() != null) {
				rowsToHighlight.add(i);
			}
		}

		Object[][] data = {};

		if (messages != null && messages.size() > 0) {
			data = new Object[messages.size()][4];
			// Put all of the messages for the topic into an array of arrays
			for (int i = 0; i < messages.size(); i++) {
				@SuppressWarnings("deprecation")
				String minutes = "" + messages.get(i).getSentDate().getMinutes();
				// Ensures 1:03 is not shown as 1:3
				if (minutes.length() == 1) {
					minutes = "0" + minutes;
				}
				@SuppressWarnings("deprecation")
				String hours = "" + messages.get(i).getSentDate().getHours();
				data[i][0] = hours + ":" + minutes;
				data[i][1] = messages.get(i).getFrom().getName();
				data[i][2] = messages.get(i).getMessage();
				data[i][3] = messages.get(i).getId();
			}
		}

		// Add the array of data arrays (of message info) to the table model
		// that's shown to the user.
		messagesTableModel = new DefaultTableModel(data, columns);

		return messagesTableModel;
	}

	/**
	 * Gets all of the users in a topic at the time the topic was opened by the
	 * user. Adds all of those users to a DefaultTableModel
	 * 
	 * @return A DefaultTableModel of all users currently in the topic.
	 */
	public DefaultTableModel generateUsersTableModel() {
		Object[] columns = { "Users", "User ID" };
		List<JMSTopicUser> users = topicService.getAllTopicUsers(topic);

		Object[][] data = {};

		// Loop through all users in the topic and put them into an array of
		// arrays.
		if (users != null && users.size() > 0) {
			data = new Object[users.size()][2];
			for (int i = 0; i < users.size(); i++) {
				data[i][0] = users.get(i).getUser().getName();
				data[i][1] = users.get(i).getUser().getId();
			}
		}

		// Put the array of data arrays into a DefaultTableModel
		usersTableModel = new DefaultTableModel(data, columns);

		return usersTableModel;
	}

	/**
	 * Set the name of the person the next private message should be sent to.
	 * 
	 * @param name
	 *            The name of the person to send the next private message to.
	 */
	public void setNameMessageTo(String name) {
		this.nameSendingMessageTo = name;
	}

	/**
	 * Overloaded method for handleSubmitPressed(String text).
	 */
	public void handleSubmitPressed() {
		handleSubmitPressed(null);
	}

	/**
	 * Highlights the bottom-most row in the messages table grey.
	 */
	public void highlightBottomMessage() {
		ColoredTable messagesTable = frame.getMessagesTable();
		int lastRow = messagesTable.getRowCount() - 1;
		messagesTable.setRowColor(lastRow, Color.LIGHT_GRAY);
	}

	/**
	 * Register the user as in the topic so other clients can see them.
	 */
	public void markUserAsInTopic() {
		topicService.addTopicUser(topic, user);
	}

	/**
	 * Removes the user from the topic and cancels all event registration leases
	 */
	public void handleWindowClose() {
		topicService.removeTopicUser(topic, user);
		try {
			topicRemovedRegistration.getLease().cancel();
			messageReceivedRegistration.getLease().cancel();
			userAddedRegistration.getLease().cancel();
			userRemovedRegistration.getLease().cancel();
		} catch (Exception e) {
			System.err.println("Failed to remove ChatroomController listener(s).");
		}
	}

	/**
	 * Handles the "Send Private Message" button being pressed
	 */
	public void handlePrivateMessageSendPressed() {
		// Check that the user has selected someone to send a message to.
		if (StringUtils.isBlank(nameSendingMessageTo)) {
			JOptionPane.showMessageDialog(frame, "Please select a user to send a message to");

			return;
		}

		String privateMessageToSend = getPrivateMessageToSend();

		// Ensure that the message being sent is not null or blank (0 length or
		// all spaces)
		if (StringUtils.isNotBlank(privateMessageToSend)) {
			handleSubmitPressed(privateMessageToSend);
		} else {
			JOptionPane.showMessageDialog(frame, "Message input blank.  No message sent.");
		}

	}

	/**
	 * Highlights all PMs to/from a given user.
	 */
	public void highlightAllPMsInInitialTableModel() {
		ColoredTable messagesTable = frame.getMessagesTable();

		for (Integer i : rowsToHighlight) {
			messagesTable.setRowColor(i, Color.LIGHT_GRAY);
		}
	}

	/**
	 * Getter for this chatroom's messages DefaultTableModel.
	 * 
	 * @return This chatroom's messages DefaultTableModel.
	 */
	public DefaultTableModel getMessagesTableModel() {
		return messagesTableModel;
	}

	/**
	 * Getter for this chatroom's users DefaultTableModel.
	 * 
	 * @return This chatroom's users DefaultTableModel.
	 */
	public DefaultTableModel getUsersTableModel() {
		return usersTableModel;
	}

	/**
	 * If the topic is deleted, notify the user then close the topic's window.
	 */
	public void handleTopicDeleted() {
		messagesTableModel = null;

		JOptionPane.showMessageDialog(frame, "This topic (" + topic.getName()
				+ ") has been deleted by its owner.  The topic window will now close.");

		handleWindowClose();
	}

	/**
	 * When a user opts to send a private message, this method is called.
	 * 
	 * It creates a message dialogue box that asks for the message to be sent.
	 * 
	 * @return The private message to be sent.
	 */
	private String getPrivateMessageToSend() {
		JPanel messageSendPanel = new JPanel();
		JTextField tfMessage = new JTextField(20);

		messageSendPanel.add(tfMessage);

		String[] options = new String[] { "Submit" };

		JOptionPane.showOptionDialog(null, messageSendPanel, "Private Message to Send", JOptionPane.NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		return new String(tfMessage.getText());
	}

	/**
	 * Handles both public and private message sending.
	 * 
	 * @param text
	 *            The message to send.
	 */
	private void handleSubmitPressed(String text) {
		JTextField tfMessageInput = frame.getTfMessageInput();

		// If text is not blank, it's a private message. If it is blank, we need
		// to grab the text from the message input box
		if (StringUtils.isBlank(text)) {
			text = tfMessageInput.getText();
		}

		// Makes sure users don't send stupidly long messages.
		if (text.length() > 1000) {
			JOptionPane.showMessageDialog(frame, "Messages must be less than 1000 characters.");

			return;
		}

		// Check that the text to send is not null or blank.
		if (StringUtils.isNotBlank(text)) {
			boolean successfullyAddedToSpace = false;
			try {
				// If there is no user in particular to send the message to,
				// it's a public message.
				if (StringUtils.isBlank(nameSendingMessageTo)) {
					messageService.sendMessage(new JMSMessage(topic, new Date(), user, null, UUID.randomUUID(), text));

					// If something goes wrong, the next line won't be called
					// (as we'll be in the catch block). So this is how we
					// know nothing went wrong
					successfullyAddedToSpace = true;
				} else {
					String baseName = userService.getBaseNameFromName(nameSendingMessageTo);
					JMSUser userTo = userService.getUserByBaseName(baseName);

					if (userTo == null) {
						throw new ResourceNotFoundException("User sending message to does not exist.");
					}

					messageService
							.sendMessage(new JMSMessage(topic, new Date(), user, userTo, UUID.randomUUID(), text));
					successfullyAddedToSpace = true;
				}
			} catch (Exception e) {
				System.err.println("Failed to create message in topic.");
				e.printStackTrace();
			} finally {
				// Reset this variable for next message send attempt
				nameSendingMessageTo = null;
			}

			if (successfullyAddedToSpace) {
				scrollToBottomOfMessages();
				tfMessageInput.setText(null);
			} else {
				JOptionPane.showMessageDialog(frame,
						"Failed to send message to server.  Perhaps the owner has deleted the topic?");
			}
		}
	}

	/**
	 * Sets up listener for the current topic's deletion.
	 */
	private void registerTopicRemovedListener() {
		JavaSpace05 space = SpaceService.getSpace();
		JMSTopicDeleted template = new JMSTopicDeleted(topic);
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
					Lease.FOREVER, // Should maybe not be forever?
					null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to get new topic(s)");
			e.printStackTrace();
		}
	}

	/**
	 * Sets up listener for messages that are sent in the current topic.
	 */
	public void registerMessageListener() {
		JavaSpace05 space = SpaceService.getSpace();
		JMSMessage template = new JMSMessage(topic);
		ArrayList<JMSMessage> templates = new ArrayList<JMSMessage>(1);
		templates.add(template);

		try {
			// create the exporter
			Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(),
					false, true);

			// register this as a remote object
			// and get a reference to the 'stub'
			MessageRemoteEventListener eventListener = new MessageRemoteEventListener(this, topic, user);
			messageReceivedStub = (RemoteEventListener) myDefaultExporter.export(eventListener);

			messageReceivedRegistration = space.registerForAvailabilityEvent(templates, null, true, messageReceivedStub,
					Lease.FOREVER, null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to setup message listener.");
			e.printStackTrace();
		}
	}

	/**
	 * Sets up listener for users that join the current topic.
	 */
	private void registerUserAddedListener() {
		JavaSpace05 space = SpaceService.getSpace();
		JMSTopicUser template = new JMSTopicUser(topic);
		ArrayList<JMSTopicUser> templates = new ArrayList<JMSTopicUser>(1);
		templates.add(template);

		try {
			// create the exporter
			Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(),
					false, true);

			// register this as a remote object
			// and get a reference to the 'stub'
			TopicUserAddedRemoteEventListener eventListener = new TopicUserAddedRemoteEventListener(this);
			usersAddedStub = (RemoteEventListener) myDefaultExporter.export(eventListener);

			userAddedRegistration = space.registerForAvailabilityEvent(templates, null, true, usersAddedStub,
					Lease.FOREVER, null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to get new user(s)");
			e.printStackTrace();
		}
	}

	/**
	 * Sets up listener for users that leave the current topic.
	 */
	private void registerUserRemovedListener() {
		JavaSpace05 space = SpaceService.getSpace();
		JMSTopicUserRemoved template = new JMSTopicUserRemoved(topic);
		ArrayList<JMSTopicUserRemoved> templates = new ArrayList<JMSTopicUserRemoved>(1);
		templates.add(template);

		try {
			// create the exporter
			Exporter myDefaultExporter = new BasicJeriExporter(TcpServerEndpoint.getInstance(0), new BasicILFactory(),
					false, true);

			// register this as a remote object
			// and get a reference to the 'stub'
			TopicUserRemovedRemoteEventListener eventListener = new TopicUserRemovedRemoteEventListener(this);
			usersRemovedStub = (RemoteEventListener) myDefaultExporter.export(eventListener);

			userRemovedRegistration = space.registerForAvailabilityEvent(templates, null, true, usersRemovedStub,
					Lease.FOREVER, null);
		} catch (TransactionException | IOException e) {
			System.err.println("Failed to remove user(s)");
			e.printStackTrace();
		}
	}

	/**
	 * Moves the current view down so the most recent message is visible.
	 */
	private void scrollToBottomOfMessages() {
		JTable messagesTable = frame.getMessagesTable();
		messagesTable.scrollRectToVisible(messagesTable.getCellRect(messagesTable.getRowCount() - 1, 0, true));
	}
}
