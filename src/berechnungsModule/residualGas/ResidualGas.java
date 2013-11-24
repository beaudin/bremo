package berechnungsModule.residualGas;

import bremo.parameter.CasePara;


public abstract class ResidualGas {
	protected final CasePara CP;	

	protected ResidualGas(CasePara cp){
		CP=cp;
	}


	/**
	 * 
	 * @return returns the mass of residual gas per cycle in [kg/Cycle]
	 */
	public abstract double get_mResidualGas_perCycle();
	

	/**
	 * Returns true if the choosen model for internal EGR involves 
	 * the calculation of the gasexchange process
	 * @return
	 */
	public abstract boolean involvesGasExchangeCalc();




}
