package berechnungsModule.wandwaerme;


import matLib.MatLibBase;
import misc.VektorBuffer;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;


public class Rotax83 extends WandWaermeUebergang{

	
	private Motor_HubKolbenMotor motor;
	private double C_1;
	private double C_2;
	private double C_u;
	private double C_b;
	private double Hubvolumen;
	private double Volumen;
	private double mittlereKolbengeschwindigkeit;
	protected  IndizierDaten indiD;
	private double Temperatur_1; //bei Kompressionsbeginn
	private double Druck_1;	//bei Kompressionsbeginn
	private double Volumen_1; //bei Kompressionsbeginn
	private double refPunkt; //RefPunktWoschniHuber
	private double pZyl_a; //bei RefPunktWoschniHuber
	private double Vol_a; //bei RefPunktWoschniHuber
	private double Schleppdruck=1;
	private double n; //polytropic exponent
	private boolean setRefConditions=true;	
	
	protected Rotax83(CasePara cp) {
		//TODO: Irgendwas einbauen, damit es nur bei Zweizonenrechnung läuft, sonste einen Fehler wirft!
		super(cp);
		this.cp=super.cp;
		this.motor=(Motor_HubKolbenMotor) super.motor;
		C_u = 1; //2;
		C_b = 1;// 0.7;
		C_1 = 130;
		C_2 = 1.4;
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_DrehzahlInUproSec();	//[m/s]
		Hubvolumen = motor.get_Hubvolumen();
		super.feuerstegMult=0.3;
	}	

	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time)+0.3*motor.get_FeuerstegFlaeche();
	}
	
	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K)
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt){		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double alpha=C_1*Math.pow( (motor.get_V(time)),-0.06)*Math.pow( (p*1E-5),0.8)*
			Math.pow(T,-0.4)*Math.pow((mittlereKolbengeschwindigkeit +C_2),0.8) ;
		return alpha;
	}
	
	
	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K) für die unverbrannte Zone
	@Override
	public double get_WaermeUebergangsKoeffizientUnverbrannt(double time, Zone[] zonen_IN, double fortschritt){		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		//double T=get_Tmb(zonen_IN);	//Mittlere Brennraumtemperatur
		double T=zonen_IN[0].get_T();	//Temperatur unverbrannte Zone
		double alpha_u=C_u*C_1*Math.pow( (motor.get_V(time)),-0.06)*Math.pow( (p*1E-5),0.8)*
			Math.pow(T,-0.4)*Math.pow((mittlereKolbengeschwindigkeit +C_2),0.8) ;
		return alpha_u;
	}	
	
	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K) für die verbrannte Zone
	@Override
	public double get_WaermeUebergangsKoeffizientVerbrannt(double time, Zone[] zonen_IN, double fortschritt){		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		//double T=get_Tmb(zonen_IN);	//Mittlere Brennraumtemperatur
		double T=zonen_IN[1].get_T();	//Temperatur verbrannte Zone
		double alpha_b=C_b*C_1*Math.pow( (motor.get_V(time)),-0.06)*Math.pow( (p*1E-5),0.8)*
			Math.pow(T,-0.4)*Math.pow((mittlereKolbengeschwindigkeit +C_2),0.8) ;
		return alpha_b;
	}
	
	public double get_WandWaermeStromDichteUnverbrannt(double time, Zone[] zonen_IN, double fortschritt){	
	double qw=0;
	double alpha_u=this.get_WaermeUebergangsKoeffizientUnverbrannt(time, zonen_IN, fortschritt);
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
		
		double T=zonen_IN[0].get_T();	//Temperatur unverbrannte Zone
		//double T=get_Tmb(zonen_IN);
		qw=alpha_u*(pistonSurf*(T-T_piston)+headSurf*(T-T_head)+cylWallSurf*(T-T_cyl))/(pistonSurf+headSurf+cylWallSurf);
		
						
//		if(Double.isNaN(T_head))	{
//			T_head=cp.get_T_Head();	
//		}
//		qw=alpha*(T-T_head);

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

	public double get_WandWaermeStromDichteVerbrannt(double time, Zone[] zonen_IN, double fortschritt){	
	double qw=0;
	double alpha_b=this.get_WaermeUebergangsKoeffizientVerbrannt(time, zonen_IN, fortschritt);
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
		
		double T=zonen_IN[0].get_T();	//Temperatur unverbrannte Zone
		//double T=get_Tmb(zonen_IN);
		qw=alpha_b*(pistonSurf*(T-T_piston)+headSurf*(T-T_head)+cylWallSurf*(T-T_cyl))/(pistonSurf+headSurf+cylWallSurf);
		
//		if(Double.isNaN(T_piston))	{
//			T_piston=cp.get_T_Piston();	
//		}
//		
//		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
//		//double T=zonen_IN[1].get_T();	//Temperatur verbrannte Zone
//		qw=alpha*(T-T_piston);

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
		
	double C_w=0;
	double C_1w=0; //2.28+0.308*0;
	double C_2w=0.001; //0.00324;
	double C_k=0.066; //0.0955; //0.6; //0.01;
	double Kolbengeschwindigkeit= motor.get_Kolbengeschwindigkeit(time);
			
	double qwk=0;
	double alpha_u=this.get_WaermeUebergangsKoeffizientUnverbrannt(time, zonen_IN, fortschritt);
	double alpha_b=this.get_WaermeUebergangsKoeffizientVerbrannt(time, zonen_IN, fortschritt);
	double p=zonen_IN[0].get_p();
	Volumen = motor.get_V(time); //TODO: Sauber programmieren!
	//double p_0=this.get_Schleppdruck(time, zonen_IN, fortschritt);
	double p_0=this.get_Schleppdruck();
	//double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
	double T_u=zonen_IN[0].get_T();
	double T_b=zonen_IN[1].get_T();
	
	//double WoschniTerm=Math.pow(C_1w*mittlereKolbengeschwindigkeit+(p-p_0)*C_2w*Hubvolumen*Temperatur_1/Druck_1/Volumen_1, 0.8);
	double WoschniTerm=Math.pow(C_1w*mittlereKolbengeschwindigkeit+(p-p_0)*C_2w*Hubvolumen*Temperatur_1/Druck_1/Volumen_1, 1);
	double KolbengeschwindigkeitsTerm=1+Math.abs(Kolbengeschwindigkeit);
	
	double Verbrennungsterm=(1+WoschniTerm*C_w)*(1+KolbengeschwindigkeitsTerm*C_k);
	
	if(motor.isHubKolbenMotor()){
		//Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);

		if(Double.isNaN(T_piston))	{
			T_piston=cp.get_T_Piston();	
		}
		qwk=alpha_u*(T_u-T_piston)*(1-fortschritt)+alpha_b*(T_b-T_piston)*fortschritt*Verbrennungsterm;

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
	
	//Wärmestromdichte Zylinderkopf in W/m^2
	public double get_WandWaermeStromDichteHead(double time, Zone[] zonen_IN, double fortschritt){
			
		double C_w=40; //40;
		double C_1w=0; //2.28+0.308*0;
		double C_2w=1E-6; //0.00324
		double C_k=0; //0.6;
		double Kolbengeschwindigkeit= motor.get_Kolbengeschwindigkeit(time);
			
		double qwk=0;
		double alpha_u=this.get_WaermeUebergangsKoeffizientUnverbrannt(time, zonen_IN, fortschritt);
		double alpha_b=this.get_WaermeUebergangsKoeffizientVerbrannt(time, zonen_IN, fortschritt);
		double p=zonen_IN[0].get_p();
		Volumen = motor.get_V(time); //TODO: Sauber programmieren!
		//double p_0=this.get_Schleppdruck(time, zonen_IN, fortschritt);
		double p_0=this.get_Schleppdruck();
		//double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double T_u=zonen_IN[0].get_T();
		double T_b=zonen_IN[1].get_T();
		
		//double WoschniTerm=Math.pow(C_1w*mittlereKolbengeschwindigkeit+C_2w*Hubvolumen*Temperatur_1/Druck_1/Volumen_1*(p-p_0), 0.8);
		double WoschniTerm=Math.pow(C_1w*mittlereKolbengeschwindigkeit+C_2w*Hubvolumen*Temperatur_1/Druck_1/Volumen_1*(p-p_0), 1);
		
		double KolbengeschwindigkeitsTerm=1+Math.abs(Kolbengeschwindigkeit);
		double Verbrennungsterm=(1+WoschniTerm*C_w)*(1+KolbengeschwindigkeitsTerm*C_k);
		
		if(motor.isHubKolbenMotor()){
			//Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);

			if(Double.isNaN(T_head))	{
				T_head=cp.get_T_Head();	
			}
			//qwk=alpha_u*(T_u-T_head)*fortschritt+alpha_b*(T_b-T_head)*(1-fortschritt)*Verbrennungsterm;
			qwk=alpha_u*(T_u-T_head)*Math.pow((1-fortschritt), 3)+alpha_b*(T_b-T_head)*(1-Math.pow((1-fortschritt), 3))*Verbrennungsterm;

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
	
	//Wärmestrom Zylinderkopf
	//Methode aus WandWaermeUebergang überschrieben mit eigener Methode
	@Override
	public double get_WandWaermeStromHead(double time, Zone[] zonen_IN,
		double fortschritt, VektorBuffer tBuffer) {		
		double whth=0;
		if(motor.isHubKolbenMotor()){
			Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
			double headSurf=hkm.get_fireDeckArea();
			double HeatFluxHead=this.get_WandWaermeStromDichtePiston(time, zonen_IN, fortschritt);
				whth=HeatFluxHead*headSurf;
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
		
	public double get_Schleppdruck(){
	//public double get_Schleppdruck(double time, Zone[] zonen_IN, double fortschritt){
		//Schleppdruck=1;
		
		if(setRefConditions){ //Schleife für Referenzbedingungen bei Rechnungsstart
			setRefConditions=false; //Damit Referenzbedingungen nur einmal aufgerufen werden
		
			indiD=new IndizierDaten(cp);	//TODO: umprogrammieren, so dass die Indizierdatei nur ein einziges Mal eingelesen wird
			
			//Polytropenexponent für die Schleppdruckberechnung ermitteln.
			//Dies wird in den 10°KW vorm Referenzpunkt gemacht...
			refPunkt=cp.get_refPunkt_WoschniHuber();

			double[] n_array = new double[10]; 
			pZyl_a = indiD.get_pZyl(refPunkt);
			Vol_a = motor.get_V(refPunkt);
			double pZyl_b = 0;
			double Vol_b = 0;
			int cnt=0;
			
			//Schleifen-Abbruch nicht mit A<B sondern B-A>sehr kleinem Wert nahe Null!?
			for(double kw=cp.convert_SEC2KW(refPunkt)-10; (cp.convert_SEC2KW(refPunkt)-kw) > 1E-6; kw++){
//			for(double kw=cp.convert_SEC2KW(refPunkt)-10; kw < cp.convert_SEC2KW(refPunkt); kw++){ //ORIGINAL
//			for(double kw=refPunkt-cp.convert_KW2SEC(10); kw < refPunkt; kw++){
				pZyl_b=indiD.get_pZyl(cp.convert_KW2SEC(kw));
				Vol_b=motor.get_V(cp.convert_KW2SEC(kw));
				n_array[cnt]=Math.log10(pZyl_a/pZyl_b)/Math.log10(Vol_b/Vol_a);
				cnt++;
			}
			n=MatLibBase.mw_aus_1DArray(n_array); //Polytropenexponent
			///////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////
			
			Druck_1=indiD.get_pZyl(cp.get_Einlassschluss()); //[bar] //Druck
			Volumen_1=motor.get_V(cp.get_Einlassschluss());	//[m^3]	//Volumen
			Temperatur_1=cp.get_T_IVC_WHT(); //[K]	
			
		} //Ende RefConditions				
			
		//polytrope Schleppdruckberechnung bezüglich Einlassschluss für Woschni-Modell
		//Schleppdruck = Druck_1*Math.pow((Volumen_1/motor.get_V(time)),n); //[Pa]
		Schleppdruck = Druck_1*Math.pow((Volumen_1/Volumen),n); //[Pa]
		
		return Schleppdruck;
	}

}
