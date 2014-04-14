package berechnungsModule.blowby;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;

public class LeckageMassenstrom extends BlowBy {
	private double Aeff;
	private boolean isIndiziert;
	private IndizierDaten indi;

	protected LeckageMassenstrom(CasePara CP) {
		super(CP);
		if(CP.is_pKGH_indiziert()){
			indi = new IndizierDaten(CP);
			isIndiziert = true;
		}
		Aeff = cp.get_FlaecheBB();
	}
	/**
	 * Berechnung der BlowBy-Masse nach VL-Skript "Thermodynamik und Energieumsetzung - 11 Thermodynamik
	 * des Hochdruckprozesses" (WS 2013/2014). Verbrannte Zone muss die letzte Zone sein (höchster Index)!
	 * @param time - Zeit
	 * @param zonen_IN - Alle Zonen
	 */
	public double get_mLeckage(double time, Zone[] zonen_IN) {
		double dmL=0;
		double mges=0;
		//Bestimmen der Gesamtmasse
		for(int i=0; i<zonen_IN.length; i++){
			mges += zonen_IN[i].get_m();
		}
		
		//Berechnen des Gesamtleckagemassenstroms. Die Aufteilung auf die Zonen erfolgt in der DVA/LWA;
		double p1 = zonen_IN[0].get_p();
		double p2;
		if(isIndiziert){
			p2 = indi.get_pKGH(time); // Kurbelgehäusedruck
		}else{
			p2 = cp.get_pKGH();
		}
		
		for(int i=0; i<zonen_IN.length; i++){
			double R = zonen_IN[i].get_ggZone().get_R();
			double T = zonen_IN[i].get_T();
			double kappa = zonen_IN[i].get_ggZone().get_kappa(T);
			double anteil = zonen_IN[i].get_m() / mges;
			
			double t1 = Math.sqrt(1/(R*T));
			double t2 = Math.sqrt(2*kappa / (kappa - 1));
			double t3 = Math.pow( p2/p1 , 2/kappa ) - Math.pow( p2/p1 , (kappa+1)/kappa );
			if(p2>p1){ //falls der Druck im Zylinder kleiner als der Kurbelgehäusedruck ist
				t3 = -t3;
				anteil = -anteil; //damit wird der Massenstrom dann negativ
			}
			dmL += anteil * Aeff * p1 * t1 * t2 * Math.sqrt(t3);
		}
		return dmL;
	}
	@Override
	public boolean is_Berechnet() {
		return true;
	}
}
