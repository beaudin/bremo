package berechnungsModule.Berechnung;

import io.DurchflusskennzahlFileReader;
import io.VentilhubFileReader;

import java.util.Hashtable;
import java.util.Vector;

import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.PostProcessor;
import berechnungsModule.blowby.BlowBy;
import berechnungsModule.gemischbildung.Frommelt;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import berechnungsModule.turbulence.TurbulenceModel;
import berechnungsModule.wandwaerme.PerfektIsoliert;
import berechnungsModule.wandwaerme.WandWaermeUebergang;
import bremo.parameter.BrennverlaufDaten;
import bremo.parameter.IndizierDaten;
import misc.VektorBuffer;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import bremo.parameter.CasePara;
import bremo.sys.Solver;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.NegativeMassException;
import bremoExceptions.ParameterFileWrongInputException;

/**
 * Klasse zur Durchfuehrung einer APR (1 zonig) über den kompletten Zyklus (inkl. LW-Teil). Dabei wird mit
 * vorgegebenem Brennverlauf der entsprechende Zylinderdruck berechnet. <br>
 * Inputfileparameter: <br>
 * mAGR_extern [kg/h], p_Ini [Pa], m_ini [kg] oder T_ini_APRkpl [K], pEin [Pa], pAus [Pa]
 * verbrennungsBeginnAutoDetect, verbrennungsBeginn [KWnZOT] / [s],
 */

public class APR_kompletterZyklus extends APR {
	
	private WandWaermeUebergang wandWaermeModell;
	private Motor motor;
	private GleichGewichtsRechner gg;
	private MasterEinspritzung masterEinspritzung;
	private BlowBy blowbyModell;
	private TurbulenceModel turb; // für Bargende
	private IndizierDaten indiD;
	private final int ANZAHL_ZONEN;
	private double dQw, Qw = 0, Qb = 0;
	private double Qwp = 0, Qwh = 0, Qwl = 0;
	private double dmL, mL = 0;
	private double zonenMasseVerbrannt = 0;
	private boolean krstVerbrannt = false;
	private double t_VerbrennungsBeginn;
	private boolean verbrennungHatBegonnen;
	private boolean bargende = false; // Nur wenn Bargende
	
	private double whtfMult;

	private double mINIT;

	private double dmZoneBurn = 0, Qzu;
	private Zone[] initialZones;
	private boolean esBrennt = false;
	private double dQburnMAX = 0;
	private double fortschritt = 0;

	// protected final IndizierDaten indiD; hier mit vorgegebenem Brennverlauf, statt Druck
	protected final BrennverlaufDaten brennverlauf;

	// Speicher fuer Rechenergebnisse die in anderen Routinen zur Verfügung stehen sollen
	private misc.VektorBuffer T_buffer;
	private misc.VektorBuffer dQb_buffer;
	private misc.VektorBuffer dQw_buffer;
	private misc.VektorBuffer dmb_buffer;
	private misc.VektorBuffer p_buffer;
	
	// für LW - Teil
	private double hub_A, hub_E;
	private double pSaug, TSaug, pAbg, TAbg, kappa, Rgas;
	private DurchflusskennzahlFileReader DF_Datei_Ein;
	private DurchflusskennzahlFileReader DF_Datei_Aus;
	private VentilhubFileReader VH_Datei_Ein;
	private VentilhubFileReader VH_Datei_Aus;
	private GasGemisch gAbgasbehaelter;
	private GasGemisch gFrischluftbehaelter, feuchteLuft;
	private double A_E;	//Referenzfläche für den Durchflusskennwert des Einlassventils
	private double A_A; //Referenzfläche für den Durchflusskennwert des Auslassventils
	private double dmEVTemp;
	private double dm, mGes;
	private double dmAusgabe;
	private double dmAuslass, dmEinlass, mAuslass, mEinlass;
	private double alpha_E_vor; //Durchflusskennwert des Einlassventils, vorwärts
	private double alpha_E_rueck; //Durchflusskennwert des Einlassventils, rückwärts
	private double alpha_A_vor; //Durchflusskennwert des Auslassventils, vorwärts
	private double alpha_A_rueck; //Durchflusskennwert des Auslassventils, rückwärts
	private double dQburn = 0; //aus MasterLWA

// Konstruktoren analog zur APR (erstmal nicht benötigt)
//	protected APR_kompletterZyklus(CasePara cp, boolean waermeVerluste,	String brennverlaufsart, double wertQ, double startQ) {
//		super(cp,new ErgebnisBuffer(cp,"apr_kpl"));
//		cp = cp;
//		brennverlauf = new BrennverlaufDaten(cp, brennverlaufsart, wertQ, startQ);
//		ANZAHL_ZONEN = 1;
//		this.createMe(cp, waermeVerluste);
//	}
//	
//
//	protected APR_kompletterZyklus(CasePara cp, boolean waermeVerluste, String brennverlaufsart, double wertQ) {
//		super(cp,new ErgebnisBuffer(cp,"apr_kpl"));
//		cp = cp;
//		brennverlauf = new BrennverlaufDaten(cp, brennverlaufsart, wertQ);
//		ANZAHL_ZONEN = 1;
//		this.createMe(cp, waermeVerluste);
//	}
//
//	protected APR_kompletterZyklus(CasePara cp, boolean waermeVerluste, String brennverlaufsart) {
//		super(cp,new ErgebnisBuffer(cp,"apr_kpl"));
//		cp = cp;
//		brennverlauf = new BrennverlaufDaten(cp, brennverlaufsart);
//		ANZAHL_ZONEN = 1;
//		this.createMe(cp, waermeVerluste);
//	}
//
//	/**
//	 * Brennverlauf mit Vibeparametern vorgeben
//	 * 
//	 * @param cp
//	 * @param vibeBV
//	 */
//	protected APR_kompletterZyklus(CasePara cp, String rechenBV) {
//		super(cp,new ErgebnisBuffer(cp,"apr_kpl"));
//		cp = cp;
//		if (rechenBV.equalsIgnoreCase("Vibe")) {
//			brennverlauf = new BrennverlaufDaten(cp, "Vibe");
//		} else {
//			brennverlauf = null;
//			try {
//				throw new BirdBrainedProgrammerException(
//						"Es ist nur Vibe implementiert!");
//			} catch (BirdBrainedProgrammerException e) {
//				e.stopBremo();
//			}
//		}
//		ANZAHL_ZONEN = 1;
//		this.createMe(cp, true);
//	}

