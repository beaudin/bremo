package berechnungsModule.wallHeatTransfer;

import misc.VectorBuffer;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;


public class PerfektIsoliert extends WallHeatTransfer {

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
	public double get_wallHeatFlux(double time, Zone[] zonen_IN, double fortschritt, VectorBuffer tBuffer){
		return 0;
	}	

}
