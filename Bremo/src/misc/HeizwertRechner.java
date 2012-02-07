package misc;

import java.util.Hashtable;

import berechnungsModule.ohc_Gleichgewicht.GleichGewichtsRechner;
import bremo.parameter.CasePara;
import bremoExceptions.MiscException;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.SpeziesFabrik;
import kalorik.spezies.Spezies;

public class HeizwertRechner {

	private HeizwertRechner(){
	}	
	
	

	/**
	 * Berechnet den Heizwert bei perfekter Verbrennung (Produkte = CO2, H2O, O2) 
	 * über die Standard Reaktionsenthalpie
	 * @param krst
	 * @return Hu [J/mol]
	 */
	public static double calcHup_perfekt(CasePara cp, Spezies krst){
		
		double x=krst.get_AnzC_Atome();
		double y=krst.get_AnzH_Atome();		
		double hf_O2=cp.SPEZIES_FABRIK.get_spezO2().get_delta_hf298_mol();
		double hf_CO2=cp.SPEZIES_FABRIK.get_spezCO2().get_delta_hf298_mol();
		double hf_H2O=cp.SPEZIES_FABRIK.get_spezH2O().get_delta_hf298_mol();
		double hf_krst=krst.get_delta_hf298_mol();
		double o2min=krst.get_O2_min();
		
		double Hu=(hf_krst+o2min*hf_O2-x*hf_CO2-0.5*y*hf_H2O);

		return Hu;
	}	
	

	/**
	 * Berechnet die Standardbildungsenthalpie, so dass  bei perfekter Verbrennung (Produkte = CO2, H2O, O2) 
	 * der vorgegebene Heizwert berechnet wird.
	 * @param c
	 * @param h
	 * @param o
	 * @param hu gewuenschter Heizwert [J/mol]
	 * @return Stabdardbildungsenthalpie [J/mol]
	 */
	public static double deltaHf4Hu(CasePara cp,double c, double h, double o, double hu ){
		
		double x=c;
		double y=h;		
		double z=o;
		double hf_O2=cp.SPEZIES_FABRIK.get_spezO2().get_delta_hf298_mol();
		double hf_CO2=cp.SPEZIES_FABRIK.get_spezCO2().get_delta_hf298_mol();
		double hf_H2O=cp.SPEZIES_FABRIK.get_spezH2O().get_delta_hf298_mol();
		
		double o2min=x-0.25*y-0.5*z;		

		return hu-o2min*hf_O2+x*hf_CO2+0.5*y*hf_H2O;		
	}




	/**
	 * Berechnet den Heizwert bei perfekter Verbrennung (Produkte = CO2, H2O, O2) 
	 * über die Standard Reaktionsenthalpie
	 * @param krst Kraftstoff vom Typ Speziesobjekt
	 * @param bezugsTemp Temperatur mit dem Luft und Krafstoff zugefuehrt werden
	 * @return Hu [J/mol]
	 */
	public static double calcHup_perfekt(CasePara cp,Spezies krst, double bezugsTemp){

		double lst=krst.get_Lst();

		if (lst<=0){
			try{
				throw new MiscException("Mit einem Lst<=0 funktioniert die Berechnung von Hu nicht!");
			}catch(MiscException e){
				e.stopBremo();				
			}			
		}		

		double mk=1; //[kg]
		double mL_tr=1*mk*lst;
		double m_ges=mk+mL_tr;

		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();

		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(cp.SPEZIES_FABRIK.get_spezLuft_trocken(), mL_tr/m_ges);		
		GasGemisch gemisch=new GasGemisch("Gemisch");
		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);		


		double h_unverbrannt=gemisch.get_h_mass(bezugsTemp);
		double H_unverbrannt=h_unverbrannt*m_ges/mk; //auf ein kg Krafstoff bezogene Enthalpie in [J/kg]		

		double lambda=1d;	

