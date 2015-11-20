package services.helper;

import java.rmi.RemoteException;

import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.Transaction.Created;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import services.SpaceService;

public class TransactionHelper {
	public static final long DEFAULT_TIMEOUT_IN_MILLIS = 3000l;
	
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

	public static Transaction getTransaction() {
			return getTransaction(DEFAULT_TIMEOUT_IN_MILLIS);
	}

}
