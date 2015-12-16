package services.helper;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.space.JavaSpace05;
import net.jini.space.MatchSet;

/**
 * A generic bulk entry lookup tool.
 * 
 * @author Jonathan Sterling
 *
 */
public class EntryLookupHelper implements Serializable {
	private static final long serialVersionUID = -6457049864685809692L;

	public EntryLookupHelper() {
	}

	/**
	 * Finds(reads) all entries that match a given Entry template in a given
	 * space.
	 * 
	 * Overload of the findAllMatchingTemplate(JavaSpace05 space, T template,
	 * Transaction transaction). Simply creates a transaction, calls the other
	 * findAllMatchingTemplate() method, then commits the transaction before
	 * returning the entries.
	 * 
	 * @param space
	 *            The space to search for entries in.
	 * @param template
	 *            The template to search for.
	 * @return All entries that match the given template in the given space.
	 */
	public <T extends Entry> List<T> findAllMatchingTemplate(JavaSpace05 space, T template) {
		Transaction transaction = TransactionHelper.getTransaction();

		List<T> entries = findAllMatchingTemplate(space, template, transaction);

		try {
			transaction.commit();
		} catch (UnknownTransactionException | CannotCommitException | RemoteException e) {
			System.err.println("Failed to commit transaction.");
			e.printStackTrace();
		}

		return entries;
	}

	/**
	 * Finds(reads) all entries that match a given Entry template in a given
	 * space.
	 * 
	 * <b>Does not commit the transaction</b>
	 * 
	 * @param space
	 *            The space to search for entries in.
	 * @param template
	 *            The template to search for.
	 * @param transaction
	 *            The transaction in which the lookup will occur
	 * @return All entries that match the given template in the given space.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Entry> List<T> findAllMatchingTemplate(JavaSpace05 space, T template, Transaction transaction) {
		// The list of entries that will eventually be returned.
		List<T> entries = new ArrayList<T>();

		try {
			// JavaSpace05's "contents" method requires a collection of
			// templates, so we create a collection of one element, containing
			// only the passed-in template
			List<T> templateList = new ArrayList<T>(1);
			templateList.add(template);

			// JavaSpace05's "contents()" method is effectively a "readAll()"
			// method. This method is dangerous if there are a large amount of
			// Entry objects
			MatchSet matchSet = space.contents(templateList, transaction, 500, Long.MAX_VALUE);

			// Iterate through the matchset and put each entry into the entries
			// ArrayList
			T entry = (T) matchSet.next();
			while (entry != null) {
				entries.add(entry);
				entry = (T) matchSet.next();
			}
		} catch (Exception e) {
			if (transaction != null) {
				try {
					transaction.abort();
				} catch (Exception e1) {
					System.err.println("Failed to abort transaction");
					e1.printStackTrace();
				}
			}

			System.err.println("Failed to read all entries of type " + template.getClass().getSimpleName());
			e.printStackTrace();
		}

		return entries;
	}

	/**
	 * Takes all Entries matching a given template from a given space. This
	 * method overloads the takeAllMatchingTemplate(JavaSpace05 space, T
	 * template, Transaction transaction) method
	 * 
	 * @param space
	 *            The space to take Entries from
	 * @param template
	 *            The template to match Entries against
	 * 
	 * @return All entries that match the given template from the given space
	 */
	public <T extends Entry> List<T> takeAllMatchingTemplate(JavaSpace05 space, T template) {
		try {
			Transaction transaction = TransactionHelper.getTransaction();
			List<T> entries = takeAllMatchingTemplate(space, template, transaction);
			transaction.commit();

			return entries;
		} catch (UnknownTransactionException | CannotCommitException | RemoteException e) {
			System.err.println("Failed to commit transaction.");
			e.printStackTrace();

			return new ArrayList<T>();
		}

	}

	/**
	 * Takes all Entries matching a given template from a given space, inside of
	 * a given transaction.
	 * 
	 * <b>Does not commit the passed-in transaction</b>
	 * 
	 * @param space
	 *            The space to take Entries from
	 * @param template
	 *            The template to match Entries against
	 * @param transaction
	 *            The transaction to run the takeAll inside of
	 * 
	 * @return All entries that match the given template from the given space
	 */
	@SuppressWarnings("unchecked")
	public <T extends Entry> List<T> takeAllMatchingTemplate(JavaSpace05 space, T template, Transaction transaction) {
		List<T> entries = new ArrayList<T>();

		try {
			List<T> templateList = new ArrayList<T>(1);
			templateList.add(template);

			entries.addAll(space.take(templateList, transaction, 500, Long.MAX_VALUE));
		} catch (Exception e) {
			System.err.println("Failed to take all entries of type");
			e.printStackTrace();
		}

		return entries;
	}
}
