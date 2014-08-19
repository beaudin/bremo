package berechnungsModule.wandwaerme;

import java.security.InvalidParameterException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import io.VentilhubFileReader;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.LadungswechselAnalyse.LadungsWechselAnalyse;
import misc.HeizwertRechner;
import misc.VektorBuffer;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;
import bremo.parameter.IndizierDaten;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;
import bremoExceptions.StopBremoException;

public class Hensel extends WandWaermeUebergang {

	private Motor_HubKolbenMotor motor;
	private CasePara cp;
//	protected final IndizierDaten indiD;
	
	private double mittlereKolbengeschwindigkeit; 
	private double Twall;
	private double t_zzp;	//Temperatur beim Referenzzeitpunkt [K]
    private double p_zzp;	//Druck beim Referenzzeitpunkt [Pa]
    private boolean setRefConditions=true;
    double mluft;
    double dichte;
    double x_rg;
    double lambda;
    double xCO2_Ex;
    double time_MultStart, time_MultMax, burnMult, burnDurMult;


	
	protected Hensel(CasePara cp) {
		super(cp);
		this.cp=super.cp;
		this.motor=(Motor_HubKolbenMotor) super.motor;
		mittlereKolbengeschwindigkeit=cp.get_DrehzahlInUproSec()*motor.get_Hub()*2; //[m/s]
		Twall=cp.get_T_Cyl();
	    t_zzp = 0;
	    p_zzp = 0;
	    xCO2_Ex = 1;
	    burnMult = cp.get_whtfMult_burn();
	    burnDurMult = cp.get_whtfMult_burn_duration();
	}

