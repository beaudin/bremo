package bremoExceptions;
import io.AusgabeSteurung;

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
		     log_Message();
		     AusgabeSteurung.Error(Separator + "!!!!   Bremo wurde gestopt    !!!!"+ Separator);
		     
		} catch (Exception e) {
			
		}
		throw new InvalidParameterException(Separator+super.getMessage()+Separator);
	}
	
	public void log_Message(){
		//TODO hier kann irgendwann mal ein eintrag is das logFile stehen		
		log_Error(super.getMessage());
//		LogFileWriter.addItemToLog(super.getMessage());	
	}
	
	public void log_Warning(){
		log_Warning(super.getMessage());
	}
	
	public void log_Warning(String message){
		//TODO hier kann irgendwann mal ein eintrag is das logFile stehen		
		AusgabeSteurung.Warning(Separator + "ACHTUNG: " + message+ Separator);
//		LogFileWriter.addItemToLog("WARNING: " + message);	
	}
	public void log_Error(){
		log_Error(super.getMessage());
	}
	
	public void log_Error(String message){
		//TODO hier kann irgendwann mal ein eintrag is das logFile stehen		
		AusgabeSteurung.Error(Separator + "FEHLER: " + message+ Separator);
//		LogFileWriter.addItemToLog("WARNING: " + message);	
	}
	
}
