package berechnungsModule.LadungswechselAnalyse;

import java.util.Hashtable;

import io.DurchflusskennzahlFileReader;
import io.VentilhubFileReader;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.SpeziesFabrik;
import kalorik.spezies.Spezies;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.Zone;
import misc.VektorBuffer;
import berechnungsModule.blowby.BlowBy;
import berechnungsModule.blowby.BlowByFabrik;
import berechnungsModule.gemischbildung.Frommelt;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;

/**
 * Ladungswechselanalyse, bei der ein Kraftstoffumsatz in der Zwischenkompression berücksichtigt werden kann.
 * @author neurohr
 *
 */
public class LadungsWechselAnalyse extends MasterLWA {
	
	private WandWaermeUebergang wandWaermeModell;
	private BlowBy blowbyModell;
	private Motor m;
	private MasterEinspritzung masterEinspritzung;
	private IndizierDaten indiD;
	private double pInit,VInit;
	private double A_E;	//Referenzfläche für den Durchflusskennwert des Einlassventils
	private double A_A; //Referenzfläche für den Durchflusskennwert des Auslassventils
	private double alpha_E_vor; //Durchflusskennwert des Einlassventils, vorwärts
	private double alpha_E_rueck; //Durchflusskennwert des Einlassventils, rückwärts
	private double alpha_A_vor; //Durchflusskennwert des Auslassventils, vorwärts
	private double alpha_A_rueck; //Durchflusskennwert des Auslassventils, rückwärts
	private double hub_A, hub_E;
	private double dQw;
	private double Qburn;
	private double Qmax;
	private boolean krstVerbrannt;
	boolean firstEB=false;
	private double dmZoneBurn;
	private double mInit,fortschritt,zonenMasseVerbrannt=0;
	private int anzZonen; 
//	Spezies spezZU;
	private GasGemisch gAbgasbehaelter;
	private GasGemisch gFrischluftbehaelter, feuchteLuft;
	private double dmEVTemp;
	private double dmL, mL=0;


	private double pSaug, TSaug,TSaug_alt, pAbg, TAbg, kappa, pZyl, TZyl, Rgas,mLF_mess;
	private double mLF_DIFF,mLF_DIFF_alt;
	private boolean firstRun=true;
	
	private VektorBuffer T_buffer ;
	private DurchflusskennzahlFileReader DF_Datei_Ein;
	private DurchflusskennzahlFileReader DF_Datei_Aus;
	private VentilhubFileReader VH_Datei_Ein;
	private VentilhubFileReader VH_Datei_Aus;
	
