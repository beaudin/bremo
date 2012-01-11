package berechnungsModule;



import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;
/**
 * Ueberklasse von der alle Berechnungsmodule abgeleitet sind. Diese Klasse stellt sicher, dass alle
 * BerechnungsModule die selbe Fehlermeldung ausgeben wenn im Inputfile nicht die richtige
 * Auswahl getroffen wurde. 
 * @author eichmeier
 *
 */
public abstract class ModulPrototyp {	
	
	protected ModulPrototyp(){}	

	
	protected static String get_ModulWahl(String modulFlag, String [] moeglicheModelle, CasePara CP){			

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
	
	//DIE KINDERKLASSE MUESSEN AUF JEDEN FALL EINE STATIC METHODE GET_INSTANCE IMPLEMENTIEREN. 
	//DIES GEHT IN DIESER KLASSE LEIDER NICHT
	
}
