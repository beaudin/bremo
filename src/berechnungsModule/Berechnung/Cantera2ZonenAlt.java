//package berechnungsModule.Berechnung;
//	import java.util.Hashtable;
//
//	import kalorik.spezies.GasGemisch;
//	import kalorik.spezies.Spezies;
//	import misc.HeizwertRechner;
//	import misc.VektorBuffer;
//	import berechnungsModule.gemischbildung.Einspritzung;
//	import berechnungsModule.gemischbildung.MasterEinspritzung;
//	import berechnungsModule.gemischbildung.Spray;
//	import berechnungsModule.motor.Motor;
//	import berechnungsModule.wandwaerme.WandWaermeUebergang;
//	import bremo.parameter.CasePara;
//	import bremo.parameter.IndizierDaten;
//	import bremoExceptions.NegativeMassException;
//	import bremoExceptions.ParameterFileWrongInputException;
//
//
//public class Cantera2ZonenAlt extends APR {
//
//			private double dQburn[];
//			private double dQburnSum;
//			private final int nbrOfZones;	
//			
//			private Motor motor;
//			private WandWaermeUebergang wandWaermeModell;
//			private MasterEinspritzung masterEinspritzung;
//			private Zone [] initialZones;
//			
//			private IndizierDaten indiD;
//
//			
//			private VektorBuffer T_buffer;
//			private VektorBuffer dQb_buffer;
//			private VektorBuffer dQw_buffer;
//			private VektorBuffer dmb_buffer;
//			private double fortschritt=0;
//			private double QbMax, dQw, Qb=0;
//			private double [] dm, T_CT[];
//			private Spezies [] spec_CT;
//			
//			protected Cantera2ZonenAlt(CasePara cp) {
//				super(cp);			
//				
//				
//				indiD=new IndizierDaten(cp);
//				
//				nbrOfZones=2;	
//				
//				dQburn=new double [nbrOfZones];
//
//				motor = CP.MOTOR;
//				wandWaermeModell=CP.WAND_WAERME;	
//				masterEinspritzung=CP.MASTER_EINSPRITZUNG;
//				checkEinspritzungen(masterEinspritzung); 	
//
//				T_buffer = new VektorBuffer(cp);
//				dQb_buffer = new VektorBuffer(cp);	
//				dQw_buffer = new VektorBuffer(cp);	
//				dmb_buffer = new VektorBuffer(cp);	
//
//				/////////////////////////////////////////////////////////////
//				///////////Initialisieren der Anfangsbedingungen/////////////
//				/////////////////////////////////////////////////////////////		
//				initialZones=new Zone[nbrOfZones];
//
//
//				double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
//				double mKrstDampfINIT=
//						masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0); 
//				double mINIT= mVerbrennungsLuft+mKrstDampfINIT;
//				Spezies krst=
//						masterEinspritzung.get_spezKrst_verdampft(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0);
//				Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	
//
//				Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
//
//				frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mINIT);
//				frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mINIT);		
//
//				GasGemisch gemischINIT=new GasGemisch("GemischINIT");	
//				gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
//				
//				//Anfangsbedingungen Setzen
//				//p Init
//				double p_init=indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
//				//V Init
//				double V_init=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
//				//T Init		
//				Spezies ggZone_init=gemischINIT;
//				double R=ggZone_init.get_R();
//				double T_init=(p_init*V_init)/(mINIT*R);	
//				
//				double mD=(1+1e-20)*CP.SYS.MINIMALE_ZONENMASSE;
//				double mG=mINIT-mD;
//				double VD=mD*R*T_init/p_init;
//				
//				double VG=mG*R*T_init/p_init;	
//				
//				double testV=V_init-VG-VD;
//				System.out.println(testV);				
//				//unverbrannte Zone
//				this.initialZones[0]=new Zone(CP,p_init, VG, T_init, 
//						mG,ggZone_init , false,0);
//
//			
//				this.initialZones[1]=new Zone(CP,p_init, VD, T_init, 
//						mD,ggZone_init , false,1);			
//				
//				QbMax=masterEinspritzung.get_mKrst_Sum_ASP()
//						*masterEinspritzung.get_spezKrstALL().get_Hu_mass();
//				
//			}
//
//			@Override
//			public Zone[] calc_dQburn(Zone[] zonen) {
//				CanteraCaller cc=CP.CANTERA_CALLER;				
//				double []x, y; 
//				double[] TpX=new double[CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()+2]; 
//				double[] p_V_T_mi=new double[CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()+3]; 
//				
//				for(int i=0;i<zonen.length;i++){		
//					TpX[0]=zonen[i].get_T();
//					TpX[1]=zonen[i].get_p();
//					x=zonen[i].get_MoleFractions();	
//					for(int k=0;k<x.length;k++)
//						TpX[k+2]=x[k];
//					
//					cc.set_StateTpX(TpX);
//					cc.calcTimeStep(TpX, CP.SYS.WRITE_INTERVAL_SEC);
////					System.out.println("deltaQ_mass " + cc.get_deltaQ_mass()/CP.SYS.WRITE_INTERVAL_SEC);
////					System.out.println("dQ_mass " + cc.get_dQ_mass());
//					dQburn[i]=-1*cc.get_deltaQ_mass_h()*zonen[i].get_m()/CP.SYS.WRITE_INTERVAL_SEC;
//					//Auf diese Weise kommt etwas mehr Waerme ins System! 
//					//dQburn[i]=-1*cc.get_dQ_mass()*zonen[i].get_m();
//					dQburnSum=0;
//					for(int k=0;k<dQburn.length;k++) dQburnSum+=dQburn[k];
//					
//					p_V_T_mi[0]=zonen[i].get_p();
//					p_V_T_mi[1]=zonen[i].get_V();
//					p_V_T_mi[2]=5.55; //changed in the Zone to the according Value
//					y=cc.get_massFractions();
//					for(int k=0;k<x.length;k++)
//						p_V_T_mi[k+3]=y[k]*zonen[i].get_m();
//					
//					zonen[i].set_p_V_T_mi(p_V_T_mi);			
//				}		
//				return zonen;
//			}
//			
//			public SpecieAndTemp[] callCantera(Zone[] zonen){
//				CanteraCaller cc=CP.CANTERA_CALLER;				
//				double []x, y; 
//				double[] TpX=new double[CP.SPEZIES_FABRIK.get_NmbrOfAllSpez()+2]; 			
//				
//				SpecieAndTemp[] sT=new SpecieAndTemp[zonen.length];
//				
//				for(int i=0;i<zonen.length;i++){		
//					TpX[0]=zonen[i].get_T();
//					TpX[1]=zonen[i].get_p();
//					x=zonen[i].get_MoleFractions();	
//					for(int k=0;k<x.length;k++)
//						TpX[k+2]=x[k];
//					
//					cc.set_StateTpX(TpX);
//					cc.calcTimeStep(TpX, CP.SYS.WRITE_INTERVAL_SEC);
//					y=cc.get_massFractions();
//					Hashtable<Spezies, Double> ht=new Hashtable<Spezies,Double>();
//					
//					for(int j=0;j<y.length;j++)
//						ht.put(CP.SPEZIES_FABRIK.get_Spez(j), y[j]);
//					
//					GasGemisch spec=new GasGemisch("CanteraBurned_" +i);
//					spec.set_Gasmischung_massenBruch(ht);
//					
//					sT[i]=new SpecieAndTemp(spec,cc.get_T());				
//				}
//				return sT;
//			}
//			
//			
//
//			@Override
//			public boolean initialiseBurntZone() {		
//				return false;
//			}
//
//			@Override
//			protected Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {
//					double T0=zonen_IN[0].get_T();
//					Spezies spez0=zonen_IN[0].get_ggZone();
//					double V0=zonen_IN[0].get_V();
//					double V1=zonen_IN[1].get_V();
//					
//					
//					
//					zonen_IN[0].set_dQ_ein_aus(this.dQburn[0]);
//					zonen_IN[1].set_dQ_ein_aus(this.dQburn[1]);
//					
//					dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);
//					//GasolineZone
//					double dQw0=dQw*V0/(V0+V1);			
//					zonen_IN[0].set_dQ_ein_aus(-1*dQw0);		
//					//DieselZone
//					double dQw1=dQw*V1/(V0+V1);
//					zonen_IN[1].set_dQ_ein_aus(-1*dQw1);				
//					
//					//Verdampfungswaerme abfuehren
//					zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			
//
//					//Einspritzung des Kraftstoffs
//					zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);	
//					
//					
//					double dma=0;
//					for(int i=0;i<masterEinspritzung.get_AlleEinspritzungen().length;i++){	
//						Einspritzung es=masterEinspritzung.get_AlleEinspritzungen()[i];
//						if(es.get_ID_Zone()==1){
//							dma+=((Spray) es).get_dma(time);
//						}				
//					}	
//						
//					//Abfuehren des air entrainments aus der OttoZone
//					if(zonen_IN[0].get_m()>=CP.SYS.MINIMALE_ZONENMASSE){
//						try {
//							zonen_IN[0].set_dm_aus(dma);
//						} catch (NegativeMassException e) {
//							// Wenn die OttoZone ganz leer ist stimmt irgendwas nicht!
//							e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
//							dma=0;
//						}
//						//Zufuehren des air entrainments in die DieselZone
//						zonen_IN[1].set_dm_ein(dma,T0 ,spez0);
//					}				
//					return zonen_IN;					
//			}
//
//			@Override
//			public void bufferErgebnisse(double time, Zone[] zn) {
//				dQb_buffer.addValue(time, dQburnSum);	
//				dQw_buffer.addValue(time, dQw);	
//				dmb_buffer.addValue(time, 0);
//				Qb=Qb+dQburnSum*super.CP.SYS.WRITE_INTERVAL_SEC;
//				for(int i=0;i<zn.length;i++)
//					dm[i]=zn[i].get_m()/CP.SYS.WRITE_INTERVAL_SEC;
//				
//				fortschritt=Qb/QbMax;
//				
//				this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
//				
//				int i=-1;
//				i+=1;
//				super.buffer_EinzelErgebnis("CrankAngle [∞KW]",super.CP.convert_SEC2KW(time),i);
//
//				i+=1;
//				super.buffer_EinzelErgebnis("Time [s n. Rechenbeginn]",time,i);		
//
//				i+=1;
//				double V=motor.get_V(time);
//				super.buffer_EinzelErgebnis("Volume [m3]",V,i);
//				
//				i+=1;
//				super.buffer_EinzelErgebnis("Volume 0",zn[0].get_V()/V,i);
//				
//				i+=1;
//				super.buffer_EinzelErgebnis("Volume 1",zn[1].get_V()/V,i);
//				
//
//				i+=1;
//				super.buffer_EinzelErgebnis("T_Zone_0 [K]",zn[0].get_T(),i);
//				
//				i+=1;
//				super.buffer_EinzelErgebnis("T_Zone_1 [K]",zn[1].get_T(),i);
//
//				i+=1;
//				double Tm=wandWaermeModell.get_Tmb(zn);
//				super.buffer_EinzelErgebnis("T_m [K]",Tm,i);
//				T_buffer.addValue(time, Tm);
//				
//				i+=1;
//				double T_BurnAdiabat=HeizwertRechner.calcAdiabateFlammenTemp(super.CP,
//						 zn[0].get_ggZone(), zn[0].get_p(), zn[0].get_T());		
//				super.buffer_EinzelErgebnis("T_BurnAdiabat [K]", T_BurnAdiabat,i);
//
//				double pExp=indiD.get_pZyl(time);
//				i+=1;
//				super.buffer_EinzelErgebnis("p_exp [bar]", pExp*1E-5,i);
//
//				i+=1;
//				super.buffer_EinzelErgebnis("p [bar]",zn[0].get_p()*1e-5,i);
//
//				i+=1;
//				super.buffer_EinzelErgebnis("dQb [J/s]",dQburnSum,i);
//
//				i+=1;
//				super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburnSum),i);
//				
//				i+=1;
//				super.buffer_EinzelErgebnis("dQb0 [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn[0]),i);
//
//				i+=1;
//				super.buffer_EinzelErgebnis("dQb1 [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn[1]),i);
//				
//				i+=1;		
//				super.buffer_EinzelErgebnis("dQw [J]", super.CP.convert_ProSEC_2_ProKW(dQw),i);
//				
//				i+=1;		
//				super.buffer_EinzelErgebnis("Qb [J/CA]", Qb,i);
//				
//				i+=1;		
//				super.buffer_EinzelErgebnis("Qb/QbMax [J/CA]", Qb/QbMax,i);
//				
//				i+=1;
//				super.buffer_EinzelErgebnis("Zonenmasse [kg]", zn[0].get_m(),i);		
//
//				i+=1;
//				double kontrolMasse=zn[0].get_p()*zn[0].get_V();		
//				kontrolMasse=kontrolMasse/(zn[0].get_ggZone().get_R()*zn[0].get_T());
//				super.buffer_EinzelErgebnis("ZonenKontrollmasse [kg]", kontrolMasse,i);		
//				
//				
//				double []mi=zn[0].get_mi();
//				for(int idx=0;idx<mi.length;idx++){
//					i=i+1;
//					super.buffer_EinzelErgebnis("y0_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name(),
//							mi[idx]/zn[0].get_m(),i);		
//				}
//					
//				
//				double []mi1=zn[1].get_mi();
//				for(int idx=0;idx<mi.length;idx++){
//					i+=1;
//					super.buffer_EinzelErgebnis("y1_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name(),
//							mi1[idx]/zn[1].get_m(),i);
//				}
//		
//			}
//
//			@Override
//			public Zone[] get_initialZones() {
//				return initialZones;
//			}
//
//			@Override
//			public Zone[] get_initialZonesWhileRunning() {		
//				return null;
//			}
//
//			@Override
//			public int get_anzZonen() {
//				return nbrOfZones;
//			}
//
//			@Override
//			public VektorBuffer get_dm_buffer() {
//				// TODO Auto-generated method stub
//				return dmb_buffer;
//			}
//
//			@Override
//			public VektorBuffer get_dQw_buffer() {
//				// TODO Auto-generated method stub
//				return this.dQw_buffer;
//			}
//
//			@Override
//			public VektorBuffer get_dQb_buffer() {
//				// TODO Auto-generated method stub
//				return this.dQb_buffer;
//			}
//
//			@Override
//			protected void checkEinspritzungen(MasterEinspritzung me) {
//				for(int i=0;i<me.get_AlleEinspritzungen().length;i++){	
//					Einspritzung es=me.get_AlleEinspritzungen()[i];
//					if(es.get_ID_Zone()==0){
//						if(es.get_Krst().get_name().equalsIgnoreCase("nc7h16")){
//							try {
//								throw new ParameterFileWrongInputException("Fuer das gwaehlte Berechnungsmodell " +
//										"darf die DieselEinspritzung nicht " +
//								"in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone 0 ");
//							} catch (ParameterFileWrongInputException e) {				
//								e.stopBremo();
//							}
//						}				
//					}
//					if(es.get_ID_Zone()==1){
//						if(es.get_Krst().get_name().equalsIgnoreCase("nc7h16")==false){
//							//ToDo checken dass die EInspritzung ein pakestModell ist!
//							try {
//								throw new ParameterFileWrongInputException("F¸r das gwaehlte Berechnungsmodell " +
//										"darf ausschlieﬂlich Diesel in Zone 1 eingespritzwerden \n" +
//										"Eingespritzt wurde aber " +es.get_Krst().get_name());
//							} catch (ParameterFileWrongInputException e) {				
//								e.stopBremo();
//							}
//						}
//					}
//					if(es.get_ID_Zone()>1){				
//						try {
//							throw new ParameterFileWrongInputException("Einspritzungen sind ausschlieﬂlich in die " +
//									"Zonen 0 und 1 moeglich! \n" +
//									"Gewaehlt wurde aber Zone " +es.get_ID_Zone())  ;
//						} catch (ParameterFileWrongInputException e) {				
//							e.stopBremo();
//						}
//
//					}
//				}
//			}
//			
//			private class SpecieAndTemp{
//				private Spezies spec;
//				private double T;			
//				private SpecieAndTemp(Spezies spec, double T){
//					this.spec=spec;
//					this.T=T;
//				}
//				
//				public double get_T(){
//					return T;
//				}
//				
//				public Spezies get_Spec(){
//					return spec;
//				}
//				
//			}
//			
//			
//			
//	
//
//}
