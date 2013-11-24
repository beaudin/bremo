package berechnungsModule.mixtureFormation;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class PortFuelInjection extends Injection {
	
	public static final String FLAG="homogeneous";
	public static final String FLAG2="portFuelInjection";

	public PortFuelInjection(CasePara cp,int index) {
		super(cp,index);
		//eoi ist eigentlich auf NaN gesetzt....
		if(super.eoi>CP.SYS.SIMULATION_START_SEC){
			try{
				throw new ParameterFileWrongInputException("Bei dem gewaehlten Einspritzmodell" +
						" (\"" +FLAG +"\" bzw. \""+ FLAG2+ "\") " +
						"fuer die " + index+ "te Einspritzung liegt das Einspritzende " +
						"nach dem Rechnungsbeginn. \n" +
						"Ist die Annahme eines vollstaendig homogenen Gemsichs dann zulaessig?");
			}catch(ParameterFileWrongInputException e){
				e.log_Warning();				
			}
		}		
	}


	public double get_dQ_krstDampf(double time) {		
		return 0;
	}


	public double get_mKrst_verdampft(double time) {
		return super.mKrst;
	}


	public double get_m_fuelLiquid(double time) {		
		return 0;
	}
	
	public double get_mKrst_DampfUndFluessig(double time){
		return mKrst;
	}



	public double get_diff_mKrst_dampf(double time) {	
		return 0;
	}


	@Override
	public void calculateIntegralvalues(double time, Zone zn) {			
		super.mKrst_dampf.addValue(time,super.mKrst );
		super.mKrst_fluessig.addValue(time, super.mKrst);		
	}


	@Override
	public double get_dQ_fuelVapor(double time, Zone zn) {
		return 0;
	}


	@Override
	public double get_diff_mFuel_vapor(double time, Zone zn) {
		return 0;
	}


	@Override
	public double get_T_fuelVapor(double time, Zone zn) {
		return zn.get_T();
	}


	@Override
	public double get_kineticEnergyFlux(double time, double pCyl) {		
		return 0;
	}

}
