package berechnungsModule;

import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class ModulFabrik {
	
	
	protected final CasePara CP;
	
	public ModulFabrik(CasePara cp){
		CP=cp;		
	}
	
	
	
	protected  String get_ModulWahl(String modulFlag, String [] moeglicheModelle){			

		if(!CP.MODUL_VORGABEN.containsKey(modulFlag)){			
			try {
				String message="Im Inputfile wurde kein gueltiges \""+ modulFlag +"\"-Modul angegeben \n" +
				"Die Definition erfolgt z.B. so: \n ["+ modulFlag + ": ModulName]";
				throw new ParameterFileWrongInputException(message);
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
		}			

		String modul=null;
		boolean found=false;

		modul= CP.MODUL_VORGABEN.get(modulFlag);
		int u=0;

		while(u<moeglicheModelle.length && found==false){
			if(modul.equals(moeglicheModelle[u]))
				found=true;
			u++;			
		}	

		if(!found){
			try {
				String message="Fuer das \""+modulFlag+"\"-Modul wurde folgender Wert im InputFile angegeben: \"" +
				modul+"\". Dies ist keine gueltige Auswahl! \n" +
				"Moegliche Module sind: \n";

				for( int i=0;i<moeglicheModelle.length;i++){
					message= message+moeglicheModelle[i] + " \n" ;						
				}			
				throw new ParameterFileWrongInputException(message);
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
		}
		return modul;
	}
	

}
