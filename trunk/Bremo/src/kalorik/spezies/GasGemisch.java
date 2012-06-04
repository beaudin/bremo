package kalorik.spezies;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;
import bremoExceptions.StopBremoException;
import matLib.MatLibBase;
import misc.PhysKonst;


public class GasGemisch extends Spezies {
	
	
	private double M; 
	private Hashtable<Spezies,Double> speziesMolenBruchHash; 
	private Hashtable<Spezies,Double> speziesMolenBruchHashDetail;	
	
	public GasGemisch(String name){				
		super();
		this.name=name;		
	}
	
	
	public GasGemisch(Hashtable<Spezies,Double> speziesMolenBruchHash, String name) {	
		super();
		erzeugeGasGemsich(speziesMolenBruchHash, name);
	}
	/**
	 * Dies ist ein "Quasi-Konstruktor". 
	 * Er wird vom Konstruktor als auch von set_Gasmischung aufgerufen und erzeugt das Gasgemisch
	 * @param speziesMolenBruchHash
	 * @param name
	 */
	private void erzeugeGasGemsich(Hashtable<Spezies,Double> speziesMolenBruchHash, String name){
		Gasmischer.checkSum(speziesMolenBruchHash);
		this.speziesMolenBruchHash=speziesMolenBruchHash;
		this.name=name;
		this.M=Gasmischer.calc_M_misch(speziesMolenBruchHash);
		this.speziesMolenBruchHashDetail=berechne_DetailMolenbrueche(speziesMolenBruchHash);	
		this.isGasGemisch=true;//Variable kommt aus Superklasse	

		this.h_evap_mol= Gasmischer.calc_h_evap_misch_mol(speziesMolenBruchHash);
		this.Hu_mol=Gasmischer.calc_Hu_misch_mol(speziesMolenBruchHash);
		this.anzO_Atome=Gasmischer.calc_AnzO_Atome_misch(speziesMolenBruchHash);
		this.anzC_Atome=Gasmischer.calc_AnzC_Atome_misch(speziesMolenBruchHash);
		this.anzH_Atome=Gasmischer.calc_AnzH_Atome_misch(speziesMolenBruchHash);
		this.anzN_Atome=Gasmischer.calc_AnzN_Atome_misch(speziesMolenBruchHash);	
		this.delta_hf298_mol=Gasmischer.calc_h_misch_mol(speziesMolenBruchHash, 298.15);
	}

