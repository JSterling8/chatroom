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

	@SuppressWarnings("deprecation")
	public void notify(RemoteEvent event) {
		try {
			AvailabilityEvent availEvent = (AvailabilityEvent) event;
			JMSMessage message = (JMSMessage) availEvent.getEntry();

			if (message.getTo() == null || message.getTo().getId().equals(user.getId())
					|| message.getFrom().getId().equals(user.getId())) {
				JMSUser userFrom = message.getFrom();
				String messageText = message.getMessage();

				if (userFrom.getId().equals(user.getId()) && message.getTo() != null) {
					messageText = "PM TO '" + message.getTo().getName() + "': " + messageText;
				}
				Date sentDate = message.getSentDate();

				String minutes = "" + sentDate.getMinutes();
				if (minutes.length() == 1) {
					minutes = "0" + minutes;
				}
				String hours = "" + sentDate.getHours();
				String timestamp = hours + ":" + minutes;

				Object[] rowData = { timestamp, userFrom.getName(), messageText };
				if (controller.getMessagesTableModel() != null) {
					controller.getMessagesTableModel().addRow(rowData);

					// If it's a PM, mark it as such...
					if (message.getTo() != null) {
						controller.highlightBottomMessage();
					}
				}
			} else {
				// Not for us. Ignore...
			}
		} catch (Exception e) {
			System.err.println("Failed to run notify method for Messages");
			e.printStackTrace();
		}
	}
}