package berechnungsModule.Berechnung;

import io.MultiZoneInitFileReader;
import io.TurbulenceFileReader;

import java.io.File;
import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import matLib.DglSysFunction;
import matLib.RungeKutta;
import misc.VektorBuffer;
import berechnungsModule.SolverConnector;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import berechnungsModule.turbulence.TurbulenceModelFactory;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;

public class APR_CanteraMultiZoneHomogeneous extends APR_Cantera {

	private Motor motor;
	private WandWaermeUebergang wandWaermeModell;
	private MasterEinspritzung masterEinspritzung;
	private Zone [] initialZones;

	private IndizierDaten indiD;
	private VektorBuffer T_buffer;
	private VektorBuffer dQb_buffer;
	private VektorBuffer dQw_buffer;
	private VektorBuffer dmb_buffer;
	private double fortschritt=0;
	private double QbMax,mGes=0, QwOld=0;	
	private boolean compareToExp=false;
	private int nbrOfZones;
	private double mfN_0;
	private double mfC_0;
	private double dVz[];
	private double Vz_old[];
	private TurbulenceFileReader tfr=null;
	private SolverConnector mpi;
	

	protected APR_CanteraMultiZoneHomogeneous(CasePara cp) {		
		super(cp);

		if(CP.compareToExp()){
			indiD=new IndizierDaten(cp);
			compareToExp=true;
		}					

		motor = CP.MOTOR;
		wandWaermeModell=CP.WAND_WAERME;	
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;			

		T_buffer = new VektorBuffer(cp);
		dQb_buffer = new VektorBuffer(cp);	
		dQw_buffer = new VektorBuffer(cp);	
		dmb_buffer = new VektorBuffer(cp);
		this.mpi=new SolverConnector(2);
		//initial Values for the parameters to be integrated
		mpi.set_modelParameter2Integrate(0, 0);//dQb
		mpi.set_modelParameter2Integrate(0, 1);//dQw
		CP.SOLVER.connect2Solver(mpi);

		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////			
		//		initialZones=this.initReadInHeatReleaseProfile();
		//		initialZones=this.initValidateCT();
		initialZones=this.initFromFile();	
		checkEinspritzungen(masterEinspritzung); 		
	}

