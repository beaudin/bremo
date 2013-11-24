package berechnungsModule;

import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class ModuleFactory {
	
	
	protected final CasePara CP;
	
	public ModuleFactory(CasePara cp){
		CP=cp;		
	}
	
	
	
	public  String get_ModulWahl(String modulFlag, String [] moeglicheModelle){			

		if(!CP.MODUL_VORGABEN.containsKey(modulFlag)){			
			try {
				String message="In the inputFile no valid \""+ modulFlag +"\"-Module has been specified! \n" +
				"Please define a valid module this way: \n["+ modulFlag + ": ModuleName]";
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
				String message="For the  \""+modulFlag+"\"-Module you specified: \"" +
				modul+"\". This is not a valid choice! \n" +
				"Your options are: \n";

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
