package berechnungsModule.Berechnung;



import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import kalorik.GasMixture;
import kalorik.Spezies;
import io.HeatFluxFileReader;
import matLib.DiefferetialEqationSys;
import matLib.RungeKutta;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;
import bremoExceptions.StopBremoException;


public abstract class APR_Cantera extends APR {

	private CanteraCaller cc=null;
	private CanteraCaller ccPEQ=null;
	private CanteraCaller ccPEQs[]=null;
	protected double dQburn[], dQburnU[],dQburnHf[];
	protected double dQburnVol[];
	protected double dQburnSum, dQburnSumU, dQburnSumHf;
	protected double deltaV=5.5;
	private double T_StartKinetics;
	//protected final int nbrOfZones;
	private final double lhvCorr;
	private HeatFluxFileReader dQbReader=null;


	protected APR_Cantera(CasePara cp) {
		super(cp);	
		lhvCorr=cp.get_lhvCorr();
	}
	
	public void releaseCantera(){
		for(int i=0;i<ccPEQs.length;i++)ccPEQs[i].releaseCantera();
	}

 
	@Override
	public Zone[] calc_dQburn(Zone[] zonen) {
		T_StartKinetics=CP.get_T_StartKinetics();
		boolean calcKinetics=true;
		boolean compareToKiva=false;
		boolean ct=false;
		boolean parallel=true;
		if(calcKinetics){
			if(ct)
				return this.calc_dQburnPressureCT(zonen);
			else if(parallel)
				return this.calc_dQburnPressureEQParallel(zonen);
			else 
				return this.calc_dQburnPressureEQ(zonen);
			 }
		else if(compareToKiva){
			return this.read_dQburnPressureEQ(zonen);			
		}else
			return zonen;		
	}
	
	private Zone[] calc_dQburnPressureCT(Zone[] zonen) {
		if(cc==null)
			cc=new CanteraCaller(super.CP,zonen.length);

		//Das muss sein sonst funktioniert es nicht!
		Zone[] znOUT=new Zone[zonen.length];

		double VGes0=0;		
		for(int i=0;i<zonen.length;i++)
			VGes0=VGes0+zonen[i].get_V();	
		boolean calcKinetics=false;
		for(int i=0;i<zonen.length;i++){
			if(zonen[i].get_T()>T_StartKinetics) calcKinetics=true;
		}
	
		double T0[]=new double [zonen.length];
		double U0[]=new double [zonen.length];
		double hf0[]=new double [zonen.length];
		for(int i=0;i<zonen.length;i++){
			dQburn[i]=0;
			dQburnVol[i]=0;		
			T0[i]=zonen[i].get_T();
			U0[i]=zonen[i].get_m()*zonen[i].get_gasMixtureZone().get_u_mass(T0[i]);	
			hf0[i]=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
		}

		if(calcKinetics){	
			System.out.println("CalcKinetics");
			znOUT=cc.calcTimeStep(zonen, CP.SYS.WRITE_INTERVAL_SEC);				
		}else{
			znOUT=zonen;
		}	

		for(int i=0;i<zonen.length;i++){
			dQburn[i]=-1*cc.get_dQ_mass_Zone(i)*znOUT[i].get_m();
			dQburnVol[i]=-1*cc.get_dQ_vol_Zone(i)*znOUT[i].get_V();
			double u1=zonen[i].get_gasMixtureZone().get_u_mass(T0[i]);
			double U1=u1*zonen[i].get_m();
			double deltaQburn=(U0[i]-U1);			
			dQburnU[i]=lhvCorr*deltaQburn/CP.SYS.WRITE_INTERVAL_SEC; 	
			double hf1=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
			dQburnHf[i]=(hf0[i]-hf1)*zonen[i].get_m()/CP.SYS.WRITE_INTERVAL_SEC;  
		}

		dQburnSum=0;
		for(int k=0;k<dQburn.length;k++) dQburnSum+=dQburn[k];
		
		dQburnSumU=0;
		for(int k=0;k<dQburnU.length;k++) dQburnSumU+=dQburnU[k];
		
		dQburnSumU=0;
		for(int k=0;k<dQburnU.length;k++) dQburnSumU+=dQburnU[k];
		
		dQburnSumHf=0;
		for(int k=0;k<dQburnHf.length;k++) dQburnSumHf+=dQburnHf[k];

		double VGes1=0;
		for(int i=0;i<zonen.length;i++)
			VGes1=VGes1+znOUT[i].get_V();
		deltaV=VGes0-VGes1;
		return znOUT;
		
	}
	
