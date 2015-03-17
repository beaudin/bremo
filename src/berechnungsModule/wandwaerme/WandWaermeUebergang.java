package berechnungsModule.wandwaerme;




import berechnungsModule.Berechnung.Zone;
import matLib.MatLibBase;
import misc.VektorBuffer;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;


public abstract class WandWaermeUebergang {
//Abstrakte Klasse für alle Wandwärmemodelle
	protected double T_cyl=Double.NaN;
	protected double T_piston=Double.NaN;
	protected double T_head=Double.NaN;
	protected double feuerstegMult;
	
	protected Motor motor;
	protected CasePara cp;	
	
	protected WandWaermeUebergang(CasePara cp){				
		motor=cp.MOTOR;
		this.cp=cp;		
	}
	
	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K)
	public abstract double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt);
	
	//Hiermit bekommt der Benutzer den Alpha_unverbrannt-Wert in W/(m^2K)
	public double get_WaermeUebergangsKoeffizientUnverbrannt(double time, Zone[] zonen_IN, double fortschritt){
		return 0;
	}
		
	//Hiermit bekommt der Benutzer den Alpha_verbrannt-Wert in W/(m^2K)
	public double get_WaermeUebergangsKoeffizientVerbrannt(double time, Zone[] zonen_IN, double fortschritt){
	return 0;
	}
	
	/**
	 * Liefert die Flaeche die zur Berechnung des Wandwaermestroms verwendet wird. 
	 * @param time
	 * @return Oberflaeche des Brennraums in [m^2]
	 */
	public abstract double get_BrennraumFlaeche(double time);

	
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

	//Modelle die auch fuer nicht Kolbenmotoren geeignet sind muessen diese Methode ueberschreiben
	public double get_WandWaermeStrom(double time, Zone[] zonen_IN,
			double fortschritt, VektorBuffer tBuffer) {		
		double wht=0;
		double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
		if(motor.isHubKolbenMotor()){
			Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
			double pistonSurf=hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();
			double headSurf=hkm.get_fireDeckArea();
			double cylWallSurf=hkm.get_CylinderLinerArea(time);

			if(Double.isNaN(T_cyl)||Double.isNaN(T_piston)||Double.isNaN(T_head))	{
				T_cyl=cp.get_T_Cyl();	
				T_piston=cp.get_T_Piston();	
				T_head=cp.get_T_Head();	
			}
			double T=get_Tmb(zonen_IN);
			wht=alpha*(pistonSurf*(T-T_piston)+headSurf*(T-T_head)+cylWallSurf*(T-T_cyl));

		}else{
			try{
				throw new BirdBrainedProgrammerException("WHT-Models " +
						"for non Piston engines must override this method!");					
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return wht;
	}
	
	//Modelle die auch fuer nicht Kolbenmotoren geeignet sind muessen diese Methode ueberschreiben
	public double get_WandWaermeStromPiston(double time, Zone[] zonen_IN,
			double fortschritt, VektorBuffer tBuffer) {		
		double whtp=0;
		double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
		if(motor.isHubKolbenMotor()){
			Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
			double pistonSurf=hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();

			if(Double.isNaN(T_piston))	{
				T_piston=cp.get_T_Piston();		
			}
			double T=get_Tmb(zonen_IN);
			whtp=alpha*pistonSurf*(T-T_piston);

		}else{
			try{
				throw new BirdBrainedProgrammerException("WHT-Models " +
						"for non Piston engines must override this method!");					
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return whtp;
	}
	
	//Modelle die auch fuer nicht Kolbenmotoren geeignet sind muessen diese Methode ueberschreiben
	public double get_WandWaermeStromHead(double time, Zone[] zonen_IN,
			double fortschritt, VektorBuffer tBuffer) {		
		double whth=0;
		double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
		if(motor.isHubKolbenMotor()){
			Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
			double headSurf=hkm.get_fireDeckArea();

			if(Double.isNaN(T_head))	{
				T_head=cp.get_T_Head();	
			}
			double T=get_Tmb(zonen_IN);
			whth=alpha*headSurf*(T-T_head);

		}else{
			try{
				throw new BirdBrainedProgrammerException("WHT-Models " +
						"for non Piston engines must override this method!");					
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return whth;
	}
	
	//Modelle die auch fuer nicht Kolbenmotoren geeignet sind muessen diese Methode ueberschreiben
	public double get_WandWaermeStromCyl(double time, Zone[] zonen_IN,
			double fortschritt, VektorBuffer tBuffer) {		
		double whtl=0;
		double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
		if(motor.isHubKolbenMotor()){
			Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
			double cylWallSurf=hkm.get_CylinderLinerArea(time);

			if(Double.isNaN(T_cyl))	{
				T_cyl=cp.get_T_Cyl();	
			}
			double T=get_Tmb(zonen_IN);
			whtl=alpha*cylWallSurf*(T-T_cyl);

		}else{
			try{
				throw new BirdBrainedProgrammerException("WHT-Models " +
						"for non Piston engines must override this method!");					
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return whtl;
	}
	
	//...und hiermit den Wandwärmestrom in W
//	public double get_WandWaermeStrom(double time, Zone[] zonen_IN, double fortschritt, VektorBuffer tBuffer){
//		double Brennraumflaeche = this.get_BrennraumFlaeche(time);		
//		double WWSD=get_WandWaermeStromDichte(time,zonen_IN, fortschritt, tBuffer)*Brennraumflaeche;	
//		return WWSD;
//	}	
	

	//...und hiermit die Wärmestromdichte in W/m^2
//	public double get_WandWaermeStromDichte(double time, Zone[] zonen_IN, double fortschritt, VektorBuffer tBuffer){
//		//wall temperature
//		if(Double.isNaN(T_wall))	
//			T_wall=cp.get_T_Wand();	
//		// Waermestromdichte in W/m^2 (W/m^2 x K)
//		double T=get_Tmb(zonen_IN);
//		//multiply with factor		
//		return get_WaermeUebergangsKoeffizient(time,zonen_IN, fortschritt) * (T- T_wall);
	
	public double get_WandWaermeStromDichte(double time, Zone[] zonen_IN, double fortschritt){	
	double qw=0;
	double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
	if(motor.isHubKolbenMotor()){
		Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
		double pistonSurf=hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();
		double headSurf=hkm.get_fireDeckArea();
		double cylWallSurf=hkm.get_CylinderLinerArea(time);

		if(Double.isNaN(T_cyl)||Double.isNaN(T_piston)||Double.isNaN(T_head))	{
			T_cyl=cp.get_T_Cyl();	
			T_piston=cp.get_T_Piston();	
			T_head=cp.get_T_Head();	
		}
		double T=get_Tmb(zonen_IN);
		qw=alpha*(pistonSurf*(T-T_piston)+headSurf*(T-T_head)+cylWallSurf*(T-T_cyl))/(pistonSurf+headSurf+cylWallSurf);

	}else{
		try{
			throw new BirdBrainedProgrammerException("WHT-Models " +
					"for non Piston engines must override this method!");					
		}catch(BirdBrainedProgrammerException bbpe){
			bbpe.stopBremo();
		}
	}
	return qw; // [W/m^2]	
	}
	
	//Wärmestromdichte Kolben in W/m^2
	public double get_WandWaermeStromDichtePiston(double time, Zone[] zonen_IN, double fortschritt){	
	double qwk=0;
	double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
	if(motor.isHubKolbenMotor()){
		//Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
		//double pistonSurf=hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();

		if(Double.isNaN(T_piston))	{
			T_piston=cp.get_T_Piston();	
		}
		double T=get_Tmb(zonen_IN);
		//qwk=alpha*pistonSurf*(T-T_piston)/pistonSurf;
		qwk=alpha*(T-T_piston);

	}else{
		try{
			throw new BirdBrainedProgrammerException("WHT-Models " +
					"for non Piston engines must override this method!");					
		}catch(BirdBrainedProgrammerException bbpe){
			bbpe.stopBremo();
		}
	}
	return qwk; // [W/m^2]	
	}
	
	//Wärmestromdichte Head in W/m^2
	public double get_WandWaermeStromDichteHead(double time, Zone[] zonen_IN, double fortschritt){	
	double qwh=0;
	double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
	if(motor.isHubKolbenMotor()){
		//Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
		//double headSurf=hkm.get_fireDeckArea();

		if(Double.isNaN(T_head))	{
			T_head=cp.get_T_Head();	
		}
		double T=get_Tmb(zonen_IN);
		//qwh=alpha*headSurf*(T-T_head)/headSurf;
		qwh=alpha*(T-T_head);

	}else{
		try{
			throw new BirdBrainedProgrammerException("WHT-Models " +
					"for non Piston engines must override this method!");					
		}catch(BirdBrainedProgrammerException bbpe){
			bbpe.stopBremo();
		}
	}
	return qwh; // [W/m^2]	
	}
	
	//Wärmestromdichte Liner in W/m^2
	public double get_WandWaermeStromDichteCyl(double time, Zone[] zonen_IN, double fortschritt){	
	double qwc=0;
	double alpha=this.get_WaermeUebergangsKoeffizient(time, zonen_IN, fortschritt);
	if(motor.isHubKolbenMotor()){
		//Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
		//double cylWallSurf=hkm.get_CylinderLinerArea(time);

		if(Double.isNaN(T_cyl))	{
			T_cyl=cp.get_T_Cyl();	
		}
		double T=get_Tmb(zonen_IN);
		//qwc=alpha*cylWallSurf*(T-T_cyl)/cylWallSurf;
		qwc=alpha*(T-T_cyl);

	}else{
		try{
			throw new BirdBrainedProgrammerException("WHT-Models " +
					"for non Piston engines must override this method!");					
		}catch(BirdBrainedProgrammerException bbpe){
			bbpe.stopBremo();
		}
	}
	return qwc; // [W/m^2]	
	}
	
	public double get_WandWaermeStromDichteUnverbrannt(double time, Zone[] zonen_IN, double fortschritt){	
		return 0;
	}
	
	public double get_WandWaermeStromDichteVerbrannt(double time, Zone[] zonen_IN, double fortschritt){	
		return 0;
	}
	
	public double get_WandWaermeStromDichteSpielwiese(double time, Zone[] zonen_IN, double fortschritt){
		return 0;
	}
	
	//public double get_Schleppdruck(double time, Zone[] zonen_IN){
	public double get_Schleppdruck(){
		return 0;
	}
	
	//Für Ausgabe Heinle in DVA-File
	public double get_u_v(double p, double T, double fortschritt, double time, Zone[] zonen_IN) {
		return 0;
	}
	
	//Für Ausgabe Heinle in DVA-File
	public double get_Verbrennungsterm(double p, double T, double fortschritt, double time, Zone[] zonen_IN) {
		return 0;
	}
	
}
