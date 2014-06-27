package berechnungsModule.turbulence;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;

public abstract class TurbulenceModel {

	protected final CasePara CP;
	public TurbulenceModel(CasePara cp) {
		this.CP=cp;
	}
	
	/**
	 * Returns the dissipation rate of turbulence kinetic energy
	 * @param time [s after start of calculation]
	 * @return eps in [m^2/s^3]
	 */
	public abstract double get_eps(Zone zn[],double time);
	
	/**
	 * Returns the turbulence intensity
	 * @param time [s after start of calculation]
	 * @return u in [m/s]
	 */
	public abstract double get_intensity(Zone zn[],double time);
	
	/**
	 * Returns  the specific turbulence kinetic energy
	 * @param time [s after start of calculation]
	 * @return k in [m^2/s^2]
	 */
	public abstract double get_k(Zone zn[],double time);
	
	
	/**
	 * Returns  the integral length scale 
	 * @param time [s after start of calculation]
	 * @return l in [m]
	 */
	public abstract double get_turbulenceIntegralLengthScale(Zone zn[],double time);
	
	
	/**
	 * Method to update the differentials (e.g. d_k/dt and d_eps/dt)  
	 * @param zn
	 * @param time
	 */
	public abstract void update(Zone zn[], double time);
	
	public abstract void initialize(Zone zn[], double time);
	
	//Methode wird benötigt, um bei einer DVA mit WWM Bargende die Turbulenz richtig berechnen zu können
	public abstract void set_k(double turbulence,int i); 
	
}
