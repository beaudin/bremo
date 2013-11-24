package berechnungsModule.residualGas;

import bremo.parameter.CasePara;

public class Heywood extends ResidualGas {

	protected Heywood(CasePara cp) {
		super(cp);	
	}

	@Override
	public double get_mResidualGas_perCycle() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}

}
