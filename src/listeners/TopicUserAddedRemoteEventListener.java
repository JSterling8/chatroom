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

public class TopicUserAddedRemoteEventListener implements RemoteEventListener, Serializable {
	private static final long serialVersionUID = 2510981009015708368L;
	
	private final ChatroomController controller;
	
	public TopicUserAddedRemoteEventListener(ChatroomController controller){
		super();
		
		this.controller = controller;
	}
	
	public void notify(RemoteEvent event) {
		try {
			AvailabilityEvent availEvent = (AvailabilityEvent) event;
			JMSTopicUser topicUser = (JMSTopicUser) availEvent.getEntry();
			JMSUser user = topicUser.getUser();
			
			if(notAlreadyInUserList(user)){
				Object[] rowData = { user.getName(), user.getId() };
				
				controller.getUsersTableModel().addRow(rowData);
			}
		} catch (Exception e) {
			System.err.println("Failed to run notify method for TopicUsers");
			e.printStackTrace();
		}
	}

	private boolean notAlreadyInUserList(JMSUser user) {
		DefaultTableModel userTableModel = controller.getUsersTableModel();
		int rows = userTableModel.getRowCount();
		
		for(int i = 0; i < rows; i++){
			UUID idInTable = (UUID) userTableModel.getValueAt(i, 1);
			
			if(idInTable.equals(user.getId())) {
				return false;
			}
		}
		
		return true;
	}
}