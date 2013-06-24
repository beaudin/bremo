package berechnungsModule.internesRestgas;


import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public abstract class InternesRestgas {
	protected final CasePara CP;	

	protected InternesRestgas(CasePara cp){
		CP=cp;
	}


	/**
	 * 
	 * @return liefert die Masse des internen Restgases in [kg/ASP]
	 */
	public abstract double get_mInternesRestgas_ASP();
	

	/**
	 * Returns true if the choosen model for internal EGR involves 
	 * the calculation of the gasexchange process
	 * @return
	 */
	public abstract boolean involvesGasExchangeCalc();




}
