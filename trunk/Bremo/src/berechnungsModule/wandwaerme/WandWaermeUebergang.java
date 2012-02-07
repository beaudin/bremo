package berechnungsModule.wandwaerme;




import berechnungsModule.Berechnung.Zone;
import misc.VektorBuffer;
import berechnungsModule.motor.Motor;
import bremo.parameter.*;


public abstract class WandWaermeUebergang {
//Abstrakte Klasse für alle Wandwärmemodelle
	protected final double T_WAND;
	protected Motor motor;
	protected CasePara cp;	
	
	
	protected WandWaermeUebergang(CasePara cp){				
		motor=cp.MOTOR;
		this.cp=cp;
		T_WAND=cp.get_T_Wand();
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
