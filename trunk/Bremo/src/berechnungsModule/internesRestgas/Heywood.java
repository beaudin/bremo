package berechnungsModule.internesRestgas;

import io.VentilhubFileReader;
import bremo.parameter.CasePara;

public class Heywood extends InternesRestgas {
	private double masseRestgas = -5.55;

	protected Heywood(CasePara cp) {
		super(cp);	
	}

	@Override
	public double get_mInternesRestgas_ASP() {
		/** Berechnung der internen Restgasmasse nach Heywood.
		 * Berechnung nach SAE Paper 931025 von 1993.
		 * Berechnung lt. Paper nur für niedrige Drehzahlen gültig! 
		 * Außerdem nur gültig bei positiver Ventilüberschneidung, sonst Überschneidungsfaktor = 0!!
		 */
		if(masseRestgas==-5.55)
			masseRestgas = internesRestgasBerechnen(super.CP);
		
		return masseRestgas;
	}
	
	private double internesRestgasBerechnen(CasePara cp){
		double rateRestgas = 0;
		double masseRestgas;
		
		if(cp.get_Einlassoeffnet()>=cp.get_Auslassschluss()) {
			double volumen = cp.MOTOR.get_V(cp.get_Auslassschluss());
			masseRestgas = cp.get_p_Abgas() * volumen / (cp.get_T_Abgas() * cp.get_spezAbgas().get_R()); 
		}
		
		cp.get_spezAbgas().get_R();
		
		// Größen aus der Input-Datei:
		double drehzahl,pIntake,pExhaust,epsilon;
		
		drehzahl = cp.get_DrehzahlInUproSec(); //[U/sec]
		pIntake = cp.get_p_LadeLuft() / Math.pow(10,5); //[Pa] -> [bar]
		pExhaust = cp.get_p_Abgas() / Math.pow(10,5); //[Pa] -> [bar]
		epsilon = cp.get_Verdichtung();
		
		double fuelEquRatio = 1 / cp.get_Lambda_trocken();
		double overlapFactor = 0;
		
		//Ventilüberschneidung aus Wert oder Hubkurven berechnen:
		if(cp.SYS.VENTILHUB_EIN_FILE!=null && cp.SYS.VENTILHUB_AUS_FILE!=null){
			overlapFactor = overlapfactor_aus_Hubkurven(cp);
		}else{
			double bohrung = cp.get_Bohrung() * 1000; //[m] -> [mm]
			double ventilhub= (cp.get_EV_Hub_max() + cp.get_AV_Hub_max()) / 2 * 1000; //[m] -> [mm]
			double ventildurchmesser = (cp.get_EV_durchmesser() + cp.get_AV_durchmesser()) / 2 * 1000; //[m] -> [mm]
			double valveOverlap = cp.get_Ventilueberschneidung(); //[KW]
			
			//OF in °KW/m
			overlapFactor = 1.45 / bohrung * (107 + 7.8 * valveOverlap + Math.pow(valveOverlap,2)) * ventilhub * ventildurchmesser / Math.pow(bohrung,2);
		}

		rateRestgas = 1.266 * overlapFactor / drehzahl * Math.pow((pIntake / pExhaust),-0.87) * Math.sqrt(Math.abs(pExhaust-pIntake)) + 0.632/epsilon * fuelEquRatio * Math.pow((pIntake/pExhaust),-0.74);
		
		masseRestgas = (cp.get_mLuft_feucht_ASP() + cp.get_mAGR_extern_ASP() + cp.get_mKrst_ASP()) * rateRestgas / (1 - rateRestgas);
		return masseRestgas;
	}
	
	private double overlapfactor_aus_Hubkurven(CasePara cp) {
		double overlapfactor;
		
		VentilhubFileReader EV_Hub = new VentilhubFileReader(cp,"Einlass");
		VentilhubFileReader AV_Hub = new VentilhubFileReader(cp,"Auslass");
		
		double t_EV_015, t_AV_015, t_EV_AV;
		double t_AV_offset = 2 / cp.get_DrehzahlInUproSec();
		
		//Zeitpunkt von EV-Hub = 0,15mm berechnen.
		double ti = cp.get_Einlassoeffnet();
		double t0 = cp.get_Einlassoeffnet();
		
		while(EV_Hub.get_Hub(ti) < 0.00015) {
			t0 = ti;
			ti += 0.00001;
		}
		t_EV_015 = (((0.00015 - EV_Hub.get_Hub(ti)) * (ti - t0)) / (EV_Hub.get_Hub(t0) - EV_Hub.get_Hub(ti))) + ti;
		
		
		if(AV_Hub.get_Hub(t_EV_015 + t_AV_offset) <= 0.00015) return 0;
		
		//Zeitpunkt von AV-Hub = 0,15mm berechnen.
		t0 = cp.get_Auslassoeffnet();
		ti = cp.get_Auslassschluss();
		ti = (ti + t0) / 2;
		t0 = ti;
		
		while(AV_Hub.get_Hub(ti) > 0.00015) {
			t0 = ti;
			ti += 0.00001;
		}
		t_AV_015 = (((0.00015 - AV_Hub.get_Hub(ti)) * (ti - t0)) / (AV_Hub.get_Hub(t0) - AV_Hub.get_Hub(ti))) + ti;
		
		if(t_AV_015 <= (t_EV_015 + t_AV_offset)) return 0;
		
		//Finden des Zeitpunktes, bei dem EV-Hub = AV-Hub ist.
		double ti_EV = t_EV_015;
		double ti_AV = t_AV_offset + t_EV_015;
		double t0_EV = ti_EV, t0_AV = ti_AV;
		
		while(AV_Hub.get_Hub(ti_AV) > EV_Hub.get_Hub(ti_EV)) {
			t0_EV = ti_EV;
			t0_AV = ti_AV;
			ti_EV += 0.00001;
			ti_AV += 0.00001;
		}
		
		double e1 = EV_Hub.get_Hub(t0_EV);
		double e2 = EV_Hub.get_Hub(ti_EV);
		double a1 = AV_Hub.get_Hub(t0_AV);
		double a2 = AV_Hub.get_Hub(ti_AV);
		
		t_EV_AV = ti_EV + (((t0_EV - ti_EV) * (e2 - a2)) / (a1 - a2 - e1 + e2));

		//Integral der entsprechenden Flächen unter den Hubkurven berechnen [°KW*m].
		double flaeche_EV = EV_Hub.get_integral(cp.get_DrehzahlInUproSec(), t_EV_015, t_EV_AV);
		double flaeche_AV = AV_Hub.get_integral(cp.get_DrehzahlInUproSec(), t_AV_offset + t_EV_AV, t_AV_015);
		
		//OF in °KW/m
		overlapfactor = ((cp.get_EV_durchmesser() * flaeche_EV) + (cp.get_AV_durchmesser() * flaeche_AV)) / (0.25 * Math.PI * Math.pow(cp.get_Bohrung(),2));
		
		return overlapfactor;
	}

}
