package berechnungsModule.turbulence;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import berechnungsModule.SolverConnector;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

public class Turbulence_k_eps extends TurbulenceModel{

	SolverConnector solCon;
	private Motor_HubKolbenMotor mot;
	private double mittlereKolbengeschwindigkeit;
	private double k_esInit;
	
	public Turbulence_k_eps(CasePara cp) {
		super(cp);
		solCon = new SolverConnector(1);	
		super.CP.SOLVER.connect2Solver(solCon);
		
		if(super.CP.MOTOR.isHubKolbenMotor()){
			mot = ((Motor_HubKolbenMotor)super.CP.MOTOR);
		}else{
			try{
				throw new ParameterFileWrongInputException("The chosen turbulence model is only vaild for " +
						"engines with reciprocating pistons! ");
			}catch(ParameterFileWrongInputException pfwe){
				pfwe.stopBremo();
			}
		}
		mittlereKolbengeschwindigkeit = 2*mot.get_Hub()*cp.get_DrehzahlInUproSec();
	}
	
	public void initialize(Zone zn[], double time){					//initialConditions
		
		if (time == CP.get_Einlassschluss()){

		double d_EV = CP.get_EV_Durchmesser();						//Durchmesser Einlassventil [m]
		double h_EV = CP.get_EV_Hub_max();							//MAX Einlassventilhub
		
		double m = 0;
		for(int i = 0; i<zn.length;i++)m = m+zn[i].get_m();
		double rho_IVC = m/this.mot.get_V(CP.get_Einlassschluss());	//rho_IVC als rho_th -> V_ES/Vmax für Liefergrad
		
//		Hashtable<Spezies, Double> ht=new Hashtable<Spezies, Double>();
//		for(int i=0; i<zn.length;i++) ht.put(zn[i].get_ggZone(), zn[i].get_m()/m);
//		GasGemisch gg=new GasGemisch("ggTurb");
//		gg.set_Gasmischung_massenBruch(ht);
//		double R=gg.get_R();
//		
//		double rho_Intake=CP.get_p_LadeLuft()/R/CP.get_T_LadeLuft();//rho_Intake als rho_th aus Gasgleichung
		
		
		double rho_th = rho_IVC;
		double lambda_L = m/this.mot.get_V_MAX()/rho_th;			//Liefergrad
		
																	//Berechnung der Einlassgeschwindigkeit
		double w_ein = mittlereKolbengeschwindigkeit*Math.pow(mot.get_Bohrung(),2)*lambda_L
					/(4*d_EV*h_EV*Math.sin(45));
		k_esInit = 0.5*Math.pow(w_ein, 2);							//Berechnung der kinetischen Energie bei Einlass schließt
		}
		if (time != CP.get_Einlassschluss()){
			k_esInit = CP.get_turbKineticEnergy_Ini();
		} else if (k_esInit == -5.55){
			try {
				throw new ParameterFileWrongInputException(
						"Es wurde kein Wert für die Turbulenz k angegeben "
						+"bzw nicht bei Einlassschluss losgerechnet!");
			} catch (ParameterFileWrongInputException kfwe) {
				kfwe.stopBremo();
			}
		}
		solCon.set_modelParameter2Integrate(k_esInit, 0);	
	}

	private double calc_dk(Zone zn[], double time){
		double k = get_k(zn,time);
		double l = get_turbulenceIntegralLengthScale(zn,time);		//Charakteristische Länge
		double eps = 2.184;											//Vorsicht: bei Bargende eps = eps_q aus Messdaten
		double eps_q = eps;											//s. Bargende S.105
	
		double dV_V = this.mot.get_dV(time)/this.mot.get_V(time);	// drho/rho = -dV/V

		double d = CP.get_Bohrung();
		double d_m = CP.get_PistonBowlDiameter();					//bei Flachkolben PistonBowlDiameter [m] := 0 in Inputfile eintragen
		double V_m = d_m/4*Math.PI;									//Muldenvolumen
		double a = 1;												//a und b zum gewichten von d_x s.Bargende Bild 7.16
		double b = 1;
		double d_x = a*d+b*d_m/(a+b);								//d_x beliebig zwischen Kolbendurchmeser und Muldendurchmesser wählbar
																	//siehe Bargende S.99 (Diss.)
		double w_r = dV_V*V_m/(this.mot.get_V(time)-V_m)*			//radiale und axiale Geschw.komponenten
				(Math.pow(d, 2)-Math.pow(d_x, 2))/4*d_x;
		double w_a = dV_V*CP.get_PistonBowlDepth();					//bei Flachkolben PistonBowlDepth	 [m] := 0 in Inputfile eintragen
		
		double w_q = 1/3*(w_r*(1+d_m/d)+w_a*(Math.pow(d_m/d, 2)));	
		
		double k_q = 0.5*Math.pow(w_q,2);							//0 für Flachkolben
		
		double dk = 0;												//Berechnung von k
					
		dk = dk-2d/3d*k*dV_V;										//dk aufgrund der Dichteänderung
		
		dk = dk-eps*Math.pow(k, 1.5)/l;								//dk aufgrund von Dissipation
								
		if(CP.convert_SEC2KW(time)>=0){		//ZOT==0????			//dk aufgrund von Quetschströmungen erst ab phi>ZOT
			dk = dk+eps_q*Math.pow(k_q, 1.5)/l;						//s. Bargende Gl. 8.10
		}
		return dk;
	}	
	
	public double get_turbulenceIntegralLengthScale(Zone zn[], double time){
		return Math.cbrt(mot.get_V(time)*6/Math.PI);	
	}
	
	public void update(Zone zn[], double time){
		solCon.set_diff_modelParameter2Integrate(this.calc_dk(zn, time), 0);
		}
	@Override
	public double get_eps(Zone zn[],double time) {
		return 2.184;
	}

	@Override
	//Uebernommen von Eichmeier, unbenutzt bei Bargende
	public double get_intensity(Zone zn[],double time) {	
		return Math.sqrt((2.0/3.0)*this.get_k(zn,time));
	}

	@Override
	public double get_k(Zone zn[],double time) {		
		return solCon.get_modelParameter2Integrate(0);
	}
	
	//Methode wird benötigt, um bei einer DVA mit WWM Bargende die Turbulenz richtig berechnen zu können
	@Override
	public void set_k(double turbulence,int i){
		solCon.set_modelParameter2Integrate(turbulence, 0);
	}
	
}