	private Hashtable<Spezies,Double> 
	berechne_DetailMolenbrueche(Hashtable<Spezies,Double>speziesMolenBruchHash){

		Hashtable<Spezies,Double> molenbruchDetailHash=new 	Hashtable<Spezies,Double>();		
		Enumeration<Spezies> e1;
		e1 = speziesMolenBruchHash.keys();	

		Spezies spez1, spez2;
		while(e1.hasMoreElements()){
			spez1=e1.nextElement();
			if(spez1.isGasGemisch==true){
				//ab hier wird spez1 wie ein eigenes Gasgemsich behandelt
				Hashtable<Spezies,Double> molenbruchHash2=((GasGemisch)spez1).get_speziesMolenBruecheDetail();
				Enumeration<Spezies> e2;
				e2=molenbruchHash2.keys();
				while(e2.hasMoreElements()){
					spez2=e2.nextElement();
					//diese Abfrage ist nötig um nicht Gefahr zu laufen, dass spez2 einen schon bestehenden Eintrag
					//in der Hashtable überschreibt. Dies kann dann der Fall sein wenn zwei Gase jeweils eine Spezies mit 
					//dem selben HashCode enthalten!!
					if(molenbruchDetailHash.containsKey(spez2)){
						//multiplizieren des vorhandenn SpeziesMolenbruch im zugemischten Gas mit dem Molenbruch des zugemischten Gases am GesamtGas 
						// danach Addition zum bereits vorhandenen Molenbruch
						double temp=molenbruchDetailHash.get(spez2)+molenbruchHash2.get(spez2)*speziesMolenBruchHash.get(spez1);
						molenbruchDetailHash.put(spez2,temp);
					}else{						
						double temp=molenbruchHash2.get(spez2)*speziesMolenBruchHash.get(spez1);
						molenbruchDetailHash.put(spez2,temp);						
					}
				}
				//sicher ist sicher
				e2=null;
			}
			else{
				if(molenbruchDetailHash.containsKey(spez1)){
					//dieser Fall tritt auf wenn man ein Gas mit sich selbst mischt zum Beispiel wenn 
					//ein GasGemisch aus zwei GasGemischen besteht in denen die selbe Spezies enthalten ist 
					//so kann die selbe Spezies (identischer HashCode)
					//auf zwei Wegen in das neue Gemisch kommen 			
					double temp=molenbruchDetailHash.get(spez1)+speziesMolenBruchHash.get(spez1);
					molenbruchDetailHash.put(spez1,temp);
				}else{
					molenbruchDetailHash.put(spez1,speziesMolenBruchHash.get(spez1));	
				}							
			}			
		}
		//zur Sicherheit
		e1=null;		
		spez1=spez2=null;


		//Überprüfen ob spezies mit dem selben namen aber mit unterschiedlichen Hashcodes vorhanden sind
		//Vergleich des molenbruchDetailHash mit sich selbst!
		Enumeration<Spezies> e3,e4;
		e3=molenbruchDetailHash.keys();
		e4=molenbruchDetailHash.keys();
		Spezies spez3, spez4;
		while(e3.hasMoreElements()){
			spez3=e3.nextElement();			
			while(e4.hasMoreElements()){
				spez4=e4.nextElement();
				//verhindert den Vergleich einer Spezies mit sich selber
				if(spez3!=spez4){
					if(spez3.get_name().equals(spez4.get_name())){
						double temp=molenbruchDetailHash.get(spez3)+molenbruchDetailHash.get(spez4);
						molenbruchDetailHash.put(spez3, temp);
						molenbruchDetailHash.remove(spez4);
					}						
				}					
			}			
		}		

		return molenbruchDetailHash;
	}



