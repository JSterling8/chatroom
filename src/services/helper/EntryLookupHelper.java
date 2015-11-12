package services.helper;

import java.util.ArrayList;
import java.util.List;

import models.JMSMessage;
import models.JMSTopic;
import models.JMSUser;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

public class EntryLookupHelper {
	public EntryLookupHelper() {}

	@SuppressWarnings("unchecked")
	public <T extends Entry> List<T> findAllOfType(JavaSpace space, T template) {		
		Transaction transaction = null;
		List<T> entries = new ArrayList<T>();

		try {
/*			TransactionManager transactionManager = SpaceService.getManager();
			Created transactionCreated = TransactionFactory.create(transactionManager, 1000 * 10);
			transaction = transactionCreated.transaction;*/

			while (space.readIfExists(template, transaction, 1000) != null) {
				entries.add((T) space.takeIfExists(template, transaction, 1000));
			}
			
			for(Entry entry : entries) {
				space.write(entry, null, Lease.FOREVER);
			}

			/*transaction.abort();*/

		} catch (Exception e) {
/*			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}*/

			e.printStackTrace();
		}

		return entries;
	}
}
