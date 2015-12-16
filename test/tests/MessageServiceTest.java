package tests;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import exceptions.ResourceNotFoundException;
import models.JMSMessage;
import models.JMSTopic;
import models.JMSUser;
import services.MessageService;

public class MessageServiceTest {

	// Null topics cannot have messages sent
	// Get all messages for user
	// Check pm to another user not visible to user who didn't send it
	// Check pm visible to from user
	// Check pm visible to to user
	// Check messages in correct order
	// Check pm only to user who is currently in topic

	@Before
	public void setup() {
		
	}
	
	@Test
	public void checkNonExistantTopicCantHaveMessageSent() {
		// Regular users can't user have special-character only names, so
		// nothing will be duplicated. Same with topic names.
		JMSUser user = new JMSUser("$$$", "$$$");
		JMSTopic nonExistantTopic = new JMSTopic("$$$", null);

		JMSMessage message = new JMSMessage(nonExistantTopic, new Date(), user, null, UUID.randomUUID(), "sfsdf");
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
