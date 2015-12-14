package services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import javax.naming.directory.InvalidAttributeValueException;

import org.apache.commons.lang3.StringUtils;

import exceptions.DuplicateEntryException;
import exceptions.ResourceNotFoundException;
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

	public void deleteTopic(JMSTopic topic) {

		if (isValidTopic(topic)) {
			try {
				space.takeIfExists(topic, null, 3000);

				deleteAllTopicUsers(topic);
				MessageService.getMessageService().deleteAllTopicMessages(topic);

				space.write(new JMSTopicDeleted(topic), null, 1000l * 60l);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Attempted to delete topic with null fields.  "
					+ "Due to how JavaSpaces work, this will delete one at random and is not allowed.");
		}
	}

	/**
	 * Gets all users that are currently in a given topic.
	 * 
	 * @param topic The topic to get users for.
	 * 
	 * @return All of the users in a given topic.
	 */
	public List<JMSTopicUser> getAllTopicUsers(JMSTopic topic) {
		return lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic));
	}

	/**
	 * Removes all users from a given topic.
	 * 
	 * @param topic A topic to remove all of the users from.
	 */
	private void deleteAllTopicUsers(JMSTopic topic) {
		JMSTopicUser template = new JMSTopicUser(topic);

		try {
			while (space.readIfExists(template, null, 1000) != null) {
				space.takeIfExists(template, null, 1000);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to delete all users from Topic entitled: '" + topic.getName() + "' with ID: '"
					+ topic.getId().toString() + "'");
			e.printStackTrace();
		}
	}

	public void addTopicUser(JMSTopic topic, JMSUser user) {
		JMSTopicUser topicUser = new JMSTopicUser(topic, user);

		try {
			// Only create if the user isn't already in there...
			if (space.readIfExists(topicUser, null, 1000) == null) {
				space.write(topicUser, null, Lease.FOREVER);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to add user to topic");
			e.printStackTrace();
		}
	}

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

	public void removeTopicUser(JMSTopic topic, JMSUser user) {
		try {
			boolean removed = false;
			JMSTopicUser template = new JMSTopicUser(topic, user);

			while (space.takeIfExists(template, null, 1000) != null) {
				removed = true;
			}

			if (removed) {
				JMSTopicUserRemoved removedTopicUser = new JMSTopicUserRemoved(template.getTopic(), template.getUser());

				space.write(removedTopicUser, null, 1000l * 60l);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			System.err.println("Failed to remove user from topic.  " + "User ID: '" + user.getId().toString()
					+ "' && Topic ID: '" + topic.getId().toString() + "'");
			e.printStackTrace();
		}
	}

	private boolean isValidTopic(JMSTopic topic) {
		if (StringUtils.isNotBlank(topic.getBaseName()) && StringUtils.isNotBlank(topic.getName())
				&& topic.getId() != null) {
			return true;
		}

		return false;
	}
}
