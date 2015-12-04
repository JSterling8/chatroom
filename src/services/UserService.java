package services;

import java.rmi.RemoteException;

import org.apache.commons.lang3.StringUtils;

import exceptions.DuplicateEntryException;
import exceptions.ResourceNotFoundException;
import models.JMSUser;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace05;
import services.helper.TransactionHelper;

public class UserService {
	private static final long NINETY_DAYS_IN_MILLIS = 1000l * 60l * 60l * 24l * 90l;

	private static final JavaSpace05 space = SpaceService.getSpace();
	
	private static UserService userService;
	
	private UserService(){}
	
	public static UserService getUserService(){
		if (userService == null){
			userService = new UserService();
		}
		
		return userService;
	}
	
	/**
	 * 
	 * @param user The JMSUser to add to the space.
	 * @return <code>true</code> if successfully created.  <code>false</code> if not.
	 * @throws RemoteException 
	 * @throws TransactionException 
	 * @throws DuplicateEntryException 
	 */
	public boolean createUser(JMSUser user) throws RemoteException, TransactionException, DuplicateEntryException{
		isValidUser(user);
		
		Transaction transaction = TransactionHelper.getTransaction();
		
		if(getUserByBaseName(user.getBaseName(), transaction) == null){
			space.write(user, transaction, NINETY_DAYS_IN_MILLIS);
			transaction.commit();
		} else {
			throw new DuplicateEntryException("User with name: '" + user.getName() + "' already exists.");
		}
		
		return true;
	}
	
	public void renewUserLease(JMSUser user) throws ResourceNotFoundException{
		isValidUser(user);
		
		Transaction transaction = TransactionHelper.getTransaction();
		
		JMSUser userFromSpace = getUserByBaseName(user.getBaseName(), transaction);
		
		if(userFromSpace != null){
			try {
				space.takeIfExists(userFromSpace, transaction, 1000);
				space.write(user, transaction, NINETY_DAYS_IN_MILLIS);
				transaction.commit();
			} catch (RemoteException | TransactionException | InterruptedException | UnusableEntryException e) {
				System.err.println("Failed to renew user's lease");
				e.printStackTrace();
			}
		} else {
			throw new ResourceNotFoundException("Failed to renew lease.  User does not exist in space.");
		}
	}
	
	private boolean isValidUser(JMSUser user) {
		if(StringUtils.isNotBlank(user.getBaseName()) &&
				StringUtils.isNotBlank(user.getName()) &&
				StringUtils.isNotBlank(user.getId().toString()) &&
				StringUtils.isNotBlank(user.getPassword())) {
			return true;
		}
		
		return false;
	}

	/**
	 * This method performs a <code>read</code> not a <code>take</code>.
	 * 
	 * @param baseName The base name to search for
	 * @param transaction Optional.  Can be null.
	 * 
	 * @return A copy of the first JMSUser account found that has the base name specified, or null if none exist.
	 */
	public JMSUser getUserByBaseName(String baseName, Transaction transaction){
		JMSUser template = new JMSUser();
		template.setBaseName(baseName);

		JMSUser userFound = null;
		
		try {
			userFound = (JMSUser) space.readIfExists(template, transaction, 3000);
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			e.printStackTrace();
		}
		
		return userFound;
	}
	
	public JMSUser getUserByBaseName(String baseName){
		return getUserByBaseName(baseName, null);
	}
	
	public String getBaseNameFromName(String name){
		String baseName = name;
		baseName = baseName.replaceAll("[^A-Za-z0-9]", "");
		baseName = baseName.toUpperCase();
		
		return baseName;
	}
}
