package bremoExceptions;

public class NegativeMassException extends StopBremoException {

	
	private static final long serialVersionUID = 1L;

	public NegativeMassException(String message){
		super(message);
	}
}
