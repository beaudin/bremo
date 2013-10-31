package KivaPostProcessing;

import io.FileWriter_txt;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.Callable;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import kalorik.spezies.SpeziesFabrik;

import bremo.parameter.CasePara;
import bremoExceptions.MiscException;
import bremoExceptions.StopBremoException;

public class KivaPostProcessingThread implements Callable{
	
	private KivaPostProcessReader kpr;
	private GasGemisch ggVec [];
	private final CasePara CP;
	private String path;
	//Vectors holding the cellIdx corresponding to each lambda bin
	private Vector<Integer> vecsIdx[]; 
	//array holding the mass [kg] corresponding to each lambda bin
	double mass[];
	//array holding the volume [m^3] corresponding to each lambda bin
	double vol[];
	//lambda values used for binning
	double lambdas[];
	//The lamba value if all cells are llumped together
	double lambdaTot;
	//nbr of bins
	private int  nbrOfBins=200;	
	// degree of heterogeneity
	private double doh;
	
	
	private int nbrOfInjHoles;

	public KivaPostProcessingThread(String path, CasePara cp, int nbrOfInjHoles) {
		this.CP=cp;		//only used to get the speziesFabrik
		this.path=path;			
		this.vecsIdx=new Vector[nbrOfBins]; //Vectors holding the cellIdx
		for(int i=0;i<vecsIdx.length;i++)vecsIdx[i]=new Vector<Integer>();
		//array holding the mass [kg] corresponding to each lambda bin
		mass=new double [nbrOfBins];
		//array holding the volume [m^3] corresponding to each lambda bin
		vol=new double [nbrOfBins];		
		lambdas=new double[nbrOfBins];//lambda values used for binning		
		double lamMax=5.5, lamMin=1.5;
		double interVall=(lamMax-lamMin)/nbrOfBins;
		for(int i=0;i<nbrOfBins;i++)lambdas[i]=lamMin+i*interVall;
		this.nbrOfInjHoles=nbrOfInjHoles;
	} 
	
