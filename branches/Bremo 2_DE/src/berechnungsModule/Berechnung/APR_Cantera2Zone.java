//package berechnungsModule.Berechnung;
//import java.util.Hashtable;
//
//import kalorik.spezies.GasGemisch;
//import kalorik.spezies.Spezies;
//import misc.HeizwertRechner;
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
//public class APR_Cantera2Zone extends APR_Cantera {		
//		private Motor motor;
//		private WandWaermeUebergang wandWaermeModell;
//		private MasterEinspritzung masterEinspritzung;
//		private Zone [] initialZones;
//		
//		private IndizierDaten indiD;
//		
//		private VektorBuffer T_buffer;
//		private VektorBuffer dQb_buffer;
//		private VektorBuffer dQw_buffer;
//		private VektorBuffer dmb_buffer;
//		private double fortschritt=0;
//		private double QbMax, dQw, Qb=0, mGes;
//
//		
//		protected APR_Cantera2Zone(CasePara cp) {
//			super(cp,2);				
//			
//			indiD=new IndizierDaten(cp);						
//
//			motor = CP.MOTOR;
//			wandWaermeModell=CP.WAND_WAERME;	
//			masterEinspritzung=CP.MASTER_EINSPRITZUNG;
//			checkEinspritzungen(masterEinspritzung); 	
//
//			T_buffer = new VektorBuffer(cp);
//			dQb_buffer = new VektorBuffer(cp);	
//			dQw_buffer = new VektorBuffer(cp);	
//			dmb_buffer = new VektorBuffer(cp);	
//
//			/////////////////////////////////////////////////////////////
//			///////////Initialisieren der Anfangsbedingungen/////////////
//			/////////////////////////////////////////////////////////////		
//			initialZones=new Zone[nbrOfZones];
//			super.dQburn=new double [nbrOfZones];
//
//
//			double mINIT, p_init, V_init,T_init,mD,mG,VD,VG,R;
//			GasGemisch gemischINIT=new GasGemisch("GemischINIT");
//			mD=(1+1e-6)*CP.SYS.MINIMALE_ZONENMASSE;
//			
////			double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
////			double mKrstDampfINIT=
////					masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0); 
////			mINIT= mVerbrennungsLuft+mKrstDampfINIT;
////			Spezies krst=
////					masterEinspritzung.get_spezKrst_verdampft(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0);
////			
////			Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	
////			Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
////			frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mINIT);
////			frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mINIT);	
////				
////			gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
////			
////			//Anfangsbedingungen Setzen
////			//p Init
////			p_init=indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);			
////
////			//V Init
////			V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
////
////			QbMax=masterEinspritzung.get_mKrst_Sum_ASP()
////					*masterEinspritzung.get_spezKrstALL().get_Hu_mass();
////			
////			mGes=masterEinspritzung.get_mKrst_Sum_ASP()+mVerbrennungsLuft;			
////			
////			
//			double [] y=new double [CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()];
//			//ohne Diesel!
//			//y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_nC7H16())]=0.0036814;
//			y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezO2())]=0.17458;
//			y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezN2())]=0.74012;
//			y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezCO2())]=0.044454;		
//			y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_spezH2O())]=0.020201;
//			y[CP.SPEZIES_FABRIK.get_indexOf(CP.SPEZIES_FABRIK.get_iC8H18())]=0.016964;
//			
//			Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
//
//			double sum=0;
//			for(int j=0;j<y.length;j++)sum=sum+y[j];
//			for(int j=0;j<y.length;j++)y[j]=y[j]/sum;
//
//			for(int j=0;j<y.length;j++){			
//				ht.put(CP.SPEZIES_FABRIK.get_Spez(j), y[j]);
//			}			
//			
//			gemischINIT.set_Gasmischung_massenBruch(ht);			
//			mGes=0.00096818;			
//			mINIT=mGes-0.0036814*mGes;			
//			
//			p_init=2.1169*1e5;
//			V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC); //4.495833146709407E-4
//			//T_init=345.79;
//			double V_SK=0.00044958;
//			System.out.println((V_init-V_SK)/V_SK*100);				
//			
//			R=gemischINIT.get_R(); //285.0212545788516				
//			T_init=p_init*V_init/mINIT/R; //346.1616364746153			
//			mG=mINIT-mD;
//			VG=mG*R*T_init/p_init;			
//			VD=mD*R*T_init/p_init;			
//			double testV=V_init-VG-VD;
//			System.out.println(testV);				
//			//unverbrannte Zone
//			this.initialZones[0]=new Zone(CP,p_init, VG, T_init, 
//					mG,gemischINIT , false,0);
//		
//			this.initialZones[1]=new Zone(CP,p_init, VD, T_init, 
//					mD,gemischINIT , false,1);							
//			
//		}			
//
//		@Override
//		public boolean initialiseBurntZone() {		
//			return false;
//		}
//
//		@Override
//		protected Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {
//				double T0=zonen_IN[0].get_T();
//				Spezies spez0=zonen_IN[0].get_ggZone();
//				double V0=zonen_IN[0].get_V();
//				double V1=zonen_IN[1].get_V();				
//				
//				dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
//				//GasolineZone
//				double dQw0=dQw*V0/(V0+V1);			
//				zonen_IN[0].set_dQ_ein_aus(-1*dQw0);		
//				//DieselZone
//				double dQw1=dQw*V1/(V0+V1);
//				zonen_IN[1].set_dQ_ein_aus(-1*dQw1);				
//				
//				//Verdampfungswaerme abfuehren
//				zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			
//
//				//Einspritzung des Kraftstoffs
//				zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);				
//				
//				double dma=0;
//				for(int i=0;i<masterEinspritzung.get_AllInjections().length;i++){	
//					Einspritzung es=masterEinspritzung.get_AllInjections()[i];
//					if(es.get_ID_Zone()==1){
//						dma+=((Spray) es).get_dma(time);
//					}				
//				}	
//					
//				//Abfuehren des air entrainments aus der OttoZone
//				if(zonen_IN[0].get_m()>=CP.SYS.MINIMALE_ZONENMASSE){
//					try {
//						zonen_IN[0].set_dm_aus(dma);
//					} catch (NegativeMassException e) {
//						// Wenn die OttoZone ganz leer ist stimmt irgendwas nicht!
//						e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
//						dma=0;
//					}
//					//Zufuehren des air entrainments in die DieselZone
//					zonen_IN[1].set_dm_ein(dma,T0 ,spez0);
//				}				
//				return zonen_IN;					
//		}
//
//		@Override
//		public void bufferErgebnisse(double time, Zone[] zn) {
//			dQb_buffer.addValue(time, dQburnSum);	
//			dQw_buffer.addValue(time, dQw);	
//			dmb_buffer.addValue(time, 0);
//			Qb=Qb+dQburnSum*super.CP.SYS.WRITE_INTERVAL_SEC;
//			
//			fortschritt=Qb/QbMax;
//			
//			this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
//			
//			int i=-1;
//			i+=1;
//			super.buffer_EinzelErgebnis("CrankAngle [°KW]",super.CP.convert_SEC2KW(time),i);
//
//			i+=1;
//			super.buffer_EinzelErgebnis("Time [s n. Rechenbeginn]",time,i);		
//
//			i+=1;
//			double V=motor.get_V(time);
//			super.buffer_EinzelErgebnis("Volume [m3]",V,i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("Volume 0",zn[0].get_V()/V,i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("Volume 1",zn[1].get_V()/V,i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("V-V0-V1 [%]",(V-zn[1].get_V()-zn[0].get_V())/V*100,i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("VdeltaVGes",super.deltaV,i);
//			
//
//			i+=1;
//			super.buffer_EinzelErgebnis("T_Zone_0 [K]",zn[0].get_T(),i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("T_Zone_1 [K]",zn[1].get_T(),i);
//
//			i+=1;
//			double Tm=wandWaermeModell.get_Tmb(zn);
//			super.buffer_EinzelErgebnis("T_m [K]",Tm,i);
//			T_buffer.addValue(time, Tm);			
//			
//
//			double pExp=indiD.get_pZyl(time);
//			i+=1;
//			super.buffer_EinzelErgebnis("p_exp [bar]", pExp*1E-5,i);
//
//			i+=1;
//			super.buffer_EinzelErgebnis("p_0 [bar]",zn[0].get_p()*1e-5,i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("p_1 [bar]",zn[1].get_p()*1e-5,i);			
//
//			i+=1;
//			super.buffer_EinzelErgebnis("dQb [J/s]",super.dQburnSum,i);
//
//			i+=1;
//			super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnSum),i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("dQb0 [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburn[0]),i);
//
//			i+=1;
//			super.buffer_EinzelErgebnis("dQb1 [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburn[1]),i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("dQbVol1 [J/s]", super.CP.convert_ProSEC_2_ProKW(super.dQburnVol[0]),i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("dQbVol2 [J/KW]", super.CP.convert_ProSEC_2_ProKW(super.dQburnVol[1]),i);
//						
//			i+=1;		
//			super.buffer_EinzelErgebnis("dQw [J]", super.CP.convert_ProSEC_2_ProKW(dQw),i);
//			
//			i+=1;		
//			super.buffer_EinzelErgebnis("Qb [J/CA]", Qb,i);
//			
//			i+=1;		
//			super.buffer_EinzelErgebnis("Qb/QbMax [J/CA]", Qb/QbMax,i);
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("Zonenmasse_0 [kg]", zn[0].get_m(),i);		
//
//			i+=1;
//			double kontrolMasse=zn[0].get_p()*zn[0].get_V();		
//			kontrolMasse=kontrolMasse/(zn[0].get_ggZone().get_R()*zn[0].get_T());
//			super.buffer_EinzelErgebnis("ZonenKontrollmasse_0 [kg]", kontrolMasse,i);	
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("Zonenmasse_1 [kg]", zn[1].get_m(),i);		
//
//			i+=1;
//			kontrolMasse=zn[1].get_p()*zn[1].get_V();		
//			kontrolMasse=kontrolMasse/(zn[1].get_ggZone().get_R()*zn[1].get_T());
//			super.buffer_EinzelErgebnis("ZonenKontrollmasse_1 [kg]", kontrolMasse,i);	
//			
//			i+=1;
//			double sum_m=0;
//			for(int k=0;k<zn.length;k++) sum_m=sum_m+zn[k].get_m();
//			super.buffer_EinzelErgebnis("sum_m [kg]", sum_m,i);	
//			
//			i+=1;
//			super.buffer_EinzelErgebnis("mGes [kg]", mGes,i);				
//
//				
//			
//			i+=1;
//			for(int k=0;k<this.nbrOfZones;k++){					
//				double []mi=zn[k].get_mi();
//				for(int idx=0;idx<mi.length;idx++){
//					String name="Z_"+k+"_y_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name();
//					super.buffer_EinzelErgebnis(name,
//							mi[idx]/zn[k].get_m(),i);
//					//System.out.println(name+" : "+i);
//					i+=1;
//				}	
//			}
//	
//		}
//
//		@Override
//		public Zone[] get_initialZones() {
//			return initialZones;
//		}
//
//		@Override
//		public Zone[] get_initialZonesWhileRunning() {		
//			return null;
//		}
//
//		@Override
//		public int get_anzZonen() {
//			return nbrOfZones;
//		}
//
//		@Override
//		public VektorBuffer get_dm_buffer() {
//			return dmb_buffer;
//		}
//
//		@Override
//		public VektorBuffer get_dQw_buffer() {
//			return this.dQw_buffer;
//		}
//
//		@Override
//		public VektorBuffer get_dQb_buffer() {
//			return this.dQb_buffer;
//		}
//
//		@Override
//		protected void checkEinspritzungen(MasterEinspritzung me) {
//			for(int i=0;i<me.get_AllInjections().length;i++){	
//				Einspritzung es=me.get_AllInjections()[i];
//				if(es.get_ID_Zone()==0){
//					if(es.get_Krst().get_name().equalsIgnoreCase("nc7h16")){
//						try {
//							throw new ParameterFileWrongInputException("Fuer das gwaehlte Berechnungsmodell " +
//									"darf die DieselEinspritzung nicht " +
//							"in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone 0 ");
//						} catch (ParameterFileWrongInputException e) {				
//							e.stopBremo();
//						}
//					}				
//				}
//				if(es.get_ID_Zone()==1){
//					if(es.get_Krst().get_name().equalsIgnoreCase("nc7h16")==false){
//						//ToDo checken dass die EInspritzung ein pakestModell ist!
//						try {
//							throw new ParameterFileWrongInputException("Für das gwaehlte Berechnungsmodell " +
//									"darf ausschließlich nc7h16 in Zone 1 eingespritzwerden \n" +
//									" Eingespritzt wurde aber " +es.get_Krst().get_name());
//						} catch (ParameterFileWrongInputException e) {				
//							e.stopBremo();
//						}
//					}
//				}
//				if(es.get_ID_Zone()>1){				
//					try {
//						throw new ParameterFileWrongInputException("Einspritzungen sind ausschließlich in die " +
//								"Zonen 0 und 1 moeglich! \n" +
//								"Gewaehlt wurde aber Zone " +es.get_ID_Zone())  ;
//					} catch (ParameterFileWrongInputException e) {				
//						e.stopBremo();
//					}
//
//				}
//			}
//		}
//}
//
