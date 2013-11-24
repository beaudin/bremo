package berechnungsModule.Berechnung;
import io.TurbulenceFileReader;
import java.util.Hashtable;

import kalorik.GasMixture;
import kalorik.Spezies;
import misc.VectorBuffer;
import berechnungsModule.mixtureFormation.Injection;
import berechnungsModule.mixtureFormation.MasterInjection;
import berechnungsModule.mixtureFormation.Packet_Hiroyasu;
import berechnungsModule.mixtureFormation.Spray;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_reciprocatingPiston;
import berechnungsModule.turbulence.TurbulenceModel;
import berechnungsModule.wallHeatTransfer.WallHeatTransfer;
import bremo.parameter.CasePara;
import bremo.parameter.IndicatorData;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;

public class APR_CanteraMultiZoneRCCI extends APR_Cantera {		
	private Motor motor;
	private WallHeatTransfer whtModel;
	private MasterInjection masterInjection;
	private Zone [] initialZones;
	private TurbulenceModel turb;

	private IndicatorData indiD;

	private VectorBuffer T_buffer;
	private VectorBuffer dQb_buffer;
	private VectorBuffer dQw_buffer;
	private VectorBuffer dmb_buffer;
	private double fortschritt=0;
	private double QbMax, dQw, Qb=0,Qw=0,mTot, whtfMult; 	
	private final int axPackets, radPackets, nbrOfPackets, indexOfMainInj;
	double sumdma=0;
	private boolean compareToExp;
	private double redFac=0.85;
	private int nbrOfZones;




	protected APR_CanteraMultiZoneRCCI(CasePara cp) {
		super(cp);
		motor = CP.MOTOR;
		whtModel=CP.WALL_HEAT_TRANSFER_MODEL;
		turb=CP.TURB_FACTORY.get_TurbulenceModel();
		masterInjection=CP.MASTER_INJECTION;		
		indexOfMainInj=CP.get_indexOfMainInj();
		axPackets=((Spray)masterInjection.get_injection(indexOfMainInj)).get_nbrOfAxPackets();
		radPackets=((Spray)masterInjection.get_injection(indexOfMainInj)).get_nbrOfRadPackets();
		nbrOfPackets=axPackets*radPackets;		
		this.nbrOfZones=nbrOfPackets+1;			
		checkInjections(masterInjection); 	
		//different ways to calculate the hest release rate
		super.dQburn=new double [nbrOfZones];	
		super.dQburnVol=new double [nbrOfZones];
		super.dQburnU=new double [nbrOfZones];
		super.dQburnHf=new double [nbrOfZones];
		//If you want to compare your simulation against exp. data you must specify the exp. indicator diagram
		if(CP.compareToExp()){
			indiD=new IndicatorData(cp);
			compareToExp=true;
		}	
		//buffering some results 
		T_buffer = new VectorBuffer(cp);
		dQb_buffer = new VectorBuffer(cp);	
		dQw_buffer = new VectorBuffer(cp);	
		dmb_buffer = new VectorBuffer(cp);	

		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////		
		initialZones=new Zone[nbrOfZones];
		whtfMult=this.CP.get_whtfMult();
		initialZones=initializeZones();
		turb.initialize(initialZones,CP.SYS.SIMULATION_START_SEC);
	}	


