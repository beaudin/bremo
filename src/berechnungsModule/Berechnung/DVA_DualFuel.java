package berechnungsModule.Berechnung;

import java.util.Hashtable;

import kalorik.GasMixture;
import kalorik.Spezies;
import misc.LHV_Calculator;
import misc.LittleHelpers;
import misc.VectorBuffer;
import berechnungsModule.chemEquilibriumSolver.ChemEquilibriumSolver;
import berechnungsModule.mixtureFormation.Injection;
import berechnungsModule.mixtureFormation.MasterInjection;
import berechnungsModule.mixtureFormation.Spray;
import berechnungsModule.motor.Motor;
import berechnungsModule.wallHeatTransfer.WallHeatTransfer;
import bremo.parameter.CasePara;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;


public class DVA_DualFuel extends DVA {

	private WallHeatTransfer wandWaermeModell;
	private Motor motor;
	private ChemEquilibriumSolver gg;
	private MasterInjection masterEinspritzung;
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
	private VectorBuffer T_buffer;
	private VectorBuffer dQb_buffer;
	private VectorBuffer dmb_buffer;
	private VectorBuffer dQw_buffer;
	private boolean dieselIsBurnt=false;
	


	protected DVA_DualFuel(CasePara cp) {
		super(cp);	

		ANZAHL_ZONEN=3;

		motor = CP.MOTOR;
		wandWaermeModell=CP.WALL_HEAT_TRANSFER_MODEL;	
		masterEinspritzung=CP.MASTER_INJECTION;
		checkInjections(masterEinspritzung); //checkt ob alle Einspritzungen in die richtigen Zonen erfolgen
		gg=CP.OHC_SOLVER;

		T_buffer = new VectorBuffer(cp);
		dQb_buffer = new VectorBuffer(cp);	
		dQw_buffer = new VectorBuffer(cp);	
		dmb_buffer=new VectorBuffer(cp);

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
			masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.SYS.SIMULATION_START_SEC,0); 				
		//GesamtMasse bei Initialisierung
		double mGesInit= mVerbrennungsLuft+mKrstDampfINIT;
		
		//GesamtMasse am Ende des ASP
		this.mGes= mVerbrennungsLuft+masterEinspritzung.get_mFuel_Sum_Cycle();


