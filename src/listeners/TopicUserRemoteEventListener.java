package listeners;

import java.io.Serializable;

import controllers.ChatroomController;
import models.JMSTopicUser;
import models.JMSUser;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.space.AvailabilityEvent;

public class TopicUserRemoteEventListener implements RemoteEventListener, Serializable {
	private static final long serialVersionUID = 2510981009015708368L;
	
	private final ChatroomController controller;
	
	public TopicUserRemoteEventListener(ChatroomController controller){
		super();
		
		this.controller = controller;
	}
	
	public void notify(RemoteEvent event) {
		try {
			AvailabilityEvent availEvent = (AvailabilityEvent) event;
			JMSTopicUser topicUser = (JMSTopicUser) availEvent.getEntry();
			JMSUser user = topicUser.getUser();
			
			Object[] rowData = { user.getName(), user.getId() };
			
			controller.getUsersTableModel().addRow(rowData);
		} catch (Exception e) {
			System.err.println("Failed to run notify method for TopicUsers");
			e.printStackTrace();
		}
	}
}