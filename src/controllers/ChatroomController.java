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
		registerMessageListener();
		registerUserAddedListener();
		registerUserRemovedListener();
		registerTopicRemovedListener();
	}

	public DefaultTableModel generateMessagesTableModel() {
		Object[] columns = { "Time Sent", "User", "Message", "Message ID" };
		List<JMSMessage> messages = messageService.getAllMessagesForUserInTopic(topic, user);

		for (int i = 0; i < messages.size(); i++) {
			JMSMessage message = messages.get(i);

			if (message.getTo() != null && StringUtils.isNotBlank(message.getTo().getName())) {
				rowsToHighlight.add(i);
			}
		}

		Object[][] data = {};

		if (messages != null && messages.size() > 0) {
			data = new Object[messages.size()][4];
			for (int i = 0; i < messages.size(); i++) {
				@SuppressWarnings("deprecation")
				String minutes = "" + messages.get(i).getSentDate().getMinutes();
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

		messagesTableModel = new DefaultTableModel(data, columns);

		return messagesTableModel;
	}

	public DefaultTableModel generateUsersTableModel() {
		Object[] columns = { "Users", "User ID" };
		List<JMSTopicUser> users = topicService.getAllTopicUsers(topic);

		Object[][] data = {};

		if (users != null && users.size() > 0) {
			data = new Object[users.size()][2];
			for (int i = 0; i < users.size(); i++) {
				data[i][0] = users.get(i).getUser().getName();
				data[i][1] = users.get(i).getUser().getId();
			}
		}

		usersTableModel = new DefaultTableModel(data, columns);

		return usersTableModel;
	}

	public void setNameMessageTo(String name) {
		this.nameSendingMessageTo = name;
	}

	public void handleSubmitPressed() {
		handleSubmitPressed(null);
	}

	private void handleSubmitPressed(String text) {
		JTextField tfMessageInput = frame.getTfMessageInput();

		// If text is not blank, it's a private message.
		if (StringUtils.isBlank(text)) {
			text = tfMessageInput.getText();
		}

		if (StringUtils.isNotBlank(text)) {
			boolean successfullyAddedToSpace = false;
			try {
				if (StringUtils.isBlank(nameSendingMessageTo)) {
					messageService
							.createMessage(new JMSMessage(topic, new Date(), user, null, UUID.randomUUID(), text));
					successfullyAddedToSpace = true;
				} else {
					String baseName = userService.getBaseNameFromName(nameSendingMessageTo);
					JMSUser userTo = userService.getUserByBaseName(baseName);
					text = "PM TO '" + userTo.getName() + "': " + text;

					messageService
							.createMessage(new JMSMessage(topic, new Date(), user, userTo, UUID.randomUUID(), text));
					successfullyAddedToSpace = true;

					// Reset this variable
					nameSendingMessageTo = null;
				}
			} catch (Exception e) {
				System.err.println("Failed to create public message in topic.");
				e.printStackTrace();

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

	public void colourBottomMessage() {
		ColoredTable messagesTable = frame.getMessagesTable();
		int lastRow = messagesTable.getRowCount() - 1;
		messagesTable.setRowColor(lastRow, Color.LIGHT_GRAY);
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
		try {
			topicRemovedRegistration.getLease().cancel();
			messageReceivedRegistration.getLease().cancel();
			userAddedRegistration.getLease().cancel();
			userRemovedRegistration.getLease().cancel();
		} catch (Exception e) {
			System.err.println("Failed to remove ChatroomController listener(s).");
		}		
	}

	public void handlePrivateMessageSendPressed() {
		if (StringUtils.isBlank(nameSendingMessageTo)) {
			JOptionPane.showMessageDialog(frame, "Please select a user to send a message to");

			return;
		}

		String messageToSend = getMessageToSend();

		if (StringUtils.isNotBlank(messageToSend)) {
			handleSubmitPressed(messageToSend);
		} else {
			JOptionPane.showMessageDialog(frame, "Message input blank.  No message sent.");
		}

	}

	public void highlightAllPMsInInitialTableModel() {
		ColoredTable messagesTable = frame.getMessagesTable();

		for (Integer i : rowsToHighlight) {
			messagesTable.setRowColor(i, Color.LIGHT_GRAY);
		}
	}

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
			System.err.println("Failed to get new message(s)");
			e.printStackTrace();
		}
	}

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

	private String getMessageToSend() {
		JPanel messageSendPanel = new JPanel();
		JTextField tfMessage = new JTextField(20);

		messageSendPanel.add(tfMessage);

		String[] options = new String[] { "Submit" };

		JOptionPane.showOptionDialog(null, messageSendPanel, "Message to send", JOptionPane.NO_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

		return new String(tfMessage.getText());
	}

	public DefaultTableModel getMessagesTableModel() {
		return messagesTableModel;
	}

	public DefaultTableModel getUsersTableModel() {
		return usersTableModel;
	}

	public void handleTopicDeleted() {
		messagesTableModel = null;

		JOptionPane.showMessageDialog(frame, "This topic (" + topic.getName()
				+ ") has been deleted by its owner.  The topic window will now close.");

		handleWindowClose();
	}

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
}
