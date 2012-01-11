package berechnungsModule.motor;

import berechnungsModule.ModulPrototyp;
import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public abstract class Motor extends ModulPrototyp {
	
	final CasePara CP;
	//Dieser Flag wird z.B. von der Klasse CasePara aufgerufen  
	public static final String MOTOR_FLAG="Motor";
	public  final static String[] MOTORENTYPEN={"HubKolbenMotor" , "ausDatei"};	
	private static Motor motor=null;
	
	protected Motor(CasePara cp){
		CP=cp;		
	}

	public abstract double get_V(double time); 
	public abstract double get_dV(double time);
	public abstract double get_BrennraumFlaeche(double time);
	public abstract double get_Verdichtung();
	public abstract double get_V_MAX();
	
	//Die Wärmeübergangsmodelle gelten nur für HubKolbenMotoren 
	//diese Abfrage wird zur Fehlerabfrage verwendet
	public abstract boolean isHubKolbenMotor();
	
	protected double convertTime_TO_KW(double time){
		return CP.convert_SEC2KW(time);	
	}
	
	//wandelt die Ableitung nach dem Kurbelwinkel in die nach der Zeit um
	//dXdTime=dXdKW*dKWdTime
	protected double convert_dKW_TO_dTime(double dKW){
		// TODO mittels diffquotienten checken ob das ergebnis stimmt
		return dKW*(CP.get_DrehzahlInUproSec()*360);	
	}
	
	//wandelt die Ableitung nach dem Kurbelwinkel in die nach der Zeit um
	//dX2dTime2=dXdKW*dKWdTime
	protected double convert_dKW2_TO_dTime2(double dKW){
		// TODO mittels diffquotienten checken ob das ergebnis stimmt
		return dKW*(CP.get_DrehzahlInUproSec()*360)*(CP.get_DrehzahlInUproSec()*360);	
	}
	
	
	/**
	 *  Liefert ein Objekt vom Typ Motor zurück. Die Art des Motors entspricht der Auswahl im InputFile.
	 * @param CasePara cp
	 * @return Motor motor
	 */
	
	public static Motor get_Instance(CasePara cp){

		if(motor==null){
			String motorVorgabe=get_ModulWahl(MOTOR_FLAG, MOTORENTYPEN, cp);
	
			if(motorVorgabe.equals("HubKolbenMotor"))
				motor=new Motor_HubKolbenMotor(cp) ;
	
			else if(motorVorgabe.equals("ausDatei"))
				motor=null;
			else if(motorVorgabe.equals("DruckKammer"))
				motor=null;
	
			if(motor==null){
				try {
					throw new BirdBrainedProgrammerException(
							"Der ausgewaehlte Motroentyp \"" +motorVorgabe + " \" wurde im InputFile " +
							"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n "  + 
					"Es fehlt der entsprechende else-if-Block oder der Motortyp wurde noch nicht implementiert");
				} catch (BirdBrainedProgrammerException e) {
					e.stopBremo();
				}
			}
		}
		return motor;		
	}
	
	/**
	 * Ruft die gleichnamige Methode mit dem Eingabeparameter cp vom Typ CasePara auf und  
	 * liefert ein Objekt vom Typ Motor zurück. Die Art des Motors entspricht der Auswahl im InputFile.
	 * Die Variable cp vom Typ CasePara wird über eine statische Methode in 
	 * Bremo.java ausgecheckt.
	 * @return Motor
	 */
	public static Motor get_Instance(){
		return get_Instance(Bremo.get_casePara());
	}
	
	

}
