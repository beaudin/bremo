package berechnungsModule.verbrennung;

import bremo.parameter.CasePara;

public class DVA_homogen extends VerbrennungsWaermeFreisetzung {
	private double lambda;
	private double dQburn_alt;
	
	public DVA_homogen(CasePara cp){
		
	}
	
	public double get_dmBurn(double time) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public double get_dQBurn(double time) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double get_dQBurn( double pIst, double pSoll) {
		
		double pFaktor=(pSoll-pIst)/pSoll;
		//Berechnen der Brennrate und speicehrn der Brennrate als "alte" Brennrate
		dQburn_alt=dQburn_alt+pFaktor*dQburn_alt;
		return dQburn_alt;
	}
	

}
