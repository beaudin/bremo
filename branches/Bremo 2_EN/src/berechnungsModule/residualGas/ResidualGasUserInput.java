package berechnungsModule.residualGas;

import bremo.parameter.CasePara;

public class ResidualGasUserInput extends ResidualGas{
	
	protected ResidualGasUserInput(CasePara cp){
		super(cp);		
	}


	public double get_mResidualGas_perCycle() {	
		return super.CP.get_mRG_perCycle_FromInputFile(new CallOnlyFromResidualGasUserInput());
	}


	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}
	
	public class CallOnlyFromResidualGasUserInput{
		private CallOnlyFromResidualGasUserInput(){
			
		}
		
	}

}


