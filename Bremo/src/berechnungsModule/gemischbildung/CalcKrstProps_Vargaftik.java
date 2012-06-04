package berechnungsModule.gemischbildung;

import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;

public class CalcKrstProps_Vargaftik extends CalcKrstProps{
	
	private String krstWahl;		
		private double koeffs[][][];
		private double M_krst;
		
		public CalcKrstProps_Vargaftik(String krstWahl){
			this.krstWahl=krstWahl;
			
			if(krstWahl.equalsIgnoreCase("ndodecan")){
				try{
					throw new BirdBrainedProgrammerException("Du musst doch tetradecan verwenden!!");	
				}catch(BirdBrainedProgrammerException e){
					e.stopBremo();				
				}
				koeffs=Koeffs_Vargaftik.get_Koeffs_ndodecan();
				this.M_krst=0.17034;  // kg/mol
			}else if(krstWahl.equalsIgnoreCase("nheptan")){
				koeffs=Koeffs_Vargaftik.get_Koeffs_nheptan();
				M_krst=0.10021; //kg/mol		
			}else if(krstWahl.equalsIgnoreCase("Diesel")){ //da fehlen noch Werte
				try{
					throw new BirdBrainedProgrammerException("Fuer Diesel wurden die Kraftstoffeigenschaften nicht definiert " +
							"--> solange sollte Tetraecan verwendet werden");	
				}catch(BirdBrainedProgrammerException e){
					e.stopBremo();				
				}
				koeffs=Koeffs_Vargaftik.get_Koeffs_diesel();
				M_krst=0.233;  // kg/mol
			}else{
	    		koeffs=Koeffs_Vargaftik.get_Koeffs_tetradecan();
	    		M_krst=0.19839; // kg/mol
			}		
		}
		
		//Molaremasse
		public double get_M_krst(){    	
	        return M_krst;
		}
		
		
	    /**
	     * Liefert die dynamische Viskositaet bei 293K
	     */
	    public double get_dynVis_krst(double T){	
		  	double vis=0;
	        try {
	        	vis= this.calc_polyFormel(T, koeffs[5]);
			} catch (MiscException e) {			
				//e.log_Warning();
				if(T>koeffs[5][1][1])
					vis= get_dynVis_krst(koeffs[5][1][1]);
				else
					vis= get_dynVis_krst(koeffs[5][1][0]);
			}
			//in ermangelung besserer Werte!!
			if(krstWahl.equalsIgnoreCase("ndodecan"))  
				vis=1.52e-3; // Pa*s
			else if(krstWahl.equalsIgnoreCase("nheptan"))
				vis=0.410e-3; // Pa*s  
			else if(krstWahl.equalsIgnoreCase("Diesel"))
				vis=3.309e-3; // Pa*s
	    	else    		 
	    		vis=2.18e-3; // Pa*s
			return vis;
		}    
	  
	    //Polynome noch nicht gut!
//	    public double get_rhoK(double T_tropfen){
//			double rhoK=-1;
//			 try {
//				 rhoK= this.calc_polyFormel(T_tropfen, koeffs[1]);
//				} catch (MiscException e) {			
//					//e.log_Warning();
//					if(T_tropfen>koeffs[1][1][1])
//						rhoK= get_rhoK(koeffs[1][1][1]);
//					else
//						rhoK= get_rhoK(koeffs[1][1][0]);
//				}		
//	        return rhoK;
//		}
	//    
	   
