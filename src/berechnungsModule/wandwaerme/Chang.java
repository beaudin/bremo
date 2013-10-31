package berechnungsModule.wandwaerme;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import matLib.MatLibBase;

import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;

public class Chang extends WandWaermeUebergang {
	private Motor_HubKolbenMotor motor;
	private CasePara cp;	
	private double vDrall;
	private double bohrungsdurchmesser;
	private double hubvolumen;
	private double mittlereKolbengeschwindigkeit;
	private double temperatur_1;
	private double druck_1;
	private double volumen_1;

	private double n;
	private double C_1;
	private double C_2;
	private double alpha_scaling;
	private boolean setRefConditions=true;

	/**
	 * Wall heat transfer according to SAE Technical Paper 2004-01-2996
	 * New Heat Transfer Correlation for an HCCI 
	 * Engine Derived from Measurements of Instantaneous Surface Heat Flux
	 * @param mp
	 */
	protected Chang(CasePara mp){
		super(mp);
		this.cp=super.cp;
			//TODO: umprogrammieren, so dass die Indizierdatei nur ein einziges Mal eingelesen wird
		this.motor=(Motor_HubKolbenMotor) super.motor;
		bohrungsdurchmesser = motor.get_Bohrung(); //[m]
		hubvolumen = motor.get_Hubvolumen();  //[m^3]
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_DrehzahlInUproSec(); //[m/s]		
		
		//Formel nach Pischinger "Thermodynamik der Verbrennungskraftmaschine" Seite 203
		vDrall=cp.get_swirlRatio()*cp.get_DrehzahlInUproSec()*Math.PI*motor.get_Bohrung()*0.7;

		C_1 = 2.28 + 0.308 * vDrall / mittlereKolbengeschwindigkeit;
		C_2 = 0.00324;
		alpha_scaling = 3.4; //aus paper
		super.feuerstegMult=0.25;
	}
	
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN,double fortschritt) {
		if(setRefConditions){
			setRefConditions=false;
			if(time!=cp.get_Einlassschluss()){//simulation start is not at IVC (e.g homogeneous multizone model
				temperatur_1=cp.get_T_IVC_WHT(); //[K]	
				n=cp.get_polyExp_WHT();
				if(cp.compareToExp()){
					IndizierDaten indiD=new IndizierDaten(cp);
					druck_1=indiD.get_pZyl(cp.get_Einlassschluss()); 
				}
				else{				
					druck_1=cp.get_p_IVC_WHT();
				}				
				volumen_1=motor.get_V(cp.get_Einlassschluss());	//[m^3]				
			}else{
//				try{
//					throw new BirdBrainedProgrammerException("Checken " +
//							"ob der WHT nach Chang funktioniert!!");
//				}catch(BirdBrainedProgrammerException bbpe){
//					bbpe.stopBremo();
//				}
				temperatur_1=super.get_Tmb(zonen_IN);
				//getting gamma for the whole cylinder
				double mTot=0;
				for(int i=0; i<zonen_IN.length;i++)mTot+=zonen_IN[i].get_m();
				Hashtable <Spezies,Double> ht=new Hashtable <Spezies,Double>();
				for(int i=0; i<zonen_IN.length;i++)
					ht.put(zonen_IN[i].get_ggZone(), zonen_IN[i].get_m()/mTot);
				
				GasGemisch gg =new GasGemisch("gg");
				gg.set_Gasmischung_massenBruch(ht);
				this.n=gg.get_kappa(temperatur_1);				
				druck_1=zonen_IN[0].get_p();
				volumen_1=motor.get_V(time);
			}			
		}
		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double s_alpha = motor.get_Kolbenweg(time);	
		double Schleppdruck = druck_1*Math.pow((volumen_1/motor.get_V(time)),n); //[Pa]

		double bdh=motor.get_Kompressionsvolumen()*4/Math.PI/Math.pow(motor.get_Bohrung(), 2);
		
		//double L_char = s_alpha + brennraumdachhoehe;
		double L_char = s_alpha + bdh;
		if (L_char >= bohrungsdurchmesser)
		{	
			L_char = bohrungsdurchmesser;
		}

		double v_tuned =  mittlereKolbengeschwindigkeit* C_1 + C_2 / 6 * hubvolumen * temperatur_1 / (druck_1 * volumen_1) * (p - Schleppdruck);  
		double alpha = alpha_scaling * 130*Math.pow( (L_char ),-0.2)*Math.pow(p*1E-5,0.8)*Math.pow(T,-0.73)*Math.pow((v_tuned ),0.8) ;

		return alpha;
	}
	
	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time)+0.25*motor.get_FeuerstegFlaeche();
	}

}