	private Zone[] read_dQburnPressureEQ(Zone[] zonen) {
		if(ccPEQ==null)
			ccPEQ=new CanteraCaller(super.CP,1);
		
		if(dQbReader==null){			
			dQbReader=new HeatFluxFileReader(CP,
					CP.get_ColumnToRead("column_dQb"), 
					CP.get_FileToRead("dQbFile"));			
		}
		

		dQburn=new double [zonen.length];	

		double VGes0=0;		
		for(int i=0;i<zonen.length;i++)
			VGes0=VGes0+zonen[i].get_V();		

		for(int i=0;i<zonen.length;i++){
			dQburn[i]=5E-55;
			dQburnVol[i]=5E-55;
			double T0=zonen[i].get_T();
			double u0=zonen[i].get_gasMixtureZone().get_u_mass(T0);
			double U0=zonen[i].get_m()*u0;
			double hf0=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
			
			if((zonen[i].get_T()>T_StartKinetics&&i==zonen.length-1)||(zonen[i].get_T()>T_StartKinetics&&i==zonen.length-2)){	//55
				//System.out.println("CalcKinetics");
				Zone zT[]=new Zone[1];
				zT[0]=zonen[i];
				//Heat release is calculated as the sum of heat release before and after the kinetic timestep
				//the HR calculated based on the rate of change of species concentration at the beginning 
				//and the end of each time step thus this value might over predict the HR 
				dQburn[i]=-0.5*ccPEQ.get_dQ_mass_Zone(0)*zT[0].get_m();			
				dQburnVol[i]=-0.5*ccPEQ.get_dQ_vol_Zone(0)*zT[0].get_V();				

				zT=ccPEQ.calcTimeStep(zT, CP.SYS.WRITE_INTERVAL_SEC);
				
				zonen[i]=zT[0];
				
				//3 Versions of HeatRelease --  they all roughly give  the same result 		
				dQburn[i]=dQburn[i]-0.5*ccPEQ.get_dQ_mass_Zone(0)*zT[0].get_m();
				dQburn[i]=dQburn[i];
				dQburnVol[i]=dQburn[i]-0.5*ccPEQ.get_dQ_vol_Zone(0)*zT[0].get_V();
				
				double deltaQburn=this.dQbReader.get_Value(CP.get_time());
				deltaQburn=0.5*deltaQburn*CP.SYS.WRITE_INTERVAL_SEC;				
							
				dQburnU[i]=deltaQburn/CP.SYS.WRITE_INTERVAL_SEC; 				
				//Heat release due to change of composition respectively change of heat of formation
				double hf1=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
				dQburnHf[i]=(hf0-hf1)*zonen[i].get_m()/CP.SYS.WRITE_INTERVAL_SEC;  
				dQburnHf[i]=dQburnHf[i];
				//Adjsuting for the different heating values of nc7h16 and ic8h18
				//lower internal energy in respect to lower LHV		
				double u1=zonen[i].get_gasMixtureZone().get_u_mass(T0);
				double uTweaked=u1+deltaQburn/zonen[i].get_m();	
						
				double TNew=zonen[i].get_gasMixtureZone().get_T4u_mass(uTweaked);			
				//System.out.println("(T-TNew)/T: "+ ((zonen[i].get_T()-TNew)/zonen[i].get_T()*100));
				//pressure in respect to new temperature
				double pNew=zonen[i].get_m()*zonen[i].get_gasMixtureZone().get_R()*TNew/zonen[i].get_V();
				//System.out.println("(p-pNew)/p: "+((zonen[i].get_p()-pNew)/zonen[i].get_p()) );
				
				//set up vector p_V_T_mi				
				double p_V_T_mi[]= zonen[i].get_p_V_T_mi();
				p_V_T_mi[0]=pNew;
				p_V_T_mi[2]=TNew;
				
				//set new state
				zonen[i].set_p_V_T_mi(p_V_T_mi);				
			}		
		}
		
		double pMax=0,pMin=Double.MAX_VALUE;
		for(int i=0;i<zonen.length;i++){
			if(zonen[i].get_p()>pMax) pMax=zonen[i].get_p();
			if(zonen[i].get_p()<pMin) pMin=zonen[i].get_p();	
		}
		dQburnSum=0;
		for(int k=0;k<dQburn.length;k++) dQburnSum+=dQburn[k];	
		
		dQburnSumU=0;
		for(int k=0;k<dQburnU.length;k++) dQburnSumU+=dQburnU[k];
		
		dQburnSumHf=0;
		for(int k=0;k<dQburnHf.length;k++) dQburnSumHf+=dQburnHf[k];

		//Equilibrate pressure		
		zonen=this.equilibrate(zonen, pMin, pMax);
		//zonen=this.pEquilibrator.equilibrate(zonen, pMin, pMax);
		double VGes1=0;
		for(int i=0;i<zonen.length;i++)
			VGes1=VGes1+zonen[i].get_V();
		deltaV=VGes0-VGes1;
		return zonen;
	}	
	
	

