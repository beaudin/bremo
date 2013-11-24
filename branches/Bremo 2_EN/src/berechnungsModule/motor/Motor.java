package berechnungsModule.motor;

import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public abstract class Motor  {
	
	final CasePara CP;
	
	protected Motor(CasePara cp){
		CP=cp;		
	}

	public abstract double get_V(double time); 
	public abstract double get_dV(double time);
	/**
	 * Liefert die Brennraumoberflaeche in [m^2].
	 * Bei HubkolbenMotoren wird die Feuerstegflaeche nicht berücksichtigt!
	 * @param time [s. n. Rechenbeginn]
	 * @return
	 */
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
		return dKW*(CP.get_rotSpeedInRotperSec()*360);	
	}
	
	//wandelt die Ableitung nach dem Kurbelwinkel in die nach der Zeit um
	//dX2dTime2=dXdKW*dKWdTime
	protected double convert_dKW2_TO_dTime2(double dKW){
		// TODO mittels diffquotienten checken ob das ergebnis stimmt
		return dKW*(CP.get_rotSpeedInRotperSec()*360)*(CP.get_rotSpeedInRotperSec()*360);	
	}	
}
