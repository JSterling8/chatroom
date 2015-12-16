package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.file.AccessDeniedException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.naming.directory.InvalidAttributeValueException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import exceptions.DuplicateEntryException;
import exceptions.ResourceNotFoundException;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSTopicUser;
import models.JMSUser;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;
import services.MessageService;
import services.SpaceService;
import services.TopicService;
import services.UserService;
import services.helper.EntryLookupHelper;

public class TopicServiceTest {
	private JMSUser aUser;
	private TopicService topicService;
	private UserService userService;
	private MessageService messageService;
	private EntryLookupHelper lookupHelper;
	private List<Lease> leases;

	@Before
	public void setup() {
		// Regular users can't user have special-character only names, so
		// nothing will be duplicated. Same with topic names.
		aUser = new JMSUser("$$$", "$$$");

		topicService = TopicService.getTopicService();
		userService = UserService.getUserService();
		messageService = MessageService.getMessageService();
		lookupHelper = new EntryLookupHelper();

		leases = new ArrayList<Lease>();
	}

	@After
	public void teardown() {
		for (Lease lease : leases) {
			try {
				if (lease != null) {
					lease.cancel();
				}
			} catch (UnknownLeaseException | RemoteException e) {
				// Ignore. Some leases are not cancelled because they are
				// deleted elsewhere
			}
		}
	}

	// Creating topic with blank name fails
	@Test
	public void testBlankTopicNameFails() {
		JMSTopic topic = new JMSTopic("", aUser);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));
		} catch (RemoteException | DuplicateEntryException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (InvalidAttributeValueException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}

	// Creating topic with only special char name fails
	@Test
	public void testOnlySpecialCharacterTopicNameFails() {
		JMSTopic topic = new JMSTopic("$!£%", aUser);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));
		} catch (RemoteException | DuplicateEntryException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (InvalidAttributeValueException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}

	// Creating duplicate topic name fails
	@Test
	public void testExactDuplicateTopicNameFails() {
		// Using a 50 character random name here to avoid collision with
		// existing name. With 50 characters, the collision chance is
		// 1 / (26^50)

		String randomTopicName = RandomStringUtils.randomAlphabetic(50);
		JMSTopic topic = new JMSTopic(randomTopicName, aUser);
		JMSTopic topicDupe = new JMSTopic(randomTopicName, aUser);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));
			leases.add(topicService.createTopic(topicDupe));
		} catch (RemoteException | InvalidAttributeValueException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (DuplicateEntryException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}

	// Creating duplicate topic name (but uppercase) fails
	@Test
	public void testUppercaseDuplicateTopicNameFails() {
		// Using a 50 character random name here to avoid collision with
		// existing name. With 50 characters, the collision chance is
		// 1 / (26^50)

		String randomTopicName = RandomStringUtils.randomAlphabetic(50);
		JMSTopic topic = new JMSTopic(randomTopicName.toLowerCase(), aUser);
		JMSTopic topicDupe = new JMSTopic(randomTopicName.toUpperCase(), aUser);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));
			leases.add(topicService.createTopic(topicDupe));
		} catch (RemoteException | InvalidAttributeValueException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (DuplicateEntryException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}

	// Creating duplicate topic name (but special characters) fails
	@Test
	public void testSpecialCharsDuplicateTopicNameFails() {
		// Using a 50 character random name here to avoid collision with
		// existing name. With 50 characters, the collision chance is
		// 1 / (26^50)
		String randomTopicName = RandomStringUtils.randomAlphabetic(50);
		JMSTopic topic = new JMSTopic(randomTopicName.substring(0, 25) + "£$$£" + randomTopicName.substring(24), aUser);
		JMSTopic topicDupe = new JMSTopic(randomTopicName.substring(0, 25) + "£$£$%£%£" + randomTopicName.substring(24),
				aUser);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));
			leases.add(topicService.createTopic(topicDupe));
		} catch (RemoteException | InvalidAttributeValueException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (DuplicateEntryException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}

	// Deleting topic by non owner fails
	@Test
	public void testDeleteTopicByNonOwnerFails() {
		String randomTopicName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomTopicName, aUser);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));

			topicService.deleteTopic(topic, new JMSUser(randomTopicName, randomTopicName));

		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException e) {
			fail("Unexpected exception thrown.");
		} catch (AccessDeniedException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
		assertTrue("Topic was deleted when it should not have been",
				topicService.getTopicByBaseName(topic.baseName).getId().equals(topic.getId()));
	}

	// Deleting topic by owner does work
	@Test
	public void testDeleteTopicByOwnerAllowed() {
		String randomTopicName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomTopicName, aUser);

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));

			topicService.deleteTopic(topic, aUser);
			assertTrue("Topic was not successfully deleted", topicService.getTopicByBaseName(topic.baseName) == null);

		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException
				| AccessDeniedException e) {
			fail("Unexpected exception thrown.");
		}
	}

	// Deleting topic removes all messages
	@Test
	public void testDeletingTopicRemovesMessages() {
		String randomTopicName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomTopicName, aUser);

		JMSMessage message = new JMSMessage(topic);
		message.setFrom(aUser);
		message.setTo(aUser);
		message.setId(UUID.randomUUID());
		message.setMessage("Test message");
		message.setSentDate(new Date());

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));
			leases.add(topicService.addTopicUser(topic, aUser));

			int numMessages = 10;
			for (int i = 0; i < numMessages; i++) {
				leases.add(messageService.sendMessage(message));
			}

			JavaSpace05 space = SpaceService.getSpace();

			assertEquals("Messages not successfully sent", numMessages,
					lookupHelper.findAllMatchingTemplate(space, message).size());

			topicService.deleteTopic(topic, aUser);

			assertEquals("Messages were not deleted when topic was deleted", 0,
					lookupHelper.findAllMatchingTemplate(space, message).size());

		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException
				| AccessDeniedException | ResourceNotFoundException e) {
			fail("Unexpected exception thrown.");
		}
	}

	// Deleting topic removes all TopicUsers
	@Test
	public void testDeletingTopicRemovesTopicUsers() {
		String randomTopicName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomTopicName, aUser);

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));

			int numUsers = 10;
			for (int i = 0; i < numUsers; i++) {
				leases.add(topicService.addDebugTopicUser(topic, aUser));
			}

			JavaSpace05 space = SpaceService.getSpace();

			assertEquals("Users not successfully added", numUsers,
					lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic, aUser)).size());

			topicService.deleteTopic(topic, aUser);

			assertEquals("TopicUsers were not deleted when topic was deleted", 0,
					lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic, aUser)).size());

		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException
				| AccessDeniedException e) {
			fail("Unexpected exception thrown.");
		}
	}
	
	// Test getting all TopicUsers
	
	// Test manual TopicUser removal
	
	// Test get topic by id
	
	// Test getting all topics
}
