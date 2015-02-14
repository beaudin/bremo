package bremoExceptions;

import java.util.Calendar;

import berechnungsModule.ErgebnisBuffer;

public class ErrorZoneException extends StopBremoException {
	
	ErgebnisBuffer ergBuf;
	
	private static final long serialVersionUID = 1L;

	public ErrorZoneException(String message){
		super(message);
	}
	
	public ErrorZoneException(String message, ErgebnisBuffer ergBufIN){
		super(message);
		this.ergBuf = ergBufIN;
		schreibeBuffer();
	}
	
	private void schreibeBuffer(){
		Calendar cal = Calendar.getInstance();
		String datetime = ""+cal.get(Calendar.YEAR)+""+cal.get(Calendar.MONTH)+""+cal.get(Calendar.DAY_OF_MONTH)+
				"_"+cal.get(Calendar.HOUR_OF_DAY)+""+cal.get(Calendar.MINUTE)+""+cal.get(Calendar.SECOND);
		ergBuf.schreibeErgebnisFile("ZONENFEHLER_LWA_"+datetime+".txt");
	}
}
