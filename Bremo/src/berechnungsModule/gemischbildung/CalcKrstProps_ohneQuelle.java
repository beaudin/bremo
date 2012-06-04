package berechnungsModule.gemischbildung;

import bremoExceptions.MiscException;

public class CalcKrstProps_ohneQuelle extends CalcKrstProps{
	private String krstWahl;		
	
	public CalcKrstProps_ohneQuelle(String krstWahl){
		super();
		this.krstWahl=krstWahl;
	}
	
	//Molaremasse
	public double get_M_krst(){
		double M_krst=-1;
		if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		M_krst=get_M_ndodecan();
		else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		M_krst=get_M_tetradecan();
		else if(krstWahl.equalsIgnoreCase("Diesel"))
    		M_krst=get_M_diesel();
    	else
    		M_krst=get_M_nheptan();    	
        return M_krst;
	}
	
	private double get_M_nheptan(){
    	double M_nheptane=0.10021;  // kg/mol
    	return M_nheptane;
    }

    private double get_M_ndodecan(){
    	double M_ndodecane=0.17034;  // kg/mol
    	return M_ndodecane;
    }
    
    private double get_M_tetradecan(){
    	double M_tetradecan=0.19839; // kg/mol
    	return M_tetradecan;
    }
   
    private double get_M_diesel(){
    	double M_diesel=0.233;  // kg/mol
    	return M_diesel;
    }
    
    
    /**
     * Liefert die dynamische Viskositaet bei 293K
     */
    public double get_dynVis_krst(double T){//T wird nur aus Kompatibilitaetsgruenden uebergeben!
		double mue_krst=-1;
		if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		mue_krst=get_mue_ndodecan();
		else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		mue_krst=get_mue_tetradecan(T);
		else if(krstWahl.equalsIgnoreCase("Diesel"))
    		mue_krst=get_mue_diesel();
    	else
    		mue_krst=get_mue_nheptan();    	
        return mue_krst;
	}
   
    private double get_mue_nheptan(){
    	double mue_nheptan=0.410e-3; // Pa*s
    	return mue_nheptan;
    }
    
    private double get_mue_ndodecan(){
        double mue_ndodecan=1.52e-3; // Pa*s
        return mue_ndodecan;
    }
    
    private double get_mue_tetradecan(double T){    	    	
    	double mue_tetradecan=2.18e-3; // Pa*s
//    	double mue_tetradecan=6.85e-4; // Pa*s aus Vargaftik 
    	return mue_tetradecan;
    }
    
    private double get_mue_diesel(){
    	double mue_diesel=3.309e-3; // Pa*s
    	return mue_diesel;
    }
	
	public double get_rhoK(double T_tropfen){
		double rhoK=-1;
		if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		rhoK=get_rhoK_ndodecan(T_tropfen);
		else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		rhoK=get_rhoK_tetradecan(T_tropfen);
		else if(krstWahl.equalsIgnoreCase("Diesel"))
    		rhoK=get_rhoK_diesel(T_tropfen);
    	else
    		rhoK=get_rhoK_nheptan(T_tropfen);    	
        return rhoK;
	}
	
	private double get_rhoK_nheptan(double T_tropfen){
		double rhoK_nheptan = 0;
		if(T_tropfen<=538)
			rhoK_nheptan=-941.03+
			             19.96181*T_tropfen-
			             0.08612051*T_tropfen*T_tropfen+
			             1.579494e-4*T_tropfen*T_tropfen*T_tropfen-
			             1.089345e-7*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		else if(T_tropfen>538)
			rhoK_nheptan=4.195281e7-
			             2.360524e5*T_tropfen+
			             442.7316*T_tropfen*T_tropfen-
			             0.2767921*T_tropfen*T_tropfen*T_tropfen;
	
		return rhoK_nheptan;
	}
	
	private double get_rhoK_ndodecan(double T_tropfen){
		double rhoK_ndodecan;
		rhoK_ndodecan=1104.98-
		              1.9277*T_tropfen+
		              0.003411*T_tropfen*T_tropfen-
		              3.2851e-6*T_tropfen*T_tropfen*T_tropfen;
		return rhoK_ndodecan;
	}
	
	private double get_rhoK_tetradecan(double T_tropfen){
		double rhoK_tetradecan;
		rhoK_tetradecan=915.017-
		                0.366493*T_tropfen-
		                4.68132e-4*T_tropfen*T_tropfen;
		return rhoK_tetradecan;
	}
	
