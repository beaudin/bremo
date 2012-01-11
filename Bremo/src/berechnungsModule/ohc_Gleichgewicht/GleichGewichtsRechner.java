package berechnungsModule.ohc_Gleichgewicht;


import java.util.Hashtable;

import berechnungsModule.ModulPrototyp;
import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import kalorik.spezies.KoeffizientenSpeziesFabrik;
import kalorik.spezies.Spezies;
// TODO Beruecksichtigung von Ar --> zurzeit verschwindet Ar bei der Verbrennung spurlos
public abstract class GleichGewichtsRechner extends ModulPrototyp {
	
	public static final String OHC_SOLVER_FLAG="OHC_Solver";
	public final static String[] OHC_SOLVER={"OlikaraBorman" , "Grill"};	
	
	Spezies spezH;
	Spezies spezO;
	Spezies spezN;
	Spezies spezH2;
	Spezies spezOH;
	Spezies spezCO;
	Spezies spezNO;
	Spezies spezO2;
	Spezies spezH2O;
	Spezies spezCO2;
	Spezies spezN2;
	Spezies spezAr;
	protected final double T_FREEZE; 
	private final String GLGW_KONST;	
	private static GleichGewichtsRechner ohcSolver=null;

	protected GleichGewichtsRechner(String gleichGewichtsKonstantenVorgabe, double T_freeze) {	
		
		T_FREEZE=T_freeze; //Vorgabe der Einfriertemperatur
		GLGW_KONST=gleichGewichtsKonstantenVorgabe; //Vorgabe der Gleichgewichtskonstanten
		
		spezH=KoeffizientenSpeziesFabrik.get_spezH();	
		spezO=KoeffizientenSpeziesFabrik.get_spezO();
		spezN=KoeffizientenSpeziesFabrik.get_spezN();
		spezH2=KoeffizientenSpeziesFabrik.get_spezH2();
		spezOH=KoeffizientenSpeziesFabrik.get_spezOH();
		spezCO=KoeffizientenSpeziesFabrik.get_spezCO();
		spezNO=KoeffizientenSpeziesFabrik.get_spezNO();
		spezO2=KoeffizientenSpeziesFabrik.get_spezO2();
		spezH2O=KoeffizientenSpeziesFabrik.get_spezH2O();
		spezCO2=KoeffizientenSpeziesFabrik.get_spezCO2();
		spezN2=KoeffizientenSpeziesFabrik.get_spezN2();		
		spezAr=KoeffizientenSpeziesFabrik.get_spezAr();	
	}	