	public Object call() throws StopBremoException {
		this.postProcess();
		return null;		
	}
	
	
	public void postProcess(){		
			try{
				throw new MiscException("Ich hab die Ausgabe der Masse " +
						"und des Volumesn geaendert --> checken ob das noch passt!");
			}catch(MiscException me){
			//	me.stopBremo();
			}			
		kpr=new KivaPostProcessReader(path);
		ggVec=new GasGemisch[kpr.get_nbrOfCells()];
		SpeziesFabrik sf=CP.SPEZIES_FABRIK;
		String speciesNames[]=kpr.get_SpeciesNames();
		//creating a species-object for each cell!
		for(int cellIdx=0;cellIdx<ggVec.length;cellIdx++){	
			Hashtable<Spezies, Double> mf=new Hashtable<Spezies, Double>();
			for(int specIdx=0;specIdx<speciesNames.length;specIdx++){
				String specName=speciesNames[specIdx];
				Spezies spec=null;
				if(specName.equalsIgnoreCase("c14H30"))
					spec=sf.get_SpeciesByName("nc7h16"); //convert c14h30 to nc7h16		
				else
					spec=sf.get_SpeciesByName(specName);
				
				if(!specName.equalsIgnoreCase("c14H30")){
					if(!spec.get_name().equalsIgnoreCase(specName)){
						try{
							throw new MiscException("Something went wrong with the KivaResultsFile and the species factory");
						}catch(MiscException me){
							me.stopBremo();
						}					
					}
				}
				//will not sum up to 1 if Kiva integrates over other species than Bremo/Cantera
				if(sf.isToIntegrate(spec))
						mf.put(spec, kpr.get_massFrac(specName, cellIdx));			
			}
			String name="gg"+cellIdx;
			ggVec[cellIdx]=new GasGemisch(name);
			ggVec[cellIdx].set_Gasmischung_massenBruch(mf);
		}
		
		//creating a species object that is a mixture of all cells
		Hashtable<Spezies, Double> mf=new Hashtable<Spezies, Double>();	
		double mTot=kpr.get_mTot();

		for(int cellIdx=0;cellIdx<ggVec.length;cellIdx++)
			mf.put(ggVec[cellIdx], kpr.get_m(cellIdx)/mTot);			
		
		GasGemisch allCellSpec=new GasGemisch("allCellSpec");
		allCellSpec.set_Gasmischung_massenBruch(mf);
		lambdaTot=allCellSpec.get_lambda();
		
		//Degree of heterogeneity DOH
		doh=0; 
		for(int iterCell=0; iterCell<ggVec.length;iterCell++){
			double a=ggVec[iterCell].get_lambda()-lambdaTot;
			doh=doh+a*a*kpr.get_m(iterCell);
		}		
		doh=Math.sqrt(doh/kpr.get_mTot())/lambdaTot;
		

		for(int cellIdx=0;cellIdx<ggVec.length;cellIdx++){
			if(ggVec[cellIdx].get_lambda()<lambdas[0]){
				vecsIdx[0].add(cellIdx);
				vol[0]=vol[0]+kpr.get_V(cellIdx)*this.nbrOfInjHoles;
				mass[0]=mass[0]+kpr.get_m(cellIdx)*this.nbrOfInjHoles;	
			}
			for(int i=0;i<lambdas.length-1;i++){
				if(ggVec[cellIdx].get_lambda()>=lambdas[i]&&
						ggVec[cellIdx].get_lambda()<lambdas[i+1]){
					vecsIdx[i].add(cellIdx);
					vol[i]=vol[i]+kpr.get_V(cellIdx)*this.nbrOfInjHoles;
					mass[i]=mass[i]+kpr.get_m(cellIdx)*this.nbrOfInjHoles;
				}
			}
			if(ggVec[cellIdx].get_lambda()>=lambdas[lambdas.length-1]){
				vecsIdx[nbrOfBins-1].add(cellIdx);
				vol[nbrOfBins-1]=vol[nbrOfBins-1]+kpr.get_V(cellIdx)*this.nbrOfInjHoles;
				mass[nbrOfBins-1]=mass[nbrOfBins-1]+kpr.get_m(cellIdx)*this.nbrOfInjHoles;
			}
		}
		double chkNbrOfCells=0;
		for(int i=0;i<vecsIdx.length;i++)chkNbrOfCells=chkNbrOfCells+vecsIdx[i].size();
		if(chkNbrOfCells!=kpr.get_nbrOfCells()){
			try{
				throw new MiscException("The nbr of cells is wrong");
			}catch(MiscException me){
				me.stopBremo();
			}
		}
		double sumVol=0;
		for(int i=0;i<vol.length;i++)sumVol=vol[i]+sumVol;
		double volTot=kpr.get_V_Tot();
		volTot=volTot*this.nbrOfInjHoles;
		double chkVol=Math.abs((sumVol-volTot)/volTot);
		double sumMass=0;
		for(int i=0;i<mass.length;i++)sumMass=mass[i]+sumMass;
		sumMass=sumMass/this.nbrOfInjHoles;
		double chkMass=Math.abs((sumMass-kpr.get_mTot())/kpr.get_mTot());
		if(chkVol>1e-3||chkMass>1e-3){
			try{
				throw new MiscException("Either volumes or masses do not sum up to the total value. "+
			"The values are: chkVol="+chkVol+" chkMass="+chkMass);
			}catch(MiscException me){
				me.stopBremo();
			}
		}
		
		
		
//		int nbrOfZones[]={1,2,5,10,20,30,40,50,100};
		int nbrOfZones[]={10};
		for(int i=0;i<nbrOfZones.length;i++){
			System.out.println("Creating out put for " +nbrOfZones[i]+" zones!");
			createBremoMultiZoneInput(nbrOfZones[i]);
		}
		System.out.println("done");			
	}
	
