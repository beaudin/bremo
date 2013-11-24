package berechnungsModule.fillAndEmptyMethod;

import java.util.Hashtable;

import io.DurchflusskennzahlFileReader;
import io.ValveLiftFileReader;
import kalorik.GasMixture;
import kalorik.Spezies;
import kalorik.SpeciesFactory;
import berechnungsModule.ResultsBuffer;
import berechnungsModule.Berechnung.SimulationModel;
import berechnungsModule.Berechnung.Zone;
import misc.VectorBuffer;
import berechnungsModule.mixtureFormation.MasterInjection;
import berechnungsModule.motor.Motor;
import berechnungsModule.wallHeatTransfer.WallHeatTransfer;
import bremo.parameter.CasePara;
import bremo.parameter.IndicatorData;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;

public class LadungsWechselAnalyse extends SimulationModel {
	
	private  WallHeatTransfer wandWaermeModell;
	private Motor m;
	private MasterInjection masterEinspritzung;
	protected final IndicatorData indiD;
	private double pInit,VInit;
	private double A_E;	//Referenzfläche für den Durchflusskennwert des Einlassventils
	private double A_A; //Referenzfläche für den Durchflusskennwert des Auslassventils
	private double alpha_E_vor; //Durchflusskennwert des Einlassventils, vorwärts
	private double alpha_E_rueck; //Durchflusskennwert des Einlassventils, rückwärts
	private double alpha_A_vor; //Durchflusskennwert des Auslassventils, vorwärts
	private double alpha_A_rueck; //Durchflusskennwert des Auslassventils, rückwärts
	private double hub_A, hub_E;
	private double dQw;
	private int anzZonen; 
//	Spezies spezZU;
	private GasMixture gAbgasbehaelter;
	private GasMixture gFrischluftbehaelter, feuchteLuft;


	private double pSaug, TSaug,TSaug_alt, pAbg, TAbg, kappa, pZyl, TZyl, Rgas,mLF_mess;
	private double mLF_DIFF,mLF_DIFF_alt;
	private boolean firstRun=true;
	
	private VectorBuffer T_buffer ;
	private DurchflusskennzahlFileReader DF_Datei_Ein;
	private DurchflusskennzahlFileReader DF_Datei_Aus;
	private ValveLiftFileReader VH_Datei_Ein;
	private ValveLiftFileReader VH_Datei_Aus;
	
	public LadungsWechselAnalyse(CasePara cp) {
		super(cp,new ResultsBuffer(cp,"LWA_"));	
		T_buffer = new VectorBuffer(CP);
		m = CP.MOTOR;
		indiD=new IndicatorData(cp);
		masterEinspritzung=CP.MASTER_INJECTION;
		anzZonen=1;
		gAbgasbehaelter =new GasMixture("AGR_intern_LWA");
		gAbgasbehaelter.set_Gasmixture_moleFracs(
				((GasMixture)CP.get_spezAbgas()).get_speziesMassenBruecheDetailToIntegrate());		
		CP.SPECIES_FACTROY.integrierMich(gAbgasbehaelter);
		
		//Kraftstoff aus Saugrohreinspritzungen wird unabhängig 
		//vom Zeitpunkt direkt zum Behaeltergemisch gerechnet
		Spezies krst=masterEinspritzung.get_spezKrst_verdampft(CP.get_EVO(), 0);		
		double mKrst=masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.get_EVO(),0);
		
		
		double mLuft_tr=CP.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=CP.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel			
		double mAGRex=CP.get_mAGR_extern_ASP();	//Masse externer AGR kg/ASP			

		//Bestimmung der Verbrennungsluftzusammensetzung	
		GasMixture agrEX=new GasMixture("AGR_extern_LWA");
		agrEX.set_Gasmixture_moleFracs(((GasMixture)CP.get_spezAbgas()).get_speziesMolenBrueche());	
		CP.SPECIES_FACTROY.integrierMich(agrEX);
		
