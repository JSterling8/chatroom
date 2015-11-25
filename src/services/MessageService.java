package services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

import models.JMSMessage;
import models.JMSTopic;
import models.JMSTopicUser;
import models.JMSUser;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;
import services.helper.EntryLookupHelper;
import services.helper.TransactionHelper;

public class MessageService implements Serializable {
	private static final long serialVersionUID = -362012410946846034L;
	private static final TopicService topicService = TopicService.getTopicService();
	private static MessageService messageService;

	private JavaSpace05 space = SpaceService.getSpace();
	private EntryLookupHelper lookupHelper = new EntryLookupHelper();

	private MessageService() {
	}

	public static MessageService getMessageService() {
		if (messageService == null) {
			messageService = new MessageService();
		}

		return messageService;
	}

	public List<JMSMessage> getAllMessagesForUserInTopic(JMSTopic topic, JMSUser user) {
		List<JMSMessage> allMessages = lookupHelper.findAllMatchingTemplate(space, new JMSMessage(topic));
		
		Iterator it = allMessages.iterator();
		
		while(it.hasNext()){
			JMSMessage message = (JMSMessage) it.next();
			if(message.getTo() != null && 
					message.getTo().getId() != user.getId() &&
					!user.getId().equals(topic.getOwner().getId())){
				it.remove();
			}
		}
		
		//TODO Research if I actually need this? I don't appear to
		Collections.sort(allMessages, new MessageComparator());
		
		return allMessages;
	}

	public void createMessage(JMSMessage message) throws Exception {
		Transaction transaction = TransactionHelper.getTransaction();

		if (topicService.doesTopicExistInSpace(message.getTopic(), transaction)) {
			if (message.getTo() == null) {
				space.write(message, transaction, Lease.FOREVER);
			} else {
				// Ensure the user sending to is in the chatroom still.
				if (message.getTopic() == null) {
					throw new InvalidAttributeValueException("Message does not have required field: topic");
				}

				// This check is here to guard against a wildcard match when we
				// check to see if the user the message is being sent to is
				// still in the space
				if (anyFieldNull(message.getTo())) {
					throw new InvalidAttributeValueException(
							"Message being sent to invalid user object (one or more fields null)");
				}

				JMSTopicUser template = new JMSTopicUser();
				template.setTopic(message.getTopic());
				template.setUser(message.getTo());
				JMSTopicUser userInSpace = (JMSTopicUser) space.readIfExists(template, transaction, 1000);

				if (userInSpace == null) {
					throw new InvalidAttributeValueException(
							"Message being sent to user who is not in the required topic.");
				}

				space.write(message, transaction, Lease.FOREVER);
			}

			transaction.commit();
		} else {
			System.err.println("User attempted to send message to another user who is not in the required topic");
			transaction.abort();

			throw new Exception("Topic no longer exists.  Perhaps it has been deleted?");
		}
	}

	private boolean anyFieldNull(JMSUser to) {
		if (to.getBaseName() == null || to.getName() == null || to.getId() == null || to.getPassword() == null) {
			return true;
		} else {
			return false;
		}
	}

	public void deleteAllTopicMessages(JMSTopic topic) {
		JMSMessage template = new JMSMessage(topic);

		try {
			EntryLookupHelper entryLookupHelper = new EntryLookupHelper();

			List<JMSMessage> topicMessages = entryLookupHelper.findAllMatchingTemplate(space, template);

			Transaction transaction = TransactionHelper.getTransaction(3000);

			for (JMSMessage message : topicMessages) {
				space.takeIfExists(message, transaction, 1000);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to delete Topic Messages");
			e.printStackTrace();
		}
	}

	public class MessageComparator implements Comparator<JMSMessage> {
	    @Override
	    public int compare(JMSMessage o1, JMSMessage o2) {
	        return o1.getSentDate().compareTo(o2.getSentDate());
	    }
	}
}
