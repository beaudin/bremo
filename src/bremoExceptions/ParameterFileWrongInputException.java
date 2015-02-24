package bremoExceptions;

import bremo.parameter.CasePara;

public class ParameterFileWrongInputException extends StopBremoException {

	private static final long serialVersionUID = 1L;

	public ParameterFileWrongInputException(String message) {
		super(message);
	}

	public void eingabeErforderlich(CasePara cp, String[] parameter,
			String vorgabe) {
		new eingabeFeld(cp, parameter, vorgabe);
	}
}
