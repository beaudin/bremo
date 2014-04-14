package berechnungsModule.blowby;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;

public abstract class BlowBy {
	CasePara cp;
	
	protected BlowBy(CasePara CP){
		this.cp = CP;
	}
	
	/**
	 * Gibt den Leckagemassenstrom in [kg/s] zurück. Dabei gilt:
	 * positiv (+) = aus dem Zylinder heraus,
	 * negativ (-) = in den Zylinder hinein.
	 * @param time
	 * @param zonen_IN
	 * @return dmL [kg/s]
	 */
	public abstract double get_mLeckage(double time, Zone[] zonen_IN);
	public abstract boolean is_Berechnet();
}
