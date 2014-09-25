package berechnungsModule.wandwaerme;

import java.util.Hashtable;

import kalorik.spezies.GasGemisch;
import kalorik.spezies.Spezies;
import matLib.MatLibBase;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;


public class Woschni extends WandWaermeUebergang {

	private Motor_HubKolbenMotor motor;
	private CasePara cp;
	protected  IndizierDaten indiD;
	private double vDrall;
	private double Bohrungsdurchmesser;
	private double Hubvolumen;
	private double mittlereKolbengeschwindigkeit;
	private double Temperatur_1; //bei Kompressionsbeginn
	private double Druck_1;	//bei Kompressionsbeginn
	private double Volumen_1; //bei Kompressionsbeginn
	private double refPunkt; //RefPunktWoschniHuber
	private double pZyl_a; //bei RefPunktWoschniHuber
	private double Vol_a; //bei RefPunktWoschniHuber
	private double Schleppdruck;
	private double n; //polytropic exponent
	private boolean setRefConditions=true;
	
	//fuer Ausgabe get_Schleppdruck
	private double pSchlepp = 0;

	protected Woschni(CasePara mp) {
		super(mp);
		this.cp=super.cp;	
//		indiD=new IndizierDaten(cp);	//TODO: umprogrammieren, so dass die Indizierdatei nur ein einziges Mal eingelesen wird
		this.motor=(Motor_HubKolbenMotor) super.motor;
		Bohrungsdurchmesser = motor.get_Bohrung(); //[m]
		Hubvolumen = motor.get_Hubvolumen();  //[m^3]
		//Kompressionsvolumen = motor.get_Kompressionsvolumen();	//[m^3]
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_DrehzahlInUproSec(); //[m/s]
		
		//Formel nach Pischinger "Thermodynamik der Verbrennungskraftmaschine" Seite 203
		vDrall=cp.get_swirlRatio()*cp.get_DrehzahlInUproSec()*Math.PI*motor.get_Bohrung()*0.7;
		super.feuerstegMult=0;

//		//Polytropenexponent für die Schleppdruckberechnung ermitteln.
//		//Dies wird in den 10°KW vorm Referenzpunkt gemacht...
//		double refPunkt=cp.get_refPunkt_WoschniHuber();		
//		double[] n_array = new double[10]; 
//		pZyl_a = indiD.get_pZyl(refPunkt);
//		Vol_a = motor.get_V(refPunkt);
//		double pZyl_b = 0;
//		double Vol_b = 0;
//		int cnt=0;
//		
//		//Schleifen-Abbruch nicht mit A<B sondern B-A>sehr kleinem Wert nahe Null!?
//		for(double kw=cp.convert_SEC2KW(refPunkt)-10; (cp.convert_SEC2KW(refPunkt)-kw) > 1E-6; kw++){
//		//for(double kw=cp.convert_SEC2KW(refPunkt)-10; kw < cp.convert_SEC2KW(refPunkt); kw++){
//			pZyl_b=indiD.get_pZyl(cp.convert_KW2SEC(kw));
//			Vol_b=motor.get_V(cp.convert_KW2SEC(kw));
//			n_array[cnt]=Math.log10(pZyl_a/pZyl_b)/Math.log10(Vol_b/Vol_a);
//			cnt++;
//		}
//		n=MatLibBase.mw_aus_1DArray(n_array); //Polytropenexponent

	}	

	
	
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {

////ANFANG Urspruengliche Schleife von Juwe	
//		if(setRefConditions){
//			setRefConditions=false;
//			if(time!=cp.get_Einlassschluss()){//simulation start is not at IVC (e.g homogeneous multizone model
//				temperatur_1=cp.get_T_IVC_WHT(); //[K]	//TODO: If-Schleife damit Abfrage (T und n) nur bei DVA, sonst aus Druckverlauf errechnen
//				n=cp.get_polyExp_WHT(); //für n bei Einlassschluss vor Rechenbeginn
//				if(cp.compareToExp()){
//					//indiD=new IndizierDaten(cp);
//					druck_1=indiD.get_pZyl(cp.get_Einlassschluss()); 
//				}
//				else{				
//					druck_1=cp.get_p_IVC_WHT(); //TODO: If-Schleife damit Abfrage nur bei DVA, sonst aus Druckverlauf holen
//				}				
//				volumen_1=motor.get_V(cp.get_Einlassschluss());	//[m^3]				
//			}else{
////				try{
////					throw new BirdBrainedProgrammerException("Checken " +
////							"ob der WHT nach Woschni funktioniert!!");
////				}catch(BirdBrainedProgrammerException bbpe){
////					bbpe.stopBremo();
////				}
//				temperatur_1=super.get_Tmb(zonen_IN);
//				//getting gamma for the whole cylinder
//				double mTot=0;
//				for(int i=0; i<zonen_IN.length;i++)mTot+=zonen_IN[i].get_m();
//				Hashtable <Spezies,Double> ht=new Hashtable <Spezies,Double>();
//				for(int i=0; i<zonen_IN.length;i++)
//					ht.put(zonen_IN[i].get_ggZone(), zonen_IN[i].get_m()/mTot);
//				
////				GasGemisch gg =new GasGemisch("gg");
////				gg.set_Gasmischung_massenBruch(ht);
////				this.n=gg.get_kappa(temperatur_1);				
//				druck_1=zonen_IN[0].get_p();
//				volumen_1=motor.get_V(time);
//			}
//			
//		}
////ENDE Urspruengliche Schleife von Juwe			
		
		if(setRefConditions){ //Schleife für Referenzbedingungen bei Rechnungsstart
			setRefConditions=false; //Damit Referenzbedingungen nur einmal aufgerufen werden		
		
		if(cp.RESTGASMODELL.involvesGasExchangeCalc()||cp.BERECHNUNGS_MODELL.isDVA()||cp.compareToExp()){ //nur wenn LWA, DVA oder APR mit Vergleich zu Experiment	
//		if(cp.BERECHNUNGS_MODELL.isDVA()|cp.compareToExp()){ //nur wenn DVA oder APR mit Vergleich zu Experiment: alles aus Druckspur holen, egal wann Rechnung startet!
			indiD=new IndizierDaten(cp);	//TODO: umprogrammieren, so dass die Indizierdatei nur ein einziges Mal eingelesen wird
			
			//Polytropenexponent für die Schleppdruckberechnung ermitteln.
			//Dies wird in den 10°KW vorm Referenzpunkt gemacht...
			refPunkt=cp.get_refPunkt_WoschniHuber();

			double[] n_array = new double[10]; 
			pZyl_a = indiD.get_pZyl(refPunkt);
			Vol_a = motor.get_V(refPunkt);
			double pZyl_b = 0;
			double Vol_b = 0;
			int cnt=0;
			
			//Schleifen-Abbruch nicht mit A<B sondern B-A>sehr kleinem Wert nahe Null!?
			for(double kw=cp.convert_SEC2KW(refPunkt)-10; (cp.convert_SEC2KW(refPunkt)-kw) > 1E-6; kw++){
//			for(double kw=cp.convert_SEC2KW(refPunkt)-10; kw < cp.convert_SEC2KW(refPunkt); kw++){ //ORIGINAL
//			for(double kw=refPunkt-cp.convert_KW2SEC(10); kw < refPunkt; kw++){
				pZyl_b=indiD.get_pZyl(cp.convert_KW2SEC(kw));
				Vol_b=motor.get_V(cp.convert_KW2SEC(kw));
				n_array[cnt]=Math.log10(pZyl_a/pZyl_b)/Math.log10(Vol_b/Vol_a);
				cnt++;
			}
			n=MatLibBase.mw_aus_1DArray(n_array); //Polytropenexponent
			///////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////
			
			Druck_1=indiD.get_pZyl(cp.get_Einlassschluss()); //[bar] //Druck
			Volumen_1=motor.get_V(cp.get_Einlassschluss());	//[m^3]	//Volumen

//			/////////T-Berechnung aus id. Gasgl./////////////////
//			//Frischgemisch als Spezies Objekt erstellen
//			//Ersatz des Verbrennungsluftaufrufes mit AGRintern=0
//			Spezies verbrennungsLuft=cp.get_spezVerbrennungsLuft();
//			//Spezies verbrennungsLuft=CP.get_spezVerbrennungsLuftPolytropenmethode();	
//			MasterEinspritzung me=cp.MASTER_EINSPRITZUNG;
//			Spezies krst=me.get_spezKrstALL();	
//			Hashtable<Spezies, Double> frischGemisch_MassenbruchHash=new Hashtable<Spezies,Double>();
//			double mKrst=me.get_mKrst_Sum_ASP();
//			double mVerbrennungsLuft=cp.get_mVerbrennungsLuft_ASP();	
//			double mGes= mVerbrennungsLuft+mKrst;
//			frischGemisch_MassenbruchHash.put(verbrennungsLuft, mVerbrennungsLuft/mGes);
//			frischGemisch_MassenbruchHash.put(krst, mKrst/mGes);		
//
//			GasGemisch frischGemisch=new GasGemisch("Frischgemisch");	
//			frischGemisch.set_Gasmischung_massenBruch(frischGemisch_MassenbruchHash);//		
//			Temperatur_1=Druck_1*Volumen_1/(mGes*frischGemisch.get_R()); //[K] //Temperatur aus idealer Gasgleichung
			Temperatur_1=cp.get_T_IVC_WHT(); //[K]
			///////////////////////////////////////////////////
			
		} //Ende mit Druckspur
		
		else{ //Also klassische APR ohne bekannten Druckverlauf => Fallunterscheidung für Rechnungsstart bei IVC oder nicht!
			
			if(time!=cp.get_Einlassschluss()){//simulation start is not at IVC (e.g homogeneous multizone model)
				
				Temperatur_1=cp.get_T_IVC_WHT(); //[K]
				n=cp.get_polyExp_WHT(); // Polytropenexponent für die Schleppdruckberechnung aus Inputfile
				Druck_1=cp.get_p_IVC_WHT();	
				Volumen_1=motor.get_V(cp.get_Einlassschluss());	//[m^3]

				}
			
			else{ //Also bei Rechenbeginn bei Einlassschluss		
				Temperatur_1=super.get_Tmb(zonen_IN);
						
				//getting gamma for the whole cylinder
				double mTot=0;
				for(int i=0; i<zonen_IN.length;i++)mTot+=zonen_IN[i].get_m();
				Hashtable <Spezies,Double> ht=new Hashtable <Spezies,Double>();
				for(int i=0; i<zonen_IN.length;i++)
					ht.put(zonen_IN[i].get_ggZone(), zonen_IN[i].get_m()/mTot);
				GasGemisch gg =new GasGemisch("gg"); //für n-Ermittlung bei Einlassschluss ohne refPunktWoschniHuber
				gg.set_Gasmischung_massenBruch(ht);  //für n-Ermittlung bei Einlassschluss ohne refPunktWoschniHuber//
				this.n=gg.get_kappa(Temperatur_1);	 //für n-Ermittlung bei Einlassschluss ohne refPunktWoschniHuber
					
				Druck_1=zonen_IN[0].get_p();
				Volumen_1=motor.get_V(time);	
			}
		} //Ende ohne Druckspur			
		} //Ende RefConditions			
		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		// Bestimmung an Hand des Kurbelwinkels und der Steuerzeiten ob Ladungswechsel oder Hochdruckteil
		// Eine Zwischenkompression wird dem Hochdruckteil zugerechnet (Hönl)
		double C_1 = 0;
		if (	// Hochdruckteil	
				((motor.get_Einlass_schliesst() <= time) && (motor.get_Auslass_oeffnet() >= time))
//				|| 
//				// Zwischenkompression
//				(motor.get_Einlass_oeffnet() >= time && motor.get_Auslass_schliesst()<= time) 	
			)
		{	// Hochdruckberechnung
			C_1 = 2.28 + 0.308 * vDrall / mittlereKolbengeschwindigkeit;
			
			//TODO: Einlesen von Schleppdruckdatei einpflegen
			//Schleppdruckberechnung im Hochdruckteil für Verbrennungsglied
			if(cp.RESTGASMODELL.involvesGasExchangeCalc()||cp.BERECHNUNGS_MODELL.isDVA()|cp.compareToExp()){ //nur wenn DVA oder APR mit Vergleich zu Experiment
				//polytrope Schleppdruckberechnung bezüglich refPunktWoschniHuber für Woschni-Modell
				Schleppdruck = pZyl_a*Math.pow((Vol_a/motor.get_V(time)),n); //[Pa]
			}
			else{ //Also klassische APR ohne bekannten Druckverlauf	
				//polytrope Schleppdruckberechnung bezüglich Einlassschluss für Woschni-Modell
				Schleppdruck = Druck_1*Math.pow((Volumen_1/motor.get_V(time)),n); //[Pa]
			}
		}
		else
		{	// Ladungswechselberechnung
			C_1 = 6.18 + 0.417 * vDrall / mittlereKolbengeschwindigkeit;
			Schleppdruck = p; //[Pa] Um Verbrennungsglied auszuschalten 
		}
		//TODO: Koeffizienten von anderen Motortypen einbauen...
		double C_2 = 0.00324; //Dieselmotoren mit Direkteinspritzung und Ottomotoren
		
		//polytrope Schleppdruckberechnung bezüglich Einlassschluss für Woschni-Modell
		//double Schleppdruck = druck_1*Math.pow((volumen_1/motor.get_V(time)),n); //[Pa]
		
		//polytrope Schleppdruckberechnung bezüglich refPunktWoschniHuber für Woschni-Modell
		//double Schleppdruck = pZyl_a*Math.pow((Vol_a/motor.get_V(time)),n); //[Pa]
		




		

		//fuer Ausgabe get_Schleppdruck
		pSchlepp = Schleppdruck;
		
		double Volumen = motor.get_V(time);	//[m^3]
		double v =  mittlereKolbengeschwindigkeit + C_2 / C_1 * Hubvolumen * Temperatur_1 / (Druck_1 * Volumen_1) * (p - Schleppdruck);  
		// Alpha in W/(m²K)
		double alpha = 130 *Math.pow( (Bohrungsdurchmesser),-0.2)*Math.pow(p*1e-5,0.8)*Math.pow(T,-0.53)*Math.pow((C_1 * v ),0.8) ;
		//Statt p in bar einzugeben kann auch 0.013 und der Druck in Pa verwendet werden!		
		return alpha;
	}

	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time);//0*motor.get_FeuerstegFlaeche();
	}
	
	@Override
	//public double get_Schleppdruck(double time,Zone[] zonen_IN){
	public double get_Schleppdruck(){		
		return pSchlepp;
	}	
	
//	@Override
//	public double get_Schleppdruck(double time,Zone[] zonen_IN){		
//		temperatur_1=super.get_Tmb(zonen_IN);
//		//getting gamma for the whole cylinder
//		double mTot=0;
//		for(int i=0; i<zonen_IN.length;i++)mTot+=zonen_IN[i].get_m();
//		Hashtable <Spezies,Double> ht=new Hashtable <Spezies,Double>();
//		for(int i=0; i<zonen_IN.length;i++)
//			ht.put(zonen_IN[i].get_ggZone(), zonen_IN[i].get_m()/mTot);
//		
//		GasGemisch gg =new GasGemisch("gg");
//		gg.set_Gasmischung_massenBruch(ht);
//		this.n=gg.get_kappa(temperatur_1);				
//		druck_1=zonen_IN[0].get_p();
//		volumen_1=motor.get_V(time);
//		double Schleppdruck = druck_1*Math.pow((volumen_1/motor.get_V(time)),n); //[Pa]
//		return Schleppdruck;
//	}
}
