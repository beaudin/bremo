package berechnungsModule.Berechnung;


import berechnungsModule.ErgebnisBuffer;
import bremo.parameter.CasePara;

public abstract class APR extends BerechnungsModell {	


	protected APR(CasePara cp) {
		super(cp,new ErgebnisBuffer(cp,"APR_"));
	}

	@Override
	public boolean isDVA() {		
		return false;
	}	
	
	public abstract Zone [] calc_dQburn(Zone [] zonen);
	
}
