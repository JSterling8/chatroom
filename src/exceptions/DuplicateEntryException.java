package exceptions;

public class DuplicateEntryException extends Exception {

	public DuplicateEntryException() {
		super();}
	
	public DuplicateEntryException(String message) {
		super(message);
	}

}
