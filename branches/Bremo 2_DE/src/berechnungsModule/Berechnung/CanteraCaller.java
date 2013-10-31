package berechnungsModule.Berechnung;

import java.util.concurrent.Callable;

import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;

public class CanteraCaller implements Callable{
	
	/**
	 * Objektreference on the C++ side */
	private final byte [] reference;	
	private final String [] namesOfAllSpecies;
	private final int nbrOfSpecies;
	private final int nbrOfZones;
	private Zone parallelZonesIN [], parallelZonesOUT [];
	private double delta_t;
	
	static {
		System.loadLibrary("CanteraCaller");
	}	
	
	//Public Methods
	
	public CanteraCaller(CasePara cp, int nbrOfZones){
		if(nbrOfZones<1){
			try {
				throw new BirdBrainedProgrammerException("The number of zones in CanteraCaller " +
						"must be at least 1");
			} catch (BirdBrainedProgrammerException e1) {
				e1.stopBremo();
			}		
		}	

		
		String inputFile=cp.get_FileNameToRead("canteraInputFileName");
		String thermoPhase=cp.get_FileNameToRead("canteraThermophase");
		
		sagMalWasEinfachSo();
		this.nbrOfZones=nbrOfZones;
		reference=getCPPObjectReference(inputFile,thermoPhase,nbrOfZones);	
		nbrOfSpecies=this.getNbrOfSpecies(reference);
		namesOfAllSpecies=new String [this.nbrOfSpecies];		
		for(int i=0;i<this.nbrOfSpecies;i++)
			namesOfAllSpecies[i]=this.getNameOfSpecie(reference, i);
	}
	
	public void set_parallelStartPoint(Zone zn [], double delta_t){
		this.delta_t=delta_t;
		this.parallelZonesIN=zn;
		this.parallelZonesOUT=new Zone [zn.length];
	}
	
	public Zone [] get_kineticZones(){
		return parallelZonesOUT;
	}

	public Object call() throws Exception {
		for(int i=0; i<parallelZonesIN.length;i++){
				double T_cut=555;
				double T_Freeze=1000;
				boolean calcKinetics=true;
				//this stops kinetics from beeing solved 
				//late during the expansion stroke
				//-->nothing happens anymore
//				if(parallelZonesIN[i].get_T_Max()>T_Freeze&&
//						parallelZonesIN[i].get_T()<T_Freeze)
//					calcKinetics=false;
				
				Zone dumpStupid[]=new Zone [1];
				dumpStupid[0]=parallelZonesIN[i];				
				if(parallelZonesIN[i].get_T()>T_cut && calcKinetics)
					dumpStupid=this.calcTimeStep(dumpStupid, delta_t);
				
				parallelZonesOUT[i]=dumpStupid[0];
		}
		return 1;
	}
	
	public void releaseCantera(){
		this.releaseCantera(reference);
	}
	
