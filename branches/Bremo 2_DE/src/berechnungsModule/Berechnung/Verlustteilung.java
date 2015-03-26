package berechnungsModule.Berechnung;

import io.AusgabeSteurung;
import io.FileWriter_txt;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import matLib.Integrator;
import matLib.MatLibBase;
import misc.LittleHelpers;
import misc.VektorBuffer;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.LadungswechselAnalyse.LadungsWechselAnalyse_ohneQb;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import bremo.sys.Solver;
import bremoExceptions.CaseParaNotInstantiatedException;
import bremoExceptions.ParameterFileWrongInputException;

public class Verlustteilung {

	protected CasePara CP;
	double kappa = 1.4;
	double epsilon;

	private MasterEinspritzung masterEinspritzung;
	private final Motor motor;

	public ErgebnisBuffer Wandwaermeverlust_buffer;
	public ErgebnisBuffer Ergebnis;
	public ErgebnisBuffer ErgebnisLWA;

	private final Integrator inti;

	double pmiLWAideal = Double.NaN;
	double etaLWAideal = Double.NaN;
	double pmiLWArealGem = Double.NaN;
	double etaLWArealGem = Double.NaN;
	double pmiLWA = Double.NaN;
	double etaLWA = Double.NaN;
	double pmiHCCO = Double.NaN;
	double etaHCCO = Double.NaN;

	public Verlustteilung(CasePara cp) {
		this.CP = cp;
		motor = CP.MOTOR;
		Wandwaermeverlust_buffer = new ErgebnisBuffer(CP, "");
		inti = new Integrator();
	}

	// Wirkungsgrad idealer Gleichraumprozess
	public double berechneEtaIdeal() {
		epsilon = CP.get_Verdichtung();
		masterEinspritzung = CP.MASTER_EINSPRITZUNG;

		double etaIdeal;
		etaIdeal = 1 - 1 / (Math.pow(epsilon, kappa - 1));
		return etaIdeal;
	}

	// pmi idealer Gleichraumprozess
	public double berechnePmiIdeal() {
		double pmi_ideal;
		double etaIdeal = berechneEtaIdeal();

		pmi_ideal = etaIdeal * masterEinspritzung.get_mKrst_Sum_ASP()
				* masterEinspritzung.get_spezKrstALL().get_Hu_mass()
				/ ((Motor_HubKolbenMotor) motor).get_Hubvolumen();
		return pmi_ideal;
	}

