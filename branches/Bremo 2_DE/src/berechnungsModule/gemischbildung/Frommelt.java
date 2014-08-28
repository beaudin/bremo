package berechnungsModule.gemischbildung;

import io.KraftstoffrateFileReader;
import io.TauFileReader;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;

public class Frommelt extends Einspritzung {

	public static final String FLAG = "Frommelt";
	private double mKrst_verdampft=0;
	double dm_krst_dampf, mKrst_tmp=0, boidummy, eoidummy, m_Krst_dampf_eoi;	
	boolean DVAerstdurchlauf = true;
	private Kraftstoff_Eigenschaften krstProp;
	private boolean isDVA = false ;
	int laenge;
	
	protected Frommelt(CasePara cp, int index) {
		super(cp, index);

		String vergleichsKraftstoff=CP.get_vergleichsKrstVerdampfung(index);
		krstProp =new Kraftstoff_Eigenschaften(vergleichsKraftstoff);
		
		// create FileReader
		super.kr_reader = new KraftstoffrateFileReader(cp, index);
		super.tau_reader = new TauFileReader(cp,index);
		
		// initial/boundary conditions
		if(super.IS_LWA_EINSPRITZUNG){// checks if injection is during the charge cycle
			super.mKrst_dampf.addValue(CP.get_Einlassschluss(), super.mKrst);
			super.mKrst_dampf.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, super.mKrst);
			super.mKrst_dampf.addValue(CP.get_Auslassoeffnet()-
					CP.SYS.WRITE_INTERVAL_SEC,super.mKrst);
			super.mKrst_dampf.addValue(CP.get_Auslassoeffnet(), 0);
		}
		else{
			super.mKrst_dampf.addValue(CP.get_Einlassschluss(), 0);
			if(super.EINSPRITZUNG_VOR_DVA){
				super.mKrst_dampf.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, super.mKrst);
			}
			else{
				super.mKrst_dampf.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, 0);
			}
			super.mKrst_dampf.addValue(CP.get_Auslassoeffnet(), 0);
		}
		
		// dummy values. saves writing time
		boidummy = super.boi+CP.SYS.DAUER_ASP_SEC;
		eoidummy = super.eoi+CP.SYS.DAUER_ASP_SEC;
	}
	
	private double get_cp_mass_fl(double T){
		return krstProp.get_cpl(T); 
	}

	@Override
	public double get_kineticEnergyFlux(double time, double pCyl) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double get_dQ_krstDampf(double time, Zone zn) {
		this.berechneIntegraleGroessen(time, zn);
		double dm=this.get_diff_mKrst_dampf(time, zn);
		double  Tkrstfl=this.get_T_fuel_liq();
		double dQ=0;
		if(dm>0){
			dQ=dm*krstProp.get_L(Tkrstfl);
			// Vereinfachung zu Krass -->Aufheizung des Krst wird erstmal nicht Beruecksichtigt
//			+dm*this.get_cp_mass_fl(Tkrstfl)*(get_Tkrst_dampf(time,zn)-get_T_Krst_fl());
		}
		return dQ; 	
	}

	@Override
	/**
	 * model: fuel evaporates due to a characteristic evaporation time
	 */
	public double get_diff_mKrst_dampf(double time, Zone zn) {
	
		// distinguish between charge and duty cycle
		if(super.IS_LWA_EINSPRITZUNG){
			// calculate evaporation only if there is fuel to evaporate
			if(time>boidummy){

				// get mass from external file or get a constant ratio
				mKrst_tmp = super.get_Mass(time);
				
				if(mKrst_tmp > super.mKrst){
					mKrst_tmp = super.mKrst;
				}
				
				// Frommelt equation
				dm_krst_dampf = (mKrst_tmp-super.mKrst_dampf.getValue(time))/super.get_Tau(time);
				if(dm_krst_dampf > 0){
					return dm_krst_dampf;
				}
				else{
					return 0;
				}
			}
			else{
				return 0;
			}
		}
		else{
			/*
			 *  Calculate evaporation only if there is fuel to evaporate.
			 *  Calculation within this block only during duty cycle!
			 */
			if(time>super.boi && time<CP.SYS.RECHNUNGS_ENDE_DVA_SEC){

				// Due to the non chronological calculation
				super.mKrst_dampf.setLastSearchIndex(laenge);

				// get mass from external file or get a constant ratio
				mKrst_tmp = super.get_Mass(time);
				
				if(mKrst_tmp > super.mKrst){
					mKrst_tmp = super.mKrst;
				}
				
				// Frommelt equation
				dm_krst_dampf = (mKrst_tmp-super.mKrst_dampf.getValue(time))/super.get_Tau(time);
				if(dm_krst_dampf > 0){
					return dm_krst_dampf;
				}
				else{
					return 0;
				}
			}
			else{
				return 0;
			}
		}
	}
	
	@Override
	public void berechneIntegraleGroessen(double time, Zone zn) {	
		if(super.IS_LWA_EINSPRITZUNG){
			// calculate evaporation only if there is fuel to evaporate
			if(time>boidummy){

				// get mass from external file or get a constant ratio
				mKrst_tmp = super.get_Mass(time);
				
				if(mKrst_tmp > super.mKrst){
					mKrst_tmp = super.mKrst;
				}
				
				// add up evaporated fuel
				mKrst_verdampft = super.mKrst_dampf.getValue(time-CP.SYS.WRITE_INTERVAL_SEC)+
						this.get_diff_mKrst_dampf(time-CP.SYS.WRITE_INTERVAL_SEC, zn)
						*CP.SYS.WRITE_INTERVAL_SEC;
				
				if(mKrst_verdampft>super.mKrst){
					mKrst_verdampft = super.mKrst;
				}
				super.mKrst_dampf.addValue(time, mKrst_verdampft);
				super.mKrst_fluessig.addValue(time, mKrst_tmp-mKrst_verdampft);
			}
			else{
				if(time<=CP.SYS.RECHNUNGS_ENDE_DVA_SEC){
					// calculate new starting index to search the Arrays
					if(DVAerstdurchlauf == true && time==CP.SYS.RECHNUNGS_BEGINN_DVA_SEC){
						double[] array = super.mKrst_dampf.getZeitachse();
						laenge = array.length-1;
						
						if(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC == super.boi){
							// value added to successfully interpolate
							super.mKrst_dampf.addValueByForce(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, super.mKrst);
						}
				
						this.isDVA = true;
						DVAerstdurchlauf = false;
					}
					
					// Due to the non chronological calculation
					super.mKrst_dampf.setLastSearchIndex(laenge);
					super.mKrst_dampf.addValueByForce(time, super.mKrst);
					super.mKrst_fluessig.addValueByForce(time, 0);
					
				}else{
					super.mKrst_dampf.addValue(time, 0);
					super.mKrst_fluessig.addValue(time, 0);	
				}
			}
		}
		else{
			/*
			 *  calculate evaporation only if there is fuel to evaporate. 
			 *  Calculation within this block only during duty cycle!
			 */
			if(time>=super.boi && time<CP.SYS.RECHNUNGS_ENDE_DVA_SEC){	
				
				// calculate new starting index to search the Arrays
				if(DVAerstdurchlauf == true){
					double[] array = super.mKrst_dampf.getZeitachse();
					laenge = array.length-3;
					
					if(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC == super.boi){
						// value added to successfully interpolate
						super.mKrst_dampf.addValue(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC, 0);
					}
			
					this.isDVA = true;
					DVAerstdurchlauf = false;
				}
				
				// Due to the non chronological calculation
				super.mKrst_dampf.setLastSearchIndex(laenge);
								
				// get mass from external file or get a constant ratio
				mKrst_tmp = super.get_Mass(time);
				
				if(mKrst_tmp > super.mKrst){
					mKrst_tmp = super.mKrst;
				}
				
				// add up evaporated fuel
				mKrst_verdampft = super.mKrst_dampf.getValue(time-CP.SYS.WRITE_INTERVAL_SEC)+
						this.get_diff_mKrst_dampf(time-CP.SYS.WRITE_INTERVAL_SEC, zn)
						*CP.SYS.WRITE_INTERVAL_SEC;
			
				if(mKrst_verdampft > super.mKrst){
					mKrst_verdampft = super.mKrst;
				}
				super.mKrst_dampf.addValue(time, mKrst_verdampft);
				super.mKrst_fluessig.addValue(time, mKrst_tmp - mKrst_verdampft);
				
			}
			else{
				if(this.isDVA && time>=CP.SYS.RECHNUNGS_ENDE_DVA_SEC){
					super.mKrst_dampf.addValueByForce(time, super.mKrst);
					super.mKrst_fluessig.addValueByForce(time, 0);
				}
				super.mKrst_dampf.addValue(time, 0);
				super.mKrst_fluessig.addValue(time, 0);
			}
		}
	}

	@Override
	public double get_Tkrst_dampf(double time, Zone zn) {
		return this.get_T_fuel_liq();
	}

}
