package exceptions;

/**
 * An exception for use when a user attempts to add a duplicate Entry where only
 * unique entries are permitted
 * 
 * @author Jonathan Sterling
 *
 */
public class DuplicateEntryException extends Exception {
	private static final long serialVersionUID = -7972952713023483444L;

	public DuplicateEntryException() {
		super();
	}

	public DuplicateEntryException(String message) {
		super(message);
	}

}
