package berechnungsModule.Berechnung;


import berechnungsModule.ResultsBuffer;
import bremo.parameter.CasePara;

public abstract class APR extends SimulationModel {	


	protected APR(CasePara cp) {
		super(cp,new ResultsBuffer(cp,"APR_"));
	}

	@Override
	public boolean isDVA() {		
		return false;
	}	
	
	public abstract Zone [] calc_dQburn(Zone [] zonen);
	

}
