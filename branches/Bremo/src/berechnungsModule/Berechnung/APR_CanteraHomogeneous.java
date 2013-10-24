package berechnungsModule.Berechnung;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import matLib.DglSysFunction;
import matLib.RungeKutta;
import misc.HeizwertRechner;
import misc.VektorBuffer;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.gemischbildung.Einspritzung;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.gemischbildung.Spray;
import berechnungsModule.motor.Motor;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;

/*public class APR_CanteraHomogeneous extends APR_Cantera {

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
	private double QbMax, dQw, Qb=0,mGes=0, Qw=0;	

	protected APR_CanteraHomogeneous(CasePara cp) {		
		super(cp,3);
		
		indiD=new IndizierDaten(cp);			

		motor = CP.MOTOR;
		wandWaermeModell=CP.WAND_WAERME;	
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;		
		checkEinspritzungen(masterEinspritzung); 		

		T_buffer = new VektorBuffer(cp);
		dQb_buffer = new VektorBuffer(cp);	
		dQw_buffer = new VektorBuffer(cp);	
		dmb_buffer = new VektorBuffer(cp);


		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////		
		initialZones=new Zone[nbrOfZones];
		
//		initialZones=this.initReadInHeatReleaseProfile();
		initialZones=this.initValidateCT();
	
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
		Zone [] zn=new Zone[nbrOfZones];
		double [] y=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];

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
		Zone [] zn=new Zone[nbrOfZones];		
		double [][] compVec = new double [3][CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];
		double p[]=new double [3];
		double T[]=new double [3];
		
		p[0]=5e5;
		p[1]=5e5;
		p[2]=5e5;		
		
		T[0]=300;
		T[1]=500;
		T[2]=900;
		
		
		double [] y0=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];
		y0[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=1.0;
		compVec[0]=y0;
		
		
		double [] y1=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];
		y1[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=1.0;
		compVec[1]=y1;

		double [] y2=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];
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
			
			double [] y=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];	
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
		Zone [] zn=new Zone[nbrOfZones];
		double [] y=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];
		
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
	

	@Override
	public boolean initialiseBurntZone() {		
		return false;
	}

	@Override
	protected Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {		

		dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
		zonen_IN[0].set_dQ_ein_aus(-1*dQw);

		//Verdampfungswaerme abfuehren
		zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

		//Einspritzung des Kraftstoffs
		zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);			

		return zonen_IN;				
	}

	@Override
	public void bufferErgebnisse(double time, Zone[] zn) {
		dQb_buffer.addValue(time, dQburnSumU);	
		dQw_buffer.addValue(time, dQw);	
		dmb_buffer.addValue(time, 0);
		Qb=Qb+dQburnSumU*super.CP.SYS.WRITE_INTERVAL_SEC;

		fortschritt=Qb/QbMax;
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);

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
		

		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("V_" + k + " [K]",zn[k].get_V(),i);
		}		

		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("T_Zone_" + k + " [K]",zn[k].get_T(),i);
		}

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_EinzelErgebnis("T_m [K]",Tm,i);
		T_buffer.addValue(time, Tm);
		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("p_"+k+" [bar]",zn[k].get_p()*1e-5,i);
		}
		
		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/s]",dQburnSum,i);

		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("dQb_" +k+" [J/s]",super.dQburn[k],i);
		}

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburnSum),i);		

		i+=1;
		super.buffer_EinzelErgebnis("dQbU [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnSumU),i);

		i+=1;
		super.buffer_EinzelErgebnis("dQbHf [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnSumHf),i);
		
		
		
		i+=1;		
		super.buffer_EinzelErgebnis("Qb [J]", Qb,i);		

		i+=1;		
		super.buffer_EinzelErgebnis("Qb/QbMax [J/CA]", Qb/QbMax,i);

		i+=1;		
		super.buffer_EinzelErgebnis("dQw [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQw),i);

		i+=1;	
		Qw=Qw+dQw*CP.SYS.WRITE_INTERVAL_SEC;
		super.buffer_EinzelErgebnis("Qw [J]", Qw,i);

		i+=1;			
		super.buffer_EinzelErgebnis("Qw/QbMax [-]", Qw/QbMax,i);


		for(int k=0;k<this.nbrOfZones;k++){
			i+=1;
			super.buffer_EinzelErgebnis("m_" +k+" [kg]", zn[k].get_m(),i);		
	
			i+=1;
			double kontrolMasse=zn[k].get_p()*zn[k].get_V();		
			kontrolMasse=kontrolMasse/(zn[k].get_ggZone().get_R()*zn[k].get_T());
			super.buffer_EinzelErgebnis("mK_"+k+" [kg]", kontrolMasse,i);
		}

		i+=1;
		double mGes=0;
		for(int j=0;j<zn.length;j++)mGes=mGes+zn[j].get_m();
		super.buffer_EinzelErgebnis("Sum_m [kg]",mGes ,i);		
		
		i+=1;
		super.buffer_EinzelErgebnis("mGesK [kg]", this.mGes,i);	
			
		//System.out.println("letzte Position: " +i);
		i+=1;
		for(int k=0;k<this.nbrOfZones;k++){					
			double []mi=zn[k].get_mi();
			for(int idx=0;idx<mi.length;idx++){
				String name="Z_"+k+"_y_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name();
				super.buffer_EinzelErgebnis(name,
						mi[idx]/zn[k].get_m(),i);
				//System.out.println(name+" : "+i);
				i+=1;
			}	
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
			if(me.get_AllInjections()[i].get_ID_Zone()!=0){
				try {
					throw new ParameterFileWrongInputException("Für das gwaehlte Berechnungsmodell " +
							"koennen die Einspritzungen " +
							"nur in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone "+ 
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
*/