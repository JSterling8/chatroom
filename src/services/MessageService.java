package services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

import exceptions.ResourceNotFoundException;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSTopicUser;
import models.JMSUser;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.space.JavaSpace05;
import services.helper.EntryLookupHelper;
import services.helper.TransactionHelper;

/**
 * A singleton that handles all interactions between a client and the JavaSpace
 * for messages
 * 
 * @author Jonathan Sterling
 *
 */
public class MessageService implements Serializable {
	private static final long serialVersionUID = -362012410946846034L;
	private static final TopicService topicService = TopicService.getTopicService();

	private static MessageService messageService;

	private JavaSpace05 space = SpaceService.getSpace();
	private EntryLookupHelper lookupHelper = new EntryLookupHelper();

	private MessageService() {
		// Uninstantiable
	}

	/**
	 * Lazily instantiates a MessageService instance if one does not exist.
	 * 
	 * @return The MessageService singleton.
	 */
	public static MessageService getMessageService() {
		if (messageService == null) {
			messageService = new MessageService();
		}

		return messageService;
	}

	/**
	 * For a given topic, this method gets all messages that are either public,
	 * to the specified user, or from a specified user.
	 * 
	 * @param topic
	 *            The topic to get messages for
	 * @param user
	 *            The user to get messages for
	 * 
	 * @return For a given topic, all messages that are either public, to the
	 *         specified user, or from the specified user.
	 */
	public List<JMSMessage> getAllMessagesForUserInTopic(JMSTopic topic, JMSUser user) {
		List<JMSMessage> allMessages = lookupHelper.findAllMatchingTemplate(space, new JMSMessage(topic));

		Iterator<JMSMessage> it = allMessages.iterator();

		while (it.hasNext()) {
			JMSMessage message = (JMSMessage) it.next();
			if (message.getTo() != null && (!message.getTo().getId().equals(user.getId())
					&& !message.getFrom().getId().equals(user.getId()))) {
				// Remove any message that is not for this user (non-public
				// messages that are not to/from this user)
				it.remove();
			}
		}

		/*
		 * Ensure that the MatchSet is returned in order. Tests show that they
		 * always are in order, however there is no guarantee for this from the
		 * JavaSpace05 JavaDoc. In the case that the list is already in order,
		 * the complexity of this sort is O(n), and doesn't add a significant
		 * overhead.
		 *
		 */
		Collections.sort(allMessages, new MessageComparator());

		return allMessages;
	}

	/**
	 * Writes a given message to the JavaSpace
	 * 
	 * @param message
	 *            The message to write to the JavaSpace.
	 * @throws RemoteException
	 *             Thrown if the message cannot be written to the space for any
	 *             reason other than the Topic not existing
	 * @throws ResourceNotFoundException
	 *             Thrown if the Topic the user wishes to send a message in does
	 *             not exist
	 */
	public void sendMessage(JMSMessage message) throws ResourceNotFoundException, RemoteException {
		try {
			if (message.getTopic() == null) {
				throw new ResourceNotFoundException("Message does not have required field: topic");
			}

			// Get a transaction to run all of this method in.
			Transaction transaction = TransactionHelper.getTransaction();

			// Check that the topic exists
			if (topicService.doesTopicExistInSpace(message.getTopic(), transaction)) {
				// If the message is public, just write it to the space
				if (message.getTo() == null) {
					space.write(message, transaction, Lease.FOREVER);
				} else {
					// This check is here to guard against a wildcard match when
					// we later check to see if the user the message is being
					// sent to is still in the space
					if (isInvalidUser(message.getTo())) {
						throw new InvalidAttributeValueException(
								"Message being sent to invalid user object (one or more fields null)");
					}

					// Check that the user the message is being sent to is in
					// the specified topic
					JMSTopicUser template = new JMSTopicUser();
					template.setTopic(message.getTopic());
					template.setUser(message.getTo());
					JMSTopicUser userInSpace = (JMSTopicUser) space.readIfExists(template, transaction, 1000);

					if (userInSpace == null) {
						throw new ResourceNotFoundException("Message being sent to user who is not in the topic.");
					}

					// If the checks have all passed, write the message to the
					// space
					space.write(message, transaction, Lease.FOREVER);
				}

				// We need to commit the transaction or nothing will happen.
				transaction.commit();
			} else {
				System.err.println("User attempted to send message to another user in a topic that does not exist.");
				transaction.abort();

				throw new ResourceNotFoundException("Topic no longer exists.  Perhaps it has been deleted?");
			}
		} catch (ResourceNotFoundException e) {
			System.err.println("Failed to write message to JavaSpace.  Topic null or does not exist.");

			throw e;
		} catch (InvalidAttributeValueException | RemoteException | TransactionException | InterruptedException
				| UnusableEntryException e) {
			System.err.println("Failed to write message to JavaSpace.  Server error.");

			throw new RemoteException("Failed to write message to JavaSpace.  Server error.");
		}
	}

	/**
	 * Checks if a given JMSUser object has all of the required fields.
	 * 
	 * @param user
	 *            The user to check the validity of
	 * @return <code>true</code> if the user is valid, otherwise
	 *         <code>false</code>
	 */
	private boolean isInvalidUser(JMSUser user) {
		if (user.getBaseName() == null || user.getName() == null || user.getId() == null
				|| user.getPassword() == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Deletes all messages for a given topic. 
	 * 
	 * <b>Transaction must be committed for this method to work</b>
	 * 
	 * @param topic
	 *            The topic whose messages will be deleted
	 * @param transaction
	 * 			  The transaction in which the deletion will occur
	 */
	public void deleteAllTopicMessages(JMSTopic topic, Transaction transaction) {
		JMSMessage template = new JMSMessage(topic);

		// Take all of the topic's messages
		EntryLookupHelper entryLookupHelper = new EntryLookupHelper();
		entryLookupHelper.takeAllMatchingTemplate(space, template, transaction);
	}

	/**
	 * Custom comparator for sorting a list of messages into chronological
	 * order.
	 * 
	 * @author Jonathan Sterling
	 *
	 */
	public class MessageComparator implements Comparator<JMSMessage> {
		@Override
		public int compare(JMSMessage o1, JMSMessage o2) {
			return o1.getSentDate().compareTo(o2.getSentDate());
		}
	}
}