		//feuchte Luft
		double mLF=mLuft_tr+mW; //gesamte Masse im Zylinder (ohne Kraftstoff)	
		mLF_mess=mLF;
		Hashtable<Spezies,Double>feuchteLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		feuchteLuft_MassenBruchHash.put(CP.SPECIES_FACTROY.get_spezLuft_trocken(),mLuft_tr/mLF);		
		feuchteLuft_MassenBruchHash.put(CP.SPECIES_FACTROY.get_H2O(),mW/mLF);
		feuchteLuft=new GasMixture("feuchteLuft_LWA");
		feuchteLuft.set_Gasmischung_massenBruch(feuchteLuft_MassenBruchHash);		
		CP.SPECIES_FACTROY.integrierMich(feuchteLuft);		
		
		//gesamte Masse 
		double mGes=mLF+mAGRex+mKrst; 
		//Frischgemsich
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
		frischGemisch_MassenbruchHash.put(feuchteLuft, mLF/mGes);
		frischGemisch_MassenbruchHash.put(agrEX, mAGRex/mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);
		GasMixture gemischINIT=new GasMixture("GemischINIT");	
		gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
		
		gFrischluftbehaelter=gemischINIT;	
		TSaug=super.CP.get_T_LadeLuft();
		TAbg=super.CP.get_T_Abgas();
		wandWaermeModell=CP.WALL_HEAT_TRANSFER_MODEL_FILL_EMPTY;
		A_E=super.CP.get_ReferenzflaecheEinlass();
		A_A=super.CP.get_ReferenzflaecheAuslass();
		
		DF_Datei_Ein=new DurchflusskennzahlFileReader(super.CP,"Einlass");
		DF_Datei_Aus=new DurchflusskennzahlFileReader(super.CP,"Auslass");
		VH_Datei_Ein=new ValveLiftFileReader(super.CP, "Einlass");
		VH_Datei_Aus=new ValveLiftFileReader(super.CP, "Auslass");		
	}

	private double dm;
	private double dmAusgabe;
	public Zone[] firstLawCombustionChamber(double time, Zone[] zonen) {
		double kw=CP.convert_SEC2KW(time);
		
//		zonen[0].set_p_V_T_mi(indiD.get_pZyl(time), zonen[0].get_V(), zonen[0].get_T(), zonen[0].get_mi_DetailToIntegrate());
		
		//Initialisierung
		dm=0;
		dmAusgabe=0;	
		pSaug=indiD.get_pEin(time);
		pAbg=indiD.get_pAus(time);
		TZyl=zonen[0].get_T();
		pZyl=zonen[0].get_p();
//		pZyl=indiD.get_pZyl(time); 
//		Die Verwendung des indizierten Zylinderdrucks führt meist dazu, dass aufgrund des thermoschocks des Drucksensors (?)
//		der Zylinderdruck im Vergleich zum Auslasssensor zu niedrig gemessen wird  und der Zylinder dadurch 
//		vollständig entleert wird. Es kommt dann zu: p=NaN 
		

		//Ventilhuebe
		hub_E=VH_Datei_Ein.get_Hub(time); 
		hub_A=VH_Datei_Aus.get_Hub(time);
		//Durchflusskennzahlen, vorwaerts und rueckwärts, Einlass und Auslass
		alpha_E_vor=DF_Datei_Ein.get_alphaVorwaerts(hub_E);
		alpha_E_rueck=DF_Datei_Ein.get_alphaRueckwaerts(hub_E);
		alpha_A_vor=DF_Datei_Aus.get_alphaVorwaerts(hub_A);
		alpha_A_rueck=DF_Datei_Aus.get_alphaRueckwaerts(hub_A);
		//Wandwaermestrom
		double fortschritt = 0; //Kraftstoff wird während des Ladungswechsels nicht umgesetzt
		dQw=wandWaermeModell.get_wallHeatFlux(time, zonen, fortschritt, T_buffer);
		//Waermestrom abfuehren
		zonen[0].set_dQ_in_out(-1*dQw);
		
		//Direkteinspritzung
		//Verdampfungswaerme abfuehren
		zonen=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen); //TODO Stimmt die Zeit  da die Rechnung bei auslassöffnet beginnt
