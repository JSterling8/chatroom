package listeners;

import java.io.Serializable;

import controllers.MainMenuController;
import models.JMSTopic;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.space.AvailabilityEvent;

/**
 * Listens for a topic being added to the space for a given MainMenuController.
 * 
 * @author Jonathan Sterling
 *
 */
public class TopicAddedRemoteEventListener implements RemoteEventListener, Serializable {
	private static final long serialVersionUID = -2085393418959111560L;

	private final MainMenuController controller;

	public TopicAddedRemoteEventListener(MainMenuController controller) {
		super();

		this.controller = controller;
	}

	/**
	 * Listens for a topic being added to the space for a given
	 * MainMenuController.
	 */
	public void notify(RemoteEvent event) {
		try {
			// Get the topic added that triggered the notification
			AvailabilityEvent availEvent = (AvailabilityEvent) event;
			JMSTopic topic = (JMSTopic) availEvent.getEntry();

			// Add the topic data to the MainMenuController's topic list.
			Object[] rowData = { topic.getName(), topic.getOwner().getName(), topic.getOwner().getId(), topic.getId() };

			controller.getTopicTableModel().addRow(rowData);
		} catch (Exception e) {
			System.err.println("Failed to run notify method for Topic Creation");
			e.printStackTrace();
		}
	}
}