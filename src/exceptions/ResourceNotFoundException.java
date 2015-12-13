package exceptions;

/**
 * An exception for use when a user attempts to access an Entry that does not
 * exist in the space.
 * 
 * @author Jonathan Sterling
 *
 */
public class ResourceNotFoundException extends Exception {
	private static final long serialVersionUID = -7276934995576677248L;

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