//		Einspritzung des Kraftstoffs
		zonen=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen);
		
		//Einlassströmung
		if(hub_E>0){		
			if(pSaug<pZyl){ //Saugrohrdruck < Zylinderdruck, Masse strömt aus dem Zylinder heraus
				kappa=zonen[0].get_gasMixtureZone().get_kappa(TZyl);	//Gemisch kommt aus dem Zylinder
				Rgas=zonen[0].get_gasMixtureZone().get_R();
				dm=A_E*alpha_E_rueck*this.get_Massenstromdichte(pZyl,TZyl, pSaug, kappa, Rgas);
				try {
					zonen[0].set_dm_out(dm);
					//TODO: masse wird dem Frischluftbehälter hinzugefügt
				} catch (NegativeMassException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dmAusgabe+=-1*dm;			
	
			}else if(pSaug>pZyl){	//Saugrohrdruck > Zylinderdruck, Masse strömt durch den Einlasskanal hinein
				kappa=gFrischluftbehaelter.get_kappa(TSaug);	//Gemisch kommt aus dem Frischluftbehälter
				Rgas=gFrischluftbehaelter.get_R();
				dm=A_E*alpha_E_vor*this.get_Massenstromdichte(pSaug,TSaug, pZyl, kappa, Rgas);
				zonen[0].set_dm_in(dm, TSaug, gFrischluftbehaelter);
				dmAusgabe+=dm;
			}else{
			}
		}
		//Auslassströmung
		if(hub_A>0){
			if(pAbg>pZyl){ //Abgasdruck > Zyldruck, Masse strömt durch den Auslasskanal hinein
				kappa=gAbgasbehaelter.get_kappa(TAbg);	//Gemisch kommt aus dem Abgasbehälter
				Rgas=gAbgasbehaelter.get_R();
				dm=A_A*alpha_A_rueck*this.get_Massenstromdichte(pAbg,TAbg, pZyl, kappa, Rgas);
				zonen[0].set_dm_in(dm, TAbg, gAbgasbehaelter);
				dmAusgabe+=dm;
			}else if(pAbg<pZyl){ //Abgasdruck < Zyldruck, Masse strömt aus dem Auslasskanal heraus
				kappa=zonen[0].get_gasMixtureZone().get_kappa(TZyl);	//Gemisch kommt aus dem Zylinder
				Rgas=zonen[0].get_gasMixtureZone().get_R();
				dm=A_A*alpha_A_vor*this.get_Massenstromdichte(pZyl,TZyl, pAbg, kappa, Rgas);
				try {
					zonen[0].set_dm_out(dm);
					//TODO: Masse wird dem Abgasbehälter hinzugefügt
				} catch (NegativeMassException e) {
					e.log_Warning();
				}
				dmAusgabe+=-1*dm;
			}else{
			}
		}
		if(((Double)zonen[0].get_p()).isNaN()){
//			System.out.println("Warnung: in der Ladungswechselanalyse kommen unmögliche Drücke vor");
		}
		return zonen;
	}
	
	/**
	 * Massenstromdichte durch einen Kanal [kg/(m²s)]. Diese muss mit der Referenzfläche und dem
	 * Alpha-Wert des Kanals multipliziert werden, um auf den richtigen Massenstrom zu kommen
	 */
	private double get_Massenstromdichte(double pHi,double THi,double pLo,double kappa, double Rgas){
		if(pLo/pHi<=Math.pow(2/(kappa+1),kappa/(kappa-1))){
			//Kritische Stroemung (siehe Heywood S. 226)
			double v=Math.sqrt(kappa)*Math.pow(2/(kappa+1), (kappa+1)/((kappa-1)*2));
			double w=pHi/(Math.sqrt(Rgas*THi));
			return v*w;
		}else{		
			double w=pHi/(Math.sqrt(Rgas*THi));
			double x=Math.sqrt(2*kappa/(kappa-1));
			double y=Math.pow(pLo/pHi,1/kappa);
			double z=Math.sqrt(1-Math.pow(pLo/pHi, ((kappa-1)/kappa)));
			return w*x*y*z;
		}
	}
	
	public double get_mAGRintern(Zone [] zonen){
		double agrIntern=0;
		if(zonen[0].get_gasMixtureZone().get_speciesMassFracs().containsKey(gAbgasbehaelter))
			agrIntern=zonen[0].get_gasMixtureZone().get_speciesMassFracs().get(gAbgasbehaelter)*zonen[0].get_m();	
		
//		System.out.println("AGRin"+agrIntern/zonen[0].get_m());
		return agrIntern;
	}
	
	
	public double get_mLuftFeucht(Zone [] zonen){
		double mLF=0;
		if(zonen[0].get_gasMixtureZone().get_speciesMassFracs().containsKey(feuchteLuft))
			mLF=zonen[0].get_gasMixtureZone().get_speciesMassFracs().get(feuchteLuft)*zonen[0].get_m();	
//		System.out.println("mLF"+mLF/zonen[0].get_m());
		return mLF;
	}

	public void bufferResults(double time, Zone [] zonen) {
		this.masterEinspritzung.berechneIntegraleGroessen(time, zonen);
		int i=0;
		super.buffer_singleResult("Kurbelwinkel [°KW]",super.CP.convert_SEC2KW(time),i);
		i++;
		super.buffer_singleResult("pZyl_mess [bar]",indiD.get_pZyl(time)*1e-5,i);
		i++;
		super.buffer_singleResult("pZyl_berech [bar]",zonen[0].get_p()*1E-5,i);
		i++;
		super.buffer_singleResult("pSaug [bar]",pSaug*1E-5,i);
		i++;
		super.buffer_singleResult("pAbg [bar]",pAbg*1E-5,i);		
		i++;
		super.buffer_singleResult("T [K]",zonen[0].get_T(),i);
		i++;
		super.buffer_singleResult("dQw [J/s]",dQw,i);
		T_buffer.addValue(time, zonen[0].get_T());
		i++;
		super.buffer_singleResult("V [m3]",zonen[0].get_V(),i);
		i++;
		double mZ=zonen[0].get_m();
		super.buffer_singleResult("m [kg]",mZ,i);
		i++;
		super.buffer_singleResult("R [J/kg/K]", zonen[0].get_gasMixtureZone().get_R(),i);
		i++;
		double mK=zonen[0].get_p()*zonen[0].get_V()/(zonen[0].get_gasMixtureZone().get_R()*zonen[0].get_T());			
		super.buffer_singleResult("mK [kg]", mK,i);		
		i++;
		super.buffer_singleResult("V_motor [m^3]",m.get_V(time),i);
		i++;
		super.buffer_singleResult("dm [kg/s]", dmAusgabe,i);		
		i++;
		super.buffer_singleResult("Hub_E [m]", hub_E,i);
		i++;
		super.buffer_singleResult("Hub_A [m]", hub_A,i);
		i++;
		super.buffer_singleResult("alpha_E_vor",alpha_E_vor,i);
		i++;
		super.buffer_singleResult("alpha_E_rück",alpha_E_rueck,i);
		i++;
		super.buffer_singleResult("alpha_A_vor",alpha_A_vor,i);
		i++;
		super.buffer_singleResult("alpha_A_rück",alpha_A_rueck,i);
		i++;
		super.buffer_singleResult("T_Saug [K]",this.TSaug,i);
		i++;
		double []mi=zonen[0].get_mi();
		for(int idx=0;idx<mi.length;idx++)
			super.buffer_singleResult(CP.SPECIES_FACTROY.get_Spez(idx).get_name()
					+"_Massenbruch",mi[idx]/mZ,i+idx);

	}
	
	
	public void set_TSaug(double mLF){	
		double relax=0.8;
		mLF_DIFF=mLF_mess-mLF;

		//Startbedingungen beim ersten Durchlauf
		if(firstRun){
			firstRun=false;
			mLF_DIFF_alt=mLF_DIFF;
			TSaug_alt=TSaug;
			TSaug=TSaug*(1+mLF_DIFF/mLF_mess);

		}else{
			double dmLF_DIFF=(mLF_DIFF-mLF_DIFF_alt)/(TSaug-TSaug_alt);
			mLF_DIFF_alt=mLF_DIFF;
			TSaug_alt=TSaug;

			TSaug=TSaug-relax*mLF_DIFF/dmLF_DIFF;
		}
		//		System.out.println("TSaug:" +TSaug);
	}

	public Zone[] get_initialZones() {
		Zone []zn=new Zone[1];
		Spezies sInit=gAbgasbehaelter;	//Rechnung fängt an mit Abgas im Zylinder
		
		pInit=indiD.get_pZyl(CP.get_EVO());		
		//V Init
		VInit=m.get_V(CP.get_EVO());
		
		double mLuft_tr=CP.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=CP.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		double mAGRex=CP.get_mAGR_extern_ASP();	//externe AGR			
		double mVerbrennungsLuft=mLuft_tr+mW+mAGRex;
		double mKrst=masterEinspritzung.get_mFuel_Sum_Cycle(); 
		double mInit= mVerbrennungsLuft+mKrst;	//Gesamtmasse
				
		double TInit =pInit*VInit/(mInit*sInit.get_R());
		
		zn[0]=new Zone(CP,pInit, VInit, TInit, mInit, sInit, false, 0);
		return zn;
	}
	

	public int get_nbrOfZones() {		
		return anzZonen;
	}


	public boolean isDVA() {
		return false;
	}

	@Override
	public Zone[] get_initialZonesWhileRunning() {
		return null;
	}

	@Override
	public boolean initialiseBurntZone() {		
		return false;
	}


	public int get_InjZone(int indexInj) {		
		return 0;
	}

	@Override
	protected void checkInjections(MasterInjection me) {
		for(int i=0;i<me.get_allInjections().length;i++){			
			if(me.get_allInjections()[i].get_ID_Zone()!=0){
				try {
					throw new ParameterFileWrongInputException("Fuer die Ladungswechselanalyse " +
							"koennen die Einspritzungen" +
							"nur in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone "+ 
							me.get_allInjections()[i].get_ID_Zone());
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}	
		}		
	}

	@Override
	public VectorBuffer get_dQw_buffer() {
		return null;
	}

	@Override
	public VectorBuffer get_dm_buffer() {
		return null;
	}

	@Override
	public VectorBuffer get_dQb_buffer() {
		return null;
	}
	
//	/**
//	 * Liefert die Verbrennungsluft analog zu der Funktion aus CasePara ausser, 
//	 * dass hier die interne AGR nicht beruecksichtigt wird
//	 * @return
//	 */
//	private Spezies get_spezVerbrennungsLuft_LWA(){
//		double mLuft_tr=CP.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
//		double mW=CP.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel			
//		double mAGRex=CP.get_mAGR_extern_ASP();		
//		double mGes=mLuft_tr+mW+mAGRex; //gesamte Masse im Zylinder (ohne Kraftstoff)
//		//Bestimmung der Verbrennungsluftzusammensetzung	
//		Spezies abgas=CP.get_spezAbgas();	
//		Hashtable<Spezies,Double>verbrennungsLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
//		verbrennungsLuft_MassenBruchHash.put(abgas, mAGRex/mGes);
//		verbrennungsLuft_MassenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezLuft_trocken(),mLuft_tr/mGes);		
//		verbrennungsLuft_MassenBruchHash.put(KoeffizientenSpeziesFabrik.get_spezH2O(),mW/mGes);
//		GasGemisch verbrennungsLuft=new GasGemisch("Verbrennungsluft_LWA");
//		verbrennungsLuft.set_Gasmischung_massenBruch(verbrennungsLuft_MassenBruchHash);	
//		return verbrennungsLuft;
//	}
//	
//	private double get_mVerbrennungsLuft_ASP(){
//		double mLuft_tr=CP.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
//		double mW=CP.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel			
//		double mAGRex=CP.get_mAGR_extern_ASP();		
//		double mGes=mLuft_tr+mW+mAGRex; //gesamte Masse im Zylinder (ohne Kraftstoff)
//		return mGes;
//	}	

}