	private Zone [] init(){
		Zone [] zn=new Zone[nbrOfZones];
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
		double mKrstDampfINIT=
				masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0); 
		double mINIT= mVerbrennungsLuft+mKrstDampfINIT;
		Spezies krst=
				masterEinspritzung.get_spezKrst_verdampft(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0);
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	

		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();

		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mINIT);
		frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mINIT);		

		GasGemisch gemischINIT=new GasGemisch("GemischINIT");	
		gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	

		//Anfangsbedingungen Setzen
		//p Init
		double p_init=indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		//V Init
		double V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		//T Init		
		Spezies ggZone_init=gemischINIT;
		double R=ggZone_init.get_R();
		double T_init=(p_init*V_init)/(mINIT*R);	
		//T_init=T_init+48;
		p_init=mINIT*R*T_init/V_init;
		//unverbrannte Zone
		zn[0]=new Zone(CP,p_init, V_init, T_init, 
				mINIT,ggZone_init , false,0);

		QbMax=masterEinspritzung.get_mKrst_Sum_ASP()
				*masterEinspritzung.get_spezKrstALL().get_Hu_mass();		

		mGes=masterEinspritzung.get_mKrst_Sum_ASP()+mVerbrennungsLuft;

		return zn;		
	}


	private Zone [] initCompareToCycleSymSK(){
		nbrOfZones=1;
		Zone [] zn=new Zone[nbrOfZones];
		double [] y=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];

		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0036814;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.17458;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.74012;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.044454;		
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.020201;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.016964;

		double mINIT=0.00096818;
		mGes=mINIT;
		//mINIT=mINIT-0.0036814*mINIT;
		double T_init=345.79;
		double p_init=2.1169*1e5;
		double V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		double V_SK=0.00044958;
		System.out.println((V_init-V_SK)/V_SK*100);	

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();

		double sum=0;
		for(int j=0;j<y.length;j++)sum=sum+y[j];
		for(int j=0;j<y.length;j++)y[j]=y[j]/sum;

		for(int j=0;j<y.length;j++) ht.put(CP.SPEZIES_FABRIK.get_Spez(j), y[j]);		

		GasGemisch spec=new GasGemisch("CanteraBurned_");
		spec.set_Gasmischung_massenBruch(ht);
		double R=spec.get_R();
		double a=T_init-p_init*V_init/mINIT/spec.get_R();
		for(int i=0;i<this.nbrOfZones;i++){
			zn[i]=new Zone(CP,p_init, V_init/this.nbrOfZones, T_init, 
					mINIT/this.nbrOfZones,spec , false,i);
		}
		return zn;		
	}


	private Zone [] initValidateCT(){

		double m=0.005;	
		this.mGes=m;
		nbrOfZones=3;
		Zone [] zn=new Zone[nbrOfZones];		
		double [][] compVec = new double [3][CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		double p[]=new double [3];
		double T[]=new double [3];

		p[0]=5e5;
		p[1]=5e5;
		p[2]=5e5;		

		T[0]=300;
		T[1]=500;
		T[2]=900;


		double [] y0=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		y0[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=1.0;
		compVec[0]=y0;


		double [] y1=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		y1[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=1.0;
		compVec[1]=y1;

		double [] y2=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];
		y2[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0036814;
		y2[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.17458;
		y2[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.74012;
		y2[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.044454;		
		y2[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.020201;
		y2[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.016964;
		//y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_soot())]=1;		
		compVec[2]=y2;	

		double VGes=0;
		for(int i=0;i<nbrOfZones;i++){

			double [] y=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];	
			y=compVec[i];
			double sum=0;
			for(int j=0;j<y.length;j++) sum=sum+y[j];
			for(int j=0;j<y.length;j++) y[j]=y[j]/sum;

			Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
			for(int j=0;j<y.length;j++) ht.put(CP.SPEZIES_FABRIK.get_Spez(j), y[j]);

			GasGemisch spec=new GasGemisch("CanteraBurned_"+i);
			spec.set_Gasmischung_molenBruch(ht);

			double T_=T[i];
			double p_=p[i];
			double m_=m/nbrOfZones;
			double V=m_*spec.get_R()*T_/p_;
			VGes=VGes+V;
			zn[i]=new Zone(CP,p_, V, T_,m_,spec , false,i);		
		}	
		return zn;		
	}


	private Zone [] initReadInHeatReleaseProfile(){			
		this.nbrOfZones=1;
		Zone [] zn=new Zone[nbrOfZones];
		double [] y=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];

		double nO2=2.30E-01;  
		double nN2=7.57E-01;
		double nCO2=0.0;
		double nH2O=0.0;
		double niC8H18=1.26E-02; 
		double nC7H16=0;
		double mINIT=0.5564241E-3;	//aus KIVA-Ergebnissen
		double p_init=0.123*1E6;
		mGes=0.559350393*1e-3; //aus KIVA-Ergebnissen

		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=nC7H16;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=nO2;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=nN2;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=nCO2;		
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=nH2O;
		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=niC8H18;	

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
		double sum=0;
		for(int j=0;j<y.length;j++)sum=sum+y[j];
		for(int j=0;j<y.length;j++)y[j]=y[j]/sum;

		for(int j=0;j<y.length;j++){			
			ht.put(CP.SPEZIES_FABRIK.get_Spez(j), y[j]);
		}			
		GasGemisch spec=new GasGemisch("CanteraBurned_");
		spec.set_Gasmischung_massenBruch(ht);		

		double V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);	
		double R=spec.get_R(); 
		double T_init=p_init*V_init/mINIT/R;		


		for(int i=0;i<this.nbrOfZones;i++){
			zn[i]=new Zone(CP,p_init, V_init/this.nbrOfZones, T_init, 
					mINIT/this.nbrOfZones,spec , false,i);
		}
		this.QbMax=429;
		return zn;		
	}	

	private Zone [] initFromFile(){	
		File file=CP.get_FileToRead("multiZoneInitFile");	
		MultiZoneInitFileReader mzr=new MultiZoneInitFileReader(file.getAbsolutePath(),CP);		
		this.nbrOfZones=mzr.get_nbrOfZonesInFile();
		super.dQburn=new double [nbrOfZones];	
		super.dQburnVol=new double [nbrOfZones];
		super.dQburnU=new double [nbrOfZones];
		super.dQburnHf=new double [nbrOfZones];
		Zone [] zn=new Zone[nbrOfZones];
		double Q1=0; //initial Energy in the combustion chamber
		double mINIT=0;
		double VTotKiva=0;
		double VZoneMax=Double.NEGATIVE_INFINITY;
		int idxVZoneMax=-5;
		for(int i=0;i<nbrOfZones;i++){
			double p_V_T_mi[]=mzr.get_pVTmi(i);
			zn[i]=new Zone(CP,p_V_T_mi,false,i);
			Q1=Q1+zn[i].get_ggZone().get_Hu_mass()*zn[i].get_m();
			mINIT=mINIT+zn[i].get_m();
			VTotKiva=VTotKiva+zn[i].get_V();
			if(zn[i].get_V()>VZoneMax){
				VZoneMax=zn[i].get_V();
				idxVZoneMax=i;
			}
		}

		double V_mot=this.motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		double chkV=Math.abs((V_mot-VTotKiva)/V_mot);
		if(chkV>1e-3){
			try{
				throw new MiscException("The volumes of all zones does not sum up " +
						"to the volume of the combustion chamber! chkV: "+ chkV+ "%");
			}catch(MiscException me){
				me.stopBremo();
			}
		}
		//adjusting the volume
		double deltaV=V_mot-VTotKiva;
		double V_new=zn[idxVZoneMax].get_V()+deltaV;
		double T_new=zn[idxVZoneMax].get_p()*V_new/zn[idxVZoneMax].get_m()/zn[idxVZoneMax].get_ggZone().get_R();
		double p_V_T_mi_temp[]=zn[idxVZoneMax].get_p_V_T_mi();
		p_V_T_mi_temp[1]=V_new;
		p_V_T_mi_temp[2]=T_new;
		zn[idxVZoneMax].set_p_V_T_mi(p_V_T_mi_temp);
		//chk if everything is ok now!
		double VTotKivaNew=0;
		for(int i=0;i<this.nbrOfZones;i++)VTotKivaNew=VTotKivaNew+zn[i].get_V();
		double chkV2=Math.abs((V_mot-VTotKivaNew)/V_mot);
		if(chkV2>1e-6){
			try{
				throw new MiscException("The volumes of all zones does not sum up " +
						"to the volume of the combustion chamber! chkV: "+ chkV);
			}catch(MiscException me){
				me.stopBremo();
			}
		}	

		//determination of total energy	
		double Q2=0;
		int nbrOfInj=CP.MASTER_EINSPRITZUNG.get_AllInjections().length;
		for(int i=0;i<nbrOfInj;i++){
			double m=CP.MASTER_EINSPRITZUNG.get_Einspritzung(i).get_mKrst_ASP();
			double lhv=CP.MASTER_EINSPRITZUNG.get_Einspritzung(i).get_Krst().get_Hu_mass();
			Q2=Q2+m*lhv;
		}
		this.QbMax=Q1+Q2;	
		//total mass (gasoline mixture + all injections)
		this.mGes=mINIT+CP.MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP();



		//find leanest and richest zone	
		int idxOfLeanestZone=-5;
		int idxOfRichestZone=-5;
		double lambdaMin=Double.POSITIVE_INFINITY;
		double lambdaMax=Double.NEGATIVE_INFINITY;
		for(int i=0;i<this.nbrOfZones;i++){
			if (zn[i].get_ggZone().get_lambda()<lambdaMin){
				lambdaMin=zn[i].get_ggZone().get_lambda();
				idxOfRichestZone=i;				
			}
			if (zn[i].get_ggZone().get_lambda()>lambdaMax){
				lambdaMax=zn[i].get_ggZone().get_lambda();
				idxOfLeanestZone=i;				
			}
		}

		if(idxOfRichestZone!=0||idxOfLeanestZone!=this.nbrOfZones-1){
			try{
				throw new ParameterFileWrongInputException("The zones in the " +
						"input file must be ordered from rich to lean");
			}catch(ParameterFileWrongInputException pfe){
				pfe.stopBremo();
			}
		}
		//defining the mixture without Diesel
		Spezies gasMix=zn[idxOfLeanestZone].get_ggZone();
		double n0=gasMix.get_AnzC_Atome()
				+gasMix.get_AnzH_Atome()
				+gasMix.get_AnzN_Atome()
				+gasMix.get_AnzO_Atome();
		//molefractions of N and C in the mixture without Diesel aka gasMix
		mfN_0=gasMix.get_AnzN_Atome()/n0;
		mfC_0=gasMix.get_AnzC_Atome()/n0;
		dVz=new double [this.nbrOfZones];
		Vz_old=new double [this.nbrOfZones];		
		return zn;		
	}	


	@Override
	public boolean initialiseBurntZone() {		
		return false;
	}


	@Override
	protected Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {		
	
		//Wall heat losses
		double dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
		dQw=CP.get_whtfMult()*dQw;
		//		dQw=0;
		

		double dQwchk=0;
	
		double V=0;
		for(int i=0;i<this.nbrOfZones;i++)V+=zonen_IN[i].get_V();
		for(int i=0;i<this.nbrOfZones;i++){			
			double dQwi=dQw*zonen_IN[i].get_V()/V;
			dQwchk+=dQwi;
			zonen_IN[i].set_dQ_ein_aus(-1*dQwi);					
		}
//		double Vchk=motor.get_V(time);	
//		System.out.println((Vchk-V)/Vchk*100);
		//		double dQwchk=0;
		//		double sumV_23=0;
		//		double exp= 2.0/3.0;
		//		for(int i=0;i<this.nbrOfZones;i++){
		//			sumV_23=sumV_23+Math.pow(zonen_IN[i].get_V(),exp);	
		//		}
		//		for(int i=0;i<this.nbrOfZones;i++){
		//			double dQwi=dQw*Math.pow(zonen_IN[i].get_V(),exp)/sumV_23;
		//			dQwchk+=dQwi;
		//			zonen_IN[i].set_dQ_ein_aus(-1*dQwi);					
		//		}

		//		double denom=0;
		//		double b=2.0/3.0;
		//		double Twall=CP.get_T_Wand();
		//		for(int i=0;i<this.nbrOfZones;i++){
		//			double a=Math.pow(zonen_IN[i].get_V(), b);			
		//			denom=denom+a*(zonen_IN[i].get_T()-Twall);			
		//		}		
		//
		//		double dQwchk=0;
		//		for(int i=0;i<this.nbrOfZones;i++){
		//			double a=Math.pow(zonen_IN[i].get_V(), b);	
		//			double factor=a*(zonen_IN[i].get_T()-Twall)/denom;
		//			double dQwi=dQw*factor;
		//			dQwchk+=dQwi;
		//			zonen_IN[i].set_dQ_ein_aus(-1*dQwi);
		//		}

		//		double dQwchk=0;
		//		double sumDelta_T=0;
		//		double Twall=CP.get_T_Wand();
		//		for(int i=0;i<this.nbrOfZones;i++){
		//			sumDelta_T=sumDelta_T+(zonen_IN[i].get_T()-Twall);			
		//		}
		//		
		//		for(int i=0;i<this.nbrOfZones;i++){
		//			double dQwi=dQw*(zonen_IN[i].get_T()-Twall)/sumDelta_T;
		//			dQwchk+=dQwi;
		//			zonen_IN[i].set_dQ_ein_aus(-1*dQwi);					
		//		}
		//
		if(Math.abs((dQwchk-dQw)/dQw)>5e-5){
			try{
				throw new BirdBrainedProgrammerException("Fu**");
			}catch(BirdBrainedProgrammerException bpe){
				bpe.stopBremo();
			}
		}

		//		zonen_IN[zonen_IN.length-1].set_dQ_ein_aus(-1*dQw);	

		//		zonen_IN[0].set_dQ_ein_aus(-1*dQw);	

		//		for(int i=0;i<3;i++){			
		//			zonen_IN[i].set_dQ_ein_aus(-1*dQw/3d);					
		//		}
		//		

		//Mixing
//		this.mixing_9(zonen_IN, time);
//		this.mixDiffusion_b(zonen_IN, time);

		//Verdampfungswaerme abfuehren
		zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);		

		//Einspritzung des Kraftstoffs
		zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);			
		
		this.mpi.set_diff_modelParameter2Integrate(super.dQburnSumU, 0);
		this.mpi.set_diff_modelParameter2Integrate(dQw, 1);
		return zonen_IN;				
	}

	private double calc_dm(Zone z, double time){
		//return calc_dm1(z,time);
		return calc_dm_TI(z,time);
	}


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

	private double calc_dm2(Zone z,double mTot, int idxZone, double time){

		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);

		double mfD=this.calc_MassFractionnC7H16(z.get_ggZone());
		if(mfD<0){
			mfD=0;
			System.out.println("FUCK YOU: "+mfD);	
		}	

		double mZone=z.get_m();
		double mD=mZone*mfD;
		double rho=mZone/z.get_V();

		//integral length scale
		double tI=2.0/3.0*tfr.get_TKE(time)/this.tfr.get_EPS(time);
		double c1=CP.get_C_Mix();

		double dcD=-c1*rho*mfD/tI;
		double b=mD/z.get_V()*dVz[idxZone];
		double dmD=dcD*z.get_V();	
		if(dmD>0)
			dmD=0;	
		
		double dm=0;
			dm=dmD/mfD;
//		dm=-1*z.get_m()*0.01/CP.SYS.WRITE_INTERVAL_SEC;
		return -1*dm;

	}


	private double calc_dm_TI(Zone z0, double time){

		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);

		double V0=z0.get_V();	
		double m0=z0.get_m();
		double rho_0=m0/V0;

		//volume combustion chamber
		double Vzyl=motor.get_V(time);
		double b=((Motor_HubKolbenMotor)motor).get_Bohrung();
		double LL=4*Vzyl/Math.PI/b/b;
		if(LL>0.5*b)LL=0.5*b;								
		//charachteristic length scale
		double C4=CP.get_C_MixRad();	
		double lc=C4*Math.pow(V0, (1d/3d));	
		double factor=Math.exp(-1d*(lc/LL-1.0));	
		//eigentlich richtig! Vielleicht statt der eins eine 0.005 oder so
		factor=Math.exp(-.0050*(lc/LL-1.0)*(lc/LL-1.0));	
//		factor=1;
		//surface 
		double surf=Math.pow(V0, 2d/3d);		
		double ue=this.tfr.get_TI(time);
		double C_Mix=CP.get_C_Mix();
		double dma=C_Mix*rho_0*ue*surf;	
		dma=dma*factor;
		return dma;

	}





	private Zone [] mixing_7(Zone [] zn, double time){

		//mixxing of the zones
		for(int i=1;i<this.nbrOfZones-1;i++){	
			double exp=2.0/3.0;
			double sum=Math.pow(zn[i-1].get_V(),exp)+Math.pow(zn[i].get_V(), exp);
			double f1=Math.pow(zn[i].get_V(),exp)/sum;
			double f0=Math.pow(zn[i-1].get_V(), exp)/sum;

			double dm=this.calc_dm(zn[i], time);		
			zn[i-1].set_dm_ein(dm*f0, zn[i].get_T(), zn[i].get_ggZone());
			zn[i+1].set_dm_ein(dm*f1, zn[i].get_T(), zn[i].get_ggZone());

			try {
				zn[i].set_dm_aus(dm);
			} catch (NegativeMassException e) {
				//does not happen
				e.stopBremo();
			}			
		}
		// mass flux from the rich zone --> idx= 0
		try {
			double dm=this.calc_dm(zn[0], time);
			zn[0].set_dm_aus(dm);
			zn[1].set_dm_ein(dm, zn[0].get_T(), zn[0].get_ggZone());
		} catch (NegativeMassException e) {
			//does not happen
			e.stopBremo();
		}
		// mass flux from the lean zone --> idx= nbrOfZones-1
		try {
			double dm=this.calc_dm(zn[this.nbrOfZones-1], time);
			zn[this.nbrOfZones-1].set_dm_aus(dm);
			zn[this.nbrOfZones-2].set_dm_ein(dm, zn[this.nbrOfZones-1].get_T(), zn[this.nbrOfZones-1].get_ggZone());
		} catch (NegativeMassException e) {
			//does not happen
			e.stopBremo();
		}		
		return zn;
	}

	private Zone [] mixing_8(Zone [] zn, double time){
			int nbrOfCouples=(int)(this.nbrOfZones/2);
			//integral length scale
			if(this.tfr==null)
				tfr=new TurbulenceFileReader(CP);
			double tI=2.0/3.0*tfr.get_TKE(time)/this.tfr.get_EPS(time);
			double c=CP.get_C_Mix();
			
				for(int i=0;i<nbrOfCouples;i++){
					int znIDX0=i*2;
					int znIDX1=znIDX0+1;
					double rho0=zn[znIDX0].get_rho();
					double rho1=zn[znIDX1].get_rho();
					double phi0=1/zn[znIDX0].get_ggZone().get_lambda();
					double phi1=1/zn[znIDX1].get_ggZone().get_lambda();
					double dm0=c*rho0*phi0/tI*zn[znIDX0].get_V();
					double dm1=c*rho1*phi1/tI*zn[znIDX1].get_V();
					
					try {
						zn[znIDX0].set_dm_aus(dm0);
						zn[znIDX1].set_dm_ein(dm0, zn[znIDX0].get_T(), zn[znIDX0].get_ggZone());
					} catch (NegativeMassException e) {						
						e.printStackTrace();
					}					
					try {
						zn[znIDX1].set_dm_aus(dm1);
						zn[znIDX0].set_dm_ein(dm1, zn[znIDX1].get_T(), zn[znIDX1].get_ggZone());
					} catch (NegativeMassException e) {						
						e.printStackTrace();
					}
					
				}

		return zn;
	}

	private Zone [] mixing_9(Zone [] zn, double time){
		double mTot=0;
		for(int i=0;i<this.nbrOfZones;i++)mTot=mTot+zn[i].get_m();
		//mixxing of the zones
		for(int i=1;i<this.nbrOfZones-1;i++){	
//			System.out.println(i);
			int i0=i-1;
			int i2=i+1;
			double mf0=this.calc_MassFractionnC7H16(zn[i0].get_ggZone());
			double mf=this.calc_MassFractionnC7H16(zn[i].get_ggZone());
			double mf2=this.calc_MassFractionnC7H16(zn[i2].get_ggZone());
			double f2=0, f0=0;			
			if(mf>mf0&&mf>mf2){
				double exp=2.0/3.0;
				double sum=Math.pow(zn[i0].get_V(),exp)+Math.pow(zn[i].get_V(), exp);
				f2=Math.pow(zn[i].get_V(),exp)/sum;//masse stroemt durch die oberflaeche von Zone 1
				f0=Math.pow(zn[i0].get_V(), exp)/sum;
				if(f0+f2!=1){
					try {
						throw new BirdBrainedProgrammerException("f2+f0 ergibt nicht 1");
					} catch (BirdBrainedProgrammerException e) {
						//does not happen
						e.stopBremo();
					}
				}				
			}			
			if(mf>mf0&&mf<mf2){
				f2=0;
				f0=1;				
			}			
			if(mf<mf0&&mf>mf2){
				f2=1;
				f0=0;				
			}

			double dm=this.calc_dm2(zn[i], mTot, i, time);			
			zn[i0].set_dm_ein(dm*f0, zn[i].get_T(), zn[i].get_ggZone());
			zn[i2].set_dm_ein(dm*f2, zn[i].get_T(), zn[i].get_ggZone());

			try {
				zn[i].set_dm_aus((f0+f2)*dm);
			} catch (NegativeMassException e) {
				//does not happen
				e.stopBremo();
			}

		}

		//inner zone
		double mf=this.calc_MassFractionnC7H16(zn[0].get_ggZone());
		double mf2=this.calc_MassFractionnC7H16(zn[1].get_ggZone());
		double dm=this.calc_dm2(zn[0], mTot, 0, time);
		if(mf>mf2){
			zn[1].set_dm_ein(dm, zn[0].get_T(), zn[0].get_ggZone());
			try {
				zn[0].set_dm_aus(dm);
			} catch (NegativeMassException e) {
				//does not happen
				e.stopBremo();
			}
		}

		//outer zone
		double mfN_1=this.calc_MassFractionnC7H16(zn[this.nbrOfZones-2].get_ggZone());
		double mfN=this.calc_MassFractionnC7H16(zn[this.nbrOfZones-1].get_ggZone());
		double dmN=this.calc_dm2(zn[this.nbrOfZones-1], mTot, this.nbrOfZones-1, time);
		if(mfN>mfN_1){
			zn[this.nbrOfZones-2].set_dm_ein(dmN, zn[this.nbrOfZones-1].get_T(), zn[this.nbrOfZones-1].get_ggZone());
			try {
				zn[this.nbrOfZones-1].set_dm_aus(dmN);
			} catch (NegativeMassException e) {
				//does not happen
				e.stopBremo();
			}
		}		
		return zn;
	}


	private Zone [] mixing_10(Zone [] zn, double time){
		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);
		double c_Mix=CP.get_C_Mix();
		//integral time scale
		double tI=2.0/3.0*tfr.get_TKE(time)/this.tfr.get_EPS(time);
		//mixxing of the zones
		for(int i=1;i<this.nbrOfZones-1;i++){		
			int i0=i-1;
			int i2=i+1;				
			double V0=zn[i0].get_V();
			double V1=zn[i].get_V();
			double V2=zn[i2].get_V();
			if(V1<V0&&V1<V2){
				//do nothing
			}else if(V1<V2&&V1>V0){				
				double deltaV=V1-V0;	
				double rho=zn[i].get_m()/V1;
				double dm=c_Mix*deltaV/tI*rho;			
				zn[i0].set_dm_ein(dm, zn[i].get_T(), zn[i].get_ggZone());
				try {
					zn[i].set_dm_aus(dm);
				} catch (NegativeMassException e) {
					//does not happen
					e.stopBremo();
				}				
			}else if(V1>V2&&V1<V0){				
				double deltaV=V1-V2;	
				double rho=zn[i].get_m()/V1;
				double dm=c_Mix*deltaV/tI*rho;			
				zn[i2].set_dm_ein(dm, zn[i].get_T(), zn[i].get_ggZone());
				try {
					zn[i].set_dm_aus(dm);
				} catch (NegativeMassException e) {
					//does not happen
					e.stopBremo();
				}				
			}else if(V1>V2&&V1>V0){				
				double deltaV1=V1-V0;	
				double deltaV2=V1-V2;
				double deltaV=0.5*(deltaV1+deltaV2);
				double rho=zn[i].get_m()/V1;
				double dm=c_Mix*deltaV/tI*rho;	
				double f2=0, f0=0;	
				f2=deltaV2/(2*deltaV);
				f0=deltaV1/(2*deltaV);
				if(f0+f2!=1){
					try {
						throw new BirdBrainedProgrammerException("f2+f0 ergibt nicht 1");
					} catch (BirdBrainedProgrammerException e) {
						//does not happen
						e.stopBremo();
					}
				}
				zn[i0].set_dm_ein(f0*dm, zn[i].get_T(), zn[i].get_ggZone());
				zn[i2].set_dm_ein(f2*dm, zn[i].get_T(), zn[i].get_ggZone());
				try {
					zn[i].set_dm_aus(dm);
				} catch (NegativeMassException e) {
					//does not happen
					e.stopBremo();
				}				
			}
		}



		//inner zone
		double V0=zn[0].get_V();
		double V1=zn[1].get_V();
		if(V0>V1){
			double deltaV=V0-V1;	
			double rho=zn[0].get_m()/V0;
			double dm=c_Mix*deltaV/tI*rho;			
			zn[1].set_dm_ein(dm, zn[0].get_T(), zn[0].get_ggZone());
			try {
				zn[0].set_dm_aus(dm);
			} catch (NegativeMassException e) {
				//does not happen
				e.stopBremo();
			}				
		}

		//inner zone
		double Vn=zn[this.nbrOfZones-1].get_V();
		double Vn_1=zn[this.nbrOfZones-2].get_V();
		if(Vn>Vn_1){
			double deltaV=Vn-Vn_1;	
			double rho=zn[this.nbrOfZones-1].get_m()/Vn;
			double dm=c_Mix*deltaV/tI*rho;			
			zn[this.nbrOfZones-2].set_dm_ein(dm, zn[this.nbrOfZones-1].get_T(), zn[this.nbrOfZones-1].get_ggZone());
			try {
				zn[this.nbrOfZones-1].set_dm_aus(dm);
			} catch (NegativeMassException e) {
				//does not happen
				e.stopBremo();
			}				
		}

		return zn;
	}	
	
	private Zone [] mixing_11(Zone [] zn, double time){		
		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);
		double tI=2.0/3.0*tfr.get_TKE(time)/this.tfr.get_EPS(time);
		double c=CP.get_C_Mix(); //3.25 war ganz gut!
		for(int i=1;i<zn.length; i++){
			double dm=c*zn[i].get_m()/tI;			
			try {
				zn[i-1].set_dm_ein(dm, zn[i].get_T(), zn[i].get_ggZone());
				zn[i].set_dm_aus(dm);
			} catch (NegativeMassException e) {
				e.printStackTrace();
			}
		}		
		return zn;
	}
	
	private Zone [] mixing_12(Zone [] zn, double time){		
		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);
		double tI=2.0/3.0*tfr.get_TKE(time)/this.tfr.get_EPS(time);
		double c=CP.get_C_Mix();
		double dmTot=c*zn[zn.length-1].get_m()/tI;	
		double rOut=zn.length-2; //idx of outmost zone that has entrainment
		double cMixRad=10; //ratio of entrainment into the outmost zone to that into the inner zone
		double D=Math.pow(tfr.get_TKE(time), 2)/this.tfr.get_EPS(time); //Diff Coeff
		double a=Math.log(cMixRad)/rOut/rOut;
		//Abwandlung
		//a=a/(1+time);
		double sum=0;
		for(int i=0;i<zn.length-1;i++)sum+=Math.exp(a*i*i);	
		
		double dmCHK=0;
		for(int i=0;i<zn.length-1; i++){
			double t=Math.exp(a*i*i)/sum;
			double dm=dmTot*t;			
			dmCHK=dmCHK+dm;
			try {
				zn[i].set_dm_ein(dm, zn[zn.length-1].get_T(), zn[zn.length-1].get_ggZone());
				zn[zn.length-1].set_dm_aus(dm);
			} catch (NegativeMassException e) {
				e.printStackTrace();
			}
		}
		
		if(Math.abs(dmCHK-dmTot)>1e-10){
			try{
				throw new BirdBrainedProgrammerException("F**K" +((dmTot-dmCHK)/dmTot));
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return zn;
	}
	
	private Zone [] mixing_12b(Zone [] zn, double time){		
		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);
		double tI=2.0/3.0*tfr.get_TKE(time)/this.tfr.get_EPS(time);
		double c=CP.get_C_Mix();
		int rOut=-1;
		double dmOutMax=Double.MIN_VALUE;
		for(int i=0;i<zn.length; i++){
			double dmOut=c*zn[i].get_m()/tI;
			if(dmOut>dmOutMax){
				dmOutMax=dmOut;			
				rOut=i;
			}
		}		
		 //idx of outmost zone that has entrainment
		double cMixRad=10; //ratio of entrainment into the outmost zone to that into the inner zone
		double a=Math.log(cMixRad)/rOut/rOut;
		double sum=0;
		for(int i=0;i<rOut;i++)sum+=Math.exp(a*i*i);	

		double dmCHK=0;
		for(int i=0;i<rOut; i++){
			double t=Math.exp(a*i*i)/sum;
			double dm=dmOutMax*t;
			dmCHK=dmCHK+dm;
			try {
				zn[i].set_dm_ein(dm, zn[rOut].get_T(), zn[rOut].get_ggZone());
				zn[rOut].set_dm_aus(dm);
			} catch (NegativeMassException e) {
				e.printStackTrace();
			}
		}		
		if(Math.abs(dmCHK-dmOutMax)>1e-10&&dmCHK>0){
			try{
				throw new BirdBrainedProgrammerException("F**K: " +((dmOutMax-dmCHK)/dmOutMax));
			}catch(BirdBrainedProgrammerException bbpe){
				bbpe.stopBremo();
			}
		}
		return zn;
	}



	private Zone[] mixDiffusion(Zone [] zn, double time){		
		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);

		double eps=this.tfr.get_EPS(time);	//dissipation rate
		double tke=this.tfr.get_TKE(time); 	//turbulence kinetic energy
		double tki=this.tfr.get_TI(time); 	//turbulence intensity

		//diffusion coefficient: D=	tki*lagrangianLengthScale 
		//lagrangianLengthScale taken as integralLengthScale=2/3tke/eps*tki (SAE-2000-01-2810)
		double diffCoeff=2.0/3.0*tki*tki*tke/eps;
		diffCoeff=diffCoeff*CP.get_C_Mix();	
		//zones are treated as circular  slices 
		//outer radius of each zone
		double [] rOut=new double [zn.length];
		//center radius of each zone
		double [] r=new double [zn.length];	

		//Slices
		//		double s=0; //length of each zone
		//		rOut[0]=Math.sqrt(zn[0].get_V()/Math.PI/s); //inner zone is a full slice
		//		for (int i=1; i<zn.length;i++)
		//			rOut[i]=Math.sqrt(zn[i].get_V()/Math.PI/s+rOut[i-1]*rOut[i-1]);

		//Spheres
		rOut[0]=Math.pow(3d*zn[0].get_V()/4d/Math.PI, 1d/3d); //inner zone is a full sphere
		for (int i=1; i<zn.length;i++)
			rOut[i]=Math.pow(3d*zn[i].get_V()/4d/Math.PI+rOut[i-1]*rOut[i-1]*rOut[i-1], 1d/3d);

		r[0]=0.5*rOut[0];
		for (int i=1; i<zn.length;i++) r[i]=0.5*(rOut[i]+rOut[i-1]);

		//loop over all surfaces/zones	
		for (int znIdx=0; znIdx<zn.length-1;znIdx++){		
			int idx0=znIdx;
			int idx1=idx0+1;		
			double[] ck0=zn[idx0].get_ci();				
			double [] ck1=zn[idx1].get_ci();
			//Surface area for diffusion
			//slice
			//			double surf=2*Math.PI*rOut[idx0]*s;

			//sphere
			double surf=4*Math.PI*rOut[idx0]*rOut[idx0];

			//molar current density in [mol/m2/s]
			double [] uJa=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];

			for (int idxSpec=0;idxSpec<uJa.length;idxSpec++){
				double sum_grad_ck=0;
				for (int i=0; i<CP.SPEZIES_FABRIK.get_nbrOfSpecies();i++){
					if(i!=idxSpec){
						double grad_ck = (ck1[i]-ck0[i])/(r[idx1]-r[idx0]);
						sum_grad_ck=sum_grad_ck+diffCoeff*grad_ck;
					}
				}
				uJa[idxSpec]=sum_grad_ck;
			}			
			//mass flows		
			for(int idxSpec=0;idxSpec<CP.SPEZIES_FABRIK.get_nbrOfSpecies();idxSpec++){
				double dmi=CP.SPEZIES_FABRIK.get_Spez(idxSpec).get_M()*uJa[idxSpec]*surf;
				if(dmi>0){
					if(zn[idx0].get_mi()[idxSpec]>dmi*CP.SYS.WRITE_INTERVAL_SEC){
						zn[idx1].set_dm_ein(dmi, zn[idx0].get_T(), CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						try {
							zn[idx0].set_dm_aus(dmi,  CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						} catch (NegativeMassException e) {
							//Does not happen
							e.printStackTrace();
						}
					}
				}else if(dmi<0){
					dmi=-1*dmi;
					if(zn[idx1].get_mi()[idxSpec]>dmi*CP.SYS.WRITE_INTERVAL_SEC){					
						zn[idx0].set_dm_ein(dmi, zn[idx1].get_T(), CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						try {
							zn[idx1].set_dm_aus(dmi,  CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						} catch (NegativeMassException e) {
							//Does not happen
							e.printStackTrace();
						}
					}
				}else{
					//do nothing
				}

			}
		}				
		return zn;
	}
	
	
	private Zone[] mixDiffusion_b(Zone [] zn, double time){		
		if(this.tfr==null)
			tfr=new TurbulenceFileReader(CP);

		double eps=this.tfr.get_EPS(time);	//dissipation rate
		double tke=this.tfr.get_TKE(time); 	//turbulence kinetic energy
		double tki=this.tfr.get_TI(time); 	//turbulence intensity

		//diffusion coefficient: D=	tki*lagrangianLengthScale 
		//lagrangianLengthScale taken as integralLengthScale=2/3tke/eps*tki (SAE-2000-01-2810)
		double diffCoeff=2.0/3.0*tki*tki*tke/eps;
		diffCoeff=diffCoeff*CP.get_C_Mix();	
		//zones are treated as circular  slices 
		//outer radius of each zone
		double [] rOut=new double [zn.length];
		//center radius of each zone
		double [] r=new double [zn.length];	

		//Slices
		//		double s=0; //length of each zone
		//		rOut[0]=Math.sqrt(zn[0].get_V()/Math.PI/s); //inner zone is a full slice
		//		for (int i=1; i<zn.length;i++)
		//			rOut[i]=Math.sqrt(zn[i].get_V()/Math.PI/s+rOut[i-1]*rOut[i-1]);

		//Spheres
		rOut[0]=Math.pow(3d*zn[0].get_V()/4d/Math.PI, 1d/3d); //inner zone is a full sphere
		for (int i=1; i<zn.length;i++)
			rOut[i]=Math.pow(3d*zn[i].get_V()/4d/Math.PI+rOut[i-1]*rOut[i-1]*rOut[i-1], 1d/3d);

		r[0]=0.5*rOut[0];
		for (int i=1; i<zn.length;i++) r[i]=0.5*(rOut[i]+rOut[i-1]);

		//loop over all surfaces/zones	
		for (int znIdx=0; znIdx<zn.length;znIdx++){	
			int idx0 = -5,idx1=-5;
		
			if(znIdx!=0&&znIdx!=zn.length-1){
				idx0=znIdx-1;
				idx1=znIdx+1;		
			} else if(znIdx==0){
				idx0=znIdx;
				idx1=znIdx+1;		
			}else if(znIdx==zn.length-1){
				idx0=znIdx-1;
				idx1=znIdx;
			}
			double[] ck0=zn[idx0].get_ci();				
			double[] ck1=zn[idx1].get_ci();
				
			//Surface area for diffusion
			//slice
			//			double surf=2*Math.PI*r[idx0]*s;

			//sphere
			double surf=4*Math.PI*r[idx0]*r[idx0];

			//molar current density in [mol/m2/s]
			double [] uJa=new double [CP.SPEZIES_FABRIK.get_nbrOfSpecies()];

			for (int idxSpec=0;idxSpec<uJa.length;idxSpec++){
				double sum_grad_ck=0;
				for (int i=0; i<CP.SPEZIES_FABRIK.get_nbrOfSpecies();i++){
					if(i!=idxSpec){
						//mittlerer gradient
						double grad_ck = (ck1[i]-ck0[i])/(r[idx1]-r[idx0]);
						sum_grad_ck=sum_grad_ck+diffCoeff*grad_ck;
					}
				}
				uJa[idxSpec]=sum_grad_ck;
			}			
			//mass flows		
			for(int idxSpec=0;idxSpec<CP.SPEZIES_FABRIK.get_nbrOfSpecies();idxSpec++){
				double dmi=CP.SPEZIES_FABRIK.get_Spez(idxSpec).get_M()*uJa[idxSpec]*surf;
				if(dmi>0&&znIdx!=zn.length-1){
					if(zn[znIdx].get_mi()[idxSpec]>dmi*CP.SYS.WRITE_INTERVAL_SEC){
						zn[idx1].set_dm_ein(dmi, zn[znIdx].get_T(), CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						try {
							zn[znIdx].set_dm_aus(dmi,  CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						} catch (NegativeMassException e) {
							//Does not happen
							e.printStackTrace();
						}
					}
				}else if(dmi<0&&znIdx!=0){
					dmi=-1*dmi;
					if(zn[znIdx].get_mi()[idxSpec]>dmi*CP.SYS.WRITE_INTERVAL_SEC){					
						zn[idx0].set_dm_ein(dmi, zn[znIdx].get_T(), CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						try {
							zn[znIdx].set_dm_aus(dmi,  CP.SPEZIES_FABRIK.get_Spez(idxSpec));
						} catch (NegativeMassException e) {
							//Does not happen
							e.printStackTrace();
						}
					}
				}else{
					//do nothing if dmi==0
				}

			}
		}
		
		
		
		return zn;
	}

	private double calc_MassFractionnC7H16(Spezies dieslGasMix){
		//nbr of moles of gasMix in one mole of dieslGasMix (N is used as tracer species)
		double nMix=dieslGasMix.get_AnzN_Atome()/mfN_0;
		//nbr of C-Atoms that come from Diesel
		double nC_Diesel=dieslGasMix.get_AnzC_Atome()-nMix*mfC_0;
		//nbr molefraction of Diesel in 1 mol of mixture
		double molFrac_Diesel=nC_Diesel/CP.SPEZIES_FABRIK.get_nC7H16().get_AnzC_Atome();			
		double massFracDiesel=molFrac_Diesel*CP.SPEZIES_FABRIK.get_nC7H16().get_M()/dieslGasMix.get_M();		
		return massFracDiesel;
	}


	@Override
	public void bufferErgebnisse(double time, Zone[] zn) {
		
		double QbU=this.mpi.get_modelParameter2Integrate(0);
		double Qw=this.mpi.get_modelParameter2Integrate(1);
		double dQw=(Qw-QwOld)/CP.SYS.WRITE_INTERVAL_SEC;
		dQb_buffer.addValue(time, dQburnSumU);	
		dQw_buffer.addValue(time, dQw);	
		dmb_buffer.addValue(time, 0);

		fortschritt=QbU/QbMax;
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);		

		for(int i=0; i<this.nbrOfZones;i++)dVz[i]=(zn[i].get_V()-Vz_old[i])/CP.SYS.WRITE_INTERVAL_SEC;
		for(int i=0; i<this.nbrOfZones;i++)Vz_old[i]=zn[i].get_V();

		int i=-1;
		i+=1;
		super.buffer_EinzelErgebnis("CrankAngle [°KW]",super.CP.convert_SEC2KW(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("Time [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_EinzelErgebnis("Volume [m3]",motor.get_V(time),i);

		i+=1;
		double VGes=0;
		for(int j=0;j<zn.length;j++)VGes=VGes+zn[j].get_V();
		super.buffer_EinzelErgebnis("Sum_V [m3]",VGes ,i);
		
		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_EinzelErgebnis("T_m [K]",Tm,i);
		T_buffer.addValue(time, Tm);

		double pExp=5;
		if(compareToExp)
			pExp=indiD.get_pZyl(time);
		i+=1;
		super.buffer_EinzelErgebnis("p_exp [bar]", pExp*1E-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("p_" + 0+  " [bar]",zn[0].get_p()*1e-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQbU [J/s]",dQburnSumU,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQbU [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburnSumU),i);


		i+=1;
		super.buffer_EinzelErgebnis("dQbHf [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnSumHf),i);		

		i+=1;		
		super.buffer_EinzelErgebnis("QbU [J]", QbU,i);			

		i+=1;		
		super.buffer_EinzelErgebnis("Qb/QbMax [J/CA]", QbU/QbMax,i);

		i+=1;		
		super.buffer_EinzelErgebnis("dQw [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQw),i);

		i+=1;		
		super.buffer_EinzelErgebnis("Qw [J]", Qw,i);

		i+=1;			
		super.buffer_EinzelErgebnis("Qw/QbMax [-]", Qw/QbMax,i);

		i+=1;
		double mGes=0;
		for(int j=0;j<zn.length;j++)mGes=mGes+zn[j].get_m();
		super.buffer_EinzelErgebnis("Sum_m [kg]",mGes ,i);		

		i+=1;
		super.buffer_EinzelErgebnis("mGesK [kg]", this.mGes,i);	

		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("dQbU_" +k+" [J/CA]",super.CP.convert_ProSEC_2_ProKW(super.dQburnU[k]),i);
		}
		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("T_Zone_" + k + " [K]",zn[k].get_T(),i);
		}

		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("V_" + k + " [-]",zn[k].get_V()/VGes,i);
		}		

		double massfrac []=new double[zn.length];
		for(int iterZn=0; iterZn<zn.length;iterZn++){
			massfrac[iterZn]=zn[iterZn].get_m()/mGes;			
		}		
		double sum=0;
		for(int j=0;j<massfrac.length;j++)sum=sum+massfrac[j];
		for(int j=0;j<massfrac.length;j++)massfrac[j]=massfrac[j]/sum;

		Hashtable<Spezies, Double> ht=new Hashtable<Spezies, Double>();
		for(int iterZn=0; iterZn<zn.length;iterZn++){
			ht.put(zn[iterZn].get_ggZone(), massfrac[iterZn]);			
		}
		GasGemisch gg=new GasGemisch("totalMix");
		gg.set_Gasmischung_massenBruch(ht);

		double lambdaMix=gg.get_lambda();
		i+=1;
		super.buffer_EinzelErgebnis("lamGesMix [-]",lambdaMix,i);

		for(int k=0;k<this.nbrOfZones;k++){	
			i+=1;			
			String name="Lambda_"+k;
			double lambda=zn[k].get_ggZone().get_lambda();
			super.buffer_EinzelErgebnis(name,
					lambda,i);
		}
		

		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("m_" +k+" [kg]", zn[k].get_m(),i);		


			double kontrolMasse=zn[k].get_p()*zn[k].get_V();		
			kontrolMasse=kontrolMasse/(zn[k].get_ggZone().get_R()*zn[k].get_T());
			if(Math.abs(kontrolMasse-zn[k].get_m())/zn[k].get_m()>1e-10){
				try{
					throw new BirdBrainedProgrammerException("The Ideal Gas Equation is not fullfilled! F**K");						
				}catch(BirdBrainedProgrammerException bppe){
					bppe.stopBremo();
				}
			}
			//			i+=1;
			//			super.buffer_EinzelErgebnis("mK_"+k+" [kg]", kontrolMasse,i);
		}

		for(int k=0;k<this.nbrOfZones;k++){	
			i+=1;
			String name="Hu_"+k+ " [J/kg]";			
			super.buffer_EinzelErgebnis(name,
					zn[k].get_ggZone().get_Hu_mass(),i);

		}

		double doh=0; //Degree of heterogeneity
		for(int iterZn=0; iterZn<zn.length;iterZn++){
			double a=zn[iterZn].get_ggZone().get_lambda()-lambdaMix;
			doh=doh+a*a*zn[iterZn].get_m();
		}
		doh=Math.sqrt(doh/mGes)/lambdaMix;

		i+=1;
		super.buffer_EinzelErgebnis("DOH [-]",doh,i);	

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
			double lam=zn[iterZn].get_ggZone().get_lambda();
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
			super.buffer_EinzelErgebnis(name,
					masses[j],i);			
		}	

		for(int k=0;k<this.nbrOfZones;k++){					
			double []mi=zn[k].get_mi();	
			i+=1;
			String name="Z_"+k+"_y_"+CP.SPEZIES_FABRIK.get_Spez(0).get_name();
			super.buffer_EinzelErgebnis(name,
					mi[0]/zn[k].get_m(),i);
			//System.out.println(name+" : "+i);
			
		}		


		//System.out.println("letzte Position: " +i);
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
	public int get_anzZonen() {
		return nbrOfZones;
	}

	@Override
	public VektorBuffer get_dm_buffer() {
		// TODO Auto-generated method stub
		return dmb_buffer;
	}

	@Override
	public VektorBuffer get_dQw_buffer() {
		// TODO Auto-generated method stub
		return this.dQw_buffer;
	}

	@Override
	public VektorBuffer get_dQb_buffer() {
		// TODO Auto-generated method stub
		return this.dQb_buffer;
	}

	@Override
	protected void checkEinspritzungen(MasterEinspritzung me) {
		for(int i=0;i<me.get_AllInjections().length;i++){
			if(me.get_AllInjections()[i].get_ID_Zone()>=nbrOfZones){
				try {
					throw new ParameterFileWrongInputException("Fuel can only be injected in Zones with an index from" +
							" 0 to "+(nbrOfZones-1)+" bur given was: " +
							me.get_AllInjections()[i].get_ID_Zone());
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}
		}		
	}	


	private class PressureEquilibrator implements DglSysFunction{
		private Zone[] zones;
		private int nbrOfZones;			
		private double [] m;
		private double expCoeff;
		private RungeKutta rk;

		public PressureEquilibrator(){	
			rk=new RungeKutta();
		}


		public Zone [] equilibrate(Zone [] z, double pMin, double pMax){			
			this.zones=z;
			nbrOfZones=z.length;
			if(nbrOfZones>1&&pMin!=pMax){
				this.m=new double[nbrOfZones];
				double Vges=0;
				for(int i=0;i<nbrOfZones;i++){
					Vges=Vges+zones[i].get_V();
					m[i]=zones[i].get_m();	
				}		

				expCoeff=Vges/(pMax-pMin)/1; //expansion in 1s with max pressure diff.

				double dt=1/50D; //

				//InitialCOnditions
				double [] V_T=new double[nbrOfZones*2];
				for(int i=0;i<nbrOfZones;i++){
					V_T[i*2]=zones[i].get_V();
					V_T[i*2+1]=zones[i].get_T();				
				}

				rk.setStepSize(dt);
				double pi[]=new double[nbrOfZones];
				boolean isEqualized=false;

				do{		
					double pMax2=0,pMin2=Double.MAX_VALUE;				
					rk.setInitialValueOfX(0);
					rk.setFinalValueOfX(1);			
					rk.setInitialValuesOfY(V_T);
					//V_T=rk.cashKarp(this);					
					V_T=rk.fourthOrder(this);
					for(int i=0;i<nbrOfZones;i++){
						double T=V_T[i*2+1];
						double V=V_T[i*2];
						pi[i]=m[i]*zones[i].get_ggZone().get_R()*T/V;					
						if(pi[i]>pMax2) pMax2=pi[i];
						if(pi[i]<pMin2) pMin2=pi[i];
					}					
					if(Math.abs(pMax2-pMin2)<0.000555)isEqualized=true;				
				}while(!isEqualized);	

				double pMean=0;
				for(int i=0;i<nbrOfZones;i++)pMean=pMean+pi[i]/nbrOfZones;


				for(int i=0;i<nbrOfZones;i++){
					double p_V_T_mi[]=zones[i].get_p_V_T_mi();
					p_V_T_mi[0]=pMean;
					p_V_T_mi[1]=V_T[i*2];
					p_V_T_mi[2]=V_T[i*2+1]; //T will be slightly changed in the Zone according to the eq. of state
					zones[i].set_p_V_T_mi(p_V_T_mi);				
				}				
			}	
			return zones;
		}


		@Override
		public double[] derivn(double x, double[] V_T) {
			double [] dV =new double[nbrOfZones];
			double [] dT=new double [nbrOfZones];
			double [] p=new double [nbrOfZones];			

			for(int i=0;i<nbrOfZones;i++)
				p[i]=m[i]*zones[i].get_ggZone().get_R()*V_T[i*2+1]/V_T[i*2];

			dV[0]=expCoeff*(p[0]-p[1]);			
			for(int i=1;i<nbrOfZones-1;i++)
				dV[i]=expCoeff*(p[i]-p[i+1]+p[i]-p[i-1]);			
			dV[nbrOfZones-1]=expCoeff*(p[nbrOfZones-1]-p[nbrOfZones-2]);

			for(int i=0;i<nbrOfZones;i++)
				dT[i]=-p[i]*dV[i]/zones[i].get_ggZone().get_cv_mass(V_T[i*2+1])/m[i];

			double dV_dT[]=new double [V_T.length];
			for(int i=0;i<nbrOfZones;i++){
				dV_dT[i*2]=dV[i];
				dV_dT[i*2+1]=dT[i];				
			}			
			return dV_dT;
		}		
	}


}
