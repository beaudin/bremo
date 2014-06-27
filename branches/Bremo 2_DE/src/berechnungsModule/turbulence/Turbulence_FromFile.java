package berechnungsModule.turbulence;

import io.TurbulenceFileReader;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;

public class Turbulence_FromFile extends TurbulenceModel {

	private final TurbulenceFileReader TFR;
	public Turbulence_FromFile(CasePara cp)  {
		super(cp);
		TFR=new TurbulenceFileReader(cp);		
	}

	@Override
	public double get_eps(Zone [] zn,double time) {		
		return TFR.get_EPS(time);
	}

	@Override
	public double get_intensity(Zone [] zn,double time) {		
		return TFR.get_TI(time);
	}

	@Override
	public double get_k(Zone [] zn,double time) {		
		return TFR.get_TKE(time);
	}

	@Override
	public void update(Zone[] zn, double time) {
		//there is nothing to integrate --> do nothing	
	}

	@Override
	public void initialize(Zone[] zn, double time) {
		// nothing to initialize --> do nothing
		
	}

	
	/**
	 * Returns 2/3*k/eps*u
	 */	
	public double get_turbulenceIntegralLengthScale(Zone zn[],double time) {
		//TODO Make reader function from file!
		return 2d/3d*get_k(zn,time)/get_eps(zn, time)*get_intensity(zn,time);
	}

	//Methode wird benötigt, um bei einer DVA mit WWM Bargende die Turbulenz richtig berechnen zu können
	@Override
	public void set_k(double turbulence,int i){
		// TODO Auto-generated method stub	
	}

}
