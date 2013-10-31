package berechnungsModule.gemischbildung;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremoExceptions.ParameterFileWrongInputException;

public class SaugrohrEinspritzungHomogen extends Einspritzung {
	
	public static final String FLAG="Homogen";
	public static final String FLAG2="SRE";

	public SaugrohrEinspritzungHomogen(CasePara cp,int index) {
		super(cp,index);
		//eoi ist eigentlich auf NaN gesetzt....
		if(super.eoi>CP.SYS.RECHNUNGS_BEGINN_DVA_SEC){
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


	public double get_mKrst_fluessig(double time) {		
		return 0;
	}
	
	public double get_mKrst_DampfUndFluessig(double time){
		return mKrst;
	}



	public double get_diff_mKrst_dampf(double time) {	
		return 0;
	}


	@Override
	public void berechneIntegraleGroessen(double time, Zone zn) {			
		super.mKrst_dampf.addValue(time,super.mKrst );
		super.mKrst_fluessig.addValue(time, super.mKrst);		
	}


	@Override
	public double get_dQ_krstDampf(double time, Zone zn) {
		return 0;
	}


	@Override
	public double get_diff_mKrst_dampf(double time, Zone zn) {
		return 0;
	}


	@Override
	public double get_Tkrst_dampf(double time, Zone zn) {
		return zn.get_T();
	}


	@Override
	public double get_kineticEnergyFlux(double time, double pCyl) {		
		return 0;
	}

}
