package berechnungsModule.internesRestgas;

import bremo.parameter.CasePara;

public class MuellerBertling extends InternesRestgas {

	protected MuellerBertling(CasePara cp) {
		super(cp);		
	}

	private double zeitpunktAuslassschluss = super.CP.get_Auslassschluss();
	private double volumen = super.CP.MOTOR.get_V(zeitpunktAuslassschluss);
	private double p = super.CP.get_p_Abgas();
	private double R = super.CP.get_spezAbgas().get_R();
	private double offsetTemperatur = super.CP.get_offsetTemperatur(); //Hier, weil Warnung sonst mehrmals auftaucht
	private double mInternesRestgas = berechne_mInternesRestgas_ASP();
	
	private double berechneTemperatur(){
		return super.CP.get_T_Abgas() + offsetTemperatur;
	}
	
	private double berechne_mInternesRestgas_ASP(){
		return p*volumen/(R*berechneTemperatur());
	}
	
	@Override
	public double get_mInternesRestgas_ASP() {
		return mInternesRestgas;
	}
		
	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}

}