	public void test(){
		this.test(reference);
	}
	
	
	public void set_StatepVTX(double [] p_V_T_X,int i){
		if(p_V_T_X.length!=(this.nbrOfSpecies+3)){
			try {
				throw new BirdBrainedProgrammerException("The vector p_V_T_X must have a " +
						"length of nbrOfSpecies+3="+(this.nbrOfSpecies+3)+ 
						" But it has a length of: "+ p_V_T_X.length);
			} catch (BirdBrainedProgrammerException e1) {
				e1.stopBremo();
			}		
		}	
		this.checkNbrOfZone(i);
		this.setStatepVTX(reference,p_V_T_X,i);		
	}
	
	
	
	
	public Zone [] calcTimeStep(Zone [] zn,double delta_t){	
		//test(this.reference);
		if(zn.length!=this.nbrOfZones){
			try {
				throw new BirdBrainedProgrammerException("The number of zones in CanteraCaller " +
						"is not the number of zones given to the function!");
			} catch (BirdBrainedProgrammerException e1) {
				e1.stopBremo();
			}		
		}
		
		double []x, y; 
		double[] p_V_T_X=new double[nbrOfSpecies+3]; 
		double[] p_V_T_mi=new double[nbrOfSpecies+3]; 
		
//		for(int i=0;i<zn.length;i++)
//			System.out.println("Tzone_" +i+ ": " + zn[i].get_T());	
	
		for(int i=0;i<zn.length;i++){				
				p_V_T_X[0]=zn[i].get_p();
				p_V_T_X[1]=zn[i].get_V();
				p_V_T_X[2]=zn[i].get_T();				
				x=zn[i].get_MoleFractions();
				for(int k=0;k<x.length;k++) p_V_T_X[k+3]=x[k];
				this.setStatepVTX(reference,p_V_T_X,i);	
		}
		//double u0=this.get_u_mass_Zone(0);
		this.calcKinetics(this.reference, delta_t);	
		//double u1=this.get_u_mass_Zone(0);
		//System.out.println((u0-u1)/u0*100);
	
		double pMean=0;
//		for(int i=0;i<zn.length;i++){
//			pMean=pMean+getPressure(reference,i); //should be the same ;-)
//		}
//		pMean=pMean/nbrOfZones;
		//Zone znOUT[]=new Zone[zn.length];
		for(int i=0;i<zn.length;i++){			
			y=this.getMassFractions(this.reference,i);
			//correcting massfractions				
			for(int k=0;k<y.length;k++)	if(y[k]<0)y[k]=0; //mostly very very small but still negative!
			double sumX=0;
			for(int j=0;j<y.length;j++) sumX=sumX+y[j];				
			for(int k=0;k<y.length;k++) y[k]=y[k]/sumX;					 
//	
			//filling vector p_V_T_mi	
			pMean=getPressure(reference,i);
			p_V_T_mi[0]=pMean;
			p_V_T_mi[1]=this.getVolume(reference,i);
			p_V_T_mi[2]=this.getTemperature(reference,i);	//might be adjustet to pMean in the zone			
			for(int k=0;k<y.length;k++) p_V_T_mi[k+3]=y[k]*zn[i].get_m();	
			
			//znOUT[i]=new Zone(cp,p_V_T_mi , false, zn[i].getID());
			zn[i].set_p_V_T_mi(p_V_T_mi);
		}
		
//		double T_CC=this.get_T(0);
//		double T_zn=zn[0].get_T();
//		double del_T=(T_CC-T_zn)/T_zn*100;	
//		
//		double u_cc=this.get_u_mass_Zone(0);
//		double u_zn=zn[0].get_ggZone().get_u_mass(zn[0].get_T());
//		double del_u=(u_cc-u_zn)/u_cc*100;
//		
//		double p_cc=this.get_p(0);
//		double p_zn=this.get_p();
//		double del_p=(p_cc-p_zn)/p_cc*100;
//		
//
//		System.out.println("T: " + del_T + " p: " + del_p + " u: "+ del_u);
		
		return zn;				
	}		
		