	private Hashtable<Spezies,Double> 
	berechne_DetailMolenbruecheToIntegrate(Hashtable<Spezies,Double>speziesMolenBruchHash){
		Hashtable<Spezies,Double> molenbruchDetailHash=new 	Hashtable<Spezies,Double>();
		if(this.isToIntegrate==true){
			molenbruchDetailHash.put(this, 1D);
		}else{

			Enumeration<Spezies> e1;
			e1 = speziesMolenBruchHash.keys();	

			Spezies spez1, spez2;
			while(e1.hasMoreElements()){
				spez1=e1.nextElement();
				if(spez1.isGasGemisch==true&&!spez1.isToIntegrate){
					//ab hier wird spez1 wie ein eigenes Gasgemsich behandelt, das nicht integriert werden soll
					Hashtable<Spezies,Double> molenbruchHash2=
						Gasmischer.massenBruch2molenBruch(((GasGemisch)spez1).get_speziesMassenBruecheDetailToIntegrate());
					Enumeration<Spezies> e2;
					e2=molenbruchHash2.keys();
					while(e2.hasMoreElements()){
						spez2=e2.nextElement();
						//diese Abfrage ist nötig um nicht Gefahr zu laufen, dass spez2 einen schon bestehenden Eintrag
						//in der Hashtable überschreibt. Dies kann dann der Fall sein wenn zwei Gase jeweils eine Spezies mit 
						//dem selben HashCode enthalten!!
						if(molenbruchDetailHash.containsKey(spez2)){
							//multiplizieren des vorhandenn SpeziesMolenbruch im zugemischten Gas mit dem Molenbruch des zugemischten Gases am GesamtGas 
							// danach Addition zum bereits vorhandenen Molenbruch
							double temp=molenbruchDetailHash.get(spez2)+molenbruchHash2.get(spez2)*speziesMolenBruchHash.get(spez1);
							molenbruchDetailHash.put(spez2,temp);
						}else{						
							double temp=molenbruchHash2.get(spez2)*speziesMolenBruchHash.get(spez1);
							molenbruchDetailHash.put(spez2,temp);						
						}
					}
					//sicher ist sicher
					e2=null;
				}
				else{
					if(molenbruchDetailHash.containsKey(spez1)){
						//dieser Fall tritt auf wenn man ein Gas mit sich selbst mischt zum Beispiel wenn 
						//ein GasGemisch aus zwei GasGemischen besteht in denen die selbe Spezies enthalten ist 
						//so kann die selbe Spezies (identischer HashCode)
						//auf zwei Wegen in das neue Gemisch kommen 			
						double temp=molenbruchDetailHash.get(spez1)+speziesMolenBruchHash.get(spez1);
						molenbruchDetailHash.put(spez1,temp);
					}else{
						molenbruchDetailHash.put(spez1,speziesMolenBruchHash.get(spez1));	
					}							
				}			
			}
			//zur Sicherheit
			e1=null;		
			spez1=spez2=null;


			//Überprüfen ob spezies mit dem selben namen aber mit unterschiedlichen Hashcodes vorhanden sind
			//Vergleich des molenbruchDetailHash mit sich selbst!
			Enumeration<Spezies> e3,e4;
			e3=molenbruchDetailHash.keys();
			e4=molenbruchDetailHash.keys();
			Spezies spez3, spez4;
			while(e3.hasMoreElements()){
				spez3=e3.nextElement();			
				while(e4.hasMoreElements()){
					spez4=e4.nextElement();
					//verhindert den Vergleich einer Spezies mit sich selber
					if(spez3!=spez4){
						if(spez3.get_name().equals(spez4.get_name())){
							double temp=molenbruchDetailHash.get(spez3)+molenbruchDetailHash.get(spez4);
							molenbruchDetailHash.put(spez3, temp);
							molenbruchDetailHash.remove(spez4);
						}						
					}					
				}			
			}		
		}
		Enumeration<Spezies> e5;
		e5=molenbruchDetailHash.keys();
		Spezies spez5;
		while(e5.hasMoreElements()){
			spez5=e5.nextElement();	
			if(!spez5.isToIntegrate){
				try{
					throw new BirdBrainedProgrammerException("Im detailierten Molenbruch fuer die Integration  "
							+"mit dem RungeKutta verfahren \n steht eine Spezies " +
									"("+spez5.get_name() +")die nicht integriert werden soll. \n " +
											"Dieser Fehler tritt auf wenn eine Spezies waehrend der Integration erzeugt wird " +
											"oder wenn die Methode integrierMich nicht aufgerufen wurde");
				}catch(BirdBrainedProgrammerException e){
					e.stopBremo();
				}
			}
		}
		
		
		
		
		return molenbruchDetailHash;

	}
	
	
	
	
	
	public void set_Gasmischung_molenBruch(Hashtable<Spezies,Double> speziesMolenBruchHash) {
		erzeugeGasGemsich(speziesMolenBruchHash,name);	
	}
	
	
	public void set_Gasmischung_massenBruch(Hashtable<Spezies,Double> speziesMassenBruchHash) {	
		erzeugeGasGemsich(Gasmischer.massenBruch2molenBruch(speziesMassenBruchHash),name);	
	}
	
	
	
	public double get_M() {		
		return  M;
	}
	
	
	
	public double get_h_mol(double T) {
		
		return Gasmischer.calc_h_misch_mol(speziesMolenBruchHash, T);
	}
	
	
	public double get_cp_mol(double T) {
		
		return Gasmischer.calc_cp_misch_mol(speziesMolenBruchHash, T);
	}
	
	
	