	private void createBremoMultiZoneInput(int nbrOfZones){
		//finding min lambda and max lambda to define lambda classes based on the actual composition
		double lamMax=Double.NEGATIVE_INFINITY;
		double lamMin=Double.POSITIVE_INFINITY;
		for(int cellIdx=0;cellIdx<ggVec.length;cellIdx++){		
			double lam=ggVec[cellIdx].get_lambda();
				if(lam>lamMax)
					lamMax=lam;
				if(lam<lamMin)
					lamMin=lam;	
		}		
		Vector<Integer> vecsIdx[]; 
		vecsIdx=new Vector[nbrOfZones]; //Vectors holding the cellIdx
		for(int i=0;i<vecsIdx.length;i++)vecsIdx[i]=new Vector<Integer>();
		
		//finding the indices of zones according to lambda classes
		double lam4Zones[]=new double[nbrOfZones];//lambda values used for distribution to the 0D-zones	
		double interVal=(lamMax-lamMin)/nbrOfZones;
		for(int i=0;i<nbrOfZones;i++)lam4Zones[i]=lamMin+i*interVal;
		for(int cellIdx=0;cellIdx<ggVec.length;cellIdx++){
			double lam=ggVec[cellIdx].get_lambda();
			for(int i=0;i<lam4Zones.length-1;i++){				
				if(lam>=lam4Zones[i]&&lam<lam4Zones[i+1]){
					vecsIdx[i].add(cellIdx);					
				}
			}
			if(lam>=lam4Zones[lam4Zones.length-1]){
				vecsIdx[nbrOfZones-1].add(cellIdx);					
			}
		}
		
		//creating the zones	
		//Header  for output
		int headerSize=ggVec[0].get_speziesMassenBruecheDetail().size()+6;			
		String header[]=new String [headerSize];
		header[0]="Lambda [-]";
		header[1]="LHV [MJ/kg]";
		header[2]="mTot [kg]";
		header[3]="p [Pa]";
		header[4]="V [m^3]";
		header[5]="T [K]";
		
		double p=kpr.get_p();
		double lamLHVmTotpVTmi[][]=new double [nbrOfZones][headerSize];
		
		int writeIdx=0;
		for(int zIdx=0;zIdx<nbrOfZones;zIdx++){
			if(vecsIdx[zIdx].size()>0){//chk if there are zones for that lambda class
				//needed to only output zones that really exist! Zones without mass must not be outputted!
				
				//calculating total volume and total mass for that lambda class
				double mTotZone=0, volTotZone=0, U_Tot=0;
				for(int i=0;i<vecsIdx[zIdx].size();i++){
					int cellIdx=vecsIdx[zIdx].get(i);
					mTotZone=mTotZone+kpr.get_m(cellIdx);
					volTotZone=volTotZone+kpr.get_V(cellIdx);
					//total internal energy
					double T=kpr.get_T(cellIdx);
					U_Tot=U_Tot+ggVec[cellIdx].get_u_mass(T)*kpr.get_m(cellIdx);
				}	
				Hashtable<Spezies, Double> mf=new Hashtable<Spezies, Double>();
				for(int i=0;i<vecsIdx[zIdx].size();i++)
					mf.put(ggVec[vecsIdx[zIdx].get(i)], kpr.get_m(vecsIdx[zIdx].get(i))/mTotZone);
				GasGemisch gg=new GasGemisch("z"+zIdx);
				gg.set_Gasmischung_massenBruch(mf);
				//T after all cells have been combined
				double Tu=gg.get_T4u_mass(U_Tot/mTotZone);
				double Ts=p*volTotZone/mTotZone/gg.get_R();
				double chk=Math.abs((Ts-Tu)/Ts);
				if(chk>7.555e-3){
					try{
						throw new MiscException("The calculated mixing temperature of  zone nbr "+ zIdx+" is not reasonable! " +
								"chk: "+chk);
					}catch(MiscException me){
						me.log_Warning();
						//me.stopBremo();
					}
				}			
				lamLHVmTotpVTmi[writeIdx][0]=gg.get_lambda();
				lamLHVmTotpVTmi[writeIdx][1]=gg.get_Hu_mass()*1e-6;
				lamLHVmTotpVTmi[writeIdx][2]=mTotZone*nbrOfInjHoles;
				lamLHVmTotpVTmi[writeIdx][3]=p;
				lamLHVmTotpVTmi[writeIdx][4]=volTotZone*nbrOfInjHoles;
				lamLHVmTotpVTmi[writeIdx][5]=Ts;
				Hashtable<Spezies, Double> ht=gg.get_speziesMassenBruecheDetailToIntegrate();			
				
				int nbrOfSpecies=CP.SPEZIES_FABRIK.get_nbrOfSpecies();
				for(int idx=0;idx<nbrOfSpecies;idx++){
					Spezies spec=CP.SPEZIES_FABRIK.get_Spez(idx);
					lamLHVmTotpVTmi[writeIdx][idx+6]=ht.get(spec)*mTotZone*nbrOfInjHoles;	
					header[idx+6]=spec.get_name();
				}
				//increase only if the zone really exists!
				writeIdx=writeIdx+1;
//				int i=4;
//				while( e.hasMoreElements()){
//					Spezies spec=e.nextElement();
//					pVTY[zIdx][i]=ht.get(spec);	
//					header[i]=spec.get_name();
//					i++;
//				}	
			}
		}		
		//delete rows with zeros	
		if(writeIdx!=nbrOfZones){
			double [][]temp=new double[writeIdx][];
			for(int i=0;i<writeIdx;i++)temp[i]=lamLHVmTotpVTmi[i];
			lamLHVmTotpVTmi=null;
			lamLHVmTotpVTmi=temp;			
		}
		
		double chkNbrOfCells=0;
		for(int i=0;i<vecsIdx.length;i++)chkNbrOfCells=chkNbrOfCells+vecsIdx[i].size();
		if(chkNbrOfCells!=kpr.get_nbrOfCells()){
			try{
				throw new MiscException("The nbr of cells is wrong");
			}catch(MiscException me){
				me.stopBremo();
			}
		}
		double volCHK=0;
		for(int i=0;i<lamLHVmTotpVTmi.length;i++)volCHK+=lamLHVmTotpVTmi[i][4]/nbrOfInjHoles;	
		volCHK=Math.abs((volCHK-kpr.get_V_Tot())/kpr.get_V_Tot());
		if(volCHK>1e-3){
			try{
				throw new MiscException("The sum over the volume of all zones is wrong: volCHK="+volCHK);
			}catch(MiscException me){
				me.stopBremo();
			}
		}
		double mCHK=0;
		for(int i=0;i<lamLHVmTotpVTmi.length;i++)
			for(int ii=6;ii<lamLHVmTotpVTmi[0].length;ii++)
				mCHK=mCHK+lamLHVmTotpVTmi[i][ii]/nbrOfInjHoles;
		mCHK=Math.abs((mCHK-kpr.get_mTot())/kpr.get_mTot());
		if(mCHK>1e-3){
			try{
				throw new MiscException("The sum over the mass of all zones is wrong: mCHK="+mCHK);
			}catch(MiscException me){
				me.stopBremo();
			}
		}
		//output the data
//		Hashtable<Spezies, Double> ht1=ggVec[0].get_speziesMassenBruecheDetailToIntegrate();
//		Enumeration<Spezies> e1=ht1.keys();
//		int idx=4;
//		while( e1.hasMoreElements()){
//			Spezies spec=e1.nextElement();
//							
//			idx=idx+1;
//		}	
		String fs=File.separator;
		int a=this.path.lastIndexOf(fs);
		String preFix="";
		if(kpr.get_mLiqFuelTot()>0)preFix="LIQ_FUEL_PRESENT_";
		String path1=this.path.substring(0, a)+"\\"+kpr.get_crankAngle()+"_"+preFix+
				"zoneInput_"+ nbrOfZones+".txt";	
		FileWriter_txt fw1=new FileWriter_txt(path1);
		fw1.writeTextLineToFile(header, false);
		fw1.writeMatrixToFile(lamLHVmTotpVTmi, true);		
	}
	
	
	/**
	 * Returns the lambda values that have been used for binning
	 * @return
	 */
	public double[] get_lambdaClasses(){
		return this.lambdas;
	}
	
	/**
	 * Returns an array holding the masses corresponding to the lambda classes 
	 * defined by {@link #get_lambdaClasses()} 
	 * @return
	 */
	public double[] get_mBin(){
		return this.mass;
	}
	/**
	 * Returns an array holding the volumes corresponding to the lambda classes 
	 * defined by {@link #get_lambdaClasses()} 
	 * @return
	 */
	public double[] get_volBin(){
		return this.vol;
	}
	
	/**
	 * Returns the crank angle specified in the KivaResultsFile
	 * @return
	 */
	public double get_CA(){
		return this.kpr.get_crankAngle();
	}
	
	/**
	 * Returns the degree of heterogeneity
	 * @return doh
	 */
	public double get_DOH(){
		return this.doh;
	}
	
	/**
	 * Returns the lambda value for all cells lumbed together
	 * @return
	 */
	public double get_LambdaTot(){
		return this.lambdaTot;
	}


}