	private Zone[] initializeZones(){
		double mINIT, p_init, V_init,T_init,mD,mG,VD,VG,R;
		double [] molFrac=new double [CP.SPECIES_FACTROY.get_nbrOfSpecies()];

		//initial conditions given in the InputFile
		//Molefractions		
		double nO2= CP.get_iniMoleFrac_O2();
		double nN2=CP.get_iniMoleFrac_N2(); 
		double nCO2=CP.get_iniMoleFrac_CO2();
		double nH2O=CP.get_iniMoleFrac_H2O();
		double niC8H18=CP.get_iniMoleFrac_iC8H18();
		double nC7H16=CP.get_iniMoleFrac_nC7H16();
		double nH2= CP.get_iniMoleFrac_H2();
		double nH= CP.get_iniMoleFrac_H();
		double nCO=CP.get_iniMoleFrac_CO();
		double nOH= CP.get_iniMoleFrac_OH();
		double nO= CP.get_iniMoleFrac_O();
		double nNO= CP.get_iniMoleFrac_NO();
		double nN= CP.get_iniMoleFrac_N();


		p_init=CP.get_p_ini();
		T_init=CP.get_T_ini();

		//creating the initial gas mixture
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_nC7H16())]=nC7H16;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_O2())]=nO2;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_N2())]=nN2;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_CO2())]=nCO2;	
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_H2O())]=nH2O;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_iC8H18())]=niC8H18;	
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_CO())]=nCO;	
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_H2())]=nH2;	
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_H())]=nH;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_OH())]=nOH;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_O())]=nO;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_NO())]=nNO;
		molFrac[CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_N())]=nN;

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
		double sum=0;
		for(int j=0;j<molFrac.length;j++)sum=sum+molFrac[j];
		if(Math.abs(sum-1)>1e-7){
			try{
				throw new ParameterFileWrongInputException("Initial mole fractions do NOT sum up to one!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();			
			}				
		}
		for(int j=0;j<molFrac.length;j++){			
			ht.put(CP.SPECIES_FACTROY.get_Spez(j), molFrac[j]/sum);
		}			
		GasMixture spec=new GasMixture("CanteraBurned_");
		spec.set_Gasmixture_moleFracs(ht);
		//initial Volume
		V_init=motor.get_V(CP.SYS.SIMULATION_START_SEC); 
		R=spec.get_R(); 
		//initial mass	
		mINIT=p_init*V_init/T_init/R; 

		//determination of total energy	
		double Q1=spec.get_LHV_mass()*mINIT;

		double Q2=0;
		int nbrOfInj=CP.MASTER_INJECTION.get_allInjections().length;
		for(int i=0;i<nbrOfInj;i++){
			double m=CP.MASTER_INJECTION.get_injection(i).get_mFuel_Cycle();
			double lhv=CP.MASTER_INJECTION.get_injection(i).get_fuel().get_LHV_mass();
			Q2=Q2+m*lhv;
		}
		this.QbMax=Q1+Q2;		


		//total mass (gasoline mixture + all injections)
		mTot=mINIT+CP.MASTER_INJECTION.get_mFuel_Sum_Cycle(); //0.559350393*1e-3; aus KIVA-Ergebnissen


		//Masse in allen Dieselzonen
		mD=(1+1e-6)*CP.SYS.MINIMUM_ZONE_MASS;
		double mDSum=mD*this.nbrOfPackets;
		mG=mINIT-mDSum;			
		VG=mG*R*T_init/p_init;	
		VD=mD*R*T_init/p_init;	

		double deltaV=V_init-VG-VD*this.nbrOfPackets;
		if(Math.abs(deltaV)>1e-15){
			try{
				throw new ParameterFileWrongInputException(" The volumes of the " +
						"zones do not sum up to the  initial volume!");
			} catch (ParameterFileWrongInputException e) {			
				e.stopBremo();			
			}				
		}

		Zone [] zn=new Zone[this.nbrOfZones];
		//gasoline
		zn[nbrOfZones-1]=new Zone(CP,p_init, VG, T_init, 
				mG,spec , false,nbrOfZones-1);
		//Diesel
		for(int i=0;i<nbrOfPackets;i++)		
			zn[i]=new Zone(CP,p_init, VD, T_init, 
					mD,spec , false,i);			
		return zn;
	}



	@Override
	public boolean initialiseBurntZone() {		
		return false;
	}


	private Zone[] entrainmentHiroyasu(double time, Zone[] zonen_IN){

		//get the main injection
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];

		int axPackets=((Spray) es).get_nbrOfAxPackets();
		int radPackets=((Spray) es).get_nbrOfRadPackets();				

		//Entrainment from gasoline zone
		double dmaTot=0;				
		int ax=0;
		int rad=0;
		do{						
			do{
				double dma=((Spray) es).get_dma_Packet(time, ax,rad );					
				//check if gasoline zone still conains enough mass
				if(zonen_IN[nbrOfZones-1].get_m()>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
						&& zonen_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){
					dmaTot=dmaTot+dma;
					zonen_IN[ax*radPackets+rad].set_dm_in(dma, 
							zonen_IN[nbrOfZones-1].get_T(), 
							zonen_IN[nbrOfZones-1].get_gasMixtureZone());
					try {
						zonen_IN[nbrOfZones-1].set_dm_out(dma);
					} catch (NegativeMassException e) {
						e.log_Warning("The gasoline zone is empty! Something went wrong");
						//should not happen!
					}								
				}
				ax++;
			}while(ax<axPackets);						
			ax=0;
			rad++;
		}while(rad<radPackets);				

		return zonen_IN;
	}


	private Zone[] entrainmentKrishnan(double time, Zone[] zonen_IN){
		//get the main injection
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];

		int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
		int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	


		double dmaTot=0;				
		int ax=0;
		int rad=0;
		do{						
			do{
				if(time>=es.get_BOI()){
					double mu=zonen_IN[nbrOfZones-1].get_m();
					double delta_t=time-es.get_BOI();
					double Y=delta_t/(es.get_EOI()-es.get_BOI());
					double t_Mix=((Spray) es).get_packet(0, 0).get_t_Mix();

					double dma=mu/(nbrOfRadPackets*nbrOfAxPackets*(1+Y)*t_Mix);		
					double a=-1*(ax+1.0)*(nbrOfRadPackets-rad)/(nbrOfRadPackets*nbrOfAxPackets);
					double f=Math.exp(a);
					//System.out.println(f);
					dma=dma*f;									
					dma=CP.get_C_Mix()*dma;		
					//check if gasoline zone still conains enough mass
					if(zonen_IN[nbrOfZones-1].get_m()>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
							&& zonen_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS
							&&((Spray) es).get_packet(ax, rad).get_lifetime(time)>0){
						dmaTot=dmaTot+dma;
						zonen_IN[ax*nbrOfRadPackets+rad].set_dm_in(dma, 
								zonen_IN[nbrOfZones-1].get_T(), 
								zonen_IN[nbrOfZones-1].get_gasMixtureZone());
						try {
							zonen_IN[nbrOfZones-1].set_dm_out(dma);
						} catch (NegativeMassException e) {
							e.log_Warning("The gasoline zone is empty! Something went wrong");
							//should not happen!
						}
					}
				}					
				ax++;
			}while(ax<nbrOfAxPackets);						
			ax=0;
			rad++;
		}while(rad<nbrOfRadPackets);		

		return zonen_IN;		
	}


	private double calc_dm_TI(Zone[] zn, int idxZone,int idxZone_1,double time){

		Zone z0=zn[idxZone];
		double V0=z0.get_V();
		double m0=z0.get_m();
		double rho_0=m0/V0;

		Zone z1=zn[idxZone_1];
		double m1=z1.get_m();
		double V1=z1.get_V();
		double rho_1=m1/V1;

		//turbulence length scale
		double ils=turb.get_turbulenceIntegralLengthScale(zn, time);

		//charachteristic length scale of the zone
		double C_cls=CP.get_C_MixRad();	
		double lc=C_cls*Math.pow(V0, (1d/3d));	
		double factor=Math.exp(-.1*(lc/ils-1.0)*(lc/ils-1.0));	
		//surface 
		double surf=Math.pow(V0, 2d/3d);		
		double ue=this.turb.get_intensity(zn, time);
		double C_Mix=CP.get_C_Mix();
		double dma=C_Mix*Math.sqrt(rho_1*rho_0)*ue*surf;
		dma=dma*factor;
		return dma;

	}

	private double turbD0=-5.55;
	/**
	 * Wie turbMix1 ausser das zur berechnung der inneren Massenstroeme der exponent der 
	 * e-Funktion mit dem Faktor 1/D/t moduliert wird. Ausserdem wird der tatsaechliche Radius 
	 * der Zonen verwendet!
	 * @param time
	 * @param zonen_IN
	 * @return
	 */
	private Zone[] turbMixApproach_2(double time, Zone[] zonen_IN){

		//get the main injection
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];

		if(time<=es.get_EOI()){
			zonen_IN=entrainmentHiroyasu(time,zonen_IN);
		}
		else{	
			//die if-Abfrage ist noch nciht getestet! 20130524
			if(zonen_IN[nbrOfZones-1].get_m()>CP.SYS.MINIMUM_ZONE_MASS){
				int axialPakete=((Spray) es).get_nbrOfAxPackets();
				int radialPakete=((Spray) es).get_nbrOfRadPackets();	
				//Entrainment from gasoline zone
				double k=turb.get_k(zonen_IN,time);
				double eps=this.turb.get_eps(zonen_IN,time);
				double tI=2.0/3.0*k/eps;			
				double c=CP.get_C_Mix();
				double dmaTot=c*zonen_IN[zonen_IN.length-1].get_m()/tI;	
				try {
					zonen_IN[nbrOfZones-1].set_dm_out(dmaTot);
				} catch (NegativeMassException e) {			
					e.printStackTrace();
				}

				//ratio between spray axis and spray rim --> dm_r=C*dmTot*exp(a*(R-r)^2
				//--> dm_0=C*dmTot*e(a*R^2) & dm_R=C*dmTot; 
				double cc=CP.get_C_MixRad();
				if(turbD0==-5.55)
					turbD0=k*k/eps;
				double turbD=k*k/eps;
				double t0=es.get_EOI()-es.get_BOI();
				int idxRmax=radialPakete-1;		
				double a=Math.log(cc)*turbD0*t0/idxRmax/idxRmax;
				double sum=0;	
				double mixTime=time-es.get_EOI();
				for(int ax =0;ax<axialPakete;ax++){
					for(int rad=0;rad<radialPakete;rad++){
						sum+=Math.exp(a*(idxRmax-rad)*(idxRmax-rad)/turbD/mixTime);	 
					}
				}
				double dmCHK=0;
				int ax=0;
				int rad=0;
				Zone z1=zonen_IN[nbrOfZones-1];	
				do{						
					do{
						Zone z0=zonen_IN[ax*radialPakete+rad];						
						double f=Math.exp(a*(idxRmax-rad)*(idxRmax-rad)/turbD/mixTime)/sum;			
						double dma=f*dmaTot;	
						z0.set_dm_in(dma, z1.get_T(), z1.get_gasMixtureZone());					
						ax++;
						dmCHK+=dma;
					}while(ax<axialPakete);						
					ax=0;
					rad++;
				}while(rad<radialPakete);
				double chk=(dmaTot-dmCHK)/dmaTot;
				if(Math.abs(chk)>1e-5){
					try{
						throw new BirdBrainedProgrammerException("F**K" +((dmaTot-dmCHK)/dmaTot));
					}catch(BirdBrainedProgrammerException bbpe){
						bbpe.stopBremo();
					}
				}
			}
		}		
		return zonen_IN;
	}



	private Zone[] turbMixApproach_1_dmFluxScheme_1(double time, Zone[] zones_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI())
			zones_IN=entrainmentHiroyasu(time,zones_IN);
		else{	
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	
			//Entrainment from gasoline zone in the outer packets
			double dmaTot=0;				
			int ax=0;
			int rad=0;	
			do{
				do{		
					int idxZ=ax*nbrOfRadPackets+rad;
					double dma=this.calc_dm_TI(zones_IN, idxZ,nbrOfZones-1, time);
					//check if gasoline zone still conains enough mass
					if(zones_IN[nbrOfZones-1].get_m()-CP.SYS.MINIMUM_ZONE_MASS 
							>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
							&& zones_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){						

						dmaTot=dmaTot+dma;					
						zones_IN[idxZ].set_dm_in(dma, 
								zones_IN[nbrOfZones-1].get_T(), 
								zones_IN[nbrOfZones-1].get_gasMixtureZone());
						try {
							zones_IN[nbrOfZones-1].set_dm_out(dma);
						} catch (NegativeMassException e) {
							e.log_Warning("The gasoline zone is empty! Something went wrong");
							//should not happen!
						}		
					}
					ax++;
				}while(ax<nbrOfAxPackets);	
				ax=0;
				rad++;
			}while(rad<nbrOfRadPackets);	
		}
		return zones_IN;
	}


	/**
	 * Entrainment cascades from the outer zones to the inner zones. The entrainment mass flux 
	 * is calculated based on turbulence intensity and the surface area of the inner zone.
	 * @param time
	 * @param zones_IN
	 * @return zones_IN 
	 */
	private Zone[] turbMixApproach_1_dmFluxScheme_2(double time, Zone[] zones_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI())
			zones_IN=entrainmentHiroyasu(time,zones_IN);
		else{	
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	
			//Entrainment from gasoline zone in the outer packets
			double dmaTot=0;				
			int ax=0;
			int rad=nbrOfRadPackets-1;				
			do{		
				int idxZ=ax*nbrOfRadPackets+rad;
				double dma=this.calc_dm_TI(zones_IN, idxZ,nbrOfZones-1, time);

				//check if gasoline zone still conains enough mass
				if(zones_IN[nbrOfZones-1].get_m()-CP.SYS.MINIMUM_ZONE_MASS 
						>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
						&& zones_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){						

					dmaTot=dmaTot+dma;					
					zones_IN[idxZ].set_dm_in(dma, 
							zones_IN[nbrOfZones-1].get_T(), 
							zones_IN[nbrOfZones-1].get_gasMixtureZone());
					try {
						zones_IN[nbrOfZones-1].set_dm_out(dma);
					} catch (NegativeMassException e) {
						e.log_Warning("The gasoline zone is empty! Something went wrong");
						//should not happen!
					}						

				}
				ax++;
			}while(ax<nbrOfAxPackets);	
			ax=-Integer.MAX_VALUE;
			rad=-Integer.MAX_VALUE;

			for (int a=0;a<nbrOfAxPackets;a++){			
				for(int r=0;r<nbrOfRadPackets-1; r++){
					int idxZ=a*nbrOfRadPackets+r;
					Zone z0=zones_IN[idxZ];
					int idxZ_1=a*nbrOfRadPackets+r+1;
					Zone z1=zones_IN[idxZ_1];
					double dm=this.calc_dm_TI(zones_IN, idxZ,idxZ_1, time);

					double f=redFac;
					if(z1.get_m()-CP.SYS.MINIMUM_ZONE_MASS<=dm*CP.SYS.WRITE_INTERVAL_SEC
							&&z1.get_m()>CP.SYS.MINIMUM_ZONE_MASS)						
						dm=f*(z1.get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;								
					if(z1.get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
						dm=0;
					z0.set_dm_in(dm,z1.get_T(), z1.get_gasMixtureZone());
					try {
						z1.set_dm_out(dm);
					} catch (NegativeMassException e) {
						e.log_Warning("Zone " + (a*nbrOfRadPackets+r+1) +
								" is empty! Something went wrong");
						//should not happen!
					}				
				}
			}		
		}
		return zones_IN;
	}
	
	private Zone[] turbMixApproach_1_dmFluxScheme_3(double time, Zone[] zones_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI()){
			zones_IN=entrainmentHiroyasu(time,zones_IN);
		}
		else{			
			//zonen_IN=entrainment(time,zonen_IN);
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	
			//Entrainment from gasoline zone in the outer packets
			double dmaTot=0;			
			int ax=0;
			int rad=nbrOfRadPackets-1;	
			do{
				int idxZ=ax*nbrOfRadPackets+rad;
				Zone z0=zones_IN[idxZ];
				Zone z1=zones_IN[nbrOfZones-1];				
				
				double dma=this.calc_dm_TI(zones_IN, idxZ,nbrOfZones-1, time);
				//check if gasoline zone still conains enough mass
				if(z1.get_m()-CP.SYS.MINIMUM_ZONE_MASS 
						>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
						&& z1.get_m()>=CP.SYS.MINIMUM_ZONE_MASS){

					double dm1=dma;
					double f=redFac;
					if(z0.get_m()-CP.SYS.MINIMUM_ZONE_MASS<=dma*CP.SYS.WRITE_INTERVAL_SEC
							&&z0.get_m()>CP.SYS.MINIMUM_ZONE_MASS)						
						dm1=f*(z0.get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;								
					if(z0.get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
						dm1=0;

					dma=Math.min(dma, dm1);					

					dmaTot=dmaTot+dma;	

					z0.set_dm_in(dma, z1.get_T(), z1.get_gasMixtureZone());
					try {
						z1.set_dm_out(dma);
					} catch (NegativeMassException e) {
						e.log_Warning("The gasoline zone is empty! Something went wrong");
						//should not happen!
					}

					z1.set_dm_in(dma, z0.get_T(),z0.get_gasMixtureZone());
					try {
						z0.set_dm_out(dma);
					} catch (NegativeMassException e) {
						e.log_Warning("The zone "+ (ax*nbrOfRadPackets+ rad)+ 
								" is empty! Something went wrong");
						//should not happen!
					}
				}
				ax++;
			}while(ax<nbrOfAxPackets);	
			ax=-1;
			rad=-1;		
			for (int a=0;a<nbrOfAxPackets;a++){			
				for(int r=0;r<nbrOfRadPackets-1; r++){		
					int idxZ0=a*nbrOfRadPackets+r;
					int idxZ1=a*nbrOfRadPackets+r+1;
					Zone z0=zones_IN[idxZ0];
					Zone z1=zones_IN[idxZ1];
					double dm=this.calc_dm_TI(zones_IN, idxZ0,idxZ1, time);

					double f=redFac;
					double dm0=dm;
					if(z0.get_m()-CP.SYS.MINIMUM_ZONE_MASS<=dm*CP.SYS.WRITE_INTERVAL_SEC
							&&z0.get_m()>CP.SYS.MINIMUM_ZONE_MASS)				
						dm0=f*(z0.get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;
					if(z0.get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
						dm0=0;

					double dm1=dm;
					if(z1.get_m()-CP.SYS.MINIMUM_ZONE_MASS<=dm*CP.SYS.WRITE_INTERVAL_SEC
							&&z1.get_m()>CP.SYS.MINIMUM_ZONE_MASS)						
						dm1=f*(z1.get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;								
					if(z1.get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
						dm1=0;

					dm=Math.min(dm1, dm0);								

					//diffusion into zone r	
					z0.set_dm_in(dm,z1.get_T(),z1.get_gasMixtureZone());
					try {
						z1.set_dm_out(dm);
					} catch (NegativeMassException e) {
						e.log_Warning("Zone "+ (a*nbrOfRadPackets+r+1) +" is empty! Something went wrong");
						//Passiert eh nicht!!
					}

					//diffusion into zone r+1
					z1.set_dm_in(dm,z0.get_T(),z0.get_gasMixtureZone());
					try {
						z0.set_dm_out(dm);
					} catch (NegativeMassException e) {
						e.log_Warning("Zone " + z0+ " is empty! Something went wrong");
						//Passiert eh nicht!!
					}				
				}
			}
		}

		return zones_IN;
	}




	@Override
	protected Zone[] firstLawCombustionChamber(double time, Zone[] zonen_IN) {		

		//Wall heat losses
		dQw=whtModel.get_wallHeatFlux(time, zonen_IN, fortschritt, T_buffer);
		dQw=whtfMult*dQw;
		double V=0;
		//this assures that the volume fractions add up to one
		for(int i=0;i<this.nbrOfZones;i++)V+=zonen_IN[i].get_V(); 
		for(int i=0;i<this.nbrOfZones;i++){
			double dQwi=dQw*zonen_IN[i].get_V()/V;
			zonen_IN[i].set_dQ_in_out(-1*dQwi);					
		}

		//Diesel injection
		for(int i=0;i<masterInjection.get_allInjections().length;i++){	
			Injection es=masterInjection.get_allInjections()[i];					
			if(i==indexOfMainInj){					
				int axPackets=((Spray) es).get_nbrOfAxPackets();
				int radPackets=((Spray) es).get_nbrOfRadPackets();	
				for(int ax=0;ax<axPackets;ax++){	
					for(int rad=0;rad<radPackets; rad++){ //Warum nicht anders herum zaehlen zuerst bekommen die Pakete aussen die Luft 
						double dm_fuelEvap=((Spray) es).get_diff_m_fuelVapor_Packet(time, 
								zonen_IN[ax*radPackets+rad], 	
								ax, 
								rad);
						double T_fuelEvap=((Spray) es).get_T_fuelVapor_Packet(time, 
								zonen_IN[ax*radPackets+rad], 
								ax, 
								rad);

						zonen_IN[ax*radPackets+rad].set_dm_in(dm_fuelEvap, T_fuelEvap, es.get_fuel());	

						double dQ_fuelEvap=((Spray) es).get_dQ_fuelVapor_Packet(	time, 
								zonen_IN[ax*radPackets+rad], 
								ax, 
								rad);						

						zonen_IN[ax*radPackets+rad].set_dQ_in_out(-1*dQ_fuelEvap);
					}
				}
			}else{
				int indexZone=es.get_ID_Zone(); //this should be the index of the gasoline zone 
				double dm_fuelEvap=es.get_diff_mFuel_vapor(time, zonen_IN[indexZone]);
				double T_fuelEvap=es.get_T_fuelVapor(time, zonen_IN[indexZone]);
				zonen_IN[indexZone].set_dm_in(dm_fuelEvap, T_fuelEvap, es.get_fuel());

				double dQ_fuel=es.get_dQ_fuelVapor(time, zonen_IN[indexZone]);	
				zonen_IN[indexZone].set_dQ_in_out(-1*dQ_fuel);				
			}
		}

		//Mixing		
		if(CP.get_mixingProcess()==1)
			zonen_IN=turbMixApproach_1_dmFluxScheme_1(time, zonen_IN);
		else if(CP.get_mixingProcess()==2)
			zonen_IN=turbMixApproach_1_dmFluxScheme_2(time, zonen_IN);
		else if(CP.get_mixingProcess()==3)
			zonen_IN=turbMixApproach_1_dmFluxScheme_3(time, zonen_IN);	
		else if(CP.get_mixingProcess()==4)
			zonen_IN=turbMixApproach_2(time, zonen_IN);
		else if(CP.get_mixingProcess()==5)
			zonen_IN=entrainmentKrishnan(time, zonen_IN);
		else if(CP.get_mixingProcess()==6)
			zonen_IN=entrainmentHiroyasu(time, zonen_IN);
		else{
			try{
				throw new BirdBrainedProgrammerException("The chosen number " +
						"for the mixing process is not valid");
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}

		this.turb.update(zonen_IN, time);
		return zonen_IN;					
	}

	@Override
	public void bufferResults(double time, Zone[] zn) {
		dQb_buffer.addValue(time, dQburnSum);	
		dQw_buffer.addValue(time, dQw);	
		dmb_buffer.addValue(time, 0);			
		Qb=Qb+super.dQburnSumU*super.CP.SYS.WRITE_INTERVAL_SEC;	
		Qw=Qw+dQw*super.CP.SYS.WRITE_INTERVAL_SEC;	

		fortschritt=Qb/QbMax;

		for(int i=0;i<masterInjection.get_allInjections().length;i++){	
			Injection es=masterInjection.get_allInjections()[i];	
			if(i==this.indexOfMainInj){
				int axPackets=((Spray) es).get_nbrOfAxPackets();
				int radPackets=((Spray) es).get_nbrOfRadPackets();
				for(int ax=0;ax<axPackets;ax++)	
					for(int rad=0;rad<radPackets; rad++)
						((Spray)es).calculateIntegralvalues(time, zn[ax*radPackets+rad],ax,rad);
			}
			else
				es.calculateIntegralvalues(time, zn[nbrOfZones-1]);
		}

		int i=-1;
		i+=1;
		super.buffer_singleResult("CrankAngle [°KW]",super.CP.convert_SEC2KW(time),i);

		i+=1;
		super.buffer_singleResult("Time [s n. Rechenbeginn]",time,i);		

		i+=1;
		double V=motor.get_V(time);
		super.buffer_singleResult("Volume [m3]",V,i);	

		double sum_V=0;
		for(int k=0;k<this.nbrOfZones;k++)sum_V+=zn[k].get_V();

		i+=1;
		super.buffer_singleResult("V-sum_Vk [%]",(V-sum_V)/V*100,i);	

		i+=1;
		double Tm=whtModel.get_Tmb(zn);
		super.buffer_singleResult("T_m [K]",Tm,i);
		T_buffer.addValue(time, Tm);

		double pExp=5;
		if(compareToExp)
			pExp=indiD.get_pZyl(time);
		i+=1;
		super.buffer_singleResult("p_exp [bar]", pExp*1E-5,i);

		i+=1;
		super.buffer_singleResult("p_" + 0+  " [bar]",zn[0].get_p()*1e-5,i);

		i+=1;
		super.buffer_singleResult("dQb [J/s]",super.dQburnSumU,i);

		i+=1;
		super.buffer_singleResult("dQbU [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnSumU),i);

		i+=1;		
		super.buffer_singleResult("Qb [J]", Qb,i);

		i+=1;		
		super.buffer_singleResult("Qb/QbMax [-]", Qb/QbMax,i);


		i+=1;		
		super.buffer_singleResult("dQw [J/CA]", super.CP.convert_ProSEC_2_ProKW(dQw),i);

		i+=1;		
		super.buffer_singleResult("Qw [J]", Qw,i);


		double sum_m=0;
		for(int k=0;k<this.nbrOfZones;k++)sum_m+=zn[k].get_m();
		i+=1;
		super.buffer_singleResult("sum_mk [kg]", sum_m,i);				

		i+=1;
		super.buffer_singleResult("mTot [kg]", mTot,i);


		int nbrOfPacketsAlive=-5;
		int axP=0, radP = 0;		
		Injection es=masterInjection.get_allInjections()[this.indexOfMainInj];			
		nbrOfPacketsAlive=((Spray)es).get_nbrOfPacketsAlive(time);
		axP=((Spray)es).get_nbrOfAxPackets();
		radP=((Spray)es).get_nbrOfRadPackets();

		i+=1;
		super.buffer_singleResult("nbrOfPacketsAlive [-]",nbrOfPacketsAlive,i);		


		double ma=0,s=0,dma=0,m_fuelLiq=0,T_fuelVap=0;		
		ma=((Spray)es).get_ma(time);					
		sumdma+=((Spray)es).get_dma(time)*CP.SYS.WRITE_INTERVAL_SEC;
		s=((Spray)es).get_packet(0, 0).get_s(time);
		dma=((Spray)es).get_dma(time);
		m_fuelLiq=es.get_m_fuelLiquid(time);
		T_fuelVap=es.get_T_fuelVapor(time,zn[es.get_ID_Zone()]);		

		i+=1;
		super.buffer_singleResult("T_fuelVapor [K]", T_fuelVap,i);
		i+=1;
		super.buffer_singleResult("m_fuelLiq [kg]", m_fuelLiq,i);		
		i+=1;
		super.buffer_singleResult("m_a [kg]", ma,i);
		i+=1;
		super.buffer_singleResult("sumdma [kg]", sumdma,i);
		i+=1;
		super.buffer_singleResult("s00 [kg]", s,i);
		i+=1;
		super.buffer_singleResult("dm_a [kg/s]", dma,i);	

		i+=1;
		super.buffer_singleResult("V_" +(this.nbrOfZones-1)+" [-]", zn[this.nbrOfZones-1].get_V()/V,i);

		i+=1;
		super.buffer_singleResult("m_" +(this.nbrOfZones-1)+" [kg]", zn[(this.nbrOfZones-1)].get_m(),i);


		for(int k=0;k<this.nbrOfZones-1;k++){
			i+=1;
			super.buffer_singleResult("V_" +k+" [-]", zn[k].get_V()/V,i);
		}


		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_singleResult("T_" + k+ " [K]",zn[k].get_T(),i);
		}		

		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_singleResult("dQb_"+k+ "[J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnU[k]),i);
		}

		for(int ax=0;ax<axP;ax++){
			double sum=0;
			for(int rad=0;rad<radP;rad++){				
				sum=sum+zn[ax*radP+rad].get_m();			
			}
			i+=1;
			super.buffer_singleResult("mAx_"+ax+ "[kg]", sum,i);
		}



		for(int k=0;k<this.nbrOfZones-1;k++){
			i+=1;
			super.buffer_singleResult("m_" +k+" [kg]", zn[k].get_m(),i);
		}	

		double massfrac []=new double[zn.length];
		for(int iterZn=0; iterZn<zn.length;iterZn++){
			massfrac[iterZn]=zn[iterZn].get_m()/sum_m;			
		}		
		double sum=0;
		for(int j=0;j<massfrac.length;j++)sum=sum+massfrac[j];
		for(int j=0;j<massfrac.length;j++)massfrac[j]=massfrac[j]/sum;

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies, Double>();
		for(int iterZn=0; iterZn<zn.length;iterZn++){
			ht.put(zn[iterZn].get_gasMixtureZone(), massfrac[iterZn]);			
		}

		GasMixture gg=new GasMixture("totalMix");
		gg.set_Gasmischung_massenBruch(ht);
		i+=1;
		double lambdaMix=gg.get_lambda();
		super.buffer_singleResult("lamTotMix [-]",lambdaMix,i);

		double doh=0; //Degree of heterogeneity
		for(int iterZn=0; iterZn<zn.length;iterZn++){
			double a=zn[iterZn].get_gasMixtureZone().get_lambda()-lambdaMix;
			doh=doh+a*a*zn[iterZn].get_m();
		}
		doh=Math.sqrt(doh/sum_m)/lambdaMix;

		i+=1;
		super.buffer_singleResult("DOH [-]",doh,i);	


		for(int k=0;k<this.nbrOfZones;k++){	
			i+=1;			
			String name="Lambda_"+k;
			double lambda=zn[k].get_gasMixtureZone().get_lambda();
			super.buffer_singleResult(name,
					lambda,i);
		}

		int nbrLam=this.nbrOfZones;
		double lMax=5.5;		
		//System.out.println(lMax);
		double lMin=1.5;
		double lamInt=(lMax-lMin)/nbrLam;
		//System.out.println(lamInt);
		double lambdas []= new double [nbrLam];
		double masses[]=new double [nbrLam];
		for(int j=0;j<nbrLam;j++){
			lambdas [j]=lMin+j*lamInt;
		}	

		for(int iterZn=0; iterZn<zn.length;iterZn++){
			double lam=zn[iterZn].get_gasMixtureZone().get_lambda();
			for(int j=0;j<nbrLam-1;j++){
				//if(lam<=lambdas[j]&& lam>lambdas[j+1])
				if(lam>=lambdas[j]&&lam<lambdas[j+1])
					masses[j]=masses[j]+zn[iterZn].get_m();				
			}
			if(lam>=lambdas[lambdas.length-1])
				masses[lambdas.length-1]=masses[lambdas.length-1]+zn[iterZn].get_m();		
		}

		for(int j=0;j<nbrLam;j++){
			i+=1;			
			//String name=lambdas[j+1]+"<L<"+lambdas[j];
			double lam=lambdas[j]+0.5*lamInt;
			String name="Lam_"+lam;
			super.buffer_singleResult(name,
					masses[j],i);			
		}


		int index=CP.SPECIES_FACTROY.get_indexOf(CP.SPECIES_FACTROY.get_nC7H16());
		for(int k=0;k<this.nbrOfZones;k++){	
			i+=1;
			String name="nc7h16_"+k;
			double []mi=zn[k].get_mi();
			super.buffer_singleResult(name,
					mi[index]/zn[k].get_m(),i);

		}		
		double sum_mD=0;
		for(int k=0;k<this.nbrOfZones;k++){				
			double []mi=zn[k].get_mi();
			i+=1;
			super.buffer_singleResult("mD_"+k,mi[index],i);
			sum_mD=sum_mD+mi[index];	
		}

		i+=1;
		super.buffer_singleResult("mD_Tot",sum_mD,i);

		//		int index=CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_soot());
		//		for(int k=0;k<this.nbrOfZones;k++){	
		//			i+=1;
		//			String name="soot"+k;
		//			double []mi=zn[k].get_mi();
		//			super.buffer_EinzelErgebnis(name,
		//					mi[index]/zn[k].get_m(),i);
		//
		//		}



		//		i+=1;
		//		for(int k=0;k<this.nbrOfZones;k++){					
		//			double []mi=zn[k].get_mi();
		//			for(int idx=0;idx<mi.length;idx++){
		//				String name="Z_"+k+"_y_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name();
		//				super.buffer_EinzelErgebnis(name,
		//						mi[idx]/zn[k].get_m(),i);
		//				//System.out.println(name+" : "+i);
		//				i+=1;
		//			}	
		//		}


		i+=1;
		super.buffer_singleResult("TKE_M [m^2/s^2]", this.turb.get_k(zn,time),i);

		i+=1;
		super.buffer_singleResult("EPS_M [m^2/s^3]", this.turb.get_eps(zn,time),i);

		i+=1;
		super.buffer_singleResult("TI_M [m/s]", this.turb.get_intensity(zn,time),i);

		i+=1;
		super.buffer_singleResult("L_M [m]", this.turb.get_turbulenceIntegralLengthScale(zn,time),i);


		for(int k=0;k<this.nbrOfZones;k++){	
			i+=1;
			String name="Hu_"+k+ " [J/kg]";			
			super.buffer_singleResult(name,
					zn[k].get_gasMixtureZone().get_LHV_mass(),i);

		}	
	}

	@Override
	public Zone[] get_initialZones() {
		return initialZones;
	}

	@Override
	public Zone[] get_initialZonesWhileRunning() {		
		return null;
	}

	@Override
	public int get_nbrOfZones() {
		return nbrOfZones;
	}

	@Override
	public VectorBuffer get_dm_buffer() {
		return dmb_buffer;
	}

	@Override
	public VectorBuffer get_dQw_buffer() {
		return this.dQw_buffer;
	}

	@Override
	public VectorBuffer get_dQb_buffer() {
		return this.dQb_buffer;
	}

	@Override
	protected void checkInjections(MasterInjection me) {

		int nbrOfInj=me.get_allInjections().length;
		for(int i=0;i<nbrOfInj;i++){
			if(me.get_allInjections()[i].get_fuel()==CP.SPECIES_FACTROY.get_nC7H16()||
					me.get_allInjections()[i].get_fuel()==CP.SPECIES_FACTROY.get_iC8H18()){
				//do nothing
			}else{
				try {
					throw new ParameterFileWrongInputException("For the RCCI Combustion Model  " +
							"only  nC7H16 and iC8H18 are allowed as fuels");
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}

			if(me.get_allInjections()[i].get_ID_Zone()!=this.nbrOfZones-1){
				try {
					throw new ParameterFileWrongInputException("Only injections into the " +
							"gasoline zone (index: " +(this.nbrOfZones-1) +")  are allowed!");
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}		
		}	

		if(me.get_allInjections()[indexOfMainInj].get_fuel()!=CP.SPECIES_FACTROY.get_nC7H16()){
			try {
				throw new ParameterFileWrongInputException("The main Injection has to inject nC7H16");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}		
		}


		try{
			if(((Spray) me.get_allInjections()[indexOfMainInj]).get_nbrOfAxPackets()!=this.axPackets || 
					((Spray) me.get_allInjections()[indexOfMainInj]).get_nbrOfRadPackets()!=this.radPackets)
				try {
					throw new BirdBrainedProgrammerException("The number of packets was changed somewhere");
				} catch (BirdBrainedProgrammerException e) {				
					e.stopBremo();
				}
		}catch(ClassCastException cce){
			try {
				throw new ParameterFileWrongInputException("The main injection has to be a packet model (eg: Hiroyasu)");
			} catch (ParameterFileWrongInputException e) {				
				e.stopBremo();
			}
		}
	}
}