	private Zone[] calc_dQburnPressureEQ(Zone[] zonen) {
		if(ccPEQ==null)
			ccPEQ=new CanteraCaller(super.CP,1);
			//ccPEQ=new CanteraCaller("ERCmechLHVAdapted.cti","gas",1);


		dQburn=new double [zonen.length];	

		double VGes0=0;		
		for(int i=0;i<zonen.length;i++)
			VGes0=VGes0+zonen[i].get_V();		

		for(int i=0;i<zonen.length;i++){
			dQburn[i]=5E-5;
			dQburnVol[i]=5E-5;
			double T0=zonen[i].get_T();
			double u0=zonen[i].get_gasMixtureZone().get_u_mass(T0);
			double U0=zonen[i].get_m()*u0;
			double hf0=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
			if(zonen[i].get_T()>T_StartKinetics){	//55
				//System.out.println("CalcKinetics");
				Zone zT[]=new Zone[1];
				zT[0]=zonen[i];
				//Heat release is calculated as the sum of heat release before and after the kinetic timestep
				//the HR calculated based on the rate of change of species concentration at the beginning 
				//and the end of each time step thus this value might over predict the HR 
				dQburn[i]=-0.5*ccPEQ.get_dQ_mass_Zone(0)*zT[0].get_m();			
				dQburnVol[i]=-0.5*ccPEQ.get_dQ_vol_Zone(0)*zT[0].get_V();				

				zT=ccPEQ.calcTimeStep(zT, CP.SYS.WRITE_INTERVAL_SEC);
				
				zonen[i]=zT[0];
				
				//3 Versions of HeatRelease --  they all roughly give  the same result 		
				dQburn[i]=dQburn[i]-0.5*ccPEQ.get_dQ_mass_Zone(0)*zT[0].get_m();
				dQburn[i]=dQburn[i]*lhvCorr;
				dQburnVol[i]=dQburn[i]-0.5*ccPEQ.get_dQ_vol_Zone(0)*zT[0].get_V();
				//For isothermal (T before and after combustion is the same) combustion dQ=dU !	
				double u1=zonen[i].get_gasMixtureZone().get_u_mass(T0);
				double U1=u1*zonen[i].get_m();
				double deltaQburn=(U0-U1);			
				dQburnU[i]=lhvCorr*deltaQburn/CP.SYS.WRITE_INTERVAL_SEC; 				
				//Heat release due to change of composition respectively change of heat of formation
				double hf1=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
				dQburnHf[i]=(hf0-hf1)*zonen[i].get_m()/CP.SYS.WRITE_INTERVAL_SEC;  
				dQburnHf[i]=lhvCorr*dQburnHf[i];
				//Adjsuting for the different heating values of nc7h16 and ic8h18
				//lower internal energy in respect to lower LHV								
				double uTweaked=u1+lhvCorr*deltaQburn/zonen[i].get_m();				

				double TNew=zonen[i].get_gasMixtureZone().get_T4u_mass(uTweaked);			
			
				//pressure in respect to new temperature
				double pNew=zonen[i].get_m()*zonen[i].get_gasMixtureZone().get_R()*TNew/zonen[i].get_V();
				//System.out.println("(p-pNew)/p: "+((zonen[i].get_p()-pNew)/zonen[i].get_p()) );
				
				//set up vector p_V_T_mi				
				double p_V_T_mi[]= zonen[i].get_p_V_T_mi();
				p_V_T_mi[0]=pNew;
				p_V_T_mi[2]=TNew;
				
				//set new state
				zonen[i].set_p_V_T_mi(p_V_T_mi);				
			}		
		}
		
		double pMax=0,pMin=Double.MAX_VALUE;
		for(int i=0;i<zonen.length;i++){
			if(zonen[i].get_p()>pMax) pMax=zonen[i].get_p();
			if(zonen[i].get_p()<pMin) pMin=zonen[i].get_p();	
		}
		dQburnSum=0;
		for(int k=0;k<dQburn.length;k++) dQburnSum+=dQburn[k];	
		
		dQburnSumU=0;
		for(int k=0;k<dQburnU.length;k++) dQburnSumU+=dQburnU[k];
		
		dQburnSumHf=0;
		for(int k=0;k<dQburnHf.length;k++) dQburnSumHf+=dQburnHf[k];

		//Equilibrate pressure		
		zonen=this.equilibrate(zonen, pMin, pMax);
		//zonen=this.pEquilibrator.equilibrate(zonen, pMin, pMax);
		double VGes1=0;
		for(int i=0;i<zonen.length;i++)
			VGes1=VGes1+zonen[i].get_V();
		deltaV=VGes0-VGes1;
		return zonen;
	}	
	