	// Berechnen der Verluste MTZ 2/2005 Jahrgang 66 S.120f
	public void berechneVerluste() {

		masterEinspritzung = CP.MASTER_EINSPRITZUNG;

		Ergebnis = new ErgebnisBuffer(CP, "txt");
		double x0;
		// initial value of x
		x0 = CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;
		int anzSimWerte = CP.SYS.ANZ_BERECHNETER_WERTE;
		double time;
		int i;
		// Zeitachse
		// for(int k=1;k<anzSimWerte;k++){
		// time=x0+k*CP.SYS.WRITE_INTERVAL_SEC;
		//
		// Versatz zwischen -360 und Rechnungsbeginn
		int versatz = (int) Math.round((360 + CP.SYS.RECHNUNGS_BEGINN_DVA_KW)
				/ CP.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC)); // Kerrom:
																			// Math.round()
																			// hinzugefügt
		// Anzahl Werte von -360 bis 360
		int anz = (int) (CP.SYS.DAUER_ASP_KW / CP
				.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC));

		// gesamte Druckspur zusammenbauen...
		// IndizierDaten indi=new IndizierDaten(CP,true);
		IndizierDaten indi = new IndizierDaten(CP);
		double[] pGesamt = new double[anz];

		// for (int l = 0; l < anz; l++) {
		// time=CP.convert_KW2SEC(-360)+l*CP.SYS.WRITE_INTERVAL_SEC;
		// pGesamt [l]= indi.get_pZyl(time); //[Pa]
		// }

		// for(int k=1;k<anz;k++){
		// //+CP.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC)
		for (int k = 0; k < anz; k++) {
			time = CP.convert_KW2SEC(-360) + k * CP.SYS.WRITE_INTERVAL_SEC;
			//
			pGesamt[k] = indi.get_pZyl(time); // [Pa]

			Ergebnis.buffer_EinzelErgebnis("Kurbelwinkel [°KW]",
					Math.round(10 * CP.convert_SEC2KW(time)) / 10.0, 0);

			Ergebnis.buffer_EinzelErgebnis("Zeit [s n. Rechenbeginn]", time, 1);

			// Brennraumvolumen

			Ergebnis.buffer_EinzelErgebnis("Brennraumvolumen [m3]",
					motor.get_V(time), 2);
		}

		// Realer Ladungswechsel bzw. Originaldruckverlauf FALL 1h
		i = 3;

		for (int j = 0; j < pGesamt.length; j++) {
			Ergebnis.buffer_EinzelErgebnis("p_Original [bar]",
					pGesamt[j] / 1e5, i);
		}

		double pmiOriginal = LittleHelpers.berechnePmi(CP, pGesamt);
		double etaOriginal = pmi2eta(pmiOriginal);
		AusgabeSteurung.Warning("Verlustteilung FALL 1h: Originaldruckverlauf");

		// Zuerst mit Wandwärmeverlust FALL 1f rechnen, damit für idealen
		// Ladungswechsel vollständig expandiert
		APR_homogen_EinZonig wandwaermeverlust = new APR_homogen_EinZonig(CP,
				true, "Vorgabe");
		VektorBuffer mitWandwaermeverlust = berechnungDurchfuehren(wandwaermeverlust);
		double[] pMit = mitWandwaermeverlust.getValues();

		for (int k = 0; k < pMit.length; k++) {
			pGesamt[k + versatz] = pMit[k]; // [Pa]
		}

		// //////////////////Kerrom//////////////////////////////
		// Orte im Array
		int UTvorZOTimArray = (int) Math.round(180 / CP
				.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC));
		int UTnachZOTimArray = (int) Math.round(540 / CP
				.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC));
		// ///////////////////////////////////////////////////////////////////////////////////////////

		// ideale Ladungswechselverluste FALL 1g
		i += 1;

		for (int k = 0; k < UTvorZOTimArray; k++) { // Kerrom:
													// 180/CP.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC)
													// durch UTvorZOTimArray
													// ersetzt
			pGesamt[k] = CP.get_p_LadeLuft();
			// pGesamt [k]= pGesamt [UTvorZOTimArray]; //Kerrom:
			// (int)(180/CP.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC))
			// durch UTvorZOTimArray ersetzt
		}

		for (int k = UTnachZOTimArray; k < pGesamt.length; k++) { // Kerrom:
																	// (int)(540/CP.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC))
																	// durch
																	// UTnachZOTimArray
																	// ersetzt
			pGesamt[k] = CP.get_p_Abgas();
		}

		for (int j = 0; j < pGesamt.length; j++) {
			Ergebnis.buffer_EinzelErgebnis("LWAideal [bar]", pGesamt[j] / 1e5,
					i);
		}
		pmiLWAideal = LittleHelpers.berechnePmi(CP, pGesamt);
		etaLWAideal = pmi2eta(pmiLWAideal);
		AusgabeSteurung
				.Warning("Verlustteilung FALL 1g: Ladungswechselverluste, ideal");

		// Ladungswechselschleife löschen
		for (int k = 0; k < UTvorZOTimArray; k++) { // Kerrom: UTvorZOTimArray
													// (s.o)
			pGesamt[k] = 0;
		}

		for (int k = UTnachZOTimArray; k < pGesamt.length; k++) { // Kerrom: s.o
			pGesamt[k] = 0;
		}

		// Mit Wandwärmeverlust FALL 1f
		i += 1;
		for (int j = 0; j < pGesamt.length; j++) {
			Ergebnis.buffer_EinzelErgebnis("p_mitWandwärmeverlust [bar]",
					pGesamt[j] / 1e5, i);
		}
		//

		double pmiMit = LittleHelpers.berechnePmi(CP, pGesamt);
		// double pmiMit = LittleHelpers.berechnePmi (CP, pMit );
		double etaMit = pmi2eta(pmiMit);
		AusgabeSteurung
				.Warning("Verlustteilung FALL 1f: Realer Brennverlauf mit Wandwärmeverlusten ");

		// Realgasverhalten FALL 1e
		// NICHT vorhanden
		AusgabeSteurung
				.Warning("Verlustteilung FALL 1e: Realgasverhalten  ==NICHT IMPLEMENTIERT==");
		//

		// Realer Brennverlauf( realer Brennverlauf, adiabat) FALL 1d
		APR_homogen_EinZonig APRbrennverlauf = new APR_homogen_EinZonig(CP,
				false, "Vorgabe");
		VektorBuffer brennverlauf = berechnungDurchfuehren(APRbrennverlauf);
		double[] pBrennverlauf = brennverlauf.getValues();
		i += 1;

		for (int k = 0; k < pBrennverlauf.length; k++) {
			pGesamt[k + versatz] = pBrennverlauf[k]; // [Pa]
		}

		// for(int j=0; j<pMit.length; j++){
		// Ergebnis.buffer_EinzelErgebnis("p_realer Brennverlauf [bar]",pBrennverlauf[j]/1e5,i);
		// }

		for (int j = 0; j < pGesamt.length; j++) {
			Ergebnis.buffer_EinzelErgebnis("p_realer Brennverlauf [bar]",
					pGesamt[j] / 1e5, i);
		}

		// double pmiBrennverlauf = LittleHelpers.berechnePmi (CP, pBrennverlauf
		// );
		double pmiBrennverlauf = LittleHelpers.berechnePmi(CP, pGesamt);
		double etaBrennverlauf = pmi2eta(pmiBrennverlauf);
		AusgabeSteurung
				.Warning("Verlustteilung FALL 1d: Realer Brennverlauf, adiabat");

		// HC/CO-Emissionen Fall 1c'
		boolean HCCOeingabe = false;
		GasGemisch frischGemisch = new GasGemisch("Frischgemisch");
		// MasterEinspritzung me= CP.MASTER_EINSPRITZUNG;
		double mKrst = masterEinspritzung.get_mKrst_Sum_ASP();
		double mVerbrennungsLuft = CP.get_mVerbrennungsLuft_ASP();
		double mGes = mVerbrennungsLuft + mKrst;

		GasGemisch abgas = new GasGemisch("abgas");
		abgas.set_Gasmischung_molenBruch(CP.OHC_SOLVER.get_GG_molenBrueche(1e5,
				300, frischGemisch));

		VektorBuffer dQbuffer = wandwaermeverlust.get_dQb_buffer();
		double verbrennungschwerpunkt = findeVerbrennungsschwerpunkt(dQbuffer);
		double Qmax = masterEinspritzung.get_mKrst_Sum_ASP()
				* masterEinspritzung.get_spezKrstALL().get_Hu_mass();

		double M = abgas.get_M();
		double hc = CP.get_HC();
		double co = CP.get_CO();
		if (hc != 0 && co != 0) {
			HCCOeingabe = true;
			/*
			 * if(hc==0 || co==0 ){ try{ throw new
			 * ParameterFileWrongInputException
			 * ("HC- oder CO-Wert im Inputfile nicht angegeben");
			 * }catch(ParameterFileWrongInputException e){ e.stopBremo(); } }
			 */double Hu_CO = 282.9 * 1e3; // [J/mol] aus R. Pischinger S. 93
			// double Hu_HC=600*1e3; //[J/mol] aus R. Pischinger S. 93
			double Hu_HC = 2044.2 * 1e3; // Ge Liu
			double Qcohc = mGes * (hc * Hu_HC + co * Hu_CO) / M;

			APR_homogen_EinZonig APRhcCo = new APR_homogen_EinZonig(CP, false,
					"Qneu-startNeu", Qmax - Qcohc, verbrennungschwerpunkt);
			VektorBuffer COHC = berechnungDurchfuehren(APRhcCo);
			double[] pHCCO = COHC.getValues();

			i += 1;

			for (int k = 0; k < pHCCO.length; k++) {
				pGesamt[k + versatz] = pHCCO[k]; // [Pa]
			}

			// for(int j=0; j<pMit.length; j++){
			// Ergebnis.buffer_EinzelErgebnis("p_HC_CO [bar]",pHCCO[j]/1e5,i);
			// }
			for (int j = 0; j < pGesamt.length; j++) {
				Ergebnis.buffer_EinzelErgebnis("p_HC_CO [bar]",
						pGesamt[j] / 1e5, i);
			}

			// pmiHCCO = LittleHelpers.berechnePmi (CP, pHCCO );
			pmiHCCO = LittleHelpers.berechnePmi(CP, pGesamt);
			etaHCCO = pmi2eta(pmiHCCO);
		} else {
			HCCOeingabe = false;
		}
		AusgabeSteurung.Warning("Verlustteilung FALL 1c': HC-/CO-Emissionen");

		// Verbrennungslage (adiabat, punktuelle Wärmefreisetzung im
		// Verbrennungsschwerpunkt) FALL 1c
		APR_homogen_EinZonig APRverbrennungslage = new APR_homogen_EinZonig(CP,
				false, "Qneu-startNeu", Qmax, verbrennungschwerpunkt);
		VektorBuffer verbrennungslage = berechnungDurchfuehren(APRverbrennungslage);
		double[] pVerbrennungslage = verbrennungslage.getValues();
		i += 1;

		for (int k = 0; k < pVerbrennungslage.length; k++) {
			pGesamt[k + versatz] = pVerbrennungslage[k]; // [Pa]
		}

		// for(int j=0; j<pMit.length; j++){
		// Ergebnis.buffer_EinzelErgebnis("p_Verbrennungslage [bar]",pVerbrennungslage[j]/1e5,i);
		// }
		for (int j = 0; j < pGesamt.length; j++) {
			Ergebnis.buffer_EinzelErgebnis("p_Verbrennungslage [bar]",
					pGesamt[j] / 1e5, i);
		}

		// double pmiVerbrennungslage = LittleHelpers.berechnePmi (CP,
		// pVerbrennungslage );
		double pmiVerbrennungslage = LittleHelpers.berechnePmi(CP, pGesamt);
		double etaVerbrennungslage = pmi2eta(pmiVerbrennungslage);
		AusgabeSteurung
				.Warning("Verlustteilung FALL 1c: Punktuelle Wärmefreisetzung im Verbrennungsschwerpunkt, adiabat");

		// Reale Ladung (adiabat, punktuelle Wärmefreisetzung im OT) FALL 1b
		APR_homogen_EinZonig APRreal = new APR_homogen_EinZonig(CP, false,
				"Punktuell-OT");
		VektorBuffer real = berechnungDurchfuehren(APRreal);
		double[] pReal = real.getValues();
		i += 1;

		for (int k = 0; k < pReal.length; k++) {
			pGesamt[k + versatz] = pReal[k]; // [Pa]
		}

		// for(int j=0; j<pMit.length; j++){
		// Ergebnis.buffer_EinzelErgebnis("p_Reale Ladung [bar]",pReal[j]/1e5,i);
		// }

		for (int j = 0; j < pGesamt.length; j++) {
			Ergebnis.buffer_EinzelErgebnis("p_Reale Ladung [bar]",
					pGesamt[j] / 1e5, i);
		}

		// double pmiReal = LittleHelpers.berechnePmi (CP, pReal );
		double pmiReal = LittleHelpers.berechnePmi(CP, pGesamt);
		double etaReal = pmi2eta(pmiReal);
		AusgabeSteurung
				.Warning("Verlustteilung FALL 1b: Punktuelle Wärmefreisetzung in OT, adiabat");

		// Idealer Gleichraumprozess FALL 1a

		// //////////////////////Kerrom////////////////////////////////////////////////////////
		double OT_Versatz = motor.get_OT_Versatz(); // [KWnZOT]

		// Orte im Array pGesamt -> "Indizes" des Druckarrays
		int OTimArray = (int) Math.round((360 + OT_Versatz)
				/ CP.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC));
		int anfangExpansionImArray = OTimArray + 1;

		// Berechnungszeitpunkte
		double t_UTvorZOT = CP.convert_KW2SEC(-180); // Beginn der Kompression
														// bei -180 KW
		double t_OT = CP.convert_KW2SEC(0 + OT_Versatz); // Ende der Kompression
															// bei 0 KW (bzw.
															// OT-Versatz)
		double t_anfangExpansion = t_OT + CP.SYS.WRITE_INTERVAL_SEC; // Ende der
																		// Wärmezufuhr=Beginn
																		// der
																		// Expansion
																		// (Wärme
																		// wird
																		// in
																		// einem
																		// Zeitschritt
																		// zugeführt)

		// polytrope Kompression
		// Startzustand (Zustand 1) bei UTvorZOT
		double p_anfangKompression = indi.get_pZyl(t_UTvorZOT); // Druck aus
																// Indizierdaten
		double v_anfangKompression = motor.get_V(t_UTvorZOT); // Volumen V

		berechnePolytropeZustandsaenderung(pGesamt, UTvorZOTimArray, OTimArray,
				t_UTvorZOT, v_anfangKompression, p_anfangKompression);

		// Wärmezufuhr
		double R = 287.1; // [J/(kg K)]
		double cv = R / (kappa - 1); // [J/(kg K)]
		double v_endeKompression = motor.get_V(t_OT);

		double pMaxGleichraum = berechneDruckNachWaermezufuhr(R, Qmax, cv,
				pGesamt[OTimArray], v_endeKompression);

		// polytrope Expansion
		berechnePolytropeZustandsaenderung(pGesamt,
				(anfangExpansionImArray - 1), UTnachZOTimArray - 1,
				t_anfangExpansion, v_endeKompression, pMaxGleichraum);
		// UTnachZOTimArray-1, weil der Wert bei "ende" noch berechnet wird
		// anfangExpansion-1, damit Druck- und Volumenspur übereinstimmen.
		// Der max. Druck taucht nicht in der Druckspur auf
		// (Druck nach Wärmezufuhr im Array = erster expandierter Wert, damit
		// Druck- u. Volumenspur im Folgenden zusammenpassen)

		// Druckverlauf in Ausgabedatei schreiben
		i += 1; // Spalte
		for (int k = 0; k < pGesamt.length; k++) {
			Ergebnis.buffer_EinzelErgebnis("p_Gleichraumprozess [bar] ",
					pGesamt[k] / 1e5, i);
		}

		// Wirkungsgrad des eben berechneten Gleichraumprozesses
		double pmiGleichraum = LittleHelpers.berechnePmi(CP, pGesamt);
		double etaGleichraum = pmi2eta(pmiGleichraum);

		// Wirkungsgrad des idealen Gleichraumprozesses abhängig von epsilon
		// /////////////////////////////////////////////////////////////////////////////////////////////////////////
		double pmiIdeal = berechnePmiIdeal();
		double etaIdeal = berechneEtaIdeal();
		AusgabeSteurung
				.Warning("Verlustteilung FALL 1a: Idealer Gleichraumprozess");

		// if(CP.MODUL_VORGABEN.get("internesRestgasModell").equals("LWA")){
		// //START: LWA
		//
		// //LWA-Zeitachse
		// ErgebnisLWA = new ErgebnisBuffer(CP,"");
		// double x0_LW=CP.get_Auslassoeffnet(); //initial value of x in [s]
		// double xn_LW = CP.get_Einlassschluss()+CP.SYS.DAUER_ASP_SEC; //final
		// value of x in [s]
		// double schrittweite_LW = CP.SYS.WRITE_INTERVAL_SEC; //in [s]
		// int anzSimWerteLW=(int) ((xn_LW-x0_LW)/schrittweite_LW+1);
		// double timeLW;
		// int l;
		// for(int k=1;k<anzSimWerteLW;k++){
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		//
		// ErgebnisLWA.buffer_EinzelErgebnis("Kurbelwinkel-LW [°KW]",CP.convert_SEC2KW(timeLW),0);
		//
		// ErgebnisLWA.buffer_EinzelErgebnis("Brennraumvolumen-LW [m3]",motor.get_V(timeLW),1);
		//
		//
		//
		// }
		//
		// //der OT (kleinstes Volumen) wird berechnet
		// double Vmin=999999999;
		// double [] V= new double[anzSimWerteLW];
		// for(int k=1;k<anzSimWerteLW;k++){
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		// double VminTemp=motor.get_V(timeLW);
		// V[k-1]=VminTemp;
		// Vmin= Math.min(Vmin,VminTemp);
		//
		// }
		//
		//
		// //umrechnen in k-ten Wert
		//
		//
		// int cnt = sucheOT(Vmin);
		//
		//
		// l=2;
		//
		//
		// //ideale Ladungswechselverluste
		// IndizierDaten indiDgemittelt=new IndizierDaten(CP,true);
		// double [] pLWAideal=new double[anzSimWerteLW];
		//
		//
		// for (int k = 0; k < cnt; k++) {
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		// pLWAideal [k]= indiDgemittelt.get_pAus(timeLW);
		// }
		//
		// for (int k = cnt ; k < anzSimWerteLW; k++) {
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		// pLWAideal [k]= indiDgemittelt.get_pEin(timeLW);
		// }
		//
		//
		// for(int j=0; j<pLWAideal.length; j++){
		// ErgebnisLWA.buffer_EinzelErgebnis("LWAideal [bar]",pLWAideal[j]/1e5,l);
		// }
		// pmiLWAideal = LittleHelpers.berechnePmi (CP, pLWAideal );
		// etaLWAideal = pmi2etaLW(pmiMit,pmiLWAideal);
		// System.err.println("Verlustteilung: Ladungswechselverluste, ideal");
		//
		// // //reale gemittelte Ladungswechselverluste
		// //
		// // LadungsWechselAnalyse_ohneQb LWArealGem = new
		// LadungsWechselAnalyse_ohneQb(CP,true);
		// // VektorBuffer LWArealGemBuffer =
		// ladungswechselAnalyseDurchfuehren(LWArealGem);
		// // double [] pLWArealGem = LWArealGemBuffer.getValues();
		// //
		// // l+=1;
		// // for(int j=0; j<pLWArealGem.length; j++){
		// //
		// ErgebnisLWA.buffer_EinzelErgebnis("LWArealGemittelt [bar]",pLWArealGem[j]/1e5,l);
		// // }
		// // pmiLWArealGem = LittleHelpers.berechnePmi (CP, pLWArealGem );
		// // etaLWArealGem = pmi2etaLW(pmiMit,pmiLWArealGem);
		// //
		// System.err.println("Verlustteilung: Ladungswechselverluste, gemittelter Realverlauf");
		//
		//
		//
		// // reale Ladungswechselverluste
		// LadungsWechselAnalyse_ohneQb LWA = new
		// LadungsWechselAnalyse_ohneQb(CP);
		// VektorBuffer LWAbuffer = ladungswechselAnalyseDurchfuehren(LWA);
		// double [] pLWA = LWAbuffer.getValues();
		// l+=1;
		//
		// for(int j=0; j<pLWA.length; j++){
		// ErgebnisLWA.buffer_EinzelErgebnis("LWA [bar]",pLWA[j]*1e-5,l);
		// }
		// pmiLWA = LittleHelpers.berechnePmi (CP, pLWA );
		// etaLWA = pmi2etaLW(pmiMit,pmiLWA);
		// System.err.println("Verlustteilung: Ladungswechselverluste, LWA");
		//
		// } //ENDE: LWA
		//
		// else{ //OHNE LWA ei...fach aus dem Druckverlauf gelesen
		// //TODO: Hier pmi Ladungswechsel berechnen
		//
		//
		// //LWA-Zeitachse
		// ErgebnisLWA = new ErgebnisBuffer(CP,"");
		// double x0_LW=CP.get_Auslassoeffnet(); //initial value of x in [s]
		// double xn_LW = CP.get_Einlassschluss()+CP.SYS.DAUER_ASP_SEC; //final
		// value of x in [s]
		// double schrittweite_LW = CP.SYS.WRITE_INTERVAL_SEC; //in [s]
		// int anzSimWerteLW=(int) ((xn_LW-x0_LW)/schrittweite_LW+1);
		// double timeLW;
		// int l;
		// for(int k=1;k<anzSimWerteLW;k++){
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		//
		// ErgebnisLWA.buffer_EinzelErgebnis("Kurbelwinkel-LW [°KW]",CP.convert_SEC2KW(timeLW),0);
		//
		// ErgebnisLWA.buffer_EinzelErgebnis("Brennraumvolumen-LW [m3]",motor.get_V(timeLW),1);
		// }
		//
		//
		//
		// //der OT (kleinstes Volumen) wird berechnet
		// double Vmin=999999999;
		// double [] V= new double[anzSimWerteLW];
		// for(int k=1;k<anzSimWerteLW;k++){
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		// double VminTemp=motor.get_V(timeLW);
		// V[k-1]=VminTemp;
		// Vmin= Math.min(Vmin,VminTemp);
		//
		// }
		//
		//
		// //umrechnen in k-ten Wert
		//
		//
		// int cnt = sucheOT(Vmin);
		//
		//
		// l=2;
		//
		//
		// //ideale Ladungswechselverluste
		// IndizierDaten indiDgemittelt=new IndizierDaten(CP,true);
		// double [] pLWAideal=new double[anzSimWerteLW];
		//
		//
		// for (int k = 0; k < cnt; k++) {
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		// pLWAideal [k]= CP.get_p_Abgas();
		// }
		//
		// for (int k = cnt ; k < anzSimWerteLW; k++) {
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		// pLWAideal [k]= CP.get_p_LadeLuft();
		// }
		//
		//
		// for(int j=0; j<pLWAideal.length; j++){
		// ErgebnisLWA.buffer_EinzelErgebnis("LWAideal [bar]",pLWAideal[j]*1e-5,l);
		// }
		// pmiLWAideal = LittleHelpers.berechnePmi (CP, pLWAideal );
		// etaLWAideal = pmi2etaLW(pmiMit,pmiLWAideal);
		// System.err.println("Verlustteilung: Ladungswechselverluste, ideal");
		//
		// // //reale gemittelte Ladungswechselverluste
		// // l+=1;
		// // for(int j=0; j<pLWAideal.length; j++){
		// // //FAKE-Kanal für gemittelte LWA-Verluste
		// //
		// ErgebnisLWA.buffer_EinzelErgebnis("LWArealGemittelt [bar]",Double.NaN,l);
		// // }
		// // pmiLWArealGem = LittleHelpers.berechnePmi (CP, pLWAideal );
		// // etaLWArealGem = pmi2etaLW(pmiMit,pmiLWAideal);
		// //
		// System.err.println("Verlustteilung: Ladungswechselverluste, gemittelter Realverlauf");
		//
		// // reale Ladungswechselverluste
		// IndizierDaten indiD=new IndizierDaten(CP,true);
		// double [] pLWA=new double[anzSimWerteLW];
		// l+=1;
		//
		// for (int k = 0; k < anzSimWerteLW; k++) {
		// timeLW=x0_LW+k*CP.SYS.WRITE_INTERVAL_SEC;
		// pLWA [k]= indiD.get_pZyl(timeLW);
		// }
		//
		//
		// for(int j=0; j<pLWA.length; j++){
		// ErgebnisLWA.buffer_EinzelErgebnis("LWA [bar]",pLWA[j]*1e-5,l);
		// }
		// pmiLWA = LittleHelpers.berechnePmi (CP, pLWA );
		// etaLWA = pmi2etaLW(pmiMit,pmiLWA);
		// System.err.println("Verlustteilung: Ladungswechselverluste, Realverlauf");
		//
		// } //OHNE LWA

		// Reibverluste (nur wenn pme im Inputfile angegeben wurde)
		double pmeBar = CP.get_pme();
		double pme = pmeBar * 1e5; // Umrechnung in [Pa]
		double pmr = Double.NaN;
		double etaMech = Double.NaN;
		if (pme > 0) {
			pmr = pmiMit - pme;
			// etaMech = pme/pmiMit;
			etaMech = pmi2eta(pme);
		}
		// HC-CO-Eingabe getätigt?
		if (HCCOeingabe == false) {
			// System.out.println("HC- oder CO-Wert im Inputfile nicht angegeben!!!! --->> Keine Ausgabe bezüglich dieser Verluste möglich!");
			String message = "";
			ParameterFileWrongInputException e = new ParameterFileWrongInputException(
					message);
			e.log_Warning("HC- oder CO-Wert im Inputfile nicht angegeben!!!! --->> Keine Ausgabe bezüglich dieser Verluste möglich!");
		}

		// ////////////////
		// Ausgabebereich//
		// ////////////////

		// /////////Kerrom//////////////////////////////////
		// Ausgabe von pMaxGleichraum in eigene Datei
		String[] headerPmaxGleichraum = { "Kurbelwinkel [°KW]",
				"Zeit [s n. Rechenbeginn]", "Brennraumvolumen [m3]",
				"pMax Gleichraumprozess [bar]" };
		double[] pMaxGleichraumArray = {
				Math.round(CP.convert_SEC2KW(t_OT) * 10) / 10.0,
				t_anfangExpansion, motor.get_V(t_anfangExpansion),
				pMaxGleichraum / 1e5 };
		FileWriter_txt txtFilePmax = new FileWriter_txt(
				CP.get_workingDirectory() + CP.get_CaseName()
						+ "_Verlustteilung-pMaxGleichraum" + ".txt");
		txtFilePmax.writeTextLineToFile(headerPmaxGleichraum, false);
		txtFilePmax.writeLineToFile(pMaxGleichraumArray, true);

		// Ausgabe in Fenster
		AusgabeSteurung.Error("\n"
				+ "Verlustteilung_Post_Ergebnisse (s. MTZ 2/2005 S.120f):");
		AusgabeSteurung.Error("====================================");
		AusgabeSteurung.Warning("pMax_Gleichraumprozess (@Vmin)= "
				+ pMaxGleichraum / 1e5 + " [bar]\n");
		AusgabeSteurung.Warning("pmi_Idealprozess = " + pmiIdeal * 1e-5
				+ " [bar]\n" + "pmi_RealeLadung = " + pmiReal * 1e-5
				+ " [bar]\n" + "pmi_Verbrennungslage = "
				+ pmiVerbrennungslage * 1e-5 + " [bar]\n" + "pmi_HCCO = "
				+ pmiHCCO * 1e-5 + " [bar]\n" + "pmi_Brennverlauf = "
				+ pmiBrennverlauf * 1e-5 + " [bar]\n"
				+ "pmi_Wandwärmeverlust = " + pmiMit * 1e-5 + " [bar]\n"
				+ "pmi_LWAideal = " + pmiLWAideal * 1e-5 + " [bar]\n"
				+ "pmi_Realprozess = " + pmiOriginal * 1e-5 + " [bar]\n"
				+ "pmi_Reibung = " + pme * 1e-5 + " [bar]\n"
		// +"pmi_Gleichraumprozess = " + pmiGleichraum*1e-5 +" [bar]\n"
				);
		AusgabeSteurung.Warning("Wirkungsgrade:\n" + "eta_Idealprozess = "
				+ etaIdeal + " [-]\n" + "eta_RealeLadung = " + etaReal
				+ " [-]\n" + "eta_Verbrennungslage = " + etaVerbrennungslage
				+ " [-]\n" + "eta_HCCO = " + etaHCCO + " [-]\n"
				+ "eta_Brennverlauf = " + etaBrennverlauf + " [-]\n"
				+ "eta_Wandwärmeverlust = " + etaMit + " [-]\n"
				+ "eta_LWAideal = " + etaLWAideal + " [-]\n"
				+ "eta_Realprozess = " + etaOriginal + " [-]\n"
				+ "eta_Reibung = " + etaMech + " [-]\n"
		// +"eta_Gleichraumprozess =  " + etaGleichraum + " [-]"
				);
		// ///////////////////////////////////////////////////////////////////////////////////////

		// double[] pmi =
		// {Double.NaN,Double.NaN,pmiIdeal*1e-5,pmiReal*1e-5,pmiVerbrennungslage*1e-5,
		// pmiHCCO*1e-5,pmiBrennverlauf*1e-5,pmiMit*1e-5,pmiLWAideal*1e-5,pmiOriginal*1e-5,pmiLWArealGem*1e-5,pmiLWA*1e-5,pmr*1e-5};
		// String [] headerPmi =
		// {"pmi-Werte [bar]","----->>","Idealprozess","Reale Ladung","Verbrennungslage","HC-/CO-Emissionen","Realer Brennverlauf","Wandwärmeverlust","LWAideal","LWA_Original","LWArealGemittelt","LWA real","Reibmitteldruck"};
		double[] pmi = { Double.NaN, Double.NaN, pmiIdeal * 1e-5,
				pmiReal * 1e-5, pmiVerbrennungslage * 1e-5, pmiHCCO * 1e-5,
				pmiBrennverlauf * 1e-5, pmiMit * 1e-5, pmiLWAideal * 1e-5,
				pmiOriginal * 1e-5, pme * 1e-5, pmiGleichraum * 1e-5 };
		String[] headerPmi = { "pmi-Werte [bar]", "----->>", "Idealprozess",
				"Reale Ladung", "Verbrennungslage", "HC-/CO-Emissionen",
				"Realer Brennverlauf", "Wandwärmeverlust", "LWAideal",
				"LWA_Original", "Reibung", "Gleichraumprozess" };

		// double[] eta =
		// {Double.NaN,Double.NaN,etaIdeal,etaReal,etaVerbrennungslage,etaHCCO,etaBrennverlauf,etaMit,etaLWAideal,etaLWArealGem,etaLWA,etaMech};
		// String [] headerEta =
		// {"Wirkungsgrade [%]","----->>","Idealprozess","Reale Ladung","Verbrennungslage","HC-/CO-Emissionen","Realer Brennverlauf","Wandwärmeverlust","LWAideal","LWArealGemittelt","LWA real","Mechanischer Wirkungsgrad"};
		double[] eta = { Double.NaN, Double.NaN, etaIdeal, etaReal,
				etaVerbrennungslage, etaHCCO, etaBrennverlauf, etaMit,
				etaOriginal, etaLWAideal, etaMech, etaGleichraum };
		String[] headerEta = { "Wirkungsgrade [%]", "----->>", "Idealprozess",
				"Reale Ladung", "Verbrennungslage", "HC-/CO-Emissionen",
				"Realer Brennverlauf", "Wandwärmeverlust", "LWAideal",
				"LWA_Original", "Reibung", "Gleichraumprozess" };

		// String name="Verlustteilung-Verläufe_"+CP.get_CaseName()+".txt";
		Ergebnis.schreibeErgebnisFile(CP.get_CaseName()
				+ "_Verlustteilung-APR-Verlauf" + ".txt");

		// if(CP.MODUL_VORGABEN.get("internesRestgasModell").equals("LWA")){
		// //START: LWA
		// ErgebnisLWA.schreibeErgebnisFile("Verlustteilung-LWA-Verlauf_"+CP.get_CaseName()+".txt");
		// }// ENDE: LWA

		FileWriter_txt txtFile = new FileWriter_txt(CP.get_workingDirectory()
				+ CP.get_CaseName() + "_Verlustteilung-Wirkungsgrade" + ".txt");
		txtFile.writeTextLineToFile(headerPmi, false);
		txtFile.writeLineToFile(pmi, true);
		txtFile.writeTextLineToFile(headerEta, true);
		txtFile.writeLineToFile(eta, true);

		// APR_homogen_EinZonig mitWandwaerme = new APR_homogen_EinZonig(CP,
		// true);
		// VektorBuffer mitWandwaermeverlust =
		// berechnungDurchfuehren(mitWandwaerme);
		// double [] pMit = mitWandwaermeverlust.getValues();
		// double pmiMit = LittleHelpers.berechnePmi (CP, pMit );

		// System.out.println(pmiOhne);
		// System.out.println(pmiMit);
		/*
		 * FileWriter_txt txtFile = new FileWriter_txt(
		 * "F://Workspace//Bremo//src//InputFiles//Verlustteilung.txt"); String
		 * [] header = {"pOhne", "pMit","pmiOhne", "pmiMit"}; double[] pmi =
		 * {pmiOhne,pmiMit}; double [][] matrix=new double [3][];
		 * 
		 * matrix[0]=pOhne; matrix[1]=pMit; matrix[2]=pmi;
		 * 
		 * txtFile.writeTextLineToFile(header, false); //
		 * txtFile.writeLineToFile (pmi, true);
		 * txtFile.writeMatrixToFile(matrix, true);
		 */

	}

	public VektorBuffer berechnungDurchfuehren(BerechnungsModell dglSys) {
		try {
			double x0, xn, schrittweite;
			// Solver sol=new Solver(CP, dglSys);
			Solver sol = CP.SOLVER;
			sol.set_BerechnungsModell(dglSys); // sonst wird nicht mit dglSys
												// gerechnet
			// initial value of x
			x0 = CP.SYS.RECHNUNGS_BEGINN_DVA_SEC; // sollte normalerweise null
													// sein aber man weiss ja
													// nie
			sol.setInitialValueOfX(x0);

			// final value of x
			// xn = CP.SYS.RECHNUNGS_ENDE_DVA_SEC; //in [s] //ORIGINAL
			xn = CP.convert_KW2SEC(180); // in [s]
			sol.setFinalValueOfX(xn);

			schrittweite = CP.SYS.WRITE_INTERVAL_SEC; // in [s]
			sol.setStepSize(schrittweite);

			// int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE; //ORIGINAL
			int anzSimWerte = (int) ((xn - x0) / CP.SYS.WRITE_INTERVAL_SEC);
			double time;
			Zone[] zn = dglSys.get_initialZones();
			dglSys.bufferErgebnisse(x0, zn);

			Zone[] znTemp = null;

			// for(int i=1;i<anzSimWerte-1;i++){ //Auch hier (wie bei APR)
			// angepasster simWert, um einen Interpolationsfehler zu vermeiden
			for (int i = 1; i < anzSimWerte + 1; i++) { // geänderte
														// Simulationslänge bis
														// 180°KWnZOT

				time = x0 + i * CP.SYS.WRITE_INTERVAL_SEC;

				System.out.println("berechne Zeitschritt: "
						+ Math.round(10 * CP.convert_SEC2KW(time)) / 10.0
						+ "[KW]");

				sol.setFinalValueOfX(time);

				znTemp = sol.solveRK(zn);

				CP.set_aktuelle_Rechenzeit(time);

				zn = znTemp;

				znTemp = null;

				dglSys.bufferErgebnisse(time, zn);
				sol.setInitialValueOfX(time);

				if (dglSys.initialiseBurntZone()) {
					zn = dglSys.get_initialZonesWhileRunning();
					// Damit die Anfangsbedingungen der Zonen im Ergebnisfile
					// erscheinen
					dglSys.bufferErgebnisse(time, zn);
				}

			}
		} catch (NullPointerException e) {
			try {
				throw new CaseParaNotInstantiatedException();
			} catch (CaseParaNotInstantiatedException p) {
				AusgabeSteurung.Error("Rechnung von USER gestopt !!!");
				p.stopBremo();
			}
		}

		// dglSys.schreibeErgebnisFile(name +".txt");

		return dglSys.get_p_buffer();

	}

	public double pmi2eta(double pmi) {
		double eta;
		eta = pmi
				/ (masterEinspritzung.get_mKrst_Sum_ASP()
						* masterEinspritzung.get_spezKrstALL().get_Hu_mass() / ((Motor_HubKolbenMotor) motor)
							.get_Hubvolumen());
		if (eta < 0)
			return Double.NaN;
		else
			return eta;
	}

	public double pmi2etaLW(double pmiHD, double pmiLW) {
		double etaLW;
		etaLW = ((pmiHD + pmiLW) * ((Motor_HubKolbenMotor) motor)
				.get_Hubvolumen())
				/ (masterEinspritzung.get_mKrst_Sum_ASP() * masterEinspritzung
						.get_spezKrstALL().get_Hu_mass());

		return etaLW;
	}

	private VektorBuffer ladungswechselAnalyseDurchfuehren(
			BerechnungsModell dglSys_LW) {

		double x0_LW, xn_LW, schrittweite_LW;
		double time;
		double mLuftFeucht;
		double mLuft_tr = CP.get_mLuft_trocken_ASP(); // trockene Luftmasse pro
														// ASP
		double mW = CP.get_mWasser_Luft_ASP(); // Wassermasse pro Arbeitsspiel
		double mLuftFeucht_mess = mLuft_tr + mW;

		Zone[] zn_LW;

		// dglSys_LW=new LadungsWechselAnalyse(CP);
		// SolverSolver LW_SOL= new Solver(CP,dglSys_LW);
		Solver LW_SOL = CP.SOLVER;
		LW_SOL.set_BerechnungsModell(dglSys_LW); // sonst wird nicht mit
													// dglSys_LW gerechnet
		x0_LW = CP.get_Auslassoeffnet(); // initial value of x in [s]
		LW_SOL.setInitialValueOfX(x0_LW);
		xn_LW = CP.get_Einlassschluss() + CP.SYS.DAUER_ASP_SEC; // final value
																// of x in [s]
		LW_SOL.setFinalValueOfX(xn_LW);
		schrittweite_LW = CP.SYS.WRITE_INTERVAL_SEC; // in [s]
		LW_SOL.setStepSize(schrittweite_LW);

		// While --> LW-Analyse wird wiederholt, bis die Masse im Zylinder
		// konvergiert hat
		// Schleife über °KW (Auslassöffnet bis Einlassschluss)
		int anzSimWerte = (int) ((xn_LW - x0_LW) / schrittweite_LW + 1);
		Zone[] znTemp = null;
		zn_LW = dglSys_LW.get_initialZones();

		dglSys_LW.bufferErgebnisse(x0_LW, zn_LW);
		double m_neu = 0;
		double m_alt = zn_LW[0].get_m();

		double pInit = zn_LW[0].get_p();
		double VInit = zn_LW[0].get_V();
		Spezies abgas = zn_LW[0].get_ggZone();// CP.get_spezAbgas();
		int idx2 = 0;
		// if(real==true){
		do {// Iterationsschleife um auf den selben Massenstrom zu kommen wie am
			// PS gemessen --> Variation der Ladelufttemperatur
			double f_mInit = 0.1;
			int idx = 1;
			while (f_mInit > 0.0005 && idx <= 50) {
				dglSys_LW.clearErgebnisBuffer();
				double TInit = pInit * VInit / (m_alt * abgas.get_R());
				// Zone wird jedes Mal neu erzeugt. Ihre Masse ist gleich die
				// Masse, die beim Ende der letzen
				// Iteration (Einlassschluss) im Zylinder war.
				zn_LW[0] = new Zone(CP, pInit, VInit, TInit, m_alt, abgas,
						false, 0);
				for (int i = 1; i < anzSimWerte - 1; i++) {
					time = x0_LW + i * CP.SYS.WRITE_INTERVAL_SEC;
					LW_SOL.setFinalValueOfX(time);
					znTemp = LW_SOL.solveRK(zn_LW);
					CP.set_aktuelle_Rechenzeit(time);
					zn_LW = znTemp;
					znTemp = null;
					dglSys_LW.bufferErgebnisse(time, zn_LW);
					LW_SOL.setInitialValueOfX(time);
					if (CP.SYS.DUBUGGING_MODE) {
						if (Math.abs(time - CP.SYS.DUBUGGING_TIME_SEC) < 0.5 * CP.SYS.WRITE_INTERVAL_SEC) { // Rechnet
																											// bis
																											// KW
																											// und
																											// schreibt
																											// dann
																											// alle
																											// Werte
																											// ins
																											// txt-file
							// dglSys_LW.schreibeErgebnisFile("DEBUG_"+CP.get_CaseName()+".txt");
							dglSys_LW.schreibeErgebnisFile(CP.get_CaseName()
									+ "_Verlustteilung.dbl"); // Früher DEBUG_
							System.out.println("I am plotting...");
						}
					}
				}

				m_neu = zn_LW[0].get_m();

				f_mInit = Math.abs(m_neu - m_alt) / (m_alt);
				System.out.println("Relative Abweichung der Gesamtmasse: "
						+ f_mInit);

				m_alt = m_neu;
				idx++;
			}

			System.out.println("Masse bei Iteration " + idx2 + " = " + m_neu
					+ " kg");
			mLuftFeucht = ((LadungsWechselAnalyse_ohneQb) dglSys_LW)
					.get_mLuftFeucht(zn_LW);
			// Anpassung der Ladelufttemperatur um auf die gemessenen Luftmasse
			// zu kommen
			((LadungsWechselAnalyse_ohneQb) dglSys_LW).set_TSaug(mLuftFeucht);
			idx2 += 1;
		} while (Math.abs((mLuftFeucht_mess - mLuftFeucht) / mLuftFeucht_mess) > 0.005
				&& idx2 <= 50);
		// "_" + (idx-1) +
		// }
		/*
		 * else if(real==false){ dglSys_LW.clearErgebnisBuffer(); double TInit
		 * =pInit*VInit/(m_alt*abgas.get_R()); //Zone wird jedes Mal neu
		 * erzeugt. Ihre Masse ist gleich die Masse, die beim Ende der letzen
		 * //Iteration (Einlassschluss) im Zylinder war. zn_LW[0]=new
		 * Zone(CP,pInit, VInit,TInit, m_alt, abgas, false, 0); for(int
		 * i=1;i<anzSimWerte-1;i++){ time=x0_LW+i*CP.SYS.WRITE_INTERVAL_SEC;
		 * LW_SOL.setFinalValueOfX(time); znTemp=LW_SOL.solveRK(zn_LW);
		 * CP.set_aktuelle_Rechenzeit(time); zn_LW=znTemp; znTemp=null;
		 * dglSys_LW.bufferErgebnisse(time,zn_LW);
		 * LW_SOL.setInitialValueOfX(time); } }
		 */return dglSys_LW.get_p_buffer();

	}

	private double findeVerbrennungsschwerpunkt(VektorBuffer dQbuffer) {
		double[] t = dQbuffer.getZeitachse();
		double dt = t[1] - t[0];
		double[] mb = inti.get_IntegralVerlauf(dt, dQbuffer.getValues());
		double[] mb_norm = MatLibBase.normierVec(mb);

		double ums = 0;
		int i = 0;
		boolean found = false;
		// 50%-Umsatzpunkt

		do {
			if (mb_norm[i] >= 0.5) {
				found = true;

				if (CP.SYS.IS_KW_BASIERT)
					ums = CP.convert_SEC2KW(t[i]);
				else
					ums = t[i];
			}
			i += 1;
		} while (found == false && i < mb_norm.length);
		i = i - 1; // vermeidet IndexOutOfBoundsException

		return ums;
	}

	private int sucheOT(double Vmin) {
		double x0_LW = CP.get_Auslassoeffnet(); // initial value of x in [s]
		double xn_LW = CP.get_Einlassschluss() + CP.SYS.DAUER_ASP_SEC; // final
																		// value
																		// of x
																		// in
																		// [s]
		double schrittweite_LW = CP.SYS.WRITE_INTERVAL_SEC; // in [s]
		int anzSimWerteLW = (int) ((xn_LW - x0_LW) / schrittweite_LW + 1);
		double timeLW;
		double VTemp;
		for (int k = 1; k < anzSimWerteLW; k++) {
			timeLW = x0_LW + k * CP.SYS.WRITE_INTERVAL_SEC;
			VTemp = motor.get_V(timeLW);

			if (VTemp == Vmin) {
				return k;
			}
		}
		return -1;
	}

	// ///////////////////////Kerrom///////////////////////////////////////
	/**
	 * @param pGesamt
	 *            Druckarray
	 * @param anfang
	 *            Ort im Druckarray (Anfangszustand)
	 * @param ende
	 *            Ort im Druckarray (Endzustand)
	 * @param t_anfang
	 *            Zeit (Anfangszustand) [s]
	 * @param v_anfang
	 *            Volumen (Anfangszustand) [m^3]
	 * @param p_anfang
	 *            Druck (Anfangszustand) [Pa] berechnet werden die Werte von
	 *            "anfang+1" bis einschließlich "ende" }
	 */
	private void berechnePolytropeZustandsaenderung(double[] pGesamt,
			int anfang, int ende, double t_anfang, double v_anfang,
			double p_anfang) {

		for (int i = anfang; i < ende; i++) {
			double zeitpunkt = t_anfang + (i - anfang)
					* CP.SYS.WRITE_INTERVAL_SEC;
			double volumen = motor.get_V(zeitpunkt);
			pGesamt[i + 1] = p_anfang * Math.pow(v_anfang / volumen, kappa);
		}
	}

	/**
	 * idealer Gleichraumprozess:
	 * 
	 * @param R
	 *            Gaskonstante [J/(kgK)]
	 * @param qzu
	 *            (Qzu) Wärmezufuhr [J]
	 * @param cv
	 *            isochore Wärmekapazität [J/(kgK)]
	 * @param pAnfang
	 *            Druck Anfangszustand [Pa]
	 * @param vAnfang
	 *            Volumen Anfangszustand [m^3]
	 * @return Druck nach Wärmezufuhr [Pa]
	 */
	private double berechneDruckNachWaermezufuhr(double R, double qzu,
			double cv, double pAnfang, double vAnfang) {
		return qzu * R / (cv * vAnfang) + pAnfang;
	}

	// /////////////////////////////////////////////////////////////////////////////////

	public void destroy() {
		CP = null;
	}
}