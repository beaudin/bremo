package misc;

import kalorik.SpeciesFactory;
import bremoExceptions.MiscException;

public class PhysConst {
	//allgemeine Gaskonstante	
	public static double get_R_allg(){
		//final double R_allg=8.314472; // J/molK
		final double R_allg=8.314; // J/molK
		return R_allg;
	}
	
	//allgemeine Gaskonstante	
	public static double get_R_H2O(){
		return get_R_allg()/get_M_H2O(); // J/kg/K
	}
	
	//allgemeine Gaskonstante	
	public static double get_R_Luft(){
		return get_R_allg()/get_M_Luft(); // J/kg/K
	}
	
	
	//Molaremasse O2	
	public static double get_M_O2(){
		final double M_O2=0.0319988;	//kg/mol
		return M_O2;	
	}
	
	//Molaremasse O	
	public static double get_M_O(){
		final double M_O=0.0159994;	//kg/mol
		return M_O;	
	}
	
	//Molaremasse OH	
	public static double get_M_OH(){
		final double M_OH=0.0170073 ;	//kg/mol
		return M_OH;	
	}
	
	//Molaremasse N2	
	public static double get_M_N2(){
		//final double M_N2=0.0280134;	//kg/mol
		final double M_N2=2*get_M_N();	//kg/mol
		return M_N2;	
	}
	
	//Molaremasse N	
	public static double get_M_N(){
		final double M_N=0.01400674;	//kg/mol 

		return M_N;	
	}
	
	//Molaremasse CO2	
	public static double get_M_CO2(){
		final double M_CO2=0.0440098;	//kg/mol
		return M_CO2;	
	}
	
	//Molaremasse CO	
	public static double get_M_CO(){
		final double M_CO=0.0280104;	//kg/mol
		return M_CO;	
	}
	
	//Molaremasse C	
	public static double get_M_C(){
		final double M_C=	0.0120110;	//kg/mol
		return M_C;	
	}
	
	//Molaremasse H2	
	public static double get_M_H2(){
		final double M_H2=	0.0020159;	//kg/mol
		return M_H2;	
	}
	//Molaremasse H	
	public static double get_M_H(){
		final double M_H=	0.00100794;	//kg/mol 
		return M_H;	
	}
	
	//Molaremasse H2O	
	public static double get_M_H2O(){
		final double M_H2O=	0.0180153;	//kg/mol
		return M_H2O;	
	}
	
	//Molaremasse S	
	public static double get_M_S(){
		final double M_S=	0.0320660;	//kg/mol
		return M_S;	
	}
	
	//Molaremasse S	
	public static double get_M_Ar(){
		final double M_Ar=	0.0399480;	//kg/mol
		return M_Ar;	
	}
	
	//Molaremasse S	
	public static double get_M_C2H5OH(){
		final double M_C2H5OH=	0.04607;	//kg/mol
		return M_C2H5OH;	
	}
		
	
	//////////////////////////////////////////////////////////
	//				Zusammensetzung von Luft				//	
	//				Werte nach NASA ThermoBuild				//	
	//////////////////////////////////////////////////////////
	/**
	 * Die Konzentration von Ar wird nicht beruecksichtigt.	
	 * @return 1-vol_O2-vol_CO2
	 */
	public static double get_vol_N2_Luft(){
		final double volN2_Luft=0.78084;	//m³/m³
		
		return 1-get_vol_O2_Luft()-get_vol_CO2_Luft();	
	}
	
	/**
	 * Die Konzentration von Ar wird nicht beruecksichtigt.	
	 * @return vol_O2
	 */	
	public static double get_vol_O2_Luft(){
		final double volO2_Luft=0.209476;	//m³/m³
		return volO2_Luft;	
		
	}
	/**
	 * Die Konzentration von Ar wird vorerst nicht berücksichtigt
	 * @return 0
	 */
	public static double get_vol_Ar_Luft(){
//		final double volAr_Luft=0.009365;	//m³/m³
//		Damit die Summe der Konzentrationen 1 ergibt
		final double volAr_Luft=1-get_vol_N2_Luft()-get_vol_O2_Luft()-get_vol_CO2_Luft();	
		return 0;	
	}	
	
	/**
	 * Die Konzentration von Ar wird nicht beruecksichtigt.	
	 * @return vol_CO2
	 */	
	public static double get_vol_CO2_Luft(){
//		final double volCO2_Luft=0.000319;	//m³/m³		
		final double volCO2_Luft=0.00;	//m³/m³	
		return volCO2_Luft;			
	}
	
	public static double get_mass_N2_Luft(){
		return get_vol_N2_Luft()*get_M_N2()/get_M_Luft();
	}
		
	public static double get_mass_O2_Luft(){
		return get_vol_O2_Luft()*get_M_O2()/get_M_Luft();
	}
	
	public static double get_mass_Ar_Luft(){
		return get_vol_Ar_Luft()*get_M_Ar()/get_M_Luft();	
	}	
	
	public static double get_mass_CO2_Luft(){
		return get_vol_CO2_Luft()*get_M_CO2()/get_M_Luft();
	}
	
		
	//Molaremasse Luft	
	public static double get_M_Luft(){
		double M_Luft= 0.0289644; 	// kg/mol
		return M_Luft;
//		return SpeziesFabrik.get_spezLuft_trocken().get_M();
	}

	
	private static boolean schonGewarnt=false;
	/** 
	 * @param TinKelvin 
	 * @return Liefert den Saettigungsdampfdruck von Wasser in [Pa]
	 */
	public static double get_saettigunsDampfdruck_H2O(double TinKelvin){
		//Nach Baehr Thermodynamik Seite 289
		double tInCelsius=TinKelvin-273.16;
		double pTr=0.000611657; //Tripelpunktsdruck ptr in [Pa]
		double pws=-1;
		if(tInCelsius>=0.01){
			pws= pTr*Math.exp(17.2799-4102.99/(tInCelsius+237.431));
			if(tInCelsius>60)
				try{
					throw new MiscException("Die Berechnung des Saettigungsdampfdrucks bei Temperaturen " +
							"oberhalb von 60°C kann ungenau sein");
				}catch(MiscException e){
					if(!schonGewarnt){
						e.log_Warning();
						schonGewarnt=true;
					}
				}			
		}else{
			pws= pTr*Math.exp(22.5129*(1-(273.16/TinKelvin)));
			try{
				throw new MiscException("Die Berechnung des Saettigungsdampfdrucks erfolgt mit einer Formel " +
						"fuer einen Temperaturbereich unter 0°C. Kann das sein?");
			}catch(MiscException e){
				e.log_Warning();
			}	
		}
		
		return pws;
	}


}
