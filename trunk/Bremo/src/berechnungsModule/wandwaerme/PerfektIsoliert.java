package berechnungsModule.wandwaerme;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;


public class PerfektIsoliert extends WandWaermeUebergang {

	public PerfektIsoliert(CasePara cp){		
		super(cp);		
	}	
	

	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {			
		return 0;
	}
	
	@Override
	public double get_BrennraumFlaeche(double time) {	
		return 0;
	}	

}
