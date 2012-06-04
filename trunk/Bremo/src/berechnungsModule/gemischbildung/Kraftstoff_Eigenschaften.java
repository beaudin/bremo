package berechnungsModule.gemischbildung;

public class Kraftstoff_Eigenschaften {

	public static String [] kraftstoffe={"ndodecan","tetradecan","Diesel"};
	private CalcKrstProps krstProps;
	boolean vargaftik=false;
	
	public Kraftstoff_Eigenschaften(String krstWahl){
		
		if(vargaftik)
			krstProps=new CalcKrstProps_Vargaftik(krstWahl);
		else
			krstProps=new CalcKrstProps_ohneQuelle(krstWahl);
	}
	
	//Molaremasse
	public double get_M_krst(){		
        return krstProps.get_M_krst();
	}

    /**
     * Liefert die dynamische Viskositaet bei 293K
     */
    public double get_dynVis_krst(double T){//T wird nur aus Kompatibilitaetsgruenden uebergeben!
		return krstProps.get_dynVis_krst(T);
	}
   
    public double get_rhoK(double T){	 	
        return krstProps.get_rhoK(T);
	}
	
	public double get_diff_rhoK(double T){		
        return krstProps.get_diff_rhoK(T);
    }
    
    /**
	 * liefert Waermekapazitaet von dampffoermigem Kraftstoff in J/Kg/K
	 * @param T_tropfen
	 * @return
	 */
	public double get_cp(double T){	    	  	
        return krstProps.get_cp(T);
    }
	
	/**
	 * liefert Waermekapazitaet von fluessigem Kraftstoff in J/Kg/K
	 * @param T_tropfen
	 * @return
	 */
	public double get_cpl(double T){				
        return krstProps.get_cpl(T);
    }
	
	/**
	 * liefert waermeleitfaehigkeit von Kraftstoff in W/m/K
	 * @param T_tropfen
	 * @return
	 */
	public double get_waermeLeit(double T){		
	    return krstProps.get_waermeLeit(T);
    }
	
	/**
	 * Liefert den Saettigungsdampfdruck von Kraftstoff in N/m2 
	 * (siehe: ELSEVIER 1290-0729)
	 * @param T_tropfen in K
	 * @return
	 */
	public double get_pS(double T){	        	
        return krstProps.get_pS(T);
    }
	
	/**
	 * Liefert den latent heat of evaporation in J/Kg 
	 * (siehe: ELSEVIER 1290-0729)
	 * @param T_tropfen in K
	 * @return
	 */
    public double get_L(double T){    	
        return krstProps.get_L(T);
    }
    
    /**
	 * Liefert die Waermeleitfaehigkeit von Luft in W/m/K
	 * Die Werte stammen aus Vargaftik
	 * @param T
	 * @return
	 */
	public double get_waermeLeitLuft(double T){
			return krstProps.get_waermeLeitLuft(T); 
	}
	
	/**
	 * Liefert die dynamische Viskositaet von Luft in Ns/m2
	 * Die Werte stammen aus Vargaftik
	 * @param T
	 * @return
	 */
	public double get_dynVisLuft(double T){
		return krstProps.get_dynVisLuft(T); 
	}	
}
	
	


