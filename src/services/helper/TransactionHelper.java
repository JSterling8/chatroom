package services.helper;

import java.rmi.RemoteException;

import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.Transaction.Created;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import services.SpaceService;

/**
 * A simple helper class for getting a transaction.
 * 
 * @author Jonathan Sterling
 *
 */
public class TransactionHelper {
	public static final long DEFAULT_TIMEOUT_IN_MILLIS = 3000l;

	private TransactionHelper() {
		// Uninstantiable
	}

	/**
	 * Gets a transaction with a given timeout
	 * 
	 * @param timeout
	 *            The timeout of the transaction that will be created
	 * 
	 * @return A transaction with a given timeout
	 */
	public static Transaction getTransaction(long timeout) {
		Transaction transaction = null;

		try {
			TransactionManager transactionManager = SpaceService.getManager();
			Created transactionCreated;
			transactionCreated = TransactionFactory.create(transactionManager, timeout);
			transaction = transactionCreated.transaction;
		} catch (LeaseDeniedException | RemoteException e) {
			System.err.println("Failed to create transaction.");
			e.printStackTrace();
		}

		return transaction;
	}

	/**
	 * Overload of getTransaction(long timeout). Uses a default timeout of 3000
	 * milliseconds
	 * 
	 * @return A Transaction with a 3000 ms timeout
	 */
	public static Transaction getTransaction() {
		return getTransaction(DEFAULT_TIMEOUT_IN_MILLIS);
	}
}
