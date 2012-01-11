package berechnungsModule.wandwaerme;




import java.util.Hashtable;

import misc.PhysKonst;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.ModulPrototyp;
import berechnungsModule.Berechnung.Zone;
import misc.VektorBuffer;
import berechnungsModule.motor.Motor;
import bremo.main.Bremo;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

public abstract class WandWaermeUebergang extends ModulPrototyp{
//Abstrakte Klasse für alle Wandwärmemodelle
	protected final double T_WAND;
	protected Motor motor;
	protected CasePara cp;	
	private static WandWaermeUebergang wwModell=null;
	private static WandWaermeUebergang wwModell_LW=null;
	public static final String WANDWAERME_FLAG="Wandwaermemodell";
	private static final String WANDWAERME_FLAG_LW="Wandwaermemodell_LW";
	public  static final String[] WANDWAERMEMODELLE
					={"WoschniHuber", "Woschni", "Hohenberg", "Hans", "Bargende", "Chang","ohne"};	
	public  static final String[] WANDWAERMEMODELLE_LW
		={"WoschniHuber", "Woschni", "Hohenberg", "Hans", "Bargende", "Chang", "ohne"};	
	
	protected WandWaermeUebergang(CasePara cp){				
		motor=Motor.get_Instance(cp);
		this.cp=cp;
		T_WAND=cp.get_T_Wand();
	}

