package listeners;

import java.io.Serializable;
import java.util.UUID;

import javax.swing.table.DefaultTableModel;

import controllers.ChatroomController;
import models.JMSTopicUser;
import models.JMSUser;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.space.AvailabilityEvent;

/**
 * Listens for a user being added to a given topic in a given ChatroomController
 * 
 * @author Jonathan Sterling
 *
 */
public class TopicUserAddedRemoteEventListener implements RemoteEventListener, Serializable {
	private static final long serialVersionUID = 2510981009015708368L;

	private final ChatroomController controller;

	public TopicUserAddedRemoteEventListener(ChatroomController controller) {
		super();

		this.controller = controller;
	}

	/**
	 * Listens for a user being added to a given topic in a given
	 * ChatroomController
	 */
	public void notify(RemoteEvent event) {
		try {
			// Get the user added that triggered the notification
			AvailabilityEvent availEvent = (AvailabilityEvent) event;
			JMSTopicUser topicUser = (JMSTopicUser) availEvent.getEntry();
			JMSUser user = topicUser.getUser();

			// If the user isn't already in the user list, add it to the list
			if (!alreadyInUserList(user)) {
				Object[] rowData = { user.getName(), user.getId() };

				controller.getUsersTableModel().addRow(rowData);
			}
		} catch (Exception e) {
			System.err.println("Failed to run notify method for TopicUsers");
			e.printStackTrace();
		}
	}

	/**
	 * Checks to see if a given user is in this Listener's ChatroomController's
	 * user list
	 * 
	 * @param user
	 *            The user to check for.
	 * @return <code>true</code> if the user is in this Listener's
	 *         ChatroomController's user list. Otherwise, <code>false</code>.
	 */
	private boolean alreadyInUserList(JMSUser user) {
		DefaultTableModel userTableModel = controller.getUsersTableModel();
		int rows = userTableModel.getRowCount();

		// Loops through all user IDs in ChatroomController's user list
		for (int i = 0; i < rows; i++) {
			UUID idInTable = (UUID) userTableModel.getValueAt(i, 1);

			// Returns true if any user ID in the list matches the given user's
			// ID
			if (idInTable.equals(user.getId())) {
				return true;
			}
		}

		// If the method gets to here, the user is not already in the list, so
		// return false
		return false;
	}
}