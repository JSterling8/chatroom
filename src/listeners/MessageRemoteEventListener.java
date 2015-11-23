package listeners;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;

import controllers.ChatroomController;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSUser;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;

public class MessageRemoteEventListener implements RemoteEventListener, Serializable {
	private static final long serialVersionUID = 7526472345622976341L;
	private final ChatroomController controller;
	private final JMSTopic topic;
	private final JMSUser user;
	
	public MessageRemoteEventListener(ChatroomController controller, JMSTopic topic, JMSUser user){
		super();
		
		this.controller = controller;
		this.topic = topic;
		this.user = user;
	}
	
	@Override
	public void notify(RemoteEvent event) {
		try {
			JMSMessage message = (JMSMessage) event.getSource();
			
			if 	(		
					message.getTo() == null ||
					message.getTo().getId().equals(user.getId()) || 
					message.getFrom().getId().equals(user.getId()) ||
					topic.getOwner().getId().equals(user.getId())
				) {
				JMSUser userFrom = message.getFrom();
				String messageText = message.getMessage();
				Date sentDate = message.getSentDate();
				Object[] rowData = { sentDate.toString(), userFrom, messageText };
				controller.generateMessagesTableModel().addRow(rowData);
				
				// If it's a PM, mark it as such...
				if(message.getTo() != null) {
					controller.colourBottomMessage();
				}
			} else {
				// Not for us.  Ignore...
			}
		} catch (Exception e) {
			System.err.println("Failed to run notify method");
			e.printStackTrace();
		}
	}
}