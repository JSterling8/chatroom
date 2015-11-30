package exceptions;

public class DuplicateEntryException extends Exception {
	private static final long serialVersionUID = -7972952713023483444L;

	public DuplicateEntryException() {
		super();}
	
	public DuplicateEntryException(String message) {
		super(message);
	}

}
