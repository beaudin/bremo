//package berechnungsModule.Berechnung;
//import java.util.Hashtable;
//
//import kalorik.spezies.GasGemisch;
//import kalorik.spezies.Spezies;
//import misc.VektorBuffer;
//import berechnungsModule.gemischbildung.Einspritzung;
//import berechnungsModule.gemischbildung.MasterEinspritzung;
//import berechnungsModule.gemischbildung.Spray;
//import berechnungsModule.motor.Motor;
//import berechnungsModule.wandwaerme.WandWaermeUebergang;
//import bremo.parameter.CasePara;
//import bremo.parameter.IndizierDaten;
//import bremoExceptions.NegativeMassException;
//import bremoExceptions.ParameterFileWrongInputException;
//
//public class APR_CanteraMultiZoneBackUp extends APR_Cantera {		
//	private Motor motor;
//	private WandWaermeUebergang wandWaermeModell;
//	private MasterEinspritzung masterEinspritzung;
//	private Zone [] initialZones;
//
//	private IndizierDaten indiD;
//
//	private VektorBuffer T_buffer;
//	private VektorBuffer dQb_buffer;
//	private VektorBuffer dQw_buffer;
//	private VektorBuffer dmb_buffer;
//	private double fortschritt=0;
//	private double QbMax, dQw, Qb=0, mGes;
//	private int axPakete, radPakete, anzPakete;
//
//
//	protected APR_CanteraMultiZoneBackUp(CasePara cp) {
//		super(cp,cp.get_anzAxialPakete(1)*cp.get_anzRadialPakete(1)+1);
//		axPakete=cp.get_anzAxialPakete(1);
//		radPakete=cp.get_anzRadialPakete(1);
//		anzPakete=axPakete*radPakete;
//
//		indiD=new IndizierDaten(cp);
//		motor = CP.MOTOR;
//		wandWaermeModell=CP.WAND_WAERME;	
//		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
//		checkEinspritzungen(masterEinspritzung); 	
//
//		T_buffer = new VektorBuffer(cp);
//		dQb_buffer = new VektorBuffer(cp);	
//		dQw_buffer = new VektorBuffer(cp);	
//		dmb_buffer = new VektorBuffer(cp);	
//
//		/////////////////////////////////////////////////////////////
//		///////////Initialisieren der Anfangsbedingungen/////////////
//		/////////////////////////////////////////////////////////////		
//		initialZones=new Zone[nbrOfZones];
//		super.dQburn=new double [nbrOfZones];
//
//
//		double mINIT, p_init, V_init,T_init,mD,mG,VD,VG,R;
//
//		//			double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
//		//			double mKrstDampfINIT=
//		//					masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0); 
//		//			mINIT= mVerbrennungsLuft+mKrstDampfINIT;
//		//			Spezies krst=
//		//					masterEinspritzung.get_spezKrst_verdampft(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0);
//		//			Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	
//		//
//		//			Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
//		//
//		//			frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mINIT);
//		//			frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mINIT);		
//		//
//		//			GasGemisch gemischINIT=new GasGemisch("GemischINIT");	
//		//			gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
//		//			
//		//			//Anfangsbedingungen Setzen
//		//			//p Init
//		//			p_init=indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
//		//			//V Init
//		//			V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
//		//			//T Init		
//		//			Spezies ggZone_init=gemischINIT;
//		//			R=ggZone_init.get_R();
//		//			T_init=(p_init*V_init)/(mINIT*R);	
//		//			
//		//			mD=(1+1e-5)*CP.SYS.MINIMALE_ZONENMASSE;		
//		//			mG=mINIT-mD;
//		//			VD=mD*R*T_init/p_init;
//		//			
//		//			VG=mG*R*T_init/p_init;	
//		//			
//		//			double testV=V_init-VG-VD;
//		//			System.out.println(testV);				
//		//			//unverbrannte Zone
//		//			this.initialZones[0]=new Zone(CP,p_init, VG, T_init, 
//		//					mG,ggZone_init , false,0);
//		//
//		//		
//		//			this.initialZones[1]=new Zone(CP,p_init, VD, T_init, 
//		//					mD,ggZone_init , false,1);	
//		//			
//		//			System.out.println(masterEinspritzung.get_mKrst_Sum_ASP());
//		//			QbMax=masterEinspritzung.get_mKrst_Sum_ASP()
//		//					*masterEinspritzung.get_spezKrstALL().get_Hu_mass();
//		//			
//		//			mGes=masterEinspritzung.get_mKrst_Sum_ASP()+mVerbrennungsLuft;	
//
//
//
//
//		double [] y=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];
////		//ohne Diesel!
//		//Single Injection pmi=8.5 AGR 50% 
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.1327011879;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.7312165251;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.0787026718;		
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.0343273968;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.0230522184;		
//		
//		
//		//vollstaendig pmi=8.5  homogen AGR 50% 
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0014775074;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.1324136548;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.7301018102;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.0786625027;		
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.0343065527;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.0230379723;	
//					
//
//		//Doppeleinspritzung pmi=8.5  AGR 50% 
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0007540925;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.1325095855;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.7306307589;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.0787194926;		
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.0343314074;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.0230546631;	
//		
//		//AGR 50% 
////		mGes=8.490196E-4;				
////		mINIT=8.4840757E-4;		
////		p_init=797370.29;
//		
//								
//
//					
////		//pmi=8.5 AGR 45%
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0007508873;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.1484067760;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.7327602274;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.0680568594;		
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.0270682870;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.0229569630;	
////		
////		//pmi=8.5 AGR 45% 
////		mGes=8.48996E-4;				
////		mINIT=8.48384E-4;		
////		p_init=798830.94;
//							
//
////		//pmi=5.5 AGR 0%
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.2248595390;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.7517932134;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.0032505486;		
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.0012674481;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.0188292220;	
////		
////		//AGR 45% 
////		mGes=6.547059E-4;				
////		mINIT=6.525837E-4;		
////		p_init=406912.65;
////			
//				
//
////		//pmi=5.5 AGR 0% homogen
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0032417385;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.2241306075;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.7493561204;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.0032400112;		
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.0012633394;
////		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.0187681830;	
////		
////		
////		mGes=6.547059E-4;				
////		mINIT=mGes;		
////		p_init=767724.72;
//		
//		
//		//pmi=5.5 AGR 0% 3.5% weniger iC8H18
//		double redFac=1-0.035;
//		double nO2=0.2248595390;
//		double nN2=0.7517932134;
//		double nCO2=0.0032505486;
//		double nH2O=0.0012674481;
//		double niC8H18=0.0188292220;
//		double sumF=redFac*niC8H18+nH2O+nCO2+nN2+nO2;
//		
//		
//		mGes=6.547059E-4;	
//		mGes=mGes*(1-niC8H18*redFac); //Stimmt nicht genau, da ja auch nohc Diesel Reduziert wird
//		mINIT=6.525837E-4*(1-niC8H18*redFac);		
//		p_init=406912.65;
//
//
//		
//		nO2=nO2/sumF;
//		nN2=nN2/sumF;
//		nCO2=nCO2/sumF;
//		nH2O=nH2O/sumF;
//		niC8H18=redFac*niC8H18/sumF;
//		
//		
//		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0;
//		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=nO2;
//		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=nN2;
//		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=nCO2;		
//		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=nH2O;
//		y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=niC8H18;			
//
//
//		Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
//
//		double sum=0;
//		for(int j=0;j<y.length;j++)sum=sum+y[j];
//		for(int j=0;j<y.length;j++)y[j]=y[j]/sum;
//
//		for(int j=0;j<y.length;j++){			
//			ht.put(CP.SPEZIES_FABRIK.get_Spez(j), y[j]);
//		}			
//		GasGemisch spec=new GasGemisch("CanteraBurned_");
//		spec.set_Gasmischung_massenBruch(ht);
//		
//		double HuSpec=spec.get_Hu_mass();
//		this.QbMax=spec.get_Hu_mass()*mGes;
//		
//		double HunC7H16=CP.SPEZIES_FABRIK.get_nC7H16().get_Hu_mass();
//		double HuiC8H18=CP.SPEZIES_FABRIK.get_iC8H18().get_Hu_mass();
//		
//		double Hu=(0.0032417385*HunC7H16+0.0187681830*HuiC8H18)/(0.0032417385+0.0187681830);
//	
//
//		V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC); //4.495833146709407E-4
//		R=spec.get_R(); 
//		T_init=p_init*V_init/mINIT/R; //362.01349 bzw. 525.80315 bzw. 486.62621 bzw 572.88913
//
//	
//		//Masse in allen Dieselzonen
//
//		mD=(1+1e-6)*CP.SYS.MINIMALE_ZONENMASSE;
//		double mDSum=mD*this.anzPakete;
//		mG=mINIT-mDSum;			
//		VG=mG*R*T_init/p_init;	
//		VD=mD*R*T_init/p_init;	
//
//		double testV=V_init-VG-VD*this.anzPakete;
//		System.out.println("testV: "+ testV);				
//		//gasoline
//		this.initialZones[nbrOfZones-1]=new Zone(CP,p_init, VG, T_init, 
//				mG,spec , false,nbrOfZones-1);
//		//Diesel
//		for(int i=0;i<anzPakete;i++)		
//			this.initialZones[i]=new Zone(CP,p_init, VD, T_init, 
//					mD,spec , false,i);				
//	}	
//	
//
//	@Override
//	public boolean initialiseBurntZone() {		
//		return false;
//	}
//
//	@Override
//	protected Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {		
//
//		dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
//		double V=motor.get_V(time);
//		for(int i=0;i<this.nbrOfZones;i++){
//			double dQwi=dQw*zonen_IN[i].get_V()/V;
//			zonen_IN[i].set_dQ_ein_aus(-1*dQwi);					
//		}
//
//		for(int i=0;i<masterEinspritzung.get_AllInjections().length;i++){	
//			Einspritzung es=masterEinspritzung.get_AllInjections()[i];					
//			if(es.get_Krst()==CP.SPEZIES_FABRIK.get_nC7H16()){
//
//				double dmaSum=((Spray) es).get_dma(time);
//
//				int axialPakete=((Spray) es).get_AnzahlAxialPakete();
//				int radialPakete=((Spray) es).get_AnzahlRadialPakete();
//				for(int ax=0;ax<axialPakete;ax++){	
//					for(int rad=0;rad<radialPakete; rad++){
//						double dmKrst=((Spray) es).get_diff_mKrst_dampf_Paket(	time, 
//								zonen_IN[ax*radialPakete+rad], 	
//								ax, 
//								rad);
//						double Tkrst=((Spray) es).get_Tkrst_dampf_Paket(time, 
//								zonen_IN[ax*radialPakete+rad], 
//								ax, 
//								rad);
//
//						zonen_IN[ax*radialPakete+rad].set_dm_ein(dmKrst, Tkrst, es.get_Krst());	
//
//						double dQ_krst=((Spray) es).get_dQ_krstDampf_Paket(	time, 
//								zonen_IN[ax*radialPakete+rad], 
//								ax, 
//								rad);	
//						
//						zonen_IN[ax*radialPakete+rad].set_dQ_ein_aus(-1*dQ_krst);
//
//						if(zonen_IN[nbrOfZones-1].get_m()>=dmaSum*CP.SYS.WRITE_INTERVAL_SEC){
//							double dma=((Spray) es).get_dma_Paket(time, ax, rad);
//							zonen_IN[ax*radialPakete+rad].set_dm_ein(dma, 
//									zonen_IN[nbrOfZones-1].get_T(), 
//									zonen_IN[nbrOfZones-1].get_ggZone());
//
//							try {
//								zonen_IN[nbrOfZones-1].set_dm_aus(dma);
//							} catch (NegativeMassException e) {
//								e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
//								//Passiert eh nicht!!
//							}
//						}//else
//						//System.out.println("hmm");
//
//					}
//				}
//				
//			}				
//		}
//		return zonen_IN;					
//	}
//
//	@Override
//	public void bufferErgebnisse(double time, Zone[] zn) {
//		dQb_buffer.addValue(time, dQburnSum);	
//		dQw_buffer.addValue(time, dQw);	
//		dmb_buffer.addValue(time, 0);
//		Qb=Qb+dQburnSum*super.CP.SYS.WRITE_INTERVAL_SEC;
//
//		fortschritt=Qb/QbMax;
//
//		for(int i=0;i<masterEinspritzung.get_AllInjections().length;i++){	
//			Einspritzung es=masterEinspritzung.get_AllInjections()[i];	
//			if(es.get_Krst()==CP.SPEZIES_FABRIK.get_nC7H16()){
//				int axialPakete=((Spray) es).get_AnzahlAxialPakete();
//				int radialPakete=((Spray) es).get_AnzahlRadialPakete();
//				for(int ax=0;ax<axialPakete;ax++)	
//					for(int rad=0;rad<radialPakete; rad++)
//						((Spray)es).berechneIntegraleGroessen_Paket(time, zn[ax*radialPakete+rad],ax,rad);
//			}
//			else
//				es.berechneIntegraleGroessen(time, zn[nbrOfZones-1]);
//		}
//
//		int i=-1;
//		i+=1;
//		super.buffer_EinzelErgebnis("CrankAngle [°KW]",super.CP.convert_SEC2KW(time),i);
//
//		i+=1;
//		super.buffer_EinzelErgebnis("Time [s n. Rechenbeginn]",time,i);		
//
//		i+=1;
//		double V=motor.get_V(time);
//		super.buffer_EinzelErgebnis("Volume [m3]",V,i);	
//		
//		double sum_V=0;
//		for(int k=0;k<this.nbrOfZones;k++)sum_V+=zn[k].get_V();
//
//		i+=1;
//		super.buffer_EinzelErgebnis("V-sum_Vk [%]",(V-sum_V)/V*100,i);	
//		
//		i+=1;
//		double Tm=wandWaermeModell.get_Tmb(zn);
//		super.buffer_EinzelErgebnis("T_m [K]",Tm,i);
//		T_buffer.addValue(time, Tm);				
//
//		double pExp=indiD.get_pZyl(time);
//		i+=1;
//		super.buffer_EinzelErgebnis("p_exp [bar]", pExp*1E-5,i);
//
//		//		for(int k=0;k<this.nbrOfZones;k++){
//		//			i+=1;
//		//			super.buffer_EinzelErgebnis("p_" + k+  " [bar]",zn[k].get_p()*1e-5,i);
//		//		}
//
//		i+=1;
//		super.buffer_EinzelErgebnis("p_" + 0+  " [bar]",zn[0].get_p()*1e-5,i);
//
//		i+=1;
//		super.buffer_EinzelErgebnis("dQb [J/s]",super.dQburnSum,i);
//
//		i+=1;
//		super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnSum),i);
//
//		i+=1;		
//		super.buffer_EinzelErgebnis("dQw [J/CA]", super.CP.convert_ProSEC_2_ProKW(dQw),i);
//
//		i+=1;		
//		super.buffer_EinzelErgebnis("Qb [J]", Qb,i);
//
//		i+=1;		
//		super.buffer_EinzelErgebnis("Qb/QbMax [-]", Qb/QbMax,i);
//		
//		double sum_m=0;
//		for(int k=0;k<this.nbrOfZones;k++)sum_m+=zn[k].get_m();
//		i+=1;
//		super.buffer_EinzelErgebnis("sum_mk [kg]", sum_m,i);				
//
//		i+=1;
//		super.buffer_EinzelErgebnis("mGes [kg]", mGes,i);
//		
//		
//		int nbrOfPackets=-5;
//		for(int k=0;k<masterEinspritzung.get_AllInjections().length;k++){	
//			Einspritzung es=masterEinspritzung.get_AllInjections()[k];					
//			if(es.get_Krst()==CP.SPEZIES_FABRIK.get_nC7H16()){
//				nbrOfPackets=((Spray)es).get_nbrOfPacketsAlive(time);
//			}
//		}
//		
//		i+=1;
//		super.buffer_EinzelErgebnis("anzahl Pakete [-]",nbrOfPackets,i);		
//		
//		
//		i+=1;
//		super.buffer_EinzelErgebnis("V_" +(this.nbrOfZones-1)+" [-]", zn[this.nbrOfZones-1].get_V()/V,i);
//		
//		i+=1;
//		super.buffer_EinzelErgebnis("m_" +(this.nbrOfZones-1)+" [kg]", zn[(this.nbrOfZones-1)].get_m(),i);
//		
//		
//		i+=1;			
//		String name="Lambda_"+ 0;
//		double lambda=zn[0].get_ggZone().get_lambda();
//		super.buffer_EinzelErgebnis(name,
//				lambda,i);
//		
//		i+=1;			
//		name="Lambda_"+ (this.nbrOfZones-2);
//		lambda=zn[this.nbrOfZones-2].get_ggZone().get_lambda();
//		super.buffer_EinzelErgebnis(name,
//				lambda,i);
//		
//		
//		
//		
//		for(int k=0;k<this.nbrOfZones-1;k++){
//			i+=1;
//			super.buffer_EinzelErgebnis("V_" +k+" [-]", zn[k].get_V()/V,i);
//		}
//					
//
//		for(int k=0;k<this.nbrOfZones;k++){
//			i+=1;
//			super.buffer_EinzelErgebnis("T_" + k+ " [K]",zn[k].get_T(),i);
//		}		
//
//		for(int k=0;k<this.nbrOfZones;k++){
//			i+=1;
//			super.buffer_EinzelErgebnis("dQb_"+k+ "[J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburn[k]),i);
//		}
//
//		for(int k=0;k<this.nbrOfZones-1;k++){
//			i+=1;
//			super.buffer_EinzelErgebnis("m_" +k+" [kg]", zn[k].get_m(),i);	
//
//			//				i+=1;
//			//				double kontrolMasse=zn[k].get_p()*zn[k].get_V();		
//			//				kontrolMasse=kontrolMasse/(zn[k].get_ggZone().get_R()*zn[k].get_T());
//			//				super.buffer_EinzelErgebnis("mK_"+k+" [kg]", kontrolMasse,i);
//		}
//
//		for(int k=1;k<this.nbrOfZones-2;k++){	
//			i+=1;			
//			name="Lambda_"+k;
//			lambda=zn[k].get_ggZone().get_lambda();
//			super.buffer_EinzelErgebnis(name,
//					lambda,i);
//		}
//		
//		i+=1;			
//		name="Lambda_"+ (this.nbrOfZones-1);
//		lambda=zn[this.nbrOfZones-1].get_ggZone().get_lambda();
//		super.buffer_EinzelErgebnis(name,
//				lambda,i);
//		
//
//		
//
//		//		int index=CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16());
//		//		for(int k=0;k<this.nbrOfZones;k++){	
//		//			i+=1;
//		//			String name="nc7h16_"+k;
//		//			double []mi=zn[k].get_mi();
//		//			super.buffer_EinzelErgebnis(name,
//		//					mi[index]/zn[k].get_m(),i);
//		//
//		//		}
//
//		//		int index=CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_soot());
//		//		for(int k=0;k<this.nbrOfZones;k++){	
//		//			i+=1;
//		//			String name="soot"+k;
//		//			double []mi=zn[k].get_mi();
//		//			super.buffer_EinzelErgebnis(name,
//		//					mi[index]/zn[k].get_m(),i);
//		//
//		//		}
//
//
//
//		//		i+=1;
//		//		for(int k=0;k<this.nbrOfZones;k++){					
//		//			double []mi=zn[k].get_mi();
//		//			for(int idx=0;idx<mi.length;idx++){
//		//				String name="Z_"+k+"_y_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name();
//		//				super.buffer_EinzelErgebnis(name,
//		//						mi[idx]/zn[k].get_m(),i);
//		//				//System.out.println(name+" : "+i);
//		//				i+=1;
//		//			}	
//		//		}
//
//	}
//
//	@Override
//	public Zone[] get_initialZones() {
//		return initialZones;
//	}
//
//	@Override
//	public Zone[] get_initialZonesWhileRunning() {		
//		return null;
//	}
//
//	@Override
//	public int get_anzZonen() {
//		return super.nbrOfZones;
//	}
//
//	@Override
//	public VektorBuffer get_dm_buffer() {
//		return dmb_buffer;
//	}
//
//	@Override
//	public VektorBuffer get_dQw_buffer() {
//		return this.dQw_buffer;
//	}
//
//	@Override
//	public VektorBuffer get_dQb_buffer() {
//		return this.dQb_buffer;
//	}
//
//	@Override
//	protected void checkEinspritzungen(MasterEinspritzung me) {
//		if(me.get_AllInjections().length!=2)
//			try {
//				throw new ParameterFileWrongInputException("Es sind maximal zwei Einspritzungen moeglich");
//			} catch (ParameterFileWrongInputException e) {				
//				e.stopBremo();
//			}
//
//		if(me.get_AllInjections()[0].get_Krst()!=CP.SPEZIES_FABRIK.get_nC7H16())
//			try {
//				throw new ParameterFileWrongInputException("Fuer die erste Einspritzung ist nur nC7H16 moeglich. " +
//						"Verwendet wurde aber "+ me.get_AllInjections()[0].get_Krst());
//			} catch (ParameterFileWrongInputException e) {				
//				e.stopBremo();
//			}
//
//		if(me.get_AllInjections()[1].get_Krst()!=CP.SPEZIES_FABRIK.get_iC8H18())
//			try {
//				throw new ParameterFileWrongInputException("Fuer die zweite Einspritzung ist nur iC8H18 moeglich. " +
//						"Verwendet wurde aber "+ me.get_AllInjections()[1].get_Krst());
//			} catch (ParameterFileWrongInputException e) {				
//				e.stopBremo();
//			}		
//
//
//		try{
//			if(((Spray) me.get_AllInjections()[0]).get_AnzahlAxialPakete()!=this.axPakete || 
//					((Spray) me.get_AllInjections()[0]).get_AnzahlRadialPakete()!=this.radPakete)
//				try {
//					throw new ParameterFileWrongInputException("Irgendwo wurde die Anzahl der Palete geaendert");
//				} catch (ParameterFileWrongInputException e) {				
//					e.stopBremo();
//				}
//
//		}catch(ClassCastException cce){
//			try {
//				throw new ParameterFileWrongInputException("Die Einspsritzung von nC7H16 muss mit dem Hiroyasu-Modell " +
//						"erfolgen");
//			} catch (ParameterFileWrongInputException e) {				
//				e.stopBremo();
//			}
//		}
//	}
//}
//
//
