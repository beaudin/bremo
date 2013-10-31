package berechnungsModule.wandwaerme;

import misc.VektorBuffer;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;


public class PerfektIsoliert extends WandWaermeUebergang {

	public PerfektIsoliert(CasePara cp){		
		super(cp);	
	}	
	

	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {			
		return 0;
	}
	
	
	public double get_BrennraumFlaeche(double time) {	
		return 0;
	}
	
	@Override
	public double get_WandWaermeStrom(double time, Zone[] zonen_IN, double fortschritt, VektorBuffer tBuffer){
		return 0;
	}	

}
