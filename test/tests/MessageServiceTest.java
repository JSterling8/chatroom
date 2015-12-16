package tests;

import static org.junit.Assert.assertTrue;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import exceptions.ResourceNotFoundException;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSUser;
import net.jini.space.JavaSpace05;
import services.MessageService;
import services.SpaceService;
import services.helper.EntryLookupHelper;

public class MessageServiceTest {

	// Null topics cannot have messages sent
	// Get all messages for user
	// Check pm to another user not visible to user who didn't send it
	// Check pm visible to from user
	// Check pm visible to to user
	// Check messages in correct order
	// Check pm only to user who is currently in topic

	private JavaSpace05 space;
	private EntryLookupHelper lookupHelper;
	private JMSUser user;
	private JMSTopic topic;
	private JMSMessage message;
	
	@Before
	public void setup() {
		// Regular users can't user have special-character only names, so
		// nothing will be duplicated. Same with topic names.
		user = new JMSUser("$$$", "$$$");
		topic = new JMSTopic("$$$", null);
		message = new JMSMessage(topic, new Date(), user, null, UUID.randomUUID(), "sfsdf");
		
		space = SpaceService.getSpace();
		lookupHelper = new EntryLookupHelper();
	}
	
	@After
	public void teardown() {
		lookupHelper.takeAllMatchingTemplate(space, user);
		lookupHelper.takeAllMatchingTemplate(space, topic);
		lookupHelper.takeAllMatchingTemplate(space, message);
	}
	
	@Test
	public void checkNonExistantTopicCantHaveMessageSent() {
		MessageService messageService = MessageService.getMessageService();

		boolean failedAsExpected = false;
		try {
			messageService.sendMessage(message);
		} catch (RemoteException e) {
			failedAsExpected = false;
		} catch (ResourceNotFoundException e) {
			failedAsExpected = true;
		}

		assertTrue("Expected exception was not thrown. Users should not be able to send messages "
				+ "in topics that don't exist in the space", failedAsExpected);
	}
}
