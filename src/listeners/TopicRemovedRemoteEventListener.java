package listeners;

import java.rmi.RemoteException;
import java.util.UUID;

import javax.swing.table.DefaultTableModel;

import controllers.ChatroomController;
import controllers.MainMenuController;
import models.JMSTopic;
import models.JMSTopicDeleted;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.space.AvailabilityEvent;

/**
 * Listens for a topic being removed from the space for either a
 * MainMenuController or a ChatroomController.
 * 
 * @author Jonathan Sterling
 *
 */
public class TopicRemovedRemoteEventListener implements RemoteEventListener {
	private MainMenuController mainMenuController;
	private ChatroomController chatroomController;
	private boolean isChatroomController;

	/**
	 * Constructor to be used for listeners inside of a MainMenuController
	 * 
	 * @param mainMenuController
	 *            The controller this method is listening for.
	 */
	public TopicRemovedRemoteEventListener(MainMenuController mainMenuController) {
		super();

		this.mainMenuController = mainMenuController;
		this.isChatroomController = false;
	}

	/**
	 * Constructor to be used for listeners inside of a ChatroomController
	 * 
	 * @param chatroomController
	 *            The controller this method is listening for.
	 */
	public TopicRemovedRemoteEventListener(ChatroomController chatroomController) {
		this.chatroomController = chatroomController;
		this.isChatroomController = true;
	}

	/**
	 * Listens for a topic being removed from the space for either a
	 * MainMenuController or a ChatroomController.
	 */
	@Override
	public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
		try {
			// Get the event that triggered the notification
			AvailabilityEvent availEvent = (AvailabilityEvent) remoteEvent;
			JMSTopicDeleted topicDeleted = (JMSTopicDeleted) availEvent.getEntry();

			if (isChatroomController) {
				// If the listener is for a ChatroomController, inform the given
				// chatroom that the chatroom's topic has been deleted
				chatroomController.handleTopicDeleted();
			} else {
				// If the listener is for a MainMenuController, remove the given
				// topic from the MainMenuController's topic list.
				DefaultTableModel topicTableModel = mainMenuController.getTopicTableModel();

				// Loop through all of the topics in the MainMenuController's
				// topic list and remove the one that was deleted
				for (int i = 0; i < topicTableModel.getRowCount(); i++) {
					UUID topicIdInTable = (UUID) topicTableModel.getValueAt(i, 3);
					JMSTopic topicRemoved = topicDeleted.getTopic();
					UUID topicDeletedId = topicRemoved.getId();

					if (topicIdInTable.equals(topicDeletedId)) {
						topicTableModel.removeRow(i);

						break;
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to remove topic from list or send notifications to users.");
			e.printStackTrace();
		}
	}
}