	//@Override
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN,double fortschritt) {
		// Ansatz wie bei Bargende: 
		// 4 Terme: alpha = C * d^(m-1) * lambda * (w*rho/eta)^m * Verbr.term
		// charakt. Länge: C1 * d^(m-1)
		// Stoffgrößen:    C2 * lambda * (rho/eta)^m
		// Gasgeschw:      C3 * w^m
		// Verbrennung:    C4 * Verbr.term
		
		if(setRefConditions){
			setRefConditions=false;
			double mTot=0;
			for(int i=0; i<zonen_IN.length;i++)mTot+=zonen_IN[i].get_m();			
			//nach Hans
//			mluft= cp.get_mLuft_feucht_ASP()*cp.get_DrehzahlInUproSec()/2; // [kg/s]
//			dichte = 1.2; // kg / m³; Dichte von Luft bei 20° angenommen => Vereinfachung, da entdrosselt und Dichte aus SAugrohr...
			
			//angepasst um auch bei DVA zu funktionieren
			mluft= mTot*cp.get_DrehzahlInUproSec()/2; // [kg/s]
			dichte=mTot/motor.get_V(time); //nicht ganz sauber aber 1.2 passt bei aufladung ja wohl auch nicht			
			super.feuerstegMult=0.25;		
			Hashtable <Spezies,Double> ht=new Hashtable <Spezies,Double>();
			for(int i=0; i<zonen_IN.length;i++)
				ht.put(zonen_IN[i].get_ggZone(), zonen_IN[i].get_m()/mTot);		
			GasGemisch gg =new GasGemisch("gg");
			gg.set_Gasmischung_massenBruch(ht);	
			Spezies co2=cp.SPEZIES_FABRIK.get_spezCO2();
			double xCO2=0;
			if(gg.get_speziesMolenBruecheDetailToIntegrate().containsKey(co2)) {
				xCO2=gg.get_speziesMolenBruecheDetailToIntegrate().get(co2);
			}else{ //Für die LWA, dort wird die Zone mit gAbgasbehaelter initialisiert, in dem es kein "CO2", nur "AGR intern" gibt
				if(((GasGemisch)gg.get_speziesMolenBruecheDetailToIntegrate().keySet().toArray()[0]).get_speziesMolenBruecheDetailToIntegrate().containsKey(co2))
					xCO2 = ((GasGemisch)gg.get_speziesMolenBruecheDetailToIntegrate().keySet().toArray()[0]).get_speziesMolenBruecheDetailToIntegrate().get(co2);
			}
			Hashtable<Spezies,Double> abg=HeizwertRechner.calcMolenBruechePerfekteVerbrennung(cp, gg);
			xCO2_Ex=abg.get(cp.SPEZIES_FABRIK.get_spezCO2());			
			x_rg=xCO2/xCO2_Ex;
		
			lambda=gg.get_lambda();		
		} else {
			double mTot=0;
			for(int i=0; i<zonen_IN.length;i++)mTot+=zonen_IN[i].get_m();	
			Hashtable <Spezies,Double> ht=new Hashtable <Spezies,Double>();
			for(int i=0; i<zonen_IN.length;i++)
				ht.put(zonen_IN[i].get_ggZone(), zonen_IN[i].get_m()/mTot);		
			GasGemisch gg =new GasGemisch("gg");
			gg.set_Gasmischung_massenBruch(ht);	
			Spezies co2=cp.SPEZIES_FABRIK.get_spezCO2();
			double xCO2=0;
			if(gg.get_speziesMolenBruecheDetailToIntegrate().containsKey(co2)) {
				xCO2=gg.get_speziesMolenBruecheDetailToIntegrate().get(co2);
			}else{ //Für die LWA, dort wird die Zone mit gAbgasbehaelter initialisiert, in dem es kein "CO2", nur "AGR intern" gibt
				GasGemisch ggTemp = null;
				try{
					ggTemp = (GasGemisch)gg.get_speziesMolenBruecheDetailToIntegrate().keySet().toArray()[0]; //notwendig, da sonst Absturz ?! mn
					if(ggTemp.get_speziesMolenBruecheDetailToIntegrate().containsKey(co2))
						xCO2 = ggTemp.get_speziesMolenBruecheDetailToIntegrate().get(co2);
				} catch (ClassCastException c){
					//Abfangen eines Fehlers, der vermutlich vom Rechner abhängig ist. Tritt sporadisch auf. --mn
					//In der LWA im HCCI-Betrieb ist es aber durchaus zulässig mit reinem Abgas zu rechnen. Ein kleiner Fehler wird dann nur während
					//des Ansaugvorgangs gemacht.
					xCO2 = xCO2_Ex;
				}
			}
			x_rg=xCO2/xCO2_Ex;
		}
		double p=zonen_IN[0].get_p()*1E-5;	//Zylinderdruck in bar (das Modell wurde dementsprechend entwickelt)
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double gaskonstante = get_R_brennraum(time, zonen_IN);
		
		double C_ges = 656;
		double exp_m = 0.8;
		
		double charakt_laenge = getCharaktLaenge(time,exp_m);
		double stoffgroessen = getStoffgroessen_Detailliert(T, p, time, exp_m,gaskonstante,fortschritt,x_rg,lambda);
		double geschwindigkeitsterm = getGeschwindigkeit(time,exp_m, mluft,dichte);
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
	
	private double getStoffgroessen_Detailliert(double temperatur, 
												double druck, 
												double time, 
												double exp_m, 
												double gaskonstante, 
												double fortschritt,
												double x_rg,
												double lambda){
		// stoffgroessen = C2 * lambda * (rho/eta)^m
		// Polynome wie bei Bargende, nur nicht vereinfacht 

		double stoffgroessen = 0; // [m]
		//double C2 = 2076*1.3*1.03*3.0*0.085;
		double C2 = 1.0;

		double wearmeleitfaehig_luft = 1.0* 3.17 * 0.0001 * Math.pow(temperatur,0.772);
		double wearmeleitfaehig_abgas = 1.0 * 2.02 * 0.0001 * Math.pow(temperatur,0.837);
			
		double viskositaet_luft = 0.612 * 0.000001 * Math.pow(temperatur,0.609);
		double viskositaet_abgas = 0.355 * 0.000001 * Math.pow(temperatur,0.679);
		
		double anteil_abgas = (x_rg + (1-x_rg) * fortschritt) / lambda;
		
		double waermeleitfaehig = wearmeleitfaehig_abgas * anteil_abgas+  (1-anteil_abgas) * wearmeleitfaehig_luft;
		double viskositaet = viskositaet_abgas * anteil_abgas+  (1-anteil_abgas) * viskositaet_luft;
		
		double dichte = druck/temperatur/gaskonstante;
		
		stoffgroessen = C2 * waermeleitfaehig * Math.pow(dichte/viskositaet, exp_m);

		return stoffgroessen;
			
	}

	private double getGeschwindigkeit(double time, double exp_m,double mluft,double dichte){
		// geschwindigkeit = C3 * w^m
		// w = f(mluft,Vhub) + f(n) = f(einströmen)+f(Drehzahl)	
		double geschwindigkeitsterm = 0; // [m]
		double C3 = 1.0; // 0.98 mit exp 1.4
		double querschnitt = Math.pow(motor.get_Bohrung(),2)/4*Math.PI ;
		double vh = motor.get_IV_Stroke(); // [m]
		double vh_max = motor.get_IV_Stroke_max(); // [m]	
		
		double einstroem =  1.9*(mluft / dichte / querschnitt)*Math.pow((vh_max / vh),1.35);
		
		double momentanegeschw = motor.get_Kolbengeschwindigkeit(time);
		double geschwindigkeit = einstroem+Math.sqrt(Math.pow(momentanegeschw,2) + Math.pow(mittlereKolbengeschwindigkeit,2))/2;

		geschwindigkeitsterm = C3 * Math.pow(geschwindigkeit, exp_m);

		return geschwindigkeitsterm;
	}
	
	private double getVerbrennungsterm(double Druck,double Temperatur,double neuerfortschritt,double time){
//		// geschwindigkeit =  C4 * Verbr.term
//		// wird erst mal vernachlässigt... =1
//		// Verbrennungsglied abhängig von XRG => mit steigender Last 
		double verbrennungsterm = 0; // [m]
//		double C4 = 1;
//		double verbrennung = 1;
//		double t_uv =0, t_v = 0, verbrannt = 0, unverbrannt = 0;
//		double kappa = 1.35;
//
//		double kurbelwinkel = cp.convert_SEC2KW(time);
//		//double fortschritt = neuerfortschritt;
//		if (neuerfortschritt <= 0.0){
//			neuerfortschritt = 1e-19;
//		}
//		if (neuerfortschritt >=1.0){
//			neuerfortschritt = 1.0;			
//		}
//		double zzp = cp.get_refPunkt_Hensel();
//		if (kurbelwinkel <= zzp){ 
//			t_zzp=Temperatur;
//			p_zzp=Druck;
//		} else{
////			System.out.println("");
//		}
//		
//		t_uv = t_zzp * Math.pow( Druck/p_zzp, (kappa-1)/kappa);
//
//		t_v = Temperatur;
//		if (neuerfortschritt>=0.1){
//			t_v = 1/neuerfortschritt * ( Temperatur + (neuerfortschritt-1)*t_uv );
//			//System.out.println(t_v);
//		}
//				
//		verbrannt = neuerfortschritt * t_v / Temperatur * (t_v - Twall) / (Temperatur -Twall ); 
//		unverbrannt = (1-neuerfortschritt) * t_uv / Temperatur * (t_uv-Twall) / (Temperatur -Twall );
//		
//		//verbrennungsterm = Math.pow( verbrannt + unverbrannt ,2);
//		verbrennungsterm = 1;
//		if (neuerfortschritt > 1e-10){
//			verbrennungsterm = Math.pow(unverbrannt/verbrannt,1);
//		}
////	    System.out.println(getTzzp());
////	    System.out.println(fortschritt);
//		verbrennungsterm = 1* (Temperatur - Twall) / (Twall);
//
//		// unverbrannt = (1-neuerfortschritt) ()
//		// Verbrennungsterm soll Anstieg während Umsatz glätten
//		// => Ansatz: Funktion durch sinus ausdrücken
//		
//		//verbrennungsterm = 1+ 0.2 *(Math.sin(neuerfortschritt*Math.PI*2) + 0.2* Math.sin(2*neuerfortschritt*Math.PI*2));
//		//verbrennungsterm = 1 + 0.1 * Math.sqrt(Math.abs(Math.sin(Math.sqrt(neuerfortschritt)*Math.PI*2)));
//		//double imsinus = (Math.PI/2 + Math.asin(2*neuerfortschritt-1))/Math.PI;
//		//verbrennungsterm = 1 + 0.1 * (Math.sin(Math.sqrt(neuerfortschritt)*Math.PI*2))* (1+Math.pow(1-neuerfortschritt,2));
//		//setVerbrennungstermausgabe(verbrennungsterm);
//		// System.out.println(verbrennungsterm);
//		verbrennungsterm = C4  * verbrennung;
		verbrennungsterm = 1.0;
		
//		Multiplier für den Wärmeübergang während der Verbrennung für eine bestimmte Zeit. Anschließend klingt Multiplier
//		linear in 1/3 der angegebenen Zeit ab. --> Zur Berücksichtigung von Grenzschicht-Störung durch extrem schnelle Verbrennung.
		if(neuerfortschritt<0.1){
			time_MultStart = time;
		}else if(neuerfortschritt>=0.1 && time <= time_MultStart+burnDurMult){
			verbrennungsterm = verbrennungsterm * burnMult;
			time_MultMax = time + burnDurMult/3; //Einfach mal so gewählt 
		}else if(time<time_MultMax){
			verbrennungsterm = verbrennungsterm * (time_MultMax - time) / (burnDurMult/3) * burnMult;
		}
		return verbrennungsterm;
	}
	
	//...und hiermit die Wärmestromdichte in W/m^2
	public double get_WandWaermeStrom(double time, Zone[] zonen_IN, double fortschritt,VektorBuffer tBuffer){
		// Waermestromdichte in W/m^2 (W/m^2 x K)
		double cT=10.0;
		double phi_Delay=cT*fortschritt*1000/(cp.get_DrehzahlInUproSec()*60);
		double T=0;
		double ficticious_time=cp.convert_KW2SEC((cp.convert_SEC2KW(time)-phi_Delay));
		if(ficticious_time>=cp.get_time()){
			try{
				T=tBuffer.getValue(cp.get_time());
			}catch(InvalidParameterException ipe){
				System.err.println("Fehler bei Berechnung der Wärmestromdichte nach Hensel.\n" +
						"Dieser Fehler tritt normalerweise nur im ersten Durchlauf der LWA auf.");
			}catch(ArrayIndexOutOfBoundsException aiob){
				System.err.println("Fehler bei Berechnung der Wärmestromdichte nach Hensel in der APR.");
			}
		}else if(ficticious_time<=0){
			T=tBuffer.getValue(0);
		}else{
			T=tBuffer.getValue(ficticious_time);
		}
		
		Motor_HubKolbenMotor hkm=((Motor_HubKolbenMotor)motor);
		double pistonSurf=hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();
		double headSurf=hkm.get_fireDeckArea();
		double cylWallSurf=hkm.get_CylinderLinerArea(time);

		if(Double.isNaN(T_cyl)||Double.isNaN(T_piston)||Double.isNaN(T_head))	{
			T_cyl=cp.get_T_Cyl();	
			T_piston=cp.get_T_Piston();	
			T_head=cp.get_T_Head();	
		}
		double alpha=get_WaermeUebergangsKoeffizient(time,zonen_IN, fortschritt);
		double wht=alpha*(pistonSurf*(T-T_piston)+headSurf*(T-T_head)+cylWallSurf*(T-T_cyl));
		
//		return this.get_WaermeUebergangsKoeffizient(time,zonen_IN, fortschritt) * (T- Twall);
		return wht;
	}	
	
	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time)+0.25*motor.get_FeuerstegFlaeche();
	}
	
}