	/**
	 * Liefert ein Waermeuebergangsobjekt entsprechend der Auswahl im InputFile zurueck. 
	 * Die variable cp vom Typ CasePara wird benötigt um aus dem Eingabefile das gewuenschte 
	 * Waermeuebergangsmodell auszulesen.
	 * @param CaseParameter cp
	 * @param Motor motor
	 * @return Wandwaermemodell_Superclass
	 */
	public static WandWaermeUebergang get_Instance(CasePara cp){			
		Motor motor=Motor.get_Instance(cp);
		if(wwModell==null){
			String wandwaermemodellVorgabe=get_ModulWahl(WANDWAERME_FLAG, WANDWAERMEMODELLE, cp);
			try{
				if(wandwaermemodellVorgabe.equals("WoschniHuber")){
					if(motor.isHubKolbenMotor()){
						wwModell=new WoschniHuber(cp);
					}else{					
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Woschni")){

					if(motor.isHubKolbenMotor()){
						wwModell=new Woschni(cp);						
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Hohenberg")){

					if(motor.isHubKolbenMotor()){
						wwModell=new Hohenberg(cp);						
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Chang")){
					wwModell=null;
					if(motor.isHubKolbenMotor()){
						wwModell=new Chang(cp);
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Hans")){
					wwModell=null;
					if(motor.isHubKolbenMotor()){
						wwModell=new Hensel(cp);
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Bargende")){
					wwModell=null;
					if(motor.isHubKolbenMotor()){
						// TODO Konstruktor des Wandwaermemodells einfuegen --> is das Modell schon implementiert
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("ohne")){
					wwModell=new PerfektIsoliert(cp);				
				}				
			}
			catch(ParameterFileWrongInputException e){
				e.log_Warning();
				wwModell=new PerfektIsoliert(cp);	
			}

			if(wwModell==null){
				try {
					throw new BirdBrainedProgrammerException(
							"Das ausgewaehlte Waermeuebergangsmodell \"" + wandwaermemodellVorgabe + "\" wurde im InputFile " +
							"zwar als valide akzeptiert, \nist im Programm aber nicht richtig eingepflegt worden. \n" +
							"Es fehlt der entsprechende else-if-Block in der WandwaermeBasisKlasse" +
							" oder es wurde noch nicht implementiert \n" +
					"Die Rechnung erfolgt ohne Wandwaermeuebergangsmodell");
				} catch (BirdBrainedProgrammerException e) {
					wwModell=new PerfektIsoliert(cp);	
					e.log_Warning();
				}
			}
		}

			return wwModell;	
	}
	
	/**
	 * Liefert ein Waermeuebergangsobjekt für die Ladungswechselanalyse entsprechend der
	 * Auswahl im InputFile zurueck. Die variable cp vom Typ CasePara wird benötigt um
	 * aus dem Eingabefile das gewuenschte Waermeuebergangsmodell auszulesen.
	 * @param CaseParameter cp
	 * @return Wandwaermemodell_Superclass
	 */
	public static WandWaermeUebergang get_Instance_LW(CasePara cp){			
		Motor motor=Motor.get_Instance(cp);
		if(wwModell_LW==null){
			String wandwaermemodellVorgabe=get_ModulWahl(WANDWAERME_FLAG_LW, WANDWAERMEMODELLE_LW, cp);
			try{
				if(wandwaermemodellVorgabe.equals("WoschniHuber")){
					if(motor.isHubKolbenMotor()){
						wwModell_LW=new WoschniHuber(cp);
					}else{					
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Woschni")){

					if(motor.isHubKolbenMotor()){
						wwModell_LW=new Woschni(cp);						
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Hohenberg")){

					if(motor.isHubKolbenMotor()){
						wwModell_LW=new Hohenberg(cp);						
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Chang")){
					wwModell_LW=null;
					if(motor.isHubKolbenMotor()){
						wwModell_LW=new Chang(cp);
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Hans")){
					wwModell_LW=null;
					if(motor.isHubKolbenMotor()){
						wwModell_LW=new Hensel(cp);
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("Bargende")){
					wwModell_LW=null;
					if(motor.isHubKolbenMotor()){
						// TODO Konstruktor des Wandwaermemodells einfuegen --> is das Modell schon implementiert
					}else{
						throw new ParameterFileWrongInputException("Das ausgewaehlte Waermeuebergangsmodell \"" +
								wandwaermemodellVorgabe+ "\" ist nur fuer Hubkolbenmotoren geeignet.\n" +
						"Die Rechnung erfolgt ohne Waermeuebergangsmodell");
					}
				}
				else if(wandwaermemodellVorgabe.equals("ohne")){
					wwModell_LW=new PerfektIsoliert(cp);				
				}				
			}
			catch(ParameterFileWrongInputException e){
				e.log_Warning();
				wwModell_LW=new PerfektIsoliert(cp);	
			}

			if(wwModell_LW==null){
				try {
					throw new BirdBrainedProgrammerException(
							"Das ausgewaehlte Waermeuebergangsmodell \"" + wandwaermemodellVorgabe + "\" wurde im InputFile " +
							"zwar als valide akzeptiert, \nist im Programm aber nicht richtig eingepflegt worden. \n" +
							"Es fehlt der entsprechende else-if-Block in der WandwaermeBasisKlasse" +
							" oder es wurde noch nicht implementiert \n" +
					"Die Ladungswechselanalyse erfolgt ohne Wandwaermeuebergangsmodell");
				} catch (BirdBrainedProgrammerException e) {
					wwModell_LW=new PerfektIsoliert(cp);	
					e.log_Warning();
				}
			}
		}

			return wwModell_LW;	
	}
	
	/**
	 * Ruft die gleichnamige Methode mit dem zusaetzlichen Eingabeparameter cp vom Typ CasePara auf und 
	 * liefert ein Waermeuebergangsobjekt entsprechend der Auswahl im InputFile zurueck.
	 * Die Variable cp vom Typ CasePara wird benötigt um aus dem Eingabefile den gewuenschten 
	 * GleichGewichtsRechner auszulesen.
	 * Die variable cp vom Typ CasePara wird mittels einer statischen Methode aus Bremo.java ausgecheckt.
	 * @param Motor motor
	 * @return Wandwaermemodell_Superclass
	 */
	public static WandWaermeUebergang get_Instance(){		
		return get_Instance(Bremo.get_casePara());
	}
	
	/**
	 * Berechnet die mittlere Brennraumtemperatur anhand der bestehenden Zonen
	 * @param Zone[] zonenIN
	 * @return double Tmb
	 */
	public double get_Tmb(Zone [] zonen_IN){
		double nenner=0;
		double zaehler=0;
		double T_i=0;
		double temp=0;
		for (int i=0; i<zonen_IN.length; i++){
			if(zonen_IN[i].get_m()>=cp.SYS.MINIMALE_ZONENMASSE){
				T_i=zonen_IN[i].get_T();
				temp=zonen_IN[i].get_m()*zonen_IN[i].get_ggZone().get_cv_mass(T_i);
				nenner+=temp;
				zaehler+=temp*T_i;
			}
		}
		
		return zaehler/nenner;
	}
	
	protected double get_R_brennraum(double time, Zone [] zonen_IN){
		double R_mix=0; //Gesamtgaskonstante der Zonen
		double R_i=0;	//Gaskonstante der Zonen
		double x_i=0;	//Massenbruch der Zonen
		double m_Ges=0;	//Gesamtmasse im Zylinder
		
		//TODO: Was ist, wenn Masse noch in der Flüssigphase ist???
		for (int i=0; i<zonen_IN.length; i++){
			m_Ges+=zonen_IN[i].get_m();
		}
		
		for (int i=0; i<zonen_IN.length; i++){
			R_i=zonen_IN[i].get_ggZone().get_R();
			x_i=zonen_IN[i].get_m()/m_Ges;
			R_mix += R_i*x_i;
		}
		
		return R_mix;
	}
	
	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K)
	public abstract double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt);
	
	
	
	//...und hiermit den Wandwärmestrom in W
	public double get_WandWaermeStrom(double time, Zone[] zonen_IN, double fortschritt, VektorBuffer tBuffer){
		double Brennraumflaeche = motor.get_BrennraumFlaeche(time);
		double WWSD=get_WandWaermeStromDichte(time,zonen_IN, fortschritt, tBuffer)*Brennraumflaeche;
		return WWSD;
	}	
	
	
	//...und hiermit die Wärmestromdichte in W/m^2
	public double get_WandWaermeStromDichte(double time, Zone[] zonen_IN, double fortschritt, VektorBuffer tBuffer){
		// Waermestromdichte in W/m^2 (W/m^2 x K)
		double T=get_Tmb(zonen_IN);
		return get_WaermeUebergangsKoeffizient(time,zonen_IN, fortschritt) * (T- T_WAND);
	}

	
	
}
