package berechnungsModule.gemischbildung;

import bremoExceptions.MiscException;

public abstract class  CalcKrstProps {	
	
		public abstract double get_M_krst();
		public abstract double get_dynVis_krst(double T);
		public abstract double get_rhoK(double T);
		public abstract double get_diff_rhoK(double T);
		public abstract double get_cp(double T);
		public abstract double get_cpl(double T);
		public abstract double get_waermeLeit(double T);
		public abstract double get_pS(double T);
		public abstract double get_L(double T);

		/**
		 * Liefert die Waermeleitfaehigkeit von Luft in W/m/K
		 * Die Werte stammen aus Vargaftik
		 * @param T
		 * @return
		 */
		public double get_waermeLeitLuft(double T){
			double [][] koeffs=new double[2][10];
			double []a={-3.104056485351740E-24,
					1.557560591828850E-20,
					-3.435700413667140E-17,
					4.370550460389590E-14,
					-3.531829027665410E-11,
					1.879306854579210E-08,
					-6.581668776610750E-06,
					1.462278517202310E-03,
					-1.868650523601080E-01,
					1.047924175179390E+01};
			koeffs[0]=a;
			koeffs[1][0]=300;//Tmin
			koeffs[1][1]=800;//Tmax
			
			double L=-1;     	
			try {
				L= this.calc_polyFormel(T, koeffs);
				} catch (MiscException e) {			
					//e.log_Warning();
					if(T>koeffs[1][1])
						L= get_waermeLeitLuft(koeffs[1][1]);
					else
						L= get_waermeLeitLuft(koeffs[1][0]);
				}    	 	
//	        return 0.0684;
	        return L;
		}
		
		/**
		 * Liefert die dynamische Viskositaet von Luft in Ns/m2
		 * Die Werte stammen aus Vargaftik
		 * @param T
		 * @return
		 */
		public double get_dynVisLuft(double T){
			double [][] koeffs=new double[2][10];
			double []a={1.873460681786700E-28,
					-9.582672169216470E-25,
					2.154794432749410E-21,
					-2.794600637649480E-18,
					2.302576182566790E-15,
					-1.249212263843890E-12,
					4.459845703192240E-10,
					-1.009927080674030E-07,
					1.320418106333530E-05,
					-7.456505834861580E-04};
			koeffs[0]=a;
			koeffs[1][0]=300;//Tmin
			koeffs[1][1]=800;//Tmax
			
			double L=-1;     	
			try {
				L= this.calc_polyFormel(T, koeffs);
				} catch (MiscException e) {			
					//e.log_Warning();
					if(T>koeffs[1][1])
						L= get_dynVisLuft(koeffs[1][1]);
					else
						L= get_dynVisLuft(koeffs[1][0]);
				}    	 		
				  return L;	        
		}
		
	    /**
		 * Berechnet die Kraftstoffeigenschaften mithilfe der errechneten Koeffizienten
		 * @param T
		 * @param koeffs
		 * @return Kraftstoffeigenschaften
		 * @throws MiscException 
		 */
		protected double calc_polyFormel(double T, double [][] koeffs) throws MiscException{
			double x = 0;		
				if (T<=koeffs[1][1]&&T>=koeffs[1][0]){  
					x = koeffs[0][0] * T * T * T * T * T * T * T * T * T;
					x = x + koeffs[0][1] * T * T * T * T * T * T * T * T;
					x = x + koeffs[0][2] * T * T * T * T * T * T * T;
					x = x + koeffs[0][3] * T * T * T * T * T * T;
					x = x + koeffs[0][4] * T * T * T * T * T;
					x = x + koeffs[0][5] * T * T * T * T;
					x = x + koeffs[0][6] * T * T * T;
					x = x + koeffs[0][7] * T * T;
					x = x + koeffs[0][8] * T;
					x = x + koeffs[0][9];}
				else{				
				throw new MiscException("Die Formel zur Berechnung " + 
				"ist fuer Temperaturen unter " +koeffs[1][0]+ " und ueber " +koeffs[1][1]+ "K  nicht zulaessig. \n" +
				"Die Temperatur betraegt aber "+T+"K");			
				}		
			return x;
		}
		

		/**
		 * Berechnet die Ableitung der Kraftstoffeigenschaften mithilfe der errechneten Koeffizienten
		 * @param T
		 * @param koeffs
		 * @return Kraftstoffeigenschaften
		 * @throws MiscException 
		 */
		protected double calc_diffPolyFormel(double T, double [][] koeffs) throws MiscException{
			double x = 0;		
				if (T<=koeffs[1][1]&&T>=koeffs[1][0]){  
					x = 9*koeffs[0][0] * T * T * T * T * T * T * T * T ;
					x = x + 8*koeffs[0][1] * T * T * T * T * T * T * T ;
					x = x + 7*koeffs[0][2] * T * T * T * T * T * T ;
					x = x + 6*koeffs[0][3] * T * T * T * T * T ;
					x = x + 5*koeffs[0][4] * T * T * T * T ;
					x = x + 4*koeffs[0][5] * T * T * T ;
					x = x + 3*koeffs[0][6] * T * T ;
					x = x + 2*koeffs[0][7] * T ;
					x = x + 1*koeffs[0][8] ;}
				else{				
				throw new MiscException("Die Formel zur Berechnung " + 
				"ist fuer Temperaturen unter " +koeffs[1][0]+ " und ueber " +koeffs[1][1]+ "K  nicht zulaessig. \n" +
				"Die Temperatur betraegt aber "+T+"K");			
				}		
			return x;
		}
			
}