	public Hashtable<Spezies, Double> get_speziesMolenBrueche(){
		return speziesMolenBruchHash;		
	}
	
	public Hashtable<Spezies, Double> get_speziesMassenBrueche(){
		return Gasmischer.molenBruch2massenBruch(speziesMolenBruchHash);		
	}
	
	
	public Hashtable<Spezies, Double> get_speziesMolenBruecheDetail(){
		return speziesMolenBruchHashDetail;		
	}
	
	
	public Hashtable<Spezies, Double> get_speziesMassenBruecheDetail(){
		return Gasmischer.molenBruch2massenBruch(speziesMolenBruchHashDetail);		
	}	
	
	public Hashtable<Spezies, Double> get_speziesMassenBruecheDetailToIntegrate(){
		return Gasmischer.molenBruch2massenBruch(
				this.berechne_DetailMolenbruecheToIntegrate(speziesMolenBruchHash));		
	}
	
	
	
	
	
	
	/**
	 * <p>ABSCHÄTZUNG des Lambda-Wertes aus der Zusammensetzung der Ladung \br
	 * Liefert bei einem GasGemisch das einen Heizwert grösser als Null besitzt den Lambda-Wert. \br
	 * Luft wird betrachtet als Mischung aus O2,N2 und CO2. Ist der Verbrennungsluft
	 * AGR zugemischt werden die darin enthaltenen H O und C Atome als Teil des Kraftstoff berücksichtigt!</p>
	 * 
	 * <p>Berechnung erfolgt auf Basis der linken Seite der allgemeinen Verbrennungsgleichung: \br
	 * CxHyOz+LAM*(x+y/4-z/2)*(O2+3,773N2)-->...</p>
	 * @return Abschaetzung von lambda 
	 */
	public double get_lambda(){			
//		ABSCHÄTZUNG des Lambda-Wertes aus der Zusammensetzung der Ladung
//		Wenn der Ladung beispielsweise Wasser beigemischt ist, wird 
//		dies behandelt wie eine WasserKraftstoffemulsion.
//		Dies gilt für alle Komponenten die c,h oder o-Atome enthalten
//		Berechnung erfolgt auf Basis der linken Seite der allgemeinen Verbrennungsgleichung
//		CxHyOz+LAM*(x+y/4-z/2)*(O2+3,773N2)-->...
		double a=PhysKonst.get_vol_N2_Luft()/PhysKonst.get_vol_O2_Luft();
		double b=PhysKonst.get_vol_CO2_Luft()/PhysKonst.get_vol_O2_Luft();
		double d=1+b;

		double y=this.get_AnzH_Atome();
		double x=this.get_AnzC_Atome()-b*this.get_AnzN_Atome()/(2*a);
		double z=this.get_AnzO_Atome()-this.get_AnzN_Atome()*d/a;		
		
		return this.get_AnzN_Atome()/(2*(x+y/4-z/2)*a);
	}
	
//	public double get_LambdaTEST(){
//		//TODO funktion stimmt fuer Sauerstoffhaltigenkraftstoff nicht --> Testen
//		double o2Min=get_O2_min();
//		double lambda=1;
//		if(o2Min<0){
//			lambda=Math.abs(o2Min)/0.5*get_AnzO_Atome();
//		}else if(o2Min>0){
//			lambda=0.5*get_AnzO_Atome()/o2Min;
//		}
//		return lambda;
//	}
		
	
	public static class Gasmischer {
			
		
		public static void checkSum(Hashtable<Spezies,Double>speziesMolenMassenBruchHash){			
			Enumeration<Spezies> e = speziesMolenMassenBruchHash.keys();
			Spezies spez;
			double sum=0;
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();
				if(speziesMolenMassenBruchHash.get(spez)<0)
					try {
						throw new MiscException("Gasmischer: " +
						"Molen-/ bzw. Massenbrueche kleiner 0 \n --> Das geht nun wirklich nicht" +
						" Check: "+ spez.get_name());
					} catch (MiscException e1) {
						e1.stopBremo();
					}			
				sum=sum+speziesMolenMassenBruchHash.get(spez);
			}
			e=null;
			//Die Umwandlung von double in float verhindert, dass die Ungenauigkeit von Java (grrrrrrr) einen Fehler erzeugt wenn die Summe 
			//z.B. 0.9999999999999 ist! --> Quick and Dirty 			
			if((float)sum!=1){				
				
				e = speziesMolenMassenBruchHash.keys();
				while(e.hasMoreElements()){				
					spez=e.nextElement();
					System.out.println(spez.get_name()+ ": " +speziesMolenMassenBruchHash.get(spez));
				}
				
				try {
					throw new MiscException("Gasmischer: " + 
					"Die Summe aller Molen -/ bzw. Massenbrüche muss 1 ergeben! Sie ist aber "+ sum+ "\n --> Setzen 6");
				} catch (MiscException e1) {
					if(Double.isNaN(sum))
						e1.log_Warning();
					else
						e1.stopBremo();
				}
			}
		}
		
		
		static double calc_M_misch(Hashtable<Spezies,Double> speziesMolenBruchHash) {
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			Spezies spez;
			double M_misch=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				M_misch=M_misch+ spez.get_M()*speziesMolenBruchHash.get(spez);
			}
			
