package berechnungsModule.blowby;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;

public class leckagefrei extends BlowBy {

	protected leckagefrei(CasePara CP) {
		super(CP);
	}


	public double get_mLeckage(double time, Zone[] zonen_IN) {
		return 0;
	}

	@Override
	public boolean is_Berechnet() {
		return false;
	}
	

}
