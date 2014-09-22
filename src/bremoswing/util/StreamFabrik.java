package bremoswing.util;

import java.io.PrintWriter;
import bremoswing.SwingBremo;

public final class StreamFabrik {

	public static  PrintWriter warningPrintWriter = null;
	
	public StreamFabrik ()  {
		initializeStreamFabrik();
	} 
	
	public static void initializeStreamFabrik()  {
		
		if ( warningPrintWriter == null) {
			
			warningPrintWriter = new PrintWriter(SwingBremo.warningBuffer,true);
			
		}
	}
	
	
    
}
