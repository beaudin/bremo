package berechnungsModule.turbulence;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;

public class Turbulence_Zero extends TurbulenceModel{

	public Turbulence_Zero(CasePara cp) {
		super(cp);
	}

	@Override
	public double get_eps(Zone[] zn, double time) {
		return 0;
	}
	@Override
	public double get_intensity(Zone[] zn, double time) {
		return 0;
	}
	@Override
	public double get_k(Zone[] zn, double time) {
		return 0;
	}
	@Override
	public double get_turbulenceIntegralLengthScale(Zone[] zn, double time) {
		return 0;
	}
	
	@Override
	public void update(Zone[] zn, double time) {}
	@Override
	public void initialize(Zone[] zn, double time) {}
	@Override
	public void set_k(double turbulence, int i) {}	
}