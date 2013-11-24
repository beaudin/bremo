package berechnungsModule.wallHeatTransfer;

import java.util.Hashtable;

import kalorik.GasMixture;
import kalorik.Spezies;

import matLib.MatLibBase;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_reciprocatingPiston;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;


public class Woschni extends WallHeatTransfer {

	private Motor_reciprocatingPiston motor;
	private CasePara cp;
	protected  IndicatorData indiD;
	private double vDrall;
	private double Bohrungsdurchmesser;
	private double Hubvolumen;
	private double mittlereKolbengeschwindigkeit;
	private double temperatur_1;
	private double druck_1;
	private double volumen_1;
	private double n; //polytropic exponent
	private boolean setRefConditions=true;

	protected Woschni(CasePara mp) {
		super(mp);
		this.cp=super.cp;		
		this.motor=(Motor_reciprocatingPiston) super.motor;
		Bohrungsdurchmesser = motor.get_bore(); //[m]
		Hubvolumen = motor.get_Hubvolumen();  //[m^3]
		//Kompressionsvolumen = motor.get_Kompressionsvolumen();	//[m^3]
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_rotSpeedInRotperSec(); //[m/s]
		
		//Formel nach Pischinger "Thermodynamik der Verbrennungskraftmaschine" Seite 203
		vDrall=cp.get_swirlRatio()*cp.get_rotSpeedInRotperSec()*Math.PI*motor.get_bore()*0.7;
		super.feuerstegMult=0;

	}	

	
	
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {
		
		if(setRefConditions){
			setRefConditions=false;
			if(time!=cp.get_IVC()){//simulation start is not at IVC (e.g homogeneous multizone model
				temperatur_1=cp.get_T_IVC_WHT(); //[K]	
				n=cp.get_polyExp_WHT();
				if(cp.compareToExp()){
					indiD=new IndicatorData(cp);
					druck_1=indiD.get_pZyl(cp.get_IVC()); 
				}
				else{				
					druck_1=cp.get_p_IVC_WHT();
				}				
				volumen_1=motor.get_V(cp.get_IVC());	//[m^3]				
			}else{
				
				temperatur_1=super.get_Tmb(zonen_IN);
				//getting gamma for the whole cylinder
				double mTot=0;
				for(int i=0; i<zonen_IN.length;i++)mTot+=zonen_IN[i].get_m();
				Hashtable <Spezies,Double> ht=new Hashtable <Spezies,Double>();
				for(int i=0; i<zonen_IN.length;i++)
					ht.put(zonen_IN[i].get_gasMixtureZone(), zonen_IN[i].get_m()/mTot);
				
				GasMixture gg =new GasMixture("gg");
				gg.set_Gasmischung_massenBruch(ht);
				this.n=gg.get_kappa(temperatur_1);				
				druck_1=zonen_IN[0].get_p();
				volumen_1=motor.get_V(time);
			}
			
		}
		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		// Bestimmung an Hand des Kurbelwinkels und der Steuerzeiten ob Ladungswechsel oder Hochdruckteil
		// Eine Zwischenkompression wird dem Hochdruckteil zugerechnet (Hönl)
		double C_1 = 0;
		if (	// Hochdruckteil	
				((motor.get_Einlass_schliesst() <= time) && (motor.get_Auslass_oeffnet() >= time))
//				|| 
//				// Zwischenkompression
//				(motor.get_Einlass_oeffnet() >= time && motor.get_Auslass_schliesst()<= time) 	
			)
		{	// Hochdruckberechnung
			C_1 = 2.28 + 0.308 * vDrall / mittlereKolbengeschwindigkeit;
		}
		else
		{	// Ladungswechselberechnung
			C_1 = 6.18 + 0.417 * vDrall / mittlereKolbengeschwindigkeit;
		}
		//TODO: Koeffizienten von anderen Motortypen einbauen...
		double C_2 = 0.00324; //Dieselmotoren mit Direkteinspritzung und Ottomotoren
		
		double Schleppdruck = druck_1*Math.pow((volumen_1/motor.get_V(time)),n); //[Pa]
		double Volumen = motor.get_V(time);	//[m^3]
		double v =  mittlereKolbengeschwindigkeit + C_2 / C_1 * Hubvolumen * temperatur_1 / (druck_1 * volumen_1) * (p - Schleppdruck);  
		// Alpha in W/(m²K)
		double alpha = 130 *Math.pow( (Bohrungsdurchmesser),-0.2)*Math.pow(p*1e-5,0.8)*Math.pow(T,-0.53)*Math.pow((C_1 * v ),0.8) ;
		//Statt p in bat einzugeben kann auch 0.013 und der Druck in Pa verwendet werden!		
		return alpha;
	}

	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time);//0*motor.get_FeuerstegFlaeche();
	}
}
