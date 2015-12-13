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

public class TopicUserRemovedRemoteEventListener implements RemoteEventListener {
	private ChatroomController controller;
	
	public TopicUserRemovedRemoteEventListener(ChatroomController controller) {
		this.controller = controller;
	}
	
	@Override
	public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
		try {
			AvailabilityEvent availEvent = (AvailabilityEvent) remoteEvent;
			JMSTopicUserRemoved topicUserRemoved = (JMSTopicUserRemoved) availEvent.getEntry();
			
			DefaultTableModel usersTableModel = controller.getUsersTableModel();
			
			for(int i = 0; i < usersTableModel.getRowCount(); i++){
				UUID idOfUserInTable = (UUID) usersTableModel.getValueAt(i, 1);
				if (idOfUserInTable.equals(topicUserRemoved.getUser().getId())){
					usersTableModel.removeRow(i);
					
					break;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
