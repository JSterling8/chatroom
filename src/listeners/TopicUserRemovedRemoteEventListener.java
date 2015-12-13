package listeners;

import java.rmi.RemoteException;
import java.util.UUID;

import javax.swing.table.DefaultTableModel;

import controllers.ChatroomController;
import models.JMSTopicUserRemoved;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.space.AvailabilityEvent;

/**
 * 
 * Listens for topic users being removed from a given topic.
 * 
 * @author Jonathan Sterling
 *
 */
public class TopicUserRemovedRemoteEventListener implements RemoteEventListener {
	private ChatroomController controller;

	public TopicUserRemovedRemoteEventListener(ChatroomController controller) {
		this.controller = controller;
	}

	/**
	 * Listens for topic users being removed from a given topic.
	 */
	@Override
	public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
		try {
			// Get the removed user that triggered the notification
			AvailabilityEvent availEvent = (AvailabilityEvent) remoteEvent;
			JMSTopicUserRemoved topicUserRemoved = (JMSTopicUserRemoved) availEvent.getEntry();

			DefaultTableModel usersTableModel = controller.getUsersTableModel();

			// Find the user in the ChatroomController's user list and remove
			// them
			for (int i = 0; i < usersTableModel.getRowCount(); i++) {
				UUID idOfUserInTable = (UUID) usersTableModel.getValueAt(i, 1);
				if (idOfUserInTable.equals(topicUserRemoved.getUser().getId())) {
					usersTableModel.removeRow(i);

					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
