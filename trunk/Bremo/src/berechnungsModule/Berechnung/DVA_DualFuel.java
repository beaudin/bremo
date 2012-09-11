package berechnungsModule.Berechnung;

import java.util.Hashtable;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import misc.HeizwertRechner;
import misc.LittleHelpers;
import misc.VektorBuffer;
import berechnungsModule.gemischbildung.Einspritzung;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.gemischbildung.Spray;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;


public class DVA_DualFuel extends DVA {

	private WandWaermeUebergang wandWaermeModell;
	private Motor motor;
	private GleichGewichtsRechner gg;
	private MasterEinspritzung masterEinspritzung;
	private final int ANZAHL_ZONEN;

	private double dQw, Qw=0, Qb=0,mb=0;
	double zonenMasseVerbrannt=0;

	/**
	 * <p>
	 * Liefert die sich zum Zeitpunkt time "Rechenbeginn" in der Zone befindliche  
	 * Gasmasse, bestehend aus Kraftstoffdampf (von allen Einspritzungen) und Verbrennungsluft 
	 * </p>
	 * mINIT=Masse aus mVerbrennungsluft+mKrst_dampf [kg]
	 */
	double mGes=-5.55;	
	double dmZoneBurn=0, Qmax;

	Zone []  initialZones;	
	Zone [] initialZonesWhileRunning;	

	private boolean esBrennt=false,initialiseZonesWhileRunning=false,burntZonesAlreadyInitialised=false,
					dieselZoneAusgeblendet=false;

	private double dQburnMAX=0;
	private double fortschritt=0;	


	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verf¸gung stehen sollen
	private VektorBuffer T_buffer;
	private VektorBuffer dQb_buffer;
	private VektorBuffer dmb_buffer;
	private VektorBuffer dQw_buffer;
	private boolean dieselIsBurnt=false;
	private boolean holzHammerOttoZoneNochNichtLeer=true,dieselZoneIstLeer=false; //Wenn die V0 bei Berechnung von reinem Dieselbetrieb zu null wird 


	protected DVA_DualFuel(CasePara cp) {
		super(cp);	

		ANZAHL_ZONEN=3;

		motor = CP.MOTOR;
		wandWaermeModell=CP.WAND_WAERME;	
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		checkEinspritzungen(masterEinspritzung); //checkt ob alle Einspritzungen in die richtigen Zonen erfolgen
		gg=CP.OHC_SOLVER;

		T_buffer = new VektorBuffer(cp);
		dQb_buffer = new VektorBuffer(cp);	
		dQw_buffer = new VektorBuffer(cp);	
		dmb_buffer=new VektorBuffer(cp);

		/////////////////////////////////////////////////////////////
		///////////Initialisieren der Anfangsbedingungen/////////////
		/////////////////////////////////////////////////////////////		
		initialZones=new Zone[ANZAHL_ZONEN];


		//Erzeugen des Frishgemischs in Zone 0 --> OttoKrst + Luft + AgrEx
		/////////////////////////////////////////////////////////////
		//enthaelt Luft und AgrEx
		double mVerbrennungsLuft=CP.get_mVerbrennungsLuft_ASP();
		//OttoKrst aus Zone 0
		double mKrstDampfINIT=
			masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0); 				
		//GesamtMasse bei Initialisierung
		double mGesInit= mVerbrennungsLuft+mKrstDampfINIT;
		
		//GesamtMasse am Ende des ASP
		this.mGes= mVerbrennungsLuft+masterEinspritzung.get_mKrst_Sum_ASP();