	//Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen 
	private misc.VektorBuffer p_buffer ;							//fuer Verlustteilung Frank Haertel 
	public LadungsWechselAnalyse(CasePara cp) {
		super(cp,new ErgebnisBuffer(cp,"lwa")); //früher "LWA_"
		
		indiD=super.indiD; 											//fuer Verlustteilung Frank Haertel
		Qburn = 0;
	    createMe(cp); 												//fuer Verlustteilung Frank Haertel 
	  } 															//fuer Verlustteilung Frank Haertel 
	public LadungsWechselAnalyse(CasePara cp, boolean gemittelt){	//fuer Verlustteilung Frank Haertel  
	    super(cp,new ErgebnisBuffer(cp,"lwa")); //früher "LWA_"		//fuer Verlustteilung Frank Haertel  
	    if (gemittelt == true) 										//fuer Verlustteilung Frank Haertel 
	      indiD=new IndizierDaten(cp,true); 						//fuer Verlustteilung Frank Haertel 
	    else 														//fuer Verlustteilung Frank Haertel 
	      indiD=new IndizierDaten(cp); 								//fuer Verlustteilung Frank Haertel 
	    createMe(cp); 												//fuer Verlustteilung Frank Haertel 
	  } 															//fuer Verlustteilung Frank Haertel 
	public void createMe(CasePara cp) { 							//fuer Verlustteilung Frank Haertel 	
		
		T_buffer = new VektorBuffer(CP);
		p_buffer = new misc.VektorBuffer(cp); 						//fuer Verlustteilung Frank Haertel 
		m = CP.MOTOR;
		//indiD=new IndizierDaten(cp);								//fuer Verlustteilung Frank Haertel 
		masterEinspritzung=CP.MASTER_EINSPRITZUNG;
		anzZonen=1;
		gAbgasbehaelter =new GasGemisch("AGR_intern_LWA");
		gAbgasbehaelter.set_Gasmischung_molenBruch(
				((GasGemisch)CP.get_spezAbgas()).get_speziesMassenBruecheDetailToIntegrate());		
		CP.SPEZIES_FABRIK.integrierMich(gAbgasbehaelter);
		
		//Kraftstoff aus Saugrohreinspritzungen wird unabhängig 
		//vom Zeitpunkt direkt zum Behaeltergemisch gerechnet
		Spezies krst=masterEinspritzung.get_spezKrst_verdampft(CP.get_Auslassoeffnet(), 0);		
		double mKrst=masterEinspritzung.get_mKrst_dampffoermig_Sum_Zone(CP.get_Auslassoeffnet(),0);
		
		double mLuft_tr=CP.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=CP.get_mWasser_Luft_ASP();		//Wassermasse pro Arbeitsspiel			
		double mAGRex=CP.get_mAGR_extern_ASP();		//Masse externer AGR kg/ASP			

		//Bestimmung der Verbrennungsluftzusammensetzung	
		GasGemisch agrEX=new GasGemisch("AGR_extern_LWA");
		agrEX.set_Gasmischung_molenBruch(((GasGemisch)CP.get_spezAbgas()).get_speziesMolenBrueche());	
		CP.SPEZIES_FABRIK.integrierMich(agrEX);
		
		//feuchte Luft
		double mLF=mLuft_tr+mW; //gesamte Masse im Zylinder (ohne Kraftstoff)	
		mLF_mess=mLF;
		Hashtable<Spezies,Double>feuchteLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		feuchteLuft_MassenBruchHash.put(CP.SPEZIES_FABRIK.get_spezLuft_trocken(),mLuft_tr/mLF);		
		feuchteLuft_MassenBruchHash.put(CP.SPEZIES_FABRIK.get_spezH2O(),mW/mLF);
		feuchteLuft=new GasGemisch("feuchteLuft_LWA");
		feuchteLuft.set_Gasmischung_massenBruch(feuchteLuft_MassenBruchHash);		
		CP.SPEZIES_FABRIK.integrierMich(feuchteLuft);		
		
		//gesamte Masse 
		double mGes=mLF+mAGRex+mKrst; 
		//Frischgemsich
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
		frischGemisch_MassenbruchHash.put(feuchteLuft, mLF/mGes);
		frischGemisch_MassenbruchHash.put(agrEX, mAGRex/mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);
		GasGemisch gemischINIT=new GasGemisch("GemischINIT");	
		gemischINIT.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);	
		
		gFrischluftbehaelter=gemischINIT;	
		TSaug=super.CP.get_T_LadeLuft();
		TAbg=super.CP.get_T_Abgas();
		wandWaermeModell=CP.WAND_WAERME_LW;
		A_E=super.CP.get_ReferenzflaecheEinlass();
		A_A=super.CP.get_ReferenzflaecheAuslass();
		
		DF_Datei_Ein=new DurchflusskennzahlFileReader(super.CP,"Einlass");
		DF_Datei_Aus=new DurchflusskennzahlFileReader(super.CP,"Auslass");
		VH_Datei_Ein=new VentilhubFileReader(super.CP, "Einlass");
		VH_Datei_Aus=new VentilhubFileReader(super.CP, "Auslass");
		
		dmEVTemp = 0;
		
		//die maximal moegliche freigesetzte Waermemenge, wenn das Abgas wieder auf 25°C abgekuehlt wird 
		Qmax=masterEinspritzung.get_mKrst_Sum_ASP()*masterEinspritzung.get_spezKrstALL().get_Hu_mass();
		krstVerbrannt = false;
		