	private Zone[] calc_dQburnPressureEQParallel(Zone[] zonen) {
		//this can be any number smaller than the number of zones
		int nbrOfCanteraCallers = zonen.length;
		if(nbrOfCanteraCallers>zonen.length)
			nbrOfCanteraCallers = zonen.length;
			
		if(ccPEQs==null){			
			ccPEQs=new CanteraCaller[nbrOfCanteraCallers];
			for(int i=0; i<nbrOfCanteraCallers;i++)
				ccPEQs[i]=new CanteraCaller(super.CP,1);
		}
		//storing the state before the chemical reaction --> needed to calculate dQb
		double [] T0=new double[zonen.length];
		double [] u0=new double[zonen.length];
		double [] U0=new double[zonen.length];
		double [] hf0=new double[zonen.length];
		for(int i=0;i<zonen.length;i++){
			T0[i]=zonen[i].get_T();
			u0[i]=zonen[i].get_gasMixtureZone().get_u_mass(T0[i]);
			U0[i]=zonen[i].get_m()*u0[i];
			hf0[i]=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
		}

		

		//distribute the zones to the threads and setting the start point
		int nbrOfZonesPerCanteraCaller=zonen.length/nbrOfCanteraCallers;		
		for(int i=0;i<nbrOfCanteraCallers-1;i++){
			Zone zT[]=new Zone[nbrOfZonesPerCanteraCaller];
			for(int x=0;x<nbrOfZonesPerCanteraCaller;x++){
				zT[x]=zonen[i*nbrOfZonesPerCanteraCaller+x];				
			}	
			ccPEQs[i].set_parallelStartPoint(zT, CP.SYS.WRITE_INTERVAL_SEC);
		}
		//the rest comes to the last canteraCaller
		int ub=zonen.length-(nbrOfCanteraCallers-1)*nbrOfZonesPerCanteraCaller;
		Zone zT2[]=new Zone[ub];
		for(int i=0;i<ub;i++){
			zT2[i]=zonen[(nbrOfCanteraCallers-1)*nbrOfZonesPerCanteraCaller+i];
		}
		ccPEQs[nbrOfCanteraCallers-1].set_parallelStartPoint(zT2, CP.SYS.WRITE_INTERVAL_SEC);

		//parallel solving of kinetics
		List<Future<CanteraCaller>> futuresList = new ArrayList<Future<CanteraCaller>>();
//		int nbrOfThreads=Runtime.getRuntime().availableProcessors();
//		ExecutorService eservice = Executors.newFixedThreadPool(nbrOfThreads);
		ExecutorService eservice = Executors.newCachedThreadPool();

		for(int index = 0; index < ccPEQs.length; index++)
			futuresList.add(eservice.submit(ccPEQs[index]));

		Object taskResult;
		for(Future<CanteraCaller> future:futuresList) {
			try {
				taskResult = future.get();                        
			}
			catch (InterruptedException e) {
				try{
					throw new BirdBrainedProgrammerException("While multiprocessing something went wrong");
				}catch(BirdBrainedProgrammerException bbpe){
						bbpe.stopBremo();						
				}				
			}
			catch (ExecutionException e) {
				try{
					throw new BirdBrainedProgrammerException("While multiprocessing something went wrong");
				}catch(BirdBrainedProgrammerException bbpe){
					bbpe.stopBremo();						
				}	
			}
		}
		//if this is not called there will be too many threads alive --> out of memory
		eservice.shutdownNow();
		futuresList.removeAll(futuresList);

		//redistributing the zones	
		for(int i=0;i<ccPEQs.length-1;i++){
			Zone [] zT=ccPEQs[i].get_kineticZones();
			for(int u=0;u<zT.length;u++){
				zonen[i*nbrOfZonesPerCanteraCaller+u]=zT[u];
			}	     		
		}
		int uub=ccPEQs[ccPEQs.length-1].get_kineticZones().length;
		for(int u=0;u<uub;u++){
			zonen[(ccPEQs.length-1)*nbrOfZonesPerCanteraCaller+u]=
				ccPEQs[ccPEQs.length-1].get_kineticZones()[u];
		}

		//crash! I don't know why...
//		for(int i=0;i<ccPEQs.length;i++){
//			ccPEQs[i].releaseCantera();
//		}


		dQburn=new double [zonen.length];	

		double VGes0=0;		
		for(int i=0;i<zonen.length;i++)
			VGes0=VGes0+zonen[i].get_V();	
		
		for(int i=0;i<zonen.length;i++){
			dQburn[i]=5E-5;
			dQburnVol[i]=5E-5;
			//2 Versions of HeatRelease --  they all roughly give  the same result 
			double u1=zonen[i].get_gasMixtureZone().get_u_mass(T0[i]);
			double U1=u1*zonen[i].get_m();
			double deltaQburn=(U0[i]-U1);			
			dQburnU[i]=lhvCorr*deltaQburn/CP.SYS.WRITE_INTERVAL_SEC; 				
			//Heat release due to change of composition respectively change of heat of formation
			double hf1=zonen[i].get_gasMixtureZone().get_delta_hf298_mol()/zonen[i].get_gasMixtureZone().get_M();
			dQburnHf[i]=(hf0[i]-hf1)*zonen[i].get_m()/CP.SYS.WRITE_INTERVAL_SEC;  
			dQburnHf[i]=lhvCorr*dQburnHf[i];
			//Adjsuting for the different heating values of nc7h16 and ic8h18
			//lower internal energy in respect to lower LHV								
			double uTweaked=u1+lhvCorr*deltaQburn/zonen[i].get_m();				
		
			double TNew=zonen[i].get_gasMixtureZone().get_T4u_mass(uTweaked);		
		
			//pressure in respect to new temperature
			double pNew=zonen[i].get_m()*zonen[i].get_gasMixtureZone().get_R()*TNew/zonen[i].get_V();

			//set up vector p_V_T_mi				
			double p_V_T_mi[]= zonen[i].get_p_V_T_mi();
			p_V_T_mi[0]=pNew;
			p_V_T_mi[2]=TNew;			

			//set new state
			zonen[i].set_p_V_T_mi(p_V_T_mi);				

		}

		double pMax=0,pMin=Double.MAX_VALUE;
		for(int i=0;i<zonen.length;i++){
			if(zonen[i].get_p()>pMax) pMax=zonen[i].get_p();
			if(zonen[i].get_p()<pMin) pMin=zonen[i].get_p();	
		}
		dQburnSum=0;
		for(int k=0;k<dQburn.length;k++) dQburnSum+=dQburn[k];	

		dQburnSumU=0;
		for(int k=0;k<dQburnU.length;k++) dQburnSumU+=dQburnU[k];

		dQburnSumHf=0;
		for(int k=0;k<dQburnHf.length;k++) dQburnSumHf+=dQburnHf[k];

		//Equilibrate pressure		
		zonen=this.equilibrate(zonen, pMin, pMax);
		//zonen=this.pEquilibrator.equilibrate(zonen, pMin, pMax);
		double VGes1=0;
		for(int i=0;i<zonen.length;i++)
			VGes1=VGes1+zonen[i].get_V();
		deltaV=VGes0-VGes1;
		return zonen;
	}	
	