		//Erzeugen der KrstSpezies als MischungsSpezies aller bislang eingespritzten Kraftstoffe
		Spezies krst=masterEinspritzung.get_spezKrst_verdampft(CP.SYS.SIMULATION_START_SEC,0);
		//Erzeugen der VerbrennungsluftSpezies als Mischung aus Luft torcken, Wasser und AgrEx
		Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuft();	

		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGesInit);
		frischGemisch_MassenbruchHash.put(krst, mKrstDampfINIT/mGesInit);	
		GasMixture gemischINIT=new GasMixture("GemischINIT");	
		gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	

		//Anfangsbedingungen Setzen
		//p Init
		double p_init=super.indiD.get_pZyl(CP.SYS.SIMULATION_START_SEC);
		//V ges=V0+V1
		double V_ges=motor.get_V(CP.SYS.SIMULATION_START_SEC);
		//T Init -->fuer beide Zonen gleich	
		Spezies ggZone_init=gemischINIT;
		double R=ggZone_init.get_R();
		double T_init=(p_init*V_ges)/(mGesInit*R);	

		//Aufteilen in die Otto- und in die DieselZone
		//DieselZone
		//Masse der DieselZone
		//Initialisierung mit minimaler Masse damit die Zone Solver sichtbar ist
		double m1=1.000000001*CP.SYS.MINIMUM_ZONE_MASS; 
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
		GasMixture rauchgas=new GasMixture("Rauchgas");
		Hashtable <Spezies,Double> ht=new Hashtable<Spezies,Double>();
		ht.put(ggZone_init, 1D);
		rauchgas.set_Gasmixture_moleFracs(ht);

		double V2=m1*rauchgas.get_R()*T_init/p_init;
		this.initialZones[2]=new Zone(CP,p_init, V2, T_init, 
				m1,rauchgas, true,2);
		//		this.initialZones[2]=new Zone(1, 1e-55, 1, 
		//				1e-55,rauchgas, true,1);
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25∞C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mFuel_Sum_Cycle()*masterEinspritzung.get_spezKrstALL().get_LHV_mass();	

	}


	public int get_nbrOfZones() {		
		return ANZAHL_ZONEN;
	}


	public Zone[] firstLawCombustionChamber(double time, Zone[] zonen_IN) {			

		double dQburn=super.get_dQburn();

		//aktueller Zustand in der OttoZone
		double p=zonen_IN[0].get_p();
		double T0=zonen_IN[0].get_T();
		Spezies spez0=zonen_IN[0].get_gasMixtureZone();
		double V0=zonen_IN[0].get_V();
		double m0=zonen_IN[0].get_m();

		double m1=zonen_IN[1].get_m();
		double T1=zonen_IN[1].get_T();
		Spezies spez1=zonen_IN[1].get_gasMixtureZone();
		double V1=zonen_IN[1].get_V();

		double V2=zonen_IN[2].get_V();	

		dmZoneBurn=0;	

		if(esBrennt&&dQburn>0){	
			dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,spez1,T1,p);
			//DieselZone hat schon oder immer noch genug Masse
			if(m1-CP.SYS.MINIMUM_ZONE_MASS>dmZoneBurn*CP.SYS.WRITE_INTERVAL_SEC
					&&dieselIsBurnt==false&&burntZonesAlreadyInitialised){				
						try {
							zonen_IN[1].set_dm_out(dmZoneBurn);
							//Zone 2 wird das verbrannte Massenelement aus Zone 1 zugefuehrt
							GasMixture rauchgas;				
							rauchgas=new GasMixture("Rauchgas");
							rauchgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(p, T1, spez1));	
							//	Berechnung der Dissoziation erfolgt in der Zone automatisch				
							zonen_IN[2].set_dm_in(dmZoneBurn,T1,rauchgas);	
						} catch (NegativeMassException nmE) {
							//Dies bedeutet, dass der Diesel vollst. verbrannt ist.
							dieselIsBurnt=true;
							dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,spez0,T0,p);
							try {
								zonen_IN[0].set_dm_out(dmZoneBurn);
								//Zone 2 wird das verbrannte Massenelement aus Zone 0 zugefuehrt
								GasMixture rauchgas;				
								rauchgas=new GasMixture("Rauchgas");
								rauchgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(p, T0, spez0));	
								//	Berechnung der Dissoziation erfolgt in der Zone automatisch				
								zonen_IN[2].set_dm_in(dmZoneBurn,T0,rauchgas);	
							} catch (NegativeMassException nmE2) {					
								nmE2.log_Warning();
								dmZoneBurn=0;
							}
						}				
			}else{//DieselZone ist verbrannt oder sonst iergendwie leer	
				if(burntZonesAlreadyInitialised)
					dieselIsBurnt=true;	
				dmZoneBurn=super.convert_dQburn_2_dmKrstBurn(dQburn,spez0,T0,p);							
				if(burntZonesAlreadyInitialised&&
						m0-CP.SYS.MINIMUM_ZONE_MASS>dmZoneBurn*CP.SYS.MINIMUM_ZONE_MASS){					
					try {
						zonen_IN[0].set_dm_aus(dmZoneBurn,spez0);
						//Zone 2 wird das verbrannte Massenelement aus Zone 0 zugefuehrt
						GasMixture rauchgas;				
						rauchgas=new GasMixture("Rauchgas");
						rauchgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(p, T0, spez0));	
						//	Berechnung der Dissoziation erfolgt in der Zone automatisch				
						zonen_IN[2].set_dm_in(dmZoneBurn,T0,rauchgas);	
					} catch (NegativeMassException nmE) {					
						nmE.log_Warning();
						dmZoneBurn=0;
					}
				}
			}

			if(burntZonesAlreadyInitialised)
				zonen_IN[2].set_dQ_in_out(dQburn);
			else{
				//Verbrennungswaerme zufuehren
				double dQburn0=dQburn*V0/(V0+V1);
				//OttoZone
				zonen_IN[0].set_dQ_in_out(dQburn0);	
				//DieselZone 
				//Wenn die Diesel Zone ausgeblendet wurde ist das Volumen so klein, 
				//dass eine Unterscheidung hier unnoetig ist
				double dQburn1=dQburn*V1/(V0+V1);
				zonen_IN[1].set_dQ_in_out(dQburn1);
			}				
		}else{			
			//				//Verbrennungswaerme zufuehren
			//				double dQburn0=dQburn*V0/(V0+V1);
			//				//OttoZone
			//				zonen_IN[0].set_dQ_ein_aus(dQburn0);	
			//				//DieselZone
			//				//Wenn die Diesel Zone ausgeblendet wurde ist das Volumen so klein, 
			//				//dass eine Unterscheidung hier unnoetig ist
			//				double dQburn1=dQburn*V1/(V0+V1);
			//				zonen_IN[1].set_dQ_ein_aus(dQburn1);
			//Verbrennungswaerme zufuehren
			double dQburn0=dQburn*V0/(V0+V1+V2);
			//OttoZone
			zonen_IN[0].set_dQ_in_out(dQburn0);	
			//DieselZone
			double dQburn1=dQburn*V1/(V0+V1+V2);
			zonen_IN[1].set_dQ_in_out(dQburn1);			
			//verbrannte Zone
			double dQburn2=dQburn*V2/(V0+V1+V2);
			zonen_IN[2].set_dQ_in_out(dQburn2);
		}

		//Wandwaermestrom bestimmen			
		dQw=wandWaermeModell.get_wallHeatFlux(time, zonen_IN, fortschritt, T_buffer);			

		//Wandwaermestrom abfuehren
		//OttoZone
		double dQw0=dQw*V0/(V0+V1+V2);			
		zonen_IN[0].set_dQ_in_out(-1*dQw0);		
		//DieselZone
		double dQw1=dQw*V1/(V0+V1+V2);
		zonen_IN[1].set_dQ_in_out(-1*dQw1);			
		//verbrannte Zone
		double dQw2=dQw*V2/(V0+V1+V2);
		zonen_IN[2].set_dQ_in_out(-1*dQw2);

		//Verdampfungswaerme abfuehren
		zonen_IN=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);			

		//Einspritzung des Kraftstoffs
		zonen_IN=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);

		if(dieselIsBurnt==false){
			//Bestimmung des air entrainments in die DieselZone
			double dma=0;
			for(int i=0;i<masterEinspritzung.get_allInjections().length;i++){	
				Injection es=masterEinspritzung.get_allInjections()[i];
				if(es.get_ID_Zone()==1){
					dma+=((Spray) es).get_dma(time);
				}				
			}
//			if(esBrennt==true)
//				dma=0.7*dma; //aus Paper Hiroyasu --> verringerung des air entrainments wenn es brennt 
		
				
			//			Abfuehren des air entrainments aus der OttoZone
			if(zonen_IN[0].get_m()-CP.SYS.MINIMUM_ZONE_MASS>dma*CP.SYS.WRITE_INTERVAL_SEC){
				try {
					zonen_IN[0].set_dm_out(dma);
				} catch (NegativeMassException e) {
					// Wenn die OttoZone ganz leer ist stimmt irgendwas nicht!
					e.log_Warning("Die OttoZone ist leer. Da ist was faul!");
					dma=0;
				}
				//Zufuehren des air entrainments in die DieselZone
				zonen_IN[1].set_dm_in(dma,T0 ,spez0);
			}
		}
		return zonen_IN;	
	}

	public Zone[] get_initialZones() {		
		return initialZones;
	}


	private double mDieselZoneMAX=0;
	public void bufferResults(double time, Zone[] zn) {			
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

		if(mb>=super.CP.SYS.MINIMUM_ZONE_MASS && burntZonesAlreadyInitialised==false){
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
				zn[1].get_m()<=1.05*CP.SYS.MINIMUM_ZONE_MASS &&burntZonesAlreadyInitialised==true&&dieselZoneAusgeblendet==false){
			double a= CP.convert_SEC2KW(time);
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
		super.buffer_singleResult("Kurbelwinkel [∞KW]",super.CP.convert_SEC2KW(time),i);

		i+=1;
		super.buffer_singleResult("Zeit [s n. Rechenbeginn]",time,i);		

		i+=1;
		super.buffer_singleResult("Brennraumvolumen [m3]",motor.get_V(time),i);

		i+=1;
		super.buffer_singleResult("V0 [m3]",zn[0].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_singleResult("V1 [m3]",zn[1].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_singleResult("V2 [m3]",zn[2].get_V()/motor.get_V(time),i);

		i+=1;
		super.buffer_singleResult("T_Zone_0 [K]",zn[0].get_T(),i);

		i+=1;
		super.buffer_singleResult("T_Zone_1 [K]",zn[1].get_T(),i);

		i+=1;
		super.buffer_singleResult("T_Zone_2 [K]",zn[2].get_T(),i);

		i+=1;
		double Tm=wandWaermeModell.get_Tmb(zn);
		super.buffer_singleResult("T_mittel [K]",Tm,i);
		T_buffer.addValue(time, Tm);

		i+=1;
		double T_BurnAdiabat_0=5.55;
		if(zn[0].get_gasMixtureZone().get_Hu_mol()>0&&zn[0].get_T()>300&&zn[0].get_p()>1e5){
			T_BurnAdiabat_0=LHV_Calculator.calcAdiabateFlammenTemp(CP,
				zn[0].get_gasMixtureZone(), zn[0].get_p(), zn[0].get_T());	
		}

		super.buffer_singleResult("T_BurnAdiabat_0 [K]", T_BurnAdiabat_0,i);

		i+=1;
		double T_BurnAdiabat_1=0;
		if(zn[1].get_gasMixtureZone().get_Hu_mol()>0 ){		
			T_BurnAdiabat_1=LHV_Calculator.calcAdiabateFlammenTemp(CP,
					zn[1].get_gasMixtureZone(), zn[1].get_p(), zn[1].get_T());	
		}
		super.buffer_singleResult("T_BurnAdiabat_1 [K]", T_BurnAdiabat_1,i);		
		
		i+=1;
		super.buffer_singleResult("Lambda z1 [-]", zn[1].get_gasMixtureZone().get_lambda(),i);
		
		i+=1;
		super.buffer_singleResult("Lambda z0 [-]", zn[0].get_gasMixtureZone().get_lambda(),i);
		
		i+=1;
		super.buffer_singleResult("Lambda z2 [-]", zn[2].get_gasMixtureZone().get_lambda(),i);


		double pSoll=indiD.get_pZyl(time);
		i+=1;
		super.buffer_singleResult("p_soll [bar]", pSoll*1E-5,i);
		
		i+=1;
		super.buffer_singleResult("p_soll_roh [bar]", indiD.get_pZylRoh(time)*1E-5,i);

		i+=1;
		super.buffer_singleResult("p [bar]",zn[0].get_p()*1e-5,i);

		i+=1;
		super.buffer_singleResult("dQb [J/s]",dQburn,i);

		i+=1;
		super.buffer_singleResult("dQb [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQburn),i);

		i+=1;		
		super.buffer_singleResult("Qb [J]", Qb,i);

		i+=1;
		super.buffer_singleResult("dmb [kg/s]", dmZoneBurn,i);

		i+=1;
		super.buffer_singleResult("dmb [kg/KW]", super.CP.convert_ProSEC_2_ProKW(dmZoneBurn),i);

		i+=1;	
		super.buffer_singleResult("mb [kg]", zonenMasseVerbrannt,i);

		i+=1;
		super.buffer_singleResult("dQw [J/s]",dQw,i);

		i+=1;
		super.buffer_singleResult("dQw [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQw),i);	
		
		i+=1;
		double dQkrst=this.masterEinspritzung.get_dQ_krstDampf(time, zn);
		super.buffer_singleResult("dQKrst [J/s]",dQkrst ,i);	
		i+=1;
		super.buffer_singleResult("dQKrst [J/KW]", super.CP.convert_ProSEC_2_ProKW(dQkrst),i);	

		i+=1;		
		super.buffer_singleResult("Qw [J]",Qw,i);		

		i+=1;		
		super.buffer_singleResult("Xb[-]", fortschritt,i);


		i+=1;		
		super.buffer_singleResult("Qb/Qmax [-]", xQ,i);

		i+=1;
		super.buffer_singleResult("Qmax [J]", Qmax,i);		

		i+=1;
		double alpha=wandWaermeModell.get_WaermeUebergangsKoeffizient(time, zn, fortschritt);
		super.buffer_singleResult("Alpha [W/(m^2K)]", alpha, i);
		
//		i+=1;
//		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zn, fortschritt, T_buffer);
//		super.buffer_EinzelErgebnis("Wandw‰rmestromdichte [MW/m^2]",HeatFlux*1E-6,i);
//
//		i+=1;		
//		super.buffer_EinzelErgebnis("PQw [W]",HeatFlux*motor.get_BrennraumFlaeche(time),i);	

		i+=1;		
		super.buffer_singleResult("Brennraumfl‰che [m^2]",motor.get_BrennraumFlaeche(time),i);		

		i+=1;
		super.buffer_singleResult("Gesamtmasse [kg]", zn[0].get_m()+zn[1].get_m()+zn[2].get_m(),i);

		i+=1;		
		super.buffer_singleResult("m_0[kg]", zn[0].get_m(),i);

		i+=1;		
		super.buffer_singleResult("m_1[kg]", zn[1].get_m(),i);

		i+=1;		
		super.buffer_singleResult("m_2[kg]", zn[2].get_m(),i);


		i+=1;
		int iter=i;		
		double []mi=zn[0].get_mi();
		double m_ges=zn[0].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_singleResult("Z0_"+CP.SPECIES_FACTROY.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,i);			
		}

		i+=1;
		iter=i;	
		mi=zn[1].get_mi();
		m_ges=zn[1].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_singleResult("Z1_"+CP.SPECIES_FACTROY.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,iter+idx);
		}

		i+=1;
		iter=i;	
		mi=zn[2].get_mi();
		m_ges=zn[2].get_m();
		for(int idx=0;idx<mi.length;idx++){
			i=iter+idx;
			super.buffer_singleResult("Z2_"+CP.SPECIES_FACTROY.get_Spez(idx).get_name()
					+" [kg]" ,mi[idx]/m_ges,iter+idx);
		}		
		i+=1;		
		super.buffer_singleResult(" mDieseldampf [kg]",
				this.masterEinspritzung.get_injection(0).get_mKrst_verdampft(time),i);
		
		i+=1;
		Spezies diesel=CP.SPECIES_FACTROY.get_spezDiesel();
		if(zn[1].get_gasMixtureZone().get_speciesMassFracs().containsKey(diesel)){
			super.buffer_singleResult("m_Diesel_Z0 [kg]", zn[1].get_m()*zn[1].get_gasMixtureZone().get_speciesMassFracs().get(diesel),i);
		}else{
			super.buffer_singleResult("m_Diesel_Z0 [kg]", 0,i);
		}
		

		double ma=0,s=0,dma=0,mkrstFl=0,T_KRstVap=0;
		for(int x=0;x<masterEinspritzung.get_allInjections().length;x++){			
			Injection es=masterEinspritzung.get_allInjections()[x];	
			if(es.get_ID_Zone()==1){
				ma=((Spray)es).get_ma(time);					
				sumdma+=((Spray)es).get_dma(time)*CP.SYS.WRITE_INTERVAL_SEC;
				s=((Spray)es).get_packet(0, 0).get_s(time);
				dma=((Spray)es).get_dma(time);
				mkrstFl=es.get_m_fuelLiquid(time);
				T_KRstVap=es.get_T_fuelVapor(time,zn[es.get_ID_Zone()]);
			}
		}
		
		i+=1;
		super.buffer_singleResult("TKrstVap [K]", T_KRstVap,i);
		i+=1;
		super.buffer_singleResult("mkrstFl [kg]", mkrstFl,i);		
		i+=1;
		super.buffer_singleResult("m_a [kg]", ma,i);
		i+=1;
		super.buffer_singleResult("sumdma [kg]", sumdma,i);
		i+=1;
		super.buffer_singleResult("s00 [kg]", s,i);
		i+=1;
		super.buffer_singleResult("dm_a [kg]", dma,i);	
		
		i+=1;
		double pV=zn[0].get_p()*zn[0].get_V();
		double mRT=zn[0].get_m()*zn[0].get_gasMixtureZone().get_R()*zn[0].get_T();
		super.buffer_singleResult("pV-mRT zn0", (pV-mRT)/pV*100,i);
		
		i+=1;
		pV=zn[1].get_p()*zn[1].get_V();
		mRT=zn[1].get_m()*zn[1].get_gasMixtureZone().get_R()*zn[1].get_T();
		super.buffer_singleResult("pV-mRT zn1", (pV-mRT)/pV*100,i);	
		
		i+=1;
		pV=zn[2].get_p()*zn[2].get_V();
		mRT=zn[2].get_m()*zn[2].get_gasMixtureZone().get_R()*zn[2].get_T();
		super.buffer_singleResult("pV-mRT zn2", (pV-mRT)/pV*100,i);
		
	}
	
double sumdma=0;







	private void initBurnedZone(double time, Zone[] zonen_IN){

		initialZonesWhileRunning=new Zone[zonen_IN.length];
		double p=zonen_IN[0].get_p();
		double T1= zonen_IN[1].get_T();
		Spezies spez1=zonen_IN[1].get_gasMixtureZone();

		double Tb=LHV_Calculator.calcAdiabateFlammenTemp(CP,spez1, p,T1); 
		//		double =T_BurnAdiabat;	


		GasMixture rauchgas=new GasMixture("rg");
		rauchgas.set_Gasmixture_moleFracs(gg.get_GG_molenBrueche(p, Tb, spez1));
		double V2=this.mb*rauchgas.get_R()*Tb/p;
		//		//Berechnen der Einzelmassen in der verbrannten Zone
		Hashtable<Spezies,Double> ht=new Hashtable<Spezies,Double>();		
		ht=LittleHelpers.addiereHashs(ht, 
				rauchgas.get_speziesMassenBruecheDetailToIntegrate(), mb);
		initialZonesWhileRunning[2]=new Zone(CP,zonen_IN[2].get_p_V_T_mi(),zonen_IN[2].isBurnt(),zonen_IN[2].getID());
		initialZonesWhileRunning[2].set_p_V_T_mi(p, V2, Tb,ht);

		double m1=zonen_IN[1].get_m()+zonen_IN[2].get_m()-mb; 

		double V1=m1*spez1.get_R()*T1/p;//=motor.get_V(time)-V2-zonen_IN[0].get_V();
		//		T1=p*V1/(m1*spez1.get_R());			

		//		//Berechnen der Einzelmassen in der unverbrannten Zone
		Hashtable<Spezies,Double> ht2=new Hashtable<Spezies,Double>();		
		ht2=LittleHelpers.addiereHashs(ht2, 
				zonen_IN[1].get_gasMixtureZone().get_speziesMassenBruecheDetailToIntegrate(), m1);

		initialZonesWhileRunning[1]=new Zone(CP,zonen_IN[1].get_p_V_T_mi(),zonen_IN[1].isBurnt(),zonen_IN[1].getID());
		initialZonesWhileRunning[1].set_p_V_T_mi(p,V1, T1,ht2);	
		//OttoZone 
		double V0=motor.get_V(time)-V1-V2;
		double m0=zonen_IN[0].get_m();
		double R0=zonen_IN[0].get_gasMixtureZone().get_R();
		double T0=p*V0/m0/R0;
		//Berechnen der Einzelmassen in der OttoZone
		Hashtable<Spezies,Double> ht3=new Hashtable<Spezies,Double>();	
//		Hashtable<Spezies,Double> ht4=zonen_IN[0].get_ggZone().get_speziesMassenBruecheDetailToIntegrate();
		ht3=LittleHelpers.addiereHashs(ht3, 
				zonen_IN[0].get_gasMixtureZone().get_speziesMassenBruecheDetailToIntegrate(), m0);		
		initialZonesWhileRunning[0]=new Zone(CP,zonen_IN[0].get_p_V_T_mi(),zonen_IN[0].isBurnt(),zonen_IN[0].getID());		
		initialZonesWhileRunning[0].set_p_V_T_mi(p, V0, T0, ht3);

		burntZonesAlreadyInitialised=true;		
		//		initialZonesWhileRunning=zonen_IN;		
	}

	private void dieselZoneAusblenden( Zone zOtto ,Zone zDiesel,Zone zBurnt){
		if(zDiesel.get_V()>0&&zDiesel.get_m()>0){
			this.initialZonesWhileRunning[0]=Zone.zonenMischen(CP,zDiesel, zOtto, false, 0);			
		}
		else
			this.initialZonesWhileRunning[0]=zOtto;
		double vD=1e-55;
		this.initialZonesWhileRunning[1]=new Zone(CP,zDiesel.get_p(),vD , 1, 
				zDiesel.get_p()*vD/(zDiesel.get_gasMixtureZone().get_R()*1), 
				zDiesel.get_gasMixtureZone(), false, 1);
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
	protected void checkInjections(MasterInjection me) {
		for(int i=0;i<me.get_allInjections().length;i++){	
			Injection es=me.get_allInjections()[i];
			if(es.get_ID_Zone()==0){
				if(es.get_fuel().get_name().equalsIgnoreCase("Diesel")){
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
				if(es.get_fuel().get_name().equalsIgnoreCase("Diesel")==false){
					//ToDo checken dass die EInspritzung ein pakestModell ist!
					try {
						throw new ParameterFileWrongInputException("F¸r das gwaehlte Berechnungsmodell " +
								"darf ausschlieﬂlich Diesel in Zone 1 eingespritzwerden \n" +
								"Eingespritzt wurde aber " +es.get_fuel().get_name());
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
	public VectorBuffer get_dQw_buffer() {
		return this.dQw_buffer;
	}
	
	@Override
	public VectorBuffer get_dQb_buffer() {
		return this.dQb_buffer;
	}


	@Override
	public VectorBuffer get_dm_buffer() {
		return this.dmb_buffer;
	}

}