		//Erzeugen der KrstSpezies als MischungsSpezies aller bislang eingespritzten Kraftstoffe
		Spezies krst=masterEinspritzung.get_spezKrst_verdampft(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC,0);
		//Erzeugen der VerbrennungsluftSpezies als Mischung aus Luft torcken, Wasser und AgrEx
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	

		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGesInit);
		frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mGesInit);	
		GasGemisch gemischINIT=new GasGemisch("GemischINIT");	
		gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	

		//Anfangsbedingungen Setzen
		//p Init
		double p_init=super.indiD.get_pZyl(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		//V ges=V0+V1
		double V_ges=motor.get_V(CP.SYS.RECHNUNGS_BEGINN_DVA_SEC);
		//T Init -->fuer beide Zonen gleich	
		Spezies ggZone_init=gemischINIT;
		double R=ggZone_init.get_R();
		double T_init=(p_init*V_ges)/(mGesInit*R);	

		//Aufteilen in die Otto- und in die DieselZone
		//DieselZone
		//Masse der DieselZone
		//Initialisierung mit minimaler Masse damit die Zone Solver sichtbar ist
		double m1=1.000000001*CP.SYS.MINIMALE_ZONENMASSE; 
		//Volumen der DieselZone
		double V1=m1*R*T_init/p_init;
		this.initialZones[1]=new Zone(CP,p_init, V1, T_init, 
				m1,ggZone_init , false,1);
		//OttoZone
		//Masse der OttoZone
		double m0=mGesInit-2*m1;
		//Volumen der OttoZone
		double V0=m0*R*T_init/p_init;		
		//OttoZone
		this.initialZones[0]=new Zone(CP,p_init, V0, T_init, 
				m0,ggZone_init , false,0);


		//verbrannte Zone--> wird spaeter nochmal initialisiert
		GasGemisch rauchgas=new GasGemisch("Rauchgas");
		Hashtable <Spezies,Double> ht=new Hashtable<Spezies,Double>();
		ht.put(CP.SPEZIES_FABRIK.get_spezCO2(), 1D); //nur als Dummi belegung --> sonst gibt's nen Fehler
		rauchgas.set_Gasmischung_molenBruch(ht);

		double V2=m1*rauchgas.get_R()*T_init/p_init;
		this.initialZones[2]=new Zone(CP,p_init, V2, T_init, 
				m1,rauchgas, true,2);
		//		this.initialZones[2]=new Zone(1, 1e-55, 1, 
		//				1e-55,rauchgas, true,1);
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25∞C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();	

	}


	public int get_anzZonen() {		
		return ANZAHL_ZONEN;
	}


	public Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) {			

		double dQburn=super.get_dQburn();

		//aktueller Zustand in der OttoZone
		double p=zonen_IN[0].get_p();
		double T0=zonen_IN[0].get_T();
		Spezies spez0=zonen_IN[0].get_ggZone();
		double V0=zonen_IN[0].get_V();		

		double m1=zonen_IN[1].get_m();
		double T1=zonen_IN[1].get_T();
		Spezies spez1=zonen_IN[1].get_ggZone();
		double V1=zonen_IN[1].get_V();

		double V2=zonen_IN[2].get_V();	
		
		//Wenn V0 einmal kleienr null wird, wird keine Masse mehr aus der Zone entnommen --> Holzhammer
		if(esBrennt&&V0<=0||esBrennt&&zonen_IN[0].get_m()<=0 ){
			holzHammerOttoZoneNochNichtLeer=false; 
			V0=1E-55;
		}
		//Wenn V1 einmal kleiner null wird, wird keine Masse mehr aus der Zone entnommen --> Holzhammer
		if(V1<=0 ||zonen_IN[1].get_m()<=0 ){
			dieselZoneIstLeer=true;
			V1=1E-55;
		}


		dmZoneBurn=0;	

		if(esBrennt&&dQburn>0){				
			if(m1>=CP.SYS.MINIMALE_ZONENMASSE&&dieselIsBurnt==false){//DieselZone hat schon oder immer noch genug Masse
				dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,spez1,T1,p);

				if(burntZonesAlreadyInitialised){
					try {
						zonen_IN[1].set_dm_aus(dmZoneBurn);
						//Zone 2 wird das verbrannte Massenelement aus Zone 1 zugefuehrt
						GasGemisch rauchgas;				
						rauchgas=new GasGemisch("Rauchgas");
						rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, T1, spez1));	
						//	Berechnung der Dissoziation erfolgt in der Zone automatisch				
						zonen_IN[2].set_dm_ein(dmZoneBurn,T1,rauchgas);	
					} catch (NegativeMassException nmE) {					
						//Dies bedeutet, dass der Diesel vollst. verbrannt ist.
						dieselIsBurnt=true;
						dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,spez0,T0,p);
						try {
							zonen_IN[0].set_dm_aus(dmZoneBurn);
							//Zone 2 wird das verbrannte Massenelement aus Zone 0 zugefuehrt
							GasGemisch rauchgas;				
							rauchgas=new GasGemisch("Rauchgas");
							rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, T0, spez0));	
							//	Berechnung der Dissoziation erfolgt in der Zone automatisch				
							zonen_IN[2].set_dm_ein(dmZoneBurn,T0,rauchgas);	
						} catch (NegativeMassException nmE2) {					
							nmE.log_Warning();
							dmZoneBurn=0;
						}
					}
				}
			}else{//DieselZone ist verbrannt oder sonst iergendwie leer
				dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,spez0,T0,p);

				if(burntZonesAlreadyInitialised&&holzHammerOttoZoneNochNichtLeer){
					try {
						zonen_IN[0].set_dm_aus(dmZoneBurn,spez0);
						//Zone 2 wird das verbrannte Massenelement aus Zone 0 zugefuehrt
						GasGemisch rauchgas;				
						rauchgas=new GasGemisch("Rauchgas");
						rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, T0, spez0));	
						//	Berechnung der Dissoziation erfolgt in der Zone automatisch				
						zonen_IN[2].set_dm_ein(dmZoneBurn,T0,rauchgas);	
					} catch (NegativeMassException nmE) {					
						nmE.log_Warning();
						dmZoneBurn=0;
					}
				}
			}

			if(burntZonesAlreadyInitialised)
				zonen_IN[2].set_dQ_ein_aus(dQburn);
			else{
				//Verbrennungswaerme zufuehren
				double dQburn0=dQburn*V0/(V0+V1);
				//OttoZone
				zonen_IN[0].set_dQ_ein_aus(dQburn0);	
				//DieselZone 
				//Wenn die Diesel Zone ausgeblendet wurde ist das Volumen so klein, 
				//dass eine Unterscheidung hier unnoetig ist
				double dQburn1=dQburn*V1/(V0+V1);
				zonen_IN[1].set_dQ_ein_aus(dQburn1);
			}				
		}else{			
				//Verbrennungswaerme zufuehren
				double dQburn0=dQburn*V0/(V0+V1);
				//OttoZone
				zonen_IN[0].set_dQ_ein_aus(dQburn0);	
				//DieselZone
				//Wenn die Diesel Zone ausgeblendet wurde ist das Volumen so klein, 
				//dass eine Unterscheidung hier unnoetig ist
				double dQburn1=dQburn*V1/(V0+V1);
				zonen_IN[1].set_dQ_ein_aus(dQburn1);				


				//				//Verbrennungswaerme zufuehren
				//				double dQburn0=dQburn*V0/(V0+V1+V2);
				//				//OttoZone
				//				zonen_IN[0].set_dQ_ein_aus(dQburn0);	
				//				//DieselZone
				//				double dQburn1=dQburn*V1/(V0+V1+V2);
				//				zonen_IN[1].set_dQ_ein_aus(dQburn1);			
				//				//verbrannte Zone
				//				double dQburn2=dQburn*V2/(V0+V1+V2);
				//				zonen_IN[2].set_dQ_ein_aus(dQburn2);	

		}

		//Wandwaermestrom bestimmen			
		dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt, T_buffer);			

		//Wandwaermestrom abfuehren
		//OttoZone
		double dQw0=dQw*V0/(V0+V1+V2);			
		zonen_IN[0].set_dQ_ein_aus(-1*dQw0);		
		//DieselZone
		double dQw1=dQw*V1/(V0+V1+V2);
		zonen_IN[1].set_dQ_ein_aus(-1*dQw1);			
		//verbrannte Zone
		double dQw2=dQw*V2/(V0+V1+V2);
		zonen_IN[2].set_dQ_ein_aus(-1*dQw2);

		//Verdampfungswaerme abfuehren
		zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

		//Einspritzung des Kraftstoffs
		zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);

		if(dieselIsBurnt==false){
			//Bestimmung des air entrainments in die DieselZone
			double dma=0;
			for(int i=0;i<masterEinspritzung.get_AlleEinspritzungen().length;i++){	
				Einspritzung es=masterEinspritzung.get_AlleEinspritzungen()[i];
				if(es.get_ID_Zone()==1){
					dma+=((Spray) es).get_dma(time);
				}				
			}
//			if(esBrennt==true)
//				dma=0.7*dma; //aus Paper Hiroyasu --> verringerung des air entrainments wenn es brennt 
		
				
			//			Abfuehren des air entrainments aus der OttoZone
			if(zonen_IN[0].get_m()>=CP.SYS.MINIMALE_ZONENMASSE &&holzHammerOttoZoneNochNichtLeer){
				try {
					zonen_IN[0].set_dm_aus(dma);
				} catch (NegativeMassException e) {
					// Wenn die OttoZone ganz leer ist stimmt irgendwas nicht!
					e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
					dma=0;
				}
				//Zufuehren des air entrainments in die DieselZone
				zonen_IN[1].set_dm_ein(dma,T0 ,spez0);
			}
		}

		return zonen_IN;	
	}

	public Zone[] get_initialZones() {		
		return initialZones;
	}


	private double mDieselZoneMAX=0;
	public void bufferErgebnisse(double time, Zone[] zn) {			
		double dQburn=super.get_dQburn();
		//bei jedem Rechenschritt wird der Buffer mit den dQb-Werten aufgef¸llt
		dQb_buffer.addValue(time, dQburn);
		dmb_buffer.addValue(time,dmZoneBurn);
		dQw_buffer.addValue(time, dQw);

		if(dQburn>dQburnMAX)dQburnMAX=dQburn;
		
		esBrennt=super.verbrennungHatBegonnen(time, dQb_buffer);

		if(esBrennt==true){
			this.mb=this.mb+this.dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		}

		if(mb>=super.CP.SYS.MINIMALE_ZONENMASSE && burntZonesAlreadyInitialised==false){
			this.initBurnedZone(time, zn);
			initialiseZonesWhileRunning=true;
		}
		//Wenn die Dieselzone zu wenig Masse beinhaltet, wird sie der OttoZone zugemischt
		//zunaechst Bestimmung der maximalen Masse der DieselZone
		if(zn[1].get_m()>mDieselZoneMAX)
			mDieselZoneMAX=zn[1].get_m();
		
//		dieselZoneIstLeer==true&&burntZonesAlreadyInitialised==true&&dieselZoneAusgeblendet==false||
		if(zn[1].get_m()<=0.005*mDieselZoneMAX&&this.dieselIsBurnt==false||
				this.dieselIsBurnt==true&&burntZonesAlreadyInitialised==true&&dieselZoneAusgeblendet==false||
				zn[1].get_m()<=1.05*CP.SYS.MINIMALE_ZONENMASSE &&burntZonesAlreadyInitialised==true&&dieselZoneAusgeblendet==false){
			dieselZoneAusblenden(zn[0],zn[1],zn[2]);
			this.initialiseZonesWhileRunning=true;	
			dieselZoneAusgeblendet=true;
		}

		//Berechnen integraler Werte
//		zonenMasseVerbrannt=zonenMasseVerbrannt+dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		double a=zn[2].get_m();
		zonenMasseVerbrannt=a;
		fortschritt=zonenMasseVerbrannt/mGes;
		Qb=Qb+dQburn*super.CP.SYS.WRITE_INTERVAL_SEC;
		Qw=Qw+dQw*super.CP.SYS.WRITE_INTERVAL_SEC; 
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
		double xQ=Qb/Qmax;

		int i=-1;
		i+=1;
		super.buffer_EinzelErgebnis("Kurbelwinkel [∞KW]",super.CP.convert_SEC2KW(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_EinzelErgebnis("Brennraumvolumen [m3]",motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("V0 [m3]",zn[0].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("V1 [m3]",zn[1].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("V2 [m3]",zn[2].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_EinzelErgebnis("T_Zone_0 [K]",zn[0].get_T(),i);

		i+=1;
		super.buffer_EinzelErgebnis("T_Zone_1 [K]",zn[1].get_T(),i);

		i+=1;
		super.buffer_EinzelErgebnis("T_Zone_2 [K]",zn[2].get_T(),i);

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_EinzelErgebnis("T_mittel [K]",Tm,i);
		T_buffer.addValue(time, Tm);

		i+=1;
		double T_BurnAdiabat_0=0;
		if(zn[0].get_ggZone().get_Hu_mol()>0&& this.holzHammerOttoZoneNochNichtLeer){
			T_BurnAdiabat_0=HeizwertRechner.calcAdiabateFlammenTemp(CP,
				zn[0].get_ggZone(), zn[0].get_p(), zn[0].get_T());	
		}

		super.buffer_EinzelErgebnis("T_BurnAdiabat_0 [K]", T_BurnAdiabat_0,i);

		i+=1;
		double T_BurnAdiabat_1=0;
		if(zn[1].get_ggZone().get_Hu_mol()>0 &&this.dieselZoneIstLeer==false){		
			T_BurnAdiabat_1=HeizwertRechner.calcAdiabateFlammenTemp(CP,
					zn[1].get_ggZone(), zn[1].get_p(), zn[1].get_T());	
		}
		super.buffer_EinzelErgebnis("T_BurnAdiabat_1 [K]", T_BurnAdiabat_1,i);
		
		
		i+=1;
		super.buffer_EinzelErgebnis("Lambda z1 [-]", zn[1].get_ggZone().get_lambda(),i);
		
		i+=1;
		super.buffer_EinzelErgebnis("Lambda z0 [-]", zn[0].get_ggZone().get_lambda(),i);
		
		i+=1;
		super.buffer_EinzelErgebnis("Lambda z2 [-]", zn[2].get_ggZone().get_lambda(),i);


		double pSoll=indiD.get_pZyl(time);
		i+=1;
		super.buffer_EinzelErgebnis("p_soll [bar]", pSoll*1E-5,i);
		
		i+=1;
		super.buffer_EinzelErgebnis("p_soll_roh [bar]", indiD.get_pZylRoh(time)*1E-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("p [bar]",zn[0].get_p()*1e-5,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/s]",dQburn,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn),i);

		i+=1;		
		super.buffer_EinzelErgebnis("Qb [J]", Qb,i);

		i+=1;
		super.buffer_EinzelErgebnis("dmb [kg/s]", dmZoneBurn,i);

		i+=1;
		super.buffer_EinzelErgebnis("dmb [kg/KW]", super.CP.convert_ProSEC_2_ProKW(dmZoneBurn),i);

		i+=1;	
		super.buffer_EinzelErgebnis("mb [kg]", zonenMasseVerbrannt,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQw [J/s]",dQw,i);

		i+=1;
		super.buffer_EinzelErgebnis("dQw [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQw),i);	
		
		i+=1;
		double dQkrst=this.masterEinspritzung.get_dQ_krstDampf(time, zn);
		super.buffer_EinzelErgebnis("dQKrst [J/s]",dQkrst ,i);	
		i+=1;
		super.buffer_EinzelErgebnis("dQKrst [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQkrst),i);	

		i+=1;		
		super.buffer_EinzelErgebnis("Qw [J]",Qw,i);		

		i+=1;		
		super.buffer_EinzelErgebnis("Xb[-]", fortschritt,i);


		i+=1;		
		super.buffer_EinzelErgebnis("Qb/Qmax [-]", xQ,i);

		i+=1;
		super.buffer_EinzelErgebnis("Qmax [J]", Qmax,i);		

		i+=1;
		double alpha=wandWaermeModell.get_WaermeUebergangsKoeffizient(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("Alpha [W/(m^2K)]", alpha, i);
		
		i+=1;
		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zn, fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("Wandw‰rmestromdichte [MW/m^2]",HeatFlux*1E-6,i);

		i+=1;		
		super.buffer_EinzelErgebnis("PQw [W]",HeatFlux*motor.get_BrennraumFlaeche(time),i);	

		i+=1;		
		super.buffer_EinzelErgebnis("Brennraumfl‰che [m^2]",motor.get_BrennraumFlaeche(time),i);		

		i+=1;
		super.buffer_EinzelErgebnis("Gesamtmasse [kg]", zn[0].get_m()+zn[1].get_m()+zn[2].get_m(),i);

		i+=1;		
		super.buffer_EinzelErgebnis("m_0[kg]", zn[0].get_m(),i);

		i+=1;		
		super.buffer_EinzelErgebnis("m_1[kg]", zn[1].get_m(),i);

		i+=1;		
		super.buffer_EinzelErgebnis("m_2[kg]", zn[2].get_m(),i);


		i+=1;
		int iter=i;		
		double []mi=zn[0].get_mi();
		double m_ges=zn[0].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_EinzelErgebnis("Z0_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,i);			
		}

		i+=1;
		iter=i;	
		mi=zn[1].get_mi();
		m_ges=zn[1].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_EinzelErgebnis("Z1_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,iter+idx);
		}

		i+=1;
		iter=i;	
		mi=zn[2].get_mi();
		m_ges=zn[2].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_EinzelErgebnis("Z2_"+CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,iter+idx);
		}		
		i+=1;		
		super.buffer_EinzelErgebnis(" mDieseldampf [kg]",
				this.masterEinspritzung.get_Einspritzung(0).get_mKrst_verdampft(time),i);
		
		i+=1;
		Spezies diesel=CP.SPEZIES_FABRIK.get_spezDiesel();
		if(zn[1].get_ggZone().get_speziesMassenBrueche().containsKey(diesel)){
			super.buffer_EinzelErgebnis("m_Diesel_Z0 [kg]", zn[1].get_m()*zn[1].get_ggZone().get_speziesMassenBrueche().get(diesel),i);
		}else{
			super.buffer_EinzelErgebnis("m_Diesel_Z0 [kg]", 0,i);
		}
		

		double ma=0,s=0,dma=0,mkrstFl=0,T_KRstVap=0;
		for(int x=0;x<masterEinspritzung.get_AlleEinspritzungen().length;x++){			
			Einspritzung es=masterEinspritzung.get_AlleEinspritzungen()[x];	
			if(es.get_ID_Zone()==1){
				ma=((Spray)es).get_ma(time);					
				sumdma+=((Spray)es).get_dma(time)*CP.SYS.WRITE_INTERVAL_SEC;
				s=((Spray)es).get_paket(0, 0).get_s(time);
				dma=((Spray)es).get_dma(time);
				mkrstFl=es.get_mKrst_fluessig(time);
				T_KRstVap=es.get_Tkrst_dampf(time,zn[es.get_ID_Zone()]);
			}
		}
		
		i+=1;
		super.buffer_EinzelErgebnis("TKrstVap [K]", T_KRstVap,i);
		i+=1;
		super.buffer_EinzelErgebnis("mkrstFl [kg]", mkrstFl,i);		
		i+=1;
		super.buffer_EinzelErgebnis("m_a [kg]", ma,i);
		i+=1;
		super.buffer_EinzelErgebnis("sumdma [kg]", sumdma,i);
		i+=1;
		super.buffer_EinzelErgebnis("s00 [kg]", s,i);
		i+=1;
		super.buffer_EinzelErgebnis("dm_a [kg]", dma,i);	
	}
	
double sumdma=0;







	private void initBurnedZone(double time, Zone[] zonen_IN){

		initialZonesWhileRunning=new Zone[zonen_IN.length];
		double p=zonen_IN[0].get_p();
		double T1= zonen_IN[1].get_T();
		Spezies spez1=zonen_IN[1].get_ggZone();

		double Tb=HeizwertRechner.calcAdiabateFlammenTemp(CP,spez1, p,T1); 
		//		double =T_BurnAdiabat;	


		GasGemisch rauchgas=new GasGemisch("rg");
		rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, Tb, spez1));
		double V2=this.mb*rauchgas.get_R()*Tb/p;
		//		//Berechnen der Einzelmassen in der verbrannten Zone
		Hashtable<Spezies,Double> ht=new Hashtable<Spezies,Double>();		
		ht=LittleHelpers.addiereHashs(ht, 
				rauchgas.get_speziesMassenBruecheDetailToIntegrate(), mb);
		initialZonesWhileRunning[2]=new Zone(CP,zonen_IN[2].get_p_V_T_mi(),zonen_IN[2].isBurnt(),zonen_IN[2].getID());
		initialZonesWhileRunning[2].set_p_V_T_mi(p, V2, Tb,ht);

		double m1=zonen_IN[1].get_m()-mb; 

		double V1=m1*spez1.get_R()*T1/p;//=motor.get_V(time)-V2-zonen_IN[0].get_V();
		//		T1=p*V1/(m1*spez1.get_R());			

		//		//Berechnen der Einzelmassen in der unverbrannten Zone
		Hashtable<Spezies,Double> ht2=new Hashtable<Spezies,Double>();		
		ht2=LittleHelpers.addiereHashs(ht2, 
				zonen_IN[1].get_ggZone().get_speziesMassenBruecheDetailToIntegrate(), m1);

		initialZonesWhileRunning[1]=new Zone(CP,zonen_IN[1].get_p_V_T_mi(),zonen_IN[1].isBurnt(),zonen_IN[1].getID());
		initialZonesWhileRunning[1].set_p_V_T_mi(p,V1, T1,ht2);	
		//OttoZone 
		double V0=motor.get_V(time)-V1-V2;
		double m0=zonen_IN[0].get_m();
		double R0=zonen_IN[0].get_ggZone().get_R();
		double T0=p*V0/m0/R0;
		//Berechnen der Einzelmassen in der OttoZone
		Hashtable<Spezies,Double> ht3=new Hashtable<Spezies,Double>();	
//		Hashtable<Spezies,Double> ht4=zonen_IN[0].get_ggZone().get_speziesMassenBruecheDetailToIntegrate();
		ht3=LittleHelpers.addiereHashs(ht3, 
				zonen_IN[0].get_ggZone().get_speziesMassenBruecheDetailToIntegrate(), m0);		
		initialZonesWhileRunning[0]=new Zone(CP,zonen_IN[0].get_p_V_T_mi(),zonen_IN[0].isBurnt(),zonen_IN[0].getID());		
		initialZonesWhileRunning[0].set_p_V_T_mi(p, V0, T0, ht3);

		burntZonesAlreadyInitialised=true;		
		//		initialZonesWhileRunning=zonen_IN;		
	}

	private void dieselZoneAusblenden( Zone zOtto ,Zone zDiesel,Zone zBurnt){		
		this.initialZonesWhileRunning[0]=Zone.zonenMischen(CP,zDiesel, zOtto, false, 0);
		this.initialZonesWhileRunning[1]=new Zone(CP,zDiesel.get_p(), 1e-55, 1, 1e-55, 
				zDiesel.get_ggZone(), false, 1);
		this.initialZonesWhileRunning[2]=zBurnt;
		this.dieselIsBurnt=true;
	}


	public Zone[] get_initialZonesWhileRunning() {	
		initialiseZonesWhileRunning=false; //Beim naechsten mal soll es nicht mehr abgefragt werden
		return initialZonesWhileRunning;
	}


	public boolean initialiseBurntZone() {		
		return initialiseZonesWhileRunning;
	}


	/**
	 * Diese Methode ueberprueft ob alle Einspritzungen in die richtigen Zonen erfolgen. <br/>
	 * Otto in Zone 0 <br/>
	 * Diesel in Zone 1
	 */
	protected void checkEinspritzungen(MasterEinspritzung me) {
		for(int i=0;i<me.get_AlleEinspritzungen().length;i++){	
			Einspritzung es=me.get_AlleEinspritzungen()[i];
			if(es.get_ID_Zone()==0){
				if(es.get_Krst().get_name().equalsIgnoreCase("Diesel")){
					try {
						throw new ParameterFileWrongInputException("Fuer das gwaehlte Berechnungsmodell " +
								"darf die DieselEinspritzung nicht " +
						"in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone 0 ");
					} catch (ParameterFileWrongInputException e) {				
						e.stopBremo();
					}
				}				
			}
			if(es.get_ID_Zone()==1){
				if(es.get_Krst().get_name().equalsIgnoreCase("Diesel")==false){
					//ToDo checken dass die EInspritzung ein pakestModell ist!
					try {
						throw new ParameterFileWrongInputException("F¸r das gwaehlte Berechnungsmodell " +
								"darf ausschlieﬂlich Diesel in Zone 1 eingespritzwerden \n" +
								"Eingespritzt wurde aber " +es.get_Krst().get_name());
					} catch (ParameterFileWrongInputException e) {				
						e.stopBremo();
					}
				}
			}
			if(es.get_ID_Zone()>1){				
				try {
					throw new ParameterFileWrongInputException("Einspritzungen sind ausschlieﬂlich in die " +
							"Zonen 0 und 1 moeglich! \n" +
							"Gewaehlt wurde aber Zone " +es.get_ID_Zone())  ;
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}

			}
		}	

	}


	@Override
	public VektorBuffer get_dQw_buffer() {
		return this.dQw_buffer;
	}
	
	@Override
	public VektorBuffer get_dQb_buffer() {
		return this.dQb_buffer;
	}


	@Override
	public VektorBuffer get_dm_buffer() {
		return this.dmb_buffer;
	}
	
	public VektorBuffer get_p_buffer() {
		// TODO Auto-generated method stub
		return null;
		}

}
