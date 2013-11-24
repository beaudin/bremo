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

public class APR_CanteraMultiZoneRCCI_BackUp extends APR_Cantera {		
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
	private double QbMax, dQw, Qb=0,Qw=0,mTot, lambdaMax, whtfMult, mGasZoneInit; 	
	private final int axPackets, radPackets, nbrOfPackets, indexOfMainInj;
	private TurbulenceFileReader tfr;
	double sumdma=0;
	private boolean compareToExp;
	private double redFac=0.85;
	private int nbrOfZones;
	



	protected APR_CanteraMultiZoneRCCI_BackUp(CasePara cp) {
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
		
		super.dQburn=new double [nbrOfZones];	
		super.dQburnVol=new double [nbrOfZones];
		super.dQburnU=new double [nbrOfZones];
		super.dQburnHf=new double [nbrOfZones];

		if(CP.compareToExp()){
			indiD=new IndicatorData(cp);
			compareToExp=true;
		}	

		T_buffer = new VectorBuffer(cp);
		dQb_buffer = new VectorBuffer(cp);	
		dQw_buffer = new VectorBuffer(cp);	
		dmb_buffer = new VectorBuffer(cp);	

		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////		
		initialZones=new Zone[nbrOfZones];
		super.dQburn=new double [nbrOfZones];
		whtfMult=this.CP.get_whtfMult();
		initialZones=initializeZones();
		turb.initialize(initialZones, 0);
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
		lambdaMax=spec.get_lambda();
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
		mGasZoneInit=mG;

		double testV=V_init-VG-VD*this.nbrOfPackets;
		System.out.println("testV: "+ testV);	

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

	double deltaMixTot;
	private Zone[] turbMixing(double time, Zone[] zonen_IN){
		for(int i=0;i<masterInjection.get_allInjections().length;i++){	
			Injection es=masterInjection.get_allInjections()[i];					
			if(es.get_fuel()==CP.SPECIES_FACTROY.get_nC7H16()){	

				int axialPakete=((Spray) es).get_nbrOfAxPackets();
				int radialPakete=((Spray) es).get_nbrOfRadPackets();				
				if(time<=es.get_EOI()+5d*CP.SYS.WRITE_INTERVAL_SEC){					
					//					//Entrainment aus der OttoZone
					double dmaTot=0;
					int ax=0;
					int rad=0;						
					do{						
						do{
							double dma=((Spray) es).get_dma_Packet(time, ax,rad );							
							if(zonen_IN[nbrOfZones-1].get_m()>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
									&& zonen_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){
								dmaTot=dmaTot+dma;
								zonen_IN[ax*radialPakete+rad].set_dm_in(dma, 
										zonen_IN[nbrOfZones-1].get_T(), 
										zonen_IN[nbrOfZones-1].get_gasMixtureZone());
								try {
									zonen_IN[nbrOfZones-1].set_dm_out(dma);
								} catch (NegativeMassException e) {
									e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
									//Passiert eh nicht!!
								}								
							}
							ax++;
						}while(ax<axialPakete);						
						ax=0;
						rad++;
					}while(rad<radialPakete);
				}else{//turbulent Mixing
					//Constants					
					double C1_3=0d; 
					double C2=0d; 	// radial position
					double C4=1d; 	//charachteristic length scale
					double C5=1d;	//relative Diffusion coefficient 1
					double C6=1d;	//relative Diffusion coefficient 2

					int ax=0;
					int rad=0;
					deltaMixTot=0;
					do{						
						do{
							int z=ax*radialPakete+rad;
							int z1=ax*radialPakete+rad+1;
							//the outmost packet entrains from the gasoline zone
							if(rad==radialPakete-1)
								z1=nbrOfZones-1;

							double lifetime=((Spray) es).get_packet(ax, rad).get_lifetime(time);

							//volume combustion chamber
							double Vzyl=motor.get_V(time);
							//zone Volume
							double V=zonen_IN[z].get_V();							
							//mass of zone
							double m=zonen_IN[z].get_m();

							// Volume of zone z1
							double V1=zonen_IN[z1].get_V();							
							//mass of zone z1
							double m1=zonen_IN[z1].get_m();
							double rho_0=m/V;
							double rho_1=V1/m1;


							//large eddie length scale (Ramos page 109 eq. 44)	
							double b=((Motor_reciprocatingPiston)motor).get_bore();
							double LL=4*Vzyl/Math.PI/b/b;
							if(LL>0.5*b)LL=0.5*b;								
							//charachteristic length scale
							double lc=C4*Math.pow(V, (1d/3d));								
							//turbulent diffusion coefficient
							double Dr;
							if(lc<LL){
								//System.out.println("ich bin hier drin");
								Dr=C5*this.tfr.get_EPS(time)*Math.pow(lc, (4d/3d));
							}else{
								Dr=C6*this.tfr.get_TI(time)*LL;
							}								
							//massflow
							double exp=-1*C2*Math.pow(V, 2d/3d)/Dr/lifetime;							
							double dm=C1_3*m/lifetime*Math.exp(exp);	


							//second approach
							//							if(Math.abs(time-CP.SYS.DUBUGGING_TIME_SEC)<0.5*CP.SYS.WRITE_INTERVAL_SEC)
							//								System.out.println("Stop me here: Zone.turbMixing");
							double C0=CP.get_C_Mix(); 
							double C00=1;
							//dm=C0*m/V*Math.pow(V, 2d/3d)*this.tfr.get_TI(time)*LL/Math.pow(V, 1d/3d);
							dm=C0*m/V*Math.pow(V, 2d/3d)*this.tfr.get_TI(time)*
									Math.exp(Math.pow(LL-C00*Math.pow(V, 1d/3d), 2));
							dm=C0*m/V*Math.pow(V, 2d/3d)*this.tfr.get_TI(time);
							//surface 
							double surf=Math.pow(V, 2d/3d);		
							double ue=this.tfr.get_TI(time);
							dm=C0*Math.sqrt(rho_0*rho_1)*ue*surf;
							//This makes a differnce whereas in method turbMix4 it does not?
							dm=C0*Math.sqrt(rho_0*rho_0)*ue*surf;

							if(dm<0)dm=0;							

							double f=0.85;
							double dm0=dm;
							if(zonen_IN[z].get_m()-CP.SYS.MINIMUM_ZONE_MASS<=dm*CP.SYS.WRITE_INTERVAL_SEC
									&&zonen_IN[z].get_m()>CP.SYS.MINIMUM_ZONE_MASS)				
								dm0=f*(zonen_IN[z].get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;
							if(zonen_IN[z].get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
								dm0=0;

							double dm1=dm;
							if(zonen_IN[z1].get_m()-CP.SYS.MINIMUM_ZONE_MASS<=dm*CP.SYS.WRITE_INTERVAL_SEC
									&&zonen_IN[z1].get_m()>CP.SYS.MINIMUM_ZONE_MASS)						
								dm1=f*(zonen_IN[z1].get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;								
							if(zonen_IN[z1].get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
								dm1=0;


							dm=Math.min(dm1, dm0);								

							deltaMixTot+=dm*CP.SYS.WRITE_INTERVAL_SEC;

							//diffusion into zone r	
							zonen_IN[z].set_dm_in(dm, 
									zonen_IN[z1].get_T(), 
									zonen_IN[z1].get_gasMixtureZone());
							try {
								zonen_IN[z1].set_dm_out(dm);
							} catch (NegativeMassException e) {
								e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
								//Passiert eh nicht!!
							}

							//diffusion into zone r+1
							zonen_IN[z1].set_dm_in(dm, 
									zonen_IN[z].get_T(), 
									zonen_IN[z].get_gasMixtureZone());
							try {
								zonen_IN[z].set_dm_out(dm);
							} catch (NegativeMassException e) {
								e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
								//Passiert eh nicht!!
							}								
							ax++;
						}while(ax<axialPakete);						
						ax=0;
						rad++;
					}while(rad<radialPakete);				
				}
			}
		}

		return zonen_IN;


	}


	private Zone[] entrainment(double time, Zone[] zonen_IN){

		//get the main injection
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];

		int axialPakete=((Spray) es).get_nbrOfAxPackets();
		int radialPakete=((Spray) es).get_nbrOfRadPackets();				

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
					zonen_IN[ax*radialPakete+rad].set_dm_in(dma, 
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
			}while(ax<axialPakete);						
			ax=0;
			rad++;
		}while(rad<radialPakete);				

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
							&& zonen_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){
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


	private double calc_dm(Zone z0,Zone z1, double time){		
			return this.calc_dm_TI(z0, z1, time);
	}

	private double calc_dm_TI(Zone z0,Zone z1, double time){

		double V0=z0.get_V();	
		double m0=z0.get_m();
		double rho_0=m0/V0;
		double rho_1=z1.get_m()/z1.get_V();
		//volume combustion chamber
		double Vzyl=motor.get_V(time);
		double b=((Motor_reciprocatingPiston)motor).get_bore();
		double LL=4*Vzyl/Math.PI/b/b;
		if(LL>0.5*b)LL=0.5*b;								
		//charachteristic length scale
		double C4=CP.get_C_MixRad();	
		double lc=C4*Math.pow(V0, (1d/3d));	
		double factor=Math.exp(-1d*(lc/LL-1.0));	
		//eigentlich richtig! Vielleicht statt der eins eine 0.005 oder so
		factor=Math.exp(-1.0*(lc/LL-1.0)*(lc/LL-1.0));	
		//surface 
		double surf=Math.pow(V0, 2d/3d);		
		double ue=this.tfr.get_TI(time);
		double C_Mix=CP.get_C_Mix();
		double dma=C_Mix*Math.sqrt(rho_0*rho_1)*ue*surf;	
		//this makes nearly no differnce
		//dma=C_Mix*rho_0*ue*surf;

		//but this makes a huge difference --> without the factor it doesent work!
		dma=dma*factor;
		return dma;

	}
	
	/**
	 * Berechnet den Massenstrom auf Basis der Zonenmasse aus der entnommen 
	 * wird und der integral time scale
	 * @param z
	 * @param time
	 * @return
	 */
	private double calc_dm1(Zone z, double time){

		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);


		double Vcyl=this.motor.get_V(time);	
		double mZone=z.get_m();

		//integral length scale
		double tI=2.0/3.0*tfr.get_TKE(time)/this.tfr.get_EPS(time);
		double c1=CP.get_C_Mix();

		double dcD=-c1*mZone/tI/Vcyl;
		double b=mZone/z.get_V()*motor.get_dV(time);
		double dmD=dcD*Vcyl+b;	
		if(dmD>0)
			dmD=0;		
		return -1*dmD;
	}


	private Zone[] turbMix0(double time, Zone[] zonen_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI()){
			zonen_IN=entrainment(time,zonen_IN);
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
				Zone z0=zonen_IN[ax*nbrOfRadPackets+rad];
				Zone z1=zonen_IN[nbrOfZones-1];
				double dma=this.calc_dm(z0, z1, time);
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
					Zone z0=zonen_IN[a*nbrOfRadPackets+r];
					Zone z1=zonen_IN[a*nbrOfRadPackets+r+1];
					double dm=this.calc_dm(z0, z1, time);				

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

					deltaMixTot+=dm*CP.SYS.WRITE_INTERVAL_SEC;

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

		return zonen_IN;

	}

	private Zone[] turbMix1(double time, Zone[] zonen_IN){

		//get the main injection
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];

		if(time<=es.get_EOI()){
			zonen_IN=entrainment(time,zonen_IN);
		}
		else{

			int axialPakete=((Spray) es).get_nbrOfAxPackets();
			int radialPakete=((Spray) es).get_nbrOfRadPackets();	
			
			//Entrainment from gasoline zone
			double tI=2.0/3.0*turb.get_k(zonen_IN,time)/this.turb.get_eps(zonen_IN,time);
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
			int idxRmax=radialPakete-1;		
			double a=Math.log(cc)/idxRmax/idxRmax;
			double sum=0;	
			for(int i=0;i<radialPakete;i++)sum+=Math.exp(a*i*i);	
			double t0=Math.exp(a*0*0);
			double tR=Math.exp(a*idxRmax*idxRmax);
			double dmCHK=0;
			int ax=0;
			int rad=0;
			do{						
				do{
					Zone z0=zonen_IN[ax*radialPakete+rad];
					Zone z1=zonen_IN[nbrOfZones-1];					
					double f=Math.exp(a*(idxRmax-rad)*(idxRmax-rad))/sum/axialPakete;			
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
		return zonen_IN;
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
	private Zone[] turbMix1_b(double time, Zone[] zonen_IN){

		//get the main injection
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];

		if(time<=es.get_EOI()){
			zonen_IN=entrainment(time,zonen_IN);
		}
		else{	
			//die if-Abfrage ist noch nciht getestet! 20130524
			if(zonen_IN[nbrOfZones-1].get_m()>CP.SYS.MINIMUM_ZONE_MASS){
				int axialPakete=((Spray) es).get_nbrOfAxPackets();
				int radialPakete=((Spray) es).get_nbrOfRadPackets();	
				//double [][] r=this.get_packetOuterDiameters(zonen_IN, time);
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
//						sum+=Math.exp(a*rad*rad/turbD/mixTime);	
						sum+=Math.exp(a*(idxRmax-rad)*(idxRmax-rad)/turbD/mixTime);	 
					}
				}
				//double t0=Math.exp(a*0*0);
				double tR=Math.exp(a*idxRmax*idxRmax);
				double dmCHK=0;
				int ax=0;
				int rad=0;
				do{						
					do{
						Zone z0=zonen_IN[ax*radialPakete+rad];
						Zone z1=zonen_IN[nbrOfZones-1];		
						double f=Math.exp(a*(idxRmax-rad)*(idxRmax-rad)/turbD/mixTime)/sum;	
//						double f=Math.exp(-a*(rad)*(rad)/turbD/mixTime)/sum;	
						//Dieser bug macht hoffentlich/offensichtlich keinen grossen unterschied...ausser, dass die inneren Pakete nun die groessere Masse bekommen!
//						double f=Math.exp(a*rad*rad/turbD/mixTime)/sum/axialPakete;			
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

	
	private Zone[] turbMix2(double time, Zone[] zonen_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI())
			zonen_IN=entrainment(time,zonen_IN);
		else{	
			//zonen_IN=entrainment(time,zonen_IN);
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();				

			for(int ax=0;ax<nbrOfAxPackets;ax++){
				for(int rad=0; rad<nbrOfRadPackets; rad++){					

					//the outmost packet mixes with the gasoline zone
					Zone z1;
					if(rad==nbrOfRadPackets-1)
						z1=zonen_IN[nbrOfZones-1]; 
					else
						z1=zonen_IN[ax*nbrOfRadPackets+rad+1];

					Zone z0=zonen_IN[ax*nbrOfRadPackets+rad];

					double dm=this.calc_dm(z0, z1, time);	

					if(dm*CP.SYS.WRITE_INTERVAL_SEC>=z0.get_m()-CP.SYS.MINIMUM_ZONE_MASS &&
							z0.get_m()>CP.SYS.MINIMUM_ZONE_MASS)
						dm=redFac*(z0.get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;
					if(z0.get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
						dm=0;
					try {
						z0.set_dm_out(dm);
						z1.set_dm_in(dm, z0.get_T(),z0.get_gasMixtureZone());	
					} catch (NegativeMassException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return zonen_IN;
	}


	/**
	 * fuer die ausseren pakete wird das entrainment aus der ottozone entnommen. Das dm fuer 
	 * die ausseren Pakete wird verwendet, um den Massenstrom fuer die inneren Pakete zu bestimmen
	 * (aehnlich wie auch bei Hiroyasu). Die inneren Pakete erhalten das entrainment jeweils von den 
	 * Paketen eine ebene weiter aussen!
	 * @param time
	 * @param zonen_IN
	 * @return
	 */
	private Zone[] turbMix3_a(double time, Zone[] zonen_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI()){
			zonen_IN=entrainment(time,zonen_IN);
		}
		else{
			//zonen_IN=entrainment(time,zonen_IN);
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	
			//Entrainment from gasoline zone in the outer packets
			double dmaTot=0;	
			double dm_rim[]=new double [nbrOfAxPackets];
			int ax=0;
			int rad=nbrOfRadPackets-1;		
			double C_Mix=CP.get_C_Mix();
			do{

				double V0=zonen_IN[ax*nbrOfRadPackets+rad].get_V();	
				double m0=zonen_IN[ax*nbrOfRadPackets+rad].get_m();
				double rho_0=m0/V0;
				double rho_1=zonen_IN[nbrOfZones-1].get_m()/zonen_IN[nbrOfZones-1].get_V();
				//surface 
				double surf=Math.pow(V0, 2d/3d);		
				double ue=this.tfr.get_TI(time);
				double dma=C_Mix*Math.sqrt(rho_0*rho_1)*ue*surf;					

				//check if gasoline zone still conains enough mass
				if(zonen_IN[nbrOfZones-1].get_m()-CP.SYS.MINIMUM_ZONE_MASS 
						>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
						&& zonen_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){
					dmaTot=dmaTot+dma;
					dm_rim[ax]=dma;
					zonen_IN[ax*nbrOfRadPackets+ rad].set_dm_in(dma, 
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
			}while(ax<nbrOfAxPackets);	
			ax=-1;
			rad=-1;

			double pF=CP.get_C_MixRad(); 			
			double Crad=Math.log(pF)/((nbrOfRadPackets-1)*(nbrOfRadPackets-1));	
			for (int a=0;a<nbrOfAxPackets;a++){			
				for(int r=0;r<nbrOfRadPackets-1; r++){
					double dm=dm_rim[a]; 				
					double r_=nbrOfRadPackets-1-r;
					dm=dm*Math.exp(Crad*r_*r_);

					int z0=a*nbrOfRadPackets+r;
					if(zonen_IN[z0+1].get_m()-CP.SYS.MINIMUM_ZONE_MASS<=dm*CP.SYS.WRITE_INTERVAL_SEC
							&& zonen_IN[z0+1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){	
						dm=0.9*(zonen_IN[z0+1].get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;
					}else if(zonen_IN[z0+1].get_m()-CP.SYS.MINIMUM_ZONE_MASS>dm*CP.SYS.WRITE_INTERVAL_SEC
							&& zonen_IN[z0+1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){
						//do nothing
					}else{
						dm=0;
					}
					zonen_IN[z0].set_dm_in(dm, 
							zonen_IN[z0+1].get_T(), 
							zonen_IN[z0+1].get_gasMixtureZone());
					try {
						zonen_IN[z0+1].set_dm_out(dm);
					} catch (NegativeMassException e) {
						e.log_Warning("The gasoline zone is empty! Something went wrong");
						//should not happen!
					}				
				}
			}


		}

		return zonen_IN;

	}


	/**
	 * fuer die ausseren pakete wird das entrainment aus der ottozone entnommen. 
	 * Der Massenstrom fuer 
	 * die inneren Pakete wird bestimmt indem aus dem Volumen eine 
	 * charchteristische Flaeche und eine 
	 * charachteristische laenge berechnet wird. Die Laenge wird ins Verhaeltnis 
	 * zur integralen Laengenskala der Turbulenz gesetzt. Der so 
	 * erhaltene faktor wird maximal wenn laengen
	 * skala und der Zone und turbulenz skala uebereinstimmen. 
	 * Die inneren Pakete erhalten das entrainment jeweils von den 
	 * Paketen eine ebene weiter aussen!
	 * @param time
	 * @param zonen_IN
	 * @return
	 */
	private Zone[] turbMix3_b(double time, Zone[] zonen_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI())
			zonen_IN=entrainment(time,zonen_IN);
		else{	
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	
			//Entrainment from gasoline zone in the outer packets
			double dmaTot=0;				
			int ax=0;
			int rad=nbrOfRadPackets-1;				
			do{		
				Zone z0=zonen_IN[ax*nbrOfRadPackets+rad];
				Zone z1=zonen_IN[nbrOfZones-1];
				double dma=this.calc_dm(z0, z1, time);

				//				double V0=zonen_IN[ax*nbrOfRadPackets+rad].get_V();	
				//				double m0=zonen_IN[ax*nbrOfRadPackets+rad].get_m();
				//				double rho_0=m0/V0;
				//				double rho_1=zonen_IN[nbrOfZones-1].get_m()/zonen_IN[nbrOfZones-1].get_V();
				//				//volume combustion chamber
				//				double Vzyl=motor.get_V(time);
				//				double b=((Motor_HubKolbenMotor)motor).get_Bohrung();
				//				double LL=4*Vzyl/Math.PI/b/b;
				//				if(LL>0.5*b)LL=0.5*b;								
				//				//charachteristic length scale
				//				double C4=CP.get_C_MixRad();	
				//				double lc=C4*Math.pow(V0, (1d/3d));	
				//				double factor=Math.exp(-1*(lc/LL-1));
				//				//surface 
				//				double surf=Math.pow(V0, 2d/3d);		
				//				double ue=this.tfr.get_TI(time);
				//				double C_Mix=CP.get_C_Mix();
				//				double dma=C_Mix*Math.sqrt(rho_0*rho_1)*ue*surf;				
				//				dma=dma*factor;
				//This makes nearly no differnce!
				//dma=C_Mix*rho_0*ue*surf; 					

				//check if gasoline zone still conains enough mass
				if(zonen_IN[nbrOfZones-1].get_m()-CP.SYS.MINIMUM_ZONE_MASS 
						>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
						&& zonen_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){						

					dmaTot=dmaTot+dma;					
					zonen_IN[ax*nbrOfRadPackets+ rad].set_dm_in(dma, 
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
			}while(ax<nbrOfAxPackets);	
			ax=-Integer.MAX_VALUE;
			rad=-Integer.MAX_VALUE;

			for (int a=0;a<nbrOfAxPackets;a++){			
				for(int r=0;r<nbrOfRadPackets-1; r++){
					Zone z0=zonen_IN[a*nbrOfRadPackets+r];
					Zone z1=zonen_IN[a*nbrOfRadPackets+r+1];
					double dm=this.calc_dm(z0, z1, time);

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

					//					int z0=a*nbrOfRadPackets+r;
					//					double V0=zonen_IN[z0].get_V();	
					//					double m0=zonen_IN[z0].get_m();
					//					double rho_0=m0/V0;
					//					double rho_1=zonen_IN[z0+1].get_m()/zonen_IN[z0+1].get_V();
					//					//volume combustion chamber
					//					double Vzyl=motor.get_V(time);
					//					double b=((Motor_HubKolbenMotor)motor).get_Bohrung();
					//					double LL=4*Vzyl/Math.PI/b/b;
					//					if(LL>0.5*b)LL=0.5*b;								
					//					//charachteristic length scale
					//					double C4=CP.get_C_MixRad();
					//					double lc=C4*Math.pow(V0, (1d/3d));	
					//					double factor=Math.exp(-1*(lc/LL-1));
					//					//surface 
					//					double surf=Math.pow(V0, 2d/3d);		
					//					double ue=this.tfr.get_TI(time);
					//					double C_Mix=CP.get_C_Mix();
					//					double dm=C_Mix*Math.sqrt(rho_0*rho_1)*ue*surf;	
					//					dm=dm*factor;
					//					//This makes nearly no differnce!
					//					//dm=C_Mix*rho_0*ue*surf; 		
					//					
					//					double f=0.9;
					//					if(zonen_IN[z0+1].get_m()-CP.SYS.MINIMALE_ZONENMASSE<=dm*CP.SYS.WRITE_INTERVAL_SEC
					//							&&zonen_IN[z0+1].get_m()>CP.SYS.MINIMALE_ZONENMASSE)						
					//						dm=f*(zonen_IN[z0+1].get_m()-CP.SYS.MINIMALE_ZONENMASSE)/CP.SYS.WRITE_INTERVAL_SEC;								
					//					if(zonen_IN[z0+1].get_m()<=CP.SYS.MINIMALE_ZONENMASSE)
					//						dm=0;
					//					zonen_IN[z0].set_dm_ein(dm, 
					//							zonen_IN[z0+1].get_T(), 
					//							zonen_IN[z0+1].get_ggZone());
					//					try {
					//						zonen_IN[z0+1].set_dm_aus(dm);
					//					} catch (NegativeMassException e) {
					//						e.log_Warning("The gasoline zone is empty! Something went wrong");
					//						//should not happen!
					//					}					
				}
			}		
		}
		return zonen_IN;

	}
	
	
	/**
	 * Aehnlich wie turbMix3_b nur, dass dm anders berechnet wird!
	 * @param time
	 * @param zonen_IN
	 * @return
	 */
	private Zone[] turbMix3_c(double time, Zone[] zonen_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI())
			zonen_IN=entrainment(time,zonen_IN);
		else{	
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	

			//Entrainment from gasoline zone
			double tI=2.0/3.0*turb.get_k(zonen_IN,time)/this.turb.get_eps(zonen_IN,time);
			double c=CP.get_C_Mix();
			
			if(zonen_IN[zonen_IN.length-1].get_m()>CP.SYS.MINIMUM_ZONE_MASS){
				double dmaTot=c*zonen_IN[zonen_IN.length-1].get_m()/tI;	

				try {
					zonen_IN[nbrOfZones-1].set_dm_out(dmaTot);
				} catch (NegativeMassException e) {			
					e.printStackTrace();
				}
				//entrainment to the outmost packets
				double dmaAx=dmaTot/nbrOfAxPackets;

				int rad=nbrOfRadPackets-1;	
				for (int ax=0; ax<nbrOfAxPackets; ax++){		
					zonen_IN[ax*nbrOfRadPackets+ rad].set_dm_in(dmaAx, 
							zonen_IN[nbrOfZones-1].get_T(), 
							zonen_IN[nbrOfZones-1].get_gasMixtureZone());				
				}
			}

			for (int a=0;a<nbrOfAxPackets;a++){			
				for(int r=0;r<nbrOfRadPackets-1; r++){
					int idxZ0=a*nbrOfRadPackets+r;
					int idxZ1=a*nbrOfRadPackets+r+1;
					Zone z0=zonen_IN[idxZ0];
					Zone z1=zonen_IN[idxZ1];
					if(z1.get_m()>CP.SYS.MINIMUM_ZONE_MASS){

						double dm=c*z1.get_m()/tI;	

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
		}
		return zonen_IN;

	}
	
	/**
	 * eigentlich wie turbMix3_b nur, dass dm mit der flaeche aus den radien berechnet wird!
	 * @param time
	 * @param zonen_IN
	 * @return
	 */
	private Zone[] turbMix3_d(double time, Zone[] zonen_IN){	
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];	
		
		if(time<=es.get_EOI())
			zonen_IN=entrainment(time,zonen_IN);
		else{
			
			double rlA [][][]=this.get_r_l_A_Packets(zonen_IN, time);
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();			
		
			double ti=this.turb.get_intensity(zonen_IN, time);
			double c=CP.get_C_Mix();
						
			
			//Entrainment from gasoline zone in the outer packets
			double dmaTot=0;				
			int ax=0;
			int rad=nbrOfRadPackets-1;				
			do{					
				Zone z0=zonen_IN[ax*nbrOfRadPackets+rad];
				Zone z1=zonen_IN[nbrOfZones-1];
				
				double rho0=z0.get_m()/z0.get_V();				
				double surf=rlA[ax][2][rad];				
				double dma=c*surf*rho0*ti;
				
				//check if gasoline zone still conains enough mass
				if(z1.get_m()-CP.SYS.MINIMUM_ZONE_MASS 
						>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
						&& z1.get_m()>=CP.SYS.MINIMUM_ZONE_MASS){						

					dmaTot=dmaTot+dma;					
					z0.set_dm_in(dma,z1.get_T(), z1.get_gasMixtureZone());
					try {
						z1.set_dm_out(dma);
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
					Zone z0=zonen_IN[a*nbrOfRadPackets+r];
					Zone z1=zonen_IN[a*nbrOfRadPackets+r+1];
					
					double rho0=z0.get_m()/z0.get_V();					
					double surf=rlA[a][2][r];					
					double dm=c*surf*rho0*ti;

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
		return zonen_IN;

	}


	/**
	 * Das entrainment wird zunaechts ueber eine do while schleife aus der Ottozone entnommen und den
	 * Randpaketen zugefuehrt. Dann findet zwischen den Zonen ein austausch in beide Richtungen statt
	 * @param time
	 * @param zonen_IN
	 * @return
	 */
	private Zone[] turbMix4(double time, Zone[] zonen_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI())
			zonen_IN=entrainment(time,zonen_IN);
		else{	
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	
			//Entrainment from gasoline zone in the outer packets
			double dmaTot=0;				
			int ax=0;
			int rad=nbrOfRadPackets-1;			
			do{
				Zone z0=zonen_IN[ax*nbrOfRadPackets+rad];
				Zone z1=zonen_IN[nbrOfZones-1];
				double dma=this.calc_dm(z0, z1, time);

				//				double V0=zonen_IN[ax*nbrOfRadPackets+rad].get_V();	
				//				double m0=zonen_IN[ax*nbrOfRadPackets+rad].get_m();
				//				double rho_0=m0/V0;
				//				double rho_1=zonen_IN[nbrOfZones-1].get_m()/zonen_IN[nbrOfZones-1].get_V();
				//				//volume combustion chamber
				//				double Vzyl=motor.get_V(time);
				//				double b=((Motor_HubKolbenMotor)motor).get_Bohrung();
				//				double LL=4*Vzyl/Math.PI/b/b;
				//				if(LL>0.5*b)LL=0.5*b;								
				//				//charachteristic length scale
				//				double C4=CP.get_C_MixRad();	
				//				double lc=C4*Math.pow(V0, (1d/3d));	
				//				double factor=Math.exp(-1*(lc/LL-1));
				//				//surface 
				//				double surf=Math.pow(V0, 2d/3d);		
				//				double ue=this.tfr.get_TI(time);
				//				double dma=C_Mix*Math.sqrt(rho_0*rho_1)*ue*surf;	
				//				dma=dma*factor;
				//This makes nearly no differnce!
				//dma=C_Mix*rho_0*ue*surf; 				

				//check if gasoline zone still conains enough mass
				if(zonen_IN[nbrOfZones-1].get_m()-CP.SYS.MINIMUM_ZONE_MASS 
						>=(dmaTot+dma)*CP.SYS.WRITE_INTERVAL_SEC
						&& zonen_IN[nbrOfZones-1].get_m()>=CP.SYS.MINIMUM_ZONE_MASS){
					dmaTot=dmaTot+dma;					
					zonen_IN[ax*nbrOfRadPackets+ rad].set_dm_in(dma, 
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
			}while(ax<nbrOfAxPackets);	
			ax=-Integer.MAX_VALUE;
			rad=-Integer.MAX_VALUE;

			for (int a=0;a<nbrOfAxPackets;a++){			
				for(int r=0;r<nbrOfRadPackets-1; r++){

					Zone z0=zonen_IN[a*nbrOfRadPackets+r];
					Zone z1=zonen_IN[a*nbrOfRadPackets+r+1];
					double dm=this.calc_dm(z0, z1, time);

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
					z0.set_dm_in(dm, z1.get_T(), z1.get_gasMixtureZone());
					try {
						z1.set_dm_out(dm);
					} catch (NegativeMassException e) {
						e.log_Warning("Zone "+ (a*nbrOfRadPackets+r+1) +" is empty! Something went wrong");
						//Passiert eh nicht!!
					}

//					diffusion into zone r+1
					z1.set_dm_in(dm,z0.get_T(),z0.get_gasMixtureZone());
					try {
						z0.set_dm_out(dm);
					} catch (NegativeMassException e) {
						e.log_Warning("Zone " + (a*nbrOfRadPackets+r)
								+ " is empty! Something went wrong");
						//Passiert eh nicht!!
					}			
				}
			}		
		}
		return zonen_IN;
	}

	private Zone[] turbMix2_alt(double time, Zone[] zonen_IN){
		Injection es=masterInjection.get_allInjections()[indexOfMainInj];		

		if(time<=es.get_EOI())
			zonen_IN=entrainment(time,zonen_IN);
		else{	
			//zonen_IN=entrainment(time,zonen_IN);
			int nbrOfAxPackets=((Spray) es).get_nbrOfAxPackets();
			int nbrOfRadPackets=((Spray) es).get_nbrOfRadPackets();	
			//constants
			double C_entrain=4, Crad=1,Csurf2=1;
			C_entrain=CP.get_C_Mix();
			double pF=CP.get_C_MixRad(); 			
			Crad=Math.log(pF)/((nbrOfRadPackets-1)*(nbrOfRadPackets-1));	
			double sumdm=0;
			for(int ax=0;ax<nbrOfAxPackets;ax++){
				for(int rad=0; rad<nbrOfRadPackets; rad++){
					int z0=ax*nbrOfRadPackets+rad;
					int z1=ax*nbrOfRadPackets+rad+1;	

					//the outmost packet mixes with the gasoline zone
					if(rad==nbrOfRadPackets-1)
						z1=nbrOfZones-1;

					double V0=zonen_IN[z0].get_V();	
					double m0=zonen_IN[z0].get_m();
					double rho_0=m0/V0;
					double rho_1=zonen_IN[z1].get_m()/zonen_IN[z1].get_V();

					//large eddie length scale (Ramos page 109 eq. 44)	
					//volume combustion chamber
					//					double Vcyl=motor.get_V(time);
					//					double b=((Motor_HubKolbenMotor)motor).get_Bohrung();
					//					double LL=4*Vcyl/Math.PI/b/b;
					//					if(LL>0.5*b)LL=0.5*b;				

					//surface fo the zone that looses the mass
					double surf=Math.pow(V0, 2d/3d);
					//'radius' of the zone 
					//					double rZ=Csurf2*Math.sqrt(surf/Math.PI); 
					double ue=this.tfr.get_TI(time);
					//					ue=ue*Math.pow(rho_0/rho_1, 0.5); //according to Krishnan	
					//					double xx=(1-Math.exp(-rZ/1/LL));
					//					if(xx>0)ue=ue*xx;

					double dm00=C_entrain*Math.sqrt(rho_0*rho_1)*ue*surf;						

					double dm=dm00;	
					sumdm=sumdm+dm;

					//1-> die inneren Zonen werden entleert
					if(CP.get_mixingProcess()==1){
						if(dm*CP.SYS.WRITE_INTERVAL_SEC>=m0 &&m0>CP.SYS.MINIMUM_ZONE_MASS)
							dm=0.75*(m0-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;
						if(m0<=CP.SYS.MINIMUM_ZONE_MASS)
							dm=0;

						try {
							zonen_IN[z0].set_dm_out(dm);
							zonen_IN[z1].set_dm_in(dm, zonen_IN[z0].get_T(), zonen_IN[z0].get_gasMixtureZone());	
						} catch (NegativeMassException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//	2->	entrainment aus der otto zone
					}else if(CP.get_mixingProcess()==2){
						//entrainment aus der otto zone
						z1=nbrOfZones-1;
						if(dm*CP.SYS.WRITE_INTERVAL_SEC>=zonen_IN[z1].get_m() &&zonen_IN[z1].get_m()>CP.SYS.MINIMUM_ZONE_MASS)
							dm=0.75*(zonen_IN[z1].get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;
						if(zonen_IN[z1].get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
							dm=0;

						try {
							zonen_IN[z1].set_dm_out(dm);
							zonen_IN[z0].set_dm_in(dm, zonen_IN[z1].get_T(), zonen_IN[z1].get_gasMixtureZone());	
						} catch (NegativeMassException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}else if(CP.get_mixingProcess()==3){
						//entrainment in die Dieselzonen					
						if(dm*CP.SYS.WRITE_INTERVAL_SEC>=zonen_IN[z1].get_m() && zonen_IN[z1].get_m()>CP.SYS.MINIMUM_ZONE_MASS)
							dm=0.5*(zonen_IN[z1].get_m()-CP.SYS.MINIMUM_ZONE_MASS)/CP.SYS.WRITE_INTERVAL_SEC;
						if(zonen_IN[z1].get_m()<=CP.SYS.MINIMUM_ZONE_MASS)
							dm=0;

						try {
							//							if(dm!=0){
							zonen_IN[z1].set_dm_out(dm);
							zonen_IN[z0].set_dm_in(dm, zonen_IN[z1].get_T(), zonen_IN[z1].get_gasMixtureZone());	
							//							}
						} catch (NegativeMassException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}else{
						try{
							throw new ParameterFileWrongInputException("The variable mixingProcess has a wrong value. " +
									"Possible values are: 0,1,2 and 3! ");
						}catch(ParameterFileWrongInputException pfe){
							pfe.stopBremo();
						}
					}
				}			
			}
			System.out.println(sumdm);
		}	
		return zonen_IN;
	}



	private double [][][] get_r_l_A_Packets(Zone zn[], double time){
		
		double [][][] rlA=new double [axPackets][3][radPackets];
		Spray spray=((Spray) masterInjection.get_allInjections()[indexOfMainInj]);

		for(int axialIndex=0; axialIndex< this.axPackets; axialIndex++){
			double lp=-5;
			if(axialIndex<this.axPackets-1){
				Packet_Hiroyasu p0=	spray.get_packet(axialIndex, 0);
				Packet_Hiroyasu p1=	spray.get_packet(axialIndex+1, 0);
				lp=p0.get_s(time)-p1.get_s(time);
			}else{
				Packet_Hiroyasu p0=	spray.get_packet(axialIndex, 0);
				lp=p0.get_v0()*p0.get_packetCreationTime();
			}	
			rlA[axialIndex][1][0]=lp;
			rlA[axialIndex][0][0]=Math.sqrt(zn[axialIndex*radPackets].get_V()/Math.PI/lp);
			rlA[axialIndex][2][0]=Math.PI*2*rlA[axialIndex][0][0]*lp;
			lp=-5;
			for(int radialIndex=1; radialIndex< this.radPackets; radialIndex++){				
				if(axialIndex<this.axPackets-1){
					Packet_Hiroyasu p0=	spray.get_packet(axialIndex, radialIndex);
					Packet_Hiroyasu p1=	spray.get_packet(axialIndex+1, radialIndex);
					lp=p0.get_s(time)-p1.get_s(time);
				}else{
					Packet_Hiroyasu p0=	spray.get_packet(axialIndex, radialIndex);
					lp=p0.get_v0()*p0.get_packetCreationTime();
				}	
				rlA[axialIndex][1][radialIndex]=lp;
				double r0=rlA[axialIndex][0][radialIndex-1];				
				double vol_1=zn[axialIndex*radPackets+radialIndex].get_V();						
				rlA[axialIndex][0][radialIndex]=Math.sqrt((vol_1+Math.PI*r0*r0*lp)/Math.PI/lp);
				rlA[axialIndex][2][radialIndex]=	2*Math.PI*rlA[axialIndex][0][radialIndex]*lp;
			}		
		} 
		return rlA;
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
		if(CP.get_mixingProcess()==0)
			zonen_IN=turbMix0(time, zonen_IN);
		else if(CP.get_mixingProcess()==1)
//			zonen_IN=turbMix1(time, zonen_IN);
			zonen_IN=turbMix1_b(time, zonen_IN);
		else if(CP.get_mixingProcess()==2)
			zonen_IN=turbMix2(time, zonen_IN);
		else if(CP.get_mixingProcess()==3)
//			zonen_IN=turbMix3_b(time, zonen_IN);
			zonen_IN=turbMix3_c(time, zonen_IN);
//			zonen_IN=turbMix3_d(time, zonen_IN);
			
		else if(CP.get_mixingProcess()==4)
			zonen_IN=turbMix4(time, zonen_IN);
		else if(CP.get_mixingProcess()==5)
			zonen_IN=this.entrainment(time, zonen_IN);
		else if(CP.get_mixingProcess()==6)
			zonen_IN=this.entrainmentKrishnan(time, zonen_IN);
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
		super.buffer_singleResult("L_M [m/s]", this.turb.get_turbulenceIntegralLengthScale(zn,time),i);

		i+=1;
		super.buffer_singleResult("dmMixTot [%]", deltaMixTot/sum_m,i);

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

