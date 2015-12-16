package tests;

import static org.junit.Assert.*;

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
import net.jini.core.entry.UnusableEntryException;
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

		String randomName = RandomStringUtils.randomAlphabetic(50);
		JMSTopic topic = new JMSTopic(randomName, aUser);
		JMSTopic topicDupe = new JMSTopic(randomName, aUser);
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

		String randomName = RandomStringUtils.randomAlphabetic(50);
		JMSTopic topic = new JMSTopic(randomName.toLowerCase(), aUser);
		JMSTopic topicDupe = new JMSTopic(randomName.toUpperCase(), aUser);
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
		String randomName = RandomStringUtils.randomAlphabetic(50);
		JMSTopic topic = new JMSTopic(randomName.substring(0, 25) + "£$$£" + randomName.substring(24), aUser);
		JMSTopic topicDupe = new JMSTopic(randomName.substring(0, 25) + "£$£$%£%£" + randomName.substring(24), aUser);
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
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createDebugUser(aUser));
			leases.add(topicService.createTopic(topic));

			topicService.deleteTopic(topic, new JMSUser(randomName, randomName));

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
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);

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
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);

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
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);

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
	@Test
	public void testGetAllTopicUsers() {
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);
		JMSUser user1 = new JMSUser(randomName + "1", randomName + "1");
		JMSUser user2 = new JMSUser(randomName + "2", randomName + "2");
		JMSUser user3 = new JMSUser(randomName + "3", randomName + "3");

		try {
			leases.add(userService.createDebugUser(user1));
			leases.add(userService.createDebugUser(user2));
			leases.add(userService.createDebugUser(user3));

			leases.add(topicService.createTopic(topic));

			leases.add(topicService.addDebugTopicUser(topic, user1));
			leases.add(topicService.addDebugTopicUser(topic, user2));
			leases.add(topicService.addDebugTopicUser(topic, user3));

			JavaSpace05 space = SpaceService.getSpace();

			assertEquals("Users not successfully added", 3,
					lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic, null)).size());

			assertEquals("Failed to get all 3 topic users", 3, topicService.getAllTopicUsers(topic).size());

		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException e) {
			fail("Unexpected exception thrown.");
		}
	}

	// Test manual TopicUser removal
	@Test
	public void testTopicUserRemoval() {
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);
		JMSUser user1 = new JMSUser(randomName + "1", randomName + "1");
		JMSUser user2 = new JMSUser(randomName + "2", randomName + "2");
		JMSUser user3 = new JMSUser(randomName + "3", randomName + "3");

		try {
			leases.add(userService.createDebugUser(user1));
			leases.add(userService.createDebugUser(user2));
			leases.add(userService.createDebugUser(user3));

			leases.add(topicService.createTopic(topic));

			leases.add(topicService.addDebugTopicUser(topic, user1));
			leases.add(topicService.addDebugTopicUser(topic, user2));
			leases.add(topicService.addDebugTopicUser(topic, user3));

			JavaSpace05 space = SpaceService.getSpace();

			assertEquals("Users not successfully added", 3,
					lookupHelper.findAllMatchingTemplate(space, new JMSTopicUser(topic, null)).size());

			topicService.removeTopicUser(topic, user2);

			List<JMSTopicUser> topicUsersFound = topicService.getAllTopicUsers(topic);
			assertEquals("Failed to remove 1 topic users", 2, topicUsersFound.size());

			for (int i = 0; i < topicUsersFound.size(); i++) {
				assertFalse(topicUsersFound.get(i).getUser().getId().equals(user2.getId()));
			}

		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException e) {
			fail("Unexpected exception thrown.");
		}
	}

	// Test get topic by id
	@Test
	public void testGetTopicById() {
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);

		try {
			leases.add(topicService.createTopic(topic));
			
			JMSTopic topicFound = topicService.getTopicById(topic.getId());
			
			assertEquals(topic.getId(), topicFound.getId());
		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException e) {
			fail("Unexpected exception thrown.");
		}
	}
	
	// Test get topic by base name
	@Test
	public void testGetTopicByBaseName() {
		String randomName = RandomStringUtils.randomAlphabetic(50);

		JMSTopic topic = new JMSTopic(randomName, aUser);

		try {
			leases.add(topicService.createTopic(topic));
			
			JMSTopic topicFound = topicService.getTopicByBaseName(topic.getBaseName());
			
			assertEquals(topic.getId(), topicFound.getId());
		} catch (RemoteException | TransactionException | InvalidAttributeValueException | DuplicateEntryException e) {
			fail("Unexpected exception thrown.");
		}
	}
	
	// Test getting all topics
	@Test
	public void testGetAllTopics() {
		try {
			int initialNumOfTopics = topicService.getAllTopics().size();			
			int numTopicsToAdd = 5;
			int numTopicsToRemove = numTopicsToAdd;

			String randomName = RandomStringUtils.randomAlphabetic(50);
			JMSTopic template = new JMSTopic(randomName, new JMSUser());

			for(int i = 0; i < numTopicsToAdd; i++) {
				leases.add(topicService.createDebugTopic(template));
			}
			
			assertTrue(topicService.getAllTopics().size() == initialNumOfTopics + numTopicsToAdd);
			
			JavaSpace05 space = SpaceService.getSpace();
			for(int i = 0; i < numTopicsToAdd; i++) {
				space.takeIfExists(template, null, 500l);
			}
			
			assertTrue(topicService.getAllTopics().size() == initialNumOfTopics);
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			fail("Unexpected exception thrown.");
		}
	}
}
