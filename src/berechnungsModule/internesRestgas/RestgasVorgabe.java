package berechnungsModule.internesRestgas;

import bremo.parameter.CasePara;

public class RestgasVorgabe extends InternesRestgas{
	
	protected RestgasVorgabe(CasePara cp){
		super(cp);		
	}


	public double get_mInternesRestgas_ASP() {	
		return super.CP.get_mAGR_intern_ASP_aus_InputFile();
	}


	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}

}
