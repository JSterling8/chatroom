package services.helper;

import java.util.ArrayList;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.Transaction.Created;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import services.SpaceService;

public class EntryLookupHelper {
	public EntryLookupHelper() {}

	@SuppressWarnings("unchecked")
	public <T extends Entry> List<T> findAllMatchingTemplate(JavaSpace space, T template) {		
		Transaction transaction = null;
		List<T> entries = new ArrayList<T>();

		try {
			TransactionManager transactionManager = SpaceService.getManager();
			Created transactionCreated = TransactionFactory.create(transactionManager, 1000 * 3);
			transaction = transactionCreated.transaction;

			while (space.readIfExists(template, transaction, 1000) != null) {
				entries.add((T) space.takeIfExists(template, transaction, 1000));
			}

			transaction.abort();

		} catch (Exception e) {
			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}

			e.printStackTrace();
		}

		return entries;
	}
}