	public Zone [] calcTimeStepVec(Zone [] zn,double delta_t){	
			//test(this.reference);
			if(zn.length!=this.nbrOfZones){
				try {
					throw new BirdBrainedProgrammerException("The number of zones in CanteraCaller " +
							"is not the number of zones given to the function!");
				} catch (BirdBrainedProgrammerException e1) {
					e1.stopBremo();
				}		
			}
			
			double []x; 
			double[] p_V_T_X=new double[nbrOfZones*(nbrOfSpecies+3)]; 
			double[] p_V_T_Y_out=new double[nbrOfZones*(nbrOfSpecies+3)]; 
		
			for(int znIDX=0;znIDX<zn.length;znIDX++){			
				p_V_T_X[znIDX*(nbrOfSpecies+3)+0]=zn[znIDX].get_p();
				p_V_T_X[znIDX*(nbrOfSpecies+3)+1]=zn[znIDX].get_V();
				p_V_T_X[znIDX*(nbrOfSpecies+3)+2]=zn[znIDX].get_T();				
				x=zn[znIDX].get_MoleFractions();
				for(int k=0;k<x.length;k++) p_V_T_X[znIDX*(nbrOfSpecies+3)+k+3]=x[k];					
			}
			//IN: P_V_T_molefractions Out: P_V_T_massfractions
			p_V_T_Y_out=this.calcKineticsVec(this.reference,p_V_T_X, delta_t);
	//		
			double pMean=0;
	//		for(int i=0;i<zn.length;i++){
	//			pMean=pMean+getPressure(reference,i); //should be the same ;-)
	//		}
	//		pMean=pMean/nbrOfZones;
			
			for(int znIDX=0;znIDX<zn.length;znIDX++){	
				double p_V_T_mi[]=new double [nbrOfSpecies+3];
				double [] y =new double [nbrOfSpecies];
				p_V_T_mi[0]=p_V_T_Y_out[znIDX*(nbrOfSpecies+3)+0];
				p_V_T_mi[1]= p_V_T_Y_out[znIDX*(nbrOfSpecies+3)+1];
				p_V_T_mi[2]= p_V_T_Y_out[znIDX*(nbrOfSpecies+3)+2];			
				for(int k=0;k<nbrOfSpecies;k++) y[k]=p_V_T_Y_out[znIDX*(nbrOfSpecies+3)+k+3];			
				
				//correcting molefractions				
				for(int k=0;k<y.length;k++)	if(y[k]<0)y[k]=0; //mostly very very small but still negative!
				double sumX=0;
				for(int j=0;j<y.length;j++) sumX=sumX+y[j];				
				for(int k=0;k<y.length;k++) y[k]=y[k]/sumX;			
				for(int k=0;k<y.length;k++) p_V_T_mi[k+3]=y[k]*zn[znIDX].get_m();	
				
				zn[znIDX].set_p_V_T_mi(p_V_T_mi);
			}
			return zn;				
		}

	public void sprich(){
		sagMalWas(reference);		
	}
	
	public int get_NbrOfSpecies(){
		return this.nbrOfSpecies;
	}
	
	public double get_T(int zone){
		this.checkNbrOfZone(zone);
		return this.getTemperature(reference,zone);
	}
	
	public double get_p(int zone){
		this.checkNbrOfZone(zone);
		return this.getPressure(reference,zone);
	}
	
	public double get_p(){
		double pMean=0;
		for(int i=0;i<nbrOfZones;i++){
			pMean=pMean+getPressure(reference,i); //should be the same ;-)
		}
		pMean=pMean/nbrOfZones;
		return pMean;
	}
	
	/**
	 * 
	 * @param i
	 * @return Molarweight of Specie with Index i in kg/mol
	 */
	public double get_M_Specie(int i){
		this.checkIndexOfSpecie(i);
		return this.getMolarweightOfSpecie(reference,i)/1000;
	}
	
	/**
	 * 
	 * @return Molarweight in kg/mol
	 */
	public double get_M_Zone(int zone){
		this.checkNbrOfZone(zone);
		return this.getMolarweightOfSpecie(reference,zone)/1000;
	}

	public double get_h_mass_Zone(int zone){
		this.checkNbrOfZone(zone);
		return this.getEnthalpyMass(reference, zone);
	}
	
	/**
	 * 
	 * @return molar enthalpy in J/mol
	 */
	public double get_h_mol_Zone(int zone){
		this.checkNbrOfZone(zone);
		return this.getEnthalpyMole(reference,zone)/1000;
	}
	
	public double get_u_mass_Zone(int zone){
		this.checkNbrOfZone(zone);
		return this.getIntEnergyMass(reference, zone);
	}
	
