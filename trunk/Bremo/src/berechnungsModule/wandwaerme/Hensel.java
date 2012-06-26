package berechnungsModule.wandwaerme;

import io.VentilhubFileReader;
import berechnungsModule.Berechnung.Zone;
import misc.VektorBuffer;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;

public class Hensel extends WandWaermeUebergang {

	private Motor_HubKolbenMotor motor;
	private CasePara cp;
	protected final IndizierDaten indiD;
	
	private double mittlereKolbengeschwindigkeit; 
	private double Wandtemperatur;
	private double t_zzp;	//Temperatur beim Referenzzeitpunkt [K]
    private double p_zzp;	//Druck beim Referenzzeitpunkt [Pa]
    private VentilhubFileReader VH_Datei_Ein;
	private VentilhubFileReader VH_Datei_Aus;
	
	protected Hensel(CasePara cp) {
		super(cp);
		this.cp=super.cp;
		indiD=new IndizierDaten(cp);	//TODO: umprogrammieren, so dass die Indizierdatei nur ein einziges Mal eingelesen wird
		this.motor=(Motor_HubKolbenMotor) super.motor;
		mittlereKolbengeschwindigkeit=cp.get_DrehzahlInUproSec()*motor.get_Hub()*2; //[m/s]
		Wandtemperatur=cp.get_T_Wand();
	    t_zzp = 0;
	    p_zzp = 0;	       
	}

	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN,double fortschritt) {
		// Ansatz wie bei Bargende: 
		// 4 Terme: alpha = C * d^(m-1) * lambda * (w*rho/eta)^m * Verbr.term
		// charakt. Länge: C1 * d^(m-1)
		// Stoffgrößen:    C2 * lambda * (rho/eta)^m
		// Gasgeschw:      C3 * w^m
		// Verbrennung:    C4 * Verbr.term
		double p=zonen_IN[0].get_p()*1E-5;	//Zylinderdruck in bar (das Modell wurde dementsprechend entwickelt)
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double gaskonstante = get_R_brennraum(time, zonen_IN);
		
		double C_ges = 656;
		double exp_m = 0.8;
		
		double charakt_laenge = getCharaktLaenge(time,exp_m);
		double stoffgroessen = getStoffgroessen_Detailliert(T, p, time, exp_m,gaskonstante,fortschritt);
		double geschwindigkeitsterm = getGeschwindigkeit(time,exp_m);
		double verbrennungsterm = getVerbrennungsterm(p,T,fortschritt,time);
		
		double alpha = C_ges * charakt_laenge * stoffgroessen * geschwindigkeitsterm * verbrennungsterm;
		return alpha;
	}
	
	private double getCharaktLaenge(double time, double exp_m){
		// wie in Hohenberg und Bargende -> scheint auch ganz gut zu sein...
		// Radius einer Kugel mit dem gleichen Volumen wie dem aktuellen Zylindervolumen
		double charakteristischelaenge = 0; // [m]
		double C1 = 1.0;
		double exp= (exp_m-1)/3;
		charakteristischelaenge = C1 * Math.pow(motor.get_V(time), exp );
		
		return charakteristischelaenge;
	}
	
	private double getStoffgroessen_Detailliert(double temperatur, double druck, double time, double exp_m, double gaskonstante, double fortschritt){
		// stoffgroessen = C2 * lambda * (rho/eta)^m
		// Polynome wie bei Bargende, nur nicht vereinfacht 

		double stoffgroessen = 0; // [m]
		//double C2 = 2076*1.3*1.03*3.0*0.085;
		double C2 = 1.0;

		double x_rg = cp.get_interne_AGR_Rate();

		double wearmeleitfaehig_luft = 1.0* 3.17 * 0.0001 * Math.pow(temperatur,0.772);
		double wearmeleitfaehig_abgas = 1.0 * 2.02 * 0.0001 * Math.pow(temperatur,0.837);
			
		double viskositaet_luft = 0.612 * 0.000001 * Math.pow(temperatur,0.609);
		double viskositaet_abgas = 0.355 * 0.000001 * Math.pow(temperatur,0.679);
		
		double anteil_abgas = (x_rg + (1-x_rg) * fortschritt) / cp.get_Lambda_trocken();
		
		double waermeleitfaehig = wearmeleitfaehig_abgas * anteil_abgas+  (1-anteil_abgas) * wearmeleitfaehig_luft;
		double viskositaet = viskositaet_abgas * anteil_abgas+  (1-anteil_abgas) * viskositaet_luft;
		
		double dichte = druck/temperatur/gaskonstante;
		
		stoffgroessen = C2 * waermeleitfaehig * Math.pow(dichte/viskositaet, exp_m);

		return stoffgroessen;
			
	}

	private double getGeschwindigkeit(double time, double exp_m){
		// geschwindigkeit = C3 * w^m
		// w = f(mluft,Vhub) + f(n) = f(einströmen)+f(Drehzahl)
		double geschwindigkeitsterm = 0; // [m]
		double C3 = 1.0; // 0.98 mit exp 1.4
		double mluft = cp.get_mLuft_feucht_ASP()*cp.get_DrehzahlInUproSec()/2; // [kg/s]
		double dichte = 1.2; // kg / m³; Dichte von Luft bei 20° angenommen => Vereinfachung, da entdrosselt und Dichte aus SAugrohr...
		double querschnitt = Math.pow(motor.get_Bohrung(),2)/4*Math.PI ;
		double vh = motor.get_EV_Hub(); // [m]
		double vh_max = motor.get_EV_Hub_max(); // [m]

		double einstroem =  1.9*(mluft / dichte / querschnitt)*Math.pow((vh_max / vh),1.35);
		
		double momentanegeschw = motor.get_Kolbengeschwindigkeit(time);
		double geschwindigkeit = einstroem+Math.sqrt(Math.pow(momentanegeschw,2) + Math.pow(mittlereKolbengeschwindigkeit,2))/2;

		geschwindigkeitsterm = C3 * Math.pow(geschwindigkeit, exp_m);

		return geschwindigkeitsterm;
	}
	
	private double getVerbrennungsterm(double Druck,double Temperatur,double neuerfortschritt,double time){
		// geschwindigkeit =  C4 * Verbr.term
		// wird erst mal vernachlässigt... =1
		// Verbrennungsglied abhängig von XRG => mit steigender Last 
		double verbrennungsterm = 0; // [m]
		double C4 = 1;
		double verbrennung = 1;
		double t_uv =0, t_v = 0, verbrannt = 0, unverbrannt = 0;
		double kappa = 1.35;
		double x_rg = cp.get_interne_AGR_Rate();
		double kurbelwinkel = cp.convert_SEC2KW(time);
		//double fortschritt = neuerfortschritt;
		if (neuerfortschritt <= 0.0){
			neuerfortschritt = 1e-19;
		}
		if (neuerfortschritt >=1.0){
			neuerfortschritt = 1.0;			
		}
		double zzp = cp.get_refPunkt_WoschniHuber();
		if (kurbelwinkel <= zzp){ 
			t_zzp=Temperatur;
			p_zzp=Druck;
		} else{
//			System.out.println("");
		}
		
		t_uv = t_zzp * Math.pow( Druck/p_zzp, (kappa-1)/kappa);

		t_v = Temperatur;
		if (neuerfortschritt>=0.1){
			t_v = 1/neuerfortschritt * ( Temperatur + (neuerfortschritt-1)*t_uv );
			//System.out.println(t_v);
		}
				
		verbrannt = neuerfortschritt * t_v / Temperatur * (t_v - Wandtemperatur) / (Temperatur -Wandtemperatur ); 
		unverbrannt = (1-neuerfortschritt) * t_uv / Temperatur * (t_uv-Wandtemperatur) / (Temperatur -Wandtemperatur );
		
		//verbrennungsterm = Math.pow( verbrannt + unverbrannt ,2);
		verbrennungsterm = 1;
		if (neuerfortschritt > 1e-10){
			verbrennungsterm = Math.pow(unverbrannt/verbrannt,1);
		}
//	    System.out.println(getTzzp());
//	    System.out.println(fortschritt);
		verbrennungsterm = 1* (Temperatur - Wandtemperatur) / (Wandtemperatur);

		// unverbrannt = (1-neuerfortschritt) ()
		// Verbrennungsterm soll Anstieg während Umsatz glätten
		// => Ansatz: Funktion durch sinus ausdrücken
		
		//verbrennungsterm = 1+ 0.2 *(Math.sin(neuerfortschritt*Math.PI*2) + 0.2* Math.sin(2*neuerfortschritt*Math.PI*2));
		//verbrennungsterm = 1 + 0.1 * Math.sqrt(Math.abs(Math.sin(Math.sqrt(neuerfortschritt)*Math.PI*2)));
		//double imsinus = (Math.PI/2 + Math.asin(2*neuerfortschritt-1))/Math.PI;
		//verbrennungsterm = 1 + 0.1 * (Math.sin(Math.sqrt(neuerfortschritt)*Math.PI*2))* (1+Math.pow(1-neuerfortschritt,2));
		//setVerbrennungstermausgabe(verbrennungsterm);
		// System.out.println(verbrennungsterm);
		verbrennungsterm = C4  * verbrennung;
		verbrennungsterm = 1.0;
		return verbrennungsterm;
	}
	
	//...und hiermit die Wärmestromdichte in W/m^2
	public double get_WandWaermeStromDichte(double time, Zone[] zonen_IN, double fortschritt,VektorBuffer tBuffer){
		// Waermestromdichte in W/m^2 (W/m^2 x K)
		double cT=10.0;
		double phi_Delay=cT*fortschritt*1000/(cp.get_DrehzahlInUproSec()*60);
		double T=0;
		double ficticious_time=cp.convert_KW2SEC((cp.convert_SEC2KW(time)-phi_Delay));
		if(ficticious_time>=cp.get_aktuelle_Rechenzeit()){
			T=tBuffer.getValue(cp.get_aktuelle_Rechenzeit());
		}else if(ficticious_time<=0){
			T=tBuffer.getValue(0);
		}else{
			T=tBuffer.getValue(ficticious_time);
		}
		
		return this.get_WaermeUebergangsKoeffizient(time,zonen_IN, fortschritt) * (T- T_WAND);
	}	
	
	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time)+0.25*motor.get_FeuerstegFlaeche();
	}
	
}
