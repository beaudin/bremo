package berechnungsModule.wandwaerme;

import berechnungsModule.ModulFabrik;
import berechnungsModule.motor.Motor;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

public class WandWaermeUebergangFabrik extends ModulFabrik {
	
	public static final String WANDWAERME_FLAG="Wandwaermemodell";
	private static final String WANDWAERME_FLAG_LW="Wandwaermemodell_LW";
	public  static final String[] WANDWAERMEMODELLE
	={"WoschniHuber", "Woschni", "Hohenberg", "Hans", "Bargende", "Chang","ohne"};
	public final  WandWaermeUebergang WAND_WAERME_MODUL;
	public final  WandWaermeUebergang WAND_WAERME_MODUL_LW;
	
	public WandWaermeUebergangFabrik(CasePara cp){
		
		super(cp);
		
		WAND_WAERME_MODUL=this.initializeWandWaermeModul(WANDWAERME_FLAG, WANDWAERMEMODELLE);
		WAND_WAERME_MODUL_LW
			=this.initializeWandWaermeModul(WANDWAERME_FLAG_LW, WANDWAERMEMODELLE);
		
	}
	
	
	
	private WandWaermeUebergang initializeWandWaermeModul(String flag,String [] modelle){
		
		String wandwaermemodellVorgabe=get_ModulWahl(flag,modelle);
		WandWaermeUebergang temp = null;
		Motor motor=CP.MOTOR;
		try{
			if(wandwaermemodellVorgabe.equals("WoschniHuber")){
				if(motor.isHubKolbenMotor()){
					temp=new WoschniHuber(CP);
				}else{					
					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
				}
			}
			else if(wandwaermemodellVorgabe.equals("Woschni")){
				if(motor.isHubKolbenMotor()){
					temp=new Woschni(CP);						
				}else{
					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
				}
			}
			else if(wandwaermemodellVorgabe.equals("Hohenberg")){
				if(motor.isHubKolbenMotor()){
					temp=new Hohenberg(CP);						
				}else{
					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
				}
			}
			else if(wandwaermemodellVorgabe.equals("Chang")){
				if(motor.isHubKolbenMotor()){
					temp=new Chang(CP);
				}else{
					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
				}
			}
			else if(wandwaermemodellVorgabe.equals("Hans")){
				if(motor.isHubKolbenMotor()){
					temp=new Hensel(CP);
				}else{
					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
				}
			}
			else if(wandwaermemodellVorgabe.equals("Bargende")){	
				if(motor.isHubKolbenMotor()){

					temp=null;// TODO Konstruktor des Wandwaermemodells einfuegen --> is das Modell schon implementiert
				}else{
					throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
							wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
					"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
				}
			}
			else if(wandwaermemodellVorgabe.equals("ohne")){
				temp=new PerfektIsoliert(CP);				
			}				
		}
		catch(ParameterFileWrongInputException e){
			e.log_Warning();
			temp=new PerfektIsoliert(CP);	
		}

		if(temp==null){			
			try {
				throw new BirdBrainedProgrammerException(
						"Das ausgewaehlte Waermeuebergangsmodell \"" + wandwaermemodellVorgabe + "\" wurde im InputFile " +
						"zwar als valide akzeptiert, \nist im Programm aber nicht richtig eingepflegt worden. \n" +
						"Es fehlt der entsprechende else-if-Block in der WandwaermeBasisKlasse" +
						" oder es wurde noch nicht implementiert \n" +
				"Die Rechnung erfolgt ohne Wandwaermeuebergangsmodell");
			} catch (BirdBrainedProgrammerException e) {				
				e.log_Warning();
			}
			temp=new PerfektIsoliert(CP);			
		}
		return temp;		
	}

}