			return M_misch;
		}	
		
		
		static double calc_cp_misch_mol(Hashtable<Spezies,Double> speziesMolenBruchHash,
				double T) {	
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double cp_misch=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				cp_misch=cp_misch+ spez.get_cp_mol(T)*speziesMolenBruchHash.get(spez);
			}
			
			return cp_misch;
		}
		
		
		
		static double calc_h_misch_mol(Hashtable<Spezies,Double> speziesMolenBruchHash,
				double T) {		
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double h_misch=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				h_misch=h_misch+ spez.get_h_mol(T)*speziesMolenBruchHash.get(spez);
			}	
		
			return h_misch;
		}
			
		
		
		/**
		 * Berechnet den unteren Heizwert in [J/mol] als Mischungsgröße in Abhängigkeit der Molenbrüche
		 * @param speziesMolenBruchHash
		 * @return Hu_misch
		 */
		static double calc_Hu_misch_mol(Hashtable<Spezies,Double> speziesMolenBruchHash){			
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double Hu_misch=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				Hu_misch=Hu_misch+ spez.get_Hu_mol()*speziesMolenBruchHash.get(spez);
			}
			return Hu_misch;	
			
		}
		
		/**
		 * Berechnet die Verdampfungsenthalpie in [J/mol] als Mischungsenthalpie der Konponenten des 
		 * Gasgemsiches
		 * @param speziesMolenBruchHash
		 * @return h_evap_misch
		 */
		static double calc_h_evap_misch_mol(Hashtable<Spezies,Double> speziesMolenBruchHash){			
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double h_evap_misch=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				h_evap_misch=h_evap_misch+ spez.get_h_evap_mol()*speziesMolenBruchHash.get(spez);
			}
			return h_evap_misch;	
			
		}	
		
		/**
		 * Berechnet die Standardbildungsenthalpie in [J/mol] als Mischungsenthalpie der Konponenten des 
		 * Gasgemsiches
		 * @param speziesMolenBruchHash
		 * @return delta_hf298_misch_mol
		 */
		static double calc_delta_hf298_misch_mol(Hashtable<Spezies,Double> speziesMolenBruchHash){			
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double delta_hf298_misch_mol=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				delta_hf298_misch_mol=delta_hf298_misch_mol+ spez.get_delta_hf298_mol()*speziesMolenBruchHash.get(spez);
			}
			return delta_hf298_misch_mol;	
			
		}	
		
		
		
		static double calc_AnzC_Atome_misch(Hashtable<Spezies,Double> speziesMolenBruchHash){
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
			//Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double anzC_Atome=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				anzC_Atome=anzC_Atome+ spez.get_AnzC_Atome()*speziesMolenBruchHash.get(spez);
			}
			return anzC_Atome;	
		}
		
		
		
		static double calc_AnzH_Atome_misch(Hashtable<Spezies,Double> speziesMolenBruchHash){
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double anzH_Atome=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				anzH_Atome=anzH_Atome+ spez.get_AnzH_Atome()*speziesMolenBruchHash.get(spez);
			}
			return anzH_Atome;	
		}
		
		
		static double calc_AnzO_Atome_misch(Hashtable<Spezies,Double> speziesMolenBruchHash){
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double anzO_Atome=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				anzO_Atome=anzO_Atome+ spez.get_AnzO_Atome()*speziesMolenBruchHash.get(spez);
			}
			return anzO_Atome;	
		}
		
		static double calc_AnzN_Atome_misch(Hashtable<Spezies,Double> speziesMolenBruchHash){
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
//			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Spezies spez;
			double anzN_Atome=0;
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while(e.hasMoreElements()){				
				spez=e.nextElement();				
				anzN_Atome=anzN_Atome+ spez.get_AnzN_Atome()*speziesMolenBruchHash.get(spez);
			}
			return anzN_Atome;	
		}
		
		
		public static Hashtable<Spezies,Double> molenBruch2massenBruch(Hashtable<Spezies,Double> speziesMolenBruchHash) {
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
			Gasmischer.checkSum(speziesMolenBruchHash);
			
			Hashtable<Spezies,Double> speziesMassenbruchHash =
				new Hashtable<Spezies,Double>(speziesMolenBruchHash.size());
		
			Spezies spez;
			double M_misch=calc_M_misch(speziesMolenBruchHash);	
			
			Enumeration<Spezies> e = speziesMolenBruchHash.keys();
			
			while( e.hasMoreElements()){				
				spez=e.nextElement();
				
				speziesMassenbruchHash.put(spez, spez.get_M()/M_misch*speziesMolenBruchHash.get(spez));	
			}		
			return speziesMassenbruchHash;
		}


		public static Hashtable<Spezies,Double> 
			massenBruch2molenBruch(Hashtable<Spezies,Double> speziesMassenbruchHash) {
			
			//IllegalArgumentException wenn die Summe der Massenbrüche nicht eins ist
			Gasmischer.checkSum(speziesMassenbruchHash);
			
			Hashtable<Spezies,Double> speziesMolenenbruchHash =
				new Hashtable<Spezies,Double>(speziesMassenbruchHash.size());
			
			double temp=0, M_misch=0;
			Spezies spez;
		
			//Berechnung der scheinbaren Molarenmasse aus dem Massenbruch
			Enumeration<Spezies> e = speziesMassenbruchHash.keys();
			
			while( e.hasMoreElements()){				
				spez=e.nextElement();
//				System.out.println(spez.get_name()+": "+ spez.get_M() );
				temp=temp+speziesMassenbruchHash.get(spez)/spez.get_M();			
			}	
			
			spez=null;
			M_misch=1/temp;
			
			Enumeration<Spezies> f = speziesMassenbruchHash.keys();
			
			while( f.hasMoreElements()){				
				spez=f.nextElement();
//				System.out.println(spez.get_name());
				speziesMolenenbruchHash.put(spez, speziesMassenbruchHash.get(spez)*M_misch/spez.get_M());			
			}				
			
			return speziesMolenenbruchHash;
		}


		public static KoeffizientenSpezies erzeugeKoeffSpeziesGemisch(
				Hashtable<KoeffizientenSpezies,Double> koeffSpeziesMolenBruchHash, 
				String name,
				boolean isToIntegrate){
						
			double gemischKoeffs [][] =new double[2][9];
			double  M=0, delta_hf298_mol=0, h_evap_mol=0,Hu_mol=0;
			double [] T_grenz=new double [1]; //wird als Vektor definiert damit unten die Zuweisung zur Koeffizientenmatrix klappt
			double anzO_Atome,anzC_Atome ,anzH_Atome ,anzN_Atome ;			
			KoeffizientenSpezies spez=null;
			
			//Umkopieren der KoeffSpezies in eine Hashtable mit Spezies da sonst
			//die Funktionen von Gasmischer nicht  aufgerufen werden können			
			Hashtable<Spezies,Double> speziesMolenBruchHash = new Hashtable<Spezies,Double> ();
			Enumeration<KoeffizientenSpezies>  e = koeffSpeziesMolenBruchHash.keys();	
			while(e.hasMoreElements()){	
				spez=e.nextElement();
				speziesMolenBruchHash.put(spez,koeffSpeziesMolenBruchHash.get(spez));
			}
			
			//IllegalArgumentException wenn die Summe der Molenbrüche nicht eins ist
			Gasmischer.checkSum(speziesMolenBruchHash);
			
			
			spez=null;
			e=null;
			e = koeffSpeziesMolenBruchHash.keys();			
			while(e.hasMoreElements() ){				
				if (spez!=null && T_grenz[0]!=spez.get_T_grenz()){
					throw new IllegalArgumentException("Gasmischer.erzeugeSpeziesGemisch: " +
							"Spezies mit unterschiedlichen T_grenz können nicht berechnet werden");
				}			
				spez=e.nextElement();
				T_grenz[0]=spez.get_T_grenz();			
				//Multiplikation der Elemente der Koeffizientenmatrix mit den Molenbrüchen
				//und elementweise Addition				
				gemischKoeffs=MatLibBase.addMat(gemischKoeffs,
											spez.get_Koeffizienten(),
											koeffSpeziesMolenBruchHash.get(spez));
				}	
			
			//addieren der Grenztemperatur zur koeffizientenMatrix --> benötigt der KoeffSpezies-Konstruktor
			double [][] gemischKoeffsFINAL=new double [3][];
			
			gemischKoeffsFINAL [0]=gemischKoeffs[0];
			gemischKoeffsFINAL [1]=gemischKoeffs[1];
			gemischKoeffsFINAL [2]=T_grenz;			
			
			//Berechnen der Eingabewerte für den Konstruktor;
			M=calc_M_misch(speziesMolenBruchHash);
			delta_hf298_mol=calc_delta_hf298_misch_mol(speziesMolenBruchHash);
			h_evap_mol=calc_h_evap_misch_mol(speziesMolenBruchHash);
			Hu_mol=calc_Hu_misch_mol(speziesMolenBruchHash);
			anzO_Atome=calc_AnzO_Atome_misch(speziesMolenBruchHash);
			anzC_Atome=calc_AnzC_Atome_misch(speziesMolenBruchHash);
			anzH_Atome =calc_AnzH_Atome_misch(speziesMolenBruchHash);			
			anzN_Atome =calc_AnzN_Atome_misch(speziesMolenBruchHash);
			
			KoeffizientenSpezies  gemisch=new KoeffizientenSpezies(	gemischKoeffsFINAL,M,
													delta_hf298_mol,
													h_evap_mol,
													Hu_mol,
													anzO_Atome,
													anzC_Atome,
													anzH_Atome,
													anzN_Atome,
													name);
			return gemisch;
			
		}	
		
