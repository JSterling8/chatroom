package services;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import exceptions.DuplicateEntryException;
import models.JMSTopic;
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

public class TopicService implements Serializable {
	private static final long serialVersionUID = 4384471014207319215L;

	private static TopicService topicService;

	private JavaSpace05 space = SpaceService.getSpace();
	private EntryLookupHelper lookupHelper = new EntryLookupHelper();

	private TopicService() {
	}

	public static TopicService getTopicService() {
		if (topicService == null) {
			topicService = new TopicService();
		}

		return topicService;
	}

	public void createTopic(JMSTopic topic) throws Exception {
		Transaction transaction = null;

		try {
			transaction = TransactionHelper.getTransaction(3000);

			if (isValidTopic(topic) && !topicExistsInSpace(topic, transaction)) {
				space.write(topic, transaction, Lease.FOREVER);
				transaction.commit();
			} else {
				throw new DuplicateEntryException(
						"Failed to create topic.  Topic baseName or id matches with an existing topic.");
			}

		} catch (Exception e) {
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

	public List<JMSTopic> getAllTopics() {
		return lookupHelper.findAllMatchingTemplate(space, new JMSTopic());
	}

	public JMSTopic getTopicByName(String name) {
		JMSTopic template = new JMSTopic();
		template.setBaseName(name);

		JMSTopic topic = null;

		try {
			topic = (JMSTopic) space.readIfExists(template, null, 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return topic;
	}

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
	 *         same as specified in the paramater passed in. Also returns false
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
		try {
			space.takeIfExists(topic, null, 3000);

			deleteAllTopicUsers(topic);
			MessageService.getMessageService().deleteAllTopicMessages(topic);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<JMSTopicUser> getAllTopicUsers(JMSTopic topic) {
		return lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic));
	}

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
				&& topic.getUsers() >= 1 && topic.getId() != null) {
			return true;
		}

		return false;
	}
}