	protected APR_kompletterZyklus(CasePara cp) {
//		super(cp,new ErgebnisBuffer(cp,"apr_kpl"));
		super(cp);
		brennverlauf = new BrennverlaufDaten(cp);
		ANZAHL_ZONEN = 1;
		this.createMe(cp, true);
	}

	private void createMe(CasePara cp, boolean waermeVerluste) {
		
		whtfMult = CP.get_whtfMult();
		indiD=new IndizierDaten(CP); 
		motor = CP.MOTOR; 
		if (waermeVerluste)
			wandWaermeModell = CP.WAND_WAERME;
		else
			wandWaermeModell = new PerfektIsoliert(CP);
		masterEinspritzung = CP.MASTER_EINSPRITZUNG;	
		gg = CP.OHC_SOLVER;
		this.checkEinspritzungen(masterEinspritzung);
		blowbyModell = CP.BLOW_BY_MODELL;
		if (CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende") || CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("BargendeFVV")) {
			bargende = true;
		}
		if (bargende) { // Nur wenn Bargende
			turb = CP.TURB_FACTORY.get_TurbulenceModel(); // für Bargende
		}
		T_buffer = new misc.VektorBuffer(CP); 
		dQb_buffer = new misc.VektorBuffer(CP);
		dQw_buffer = new misc.VektorBuffer(CP);
		dmb_buffer = new misc.VektorBuffer(CP);
		p_buffer = new misc.VektorBuffer(CP);	
		
		//Initialisierung der Anfangsbedingungen
		initialZones = new Zone[ANZAHL_ZONEN];
		
		//Initialisierung der Anfangsmassen bei Auslassschluss:
		double mLuft_tr = CP.get_mLuft_trocken_ASP(); //trockene Luftmasse pro ASP
		double mW = CP.get_mWasser_Luft_ASP();	//Wassermasse pro Arbeitsspiel
		double mAGRex = CP.get_mAGR_extern_ASP();	//Masse externer AGR kg/ASP
		double mKrst = masterEinspritzung.get_mKrst_Sum_ASP();
		this.mINIT = CP.get_m_ini_APRkpl();
		double mAGRint = mINIT - (mLuft_tr + mW + mKrst + mAGRex);
		
		// analog zur LWA
		gAbgasbehaelter = new GasGemisch("AGR_intern");
		gAbgasbehaelter.set_Gasmischung_molenBruch(((GasGemisch)CP.get_spezAbgas()).get_speziesMassenBruecheDetailToIntegrate());		
		CP.SPEZIES_FABRIK.integrierMich(gAbgasbehaelter);
		
		// Kraftstoff aus allen Einspritzungen wird unabhängig vom Zeitpunkt direkt zum Behaeltergemisch gerechnet
		Spezies krst = masterEinspritzung.get_spezKrstALL();
	
		//Bestimmung der Verbrennungsluftzusammensetzung	
		GasGemisch agrEX = new GasGemisch("AGR_extern");
		agrEX.set_Gasmischung_molenBruch(((GasGemisch)CP.get_spezAbgas()).get_speziesMolenBrueche());	
		CP.SPEZIES_FABRIK.integrierMich(agrEX);
		
		//feuchte Luft
		double mLF = mLuft_tr + mW; //gesamte Masse im Zylinder (ohne Kraftstoff)	
		Hashtable<Spezies,Double>feuchteLuft_MassenBruchHash=new Hashtable<Spezies,Double>();
		feuchteLuft_MassenBruchHash.put(CP.SPEZIES_FABRIK.get_spezLuft_trocken(),mLuft_tr/mLF);		
		feuchteLuft_MassenBruchHash.put(CP.SPEZIES_FABRIK.get_spezH2O(),mW/mLF);
		feuchteLuft=new GasGemisch("feuchteLuft");
		feuchteLuft.set_Gasmischung_massenBruch(feuchteLuft_MassenBruchHash);		
		CP.SPEZIES_FABRIK.integrierMich(feuchteLuft);
		
		//gesamte Masse für Frischgemisch
		double mKrst_SRE = 0;
		for(int k=0;k<CP.get_AnzahlEinspritzungen();k++){
			if(masterEinspritzung.get_Einspritzung(k).equals("SRE") || masterEinspritzung.get_Einspritzung(k).equals("Homogen"))
				mKrst_SRE += masterEinspritzung.get_Einspritzung(k).get_mKrst_ASP();
		}
		double mGes = mLF + mAGRex + mKrst_SRE; 
		//Frischgemisch
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();		
		frischGemisch_MassenbruchHash.put(feuchteLuft, mLF/mGes);
		frischGemisch_MassenbruchHash.put(agrEX, mAGRex/mGes);
		for(int k=0;k<CP.get_AnzahlEinspritzungen();k++){
			if(masterEinspritzung.get_Einspritzung(k).equals("SRE") || masterEinspritzung.get_Einspritzung(k).equals("Homogen"))
				frischGemisch_MassenbruchHash.put(masterEinspritzung.get_Einspritzung(k).get_Krst(),
						masterEinspritzung.get_Einspritzung(k).get_mKrst_ASP()/mGes);
		}
		GasGemisch ggTemp = new GasGemisch("GemischSaugrohr");
		ggTemp.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);
		gFrischluftbehaelter = ggTemp;
		