	/**
	 * Liefert ein GleichGewichtsRechner Objekt zur Berechnung des ohc-Gleichgewichts 
	 * entsprechend der Auswahl im InputFile zurueck. 
	 * Die variable cp vom Typ CasePara wird benötigt um aus dem Eingabefile den gewuenschten 
	 * GleichGewichtsRechner auszulesen.
	 * @param CaseParameter cp
	 * @return GleichGewichtsRechner
	 */
	public static GleichGewichtsRechner get_Instance(CasePara cp) {
		
		if(ohcSolver==null){

			String ohcSolverVorgabe=get_ModulWahl(OHC_SOLVER_FLAG, OHC_SOLVER,cp);
	
			if(ohcSolverVorgabe.equals(OHC_SOLVER[0]))	//"OlikaraBorman"
				ohcSolver=
					new GleichGewichtsRechner_Olikara_Borman(cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
			else if(ohcSolverVorgabe.equals(OHC_SOLVER[1])) // "Grill"
				ohcSolver=
					new GleichGewichtsRechner_Grill(cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
	
			else if (ohcSolver==null){
				try {
					throw new BirdBrainedProgrammerException(
							"Der ausgewaehlte OHC_Solver \"" +ohcSolverVorgabe + " \" wurde im InputFile " +
							"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. " +
							"Es fehlt der entsprechende else-if-Block  oder er wurde noch nicht implementiert\n" +
							"Verwendet wird Grill");
				} catch (BirdBrainedProgrammerException e) {
					e.log_Message();
					ohcSolver=
						new GleichGewichtsRechner_Grill(cp.SYS.GLEICHGEWICHTSKONSTANTEN_VORGABE,cp.SYS.T_FREEZE);
				}
			}
		}
		return ohcSolver;		
	}	
	
	/**
	 * Ruft die gleichnamige Methode mit dem Eingabeparameter cp vom Typ CasePara auf und 
	 * Liefert ein GleichGewichtsRechner Objekt zur Berechnung des ohc-Gleichgewichts 
	 * entsprechend der Auswahl im InputFile zurueck. 
	 * Die variable cp vom Typ CasePara wird benötigt um aus dem Eingabefile den gewuenschten 
	 * GleichGewichtsRechner auszulesen.
	 * Die variable cp vom Typ CasePara wird mittels einer statischen Methode aus Bremo.java ausgecheckt.	 
	 * @return GleichGewichtsRechner
	 */
	public static GleichGewichtsRechner get_Instance(){
		return get_Instance(Bremo.get_casePara());
	}
	
	
	/**
	 * Berechnet die Zusammensetzung von dissozierendem Abgas unter der Berücksichtigung von TROCKENER Luft!!!
	 * @param p Verbrennungsdruck
	 * @param T	Verbrennungstemperatur
	 * @param lambda Verbrennungsluftverhältnis
	 * @param cKrst C-Atome im Kraftstoff
	 * @param hKrst	H-Atome im Kraftstoff
	 * @param oKrst	O-Atome im Kraftstoff
	 * @param nKrst	N-Atome im Kraftstoff
	 * @return liefert eine Hashtable mit den Spezies und den zugehörigen Molenbrüchen 
	 */
	public abstract  Hashtable<Spezies, Double> get_GG_molenBrueche(double p, double T,
			double lambda, double cKrst, double hKrst, double oKrst, double nKrst);
	
	
	
	/**
	 * Berechnet die Zusammensetzung von dissozierendem Abgas mit den Atomzahlen 
	 * aus dem Uebergegenem Speziesobjekt.
	 * Das Wasser in feuchter Luft wird hierbei berücksichtigt
	 * @param p Verbrennungsdruck
	 * @param T	Verbrennungstemperatur
	 * @param Spezies frischGemisch --> Obkjekt vom Typ GasGemisch als Mischung aus Verbrennungsluft und Krafstoff
	 * @return liefert eine Hashtable mit den Spezies und den zugehörigen Molenbrüchen 	 * 
	 */
	public abstract  Hashtable<Spezies, Double> get_GG_molenBrueche(double p, double T,
			Spezies frischGemisch);
		
	
	public double get_T_Freeze(){
		return T_FREEZE;
	}

	private double calc_Kp_7(double T, double a1, double a2, double a3,
			double a4, double a5, double a6, double a7) {
		double T2 = T * T;
		double T3 = T2 * T;
		double T4 = T3 * T;
		double Kp = Math.exp(a1 * (Math.log(T) - 1) + a2 * T / 2 + a3 * T2 / 6
				+ a4 * T3 / 12 + a5 * T4 / 20 - a6 / T + a7);
		return Kp;
	}

	
	
	
	// Berechnung der Gleichgewichtskonstanten
	public double[] get_Kp(double T) {
		double[] Kp = new double[8];
		Kp[0] = -555.55; // wird nicht belegt damit der Aufruf bei 1 beginnen
							// kann!
		
		
		if (GLGW_KONST.equalsIgnoreCase("Janaf")) {
			// Berechnung der Gleichgewichtskonstanten mit Polynomen nach den
			// Janaf-Daten
			//TODO GLGW-Konst Janaf BITTE IMPLEMENTIER MICH
			
			try{
				throw new BirdBrainedProgrammerException("Die Janaf-Gleichgewichtskonstanten " +
						"wurden noch nicht implementiert");
			}catch(BirdBrainedProgrammerException e){
				e.stopBremo();
			}
			
		}else if (GLGW_KONST.equalsIgnoreCase("Olikara")) {

			// Berechnung der Gleichgewichtskonstanten mit Polynomen nach
			// Olikara und Bormann
			// --> ACHTUNG: Ausgabe erfolgt für Partialdrücke in Atmosphären
			// --> Details siehe Diss. Gorenflo

			double dTKp = T * .001;
			double dTKp2 = dTKp * dTKp;
			double dLnTKp = Math.log(dTKp);

			// CO2-->CO +1/2 O2
			Kp[1] = 1 / (Math.pow(10, (-0.00415302 * dLnTKp + 14.8627 / dTKp
					- 4.75746 + 0.124699 * dTKp - 0.00900227 * dTKp2)));

			// H2 + «O2 -> H2O:
			Kp[2] = Math.pow(10, (-0.752364 * dLnTKp + 12.4210 / dTKp - 2.60286
					+ 0.259556 * dTKp - 0.0162687 * dTKp2));

			// «O2 + «H2 -> OH:
			Kp[3] = Math.pow(10, (-0.141784 * dLnTKp - 2.13308 / dTKp
					+ 0.853461 + 0.0355015 * dTKp - 0.00310227 * dTKp2));

			// «H2 -> H:
			Kp[4] = Math.pow(10, (0.432168 * dLnTKp - 11.2464 / dTKp + 2.67269
					- 0.0745744 * dTKp + 0.00242484 * dTKp2));

			// «O2 -> O:
			Kp[5] = Math.pow(10, (0.310805 * dLnTKp - 12.9540 / dTKp + 3.21779
					- 0.0738336 * dTKp + 0.00344645 * dTKp2));

			// «N2 -> N:
			Kp[6] = Math.pow(10, (0.389716 * dLnTKp - 24.5828 / dTKp + 3.14505
					- 0.0963730 * dTKp + 0.00585643 * dTKp2));

			// «N2 + «O2 -> NO:
			// in Diss. Gorenflo steht beim letzten Koeffizienten ein
			// Pluszeichen?????
			Kp[7] = Math.pow(10, (0.0150879 * dLnTKp - 4.70959 / dTKp
					+ 0.646096 + 0.00272805 * dTKp - 0.00154444 * dTKp2));

		}else if (GLGW_KONST.equalsIgnoreCase("Burcat")) {
			// Berechnung der Gleichgewichtskonstanten mit Polynomen nach
			// Burcat:

			// CO2-->CO +1/2 O2
			Kp[1] = calc_Kp_7(T, 2.424552150E-01, -1.061546039E-03,
					4.395287975E-07, -7.124313310E-11, 3.814344860E-15,
					3.415079838E+04, 9.659674120E+00);

			// H2+ 1/2 O2 --> H2O
			Kp[2] = calc_Kp_7(T, -2.086272015E+00, 1.818400819E-03,
					-5.567935775E-07, 6.873378010E-11, -2.930636710E-15,
					-2.846484956E+04, 6.199185480E+00);

			// 1/2 O2 +1/2 H2 --> OH
			Kp[3] = calc_Kp_7(T, -4.583653350E-01, 3.659311185E-04,
					-1.502251815E-07, 2.407506450E-11, -1.428934585E-15,
					4.712324615E+03, 4.649423800E+00);

			// 1/2 H2 --> H
			Kp[4] = calc_Kp_7(T, 1.033584750E+00, -4.132990100E-04,
					7.320028500E-08, -7.704925500E-12, 3.443980750E-16,
					2.588018791E+04, 6.547535000E-02);

			// 1/2 O2 --> O
			Kp[5] = calc_Kp_7(T, 7.131565550E-01, -3.554990101E-04,
					6.638444730E-08, -5.335064450E-12, 1.700125460E-16,
					2.983400063E+04, 3.214613650E+00);

			// 1/2 N2 --> N
			Kp[6] = calc_Kp_7(T, 9.396547150E-01, -5.235595500E-04,
					1.272921115E-07, -9.074265750E-12, 2.676777200E-16,
					5.659574934E+04, 1.713665690E+00);

			// 1/2 N2 + 1/2 O2 --> NO
			Kp[7] = calc_Kp_7(T, -4.605626000E-02, 1.643783885E-04,
					-1.122321020E-07, 1.985775365E-11, -1.079614550E-15,
					1.099139429E+04, 1.725380450E+00);
		}else{
			try{
				throw new BirdBrainedProgrammerException ("Für die Auswahl der Gleichgewichtskonstanten wurde \"" +
						GLGW_KONST +"\" eingegeben. Dieser Wert ist aber nicht zulässig. \n"+
						" Es ist unklar wie sowas durch die Kontrolle beim Einlesen des Inputfiles kommt --> Dummkopfprogrammierer!");			

			}catch(BirdBrainedProgrammerException e){
				e.stopBremo();
			}
			
		}

		return Kp;
	}

}
