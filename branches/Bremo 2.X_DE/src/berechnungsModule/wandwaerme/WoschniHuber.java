package berechnungsModule.wandwaerme;


import matLib.MatLibBase;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;


public class WoschniHuber extends WandWaermeUebergang {

	private Motor_HubKolbenMotor motor;
	private CasePara cp;
	protected final IndizierDaten indiD;
	private double vDrall;
	private double Bohrungsdurchmesser;
	private double Hubvolumen;
	private double Kompressionsvolumen;
	private double mittlereKolbengeschwindigkeit;
	private double Temperatur_1;
	private double Druck_1;
	private double Volumen_1;
	private double refPunkt;
	private double p_mi;
	private double n;
	private double pZyl_a;
	private double Vol_a;
	private double C_1;
	private double C_2;

	protected WoschniHuber(CasePara mp) {
		super(mp);		
		this.cp=super.cp;
		indiD=new IndizierDaten(cp);	//TODO: umprogrammieren, so dass die Indizierdatei nur ein einziges Mal eingelesen wird
		this.motor=(Motor_HubKolbenMotor) super.motor;
		Bohrungsdurchmesser = motor.get_Bohrung(); //[m]
		Hubvolumen = motor.get_Hubvolumen();  //[m^3]
		Kompressionsvolumen = motor.get_Kompressionsvolumen();	//[m^3]
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_DrehzahlInUproSec(); //[m/s]
		Temperatur_1=cp.get_T_IVC_WHT(); //[K]
		Druck_1=indiD.get_pZyl(cp.get_Einlassschluss()); //[bar]
		Volumen_1=motor.get_V(cp.get_Einlassschluss());	//[m^3]
		vDrall=cp.get_DrallGeschwindigkeit();
		p_mi=indiD.get_pmi();
		//Polytropenexponent für die Schleppdruckberechnung ermitteln.
		//Dies wird in den 10°KW vorm Referenzpunkt gemacht...
		refPunkt=cp.get_refPunkt_WoschniHuber();
		super.feuerstegMult=0;

		double[] n_array = new double[10]; 
		pZyl_a = indiD.get_pZyl(refPunkt);
		Vol_a = motor.get_V(refPunkt);
		double pZyl_b = 0;
		double Vol_b = 0;
		int cnt=0;

		for(double kw=cp.convert_SEC2KW(refPunkt)-10; kw < cp.convert_SEC2KW(refPunkt); kw++){
			pZyl_b=indiD.get_pZyl(cp.convert_KW2SEC(kw));
			Vol_b=motor.get_V(cp.convert_KW2SEC(kw));
			n_array[cnt]=Math.log10(pZyl_a/pZyl_b)/Math.log10(Vol_b/Vol_a);
			cnt++;
		}
		n=MatLibBase.mw_aus_1DArray(n_array); //Polytropenexponent
	}	



	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {
		//Quick and dirty but works...
		if(!cp.BERECHNUNGS_MODELL.isDVA()){
			try{
			throw new BirdBrainedProgrammerException("WoschniHuber kann nur bei einer DVA benutzt werden");
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}			
		}
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		// Bestimmung an Hand des Kurbelwinkels und der Steuerzeiten ob Ladungswechsel oder Hochdruckteil
		// Eine Zwischenkompression wird dem Hochdruckteil zugerechnet (Hönl)
		C_1 = 0;
		if (	// Hochdruckteil	
				((motor.get_Einlass_schliesst() <= time) && (motor.get_Auslass_oeffnet() >= time))
				|| 
				// Zwischenkompression
				(motor.get_Einlass_oeffnet() >= time) 	
		)
		{	// Hochdruckberechnung
			C_1 = 2.28 + 0.308 * vDrall / mittlereKolbengeschwindigkeit;
		}
		else
		{	// Ladungswechselberechnung
			C_1 = 6.18 + 0.417 * vDrall / mittlereKolbengeschwindigkeit;
		}
		//TODO: Koeffizienten von anderen Motortypen einbauen...
		C_2 = 0.00324; //Dieselmotoren mit Direkteinspritzung und Ottomotoren

		double Schleppdruck = pZyl_a*Math.pow((Vol_a/motor.get_V(time)),n); //[Pa]
		double Volumen = motor.get_V(time);	//[m^3]
		double v_Woschni =  mittlereKolbengeschwindigkeit + C_2 / C_1 * Hubvolumen * Temperatur_1 / (Druck_1 * Volumen_1) * (p - Schleppdruck);  
		double v_Huber = mittlereKolbengeschwindigkeit*(1 + 2 * (Kompressionsvolumen / Volumen) * (Kompressionsvolumen / Volumen) * Math.pow(p_mi,-0.2) );
		//		if(cp.convert_SEC2KW(time)>-20){
		//			System.out.println("");}
		v_Huber=0;
		double v = Math.max(v_Woschni,v_Huber);

		// Alpha in W/(m²K)
		double alpha = 130 *Math.pow( (Bohrungsdurchmesser),-0.2)*Math.pow(p*1E-5,0.8)*Math.pow(T,-0.53)*Math.pow((C_1 * v ),0.8) ;
		return alpha;
	}
	
	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time)+0*motor.get_FeuerstegFlaeche();
	}
}