		Hashtable<Spezies, Double>gemischINIT_MassenbruchHash=new Hashtable<Spezies,Double>();
//		gemischINIT_MassenbruchHash.put(feuchteLuft, mLF/mINIT);
//		gemischINIT_MassenbruchHash.put(agrEX, mAGRex/mINIT);
//		gemischINIT_MassenbruchHash.put(krst, mKrst/mINIT);
//		gemischINIT_MassenbruchHash.put(gAbgasbehaelter, mAGRint/mINIT);
		gemischINIT_MassenbruchHash.put(gAbgasbehaelter, 1.0);
		GasGemisch gemischINIT = new GasGemisch("GemischINIT");
		gemischINIT.set_Gasmischung_massenBruch(gemischINIT_MassenbruchHash);
		
		TSaug = CP.get_T_LadeLuft();
		TAbg = CP.get_T_Abgas();
		A_E = CP.get_ReferenzflaecheEinlass();
		A_A = CP.get_ReferenzflaecheAuslass();
		
		DF_Datei_Ein=new DurchflusskennzahlFileReader(CP,"Einlass");
		DF_Datei_Aus=new DurchflusskennzahlFileReader(CP,"Auslass");
		VH_Datei_Ein=new VentilhubFileReader(CP, "Einlass");
		VH_Datei_Aus=new VentilhubFileReader(CP, "Auslass");
		
		dmEVTemp = 0;

		// Anfangsbedingungen setzen
		// p Init
		double p_init = indiD.get_pZyl(CP.get_Auslassoeffnet());
		// V Init
		double V_init = motor.get_V(CP.get_Auslassoeffnet());
		double R = gemischINIT.get_R();
		
		
		double T_init = (p_init * V_init) / (mINIT * R);
		initialZones[0] = new Zone(CP, p_init, V_init, T_init, mINIT, gemischINIT, false, 0);
		
