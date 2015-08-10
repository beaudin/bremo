package bremo.sys;

import io.AusgabeSteurung;
import matLib.VectorTools;
import berechnungsModule.PostProcessor;
import berechnungsModule.Berechnung.APR;
import berechnungsModule.Berechnung.BerechnungsModell;
import berechnungsModule.Berechnung.DVA;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;
import bremoExceptions.CaseParaNotInstantiatedException;

public class Rechnung {

	private CasePara CP;
	private double turbulence = 0; // Turbulenzfaktor unten (falls Bargende)
									// negativ initialisiert, muss aber immer
									// positiv sein. Falls nicht ver�ndert wird
									// Fehler abgefangen
	private boolean bargende = false;
	private boolean fvv = false;
	private final double[] xVc, yVc;
	private int index_Vc = 0;

	public Rechnung(CasePara cp) {
		CP = cp;
		double n = (cp.convert_KW2SEC(-30) - cp.convert_KW2SEC(-40))
				/ cp.SYS.WRITE_INTERVAL_SEC;
		int s = (int) Math.round(n);
		xVc = new double[s];
		yVc = new double[s];
		if (CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende")
				|| CP.MODUL_VORGABEN.get("Wandwaermemodell").equals(
						"BargendeHeinle")) { // vor der do-while-Schleife, sonst
												// w�rde bei jedem Schritt
												// "equals("Bargende")" im
												// CP-File abgefragt werden
			bargende = true;
		}
		if (CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("BargendeFVV")) { // f�r
																				// Bargende
																				// nach
																				// FVV
			fvv = true;
		}
	}

	public void berechnungDurchfuehren() {
		try {
			double schrittweite;
			// BerechnungsModell dglSys=CP.BERECHNUNGS_MODELL;
			// Solver sol=new Solver(CP, dglSys);
			BerechnungsModell dglSys = CP.BERECHNUNGS_MODELL;
			Solver sol = CP.SOLVER;
			// initial value of x
			double x0 = CP.SYS.RECHNUNGS_BEGINN_DVA_SEC; // sollte normalerweise
															// null sein aber
															// man
															// weiss ja nie
			sol.setInitialValueOfX(x0);

			// final value of x
			double xn = CP.SYS.RECHNUNGS_ENDE_DVA_SEC; // in [s]
			sol.setFinalValueOfX(xn);

			schrittweite = CP.SYS.WRITE_INTERVAL_SEC; // in [s]
			sol.setStepSize(schrittweite);

			int anzGesamtIterationen = 0;

			// Anzahl simWerte muss f�r eine APR (minus 1) angepasst werden um
			// einen
			// Interpolationsfehler zu vermeiden
			int anzSimWerte; // fuer Verlustteilung Frank Haertel
			if (dglSys.isDVA() == true) { // fuer Verlustteilung Frank Haertel

				anzSimWerte = CP.SYS.ANZ_BERECHNETER_WERTE;
				// int anzSimWerte=CP.SYS.ANZ_BERECHNETER_WERTE;
			} else
				// fuer Verlustteilung Frank Haertel
				anzSimWerte = CP.SYS.ANZ_BERECHNETER_WERTE - 1; // fuer
																// Verlustteilung
																// Frank Haertel

			double time;
			Zone[] zn = dglSys.get_initialZones();
			dglSys.bufferErgebnisse(x0, zn);
			Zone[] znTemp = null;

			for (int i = 1; i < anzSimWerte; i++) {

				time = x0 + i * CP.SYS.WRITE_INTERVAL_SEC;

				if (CP.SYS.IS_KW_BASIERT)
					// System.out.println("berechne Zeitschritt: "
					// +CP.convert_SEC2KW(time)+ "[KW]"); //ORIGINAL
					System.out.println("berechne Zeitschritt: "
							+ Math.round(10 * CP.convert_SEC2KW(time)) / 10.0
							+ "[KW]");
				else
					System.out.println("berechne Zeitschritt: " + time
							+ "[sec]");
				sol.setFinalValueOfX(time);

				if (dglSys.isDVA() == true) {

					double pIst = Double.NaN;
					boolean isConverged = false;
					int idx = 0;

					if (bargende || fvv) {
						turbulence = -1;
					}

					// Erste Rechnung vor der do-while Schleife
					znTemp = sol.solveRK(zn);
					pIst = znTemp[0].get_p();
					idx++;
					// Nach der ersten Rechnung wird der Turbulenzfaktor mit
					// turbulence festgehalten, sonst wird er mit jedem weiteren
					// Rechenschritt hoch gerechnet
					// Sp�ter soll turbulence den entsprechenden Wert
					// �berschreiben
					// (bisher nur f�r WW� Bargende n�tig)
					// if
					// (CP.MODUL_VORGABEN.get("Wandwaermemodell").equals("Bargende")){
					// //vor der do-while-Schleife, sonst w�rde bei jedem
					// Schritt
					// "equals("Bargende")" im CP-File abgefragt werden
					if (bargende || fvv) {
						turbulence = CP.TURB_FACTORY.get_TurbulenceModel()
								.get_k(zn, time);
					}

					// Vergleich ob der Berechnete Druck mit dem gemessenen
					// �bereinstimmt
					// wenn nicht muss set_dQ aufgerufen werden und die Rechnung
					// erneut durchgef�hrt werden
					isConverged = ((DVA) dglSys).is_pSoll_Gleich_pIst(pIst, zn,
							time);

					do {
						znTemp = sol.solveRK(zn);
						pIst = znTemp[0].get_p();
						isConverged = ((DVA) dglSys).is_pSoll_Gleich_pIst(pIst,
								zn, time);
						idx++;

						// �berschreiben von Turbulenzfaktor k (siehe oben)
						// turbulence ist mit -1 initialisiert
						if (turbulence >= 0) {
							// if (turbulence >= -1E-12) {
							CP.TURB_FACTORY.get_TurbulenceModel().set_k(
									turbulence, 0);
							// Falls set_k nicht funktioniert oder Schmarrn
							// zur�ck
							// gegeben wird
						} else {
							AusgabeSteurung
									.Error("Vorsicht, negative Turbulenz aufgetreten - sollte nicht passieren!");
						}
					} while (isConverged == false && idx < 100);

					if (isConverged == false) {
						AusgabeSteurung
								.Error("mangelnde Konvergenz im Zeitschritt: "
										+ CP.convert_SEC2KW(time) + "[KW]");
						((DVA) dglSys).schreibe_DUBUGGING_Ergebnisse("NO_KON_"
								+ CP.get_CaseName() + "_"
								+ CP.convert_SEC2KW(time) + ".txt");
					} else
						System.out.println("DVA CON: " + (idx - 1));

					// Thermodynamisches Verdichtungsverh�ltnis nach der
					// Kompressionsmethode, beschrieben in:
					// "Christine Burkhardt (2006) - Eine praktische Methode zur Bestimmung des realen Verdichtungsverh�ltnisses"
					// aus Klopfregelung f�r Ottomotoren II, Band 74, Kapitel 8
					if (time >= CP.convert_KW2SEC(-40)
							&& time < CP.convert_KW2SEC(-30)
							&& CP.MOTOR.isHubKolbenMotor()
							&& CP.BERECHNUNGS_MODELL.isDVA()
							&& index_Vc < xVc.length) {
						double[] coeff = ((DVA) dglSys).kompressionsVolumen();
						xVc[index_Vc] = coeff[0];
						yVc[index_Vc] = coeff[1];
						index_Vc++;
						if (index_Vc == xVc.length) {
							coeff = VectorTools.lineareRegression(xVc, yVc);
							((Motor_HubKolbenMotor) CP.MOTOR)
									.set_Epsilon_thermo(coeff[1]);
						}
					}

					anzGesamtIterationen += idx - 1;

				} else {
					zn = ((APR) dglSys).calc_dQburn(zn);
					znTemp = sol.solveRK(zn);
				}

				zn = znTemp;

				znTemp = null;

				dglSys.bufferErgebnisse(time, zn);
				sol.setInitialValueOfX(time);
				CP.set_aktuelle_Rechenzeit(time);

				if (dglSys.initialiseBurntZone()) {
					zn = dglSys.get_initialZonesWhileRunning();
					// Damit die Anfangsbedingungen der Zonen im Ergebnisfile
					// erscheinen
					// dglSys.bufferErgebnisse(time,zn);
				}

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
						CP.schreibeAlleErgebnisFiles("DEBUG_"
								+ CP.get_CaseName() + ".txt");
						System.out.println("I am plotting...");
					}
				}
			}
			if (CP.SYS.DUBUGGING_MODE)
				CP.schreibeAlleErgebnisFiles(CP.get_CaseName() + ".txt");
			else
				dglSys.schreibeErgebnisFile(CP.get_CaseName() + ".txt");