	/**
	 * Uses an iteration scheme to equalize the pressure in the zones.
	 * The method is static for debugging reasons
	 * @param z
	 * @param pMin minimum pressure of all zones
	 * @param pMax maximum pressure of all zones
	 * @return zones with equal pressure
	 */
	public static Zone [] equilibrate(Zone [] z, double pMin, double pMax){
		double pPrecision=1e-6;

		if(Math.abs(pMax-pMin)>pPrecision){
			//System.out.println("pEquil");
			double p[]=new double [z.length];
			double V[]=new double[z.length];
			double T[]=new double[z.length];

			double Vges=0;
			for(int i=0;i<z.length;i++){
				p[i]=z[i].get_p();
				T[i]=z[i].get_T();
				V[i]=z[i].get_V();
				Vges=Vges+V[i];
			}		

			double VgesNeu=0,pEquilOld=0, residium;
			double V_old[]=new double [z.length];
			double V_new[]=new double [z.length];	
			double dV[]=new double [z.length];	
			int idx=0;
			boolean stop=false;
			do{			
				double pV=0;
				for(int i=0;i<z.length;i++){
					pV=pV+p[i]*V[i];	
					V_old[i]=V[i];
				}			
				double pEquil=pV/Vges;

				for(int i=0;i<z.length;i++)V_new[i]=V_old[i]*p[i]/pEquil;	

				for(int i=0;i<z.length;i++) dV[i]=V_new[i]-V_old[i];			

				for(int i=0;i<z.length;i++){				
					T[i]=T[i]-p[i]*dV[i]/z[i].get_m()/z[i].get_gasMixtureZone().get_cv_mass(T[i]);
					p[i]=z[i].get_m()*z[i].get_gasMixtureZone().get_R()*T[i]/V_new[i];	
					V[i]=V_new[i];	
				}
				
				VgesNeu=0;
				for(int i=0;i<z.length;i++) VgesNeu=VgesNeu+V[i];	
				residium=Math.abs(pEquil-pEquilOld);
				double deltaV=Math.abs((VgesNeu-Vges)/Vges);
				pEquilOld=pEquil;
				idx+=1;
				if(deltaV<1e-6 && residium<pPrecision )
					stop=true;
			}while(idx<=555 &&  stop==false ); 

			System.out.println("nbrIters: "+ idx +" Residium: "+ residium);

			if(idx>=555){
				try{
					throw new MiscException("The iteration to equalize the pressure " +
							" in all zones did not converge");
				}catch(MiscException me){
					me.log_Warning();					
				}
			}			
			
			double pMean=0;
			for(int i=0;i<z.length;i++)pMean=pMean+p[i]/z.length;
			for(int i=0;i<z.length;i++){
				double p_V_T_mi[]=z[i].get_p_V_T_mi();
				p_V_T_mi[0]=pMean;
				p_V_T_mi[1]=V[i];
				p_V_T_mi[2]=T[i]; //T will be slightly changed in the Zone according to the eq. of state
				z[i].set_p_V_T_mi(p_V_T_mi);				
			}
		}
		return z;
	}

}