		Qzu = masterEinspritzung.get_mKrst_Sum_ASP()* masterEinspritzung.get_spezKrstALL().get_Hu_mass();
		if (bargende) { // Nur wenn Bargende
			turb.initialize(initialZones, 0);
		}
	}

	public Zone[] calc_dQburn(Zone[] zonen) {
		return zonen;
	}

	public Zone[] ersterHSBrennraum(double time, Zone[] zonen_IN) { // analog zu einer LWA, bzw. APR
		
		// LW-Teil
		// Initialisierung
		dm = 0;
		dmAusgabe = 0;
		dmEinlass = 0;
		dmAuslass = 0;
		pSaug =	indiD.get_pEin(time);
		pAbg = indiD.get_pAus(time);

		// Ventilhuebe
		hub_E = VH_Datei_Ein.get_Hub(time);
		hub_A = VH_Datei_Aus.get_Hub(time);
		// Durchflusskennzahlen, vorwaerts und rueckwärts, Einlass und Auslass
		alpha_E_vor = DF_Datei_Ein.get_alphaVorwaerts(hub_E);
		alpha_E_rueck = DF_Datei_Ein.get_alphaRueckwaerts(hub_E);
		alpha_A_vor = DF_Datei_Aus.get_alphaVorwaerts(hub_A);
		alpha_A_rueck = DF_Datei_Aus.get_alphaRueckwaerts(hub_A);
		
		Spezies frischGemisch = this.get_frischGemisch();

		// dU = dQ_b + dQ_w + dW_k + dm_e*h_e + dm_a*h_a

		double dQburn = brennverlauf.get_dQburn(time);
		double p = zonen_IN[0].get_p();
		double T = zonen_IN[0].get_T();

		// Leckagestrom abführen
		dmL = blowbyModell.get_mLeckage(time, zonen_IN)	* CP.SYS.WRITE_INTERVAL_SEC;
		if (dmL >= 0) {
			try {
				zonen_IN[0].set_dm_aus(dmL);
			} catch (NegativeMassException nme) {
				nme.log_Warning("BlowBy führte zu einer Entleerung der Zone ! \n"
						+ "BlowBy-Eingaben überprüfen.");
				nme.stopBremo();
			}
		} else {
			zonen_IN[0].set_dm_ein(-dmL, T, zonen_IN[0].get_ggZone());
		}

		// Verbrennungswärme zuführen
		zonen_IN[0].set_dQ_ein_aus(dQburn);

		// Wandwärme bestimmen und dann abführen
		dQw = wandWaermeModell.get_WandWaermeStrom(time, zonen_IN, fortschritt,
				T_buffer);
		dQw = whtfMult * dQw;
		zonen_IN[0].set_dQ_ein_aus(-1 * dQw);

		// Verdampfungswärme abführen
		zonen_IN = masterEinspritzung.entnehme_dQ_krstDampf(time, zonen_IN);

		// Einspritzung des Kraftstoffs
		zonen_IN = masterEinspritzung.fuehre_diff_mKrst_dampffoermig_zu(time, zonen_IN);

		// Verhindert das Aufsummieren des Fehlers wenn der Brennverlauf
		// vor der eigentlichen Verbrennung etwas schwingt
		dmZoneBurn = 0;
		if (esBrennt && dQburn > 0 && !krstVerbrannt)
			dmZoneBurn = convert_dQburn_2_dmKrstBurn(dQburn, frischGemisch, T, p);
		// Verbrennendes Masseelement entnehmen
		if (dmZoneBurn > 0) {
			try {
				zonen_IN[0].set_dm_aus(dmZoneBurn, frischGemisch);
				GasGemisch rauchgas;
				rauchgas = new GasGemisch("Rauchgas");
				rauchgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, T, frischGemisch));

				// Massenelement wieder zumischen
				zonen_IN[0].set_dm_ein(dmZoneBurn, T, rauchgas);

			} catch (NegativeMassException nmE) {
				nmE.log_Warning();
				krstVerbrannt = true;
			}
		}

		if (bargende) { // Nur wenn Bargende
			this.turb.update(zonen_IN, time);
		}


		// Einlassströmung
		if (hub_E > 0) {
			if (pSaug < p) { // Saugrohrdruck < Zylinderdruck, Masse strömt
								// aus dem Zylinder heraus
				kappa = zonen_IN[0].get_ggZone().get_kappa(T); // Gemisch kommt aus dem Zylinder
				Rgas = zonen_IN[0].get_ggZone().get_R();
				dm = A_E * alpha_E_rueck * this.get_Massenstromdichte(p, T, pSaug, kappa, Rgas);
				dmEVTemp += dm; // Ausgeschobene Masse speichern, um sie später zurückzuführen -- mn03.14
				try {
					zonen_IN[0].set_dm_aus(dm);
				} catch (NegativeMassException e) {
					e.printStackTrace();
				}
				dmAusgabe += -1 * dm;
				dmEinlass += -1 * dm;

			} else if (pSaug > p) { // Saugrohrdruck > Zylinderdruck, Masse
										// strömt durch den Einlasskanal hinein
				if (dmEVTemp > 0) { // Ausgeschobene Masse berücksichtigen --
									// mn03.14
					GasGemisch gAusschiebebehaelter = zonen_IN[0].get_ggZone();
					kappa = gAusschiebebehaelter.get_kappa(zonen_IN[0].get_T()); // Annahme, dass adiabat ausgeschoben und angesaugt wird.
					Rgas = gAusschiebebehaelter.get_R();
					dm = A_E * alpha_E_vor * this.get_Massenstromdichte(pSaug, zonen_IN[0].get_T(), p, kappa, Rgas);
					zonen_IN[0].set_dm_ein(dm, zonen_IN[0].get_T(), gAusschiebebehaelter);
				} else { // Wenn nichts ausgeschoben wurde oder ausgeschobene
							// Masse aufgebraucht ist, dann erfolgt die Entnahme
							// aus dem Frischgemischbehälter
					kappa = gFrischluftbehaelter.get_kappa(TSaug); // Gemisch kommt aus dem Frischluftbehälter
					Rgas = gFrischluftbehaelter.get_R();
					dm = A_E* alpha_E_vor* this.get_Massenstromdichte(pSaug, TSaug, p,kappa, Rgas);
					zonen_IN[0].set_dm_ein(dm, TSaug, gFrischluftbehaelter);
				}
				dmEVTemp -= dm;
				if (dmEVTemp < 0)
					dmEVTemp = 0; // Zum Auffangen von Fehlern
				dmAusgabe += dm;
				dmEinlass += dm;
			} else {
			}
		}
		// Auslassströmung
		if (hub_A > 0) {
			if (pAbg > p) { // Abgasdruck > Zyldruck, Masse strömt durch den
								// Auslasskanal hinein
				kappa = gAbgasbehaelter.get_kappa(TAbg); // Gemisch kommt aus
															// dem Abgasbehälter
				Rgas = gAbgasbehaelter.get_R();
				dm = A_A* alpha_A_rueck* this.get_Massenstromdichte(pAbg, TAbg, p, kappa, Rgas);
				zonen_IN[0].set_dm_ein(dm, TAbg, gAbgasbehaelter);
				dmAusgabe += dm;
				dmAuslass += dm;
			} else if (pAbg < p) { // Abgasdruck < Zyldruck, Masse strömt aus dem Auslasskanal heraus
				kappa = zonen_IN[0].get_ggZone().get_kappa(T); // Gemisch kommt aus dem Zylinder
				Rgas = zonen_IN[0].get_ggZone().get_R();
				dm = A_A * alpha_A_vor * this.get_Massenstromdichte(p, T, pAbg, kappa,	Rgas);
				try {
					zonen_IN[0].set_dm_aus(dm);
				} catch (NegativeMassException e) {
					e.log_Warning();
				}
				dmAusgabe += -1 * dm;
				dmAuslass += -1 * dm;
			} else {
			}
		}

		// Überprüfung von p, T, m auf Plausibilität
		isValuePositivNumber(zonen_IN[0].get_p(), "Der Druck zum Zeitpunkt " + time + " s (" + CP.convert_SEC2KW(time) + " KW)"	+ " ist negativ oder NaN.");
		isValuePositivNumber(zonen_IN[0].get_T(), "Die Temperatur zum Zeitpunkt " + time + " s (" + CP.convert_SEC2KW(time) + " KW)" + " ist negativ oder NaN.");
		isValuePositivNumber(zonen_IN[0].get_m(), "Die Masse zum Zeitpunkt " + time + " s (" + CP.convert_SEC2KW(time) + " KW)" + " ist negativ oder NaN.");

		return zonen_IN;
	}
	
	/**
	 * 
	 * @param dQburn
	 * @param frischGemisch
	 * @param Tu
	 * @param Tv
	 * @param p
	 * @return
	 */
	protected double convert_dQburn_2_dmKrstBurn(double dQburn, Spezies frischGemisch, double Tu, double p) {
		double dmBurn = 0;
		GleichGewichtsRechner gg = CP.OHC_SOLVER;
		GasGemisch abgas = new GasGemisch("Abgas");
		abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, Tu,
				frischGemisch));

		// 1.HS fuer durchstroemten Kontrollraum:
		// m*he-m*ha-Qb=0
		double he = frischGemisch.get_h_mass(Tu);
		double ha = abgas.get_h_mass(Tu);
		double delta_h = he - ha;

		dmBurn = 1 * dQburn / delta_h;
		// ist eigentlich identisch Funktioniert aber nicht für fettes Gemsich
		// return dQburn/1/frischGemisch.get_Hu_mass();
		if (dmBurn > 0)
			return dmBurn;
		else
			return 0;
	}
	
	public boolean isDVA() {
		return false;
	}

	
	public void bufferErgebnisse(double time, Zone[] zn) {
		
		double dQburn = brennverlauf.get_dQburn(time);
		double p = zn[0].get_p();
		dQb_buffer.addValue(time, dQburn);
		dQw_buffer.addValue(time, dQw);
		dmb_buffer.addValue(time, dmZoneBurn);
		p_buffer.addValue(time, p);

		if (dQburn > dQburnMAX)
			dQburnMAX = dQburn;

		if (CP.is_VerbrennungAutoDetect() == false)
			t_VerbrennungsBeginn = CP.get_verbrennungsBeginnSEC();
		else
			t_VerbrennungsBeginn = t_VerbrennungsBeginn();
		if (time >= t_VerbrennungsBeginn) {
			verbrennungHatBegonnen = true;
		}
		esBrennt = verbrennungHatBegonnen;

		// Berechnen integraler Werte
		zonenMasseVerbrannt = zonenMasseVerbrannt + dmZoneBurn * CP.SYS.WRITE_INTERVAL_SEC;
		fortschritt = zonenMasseVerbrannt / mINIT;
		Qb = Qb + dQburn * CP.SYS.WRITE_INTERVAL_SEC;
		Qw = Qw + dQw * CP.SYS.WRITE_INTERVAL_SEC;
		mL = mL + dmL * CP.SYS.WRITE_INTERVAL_SEC;
		this.masterEinspritzung.berechneIntegraleGroessen(time, zn);
		double xQ = Qb / Qzu;

		int i = -1;
		i += 1;
		super.buffer_EinzelErgebnis("Kurbelwinkel [°KW]", Math.round(10 * CP.convert_SEC2KW(time)) / 10.0, i);

//		i += 1;
//		super.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]", time, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("Brennraumvolumen [m3]", motor.get_V(time),
				i);

		i += 1;
		super.buffer_EinzelErgebnis("p [bar]", zn[0].get_p()*1e-5, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("p_Exp [bar]", indiD.get_pZyl(time) * 1e-5, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("p_Ein [bar]", indiD.get_pEin(time) * 1e-5, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("p_Aus [bar]", indiD.get_pAus(time) * 1e-5, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("Hub_Ein [mm]", VH_Datei_Ein.get_Hub(time) * 1e4, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("Hub_Aus [mm]", VH_Datei_Aus.get_Hub(time) * 1e4, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("dmV [kg/s]", dm/CP.SYS.WRITE_INTERVAL_SEC, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("dmV [kg/KW]", CP.convert_ProSEC_2_ProKW(dm/CP.SYS.WRITE_INTERVAL_SEC), i);
		
		i += 1;
		mEinlass += (dmEinlass * CP.SYS.WRITE_INTERVAL_SEC);
		super.buffer_EinzelErgebnis("mEinlass [kg]", mEinlass, i);
		
		i += 1;
		mAuslass += (dmAuslass * CP.SYS.WRITE_INTERVAL_SEC);
		super.buffer_EinzelErgebnis("mAuslass [kg]", mAuslass, i);
		
		i += 1;
		super.buffer_EinzelErgebnis("dQh [J/s]", dQburn - dQw, i);

		i += 1;
		super.buffer_EinzelErgebnis("dQh [J/KW]", CP.convert_ProSEC_2_ProKW(dQburn - dQw), i);

		i += 1;
		super.buffer_EinzelErgebnis("Qh [J]", Qb - Qw, i);

		i += 1;
		super.buffer_EinzelErgebnis("dQb [J/s]", dQburn, i);

		i += 1;
		super.buffer_EinzelErgebnis("dQb [J/KW]", CP.convert_ProSEC_2_ProKW(dQburn), i);

		i += 1;
		super.buffer_EinzelErgebnis("Qb [J]", Qb, i);

		i += 1;
		double Tm = wandWaermeModell.get_Tmb(zn);
		super.buffer_EinzelErgebnis("T_mittel [K]", Tm, i);
		T_buffer.addValue(time, Tm);
		
		i += 1;
		mGes = zn[0].get_m();
		super.buffer_EinzelErgebnis("m [kg]", zn[0].get_m(), i);

		i += 1;
		super.buffer_EinzelErgebnis("dQw [J/s]", dQw, i);

		i += 1;
		super.buffer_EinzelErgebnis("dQw [J/KW]", CP.convert_ProSEC_2_ProKW(dQw), i);

		i += 1;
		super.buffer_EinzelErgebnis("Qw [J]", Qw, i);

		i += 1;
		super.buffer_EinzelErgebnis("Xb[-]", fortschritt, i);

		i += 1;
		super.buffer_EinzelErgebnis("Qb/Qzu [-]", xQ, i);
		
		double []mi=zn[0].get_mi();
		for(int idx=0;idx<mi.length;idx++){
			i += 1;
			super.buffer_EinzelErgebnis(CP.SPEZIES_FABRIK.get_Spez(idx).get_name()
					+"_Massenbruch [kg]" ,mi[idx]/zn[0].get_m(),i);
			}
		

		i += 1;
		double alpha = wandWaermeModell.get_WaermeUebergangsKoeffizient(time,zn, fortschritt);
		super.buffer_EinzelErgebnis("Alpha [W/(m^2K)]", alpha, i);
		
		i+=1;		
		super.buffer_EinzelErgebnis("Brennraumfläche [m^2]",wandWaermeModell.get_BrennraumFlaeche(time),i);	

		// Schleppdruck in bar
		i += 1;
		// super.buffer_EinzelErgebnis("Schleppdruck [bar]",wandWaermeModell.get_Schleppdruck(time,zn)*1E-5,i);
		super.buffer_EinzelErgebnis("pSchleppWHT [bar]",wandWaermeModell.get_Schleppdruck() * 1E-5, i);

		i += 1;
		double HeatFlux = wandWaermeModell.get_WandWaermeStromDichte(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD [MW/m^2]", HeatFlux * 1E-6, i);

		i += 1;
		double HeatFluxPiston = wandWaermeModell.get_WandWaermeStromDichtePiston(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD Kolben [MW/m^2]", HeatFluxPiston * 1E-6, i);

		i += 1;
		double HeatFluxHead = wandWaermeModell.get_WandWaermeStromDichteHead(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD Head [MW/m^2]", HeatFluxHead * 1E-6, i);

		i += 1;
		double HeatFluxCyl = wandWaermeModell.get_WandWaermeStromDichteCyl(time, zn, fortschritt);
		super.buffer_EinzelErgebnis("WSD Liner [MW/m^2]", HeatFluxCyl * 1E-6, i);

		i += 1;
		double whtp = wandWaermeModell.get_WandWaermeStromPiston(time, zn, fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("dQw Kolben [J/s]", whtp, i);
		Qwp = Qwp + whtp * CP.SYS.WRITE_INTERVAL_SEC; // Kommt einen Zeitschrit zu spät?

		i += 1;
		double whth = wandWaermeModell.get_WandWaermeStromHead(time, zn, fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("dQw Head [J/s]", whth, i);
		Qwh = Qwh + whth * CP.SYS.WRITE_INTERVAL_SEC; // Kommt einen Zeitschrit zu spät?

		i += 1;
		double whtl = wandWaermeModell.get_WandWaermeStromCyl(time, zn,	fortschritt, T_buffer);
		super.buffer_EinzelErgebnis("dQw Liner [J/s]", whtl, i);
		Qwl = Qwl + whtl * CP.SYS.WRITE_INTERVAL_SEC; // Kommt einen Zeitschrit zu spät?

		i += 1;
		super.buffer_EinzelErgebnis("Qw Kolben [J]", Qwp, i);

		i += 1;
		super.buffer_EinzelErgebnis("Qw Head [J]", Qwh, i);

		i += 1;
		super.buffer_EinzelErgebnis("Qw Liner [J]", Qwl, i);

		if (bargende) { // Nur wenn Bargende
			i += 1;
			super.buffer_EinzelErgebnis("TKE_M [m^2/s^2]", this.turb.get_k(zn, time), i);
		}

		i += 1;
		super.buffer_EinzelErgebnis("dmL [kg/s]", dmL, i);

		i += 1;
		super.buffer_EinzelErgebnis("dmL [kg/KW]",
				CP.convert_ProSEC_2_ProKW(dmL), i);

		i += 1;
		super.buffer_EinzelErgebnis("mL [kg]", mL, i);

		// buffer mass of fuel and characteristic evaporation time for each
		// injection
		int einspritzungen = CP.MASTER_EINSPRITZUNG.get_AllInjections().length;
		for (int index = 0; index < einspritzungen; index++) {
			if (CP.MASTER_EINSPRITZUNG.get_ModulWahl(CP.MASTER_EINSPRITZUNG.EINSPRITZ_MODELL_FLAG + index,
				CP.MASTER_EINSPRITZUNG.MOEGLICHE_EINSPRITZ_MODELLE).equals(Frommelt.FLAG)) { // Nur wenn Frommelt
				i += 1;
				super.buffer_EinzelErgebnis("Kraftstoffmasse_" + index + " [kg]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Mass(time), i);
				i += 1;
				super.buffer_EinzelErgebnis("Kraftstoffrate_" + index + " [kg/s]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Rate(time), i);
				i += 1;
				super.buffer_EinzelErgebnis("Tau_" + index + " [s]", CP.MASTER_EINSPRITZUNG.get_AllInjections()[index].get_Tau(time), i);
			}
			i += 1;
			super.buffer_EinzelErgebnis("Kraftstoffdampf_" + index + " [kg]", this.masterEinspritzung.get_Einspritzung(index).get_mKrst_verdampft(time), i);
		}
		
		i+=1;
		super.buffer_EinzelErgebnis("cv[J/kg]", zn[0].get_ggZone().get_cv_mass(zn[0].get_T()),i);	
		
		i+=1;
		super.buffer_EinzelErgebnis("cp [J/kg]", zn[0].get_ggZone().get_cp_mass(zn[0].get_T()),i);	

		i+=1;
		super.buffer_EinzelErgebnis("kappa [-]", zn[0].get_ggZone().get_kappa(zn[0].get_T()),i);
		
		i+=1;
		super.buffer_EinzelErgebnis("lambda [-]", zn[0].get_ggZone().get_lambda(), i);
	}		

	
	/**
	 * Erstellt eine Objetk vom Typ Spezies, als Mischung aus Verbrennungsluft
	 * (Luft + AGR) und allen Kraftstoffen die in dem Arbeitsspiel verbrannt
	 * werden. Unabhängig von der tatsaechlichen Gemischbildung verbrennt der
	 * Kraftstoff immer mit dem selben Luftverhältnis
	 * 
	 * @param time
	 * @param fortschritt
	 * @return
	 */
	private Spezies get_frischGemisch() {
		Spezies verbrennungsLuft = CP.get_spezVerbrennungsLuft();
		Spezies krst = masterEinspritzung.get_spezKrstALL();
		Hashtable<Spezies, Double> frischGemisch_MassenbruchHash = new Hashtable<Spezies, Double>();
		double mKrst = masterEinspritzung.get_mKrst_Sum_ASP();
		double mVerbrennungsLuft = CP.get_mVerbrennungsLuft_ASP();
		double mGes = mVerbrennungsLuft + mKrst;
		frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft / mGes);
		frischGemisch_MassenbruchHash.put(krst, mKrst / mGes);

		GasGemisch frischGemisch = new GasGemisch("Frischgemisch");
		frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);

		return frischGemisch;
	}

	public double get_turbFaktor(Zone[] zonen_IN, double time) {
		return turb.get_k(zonen_IN, time);
	}
	
	public Zone[] get_initialZones() {
		return initialZones;
	}

	public Zone[] get_initialZonesWhileRunning() {
		return null;
	}

	public int get_anzZonen() {
		return 1;
	}

	public VektorBuffer get_dm_buffer() {
		return dmb_buffer;
	}

	public VektorBuffer get_dQw_buffer() {
		return dQw_buffer;
	}

	public VektorBuffer get_dQb_buffer() {
		return dQb_buffer;
	}

	public VektorBuffer get_p_buffer() {
		return p_buffer;
	}

	private double verbrennungsBeginn = -5.55;

	public double t_VerbrennungsBeginn() {
		if (verbrennungsBeginn != -5.55) {
			return verbrennungsBeginn;
		}

		double x0 = CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;
		double time = 0;
		double Qbtemp = 0;
		int anzSimWerte = CP.SYS.ANZ_BERECHNETER_WERTE;
		boolean verbrennungsbeginnGefunden = false;

		// while(verbrennungsbeginnGefunden==false){
		for (int i = 1; i < anzSimWerte - 2; i++) { // Verbrennungsbeginn im
													// letzten Wert würde keinen
													// Sinn machen...
			time = x0 + i * CP.SYS.WRITE_INTERVAL_SEC;
			double dQburn = brennverlauf.get_dQburn(time);
			Qbtemp = Qbtemp + dQburn * CP.SYS.WRITE_INTERVAL_SEC;
			double xQ = Qbtemp / Qzu;
			if (xQ > 0.05) {
				verbrennungsbeginnGefunden = true;
				verbrennungsBeginn = time;
				return time;
			}
		}
		// }

		return 0;
	}
	
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
	
	
	public double get_dQburn() {
		return dQburn;
	}
	
	/**
	 * Gibt die angesaugte Luftmasse [kg] und die Gesamtmasse [kg] in diesem ASP zurück.
	 * @author neurohr
	 * @return {mLuft, mGes}
	 */
	public double[] get_massen(){
		double[] massen = {mEinlass, mGes};
		return massen;
	}

	private double get_Massenstromdichte(double pHi, double THi, double pLo,
			double kappa, double Rgas) {
		if (pLo / pHi <= Math.pow(2 / (kappa + 1), kappa / (kappa - 1))) {
			// Kritische Stroemung (siehe Heywood S. 226)
			double v = Math.sqrt(kappa)
					* Math.pow(2 / (kappa + 1), (kappa + 1) / ((kappa - 1) * 2));
			double w = pHi / (Math.sqrt(Rgas * THi));
			return v * w;
		} else {
			double w = pHi / (Math.sqrt(Rgas * THi));
			double x = Math.sqrt(2 * kappa / (kappa - 1));
			double y = Math.pow(pLo / pHi, 1 / kappa);
			double z = Math
					.sqrt(1 - Math.pow(pLo / pHi, ((kappa - 1) / kappa)));
			return w * x * y * z;
		}
	}
	
	
	// 
	/** Überprüft, ob value eine positive Zahl ist (zur Überprüfung der Plausibilität von T, p, m)
	 * @param value
	 * @param message
	 * @return
	 */
	private boolean isValuePositivNumber(double value, String message) {
		boolean positivValue = true;
		if (value < 0 || Double.isNaN(value)) {
			positivValue = false;
			try {
				throw new ParameterFileWrongInputException(message);
			} catch (ParameterFileWrongInputException e) {
				e.stopBremo();
			}
		}
		return positivValue;
	}
	
	
	@Override
	public boolean initialiseBurntZone() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