	private double get_rhoK_diesel(double T_tropfen){
		double rhoK_diesel;
		rhoK_diesel=840/(1+0.00067*(T_tropfen-288));
		return rhoK_diesel;
	}
	
	public double get_diff_rhoK(double T_tropfen){		
		double diff_rhoK=-1;     	
    	if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		diff_rhoK=get_diff_rhoK_ndodecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		diff_rhoK=get_diff_rhoK_tetradecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("Diesel"))
    		diff_rhoK=get_diff_rhoK_diesel(T_tropfen);
    	else
    		diff_rhoK=get_diff_rhoK_nheptan(T_tropfen);    	
        return diff_rhoK;
    }
    
    private double get_diff_rhoK_nheptan(double T_tropfen){
    	double diff_rhoK_nheptan = 0;
    	if(T_tropfen<=538)
    	    diff_rhoK_nheptan=19.96181-
    	                      0.17224102*T_tropfen+
    	                      4.738482e-4*T_tropfen*T_tropfen-
    	                      4.35738e-7*T_tropfen*T_tropfen*T_tropfen;
    	else if(T_tropfen>538)
    		diff_rhoK_nheptan=-2.360524e5+885.4632*T_tropfen-0.8303763*T_tropfen*T_tropfen;
    	return diff_rhoK_nheptan;
    }
    
    private double get_diff_rhoK_ndodecan(double T_tropfen){
    	double diff_rhoK_ndodecan;
    	diff_rhoK_ndodecan=-1.9277+0.006822*T_tropfen-9.8553e-6*T_tropfen*T_tropfen;
    	return diff_rhoK_ndodecan;
    }
    
	private double get_diff_rhoK_tetradecan(double T_tropfen){
		double diff_rhoK_tetradecan;
		diff_rhoK_tetradecan=-0.366493-9.36264e-4*T_tropfen;
		return diff_rhoK_tetradecan;
	}
	
	private double get_diff_rhoK_diesel(double T_tropfen){
		double diff_rhoK_diesel;
		diff_rhoK_diesel=-0.5628/Math.pow(0.00067*T_tropfen+0.80704,2);
		return diff_rhoK_diesel;
	}
	
	/**
	 * liefert Waermekapazitaet von dampffoermigem Kraftstoff in J/Kg/K
	 * @param T_tropfen
	 * @return
	 */
	public double get_cp(double T_tropfen){		
		double cpf=-1;     	
    	if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		cpf=get_cpf_ndodecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		cpf=get_cpf_tetradecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("Diesel"))
    		cpf=get_cpf_diesel(T_tropfen);
    	else
    		cpf=get_cpf_nheptan(T_tropfen);    	
        return cpf;
    }
	
	private double get_cpf_nheptan(double T_tropfen){
		double cpf_nheptan;
		cpf_nheptan=799.3401062+
		            0.3448263942*T_tropfen+
		            0.01285548641*T_tropfen*T_tropfen-
		            1664.890863e-8*T_tropfen*T_tropfen*T_tropfen+
		            644.6826474e-11*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		return cpf_nheptan;
	}
	
	private double get_cpf_ndodecan(double T_tropfen){
		double cpf_ndodecan;
		cpf_ndodecan=380.63+
		             4.1372*T_tropfen+
		             2.004e-4*T_tropfen*T_tropfen-
		             1.8009e-6*T_tropfen*T_tropfen*T_tropfen+
		             7.7875e-10*T_tropfen*T_tropfen*T_tropfen*T_tropfen-
		             1.0152e-13*T_tropfen*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		return cpf_ndodecan;
	}
	
	private double get_cpf_tetradecan(double T_tropfen){
		double cpf_tetradecan;
		cpf_tetradecan=-1.7787319+
		               6.4564177*T_tropfen-
		               3.2454867e-3*T_tropfen*T_tropfen+
		               4.5752023e-7*T_tropfen*T_tropfen*T_tropfen+
		               8.510382e-11*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		return cpf_tetradecan;
	}
	
	private double get_cpf_diesel(double T_tropfen){
		double cpf_diesel;
		cpf_diesel=380.63+
		           4.1372*T_tropfen+
		           2.004e-4*T_tropfen*T_tropfen-
		           1.8009e-6*T_tropfen*T_tropfen*T_tropfen+
		           7.7875e-10*T_tropfen*T_tropfen*T_tropfen*T_tropfen-
		           1.0152e-13*T_tropfen*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		return cpf_diesel;
	}
	
	/**
	 * liefert Waermekapazitaet von fluessigem Kraftstoff in J/Kg/K
	 * @param T_tropfen
	 * @return
	 */
	public double get_cpl(double T_tropfen){		
		double cpl=-1;     	
    	if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		cpl=get_cpl_ndodecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		cpl=get_cpl_tetradecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("Diesel"))
    		cpl=get_cpl_diesel(T_tropfen);
    	else
    		cpl=get_cpl_nheptan(T_tropfen);    	
        return cpl;
    }
	
	private double get_cpl_nheptan(double T_tropfen){
		double cpl_nheptan;
		cpl_nheptan=13058.45044066-
		            126.5095282565*T_tropfen+
		            0.5279613848638*T_tropfen*T_tropfen-
		            0.0009457386295687*T_tropfen*T_tropfen*T_tropfen+
		            6.369853422618e-7*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		return cpl_nheptan;
	}
	
	private double get_cpl_ndodecan(double T_tropfen){
		double cpl_ndodecan;
		cpl_ndodecan=803.42+
		             5.076*T_tropfen-
		             0.00221*T_tropfen*T_tropfen+
		             1.673e-6*T_tropfen*T_tropfen*T_tropfen;
		return cpl_ndodecan;
	}
	
	private double get_cpl_tetradecan(double T_tropfen){
		double cpl_tetradecan;
		cpl_tetradecan=1453.5010887*Math.exp(0.0014122933017*T_tropfen);
		return cpl_tetradecan;
	}
	
	private double get_cpl_diesel(double T_tropfen){
		double cpl_diesel;
		cpl_diesel=264+6.33*T_tropfen-0.00296*T_tropfen*T_tropfen;
		return cpl_diesel;
	}
	
	/**
	 * liefert waermeleitfaehigkeit von Kraftstoff in W/m/K
	 * @param T_tropfen
	 * @return
	 */
	public double get_waermeLeit(double T_tropfen){		
		double k=-1; 
		if(krstWahl.equalsIgnoreCase("ndodecan"))
    		k=get_k_ndodecan(T_tropfen);
		else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		k=get_k_tetradecan(T_tropfen);
		else if(krstWahl.equalsIgnoreCase("Diesel"))
    		k=get_k_diesel(T_tropfen);
    	else
    		k=get_k_nheptan(T_tropfen);    	
        return k;
    }
	
	private double get_k_nheptan(double T_tropfen){
		double k_nheptan;
		if (T_tropfen<=540.17)
		    k_nheptan=0.25844890110-4.5549450549e-4*T_tropfen;
		else{
	    	try{
	    		throw new MiscException("Die Formel zur Berechnung der " +
	    				"Waermeleitfaehigkeit fuer \"nheptan\" " +
	    				"ist fuer Temperaturen ueber 540K nicht zulaessig. \n" +
	    				"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
	    	}catch(MiscException me){
	    		me.log_Warning();
	    	}
	    	 k_nheptan=get_k_nheptan(540.17);
		}		    	 
		return k_nheptan;
	}
	
	private double get_k_ndodecan(double T_tropfen){
		double k_ndodecan;
		k_ndodecan=0.207-2.28e-4*T_tropfen;
		return k_ndodecan;
	}
	
	private double get_k_tetradecan(double T_tropfen){
		double k_tetradecan;
		if(T_tropfen<=693)
		    k_tetradecan=0.16243019148+
		                 1.1551271437e-4*T_tropfen-
		                 7.6492882118e-7*T_tropfen*T_tropfen+
		                 5.9731934732e-10*T_tropfen*T_tropfen*T_tropfen;
		else{
	    	try{
	    		throw new MiscException("Die Formel zur Berechnung der " +
	    				"Waermeleitfaehigkeit fuer \"tetradecan\" " +
	    				"ist fuer Temperaturen ueber 693K nicht zulaessig. \n" +
	    				"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
	    	}catch(MiscException me){
	    		me.log_Warning();
	    	}
	    	k_tetradecan=get_k_tetradecan(693);
		}
		return k_tetradecan;
	}
	
	private double get_k_diesel(double T_tropfen){
		double k_diesel;
		k_diesel=0.209-2.13e-4*T_tropfen;
		return k_diesel;
	}
	
	/**
	 * Liefert den Saettigungsdampfdruck von Kraftstoff in N/m2 
	 * (siehe: ELSEVIER 1290-0729)
	 * @param T_tropfen in K
	 * @return
	 */
	public double get_pS(double T_tropfen){		
		double pS=-1;     	
    	if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		pS=get_pS_ndodecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		pS=get_pS_tetradecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("Diesel"))
    		pS=get_pS_diesel(T_tropfen);
    	else
    		pS=get_pS_nheptan(T_tropfen);    	
        return pS;
    }
	
	private double get_pS_nheptan(double T_tropfen){ 
	    double pS_nheptan; //Saettigungsdampfdruck in Pa
	    if(T_tropfen<=540.17)
	        pS_nheptan=Math.pow(10,(9.02677-1258.34/(T_tropfen-53.85)));
	    else{
	    	try{
	    		throw new MiscException("Die Formel zur Berechnung des " +
	    				"Saettigungsdampfdruckes fuer \"nheptan\" " +
	    				"ist fuer Temperaturen ueber 540K nicht zulaessig. \n" +
	    				"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
	    	}catch(MiscException me){
	    		me.log_Warning();
	    	}
	    	pS_nheptan=get_pS_nheptan(540.17);
	    }	       
	    return pS_nheptan;
	}
	
	private double get_pS_ndodecan(double T_tropfen){ 
		double pS_ndodecan; //Saettigungsdampfdruck in Pa
		if(T_tropfen<=659)
			pS_ndodecan=6894.757*Math.exp(12.12767-3743.84/(T_tropfen-93.022));
		else{
			try{
				throw new MiscException("Die Formel zur Berechnung des " +
						"Saettigungsdampfdruckes fuer \"ndodecan\" " +
						"ist fuer Temperaturen ueber 659K nicht zulaessig. \n" +
						"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
			}catch(MiscException me){
				me.log_Warning();
			}
			pS_ndodecan=get_pS_ndodecan(659);
		}
		return pS_ndodecan;
	}
    
    private double get_pS_tetradecan(double T_tropfen){ 
	    double pS_tetradecan; //Saettigungsdampfdruck in Pa
	    if(T_tropfen<=693)
	        pS_tetradecan=Math.pow(10,(9.1379-1740.88/(T_tropfen-105.43)));
	    else{
			try{
				throw new MiscException("Die Formel zur Berechnung des " +
						"Saettigungsdampfdruckes fuer \"tetradecan\" " +
						"ist fuer Temperaturen ueber 693K nicht zulaessig. \n" +
						"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
			}catch(MiscException me){
				me.log_Warning();
			}
			pS_tetradecan=get_pS_tetradecan(693);
	    }
	    return pS_tetradecan;
	}
    
    private double get_pS_diesel(double T_tropfen){
    	double pS_diesel;
    	if(T_tropfen<380)
    		pS_diesel=1000*Math.exp(8.5872101-2591.5232/(T_tropfen-43));
    	else if(T_tropfen>=380 && T_tropfen<500)
    		pS_diesel=1000*Math.exp(14.060729-4436.099/(T_tropfen-43));
    	else if(T_tropfen>=500 && T_tropfen<620)
    		pS_diesel=1000*Math.exp(12.93692-3922.5184/(T_tropfen-43));
    	else if(T_tropfen>=620 && T_tropfen<=725.9)
    		pS_diesel=1000*Math.exp(16.209535-5810.817/(T_tropfen-43));
    	else{
			try{
				throw new MiscException("Die Formel zur Berechnung des " +
						"Saettigungsdampfdruckes fuer \"Diesel\" " +
						"ist fuer Temperaturen ueber 620K nicht zulaessig. \n" +
						"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
			}catch(MiscException me){
				me.log_Warning();
			}
			pS_diesel=get_pS_diesel(725.9);
    	}
    	return pS_diesel;
    }
    
    /**
	 * Liefert den latent heat of evaporation in J/Kg 
	 * (siehe: ELSEVIER 1290-0729)
	 * @param T_tropfen in K
	 * @return
	 */
    public double get_L(double T_tropfen){		
		double L=-1;     	
    	if(krstWahl.equalsIgnoreCase("ndodecan"))  
    		L=get_L_ndodecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("tetradecan"))
    		L=get_L_tetradecan(T_tropfen);
    	else if(krstWahl.equalsIgnoreCase("Diesel"))
    		L=get_L_diesel(T_tropfen);
    	else
    		L=get_L_nheptan(T_tropfen);    	
        return L;
    }
    
    private double get_L_nheptan(double T_tropfen){
		double L_nheptan; //latent heat of evaporation in J/Kg
		if(T_tropfen<=540.17)
		    L_nheptan=317.8e3*Math.pow((540.17-T_tropfen)/168.77,0.38);	
		else{
			try{
				throw new MiscException("Die Formel zur Berechnung der " +
						" Verdampfungsenthalpie fuer \"nheptan\" " +
						"ist fuer Temperaturen ueber 540K nicht zulaessig. \n" +
						"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
			}catch(MiscException me){
				me.log_Warning();
			}
			L_nheptan=0;	
		}
		return L_nheptan;
	}
	
	private double get_L_ndodecan(double T_tropfen){
		double L_ndodecan;
		if(T_tropfen<=659)
		    L_ndodecan=329037.62+1883.02*T_tropfen-
		               10.99644*T_tropfen*T_tropfen+
		               0.021056*T_tropfen*T_tropfen*T_tropfen-
		               1.44737e-5*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		else{
			try{
				throw new MiscException("Die Formel zur Berechnung der " +
						" Verdampfungsenthalpie fuer \"ndodecan\" " +
						"ist fuer Temperaturen ueber 540K nicht zulaessig. \n" +
						"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
			}catch(MiscException me){
				me.log_Warning();
			}
			 L_ndodecan=0;
		}
		return L_ndodecan;
	}
	
	private double get_L_tetradecan(double T_tropfen){
		double L_tetradecan;
		if(T_tropfen<=693)
		    L_tetradecan=4.7999679442e5-
		                 447.99679239*T_tropfen+
		                 1.0772809826*T_tropfen*T_tropfen-
		                 8.4415064357e-3*T_tropfen*T_tropfen*T_tropfen+
		                 2.907585478e-5*T_tropfen*T_tropfen*T_tropfen*T_tropfen-
		                 4.3509615486e-8*T_tropfen*T_tropfen*T_tropfen*T_tropfen*T_tropfen+
		                 2.1527777826e-11*T_tropfen*T_tropfen*T_tropfen*T_tropfen*T_tropfen*T_tropfen;
		else{
			try{
				throw new MiscException("Die Formel zur Berechnung der " +
						" Verdampfungsenthalpie fuer \"tetratdecan\" " +
						"ist fuer Temperaturen ueber 693K nicht zulaessig. \n" +
						"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
			}catch(MiscException me){
				me.log_Warning();
			}
			 L_tetradecan=0;
		}
		return L_tetradecan;
	}
	
	private double get_L_diesel(double T_tropfen){
		double L_diesel;
		if(T_tropfen<=725.9)
			L_diesel=254000*Math.pow((725.9-T_tropfen)/189.5,0.38);
		else{
			try{
				throw new MiscException("Die Formel zur Berechnung der " +
						" Verdampfungsenthalpie fuer \"diesel\" " +
						"ist fuer Temperaturen ueber 725K nicht zulaessig. \n" +
						"Die Tropefentemperatur betraegt aber "+T_tropfen+"K");
			}catch(MiscException me){
				me.log_Warning();
			}
			L_diesel=0;
		}
		return L_diesel;
	}
	
//	/**
//	 * Liefert die Waermeleitfaehigkeit von Luft in W/m/K
//	 * Die Werte stammen aus Vargaftik
//	 * @param T
//	 * @return
//	 */
//	public double get_waermeLeitLuft(double T){
//			return 0.0684; //Waermeleitfaehigkeit in W/m/K nach Vargaftik fuer 50bar und 600K  47.9e-3(0.0684 Wert von Xiaoling ohne Quelle)
//	}
//	
//	/**
//	 * Liefert die dynamische Viskositaet von Luft in Ns/m2
//	 * Die Werte stammen aus Vargaftik
//	 * @param T
//	 * @return
//	 */
//	public double get_dynVisLuft(double T){
//		return 4.355e-5 ; // 309.4e-7 dyn. Viskosität von Luft in Ns/m2 nach Vargaftik fuer 50bar und 600K (4.355e-5 Wert von Xiaoling ohne Quelle)
//	}

}
