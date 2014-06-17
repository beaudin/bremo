package berechnungsModule.wandwaerme;

import berechnungsModule.Berechnung.DVA_Homogen_EinZonig;
import berechnungsModule.Berechnung.DVA_homogen_ZweiZonig;
import berechnungsModule.Berechnung.DVA_DualFuel;
import berechnungsModule.Berechnung.APR_homogen_EinZonig;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import bremo.parameter.*;



public class Bargende extends WandWaermeUebergang {
	
	//protected final IndizierDaten indiD;
	private CasePara cp;
	private Motor_HubKolbenMotor motor;
	private double Hubvolumen;
	private double T_m;
	private double T_w;
	private double k;
	private double w;
	private double mittlereKolbengeschwindigkeit;
	private double T_zzp;															//Temp. zum Zündzeitpunkt (ZZP)
	private double p_zzp;															//Druck zum ZZP
	private static final double C = 27.97*1e3; 
	
	/*Konstante bei vereinfachter Berechnung: 253.5 da r =0.5 und R =292 [J/kg/K] 
	  Konstante bei vollständiger Berechnung: 27.97*1e3*/

	
	protected Bargende(CasePara cp) {
		super(cp);
		this.cp = super.cp;
		
		/*indiD = new IndizierDaten(cp);
		p_zzp = indiD.get_pZyl(cp.get_ZZP());*/										//ZZP nicht ermittelbar bzw. nur aus Inputfile
		
		this.motor =(Motor_HubKolbenMotor) super.motor;
		Hubvolumen = motor.get_Hubvolumen();
		mittlereKolbengeschwindigkeit = 2*motor.get_Hub()*cp.get_DrehzahlInUproSec();
		super.feuerstegMult=0.25;	
	}


	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {
		
		double p = zonen_IN[0].get_p(); 											//Zylinderdruck
		
		if (cp.MODUL_VORGABEN.get("berechnungsModell").equals("DVA_1Zonig")){		//Typecasten von Turbulenz k aus jeweiligem Berechnungsmodell
			k = ((DVA_Homogen_EinZonig)cp.BERECHNUNGS_MODELL).get_turbFaktor(zonen_IN, time);
			}
		else if (cp.MODUL_VORGABEN.get("berechnungsModell").equals("DVA_2Zonig")){
				k = ((DVA_homogen_ZweiZonig)cp.BERECHNUNGS_MODELL).get_turbFaktor(zonen_IN, time);
			}
		else if (cp.MODUL_VORGABEN.get("berechnungsModell").equals("DVA_DualFuel")){
				k = ((DVA_DualFuel)cp.BERECHNUNGS_MODELL).get_turbFaktor(zonen_IN,time);
			}
		else if (cp.MODUL_VORGABEN.get("berechnungsModell").equals("APR_1Zonig")){
				k = ((APR_homogen_EinZonig)cp.BERECHNUNGS_MODELL).get_turbFaktor(zonen_IN, time);
			}
 
		double lambda = zonen_IN[0].get_ggZone().get_lambda();						//Luftverhältnis
		double Lst = cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst();			//Mindestluftbedarf
		double R = get_R_brennraum(time, zonen_IN);									//Gaskonstante
		double r = (lambda-1)/(lambda+1/Lst);										//Luftgehalt
		double T = get_Tmb(zonen_IN);   											//Mittlere Brennraumtemperatur

		Motor_HubKolbenMotor hkm = ((Motor_HubKolbenMotor)motor);
		double pistonSurf = hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();
		double headSurf = hkm.get_fireDeckArea();
		double cylWallSurf = hkm.get_CylinderLinerArea(time);
		double totalSurf = pistonSurf+headSurf+cylWallSurf;
		
		double T_cyl = cp.get_T_Cyl();												//Temp der einzelnen Flächen
		double T_piston = cp.get_T_Piston();	
		double T_head = cp.get_T_Head();
		
		T_w = ((pistonSurf*T_piston)+(headSurf*T_head)+(cylWallSurf*T_cyl))			//Gemittelte Wandtemp. aus gewichteten Flächentemp
				/totalSurf;															//s. Bargende Gl. 6.3	
		
		T_m = (T+T_w)/2; 															//Gemittelte Temp. an Grenzschicht
		
		w = Math.sqrt(8*k+Math.pow(mittlereKolbengeschwindigkeit,2))/2;				//Wärmeübergangsrelevante Geschwindigkeit
		
		double verbrennungsterm = get_Verbrennungsterm(p, T, fortschritt, time, zonen_IN);
		
		double alpha = C*(1.15*r+2.02)/(Math.pow(R*(2.57*r+3.55),0.78)) *Math.pow(motor.get_V(time),-0.073) 
				*Math.pow(T_m, -0.477) *Math.pow(p*1e-5,0.78) *Math.pow(w,0.78)*verbrennungsterm;
		return alpha;
	}

	
	private double get_Verbrennungsterm(double p, double T, double fortschritt, double time, Zone[] zonen_IN) {
		
		double verbrennungsbeginn = 0;
//		double zuendzeitpunkt = 0;
		double A;
		double B;
		double T_uv;																//Temp. im Unverbrannten
		double T_v;																	//Temp. im Verbrannten
//		double kappa = cp.get_Kappa_Druckabgleich();
		double kappa = zonen_IN[0].get_ggZone().get_kappa(fortschritt);
		double X = fortschritt;														//Normierter Summenbrennverlauf X durch fortschritt ausgedrückt
		
		if (fortschritt <= 0.01){													//Hier wird ZZP bei Umsatz von 1% angenommen
			verbrennungsbeginn = fortschritt;
			T_zzp = T;																//Temp und Druck zum ZZP festhalten
			p_zzp = p;
		}
		
		if ((fortschritt > verbrennungsbeginn) && (fortschritt <= 0.90)){			//Verbrennungsterm für Hochdruckteil zwischen Umsatz von 1%-90%
			T_uv = T_zzp * Math.pow( p/p_zzp, (kappa-1)/kappa);						//aus polytroper Verdichtung ausgehend vom ZZP
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
