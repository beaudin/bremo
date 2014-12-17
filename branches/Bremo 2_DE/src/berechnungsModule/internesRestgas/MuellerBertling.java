package berechnungsModule.internesRestgas;

import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;

/**
 * Berechnung der Restgasmasse bei AS oder (bei Ventilüberschneidung) im LOT.
 * @author kerrom, neurohr
 * @param Indizierdaten, offsetTemperatur, Auslassschluss
 *
 */
public class MuellerBertling extends InternesRestgas {
	
	private double mAGRint = -5.55;

	protected MuellerBertling(CasePara cp) {
		super(cp);
	}
	
	private double berechne_Restgasmasse(){
		IndizierDaten indiD = new IndizierDaten(CP);
		double zeitpunkt = CP.get_Auslassschluss();
		if(zeitpunkt>CP.convert_KW2SEC(360)){
			zeitpunkt = CP.convert_KW2SEC(360);
		}
		double volumen = CP.MOTOR.get_V(zeitpunkt);
		double p = 0;
		for(int i=0;i<=10;i++){
			p += indiD.get_pZyl(zeitpunkt+(i-5)*CP.SYS.WRITE_INTERVAL_SEC);
		}
		p = p / 11;
		double R = CP.get_spezAbgas().get_R(); //Ausgehend von reinem Abgas im Zylinder (Fehler zu reiner Luft sehr gering)
		double T = CP.get_T_Abgas() + CP.get_offsetTemperatur(); //Hier, weil Warnung sonst mehrmals auftaucht
		
		double AGRint = p * volumen / R / T;
		return AGRint;
	}

	@Override
	public double get_mInternesRestgas_ASP() {
		if(mAGRint==-5.55)
			mAGRint = berechne_Restgasmasse();
		return mAGRint;
	}
		
	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}

}
