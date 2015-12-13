package listeners;

import java.io.Serializable;
import java.util.Date;

import controllers.ChatroomController;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSUser;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.space.AvailabilityEvent;

/**
 * Listens for any messages being added to a given topic for a given user. Only
 * shows users messages that are to/from them.
 * 
 * @author Jonathan Sterling
 *
 */
public class MessageRemoteEventListener implements RemoteEventListener, Serializable {
	private static final long serialVersionUID = 7526472345622976341L;

	private final ChatroomController controller;
	private final JMSTopic topic;
	private final JMSUser user;

	public MessageRemoteEventListener(ChatroomController controller, JMSTopic topic, JMSUser user) {
		super();

		this.controller = controller;
		this.topic = topic;
		this.user = user;
	}

	/**
	 * Listens for messages that are created for the current topic.
	 */
	@SuppressWarnings("deprecation")
	public void notify(RemoteEvent event) {
		try {
			// Get the message that triggered the notify method.
			AvailabilityEvent availEvent = (AvailabilityEvent) event;
			JMSMessage message = (JMSMessage) availEvent.getEntry();

			// If the message is either public, for the current user, or from
			// the current user
			if (message.getTo() == null || message.getTo().getId().equals(user.getId())
					|| message.getFrom().getId().equals(user.getId())) {
				// Get the relevant message details (to/from/text/etc.)
				JMSUser userFrom = message.getFrom();
				String messageText = message.getMessage();

				// If the message was from the current user, make who they sent
				// it to explicit
				if (userFrom.getId().equals(user.getId()) && message.getTo() != null) {
					messageText = "PM TO '" + message.getTo().getName() + "': " + messageText;
				}

				// Get the date/time the message was sent and format that into a
				// custom timestamp
				Date sentDate = message.getSentDate();

				String minutes = "" + sentDate.getMinutes();

				// If the minutes value is 0-9, prepend a 0 to it so we avoid a
				// time such as 1:3 instead of 1:03
				if (minutes.length() == 1) {
					minutes = "0" + minutes;
				}
				String hours = "" + sentDate.getHours();
				String timestamp = hours + ":" + minutes;

				// Add the message data to an Object array
				Object[] rowData = { timestamp, userFrom.getName(), messageText };
				if (controller.getMessagesTableModel() != null) {
					// Add the message data Object array to the messages table.
					controller.getMessagesTableModel().addRow(rowData);

					// If it's a PM, mark it as such...
					if (message.getTo() != null) {
						controller.highlightBottomMessage();
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to run notify method for Messages");
			e.printStackTrace();
		}
	}
}