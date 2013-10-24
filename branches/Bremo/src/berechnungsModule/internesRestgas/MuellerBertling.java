package berechnungsModule.internesRestgas;

import bremo.parameter.CasePara;

public class MuellerBertling extends InternesRestgas {

	protected MuellerBertling(CasePara cp) {
		super(cp);		
	}

	@Override
	public double get_mInternesRestgas_ASP() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}

}
