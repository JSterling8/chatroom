package services;

import java.rmi.RemoteException;
import java.util.List;

import models.JMSMessage;
import models.JMSTopic;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;
import services.helper.EntryLookupHelper;
import services.helper.TransactionHelper;

public class MessageService {
	private static MessageService messageService;

	private JavaSpace05 space = SpaceService.getSpace();
	private EntryLookupHelper lookupHelper = new EntryLookupHelper();
	
	private MessageService(){}
	
	public static MessageService getMessageService(){
		if (messageService == null) {
			messageService = new MessageService();
		}

		return messageService;
	}
	
	public List<JMSMessage> getAllMessagesForTopic(JMSTopic topic) {
		return lookupHelper.findAllMatchingTemplate(space, new JMSMessage(topic));
	}
	
	public void createPublicMessage(JMSMessage message) throws Exception{
		if(TopicService.getTopicService().getTopicById(message.getTopic().getId()) != null){
			space.write(message, null, Lease.FOREVER);
		} else {
			throw new Exception("Topic no longer exists.  Perhaps it has been deleted?");
		}
	}

	public void deleteAllTopicMessages(JMSTopic topic) {
		JMSMessage template = new JMSMessage(topic);
		
		try {
			EntryLookupHelper entryLookupHelper = new EntryLookupHelper();
			
			List<JMSMessage> topicMessages = entryLookupHelper.findAllMatchingTemplate(space, template);
			
			Transaction transaction = TransactionHelper.getTransaction(3000);
			
			for(JMSMessage message : topicMessages){
				space.takeIfExists(message, transaction, 1000);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to delete Topic Messages");
			e.printStackTrace();
		}
		
	}
	
}