			if (CP.BERECHNUNGS_MODELL.isDVA()
					&& !CP.BERECHNUNGS_MODELL.toString().contains("Weltformel")
					&& !CP.BERECHNUNGS_MODELL.toString().contains(
							"ThermodynVerdichtung")) { // Nur wenn DVA und nicht
														// Weltformel
				PostProcessor pp = new PostProcessor(dglSys.get_dm_buffer(), // war
																				// bei
																				// Juwe
																				// auskommentiert
						dglSys.get_dQb_buffer(), dglSys.get_dQw_buffer(), CP); // war
																				// bei
																				// Juwe
																				// auskommentiert
				pp.schreibeErgebnisFile(CP.get_CaseName() + ".txt"); // war bei
																		// Juwe
																		// auskommentiert
				// F�r Thermodynamisches Verdichtungsverh�ltnis:
			} else if (CP.BERECHNUNGS_MODELL.isDVA()
					&& CP.BERECHNUNGS_MODELL.toString().contains(
							"ThermodynVerdichtung")) {
				PostProcessor pp = new PostProcessor(dglSys.get_dQb_buffer(),
						dglSys.get_dQw_buffer(), CP);
				pp.schreibeErgebnisFile(CP.get_CaseName() + ".txt");
			} else { // Wenn APR eigener Konstruktor in PostProcessor mit
						// p_buffer
				PostProcessor pp = new PostProcessor(
						dglSys.get_dm_buffer(), // war
												// bei
												// Juwe
												// auskommentiert
						dglSys.get_dQb_buffer(), dglSys.get_dQw_buffer(),
						dglSys.get_p_buffer(), CP); // war bei Juwe
													// auskommentiert
				pp.schreibeErgebnisFile(CP.get_CaseName() + ".txt");
			}
			// CP.CANTERA_CALLER.releaseCantera();
			// ((APR_Cantera)dglSys).releaseCantera();
			System.out.println("Gesamtanzahl der benoetigeten Iterationen: "
					+ anzGesamtIterationen);

		} catch (NullPointerException e) {
			try {
				throw new CaseParaNotInstantiatedException();
			} catch (CaseParaNotInstantiatedException p) {
				AusgabeSteurung.Error("Rechnung von USER gestopt !!!");
				p.stopBremo();
			}
		}
	}

	/**
	 * Destroy CasePara to Stop the calcul
	 */
	public void destroy() {
		CP = null;
	}

}
