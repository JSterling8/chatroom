package services.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;

public class EntryLookupHelper implements Serializable {
	private static final long serialVersionUID = -6457049864685809692L;

	public EntryLookupHelper() {}

	@SuppressWarnings("unchecked")
	public <T extends Entry> List<T> findAllMatchingTemplate(JavaSpace05 space, T template) {		
		Transaction transaction = null;
		List<T> entries = new ArrayList<T>();

		try {
			transaction = TransactionHelper.getTransaction();

			List<T> templateList = new ArrayList<T>(1);
			templateList.add(template);
			
			//TODO - Assert there are never more than 20000 entries in the space?
			MatchSet matchSet = space.contents(templateList, transaction, 500, 20000);
			
			T entry = (T) matchSet.next();
			while(entry != null){
				entries.add(entry);
				entry = (T) matchSet.next();
			}
			
			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}

			System.err.println("Failed to get all entries of type");
			e.printStackTrace();
		}

		return entries;
	}
}
