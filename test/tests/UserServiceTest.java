package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.InvalidAttributeValueException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import exceptions.DuplicateEntryException;
import exceptions.ResourceNotFoundException;
import models.JMSUser;
import net.jini.core.lease.Lease;
import net.jini.core.lease.UnknownLeaseException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import services.UserService;
import services.helper.TransactionHelper;

public class UserServiceTest {
	private UserService userService;
	private List<Lease> leases;
	
	@Before
	public void setup() {
		userService = UserService.getUserService();
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
	
	// Duplicate user identical
	@Test
	public void testIdenticalNamesFail() {
		String randomName = RandomStringUtils.randomAlphabetic(50);
		JMSUser user = new JMSUser(randomName, randomName);
		JMSUser userDupe = new JMSUser(randomName, randomName);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createUser(user));
			leases.add(userService.createUser(userDupe));
		} catch (RemoteException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (DuplicateEntryException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}
	
	// Duplicate user case change
	@Test
	public void testSameNameDifferentCaseFails() {
		String randomName = RandomStringUtils.randomAlphabetic(50);
		JMSUser user = new JMSUser(randomName.toLowerCase(), randomName.toLowerCase());
		JMSUser userDupe = new JMSUser(randomName.toUpperCase(), randomName.toUpperCase());
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createUser(user));
			leases.add(userService.createUser(userDupe));
		} catch (RemoteException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (DuplicateEntryException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}
	
	// Duplicate user special chars
	@Test
	public void testSameNameDifferentSpecialCharsFails() {
		String randomName = RandomStringUtils.randomAlphabetic(50);
		String randomNameWithSpecialChars1 = randomName.substring(0, 25) + "£$$£" + randomName.substring(24);
		String randomNameWithSpecialChars2 = randomName.substring(0, 25) + "£$£$%£%£" + randomName.substring(24);
				
		JMSUser user = new JMSUser(randomNameWithSpecialChars1, randomNameWithSpecialChars1);
		JMSUser userDupe = new JMSUser(randomNameWithSpecialChars2, randomNameWithSpecialChars2);
		boolean expectedExceptionThrown = false;

		try {
			leases.add(userService.createUser(user));
			leases.add(userService.createUser(userDupe));
		} catch (RemoteException | TransactionException e) {
			fail("Unexpected error thrown");
		} catch (DuplicateEntryException e) {
			expectedExceptionThrown = true;
		}

		assertTrue(expectedExceptionThrown);
	}
	
	// Get user by basename transactionally
	@Test
	public void testGettingUserByBaseNameTransactionally() {
		String randomName = RandomStringUtils.randomAlphabetic(50);
		JMSUser expectedUser = new JMSUser(randomName, randomName);

		try {
			leases.add(userService.createUser(expectedUser));
			
			Transaction transaction = TransactionHelper.getTransaction();
			JMSUser actualUser = userService.getUserByBaseName(expectedUser.getBaseName(), transaction);
			transaction.commit();
			
			assertEquals(expectedUser.getId(), actualUser.getId());
		} catch (RemoteException | TransactionException | DuplicateEntryException e) {
			fail("Unexpected error thrown");
		}
	}
	
	// Renew user lease
	@Test
	public void testLeaseRenewal() {
		String randomName = RandomStringUtils.randomAlphabetic(50);
		JMSUser user = new JMSUser(randomName, randomName);

		try {
			Lease leaseInitial = userService.createUser(user);
			leases.add(leaseInitial);
			
			Lease leaseExtended = userService.renewUserLease(user);
			leases.add(leaseExtended);
			
			assertTrue(leaseInitial.getExpiration() < leaseExtended.getExpiration());
		} catch (RemoteException | TransactionException | DuplicateEntryException | InvalidAttributeValueException | ResourceNotFoundException e) {
			fail("Unexpected error thrown");
		}
	}
	
	// Get basename from name
	@Test
	public void testGettingBaseNameFromName() {
		String name = "#$#ArEgUlAr#$@Name";
		String expectedBaseName = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
		
		assertEquals(expectedBaseName, userService.getBaseNameFromName(name));
	}
}
