package bremoExceptions;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bremo.parameter.CasePara;

public class ParameterFileWrongInputException extends StopBremoException{

	public ParameterFileWrongInputException(String message) {
		super(message);		
	}
	
	public void eingabeErforderlich(CasePara cp, String[] parameter, String vorgabe){
		new eingabeFeld(cp, parameter, vorgabe);
	}
}