//		public static Spezies mischeGasmassen(double m1, Spezies s1, double m2, Spezies s2, String name){
//			
//			Hashtable <Spezies, Double> massenBruchHash= new Hashtable<Spezies, Double>(4);			
//			double m=m1+m2;
//			massenBruchHash.put(s1, m1/m);
//			massenBruchHash.put(s2, m2/m);
//			
//			Hashtable <Spezies, Double> molenBruchHash= new Hashtable<Spezies, Double>(4);	
//			molenBruchHash=massenBruch2molenBruch(massenBruchHash);
//			
//			Spezies gasGemisch = new GasGemisch(molenBruchHash,name);			
//			
//			return gasGemisch;
//			
//		}
//		
//		/**
//		 * Hier werden zwei Spezies im Verhältnis ihrer Massen gemsicht und eine Mischungsspezies (GasGemisch)
//		 * zurückgegeben. Wird der Methode ein oder zwei GasGemischSpezies übergeben so wird dies in die einzelnen Bestandteile
//		 * zerlegt und neu gemischt --> die Zusammensetzung aus den Einzelkomponenten (z.B. CO2, O2...)
//		 * des des neuen GasGemischs ist so bekannt.		 * 
//		 * @param Spezies s1 --> kann auch ein GasGemisch sein
//		 * @param double m1 --> Masse von s1 
//		 * @param Spezies s2 --> kann auch ein GasGemisch sein
//		 * @param double m2 --> Masse von s2 
//		 * @return Spezies
//		 */
//		public static Spezies mischeGasmassen_2(double m1, Spezies s1, double m2, Spezies s2, String name){
//			
//			double m=m1+m2;//Gesamtmasse des neuen GasGemischs
//			
//			Hashtable <Spezies, Double> molenBruchHash1=new Hashtable <Spezies, Double>();
//			if(s1.isGasGemisch==true){				
//				molenBruchHash1=((GasGemisch) s1).get_speziesMolenBrueche();				
//			}
//			Hashtable <Spezies, Double> massenBruchHash1=new Hashtable <Spezies, Double>();
//			massenBruchHash1=massenBruch2molenBruch(molenBruchHash1);
//			
//			Hashtable <Spezies, Double> molenBruchHash2=new Hashtable <Spezies, Double>();
//			if(s2.isGasGemisch==true){				
//				molenBruchHash2=((GasGemisch) s2).get_speziesMolenBrueche();				
//			}
//			Hashtable <Spezies, Double> massenBruchHash2=new Hashtable <Spezies, Double>();
//			massenBruchHash2=massenBruch2molenBruch(molenBruchHash2);
//			
//			//zusammenführen der identischen Spezies im massenBruchHash1 
//			//und löschen der entsprechenden Einträge im massenBruchHash2 		
//			Enumeration<Spezies> e1;
//			e1 = massenBruchHash1.keys();	
//			Enumeration<Spezies> e2;
//			e2 = massenBruchHash2.keys();
//			Spezies spez1, spez2;
//			while(e1.hasMoreElements()){
//				spez1=e1.nextElement();
//				while(e2.hasMoreElements()){
//					spez2=e2.nextElement();
//					if(spez1.get_name().equals(spez2.get_name())){
//						//bestimmen des neuen Massenbruchs
//						double xi=(massenBruchHash1.get(spez1)*m1+massenBruchHash2.get(spez2)*m2)/m;
//						massenBruchHash1.put(spez1, xi);
//						massenBruchHash2.remove(spez2);
//					}
//				}
//			}
//			//zur Sicherheit
//			spez1=spez2=null;
//			
//			//auffüllen der restlichen Spezies aus massenBruchHash2 in  massenbruchHash1
//			while(e2.hasMoreElements()){
//				spez2=e2.nextElement();
//				massenBruchHash1.put(spez2, massenBruchHash2.get(spez2));				
//			}
//			
//			
//			
//			
//			//umwandeln von massen in molenbrüche für den GasGemsich-konstruktor
//			Hashtable <Spezies, Double> molenBruchHashGasGemisch= new Hashtable<Spezies, Double>(4);	
//			molenBruchHashGasGemisch=massenBruch2molenBruch(massenBruchHash1);			
//			
//			Spezies gasGemisch = new GasGemisch(molenBruchHashGasGemisch,name);			
//			
//			return gasGemisch;
//			
//		}
//		
				
	}	






}
	


