package services;

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
import net.jini.space.JavaSpace;
import services.helper.EntryLookupHelper;

public class TopicService {
	private static TopicService topicService;

	private JavaSpace space = SpaceService.getSpace();
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
			/*TransactionManager transactionManager = SpaceService.getManager();
			Created transactionCreated = TransactionFactory.create(transactionManager, 1000 * 10);
			transaction = transactionCreated.transaction;*/
			if (isValidTopic(topic) && !topicExistsInSpace(topic, transaction)) {
				space.write(topic, transaction, Lease.FOREVER);
				/*transaction.commit();*/
			} else {
				// TODO Throw new invalid topic exception (i.e., id or something
				// is missing). Or already exists.
				throw new DuplicateEntryException();
			}

		} catch (Exception e) {
/*			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}*/

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
		} catch (Exception e) {
			// TODO Finer error handling
			e.printStackTrace();
		}

		return topic;
	}

	public void deleteTopic(JMSTopic topic) {
		try{
			space.takeIfExists(topic, null, 3000);
			
			deleteAllTopicUsers(topic);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public List<JMSTopicUser> getAllTopicUsers(JMSTopic topic){
		return lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic));
	}
	
	private void deleteAllTopicUsers(JMSTopic topic) {
		JMSTopicUser template = new JMSTopicUser(topic);
		
		try {
			while(space.readIfExists(template, null, 1000) != null){
				space.takeIfExists(template, null, 1000);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			// TODO Finer error handling?
			e.printStackTrace();
		}
	}

	public void addTopicUser(JMSTopic topic, JMSUser user){
		JMSTopicUser topicUser = new JMSTopicUser(topic, user);
		
		try {
			// Only create if the user isn't already in there...
			if(space.readIfExists(topicUser, null, 1000) == null){
				space.write(topicUser, null, Lease.FOREVER);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			// TODO Auto-generated catch block
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

	public void removeTopicUser(JMSTopic topic, JMSUser user){
		try {
			boolean removed = false;
			JMSTopicUser template = new JMSTopicUser(topic, user);
			
			while(space.takeIfExists(template, null, 1000) != null) {
				removed = true;
			}
			
			if(removed){
				// This is so a later notification can pick this up and remove the user from an open topic user list.
				space.write(new JMSTopicUserRemoved(template), null, 1000);
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			// TODO Better error handling?
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
