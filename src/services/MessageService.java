package services;

import java.util.List;

import exceptions.DuplicateEntryException;
import models.JMSMessage;
import models.JMSTopic;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;
import services.helper.EntryLookupHelper;

public class MessageService {
	private static MessageService messageService;

	private JavaSpace space = SpaceService.getSpace();
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
	
	public void createMessage(JMSMessage message) throws Exception{
		Transaction transaction = null;

		try {
			/*TransactionManager transactionManager = SpaceService.getManager();
			Created transactionCreated = TransactionFactory.create(transactionManager, 1000 * 10);
			transaction = transactionCreated.transaction;*/
			space.write(message, transaction, Lease.FOREVER);
			/*transaction.commit();*/
		} catch (Exception e) {
/*			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}*/
			//TODO Finer error catching.
			throw e;
		}
	}
	
}
