package bremoExceptions;

import berechnungsModule.ErgebnisBuffer;
import bremo.main.Bremo;
import bremo.parameter.CasePara;

//@SuppressWarnings("serial")
public abstract class StopBremoException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String Separator ="\n**************************************************";
	public StopBremoException(String message){
		super(message);
	}

	public void stopBremo() {
//		super.getMessage();
		super.printStackTrace();
		System.err.println(super.getMessage()+ Separator);	
//		CasePara cp=Bremo.get_casePara();
//		String name=cp.get_CaseName()+"_Excep.txt";
//		ErgebnisBuffer.schreibeAlleErgebnisFiles(name);
	
		//System.exit(0);		
	}
	
	public void log_Message(){
		//TODO hier kann irgendwann mal ein eintrag is das logFile stehen		
		System.err.println(super.getMessage());		
	}
	
	public void log_Warning(){
		log_Warning( super.getMessage());		
	}
	
	public void log_Warning(String message){
		//TODO hier kann irgendwann mal ein eintrag is das logFile stehen	
//		CasePara cp=Bremo.get_casePara();
//		if(cp.SYS.IS_KW_BASIERT)
//			System.err.println("WARNING: " 
//					+cp.convert_SEC2KW(cp.get_aktuelle_Rechenzeit())+ "KW " + message);	
//		else
//			System.err.println("WARNING: " 
//					+cp.get_aktuelle_Rechenzeit()+ "KW " + message);	
		System.err.println("WARNING: " + message);
			
	}
	
}
