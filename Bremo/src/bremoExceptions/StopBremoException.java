package bremoExceptions;
import java.security.InvalidParameterException;

import bremoswing.SwingBremo;
import BremoLogFile.LogFileWriter;


//@SuppressWarnings("serial")
public abstract class StopBremoException extends Exception {

	private static final long serialVersionUID = 1L;
	private static String Separator  ="\n**************************************************\n";
	
	public StopBremoException(String message){
		super(message);
	}

	public void stopBremo() {
		//System.err.println(Separator+super.getMessage()+Separator);	
		 //Dieser Befehl wird benoetigt um das Programm zu stoppen wenn es nicht als swing app laeuft
//		LogFileWriter.addItemToLog(super.getMessage());
		try {
		     SwingBremo.StateBremoThread();
		} catch (Exception e) {
			
		}
		throw new InvalidParameterException(Separator+super.getMessage()+Separator);
	}
	
	public void log_Message(){
		//TODO hier kann irgendwann mal ein eintrag is das logFile stehen		
		System.err.println(super.getMessage()+Separator);
//		LogFileWriter.addItemToLog(super.getMessage());	
	}
	
	public void log_Warning(){
		log_Warning(super.getMessage()+ Separator);
	}
	
	public void log_Warning(String message){
		//TODO hier kann irgendwann mal ein eintrag is das logFile stehen		
		System.err.println("WARNING: " + message+ Separator);
//		LogFileWriter.addItemToLog("WARNING: " + message);	
	}
	
}
