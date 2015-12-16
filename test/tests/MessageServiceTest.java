package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import exceptions.ResourceNotFoundException;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSUser;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import services.MessageService;
import services.TopicService;
import services.UserService;

public class MessageServiceTest {
	private JMSUser user;
	private JMSTopic topic;
	private JMSMessage message;
	private MessageService messageService;
	private TopicService topicService;
	private UserService userService;
	private List<Lease> leases;

	@Before
	public void setup() {
		// Regular users can't user have special-character only names, so
		// nothing will be duplicated. Same with topic names.
		user = new JMSUser("$$$", "$$$");
		topic = new JMSTopic("$$$", user);
		message = new JMSMessage(topic, new Date(), user, null, UUID.randomUUID(), "sfsdf");

		messageService = MessageService.getMessageService();
		topicService = TopicService.getTopicService();
		userService = UserService.getUserService();

		leases = new ArrayList<Lease>();
	}

	@After
	public void teardown() {
		for (Lease lease : leases) {
			try {
				lease.cancel();
			} catch (UnknownLeaseException | RemoteException e) {
				// Ignore...
			}
		}
	}

	@Test
	public void checkNonExistantTopicCantHaveMessageSent() {
		boolean failedAsExpected = false;
		try {
			leases.add(messageService.sendMessage(message));
		} catch (RemoteException e) {
			failedAsExpected = false;
		} catch (ResourceNotFoundException e) {
			failedAsExpected = true;
		}

		assertTrue("Expected exception was not thrown. Users should not be able to send messages "
				+ "in topics that don't exist in the space", failedAsExpected);
	}

	@Test
	public void checkNullTopicCantHaveMessageSent() {
		boolean failedAsExpected = false;
		try {
			message.setTopic(null);
			leases.add(messageService.sendMessage(message));
		} catch (RemoteException e) {
			failedAsExpected = false;
		} catch (ResourceNotFoundException e) {
			failedAsExpected = true;
		}

		assertTrue("Expected exception was not thrown. Users should not be able to send messages "
				+ "in topics that don't exist in the space", failedAsExpected);
	}

	@Test
	public void getAllPublicMessages() {
		List<JMSMessage> messagesToPutInSpace = new ArrayList<JMSMessage>();

		try {
			leases.add(userService.createDebugUser(user));
			leases.add(topicService.createDebugTopic(topic));

			for (int i = 0; i < 10; i++) {
				message = new JMSMessage(topic, new Date(), user, null, UUID.randomUUID(), "sfsdf");
				messagesToPutInSpace.add(message);

				leases.add(messageService.sendMessage(message));
			}
		} catch (Exception e) {
			fail("Failed to put Entries in space for test.");

			return;
		}

		List<JMSMessage> messagesInSpace = messageService.getAllMessagesForUserInTopic(topic, user);

		assertEquals("Failed to retrieve all messages", messagesToPutInSpace.size(), messagesInSpace.size());

		for (int i = 0; i < messagesInSpace.size(); i++) {
			JMSMessage messageInSpace = messagesInSpace.get(i);
			JMSMessage messageExpected = messagesToPutInSpace.get(i);

			assertEquals(messageExpected.getId(), messageInSpace.getId());
		}
	}

	@Test
	public void testPrivateMessages() {
		List<JMSMessage> messagesToPutInSpace = new ArrayList<JMSMessage>();
		JMSUser userFrom = user;
		JMSUser userTo = new JMSUser("@@@", "@@@");
		JMSUser userRandom = new JMSUser("£££", "£££");

		int numPrivateMessagesToSend = 10;

		try {
			leases.add(topicService.createDebugTopic(topic));

			leases.add(userService.createDebugUser(userTo));
			leases.add(userService.createDebugUser(userFrom));
			leases.add(userService.createDebugUser(userRandom));

			leases.add(topicService.addDebugTopicUser(topic, userFrom));
			leases.add(topicService.addDebugTopicUser(topic, userTo));
			leases.add(topicService.addDebugTopicUser(topic, userRandom));

			for (int i = 0; i < numPrivateMessagesToSend; i++) {
				message = new JMSMessage(topic, new Date(), userFrom, userTo, UUID.randomUUID(), "test message");
				messagesToPutInSpace.add(message);

				messageService.sendMessage(message);
			}

			List<JMSMessage> messagesForRandomUser = messageService.getAllMessagesForUserInTopic(topic, userRandom);
			List<JMSMessage> messagesForToUser = messageService.getAllMessagesForUserInTopic(topic, userTo);
			List<JMSMessage> messagesForFromUser = messageService.getAllMessagesForUserInTopic(topic, userFrom);

			assertEquals(0, messagesForRandomUser.size());
			assertEquals(numPrivateMessagesToSend, messagesForToUser.size());
			assertEquals(numPrivateMessagesToSend, messagesForFromUser.size());
		} catch (Exception e) {
			fail("Failed to put Entries in space for test.");

			return;
		}
	}

	@Test
	public void testMessageToUserNotInTopicFails() {
		JMSUser userFrom = user;
		JMSUser userTo = new JMSUser("@@@", "@@@");
		
		boolean expectedExceptionThrown = false;
		
		try {
			leases.add(topicService.createDebugTopic(topic));

			leases.add(userService.createDebugUser(userFrom));
			leases.add(userService.createDebugUser(userTo));

			leases.add(topicService.addDebugTopicUser(topic, userFrom));

			message = new JMSMessage(topic, new Date(), userFrom, userTo, UUID.randomUUID(), "test message");
		} catch (Exception e) {
			fail("Failed to put Entries in space for test.");
			
			return;
		}
		
		try {
			messageService.sendMessage(message);
		} catch (RemoteException | ResourceNotFoundException e) {
			expectedExceptionThrown = true;
			
			List<JMSMessage> messagesForToUser = messageService.getAllMessagesForUserInTopic(topic, userTo);
			assertEquals(0, messagesForToUser.size());
		}
		
		assertTrue(expectedExceptionThrown);
	}
}
