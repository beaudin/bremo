package berechnungsModule.internesRestgas;

import io.AusgabeSteurung;
import io.VentilhubFileReader;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;

/**
 * @author kerrom
 *
 */
public class Heywood extends InternesRestgas {

	protected Heywood(CasePara cp) {
		super(cp);	
	}
	
	//__________Parameter__________________________________________________________
	// für das Modell
	private double einlassDruck = super.CP.get_p_LadeLuft() *1e-5; // [bar]
	private double auslassDruck = super.CP.get_p_Abgas() *1e-5; // [bar]
	private double verdichtungsVerhältnis = super.CP.get_Verdichtung();
	private double drehzahl = super.CP.get_DrehzahlInUproSec();
	private double mLuft = super.CP.get_mLuft_trocken_ASP() + super.CP.get_mWasser_Luft_ASP() + super.CP.get_mAGR_extern_ASP(); //[kg]
	private double mKraftstoff = super.CP.MASTER_EINSPRITZUNG.get_mKrst_Sum_ASP(); //[kg]
	private double mGes = mKraftstoff + mLuft;	// [kg]
	private double phi = berechnePhi();
	
	private double dEinlassVentil = super.CP.get_EV_innerer_Ventilteller_Durchmesser();	// [m]
	private double dAuslassVentil = super.CP.get_AV_innerer_Ventilteller_Durchmesser();	// [m]
		
	private Motor_HubKolbenMotor hkm =(Motor_HubKolbenMotor) super.CP.MOTOR;
	private double hubvolumen = hkm.get_Hubvolumen();	// [m]

	// Steuerzeiten in [°KW]
	private double ivo = super.CP.convert_SEC2KW(super.CP.get_Einlassoeffnet());	//Einlass öffnet [°KW]
	private double ivc = super.CP.convert_SEC2KW(super.CP.get_Einlassschluss());	//Einlass schließt [°KW]
	private double evo = super.CP.convert_SEC2KW(super.CP.get_Auslassoeffnet());	//Auslass öffnet [°KW]
	private double evc = super.CP.convert_SEC2KW(super.CP.get_Auslassschluss());	//Auslass schließt [°KW]
		
	// für die Berechnung
	private double schrittweiteKW = super.CP.convert_ProKW_2_ProSEC(CP.SYS.WRITE_INTERVAL_SEC); //[°KW]
		
	// Ventilhub aus Datei
	private VentilhubFileReader ventilHubEinlass = new VentilhubFileReader(super.CP, "Einlass");
	private VentilhubFileReader ventilHubAuslass = new VentilhubFileReader(super.CP, "Auslass");
	
	// Hubkurven
	private double [] einlassHubKurve = hubkurveBauen(ventilHubEinlass, ivo, ivc); //Hubkurve von Ventil öffnet bis Ventil schließt
	private double [] auslassHubKurve = hubkurveBauen(ventilHubAuslass, evo, evc);
	
	// Einlassventilhub = Auslassventilhub
	private double iv_ev = berechneIV_EV(einlassHubKurve, auslassHubKurve);
	
	//"Flächen"berechnung (Integrale)
	private double auslassIntegrationsAnfang = (Math.abs(ivo + super.CP.SYS.DAUER_ASP_KW - evo) + iv_ev); // [°KW]	// Einlass öffnet (ivo) relativ zu Auslass öffnet (evo) + iv_ev
	private double auslassIntegrationsEnde = auslassHubKurve.length * schrittweiteKW;  // [°KW]
	private double Ai = berechneA(einlassHubKurve, 0, iv_ev); //Einlass
	private double Ae = berechneA(auslassHubKurve, auslassIntegrationsAnfang, auslassIntegrationsEnde); //Auslass
	
	
	//_______Methoden________________________________________________________________________
	private double berechnePhi(){
		double lst = super.CP.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst();
		return mKraftstoff / mLuft * lst;
	}
		
