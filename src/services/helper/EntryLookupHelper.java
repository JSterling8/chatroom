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

public class EntryLookupHelper implements Serializable {
	private static final long serialVersionUID = -6457049864685809692L;

	public EntryLookupHelper() {}

	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	public <T extends Entry> List<T> findAllMatchingTemplate(JavaSpace05 space, T template, Transaction transaction) {		
		List<T> entries = new ArrayList<T>();

		try {
			List<T> templateList = new ArrayList<T>(1);
			templateList.add(template);
			
			MatchSet matchSet = space.contents(templateList, transaction, 500, Long.MAX_VALUE);
			
			T entry = (T) matchSet.next();
			while(entry != null){
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

			System.err.println("Failed to read all entries of type");
			e.printStackTrace();
		}

		return entries;
	}
	
	public <T extends Entry> List<T> takeAllMatchingTemplate(JavaSpace05 space, T template) {		
		Transaction transaction = TransactionHelper.getTransaction();

		List<T> entries = takeAllMatchingTemplate(space, template, transaction);
		
		try {
			transaction.commit();
		} catch (UnknownTransactionException | CannotCommitException | RemoteException e) {
			System.err.println("Failed to commit transaction.");
			
			e.printStackTrace();
		}
		
		return entries;
	}
	
	/**
	 * <b>Does not commit transaction</b>
	 * 
	 * @param space
	 * @param template
	 * @param transaction
	 * @return
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
