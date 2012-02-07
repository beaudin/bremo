package bremoswing;

/**
 * @author steve
 */


public class Timer {
	
	
	int stunde;
	int minute;
	int sekunde; 
	

/**
 * Berechnen die Zeit (MilliSecond) in St:Min:Sec
 * @param 
 *         Zeit  in ms
 */
	Timer(double zeit) {
		
		zeit = zeit/1000;
		stunde = (int) (zeit / (60*60));
		minute = (int) (zeit / 60 - (stunde*60));
		sekunde = (int) (zeit % 60);
		
	}
	
	public String  toString (){
		
		return (stunde +" : "+minute+" : "+sekunde);
	}
}