	/**
	 * @param ventilhubAusDatei [m]
	 * @param anfangKW [°KW]
	 * @param endeKW [°KW]
	 * @return Hubkurve als Vektor im Bereich von anfangKW bis endeKW [m]
	 */
	private double [] hubkurveBauen(VentilhubFileReader ventilhubAusDatei, double anfangKW, double endeKW) {
		int arraylaenge = (int) Math.round(Math.abs((endeKW-anfangKW)/schrittweiteKW));
		double[] hubkurve = new double[arraylaenge];
		double time = super.CP.convert_KW2SEC(anfangKW);
		for (int i = 0; i < hubkurve.length; i++) {
			hubkurve[i] = ventilhubAusDatei.get_Hub(time); // [m]
			time += CP.SYS.WRITE_INTERVAL_SEC;
		}
		return hubkurve;
	}
	
	
	/**berechnet den Zeitpunkt [°KW], zu dem während der Ventilüberschneidung Einlasshub = Auslasshub
	 * @param einlasskurve
	 * @param auslasskurve
	 * @return Kurbelwinkel, bei dem Einlasshub (ungefähr) = Auslasshub
	 */
	private double berechneIV_EV(double[] einlasskurve, double[] auslasskurve) {
		double kwRelativ = 0;
		double ivoVerschoben = ivo + super.CP.SYS.DAUER_ASP_KW; //Einlass öffnet um ein ASP [°KW] verschoben
		try {
			int anzahlIterationen = (int) Math.round(Math.abs((evc - ivoVerschoben)/ schrittweiteKW)); //entspricht Länge der Ventilüberschneidung
			int indexAuslass = (int) Math.round(Math.abs((ivoVerschoben - evo) / schrittweiteKW)); // Differenz zw. Beginn der Auslasskurve und Beginn der Einlasskurve
			double minimaleDifferenz = Math.abs(einlasskurve[0] - auslasskurve[indexAuslass]); //Initialisierung mit ersten Werten
			for (int i = 0; i < anzahlIterationen; i++) {
				if (einlasskurve[i] != 0.0 && auslasskurve[indexAuslass + i] != 0.0) { // nur, wenn jeweils ein Wert !=0 vorhanden
					double differenz = Math.abs(einlasskurve[i] - auslasskurve[indexAuslass + i]);
					if (differenz < minimaleDifferenz) {
						minimaleDifferenz = differenz;
						kwRelativ = i * schrittweiteKW;
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			if (evc < ivoVerschoben) { //Wenn der Auslass schließt, bevor der Einlass öffnet -> keine Ventilüberschneidung
				AusgabeSteurung.Error("**************************************************" +
									"\nFehler bei der Berechnung des internen Restgases mit Heywood (IndexOutOfBoundsException)."
									+ "\nDie Daten aus dem Input File ergeben keine Ventilüberschneidung." +
									"\n**************************************************\n");
			} else {
				AusgabeSteurung.Error("**************************************************" +
								"Fehler bei Berechnung des internen Restgases mitHeywood (IndexOutOfBoundsException)."
								+ "\n**************************************************\n");
			}
		}
		return kwRelativ;
	}

	
	/** Integralberechnung mit Trapezregel
	 * (Achtung: Funktioniert nur für positive Grenzen!)
	 * @param hubkurve [m]
	 * @param untereGrenzeKW [°KW]
	 * @param obereGrenzeKW [°KW]
	 * @return Fläche unter der Hub-Kurbelwinkel-Kurve [°KW * m]
	 */
	private double berechneA(double [] hubkurve, double untereGrenzeKW, double obereGrenzeKW) {
		double ergebnis = 0.0;
		//Integrationsgrenzen als Integer abhängig von der Schrittweite
		int a = (int) Math.round(untereGrenzeKW /schrittweiteKW); 
		int b = (int) Math.round(obereGrenzeKW /schrittweiteKW);
		//berechnet das Integral im Intervall [a,b] mit der Trapezregel: 0.5*(f[i+1] + f[i])*(x[i+1] - x[i])
		for (int i = a; i < b - 1; i++) {
			ergebnis = ergebnis + schrittweiteKW * (hubkurve[i] + hubkurve[i + 1]) / 2;
		}
		return ergebnis;
	}
			
	/**
	 * @return OF aus Ventilhubkurven [°KW/m]
	 */
	private double berechneOverlapFactor(){
		double OF = (dEinlassVentil*Ai+dAuslassVentil*Ae)/hubvolumen;
		return OF;
	}
	
	private double berechneRestgasbruch(){
		return 1.266*berechneOverlapFactor()/drehzahl
		*Math.pow((einlassDruck/auslassDruck), -0.87)
		*Math.sqrt(Math.abs(auslassDruck-einlassDruck))
		+ 0.632*phi*Math.pow((einlassDruck/auslassDruck),-0.74)
		/verdichtungsVerhältnis;
	}	
	
	@Override
	public double get_mInternesRestgas_ASP() {
		return berechneRestgasbruch()*mGes;
	}

	@Override
	public boolean involvesGasExchangeCalc() {		
		return false;
	}

}