		GasGemisch abgas=new GasGemisch(calcMolenBruechePerfekteVerbrennung(cp,krst,lambda),"Gemisch");
		double H_verbrannt=abgas.get_h_mass(bezugsTemp)*m_ges/mk;	
		double Hu=(H_unverbrannt-H_verbrannt)*krst.get_M();		
		return Hu;
	}





	/**
	 * Berechnet fuer den angegebenen Kraftstoff den Heizwert aus der Differenz der  
	 * Enthalpien H_unverbrannt(bezugsTemp)-H_verbrannt(bezugsTemp)</br>
	 * Das chemGLGW wird geloest und bestimmt die Zusammmensetzung des Abgases
	 * @param krst Krafstoff vom Typ Speziesobjekt
	 * @param bezugsTemp 	Enthalpie bestimmung
	 * @param lambda		noetig fuer chemGLGW
	 * @param bezugsDruck	noetig fuer chemGLGW
	 * @return Heizwert Hu in [J/mol]
	 */	
	public static double calcHup_chemGLGW(CasePara cp, Spezies krst, double bezugsTemp, double lambda, double bezugsDruck){			

		double lst=krst.get_Lst();

		if (lst<=0){
			try{
				throw new MiscException("Mit einem Lst<=0 funktioniert die Berechnung von Hu nicht!");
			}catch(MiscException e){
				e.stopBremo();				
			}			
		}		

		double mk=1; //[kg]
		double mL_tr=lambda*mk*lst;
		double m_ges=mk+mL_tr;

		Hashtable<Spezies,Double> gemisch_MassenbruchHash=new Hashtable<Spezies,Double>();

		gemisch_MassenbruchHash.put(krst, mk/m_ges);
		gemisch_MassenbruchHash.put(cp.SPEZIES_FABRIK.get_spezLuft_trocken(), mL_tr/m_ges);		
		GasGemisch gemisch=new GasGemisch("Gemisch");
		gemisch.set_Gasmischung_massenBruch(gemisch_MassenbruchHash);		


		double h_unverbrannt=gemisch.get_h_mass(bezugsTemp);
		double H_unverbrannt=h_unverbrannt*m_ges/mk; //auf ein kg Krafstoff bezogene Enthalpie in [J]

		//Abgas
		GleichGewichtsRechner gg=cp.OHC_SOLVER;		
		//liegt die Bezugstemp unterhalb von T_Freeze (was ratsam ist) 
		//wird T_Frezze fuer das chem. Glgw verwendet
		gemisch.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(bezugsDruck, bezugsTemp, gemisch));
		//		System.out.println("LambdaAbgas: "+gemisch.get_lambda());
		double h_verbrannt=gemisch.get_h_mass(bezugsTemp);
		double H_verbrannt=h_verbrannt*m_ges/mk;
		double Hu_1=H_unverbrannt-H_verbrannt;	

		return Hu_1*krst.get_M();
	}
	
	
	
	/**
	 * Berechnet fuer das angegebene Gemisch den Gemischeizwert aus der Differenz der  
	 * Enthalpien H_unverbrannt(bezugsTemp)-H_verbrannt(bezugsTemp)</br>
	 * Das chemGLGW wird geloest und bestimmt die Zusammmensetzung des Abgases
	 * @param gemisch  Typ Speziesobjekt
	 * @param bezugsTemp 	Enthalpie bestimmung
	 * @return Heizwert Hu in [J/kg]
	 */	
	public static double calcGemischHeizwert_chemGLGW_mass(
			CasePara cp,Spezies gemisch, double bezugsTemp, double bezugsDruck){
		double m_ges=1;
		double h_unverbrannt=gemisch.get_h_mass(bezugsTemp);
		double H_unverbrannt=h_unverbrannt*m_ges; //auf ein kg Gemsich bezogene Enthalpie in [J]

		//Abgas
		GleichGewichtsRechner gg=cp.OHC_SOLVER;		
		//liegt die Bezugstemp unterhalb von T_Freeze (was ratsam ist) 
		//wird T_Frezze fuer das chem. Glgw verwendet
		GasGemisch abgas=new GasGemisch("Abgas");
		abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(bezugsDruck, bezugsTemp, gemisch));
		
		double h_verbrannt=abgas.get_h_mass(bezugsTemp);
		double H_verbrannt=h_verbrannt*m_ges;
		double Hu_1=H_unverbrannt-H_verbrannt;	

		return Hu_1/m_ges;
	}

	
	
	
	
	

	public static Hashtable<Spezies,Double>calcMolenBruechePerfekteVerbrennung(
			CasePara cp,Spezies krst, double lambda){
		if(lambda<1){
			try{
				throw new MiscException("Funktion ist nicht definiert fuer Lambda<1");
			}catch(MiscException me){
				me.stopBremo();
			}
		}

		double x=krst.get_AnzC_Atome();
		double y=krst.get_AnzH_Atome();		
		double o2min=krst.get_O2_min();	
		double _N2_O2=PhysKonst.get_vol_N2_Luft()/PhysKonst.get_vol_O2_Luft();
		double _CO2_O2=PhysKonst.get_vol_CO2_Luft()/PhysKonst.get_vol_O2_Luft();
		//CO2+H2O+CO2_Luft+N2_Luft+O2_ueberschuss
		double anzMoleAbgas=x+0.5*y+1*o2min*(_N2_O2+_CO2_O2)+(lambda-1)*o2min*(1+_N2_O2+_CO2_O2);

		//Molenbrueche
		double mb_CO2=(x+lambda*o2min*(_CO2_O2))/anzMoleAbgas;
		double mb_H2O=0.5*y/anzMoleAbgas;
		double mb_N2=lambda*o2min*(_N2_O2)/anzMoleAbgas;
		double mb_O2=(lambda-1)*o2min/anzMoleAbgas;

		Hashtable<Spezies,Double> abgas_MolenbruchHash=new Hashtable<Spezies,Double>();

		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezCO2(),mb_CO2);
		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezH2O(),mb_H2O);
		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezN2(),mb_N2);
		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezO2(),mb_O2);	
		return abgas_MolenbruchHash;
		
	}
	
	
	
	
	public static Hashtable<Spezies,Double>calcMolenBruechePerfekteVerbrennung(
			CasePara cp,Spezies gemisch){
		if(gemisch.get_O2_min()>0){
			try{
				throw new MiscException("Funktion ist nicht definiert fuer Lambda<1");
			}catch(MiscException me){
				me.stopBremo();
			}
		}

		//aC+bH+kO+dN --> eCO2+fH2O+gN2+iO2
		double a=gemisch.get_AnzC_Atome();
		double b=gemisch.get_AnzH_Atome();	
		double k=gemisch.get_AnzO_Atome();	
		double d=gemisch.get_AnzN_Atome();	
		//CO2
		double e=(a);
		double f= (0.5*b);
		double g=(0.5*d);
		double i=(0.5*k-a-0.25*b);
		
		double moleAbgas=e+f+g+i;		
		
		Hashtable<Spezies,Double> abgas_MolenbruchHash=new Hashtable<Spezies,Double>();
		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezCO2(),e/moleAbgas);
		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezH2O(),f/moleAbgas);
		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezN2(),g/moleAbgas);
		abgas_MolenbruchHash.put(cp.SPEZIES_FABRIK.get_spezO2(),i/moleAbgas);	

		return abgas_MolenbruchHash;		
	}



	/**
	 * Berechnet die adiabate Verbrennungstemperatur fuer den angegebenen Kraftstoff bei Verbrennung mit Luft 
	 * (ohne Beruecksichtigung der Dissoziation)
	 * @param krst Krafstoff vom Typ Speziesobjekt
	 * @param bezugsTemp Temperatur mit der dKrafstoff und Luft zugefuehrt werden [K] 
	 * @param lambda Luftverhaeltnis
	 * @return adiabate Verbrennungstemperatur in [K]
	 */
	public static double calcadiabateFlammenTempPerfekt(CasePara cp,Spezies krst, double T, double lambda){	
		if(krst.get_Hu_mass()<=0){
			try{
				throw new MiscException("Funktion ist nicht definiert fuer Spezies ohne Heizwert");
			}catch(MiscException me){
				me.stopBremo();
			}
		}			
			
			double lst=krst.get_Lst();
			double mk=1; //[kg]
			double mL_tr=lambda*mk*lst;
			double m_ges=mk+mL_tr;
	
			Spezies luft=cp.SPEZIES_FABRIK.get_spezLuft_trocken();
			
			GasGemisch abgas=new GasGemisch("Abgas");	
			abgas.set_Gasmischung_molenBruch(calcMolenBruechePerfekteVerbrennung(cp,krst,lambda));	
			
			double h_=mk/m_ges*krst.get_h_mass(T)+mL_tr/m_ges*luft.get_h_mass(T);			
			
			return abgas.get_T4h_mass(h_);	
	}
	
	
	/**
	 * Berechnet die adiabate Verbrennungstemperatur fuer das angegebene Gemisch bei Verbrennung mit Luft 
	 * (ohne Beruecksichtigung der Dissoziation)
	 * @param gemisch Frischgemisch vom Typ Speziesobjekt
	 * @param bezugsTemp Temperatur mit der dKrafstoff und Luft zugefuehrt werden [K] 
	 * @param lambda Luftverhaeltnis
	 * @return adiabate Verbrennungstemperatur in [K]
	 */
	public static double calcadiabateFlammenTempPerfekt(CasePara cp,Spezies gemisch, double T){	
		if(gemisch.get_Hu_mass()<=0){
			try{
				throw new MiscException("Funktion ist nicht definiert fuer Spezies ohne Heizwert");
			}catch(MiscException me){
				me.stopBremo();
			}
		}	

		//Wenn die Dissoziation des Abgases nicht berücksichtigt wird funktioniert diese
		//Art der Definition von hu  (vgl Baehr Thermodynamik) --> Bei berücksichtigung von Dissoziation veraendert sich 
		//allerdings die Standardbildungsenthalpie des Abgases mit der Temperature (also der Dissoziation)
		//und damit verändert sich auch der Heizwert
		double hu=gemisch.get_Hu_mass()+gemisch.get_h_h298_mass(T);	
		GasGemisch abgas=new GasGemisch("Abgas");	
		abgas.set_Gasmischung_molenBruch(calcMolenBruechePerfekteVerbrennung(cp,gemisch));			

		int idx=0;
		double To=6000, Tu=T; 
		double Tb;		
		do{
			Tb=(To+Tu)/2;				
			if(abgas.get_h_h298_mass(Tb)>hu)To=Tb;
			if(abgas.get_h_h298_mass(Tb)<hu)Tu=Tb;
			
			idx++;
		}while(Math.abs(To-Tu)>1e-6 && idx<=1000);
		if(idx>=100){
			try{
				throw new MiscException("Die Berechnung der adiabaten Flammentemperatur konvergierte nicht");
			}catch(MiscException me){
				me.stopBremo();
			}
		}

		return Tb;

	}
	


	/**
	 * Berechnet fuer den angegebenen Krafstoff die adiabate Verbrennungstemperatur bei Verbrennung mit Luft
	 * @param krst Kraftstoff als Objekt vom Typ Spezies
	 * @param p Duck bei dem die Verbrennung ablaeuft in [Pa]
	 * @param T	Temperatur mit der Krafstoff und Luft zugefuehrt werden in [K]
	 * @param lambda  Luftverhaeltnis mit dem der Kraftstoff verbrannt wird
	 * @return adiabate Verbrennungstemperatur in [K]
	 */
	public static double calcAdiabateFlammenTemp(
			CasePara cp, Spezies krst,double p, double T, double lambda ){				
	
			double lst=krst.get_Lst();
			double mk=1; //[kg]
			double mL_tr=lambda*mk*lst;
			double m_ges=mk+mL_tr;
	
			Spezies luft=cp.SPEZIES_FABRIK.get_spezLuft_trocken();
			GleichGewichtsRechner gg=cp.OHC_SOLVER;
			GasGemisch abgas=new GasGemisch("Abgas");		
			
			double h_=mk/m_ges*krst.get_h_mass(T)+mL_tr/m_ges*luft.get_h_mass(T);	
			
			int idx=0;
			double To=calcadiabateFlammenTempPerfekt(cp,krst,T,lambda), Tu=T; 
			double Tb;
			//Intervallschachtelung da get_T4h() fuer hohe Anfangstemp nicht konvergiert!
			do{
				Tb=(To+Tu)/2;							
				abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, Tb, lambda, 
													krst.get_AnzC_Atome(), 
													krst.get_AnzH_Atome(), 
													krst.get_AnzO_Atome(), 
													krst.get_AnzN_Atome()));
				
				if(abgas.get_h_mass(Tb)>h_)To=Tb;
				if(abgas.get_h_mass(Tb)<h_)Tu=Tb;			
				
				idx++;
			}while(Math.abs(To-Tu)>1e-6 && idx<=1000);
			
			if(idx>=100){
				try{
					throw new MiscException("Die Berechnung der adiabaten Flammentemperatur konvergierte nicht");
				}catch(MiscException me){
					me.stopBremo();
				}
			}		
			
			return Tb;				
		}
	

	/**
	 * Berechnet die adiabate Verbrennungstemperatur fuer das angegebene Gemisch 
	 * (Oxidationsmittel kann nur Luft sein da sonst der Gleichgewichtssolver nicht konvergiert)
	 * @param gemisch Gemisch, das verbrannt wird (Spezies Objekt)
	 * @param p Duck bei dem die Verbrennung ablaeuft in [Pa]
	 * @param T Temperatur mit der das Gemisch zugefuehrt wird
	 * @return
	 */
	public static double calcAdiabateFlammenTemp(CasePara cp, Spezies gemisch,double p, double T){		

		//totale Enthalpie im unverbrannten Gemisch	
		double hu=gemisch.get_h_mass(T);
		
		//Abgas
		GleichGewichtsRechner gg=cp.OHC_SOLVER;
		GasGemisch abgas=new GasGemisch("Abgas");	

		int idx=0;
		double To,Tu=T;
//		To=calcadiabateFlammenTempPerfekt(gemisch, T); 
		To=6000;
		double Tb;
		//Intervallschachtelung da get_T4h() für hohe Anfangstemp nicht konvergiert!
		do{
			Tb=(To+Tu)/2;
			abgas.set_Gasmischung_molenBruch(gg.get_GG_molenBrueche(p, Tb, gemisch));

			if(abgas.get_h_mass(Tb)>hu)To=Tb;
			if(abgas.get_h_mass(Tb)<hu)Tu=Tb;

			idx++;
		}while(Math.abs(To-Tu)>1e-6 && idx<=1000);
	
		if(idx>=100){
			try{
				throw new MiscException("Die Berechnung der adiabaten Flammentemperatur konvergierte nicht");
			}catch(MiscException me){
				me.stopBremo();
			}
		}

		return Tb;

	}

}
