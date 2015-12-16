package services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import javax.naming.directory.InvalidAttributeValueException;

import org.apache.commons.lang3.StringUtils;

import exceptions.DuplicateEntryException;
import models.JMSTopic;
import models.JMSTopicDeleted;
import models.JMSTopicUser;
import models.JMSTopicUserRemoved;
import models.JMSUser;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;
import services.helper.EntryLookupHelper;
import services.helper.TransactionHelper;

/**
 * A singleton that handles all interactions between a client and the JavaSpace
 * for topics
 * 
 * @author Jonathan Sterling
 *
 */
public class TopicService implements Serializable {
	private static final long serialVersionUID = 4384471014207319215L;

	private static TopicService topicService;

	private JavaSpace05 space = SpaceService.getSpace();
	private EntryLookupHelper lookupHelper = new EntryLookupHelper();

	private TopicService() {
		// Uninstantiable singleton
	}

	/**
	 * If a topic service instance exists, it is returned. If not, one is
	 * created, then returned.
	 * 
	 * @return The TopicService instance
	 */
	public static TopicService getTopicService() {
		if (topicService == null) {
			topicService = new TopicService();
		}

		return topicService;
	}

	/**
	 * Writes a topic to the JavaSpace
	 * 
	 * @param topic
	 * @throws Exception
	 */
	public void createTopic(JMSTopic topic) throws Exception {
		Transaction transaction = TransactionHelper.getTransaction(3000);

		try {
			// If the topic is valid (non-null name, base name, and ID)
			if (isValidTopic(topic)) {
				// If the topic does not already exist in the space
				if (!topicExistsInSpace(topic, transaction)) {
					// Write it to the space and commit the transaction
					space.write(topic, transaction, Lease.FOREVER);
					transaction.commit();
				} else {
					// Otherwise it's a duplicate, so throw an exception
					throw new DuplicateEntryException(
							"Failed to create topic.  Topic baseName or id matches with an existing topic.");
				}
			} else {
				// Otherwise the Topic object is invalid, so throw an exception
				throw new InvalidAttributeValueException("Topic being creates is invalid (one or more fields null)");
			}

		} catch (Exception e) {
			// If anything in the try block throws an error, abort the
			// transaction before rethrowing the error
			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}

			throw e;
		}
	}

	/**
	 * Used for testing and ensuring space isn't left cluttered. Returns lease
	 * so it can be removed easily from the space.  No validity checks are made
	 * 
	 * @param topic The topic to create
	 * @return The topic's lease
	 */
	public Lease createDebugTopic(JMSTopic topic) throws RemoteException, TransactionException {
		long oneMinuteInMillis = 1000l * 60l;

		return space.write(topic, null, oneMinuteInMillis);
	}

	/**
	 * Gets all of the topics in the space.
	 * 
	 * @return All of the topics in the space.
	 */
	public List<JMSTopic> getAllTopics() {
		return lookupHelper.findAllMatchingTemplate(space, new JMSTopic());
	}

	/**
	 * Looks for a topic in the space with the same base name.
	 * 
	 * @param baseName
	 *            The base name to search for.
	 * @return If a matching topic exists, that topic is returned, otherwise
	 *         <code>null</code> is returned.
	 */
	public JMSTopic getTopicByBaseName(String baseName) {
		JMSTopic template = new JMSTopic();
		template.setBaseName(baseName);

		JMSTopic topicFound = null;

		try {
			topicFound = (JMSTopic) space.readIfExists(template, null, 1000);
		} catch (Exception e) {
			System.err.println("Failed to get topic by base name. Base name queried: '" + baseName + "'");
			e.printStackTrace();
		}

		return topicFound;
	}

	/**
	 * Looks for a topic in the space with the same UUID.
	 * 
	 * @param baseName
	 *            The UUID to search for.
	 * @return If a matching topic exists, that topic is returned, otherwise
	 *         <code>null</code> is returned.
	 */
	public JMSTopic getTopicById(UUID id) {
		JMSTopic template = new JMSTopic();
		template.setId(id);

		JMSTopic topic = null;

		try {
			topic = (JMSTopic) space.readIfExists(template, null, 1000);
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to get topic by ID.  Topic ID queried: '" + id.toString() + "'");
			e.printStackTrace();
		}

		return topic;
	}

	/**
	 * This method is for checking if an EXACT topic passed in matches one
	 * existing in the space.
	 * 
	 * This method should not be used to see if a topic with the same name
	 * exists.
	 * 
	 * @param topic
	 *            The topic to check for existence of
	 * 
	 * @return Whether or not a topic exists in the space with all fields the
	 *         same as specified in the parameter passed in. Also returns false
	 *         if exception occurs
	 */
	public boolean doesTopicExistInSpace(JMSTopic topic, Transaction transaction) {
		try {
			if (space.readIfExists(topic, transaction, 500) != null) {
				return true;
			} else {
				return false;
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to get topic from space");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes a topic, including all of its messages and TopicUsers
	 * 
	 * @param topic
	 *            The topic to delete
	 */
	public void deleteTopic(JMSTopic topic) {

		// If the topic does not contain null fields
		if (isValidTopic(topic)) {
			try {
				Transaction transaction = TransactionHelper.getTransaction(10000l);

				space.takeIfExists(topic, transaction, 3000l);

				deleteAllTopicUsers(topic, transaction);
				MessageService.getMessageService().deleteAllTopicMessages(topic, transaction);

				// Writes a JMSTopicDeleted object so listeners know the topic
				// has been removed
				space.write(new JMSTopicDeleted(topic), transaction, 1000l * 60l);

				transaction.commit();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Attempted to delete topic with on or more null fields.  "
					+ "Due to how JavaSpaces work, this will delete one at random and is not allowed.");
		}
	}

	/**
	 * Gets all users that are currently in a given topic.
	 * 
	 * @param topic
	 *            The topic to get users for.
	 * 
	 * @return All of the users in a given topic.
	 */
	public List<JMSTopicUser> getAllTopicUsers(JMSTopic topic) {
		return lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic));
	}

	/**
	 * Adds a given user to a given topic.
	 * 
	 * @param topic
	 *            The topic to add the user to
	 * @param user
	 *            The user to add to the topic
	 */
	public void addTopicUser(JMSTopic topic, JMSUser user) {
		JMSTopicUser topicUser = new JMSTopicUser(topic, user);

		try {
			Transaction transaction = TransactionHelper.getTransaction();

			// Only add the TopicUser if it isn't already in there...
			if (space.readIfExists(topicUser, transaction, 1000) == null) {
				space.write(topicUser, transaction, Lease.FOREVER);
			}

			transaction.commit();
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to add user to topic");
			e.printStackTrace();
		}
	}
	
	/**
	 * Used for testing and ensuring space isn't left cluttered. Returns lease
	 * so it can be removed easily from the space.  No validity checks are made
	 * 
	 * @param topic The topic to create a JMSTopicUser for
	 * @param user User to create JMSTopicUser for
	 * @return The JMSTopicUser's lease
	 */
	public Lease addDebugTopicUser(JMSTopic topic, JMSUser user) throws RemoteException, TransactionException {
		return space.write(new JMSTopicUser(topic, user), null, 60l * 1000l);
	}

	/**
	 * Removes a given user from a given topic
	 * 
	 * @param topic
	 *            The topic to remove the user from
	 * @param user
	 *            The user to remove from the topic
	 */
	public void removeTopicUser(JMSTopic topic, JMSUser user) {
		try {
			Transaction transaction = TransactionHelper.getTransaction();

			boolean removed = false;
			JMSTopicUser template = new JMSTopicUser(topic, user);

			// If the space is in a bad state and has duplicate users in a
			// topic, this while loop ensures they are all removed
			while (space.takeIfExists(template, transaction, 1000) != null) {
				removed = true;
			}

			// Put a JMSTopicUserRemoved object in the space so listeners can
			// pick them up and remove them from other user's lists
			if (removed) {
				JMSTopicUserRemoved removedTopicUser = new JMSTopicUserRemoved(template.getTopic(), template.getUser());

				// Writes the JMSTopicUserRemoved with a 60 second lease, so
				// listeners have 60 seconds to act on it
				space.write(removedTopicUser, transaction, 1000l * 60l);
			}

			transaction.commit();
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to remove user from topic.  " + "User ID: '" + user.getId().toString()
					+ "' && Topic ID: '" + topic.getId().toString() + "'");
			e.printStackTrace();
		}
	}

	/**
	 * Whether or not a topic already exists in the space with the same base
	 * name or UUID.
	 * 
	 * @param topic
	 *            The topic to check if exists
	 * @param transaction
	 *            The transaction in which the check will take place
	 * @return <code>true</code> if a match by either base name or UUID,
	 *         otherwise <code>false</code>
	 */
	private boolean topicExistsInSpace(JMSTopic topic, Transaction transaction) {
		try {
			JMSTopic template = new JMSTopic();
			template.setBaseName(topic.getBaseName());
			JMSTopic topicBaseNameMatch = (JMSTopic) space.readIfExists(template, transaction, 2000);

			template = new JMSTopic();
			template.setId(topic.getId());
			JMSTopic topicIdMatch = (JMSTopic) space.readIfExists(template, transaction, 2000);

			if (topicBaseNameMatch != null || topicIdMatch != null) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Removes all users from a given topic.
	 * 
	 * @param topic
	 *            A topic to remove all of the users from.
	 * @param transaction
	 *            The transaction in which to run the delete
	 */
	private void deleteAllTopicUsers(JMSTopic topic, Transaction transaction) {
		JMSTopicUser template = new JMSTopicUser(topic);

		try {
			while (space.readIfExists(template, transaction, 1000) != null) {
				space.takeIfExists(template, transaction, 1000);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to delete all users from Topic entitled: '" + topic.getName() + "' with ID: '"
					+ topic.getId().toString() + "'");
			e.printStackTrace();
		}
	}

	/**
	 * Checks if a topic has any null unique identifiers (base name, name, and
	 * UUID)
	 * 
	 * @param topic
	 * @return <code>true</code> if any unique identifier is null, otherwise
	 *         <code>false</code>
	 */
	private boolean isValidTopic(JMSTopic topic) {
		if (topic.getBaseName() != null && StringUtils.isNotBlank(topic.getName()) && topic.getId() != null) {
			return true;
		}

		return false;
	}
}
