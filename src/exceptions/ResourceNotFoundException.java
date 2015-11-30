package exceptions;

public class ResourceNotFoundException extends Exception {
	private static final long serialVersionUID = -7276934995576677248L;

	public ResourceNotFoundException(String message){
		super(message);
	}
}
