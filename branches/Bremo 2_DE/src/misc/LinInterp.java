package misc;

import bremo.parameter.CasePara;
import bremoExceptions.MiscException;

public class LinInterp {
	private CasePara CP;
	private int lastsearchedIndex;
	
public LinInterp(CasePara cp){
	CP=cp;
	lastsearchedIndex=0;
}

public void set_lastsearchedIndex(int newval){
	lastsearchedIndex=newval;
}
/**
 * lineare Interpolation, ob Zeit- oder KW-Basiert wird nicht überprüft
 * @param time (s nach Rechenbeginn)
 * @param zeitAchse (aus der Datei)
 * @param values (1D Array, aus dem der Wert kommen soll)
 * @return linear interpolierter Wert aus values
 */
public double linInterPol(double time, double [] zeitAchse, double [] values){
	if(time<zeitAchse[0]) //ORIGINAL
		try{
			throw new MiscException("Bei der Interpolation wurde versucht einen Wert zu einem Zeitpunkt abzufragen, " +
					"der vor dem Beginn des angegebenen Bereichs liegt");
		}catch(MiscException e){
			e.log_Warning();				
		}
		
	    if(time>zeitAchse[zeitAchse.length-1]) //ORIGINAL
		//if(time-zeitAchse[zeitAchse.length-1]>1e-12)
			try{
				throw new MiscException("Bei der Interpolation wurde versucht einen Wert zu einem Zeitpunkt abzufragen, " +
						"der nach dem Beginn des angegebenen Bereichs liegt");
			}catch(MiscException e){
				e.log_Warning();				
		}		
		
	int index=searchTimeIndexFrom(zeitAchse,lastsearchedIndex,time);
	int index1=index+1;
	if(index == 0 && zeitAchse.length == 1){
		return values[index];
	}else{
		double m=(values[index1]-values[index])/(get_time(zeitAchse,index1)-get_time(zeitAchse,index));		
		return m*(time-get_time(zeitAchse,index))+values[index];
	}

}

public double linInterPol(double time, Double [] zeitAchse, Double [] values){
	if(time<zeitAchse[0])
		try{
			throw new MiscException("Bei der Interpolation wurde versucht einen Wert zu einem Zeitpunkt abzufragen, " +
					"der vor dem Beginn des angegebenen Bereichs liegt");
		}catch(MiscException e){
			e.log_Warning();				
		}
		
		if(time>zeitAchse[zeitAchse.length-1])
			try{
				throw new MiscException("Bei der Interpolation wurde versucht einen Wert zu einem Zeitpunkt abzufragen, " +
						"der nach dem Beginn des angegebenen Bereichs liegt");
			}catch(MiscException e){
				e.log_Warning();				
			}		

	int index=searchTimeIndexFrom(zeitAchse,lastsearchedIndex,time);
	int index1=index+1;
	if(index == 0 && zeitAchse.length == 1){
		return values[index];
	}else{
		double m=(values[index1]-values[index])/(get_time(zeitAchse,index1)-get_time(zeitAchse,index));	
		double a=m*(time-get_time(zeitAchse,index))+values[index];
		return a;
	}
}




/**
 * Sucht ab dem angegebenen Startindex nach dem letzten Wert der in der Zeitachse 
 * einen Wert liefert der KLEINER ist als die angegebene Zeit 
 * @param zeitAchse Zeitachse aus der Indizierdatei
 * @param startIndex index an dem gesucht wird
 * @param time Zeit in [ms nach Rechenbeginn] die gesucht wird
 * @return letzter index der einer Zeit  in der Zeitachse entspricht die kleiner ist als time
 */
private int searchTimeIndexFrom_IndizierDaten(double[] zeitAchse, int startIndex, double time) {
	int index = -1;	
	boolean failed=false;
	
	if(time<get_time_convert(zeitAchse,0))
		failed=true;
	
		
	if(startIndex<=zeitAchse.length-2 && startIndex>=0){					
		
		if(time<get_time_convert(zeitAchse,startIndex))
			startIndex=0;

		index=startIndex;	

		while( !failed  && time>get_time_convert(zeitAchse,index+1)){
			index++;
			if(index==zeitAchse.length-1)//letzter Wert der innerhalb der Laenge der zeitachse abgefragt werden kann
				failed=true;					
		}
	}else if(zeitAchse.length==1 && startIndex==0){
		index=0;
	}else{
		try{
			throw new MiscException("Bei der Interpolation ist ein Fehler aufgetreten.\n" +
					"Der Startindex liegt ausserhalb des gueltigen Bereichs (0<=index<Leange der Zeitachse). \n" +
					"Startindex: " +startIndex +"\n"+
					"Laenge der Zeitachse: " +(zeitAchse.length-1));
		}catch(MiscException e){
			e.stopBremo();							
		}
	}
	
	if(failed)
		try{
			throw new MiscException("Bei der Interpolation ist ein Fehler aufgetreten.\n" +
					"Die gesuchte Zeit " +time +" [s nach Rechenbeginn] bzw. " +CP.convert_SEC2KW(time) + 
					" [KW] liegt ausserhalb der Zeitachse der angegebenen Daten ");
		}catch(MiscException e){
			e.stopBremo();							
		}		
	
	lastsearchedIndex=index;

	return index;
}

/**
 * Sucht ab dem angegebenen Startindex nach dem letzten Wert der in der Zeitachse 
 * einen Wert liefert der KLEINER ist als die angegebene Zeit
 * @param zeitAchse zu den Daten passende Zeitachse 
 * @param startIndex index an dem gesucht wird
 * @param time Zeit in [ms nach Rechenbeginn] die gesucht wird
 * @return letzter index der einer Zeit in der Zeitachse entspricht die kleiner ist als time
 */
private int searchTimeIndexFrom(Double[] zeitAchse, int startIndex,double time) {
	int index = -1;	
	boolean failed=false;
	
	if(time<get_time(zeitAchse,0))
		failed=true; 
	
		
	if(startIndex<=zeitAchse.length-2 && startIndex>=0){					
		
		if(time<get_time(zeitAchse,startIndex))
			startIndex=0;

		index=startIndex;	

		while( !failed  && time>get_time(zeitAchse,index+1)){
			index++;
			if(index==zeitAchse.length-1)//letzter Wert der innerhalb der Laenge der zeitachse abgefragt werden kann
				failed=true;					
		}
	}else if(zeitAchse.length==1 && startIndex==0){
		index=0;
	}else{
		try{
			throw new MiscException("Bei der Interpolation ist ein Fehler aufgetreten.\n" +
					"Der Startindex liegt ausserhalb des gueltigen Bereichs (0<=index<Leange der Zeitachse). \n" +
					"Startindex: " +startIndex +"\n"+
					"Laenge der Zeitachse: " +(zeitAchse.length-1));
		}catch(MiscException e){
			e.stopBremo();							
		}
	}
	
	
	
	if(failed)
		try{
			throw new MiscException("Bei der Interpolation ist ein Fehler aufgetreten.\n" +
					"Die gesuchte Zeit " +time +" [s nach Rechenbeginn] bzw. " +CP.convert_SEC2KW(time) + 
					" [KW] liegt ausserhalb der Zeitachse der angegebenen Daten ");
		}catch(MiscException e){
			e.stopBremo();							
		}		
	
	lastsearchedIndex=index;

	return index;
}

/**
 * Sucht ab dem angegebenen Startindex nach dem letzten Wert der in der Zeitachse 
 * einen Wert liefert der KLEINER ist als die angegebene Zeit
 * @param zeitAchse zu den Daten passende Zeitachse  
 * @param startIndex index an dem gesucht wird
 * @param time Zeit in [ms nach Rechenbeginn] die gesucht wird
 * @return letzter index der einer Zeit in der Zeitachse entspricht die kleiner ist als time
 */
private int searchTimeIndexFrom(double [] zeitAchse, int startIndex, double time){
	int index = -1;	
	boolean failed=false;
	
	if(time<get_time(zeitAchse,0))
		failed=true;
	
		
	if(startIndex<=zeitAchse.length-2 && startIndex>=0){					
		
		if(time<get_time(zeitAchse,startIndex))
			startIndex=0;

		index=startIndex;	

		while( !failed  && time>get_time(zeitAchse,index+1)){
			index++;
			if(index==zeitAchse.length-1)//letzter Wert der innerhalb der Laenge der zeitachse abgefragt werden kann
				failed=true;					
		}
	}else if(zeitAchse.length==1 && startIndex==0){
		index=0;
	}else{
		try{
			throw new MiscException("Bei der Interpolation ist ein Fehler aufgetreten.\n" +
					"Der Startindex liegt ausserhalb des gueltigen Bereichs (0<=index<Leange der Zeitachse). \n" +
					"Startindex: " +startIndex +"\n"+
					"Laenge der Zeitachse: " +(zeitAchse.length-1));
		}catch(MiscException e){
			e.stopBremo();							
		}
	}	
	
	if(failed)
		try{
			throw new MiscException("Bei der Interpolation ist ein Fehler aufgetreten.\n" +
					"Die gesuchte Abszisse: " +time + " liegt ausserhalb der Abszisse der angegebenen Daten ");
		}catch(MiscException e){
			e.stopBremo();							
		}		
	
	lastsearchedIndex=index;

	return index;
}

private double get_time_convert(double [] zeitAchse, int index){
	if(CP.SYS.IS_KW_BASIERT){
		return CP.convert_KW2SEC(zeitAchse[index]);
	}else {
		return zeitAchse[index];
	}
}

private double get_time_convert(Double [] zeitAchse, int index){
	if(CP.SYS.IS_KW_BASIERT){
		return CP.convert_KW2SEC(zeitAchse[index]);
	}else {
		return zeitAchse[index];
	}
}

private double get_time(double[] zeitAchse, int index){
	return zeitAchse[index];
}

private double get_time(Double[] zeitAchse, int index){
	return zeitAchse[index];
}





}
