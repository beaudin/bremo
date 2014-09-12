package berechnungsModule.wandwaerme;

import berechnungsModule.Berechnung.DVA_Homogen_EinZonig;
import berechnungsModule.Berechnung.DVA_homogen_ZweiZonig;
import berechnungsModule.Berechnung.DVA_DualFuel;
import berechnungsModule.Berechnung.APR_homogen_EinZonig;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;

public class Bargende extends WandWaermeUebergang {
	
	private CasePara cp;
	private Motor_HubKolbenMotor motor;
	private double T_m;
	private double T_w;
	private double k;
	private double w;
	private double c_k;
	private double T_zzp;							//Temp. zum Zündzeitpunkt (ZZP)
	private double p_zzp;							//Druck zum ZZP
	
	/*Konstante bei vereinfachter Berechnung: 253.5 da r =0.5 und R =292 [J/kg/K]
	  Konstante bei vollständiger Berechnung: 27.97*1e3*/
	private static final double C = 27.97*1e3;
	
	protected Bargende(CasePara cp) {
		super(cp);
		this.cp = super.cp;
		this.motor =(Motor_HubKolbenMotor) super.motor;
		super.feuerstegMult=0.25;	
	}

	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {
		
		//Turbulenz k aus Berechnungsmodell
		k = cp.BERECHNUNGS_MODELL.get_turbFaktor(zonen_IN, time);
		
		double p = zonen_IN[0].get_p();										//Zylinderdruck
		double lambda = zonen_IN[0].get_ggZone().get_lambda();				//Luftverhältnis
		double Lst = cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst();	//Mindestluftbedarf
		double R = get_R_brennraum(time, zonen_IN);							//Gaskonstante
//		double R = 292;														//für vereinfachte Rechnung
		double r = (lambda-1)/(lambda+1/Lst);								//Luftgehalt
//		double r = 0.5;														//für vereinfachte Rechnung
		double T = get_Tmb(zonen_IN);   									//Mittlere Brennraumtemperatur

		//Bestimmung der gemittelten Wandtemp. aus gewichteten Flächentemp (s. Bargende Gl. 6.3)
		Motor_HubKolbenMotor hkm = ((Motor_HubKolbenMotor)motor);
		double pistonSurf = hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();
		double headSurf = hkm.get_fireDeckArea();
		double cylWallSurf = hkm.get_CylinderLinerArea(time);
		double totalSurf = pistonSurf+headSurf+cylWallSurf;
		double T_cyl = cp.get_T_Cyl();										//Temp der einzelnen Flächen
		double T_piston = cp.get_T_Piston();	
		double T_head = cp.get_T_Head();
		T_w = ((pistonSurf*T_piston)+(headSurf*T_head)+(cylWallSurf*T_cyl))
				/totalSurf;	
		T_m = (T+T_w)/2; 													//Gemittelte Temp. an Grenzschicht
		
		//Wärmeübergangsrelevante Geschwindigkeit
		c_k = motor.get_Kolbengeschwindigkeit(time);						//momentane Kolbengeschwindigkeit
		w = Math.sqrt(8*k+Math.pow(c_k,2))/2;
		//in Bargende sqrt(8*k+c_k^2), aber Grill und Merker verwenden 8/3*k!!!
		
		double verbrennungsterm = get_Verbrennungsterm(p, T, fortschritt, time, zonen_IN);
		
		double alpha = C*(1.15*r+2.02)/(Math.pow(R*(2.57*r+3.55),0.78)) *Math.pow(motor.get_V(time),-0.073) 
				*Math.pow(T_m, -0.477) *Math.pow(p*1e-5,0.78) *Math.pow(w,0.78)*verbrennungsterm;
		return alpha;
	}

	private double get_Verbrennungsterm(double p, double T, double fortschritt, double time, Zone[] zonen_IN) {
		
		double A, B, verbrennungsbeginn = 0;
		double T_uv;														//Temp. im Unverbrannten
		double T_v;															//Temp. im Verbrannten
		double kappa = zonen_IN[0].get_ggZone().get_kappa(fortschritt);		//Polytropenexponent aus Zone(n)
		
		//Normierter Summenbrennverlauf X durch fortschritt ausgedrückt
		double X = fortschritt;
		
		if (fortschritt <= 0.02){											//Hier wird ZZP bei Umsatz von 1% angenommen
			verbrennungsbeginn = fortschritt;
			T_zzp = T;														//Temp und Druck zum ZZP "festhalten"
			p_zzp = p;
		}
		
		if ((fortschritt > verbrennungsbeginn) && (fortschritt <= 0.99)){	//Verbrennungsterm für Hochdruckteil zwischen Umsatz von 1%-99%
			T_uv = T_zzp * Math.pow( p/p_zzp, (kappa-1)/kappa);				//aus polytroper Verdichtung ausgehend vom ZZP
			T_v = 1/X*T+(X-1)/X*T_uv;												
			A = X*(T_v/T)*(T_v-T_w)/(T-T_w);
			B = (1-X)*(T_uv/T)*(T_uv-T_w)/(T-T_w);

			return Math.pow((A+B),2);
		}
		else {
			return 1;
		}	
	}
	
	public double get_BrennraumFlaeche(double time) {
		return motor.get_BrennraumFlaeche(time)+0.25*motor.get_FeuerstegFlaeche();
	}
	
}
