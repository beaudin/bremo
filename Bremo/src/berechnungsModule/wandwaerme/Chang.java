package berechnungsModule.wandwaerme;

import matLib.MatLibBase;

import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;

public class Chang extends WandWaermeUebergang {
	private Motor_HubKolbenMotor motor;
	private CasePara cp;
	protected final IndizierDaten indiD;
	private double vDrall;
	private double bohrungsdurchmesser;
	private double hubvolumen;
	private double mittlereKolbengeschwindigkeit;
	private double temperatur_1;
	private double druck_1;
	private double volumen_1;
	private double refPunkt;
	private double p_mi;
	private double n;
	private double pZyl_a;
	private double Vol_a;
	private double brennraumdachhoehe;
	private double C_1;
	private double C_2;
	private double alpha_scaling;

	protected Chang(CasePara mp){
		super(mp);
		this.cp=super.cp;
		indiD=new IndizierDaten(cp);	//TODO: umprogrammieren, so dass die Indizierdatei nur ein einziges Mal eingelesen wird
		this.motor=(Motor_HubKolbenMotor) super.motor;
		bohrungsdurchmesser = motor.get_Bohrung(); //[m]
		hubvolumen = motor.get_Hubvolumen();  //[m^3]
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_DrehzahlInUproSec(); //[m/s]
		temperatur_1=cp.get_T_EinlassSchluss(); //[K]
		druck_1=indiD.get_pZyl(cp.get_Einlassschluss()); //[bar]
		volumen_1=motor.get_V(cp.get_Einlassschluss());	//[m^3]
		brennraumdachhoehe = cp.get_BrennraumdachHoehe();
		vDrall=cp.get_DrallGeschwindigkeit();
		p_mi=indiD.get_pmi();				//[bar]
		if(p_mi<1){
			p_mi=1;	//pmi muss > 1 bar sein...
		}
		C_1 = 2.28 + 0.308 * vDrall / mittlereKolbengeschwindigkeit;
		C_2 = 0.00324;
		alpha_scaling = 3.4;
		//Polytropenexponent für die Schleppdruckberechnung ermitteln.
		//Dies wird in den 10°KW vorm Referenzpunkt gemacht...
		refPunkt=cp.get_refPunkt_WoschniHuber();
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
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN,double fortschritt) { 
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double s_alpha = motor.get_Kolbenweg(time);	
		double Schleppdruck = pZyl_a*Math.pow((Vol_a/motor.get_V(time)),n)*1E-5; //[bar]

		double L_char = s_alpha + brennraumdachhoehe;
		if (L_char >= bohrungsdurchmesser)
		{	
			L_char = bohrungsdurchmesser;
		}

		double v_tuned =  mittlereKolbengeschwindigkeit* C_1 + C_2 / 6 * hubvolumen * temperatur_1 / (druck_1 * volumen_1) * (p - Schleppdruck*1E5);  
		double alpha = alpha_scaling * 130 *Math.pow( (L_char ),-0.2)*Math.pow(p*1E-5,0.8)*Math.pow(T,-0.73)*Math.pow((v_tuned ),0.8) ;

		return alpha;
	}
	
	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time)+0.25*motor.get_FeuerstegFlaeche();
	}

}