		blowbyModell = super.CP.BLOW_BY_MODELL;
	}

	private double dm;
	private double dmAusgabe;
	private double dmAuslass, dmEinlass;
	public Zone[] ersterHSBrennraum(double time, Zone[] zonen) {
//		double kw=CP.convert_SEC2KW(time);
		
//		zonen[0].set_p_V_T_mi(indiD.get_pZyl(time), zonen[0].get_V(), zonen[0].get_T(), zonen[0].get_mi_DetailToIntegrate());
		
		//Initialisierung
		dm=0;
		dmAusgabe=0;
		dmEinlass = 0;
		dmAuslass = 0;
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
		dQw=wandWaermeModell.get_WandWaermeStrom(time, zonen, fortschritt, T_buffer);
		//Waermestrom abfuehren
		zonen[0].set_dQ_ein_aus(-1*dQw);
		
		//Verbrennungswärme zuführen
		double dQburn = super.get_dQburn();
		Spezies ggZone = zonen[0].get_ggZone();
		double TZone = zonen[0].get_T();
		double pZone = zonen[0].get_p();
		zonen[0].set_dQ_ein_aus(dQburn);
		dmZoneBurn = 0;
		double umsatzStartVorgabe = CP.get_verbrennungsBeginnLWASEC();
		
		//Einspritzung läuft?
		if(masterEinspritzung.get_dm_krstDampf(time, zonen)>0){
			firstEB = true;
		}
		
		if(dQburn>0 && !krstVerbrannt && firstEB && time>=umsatzStartVorgabe){
			dmZoneBurn = super.convert_dQburn_2_dmKrstBurn(dQburn,ggZone,TZone,pZone);
		}
		//Verbrennendes Masseelement entnehmen
		if(dmZoneBurn>0){
			try {
				zonen[0].set_dm_aus(dmZoneBurn,ggZone);
				//Massenelement wieder zumischen
				zonen[0].set_dm_ein(dmZoneBurn, TZone, gAbgasbehaelter);
			} catch (NegativeMassException nmE) {					
				nmE.log_Warning();
				krstVerbrannt=true;
			}	
		}
		
		//Leckagestrom abführen
		dmL = blowbyModell.get_mLeckage(time, zonen)*CP.SYS.WRITE_INTERVAL_SEC;
		if(dmL>=0){
			try{
				zonen[0].set_dm_aus(dmL);
			}catch(NegativeMassException nme){
				nme.log_Warning("BlowBy führte zu einer Entleerung der Zone ! \n" +
						"BlowBy-Eingaben überprüfen.");
				nme.stopBremo();
			}
		}else{
			zonen[0].set_dm_ein(-dmL, zonen[0].get_T(), zonen[0].get_ggZone());
		}
		
		//Direkteinspritzung
		//Verdampfungswaerme abfuehren
		zonen=masterEinspritzung.entnehme_dQ_krstDampf(time, zonen);
//		Einspritzung des Kraftstoffs
		zonen=masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen);
		
		//Einlassströmung
		if(hub_E>0){		
			if(pSaug<pZyl){ //Saugrohrdruck < Zylinderdruck, Masse strömt aus dem Zylinder heraus
				kappa=zonen[0].get_ggZone().get_kappa(TZyl);	//Gemisch kommt aus dem Zylinder
				Rgas=zonen[0].get_ggZone().get_R();
				dm=A_E*alpha_E_rueck*this.get_Massenstromdichte(pZyl,TZyl, pSaug, kappa, Rgas);
				dmEVTemp += dm; //Ausgeschobene Masse speichern, um sie später zurückzuführen -- mn03.14
				try {
					zonen[0].set_dm_aus(dm);
				} catch (NegativeMassException e) {
					e.printStackTrace();
				}
				dmAusgabe+=-1*dm;
				dmEinlass+=-1*dm;
	
			}else if(pSaug>pZyl){	//Saugrohrdruck > Zylinderdruck, Masse strömt durch den Einlasskanal hinein
				if(dmEVTemp > 0) { //Ausgeschobene Masse berücksichtigen -- mn03.14
					GasGemisch gAusschiebebehaelter = zonen[0].get_ggZone();
					kappa=gAusschiebebehaelter.get_kappa(zonen[0].get_T()); //Annahme, dass adiabat ausgeschoben und angesaugt wird.
					Rgas=gAusschiebebehaelter.get_R();
					dm=A_E*alpha_E_vor*this.get_Massenstromdichte(pSaug, zonen[0].get_T(), pZyl, kappa, Rgas);
					zonen[0].set_dm_ein(dm, zonen[0].get_T(), gAusschiebebehaelter);
				}else{ 	//Wenn nichts ausgeschoben wurde oder ausgeschobene Masse aufgebraucht ist, dann
						// erfolgt die Entnahme aus dem Frischgemischbehälter				
					kappa=gFrischluftbehaelter.get_kappa(TSaug);	//Gemisch kommt aus dem Frischluftbehälter
					Rgas=gFrischluftbehaelter.get_R();
					dm=A_E*alpha_E_vor*this.get_Massenstromdichte(pSaug,TSaug, pZyl, kappa, Rgas);
					zonen[0].set_dm_ein(dm, TSaug, gFrischluftbehaelter);
				}
				dmEVTemp-=dm;
				if(dmEVTemp<0) dmEVTemp=0; //Zum Auffangen von Fehlern
				dmAusgabe+=dm;
				dmEinlass+=dm;
			}else{
			}
		}
		//Auslassströmung
		if(hub_A>0){
			if(pAbg>pZyl){ //Abgasdruck > Zyldruck, Masse strömt durch den Auslasskanal hinein
				kappa=gAbgasbehaelter.get_kappa(TAbg);	//Gemisch kommt aus dem Abgasbehälter
				Rgas=gAbgasbehaelter.get_R();
				dm=A_A*alpha_A_rueck*this.get_Massenstromdichte(pAbg,TAbg, pZyl, kappa, Rgas);
				zonen[0].set_dm_ein(dm, TAbg, gAbgasbehaelter);
				dmAusgabe+=dm;
				dmAuslass+=dm;
			}else if(pAbg<pZyl){ //Abgasdruck < Zyldruck, Masse strömt aus dem Auslasskanal heraus
				kappa=zonen[0].get_ggZone().get_kappa(TZyl);	//Gemisch kommt aus dem Zylinder
				Rgas=zonen[0].get_ggZone().get_R();
				dm=A_A*alpha_A_vor*this.get_Massenstromdichte(pZyl,TZyl, pAbg, kappa, Rgas);
				try {
					zonen[0].set_dm_aus(dm);
					//TODO: Masse wird dem Abgasbehälter hinzugefügt
				} catch (NegativeMassException e) {
					e.log_Warning();
				}
				dmAusgabe+=-1*dm;
				dmAuslass+=-1*dm;
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
		if(zonen[0].get_ggZone().get_speziesMassenBrueche().containsKey(gAbgasbehaelter))
			agrIntern=zonen[0].get_ggZone().get_speziesMassenBrueche().get(gAbgasbehaelter)*zonen[0].get_m();	
		
//		System.out.println("AGRin"+agrIntern/zonen[0].get_m());
		return agrIntern;
	}
	
	
	public double get_mLuftFeucht(Zone [] zonen){
		double mLF=0;
		if(zonen[0].get_ggZone().get_speziesMassenBrueche().containsKey(feuchteLuft))
			mLF=zonen[0].get_ggZone().get_speziesMassenBrueche().get(feuchteLuft)*zonen[0].get_m();	
//		System.out.println("mLF"+mLF/zonen[0].get_m());
		return mLF;
	}

	private double time_alt=0;
	public void bufferErgebnisse(double time, Zone [] zonen) {
		double p = zonen[0].get_p(); 	//fuer Verlustteilung Frank Haertel
	    p_buffer.addValue(time, p); 	//fuer Verlustteilung Frank Haertel
		
	    //Berechnen integraler Werte
	    if(time>time_alt){
		    Qburn+=super.get_dQburn()*super.CP.SYS.WRITE_INTERVAL_SEC;
		  	zonenMasseVerbrannt=zonenMasseVerbrannt+dmZoneBurn*super.CP.SYS.WRITE_INTERVAL_SEC;
		  	fortschritt=zonenMasseVerbrannt/mInit;
		  	time_alt = time;
	    }
	    this.masterEinspritzung.berechneIntegraleGroessen(time, zonen);
	    
		int i=0;
		super.buffer_EinzelErgebnis("Kurbelwinkel [°KW]",super.CP.convert_SEC2KW(time),i);
		i++;
		super.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]",time,i);
		i++;
		super.buffer_EinzelErgebnis("pZyl_mess [bar]",indiD.get_pZyl(time)*1e-5,i);
		i++;
		super.buffer_EinzelErgebnis("pZyl_berech [bar]",zonen[0].get_p()*1E-5,i);
		i++;
		super.buffer_EinzelErgebnis("pSaug [bar]",pSaug*1E-5,i);
		i++;
		super.buffer_EinzelErgebnis("pAbg [bar]",pAbg*1E-5,i);		
		i++;
		super.buffer_EinzelErgebnis("T [K]",zonen[0].get_T(),i);
		i++;
		super.buffer_EinzelErgebnis("dQw [J/s]",dQw,i);
		T_buffer.addValue(time, zonen[0].get_T());
		
		///
		i+=1;
		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zonen, fortschritt);
		super.buffer_EinzelErgebnis("WSD [MW/m^2]",HeatFlux*1E-6,i);
		
		i+=1;
		double HeatFluxPiston = wandWaermeModell.get_WandWaermeStromDichtePiston(time, zonen, fortschritt);
		super.buffer_EinzelErgebnis("WSD Kolben [MW/m^2]",HeatFluxPiston*1E-6,i);
		
		i+=1;
		double HeatFluxHead = wandWaermeModell.get_WandWaermeStromDichteHead(time, zonen, fortschritt);
		super.buffer_EinzelErgebnis("WSD Head [MW/m^2]",HeatFluxHead*1E-6,i);
		
		i+=1;
		double HeatFluxCyl = wandWaermeModell.get_WandWaermeStromDichteCyl(time, zonen, fortschritt);
		super.buffer_EinzelErgebnis("WSD Liner [MW/m^2]",HeatFluxCyl*1E-6,i);
		
		i+=1;		
		super.buffer_EinzelErgebnis("Brennraumfläche [m^2]",m.get_BrennraumFlaeche(time),i);	
		///
		
		i++;
		super.buffer_EinzelErgebnis("dQb [J/s]", super.get_dQburn(), i);
		i++;
		super.buffer_EinzelErgebnis("Qb [J]", Qburn, i);
		i++;
		super.buffer_EinzelErgebnis("Qb/Qmax [-]", Qburn/Qmax, i);
		i++;
		super.buffer_EinzelErgebnis("dmb [kg/s]", dmZoneBurn, i);
		i++;
		super.buffer_EinzelErgebnis("mb [kg]", zonenMasseVerbrannt, i);
		i++;
		super.buffer_EinzelErgebnis("Xb [-]", fortschritt, i);
		i++;
		super.buffer_EinzelErgebnis("V [m3]",zonen[0].get_V(),i);
		i++;
		double mZ=zonen[0].get_m();
		super.buffer_EinzelErgebnis("m [kg]",mZ,i);
		i++;
		super.buffer_EinzelErgebnis("R [J/kg/K]", zonen[0].get_ggZone().get_R(),i);
		i++;
		double mK=zonen[0].get_p()*zonen[0].get_V()/(zonen[0].get_ggZone().get_R()*zonen[0].get_T());			
		super.buffer_EinzelErgebnis("mK [kg]", mK,i);		
		i++;
		super.buffer_EinzelErgebnis("V_motor [m^3]",m.get_V(time),i);
		i++;
		super.buffer_EinzelErgebnis("dm [kg/s]", dmAusgabe,i);		
		i++;
		super.buffer_EinzelErgebnis("dmEV [kg/s]", dmEinlass,i);		
		i++;
		super.buffer_EinzelErgebnis("dmAV [kg/s]", dmAuslass,i);		
		i++;
		super.buffer_EinzelErgebnis("dmEV aus Zylinder [kg/s]", dmEVTemp,i);		
		i++;
		super.buffer_EinzelErgebnis("Hub_E [m]", hub_E,i);
		i++;
		super.buffer_EinzelErgebnis("Hub_A [m]", hub_A,i);
		i++;
		super.buffer_EinzelErgebnis("alpha_E_vor [-]",alpha_E_vor,i);
		i++;
		super.buffer_EinzelErgebnis("alpha_E_rück [-]",alpha_E_rueck,i);
		i++;
		super.buffer_EinzelErgebnis("alpha_A_vor [-]",alpha_A_vor,i);
		i++;
		super.buffer_EinzelErgebnis("alpha_A_rück [-]",alpha_A_rueck,i);
		i++;
		super.buffer_EinzelErgebnis("dmL [kg/s]", dmL, i);
		i++;
		super.buffer_EinzelErgebnis("dmL [kg/KW]", super.CP.convert_ProSEC_2_ProKW(dmL), i);
		i++;
		mL=mL+dmL*super.CP.SYS.WRITE_INTERVAL_SEC;
		super.buffer_EinzelErgebnis("mL [kg]", mL, i);
		i++;
		super.buffer_EinzelErgebnis("T_Saug [K]",this.TSaug,i);
		
		// buffer mass of fuel and characteristic evaporation time for each injection
		int einspritzungen = CP.MASTER_EINSPRITZUNG.get_AllInjections().length;
		for(int index=0; index<einspritzungen; index++){
			if(CP.MASTER_EINSPRITZUNG.get_ModulWahl(CP.MASTER_EINSPRITZUNG.EINSPRITZ_MODELL_FLAG+index, CP.MASTER_EINSPRITZUNG.MOEGLICHE_EINSPRITZ_MODELLE).equals(Frommelt.FLAG)){ //Nur wenn Frommelt
				i+=1;
				super.buffer_EinzelErgebnis("Kraftstoffmasse_" + index + " [kg]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Mass(time), i);
				i+=1;
				super.buffer_EinzelErgebnis("Kraftstoffrate_" + index + " [kg/s]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Rate(time), i);
				i+=1;
				super.buffer_EinzelErgebnis("Tau_" + index + " [s]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Tau(time), i);
			}
			i+=1;
			super.buffer_EinzelErgebnis("Kraftstoffdampf_" + index + " [kg]", this.masterEinspritzung.get_Einspritzung(index).get_mKrst_verdampft(time), i);	
		}
		
		i++;
		double []mi=zonen[0].get_mi();
		for(int idx=0;idx<mi.length;idx++)
			super.buffer_EinzelErgebnis(CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
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
//				System.out.println("TSaug:" +TSaug);
				System.err.println("TSaug:" +TSaug);
	}

	public Zone[] get_initialZones() {
		Zone []zn=new Zone[1];
		Spezies sInit=gAbgasbehaelter;	//Rechnung fängt an mit Abgas im Zylinder
		
		pInit=indiD.get_pZyl(CP.get_Auslassoeffnet());		
		//V Init
		VInit=m.get_V(CP.get_Auslassoeffnet());
		
		double mLuft_tr=CP.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW=CP.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel		
		double mAGRex=CP.get_mAGR_extern_ASP();	//externe AGR			
		double mVerbrennungsLuft=mLuft_tr+mW+mAGRex;
		double mKrst=masterEinspritzung.get_mKrst_Sum_ASP(); 
		double mInit= mVerbrennungsLuft+mKrst;	//Gesamtmasse
		
		//Startwert für interne AGR-Masse auslesen, falls angegeben. Dadurch können falsche Zonenwerte
		//während der ersten Schleifen umgangen werden.
		try{
			double mAGRin = CP.get_mAGR_intern_ASP_aus_InputFile();
			mInit+=mAGRin;
		}catch(Exception p){
		}
				
		double TInit =pInit*VInit/(mInit*sInit.get_R());
		
		zn[0]=new Zone(CP,pInit, VInit, TInit, mInit, sInit, false, 0);
		return zn;
	}
	

	public int get_anzZonen() {		
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
	protected void checkEinspritzungen(MasterEinspritzung me) {
		for(int i=0;i<me.get_AllInjections().length;i++){			
			if(me.get_AllInjections()[i].get_ID_Zone()!=0){
				try {
					throw new ParameterFileWrongInputException("Fuer die Ladungswechselanalyse " +
							"koennen die Einspritzungen" +
							"nur in Zone 0 erfolgen.\n Gewaehlt wurde aber Zone "+ 
							me.get_AllInjections()[i].get_ID_Zone());
				} catch (ParameterFileWrongInputException e) {				
					e.stopBremo();
				}
			}	
		}		
	}

	@Override
	public VektorBuffer get_dQw_buffer() {
		return null;
	}

	@Override
	public VektorBuffer get_dm_buffer() {
		return null;
	}

	@Override
	public VektorBuffer get_dQb_buffer() {
		return null;
	}
	
	//fuer Verlustteilung Frank Haertel
	@Override 
	public VektorBuffer get_p_buffer() { 
	    return p_buffer; 
	} 
	
	/** 
	 * Setzen der Startmasse für die Berechnung von Xb. Zudem setzen der Startbedingungen von Qburn, mb und Xb
	 * @param m_neu
	 */
	public void set_mInit(double m_neu){
		this.mInit = m_neu;
		this.time_alt = 0;
		Qburn=0;
	  	zonenMasseVerbrannt=0;
	  	fortschritt=0;
	  	firstEB=false;
	}
}


