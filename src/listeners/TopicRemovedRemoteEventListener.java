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

public class TopicRemovedRemoteEventListener implements RemoteEventListener{
	private MainMenuController mainMenuController;
	private ChatroomController chatroomController;
	private boolean isChatroomController;
	
	public TopicRemovedRemoteEventListener(MainMenuController mainMenuController) {
		super();
		
		this.mainMenuController = mainMenuController;
		this.isChatroomController = false;
	}
	
	public TopicRemovedRemoteEventListener(ChatroomController chatroomController){
		this.chatroomController = chatroomController;
		this.isChatroomController = true;
	}
	
	@Override
	public void notify(RemoteEvent remoteEvent) throws UnknownEventException, RemoteException {
		try{
			AvailabilityEvent availEvent = (AvailabilityEvent) remoteEvent;
			JMSTopicDeleted topicDeleted = (JMSTopicDeleted) availEvent.getEntry();
			
			if(isChatroomController) {
				chatroomController.handleTopicDeleted();
			} else {
				DefaultTableModel topicTableModel = mainMenuController.getTopicTableModel();
				for(int i = 0; i < topicTableModel.getRowCount(); i++) {
					UUID topicIdInTable = (UUID) topicTableModel.getValueAt(i, 3);
					JMSTopic topicRemoved = topicDeleted.getTopic();
					UUID topicDeletedId = topicRemoved.getId();
					
					if(topicIdInTable.equals(topicDeletedId)) {
						topicTableModel.removeRow(i);
						
						break;
					}
				}
			}
		} catch (Exception e){
			System.err.println("Failed to remove topic from list or send notifications to users.");
			e.printStackTrace();
		}
	}
}