	    public double get_rhoK(double T_tropfen){
			double rhoK=-1;
			if(krstWahl.equalsIgnoreCase("ndodecan"))  
	    		rhoK=get_rhoK_ndodecan(T_tropfen);
			else if(krstWahl.equalsIgnoreCase("nheptan"))
				rhoK=get_rhoK_nheptan(T_tropfen);   
			else if(krstWahl.equalsIgnoreCase("Diesel"))
	    		rhoK=get_rhoK_diesel(T_tropfen);
	    	else
	    		rhoK=get_rhoK_tetradecan(T_tropfen);    		 	
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
	    
	    //Polynome noch nicht gut!
//		public double get_diff_rhoK(double T_tropfen){		
//			double diff_rhoK=-1;  		
//			try {
//				diff_rhoK= this.calc_diffPolyFormel(T_tropfen, koeffs[1]);
//				} catch (MiscException e) {			
//					//e.log_Warning();
//					if(T_tropfen>koeffs[1][1][1])
//						diff_rhoK= get_diff_rhoK(koeffs[1][1][1]);
//					else
//						diff_rhoK= get_diff_rhoK(koeffs[1][1][0]);
//				}    	
//	        return diff_rhoK;
//	    }
		
		public double get_diff_rhoK(double T_tropfen){		
			double diff_rhoK=-1;     	
	    	if(krstWahl.equalsIgnoreCase("ndodecan"))  
	    		diff_rhoK=get_diff_rhoK_ndodecan(T_tropfen);
	    	else if(krstWahl.equalsIgnoreCase("nheptan"))
	    		diff_rhoK=get_diff_rhoK_nheptan(T_tropfen);     		
	    	else if(krstWahl.equalsIgnoreCase("Diesel"))
	    		diff_rhoK=get_diff_rhoK_diesel(T_tropfen);
	    	else
	    		diff_rhoK=get_diff_rhoK_tetradecan(T_tropfen);
	    		  	
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
		 * liefert Waermekapazitaet von gasformigem Kraftstoff in J/Kg/K
		 * @param T_tropfen
		 * @return
		 */
		public double get_cp(double T_tropfen){		
			double cp=-1;     	
			try {
				cp= this.calc_polyFormel(T_tropfen, koeffs[3]);
				} catch (MiscException e) {			
					//e.log_Warning();
					if(T_tropfen>koeffs[3][1][1])
						cp= get_cp(koeffs[3][1][1]);
					else
						cp= get_cp(koeffs[3][1][0]);
				}		
	        return cp;
	    }
		
		/**
		 * liefert waermeleitfaehigkeit von Kraftstoff in W/m/K
		 * @param T_tropfen
		 * @return
		 */
		public double get_waermeLeit(double T_tropfen){		
			double k=-1; 
			try {
				k= this.calc_polyFormel(T_tropfen, koeffs[4]);
				} catch (MiscException e) {			
					//e.log_Warning();
					if(T_tropfen>koeffs[4][1][1])
						k= get_waermeLeit(koeffs[4][1][1]);
					else
						k= get_waermeLeit(koeffs[4][1][0]);
				}	
	        return k;
	    }	
		
		
		/**
		 * Liefert den Saettigungsdampfdruck von Kraftstoff in N/m2 
		 * @param T_tropfen in K
		 * @return
		 */
		public double get_pS(double T_tropfen){		
			double pS=-1;  
			try {
				pS= this.calc_polyFormel(T_tropfen, koeffs[2]);
				} catch (MiscException e) {			
					//e.log_Warning();
					if(T_tropfen>koeffs[2][1][1])
						pS= get_pS(koeffs[2][1][1]);
					else
						pS= get_pS(koeffs[2][1][0]);
				}	
	    	 	
	        return pS;
	    }
		
		/**
		 * Liefert den latent heat of evaporation in J/Kg 
		 * (siehe: ELSEVIER 1290-0729)
		 * @param T_tropfen in K
		 * @return
		 */
	    public double get_L(double T_tropfen){		
			double L=-1;     	
			try {
				L= this.calc_polyFormel(T_tropfen, koeffs[0]);
				} catch (MiscException e) {			
					//e.log_Warning();
					if(T_tropfen>koeffs[0][1][1])
						L= 0;
					else
						L= get_L(koeffs[0][1][0]);
				}    	 	
	        return L;
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
	    	else if(krstWahl.equalsIgnoreCase("nheptan"))
	    		cpl=get_cpl_nheptan(T_tropfen);      		
	    	else if(krstWahl.equalsIgnoreCase("Diesel"))
	    		cpl=get_cpl_diesel(T_tropfen);
	    	else
	    		cpl=get_cpl_tetradecan(T_tropfen);
	    		  	
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
		
		
		//Koeffizienten berechnet mit MATLAB Curve Fitting Tool aus 
		//Vargaftik-Tables_on_the_thermophysical_properties_of_liquids_and_gases
			protected static class Koeffs_Vargaftik{
				
				public static double[][][] get_Koeffs_nheptan(){
					
					double koeffs[][][] = new double [6][][];
					
					koeffs[0] = Koeffs_Vargaftik.get_koeffs_L_nheptan();
					koeffs[1] = Koeffs_Vargaftik.get_koeffs_rho_nheptan();
					koeffs[2] = Koeffs_Vargaftik.get_koeffs_pS_nheptan();
					koeffs[3] = Koeffs_Vargaftik.get_koeffs_cpf_nheptan();
					koeffs[4] = Koeffs_Vargaftik.get_koeffs_k_nheptan();
					koeffs[5] = Koeffs_Vargaftik.get_koeffs_mue_nheptan();
					
					return koeffs;
				}
				
				public static double[][][] get_Koeffs_ndodecan(){
					
					double koeffs[][][] = new double [6][][];
					
					koeffs[0] = Koeffs_Vargaftik.get_koeffs_L_ndodecan();
					koeffs[1] = Koeffs_Vargaftik.get_koeffs_rho_ndodecan();
					koeffs[2] = Koeffs_Vargaftik.get_koeffs_pS_ndodecan();
					koeffs[3] = Koeffs_Vargaftik.get_koeffs_cpf_ndodecan();
					koeffs[4] = Koeffs_Vargaftik.get_koeffs_k_ndodecan();
					koeffs[5] = Koeffs_Vargaftik.get_koeffs_mue_ndodecan();
					
					return koeffs;               
				}
				
				public static double[][][] get_Koeffs_tetradecan(){
					
					double koeffs[][][] = new double [6][][];
					
					koeffs[0] = Koeffs_Vargaftik.get_koeffs_L_tetradecan();
					koeffs[1] = Koeffs_Vargaftik.get_koeffs_rho_tetradecan();
					koeffs[2] = Koeffs_Vargaftik.get_koeffs_pS_tetradecan();
					koeffs[3] = Koeffs_Vargaftik.get_koeffs_cpf_tetradecan();
					koeffs[4] = Koeffs_Vargaftik.get_koeffs_k_tetradecan();
					koeffs[5] = Koeffs_Vargaftik.get_koeffs_mue_tetradecan();
					
					return koeffs;
				}
				
				public static double[][][] get_Koeffs_diesel(){
					
					double koeffs[][][] = new double [6][][];
					
					koeffs[0] = Koeffs_Vargaftik.get_koeffs_L_diesel();
					koeffs[1] = Koeffs_Vargaftik.get_koeffs_rho_diesel();
					koeffs[2] = Koeffs_Vargaftik.get_koeffs_pS_diesel();
					koeffs[3] = Koeffs_Vargaftik.get_koeffs_cpf_diesel();
					koeffs[4] = Koeffs_Vargaftik.get_koeffs_k_diesel();
					koeffs[5] = Koeffs_Vargaftik.get_koeffs_mue_diesel();
					
					return koeffs;
				}
				
				public static double[][][] get_Koeffs_isooktan(){
					
					double koeffs[][][] = new double [6][][];
					
					koeffs[0] = Koeffs_Vargaftik.get_koeffs_L_isooktan();
					koeffs[1] = Koeffs_Vargaftik.get_koeffs_rho_isooktan();
					koeffs[2] = Koeffs_Vargaftik.get_koeffs_pS_isooktan();
					koeffs[3] = Koeffs_Vargaftik.get_koeffs_cpf_isooktan();
					koeffs[4] = Koeffs_Vargaftik.get_koeffs_k_isooktan();
					koeffs[5] = Koeffs_Vargaftik.get_koeffs_mue_isooktan();
					
					return koeffs;
				}
				
				public static double[][][] get_Koeffs_ethanol(){
					
					double koeffs[][][] = new double [6][][];
					
					koeffs[0] = Koeffs_Vargaftik.get_koeffs_L_ethanol();
					koeffs[1] = Koeffs_Vargaftik.get_koeffs_rho_ethanol();
					koeffs[2] = Koeffs_Vargaftik.get_koeffs_pS_ethanol();
					koeffs[3] = Koeffs_Vargaftik.get_koeffs_cpf_ethanol();
					koeffs[4] = Koeffs_Vargaftik.get_koeffs_k_ethanol();
					koeffs[5] = Koeffs_Vargaftik.get_koeffs_mue_ethanol();
					
					return koeffs;
				}
				
		        public static double[][][] get_Koeffs_H2O(){
					
					double koeffs [][][] = new double [6][][];

					koeffs[0] = Koeffs_Vargaftik.get_koeffs_L_H2O();
					koeffs[1] = Koeffs_Vargaftik.get_koeffs_rho_H2O();
					koeffs[2] = Koeffs_Vargaftik.get_koeffs_pS_H2O();
					koeffs[3] = Koeffs_Vargaftik.get_koeffs_cpf_H2O();
					koeffs[4] = Koeffs_Vargaftik.get_koeffs_k_H2O();
					koeffs[5] = Koeffs_Vargaftik.get_koeffs_mue_H2O();				
					return koeffs;
					
				}

				// Koeffizienten  fuer die Verdampfungswaermeberechnung [J/kg]
				private static double [][] get_koeffs_L_nheptan(){
					double Tmin = 183;
					double Tmax = 529;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.276754547641540E-15; 
					koeffs[0][1] = 3.897412300433650E-12;
					koeffs[0][2] = -5.215171325295530E-09;
					koeffs[0][3] = 4.013257345639670E-06;
					koeffs[0][4] = -1.956662753571840E-03;
					koeffs[0][5] = 6.266278448935470E-01;
					koeffs[0][6] = -1.317924169066500E+02;
					koeffs[0][7] = 1.754924066868920E+04;
					koeffs[0][8] = -1.342372978596300E+06;
					koeffs[0][9] = 4.535691161808430E+07;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_L_ndodecan(){
					double Tmin = 273;
					double Tmax = 659;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -7.744834574662460E-26; 
					koeffs[0][1] = 3.198257939973240E-22;
					koeffs[0][2] = -5.775636016898140E-19;
					koeffs[0][3] = 5.980509079334900E-16;
					koeffs[0][4] = -3.909874170301230E-13;
					koeffs[0][5] = -1.447353273002260E-05;
					koeffs[0][6] = 2.105595318118070E-02;
					koeffs[0][7] = -1.099643172973620E+01;
					koeffs[0][8] = 1.883019162568710E+03;
					koeffs[0][9] = 3.290376571015410E+05;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_L_tetradecan(){
					double Tmin = 273;
					double Tmax = 693;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.414929269392790E-25; 
					koeffs[0][1] = 6.379220716180790E-22;
					koeffs[0][2] = -1.263865486821750E-18;
					koeffs[0][3] = 2.152922089553380E-11;
					koeffs[0][4] = -4.351066112031270E-08;
					koeffs[0][5] = 2.907635302803750E-05;
					koeffs[0][6] = -8.441662460738050E-03;
					koeffs[0][7] = 1.077311928242640E+00;
					koeffs[0][8] = -4.480003186041370E+02;
					koeffs[0][9] = 4.799969702753970E+05;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_L_diesel(){
					double Tmin = 273;
					double Tmax = 725;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -8.019142076361940E-17; 
					koeffs[0][1] = 3.406970364906480E-13;
					koeffs[0][2] = -6.361824856180320E-10;
					koeffs[0][3] = 6.850143879831440E-07;
					koeffs[0][4] = -4.685694376084810E-04;
					koeffs[0][5] = 2.110914394964350E-01;
					koeffs[0][6] = -6.261561942938830E+01;
					koeffs[0][7] = 1.179025547870700E+04;
					koeffs[0][8] = -1.278841779802120E+06;
					koeffs[0][9] = 6.126270837367290E+07;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_L_isooktan(){
					double Tmin = 173;
					double Tmax = 543;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -4.997151241362140E-16; 
					koeffs[0][1] = 1.504225673758450E-12;
					koeffs[0][2] = -1.982910128253370E-09;
					koeffs[0][3] = 1.501497793578840E-06;
					koeffs[0][4] = -7.193560287161420E-04;
					koeffs[0][5] = 2.260241818046830E-01;
					koeffs[0][6] = -4.655978617035290E+01;
					koeffs[0][7] = 6.061880167116510E+03;
					koeffs[0][8] = -4.528461143759860E+05;
					koeffs[0][9] = 1.515905121393240E+07;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_L_ethanol(){
					double Tmin = 273;
					double Tmax = 513;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -5.958319655556090E-14; 
					koeffs[0][1] = 2.090550599237170E-10;
					koeffs[0][2] = -3.242607403053430E-07;
					koeffs[0][3] = 2.918009149021230E-04;
					koeffs[0][4] = -1.678824116047720E-01;
					koeffs[0][5] = 6.403549805447220E+01;
					koeffs[0][6] = -1.619259227424210E+04;
					koeffs[0][7] = 2.617497694224160E+06;
					koeffs[0][8] = -2.454299016792220E+08;
					koeffs[0][9] = 1.017155046351400E+10;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double[][] get_koeffs_L_H2O(){
					double Tmin = 273;
					double Tmax = 641;
					double[][] koeffs = new double [2][10];
					koeffs[0][0] = -1.229719743914290E-15; 
					koeffs[0][1] = 4.764124767916510E-12;
					koeffs[0][2] = -8.131686547566430E-09;
					koeffs[0][3] = 8.023858601645540E-06;
					koeffs[0][4] = -5.043152100089510E-03;
					koeffs[0][5] = 2.093467381722090E+00;
					koeffs[0][6] = -5.739060633862880E+02;
					koeffs[0][7] = 1.001905042773810E+05;
					koeffs[0][8] = -1.010986340933290E+07;
					koeffs[0][9] = 4.521258162041150E+08;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;

				}

		////////////////////////////////////////////////////////////////////////////////////////////////////

				// Koeffizienten fuer die Dichteberechnung [kg/m³]
				private static double [][] get_koeffs_rho_nheptan(){
					double Tmin = 183;
					double Tmax = 539.5;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.174276861284360E-18; 
					koeffs[0][1] = 3.605039953424190E-15;
					koeffs[0][2] = -4.855611577269700E-12;
					koeffs[0][3] = 3.764421649963110E-09;
					koeffs[0][4] = -1.850586454947860E-06;
					koeffs[0][5] = 5.980133642456770E-04;
					koeffs[0][6] = -1.269823113258760E-01;
					koeffs[0][7] = 1.707852732062220E+01;
					koeffs[0][8] = -1.320490854329650E+03;
					koeffs[0][9] = 4.554227348311080E+04;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_rho_ndodecan(){
					double Tmin = 273;
					double Tmax = 483;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.967655679768480E-18; 
					koeffs[0][1] = 6.230090245206750E-15;
					koeffs[0][2] = -8.692477942143570E-12;
					koeffs[0][3] = 7.011152883752770E-09;
					koeffs[0][4] = -3.600777622168830E-06;
					koeffs[0][5] = 1.220362467932740E-03;
					koeffs[0][6] = -2.727395561029770E-01;
					koeffs[0][7] = 3.872498324024190E+01;
					koeffs[0][8] = -3.166873194661440E+03;
					koeffs[0][9] = 1.143664278702440E+05;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_rho_tetradecan(){
					double Tmin = 283;
					double Tmax = 1000;//523;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 1.806052340935340E-18; 
					koeffs[0][1] = -6.554639763680940E-15;
					koeffs[0][2] = 1.050599796857070E-11;
					koeffs[0][3] = -9.761269391365510E-09;
					koeffs[0][4] = 5.793844674945480E-06;
					koeffs[0][5] = -2.278400068717210E-03;
					koeffs[0][6] = 5.936304294318720E-01;
					koeffs[0][7] = -9.882249820291070E+01;
					koeffs[0][8] = 9.537813385406820E+03;
					koeffs[0][9] = -4.057799284605100E+05;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_rho_diesel(){
					double Tmin = 273;
					double Tmax = 693;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.647708905059620E-27; 
					koeffs[0][1] = 1.932269885012000E-23;
					koeffs[0][2] = -8.039496857141350E-20;
					koeffs[0][3] = 1.933108600234370E-16;
					koeffs[0][4] = -3.312509945400080E-13;
					koeffs[0][5] = 4.640237475565310E-10;
					koeffs[0][6] = -5.875031060709910E-07;
					koeffs[0][7] = 7.159768612523870E-04;
					koeffs[0][8] = -8.639583873023430E-01;
					koeffs[0][9] = 1.040834219377940E+03;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_rho_isooktan(){
					double Tmin = 183;
					double Tmax = 543;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.727272539365210E-18; 
					koeffs[0][1] = 5.404033638413970E-15;
					koeffs[0][2] = -7.407318196108760E-12;
					koeffs[0][3] = 5.834239654753880E-09;
					koeffs[0][4] = -2.908059710099010E-06;
					koeffs[0][5] = 9.507336900143620E-04;
					koeffs[0][6] = -2.037702721974240E-01;
					koeffs[0][7] = 2.759936900593450E+01;
					koeffs[0][8] = -2.143898629126140E+03;
					koeffs[0][9] = 7.361181673938750E+04;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_rho_ethanol(){
					double Tmin = 275.8;
					double Tmax = 520.3;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -8.515318701454590E-17; 
					koeffs[0][1] = 3.006643949466050E-13;
					koeffs[0][2] = -4.694540821186300E-10;
					koeffs[0][3] = 4.253953728536370E-07;
					koeffs[0][4] = -2.465138487116270E-04;
					koeffs[0][5] = 9.473262444499340E-02;
					koeffs[0][6] = -2.414005824966270E+01;
					koeffs[0][7] = 3.933133864189730E+03;
					koeffs[0][8] = -3.717784727854170E+05;
					koeffs[0][9] = 1.553412323633410E+07;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_rho_H2O(){
					double Tmin = 273;
					double Tmax = 641;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -3.479391880672900E-19; 
					koeffs[0][1] = 1.339995406068760E-15;
					koeffs[0][2] = -2.271662019542210E-12;
					koeffs[0][3] = 2.224165536873570E-09;
					koeffs[0][4] = -1.385554528068000E-06;
					koeffs[0][5] = 5.693094930555370E-04;
					koeffs[0][6] = -1.542225655720030E-01;
					koeffs[0][7] = 2.654216557898870E+01;
					koeffs[0][8] = -2.630987932306040E+03;
					koeffs[0][9] = 1.153251520351680E+05;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}
				
		////////////////////////////////////////////////////////////////////////////////////////////////////
			
				// Koeffizienten fuer die Saettigungsdruckberechnung [N/m²]
				private static double [][] get_koeffs_pS_nheptan(){
					double Tmin = 253;
					double Tmax = 540;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 9.442672746907480E-16; 
					koeffs[0][1] = -3.039132065825880E-12;
					koeffs[0][2] = 4.304447005530200E-09;
					koeffs[0][3] = -3.519648515880180E-06;
					koeffs[0][4] = 1.830268672058780E-03;
					koeffs[0][5] = -6.271370051165250E-01;
					koeffs[0][6] = 1.414448954268120E+02;
					koeffs[0][7] = -2.023049772850900E+04;
					koeffs[0][8] = 1.663979170931630E+06;
					koeffs[0][9] = -5.994490684275760E+07;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_pS_ndodecan(){
					double Tmin = 273;
					double Tmax = 659;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 1.144108233173830E-17; 
					koeffs[0][1] = -3.268783543528100E-14;
					koeffs[0][2] = 3.712981619446510E-11;
					koeffs[0][3] = -1.996566249193210E-08;
					koeffs[0][4] = 3.614706991528360E-06;
					koeffs[0][5] = 1.496319124322980E-03;
					koeffs[0][6] = -1.044697889102260E+00;
					koeffs[0][7] = 2.645439878114130E+02;
					koeffs[0][8] = -3.259375532731870E+04;
					koeffs[0][9] = 1.626237518847330E+06;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_pS_tetradecan(){
					double Tmin = 365;
					double Tmax = 695;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 5.254606005870560E-16; 
					koeffs[0][1] = -2.262672260935600E-12;
					koeffs[0][2] = 4.315184085940060E-09;
					koeffs[0][3] = -4.785349138380630E-06;
					koeffs[0][4] = 3.401668103031790E-03;
					koeffs[0][5] = -1.607721510525890E+00;
					koeffs[0][6] = 5.052595588596420E+02;
					koeffs[0][7] = -1.018195412620620E+05;
					koeffs[0][8] = 1.193921177561990E+07;
					koeffs[0][9] = -6.206678401926010E+08;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_pS_diesel(){
					double Tmin = 340;
					double Tmax = 725;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 7.419462944391300E-17; 
					koeffs[0][1] = -3.946673099737850E-13;
					koeffs[0][2] = 8.851933048672350E-10;
					koeffs[0][3] = -1.109410459155280E-06;
					koeffs[0][4] = 8.616901016382590E-04;
					koeffs[0][5] = -4.320139198629730E-01;
					koeffs[0][6] = 1.402591829120810E+02;
					koeffs[0][7] = -2.850768165183660E+04;
					koeffs[0][8] = 3.298381537805470E+06;
					koeffs[0][9] = -1.658126128346560E+08;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_pS_isooktan(){
					double Tmin = 243;
					double Tmax = 544;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 1.373876207324540E-15; 
					koeffs[0][1] = -4.958514303644690E-12;
					koeffs[0][2] = 7.862745130169140E-09;
					koeffs[0][3] = -7.187848012604980E-06;
					koeffs[0][4] = 4.173895830090730E-03;
					koeffs[0][5] = -1.596118613725710E+00;
					koeffs[0][6] = 4.018646727890270E+02;
					koeffs[0][7] = -6.423563133756640E+04;
					koeffs[0][8] = 5.915694730822460E+06;
					koeffs[0][9] = -2.392015553454410E+08;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_pS_ethanol(){
					double Tmin = 255;
					double Tmax = 516;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -2.398624791068320E-14; 
					koeffs[0][1] = 7.824181308030170E-11;
					koeffs[0][2] = -1.122854182436470E-07;
					koeffs[0][3] = 9.305964499790870E-05;
					koeffs[0][4] = -4.909059170071220E-02;
					koeffs[0][5] = 1.709698787156110E+01;
					koeffs[0][6] = -3.932326142594090E+03;
					koeffs[0][7] = 5.761471741658180E+05;
					koeffs[0][8] = -4.881084321167320E+07;
					koeffs[0][9] = 1.822373445850380E+09;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_pS_H2O(){
					double Tmin = 273;
					double Tmax = 647;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 8.357725694132160E-16; 
					koeffs[0][1] = -3.220024906073290E-12;
					koeffs[0][2] = 5.481347009437630E-09;
					koeffs[0][3] = -5.411678637079840E-06;
					koeffs[0][4] = 3.415531205061170E-03;
					koeffs[0][5] = -1.427874976517790E+00;
					koeffs[0][6] = 3.949122297085770E+02;
					koeffs[0][7] = -6.960326346470280E+04;
					koeffs[0][8] = 7.088534426706860E+06;
					koeffs[0][9] = -3.176883658513250E+08;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

		////////////////////////////////////////////////////////////////////////////////////////////////////

				// Koeffizienten fuer die Waermekapazitaetberechnung flüssig [J/kgK]
				private static double [][] get_koeffs_cpf_nheptan(){
					double Tmin = 299;
					double Tmax = 1500;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -3.732682491349620E-25; 
					koeffs[0][1] = 1.191145865951410E-20;
					koeffs[0][2] = -7.639375828822830E-17;
					koeffs[0][3] = 2.299211191266340E-13;
					koeffs[0][4] = -3.968915238578950E-10;
					koeffs[0][5] = 4.223424560140200E-07;
					koeffs[0][6] = -2.800527781249830E-04;
					koeffs[0][7] = 1.091643702132270E-01;
					koeffs[0][8] = -1.801509095917440E+01;
					koeffs[0][9] = 2.198015466019710E+03;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_cpf_ndodecan(){
					double Tmin = 289;
					double Tmax = 1500;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -3.321521325196970E-23; 
					koeffs[0][1] = 2.815139928947970E-19;
					koeffs[0][2] = -1.039452631617460E-15;
					koeffs[0][3] = 2.192437434615240E-12;
					koeffs[0][4] = -2.907998405308260E-09;
					koeffs[0][5] = 2.511781893541040E-06;
					koeffs[0][6] = -1.409077854394770E-03;
					koeffs[0][7] = 4.906526999748500E-01;
					koeffs[0][8] = -9.112630676445000E+01;
					koeffs[0][9] = 8.242390204712440E+03;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_cpf_tetradecan(){
					double Tmin = 299;
					double Tmax = 1500;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 8.620232172104920E-24; 
					koeffs[0][1] = -5.974647951619500E-20;
					koeffs[0][2] = 1.688688971163940E-16;
					koeffs[0][3] = -2.421490383249700E-13;
					koeffs[0][4] = 1.648722736310380E-10;
					koeffs[0][5] = -5.473626659261770E-09;
					koeffs[0][6] = -7.170365775842760E-05;
					koeffs[0][7] = 4.651257371906580E-02;
					koeffs[0][8] = -7.490227675752180E+00;
					koeffs[0][9] = 1.436009502901790E+03;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_cpf_diesel(){
					double Tmin = 273;
					double Tmax = 693;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -2.187147117377010E-29; 
					koeffs[0][1] = 9.476649208599910E-26;
					koeffs[0][2] = -1.810747394095090E-22;
					koeffs[0][3] = 2.002346898047630E-19;
					koeffs[0][4] = -1.412030925570780E-16;
					koeffs[0][5] = 6.584238524296390E-14;
					koeffs[0][6] = -2.029784558038710E-11;
					koeffs[0][7] = -2.959996011645680E-03;
					koeffs[0][8] = 6.329999546862710E+00;
					koeffs[0][9] = 2.640000226738180E+02;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_cpf_isooktan(){
					double Tmin = 173;
					double Tmax = 308;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 8.450339692943020E-15; 
					koeffs[0][1] = -1.860748423784490E-11;
					koeffs[0][2] = 1.813057337692920E-08;
					koeffs[0][3] = -1.025938497904830E-05;
					koeffs[0][4] = 3.715286876142040E-03;
					koeffs[0][5] = -8.928888858988120E-01;
					koeffs[0][6] = 1.424016925874090E+02;
					koeffs[0][7] = -1.453211660135310E+04;
					koeffs[0][8] = 8.610289004458090E+05;
					koeffs[0][9] = -2.256465433486700E+07;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_cpf_ethanol(){
					double Tmin = 273;
					double Tmax = 413;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 6.115836236527850E-17; 
					koeffs[0][1] = -1.994909529993220E-13;
					koeffs[0][2] = 2.885785688429200E-10;
					koeffs[0][3] = -2.429680485253990E-07;
					koeffs[0][4] = 1.312036307634980E-04;
					koeffs[0][5] = -4.712118643272810E-02;
					koeffs[0][6] = 1.125449361126300E+01;
					koeffs[0][7] = -1.723620419920610E+03;
					koeffs[0][8] = 1.535757271812740E+05;
					koeffs[0][9] = -6.064437575397660E+06;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_cpf_H2O(){
					double Tmin = 273;
					double Tmax = 633;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 7.682720569034790E-17; 
					koeffs[0][1] = -2.981808782839890E-13;
					koeffs[0][2] = 5.100044123723810E-10;
					koeffs[0][3] = -5.044304175310480E-07;
					koeffs[0][4] = 3.178900107805130E-04;
					koeffs[0][5] = -1.323503405483380E-01;
					koeffs[0][6] = 3.639894008132460E+01;
					koeffs[0][7] = -6.375717421101930E+03;
					koeffs[0][8] = 6.453799229694770E+05;
					koeffs[0][9] = -2.875830616849030E+07;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

		////////////////////////////////////////////////////////////////////////////////////////////////////

				// Koeffizienten fuer die Waermeleitfaehigkeitsberechnung [W/mK]	
				private static double [][] get_koeffs_k_nheptan(){
					double Tmin = 273;
					double Tmax = 540;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -2.061205240148690E-23; 
					koeffs[0][1] = 7.793196798711010E-20;
					koeffs[0][2] = -1.299627349114640E-16;
					koeffs[0][3] = 1.254417470219320E-13;
					koeffs[0][4] = -7.721547708052830E-11;
					koeffs[0][5] = 3.142909077635710E-08;
					koeffs[0][6] = -8.458009051240030E-06;
					koeffs[0][7] = 1.451026978178300E-03;
					koeffs[0][8] = -1.444434293919240E-01;
					koeffs[0][9] = 6.555027457612500E+00;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_k_ndodecan(){
					double Tmin = 273;
					double Tmax = 473;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 5.920530872857370E-21; 
					koeffs[0][1] = -1.992906267504130E-17;
					koeffs[0][2] = 2.968144112487780E-14;
					koeffs[0][3] = -2.566948871454880E-11;
					koeffs[0][4] = 1.420517903238670E-08;
					koeffs[0][5] = -5.215985283031000E-06;
					koeffs[0][6] = 1.270745753029350E-03;
					koeffs[0][7] = -1.980599092863480E-01;
					koeffs[0][8] = 1.791966253422590E+01;
					koeffs[0][9] = -7.168679310722960E+02;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_k_tetradecan(){
					double Tmin = 293;
					double Tmax = 513;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -4.912614301045710E-21; 
					koeffs[0][1] = 1.817111914913590E-17;
					koeffs[0][2] = -2.974559843246160E-14;
					koeffs[0][3] = 2.828143036757960E-11;
					koeffs[0][4] = -1.721004197063560E-08;
					koeffs[0][5] = 6.950645404532060E-06;
					koeffs[0][6] = -1.862931239011250E-03;
					koeffs[0][7] = 3.194974250951090E-01;
					koeffs[0][8] = -3.181300503245790E+01;
					koeffs[0][9] = 1.401282452891320E+03;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_k_diesel(){
					double Tmin = 273;
					double Tmax = 693;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -3.123777511826530E-33; 
					koeffs[0][1] = 1.030743263552610E-29;
					koeffs[0][2] = -1.311794492525070E-26;
					koeffs[0][3] = 6.815077804987170E-24;
					koeffs[0][4] = 8.794504736767370E-22;
					koeffs[0][5] = -3.164386350362340E-18;
					koeffs[0][6] = 1.849744120375990E-15;
					koeffs[0][7] = -5.387456520656340E-13;
					koeffs[0][8] = -2.129999186427790E-04;
					koeffs[0][9] = 2.089999949171750E-01;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_k_isooktan(){
					double Tmin = 290;
					double Tmax = 440;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 1.427036175905340E-20; 
					koeffs[0][1] = -4.293299517293980E-17;
					koeffs[0][2] = 5.695989753513110E-14;
					koeffs[0][3] = -4.370849357850400E-11;
					koeffs[0][4] = 2.135945087899930E-08;
					koeffs[0][5] = -6.885387869988320E-06;
					koeffs[0][6] = 1.461861360264770E-03;
					koeffs[0][7] = -1.967045298760420E-01;
					koeffs[0][8] = 1.517659972792700E+01;
					koeffs[0][9] = -5.092441472878970E+02;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_k_ethanol(){
					double Tmin = 213;
					double Tmax = 333;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -2.188888805473360E-18; 
					koeffs[0][1] = 5.461541390547270E-15;
					koeffs[0][2] = -6.038694837364830E-12;
					koeffs[0][3] = 3.883188110200780E-09;
					koeffs[0][4] = -1.600413050737810E-06;
					koeffs[0][5] = 4.383820882982210E-04;
					koeffs[0][6] = -7.980616973084800E-02;
					koeffs[0][7] = 9.310567387568780E+00;
					koeffs[0][8] = -6.316305565938720E+02;
					koeffs[0][9] = 1.898424552595870E+04;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_k_H2O(){
					double Tmin = 273;
					double Tmax = 643;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.189844430784190E-21; 
					koeffs[0][1] = 4.722633101753860E-18;
					koeffs[0][2] = -8.257658047478830E-15;
					koeffs[0][3] = 8.347454885783950E-12;
					koeffs[0][4] = -5.375602075222660E-09;
					koeffs[0][5] = 2.286799368729760E-06;
					koeffs[0][6] = -6.425596757821000E-04;
					koeffs[0][7] = 1.149780795825870E-01;
					koeffs[0][8] = -1.188385857814130E+01;
					koeffs[0][9] = 5.407157630978260E+02;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}				
				
				// Koeffizienten fuer die Viskositaetsberechnung [Ns/m²]
				private static double [][] get_koeffs_mue_nheptan(){
					double Tmin = 293;
					double Tmax = 538;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 4.774364300498310E-24; 
					koeffs[0][1] = -1.816217670233990E-20;
					koeffs[0][2] = 3.043464017365890E-17;
					koeffs[0][3] = -2.948106894235120E-14;
					koeffs[0][4] = 1.818867971644180E-11;
					koeffs[0][5] = -7.410123972927260E-09;
					koeffs[0][6] = 1.992843768852940E-06;
					koeffs[0][7] = -3.409959305441320E-04;
					koeffs[0][8] = 3.366097598067310E-02;
					koeffs[0][9] = -1.458253762082830E+00;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_mue_ndodecan(){
					double Tmin = 263;
					double Tmax = 373;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.649535857889280E-19; //-1.29418857E-19; 
					koeffs[0][1] = 4.756935147333160E-16; //3.75827924E-16;
					koeffs[0][2] = -6.086803047623360E-13; //-4.84243527E-13;
					koeffs[0][3] = 4.535679904980880E-10; //3.63340166E-10;
					koeffs[0][4] = -2.169103278372250E-07; //-1.74955776E-07;
					koeffs[0][5] = 6.903886506357950E-05; //5.60655787E-05;
					koeffs[0][6] = -1.462447292119050E-02; //-1.19567213E-02;
					koeffs[0][7] = 1.988129008217420E+00; //1.63635473E+00;
					koeffs[0][8] = -1.573945252160680E+02; //-1.30404548E+02;
					koeffs[0][9] = 5.528654148478520E+03; //4.61062483E+03;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_mue_tetradecan(){
					double Tmin = 283;
					double Tmax = 373;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -1.912489694606290E-18; //-1.91166798E-18; 
					koeffs[0][1] = 5.672449920535780E-15; //5.67001319E-15;
					koeffs[0][2] = -7.470159394996510E-12; //-7.46695165E-12;
					koeffs[0][3] = 5.732892447288370E-09; //5.73043211E-09;
					koeffs[0][4] = -2.825517512739120E-06; //-2.82430582E-06;
					koeffs[0][5] = 9.274590688781900E-04; //9.27061714E-04;
					koeffs[0][6] = -2.027506085370600E-01; //-2.02663841E-01;
					koeffs[0][7] = 2.846454705751470E+01; //2.84523816E+01;
					koeffs[0][8] = -2.328744974180380E+03; //-2.32775118E+03;
					koeffs[0][9] = 8.458914861603620E+04; //8.45531105E+04;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}
				
				private static double [][] get_koeffs_mue_diesel(){
					double Tmin = 273;
					double Tmax = 693;
					double [][] koeffs = new double [2][10];
					koeffs[0][0] = 0;
					koeffs[0][1] = 0;
					koeffs[0][2] = 0;
					koeffs[0][3] = 0;
					koeffs[0][4] = 0;                  //TODO Koeffizienten für Diesel recherchieren
					koeffs[0][5] = 0;
					koeffs[0][6] = 0;
					koeffs[0][7] = 0;
					koeffs[0][8] = 0;
					koeffs[0][9] = 0;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_mue_isooktan(){
					double Tmin = 373;
					double Tmax = 543;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -2.931900121383670E-22; 
					koeffs[0][1] = 1.198045163391110E-18;
					koeffs[0][2] = -2.171336833452250E-15;
					koeffs[0][3] = 2.290856299952260E-12;
					koeffs[0][4] = -1.550496576326710E-09;
					koeffs[0][5] = 6.981184605255530E-07;
					koeffs[0][6] = -2.091040581730320E-04;
					koeffs[0][7] = 4.017606402410780E-02;
					koeffs[0][8] = -4.493022825705490E+00;
					koeffs[0][9] = 2.228291363973870E+02;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_mue_ethanol(){
					double Tmin = 273;
					double Tmax = 513;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = 3.809567048644690E-23; 
					koeffs[0][1] = -1.378475029540400E-19;
					koeffs[0][2] = 2.203474895781530E-16;
					koeffs[0][3] = -2.041481261501800E-13;
					koeffs[0][4] = 1.207594145691730E-10;
					koeffs[0][5] = -4.727134620412120E-08;
					koeffs[0][6] = 1.223707801782190E-05;
					koeffs[0][7] = -2.018165037083330E-03;
					koeffs[0][8] = 1.921463615017270E-01;
					koeffs[0][9] = -8.028266288353660E+00;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}

				private static double [][] get_koeffs_mue_H2O(){
					double Tmin = 273;
					double Tmax = 643;
					double [][] koeffs =  new double [2][10];
					koeffs[0][0] = -2.591367971606070E-24; 
					koeffs[0][1] = 1.131277939154960E-20;
					koeffs[0][2] = -2.181723830155290E-17;
					koeffs[0][3] = 2.439624394366140E-14;
					koeffs[0][4] = -1.743288337511310E-11;
					koeffs[0][5] = 8.256653714714400E-09;
					koeffs[0][6] = -2.592694229325500E-06;
					koeffs[0][7] = 5.207335798085580E-04;
					koeffs[0][8] = -6.074547727221830E-02;
					koeffs[0][9] = 3.139571567426610E+00;
					koeffs[1][0] = Tmin;
					koeffs[1][1] = Tmax;
					return koeffs;
				}
			}
		
	}


