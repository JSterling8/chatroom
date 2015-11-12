package services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import exceptions.DuplicateEntryException;
import models.JMSTopic;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

public class TopicService {
	private static TopicService topicService;

	private JavaSpace space = SpaceService.getSpace();

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
		Transaction transaction = null;
		List<JMSTopic> topics = new ArrayList<JMSTopic>();

		try {
/*			TransactionManager transactionManager = SpaceService.getManager();
			Created transactionCreated = TransactionFactory.create(transactionManager, 1000 * 10);
			transaction = transactionCreated.transaction;*/

			JMSTopic template = new JMSTopic();
			while (space.readIfExists(template, transaction, 1000) != null) {
				topics.add((JMSTopic) space.takeIfExists(template, transaction, 1000));
			}
			
			for(JMSTopic topic : topics){
				space.write(topic, null, Lease.FOREVER);
			}

			/*transaction.abort();*/

		} catch (Exception e) {
/*			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}*/

			e.printStackTrace();
		}

		return topics;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return topic;
	}

	public void deleteTopic(JMSTopic topic) {
		try{
			space.takeIfExists(topic, null, 3000);
		} catch (Exception e){
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

	private boolean isValidTopic(JMSTopic topic) {
		if (StringUtils.isNotBlank(topic.getBaseName()) && StringUtils.isNotBlank(topic.getName())
				&& topic.getUsers() >= 1 && topic.getId() != null) {
			return true;
		}

		return false;
	}
}