	/**
	 * 
	 * @return molar enthalpy in J/mol
	 */
	public double get_u_mol_Zone(int zone){
		this.checkNbrOfZone(zone);
		return this.getIntEnergyMole(reference,zone)/1000;
	}
	
	
	public double get_dQ_vol_Zone(int zone){
		this.checkNbrOfZone(zone);
		return this.getHeatReleaseRateVolOfZone(reference,zone);
	}	
		
	public double get_dQ_mass_Zone(int zone){
		this.checkNbrOfZone(zone);
		return this.getHeatReleaseRateMassOfZone(reference,zone);
	}
	
	public double [] get_moleFractions(int zone){
		this.checkNbrOfZone(zone);
		return this.getMoleFractions(reference,zone);
	}
	
	public double [] get_massFractions(int zone){
		this.checkNbrOfZone(zone);
		return this.getMassFractions(reference,zone);
	}
	
	public String get_getNameOfSpecie(int i){	
		this.checkIndexOfSpecie(i);
		return this.namesOfAllSpecies[ i];
	}
	
	public String [] get_getNamesOfAllSpecies(){
		return this.namesOfAllSpecies;
	}
	
	private void checkNbrOfZone(int i){
		if(i>=(this.nbrOfZones)){
			try {
				throw new BirdBrainedProgrammerException(" There is no zone with index "+ i+
						". Psossible values are: 0 < i < "+ this.nbrOfZones);
			} catch (BirdBrainedProgrammerException e1) {
				e1.stopBremo();
			}		
		}			
	}
	
	private void checkIndexOfSpecie(int i){
		if(i>=(this.nbrOfSpecies)){
			try {
				throw new BirdBrainedProgrammerException(" There is no specie with index "+ i+
						". Psossible values are: 0 < i < "+ this.nbrOfSpecies);
			} catch (BirdBrainedProgrammerException e1) {
				e1.stopBremo();
			}		
		}			
	}
	
	//Private Native Methods
	
	private native byte[] getCPPObjectReference(String inputFile, String thermoPhase, int nbrOfZones);	
	private native void test(byte[] reference);		
	
	private native int getNbrOfSpecies(byte[] reference);
	
	//TODO Implement me	 
	private native double getTemperature(byte[] reference, int nbrOfZone);
	
	private native double getPressure(byte[] reference, int nbrOfZone);
	
	//TODO Implement me	 
	private native double getVolume(byte[] reference, int nbrOfZone);	
	
	/**	
	 * @param reference
	 * @return enthalpy in J/kg
	 */
	private native double getEnthalpyMass(byte[] reference,int i);
	
	/**	 * 
	 * @param reference
	 * @return enthalpy in J/kmol
	 */
	private native double getEnthalpyMole(byte[] reference,int i);
	
	/**	
	 * @param reference
	 * @return internal Energy in J/kg
	 */
	private native double getIntEnergyMass(byte[] reference,int i);
	
	/**	 * 
	 * @param reference
	 * @return internal energy in J/kmol
	 */
	private native double getIntEnergyMole(byte[] reference,int i);
	
	private native double getMolarweightOfSpecie(byte[] reference,int i);
	
	private native double getMolarweight(byte[] reference, int zone);
	
	private native double getHeatReleaseRateVolOfZone(byte[] reference, int nbrOfZone);
	
	private native double getHeatReleaseRateMassOfZone(byte[] reference, int nbrOfZone);	

	private native double [] getMoleFractions(byte[] reference, int nbrOfZone);		
	
	private native double [] getMassFractions(byte[] reference, int nbrOfZone);
	
	private native String 	getNameOfSpecie(byte[] reference,int i);

	private native void calcKinetics(byte[] reference, double delta_t);
	
	private native double[] calcKineticsVec(byte[] reference,double[] pVTX, double delta_t);

	private native void setStatepVTX(byte[] reference, double[] pVTX,int nbrOfZone);
	
	private native void releaseCantera(byte[] reference);
	
	private native void sagMalWas(byte[] reference);
	
	private native void sagMalWasEinfachSo();	
	
}
