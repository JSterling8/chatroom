package listeners;

import java.io.Serializable;

import controllers.MainMenuController;
import models.JMSTopic;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.space.AvailabilityEvent;

public class TopicRemoteEventListener implements RemoteEventListener, Serializable {
	private static final long serialVersionUID = -2085393418959111560L;
	
	private final MainMenuController controller;
	
	public TopicRemoteEventListener(MainMenuController controller){
		super();
		
		this.controller = controller;
	}
	
	public void notify(RemoteEvent event) {
		try {
			AvailabilityEvent availEvent = (AvailabilityEvent) event;
			JMSTopic topic = (JMSTopic) availEvent.getEntry();
			
			Object[] rowData = { topic.getName(), topic.getOwner().getName(),
					topic.getOwner().getId(), topic.getId() };
			
			controller.getTopicTableModel().addRow(rowData);
		} catch (Exception e) {
			System.err.println("Failed to run notify method for Topics");
			e.printStackTrace();
		}
	}
}