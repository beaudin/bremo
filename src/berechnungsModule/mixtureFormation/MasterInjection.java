package berechnungsModule.mixtureFormation;


import java.util.Hashtable;

import berechnungsModule.ModuleFactory;
import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.MiscException;
import kalorik.GasMixture;
import kalorik.Spezies;
import kalorik.SpeciesFactory;

public class MasterInjection extends ModuleFactory{
	
	protected int anzEinspr;	
	
	public static final String INJECTION_MODEL_FLAG="injectionModel_"; //gibt den Eintrag im inputfile an
	public  final static String[] VALID_MODELS={PortFuelInjection.FLAG,
															PortFuelInjection.FLAG2, 
															Spray.FLAG,
															SimpleDirectInjection.FLAG};	
	

	//Vielleicht kann hier noch eine extra Klasse verwendet werden, ist aber doppelt gemoppelt. 
	protected final Injection[] einspritzungen; 

	public  MasterInjection(CasePara cp){
		super(cp);
		anzEinspr=CP.get_nbrOfInjections(); 

		einspritzungen= new Injection [anzEinspr];	
		for(int index=0;index<anzEinspr;index++){
			einspritzungen[index]=get_Einzeleinspritzung(cp, index); 
		}

	}

	
	private  Injection get_Einzeleinspritzung(CasePara cp, int index){
		Injection einspritzung=null;	
		String modulFlag=INJECTION_MODEL_FLAG+index;;

		
		 String einspritzungsModellVorgabe=
			get_ModulWahl(modulFlag, VALID_MODELS);

		if(einspritzungsModellVorgabe.equals(PortFuelInjection.FLAG)|| 
				einspritzungsModellVorgabe.equals(PortFuelInjection.FLAG2)){
			
			einspritzung=(Injection) new PortFuelInjection(cp, index);

		}else if(einspritzungsModellVorgabe.equals(Spray.FLAG)){
			einspritzung=new Spray(cp, index);	
			
		}else if(einspritzungsModellVorgabe.equals(SimpleDirectInjection.FLAG)){			
			einspritzung=new SimpleDirectInjection(cp, index);			
		}

		if(einspritzung==null){
			try {
				throw new BirdBrainedProgrammerException(
						"Das Einspritzungsmodell \"" +einspritzungsModellVorgabe + 
						" \" wurde im InputFile " +
						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n" +
				"Es fehlt der entsprechende else-if-Block oder das  Modell wurde noch nicht implementiert");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
		}
		return einspritzung;		
	}	
	


	/**
	 * Liefert ein Objekt vom Typ Einspritzung. 
	 * 
	 * @param index --> gibt an um welche Einspritzung es sich handelt. 
	 * Die Nummerierung der Einspritzungen beginnt bei 1
	 * @return Objekt vom Typ Einspritzung
	 */
	public Injection get_injection(int index){
		if(index>=einspritzungen.length){
			try{
				throw new MiscException("The given Index for the main injection"+ index + 
						" exceeds the number of injections("+einspritzungen.length+")");
			}catch(MiscException me){
				me.stopBremo();
			}
		}			
		return einspritzungen[index]; 
	}


	public Injection [] get_allInjections(){
		return einspritzungen;
	}	


	/**
	 * Liefert den dampffoermigen Teil des zum Zeitpunkt time [sec n. Rechnungsbeginn] 
	 * in die Zone mit der ID "zonenID" 
	 * bereits eingespritzten Krafstoffs (aller Einspritzungen). 
	 * Der ausgegebene Wert ist nur bei  Saugrohreinspritzung korrekt, da die integration nach dem 
	 * eulerverfahren erfolgt und er Fehler dann relativ Groﬂ wird. 
	 * Hier sollte generell die Behandlung der Einspritzung angepasst werden...
	 * @param time
	 * @return
	 */
	public  double get_mKrst_dampffoermig_Sum_Zone(double time, int zonenID){
		double sum=0;
		for(int index=0;index<anzEinspr;index++){
			if(einspritzungen[index].get_ID_Zone()==zonenID)
				sum+=einspritzungen[index].get_mKrst_verdampft(time);
		}		
		return sum;
	}

	public  double get_kinEnergyFlux(double time, double pCyl){
		double sum=0;
		for(int index=0;index<anzEinspr;index++){			
				sum+=einspritzungen[index].get_kineticEnergyFlux(time, pCyl);
		}		
		return sum;
	}


	/**
	 * Fuehrt den Zonen automatisch die Menge an Krst zu die das jeweilige Einspritzmodell vorgibt.
	 * Es ist unerheblich in welche Zone der Kraftstoff eingespritzt wird 
	 * @param time
	 * @param zn
	 * @return
	 */	 
	public  Zone [] fuehre_diff_mKrst_dampffoermig_zu(double time,Zone [] zn){

		int posZn = -1;
		for(int index=0;index<anzEinspr;index++){
			for(int x=0;x<zn.length;x++){
				if(zn[x].getID()==einspritzungen[index].get_ID_Zone())
					posZn=x;
			}
			if(posZn==-1){
				if(this.isLWA(time)==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ " wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}				
				}else if(einspritzungen[index].IS_LWA_EINSPRITZUNG){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ " wurde die ID der Zone in die eingespritzt wrid "
								+"fuer die LWA nicht richtig definiert.");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}	
				}
					
			}else{
				double dmKrst=einspritzungen[index].get_diff_mFuel_vapor(time, zn[posZn]);
				if(dmKrst>0)
					zn[posZn].set_dm_in(dmKrst, einspritzungen[index].get_T_fuelVapor(time, zn[posZn]), einspritzungen[index].get_fuel());
			}
		}
		return zn;
	}		


	/**
	 * <p>Berechnet den Waermestrom der den Zonen aufgrund der Verdampfung entzogen wird. Diese
	 * Funktion veraendert die Zonen indem set_dQ_ein_aus aufgerufen wird! </p>
	 * @param time
	 * @param Zone [] zn 
	 */
	public Zone [] entnehme_dQ_krstDampf(double time, Zone []zn){
		double dQ=0;
		int posZn = -1;
		for(int index=0;index<anzEinspr;index++){
			for(int x=0;x<zn.length;x++){
				if(zn[x].getID()==einspritzungen[index].get_ID_Zone())
					posZn=x;
			}
			if(posZn==-1){
				if(this.isLWA(time)==false){
					System.out.println(CP.convert_SEC2KW(time));
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ 
								" wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}
				}else if(einspritzungen[index].IS_LWA_EINSPRITZUNG){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ " wurde die ID der Zone in die eingespritzt wrid "
								+"fuer die LWA nicht richtig definiert.");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}	
				}
			}else{			
				dQ=einspritzungen[index].get_dQ_fuelVapor(time, zn[posZn]);
				zn[posZn].set_dQ_in_out(-1*dQ);
			}
		}	
		return zn;
	}
	
	
	/**
	 * <p>Berechnet den Waermestrom der den Zonen aufgrund der Verdampfung entzogen wird und gibt 
	 * die Summe des Waermestoms fuer alle 
	 * Zonen zurueck </p>
	 * @param time
	 * @param Zone [] zn 
	 */
	public double get_dQ_krstDampf(double time, Zone []zn){
		double dQ=0;
		int posZn = -1;
		for(int index=0;index<anzEinspr;index++){
			for(int x=0;x<zn.length;x++){
				if(zn[x].getID()==einspritzungen[index].get_ID_Zone())
					posZn=x;
			}
			if(posZn==-1){
				if(this.isLWA(time)==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ 
								" wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}
			}else if(einspritzungen[index].IS_LWA_EINSPRITZUNG){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ " wurde die ID der Zone in die eingespritzt wrid "
								+"fuer die LWA nicht richtig definiert.");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}	
				}
			}else{			
				dQ=dQ+einspritzungen[index].get_dQ_fuelVapor(time, zn[posZn]);
			}
		}	
		return dQ;
	}
	
	/**
	 * <p>Berechnet den Massenstrom der den Zonen aufgrund der Verdampfung zugefuehrt wird und gibt 
	 * die Summe des Massenstroms fuer alle 
	 * Zonen zurueck </p>
	 * @param time
	 * @param Zone [] zn 
	 */
	public double get_dm_krstDampf(double time, Zone []zn){
		double dm=0;
		int posZn = -1;
		for(int index=0;index<anzEinspr;index++){
			for(int x=0;x<zn.length;x++){
				if(zn[x].getID()==einspritzungen[index].get_ID_Zone())
					posZn=x;
			}
			if(posZn==-1){
				if(this.isLWA(time)==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ 
								" wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}
			}else if(einspritzungen[index].IS_LWA_EINSPRITZUNG){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ " wurde die ID der Zone in die eingespritzt wrid "
								+"fuer die LWA nicht richtig definiert.");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}	
				}
			}else{			
				dm=dm+einspritzungen[index].get_diff_mFuel_vapor(time, zn[posZn]);
			}
		}	
		return dm;
	}
	
	



	public  void berechneIntegraleGroessen(double time, Zone[] zn){		
		int posZn = -1;
		for(int index=0;index<anzEinspr;index++){
			for(int x=0;x<zn.length;x++){ //Herausfinden zu welcher Zone die Einspritzung gehˆrt
				if(zn[x].getID()==einspritzungen[index].get_ID_Zone())
					posZn=x;
			}
			if(posZn==-1){
				if(this.isLWA(time)==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +(index+1)+ " wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}	
				}else if(einspritzungen[index].IS_LWA_EINSPRITZUNG){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ " wurde die ID der Zone in die eingespritzt wrid "
								+"fuer die LWA nicht richtig definiert.");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}	
				}
			}else{				
				einspritzungen[index].calculateIntegralvalues(time,zn[posZn]);
			}
		}				

	}	

	/**
	 * <p>Liefert die gesamte Kraftsoffmasse  die im Arbeitsspiel eingespritzt wird in [kg]. </p> 
	 * @return gesamte Kraftstoffmasse (aller Einspritzungen) 
	 */
	public  double get_mFuel_Sum_Cycle(){
		double sum=0;
		for(int index=0;index<anzEinspr;index++){
			sum+=einspritzungen[index].get_mFuel_Cycle();
		}		
		return sum;
	}



	/**
	 * <p>Liefert ein Objekt vom Typ Spezies zurueck, das durch die Mischung der zur Zeit 
	 * time [s n. Rechenbeginn] in der Zone mit der ID zonenID 
	 * bereits verdampften Kraftstoffe gebildet wird. </p>
	 * @return Spezies krst 
	 */
	public  Spezies get_spezKrst_verdampft(double time, int zonenID){
		double mKrstDampf_ges_t=this.get_mKrst_dampffoermig_Sum_Zone(time, zonenID);
		if(mKrstDampf_ges_t>0){
			Hashtable<Spezies, Double> krstMassenbruchHash=new Hashtable<Spezies, Double>();	
			for(int index=0;index<anzEinspr;index++){
				if(einspritzungen[index].get_ID_Zone()==zonenID){
					double m=einspritzungen[index].get_mKrst_verdampft(time);
					Spezies spez=einspritzungen[index].get_fuel();	
					if(krstMassenbruchHash.containsKey(spez)){
						krstMassenbruchHash.put(spez, krstMassenbruchHash.get(spez)+m/mKrstDampf_ges_t);			
					}else{				
						krstMassenbruchHash.put(spez, m/mKrstDampf_ges_t);	
					}
				}
			}	
			GasMixture mixKrst=new GasMixture("KraftstoffMix");
			mixKrst.set_Gasmischung_massenBruch(krstMassenbruchHash);

			return mixKrst;
		}else
//			return CP.SPEZIES_FABRIK.get_spezCO2(); //Damit was zur¸ckgegeben wird!
			return null;
	}



	/**
	 * <p>Liefert ein Objekt vom Typ Spezies zurueck, das durch die Mischung 
	 * aller Kraftstoffe der </br>
	 * einzelenen Eispritzungen gebildet wird. </p>
	 * @return Spezies krst 
	 */
	public  Spezies get_spezKrstALL(){
		double mKrstDampf_ges=get_mFuel_Sum_Cycle();
		if(mKrstDampf_ges>0){
			Hashtable<Spezies, Double> krstMassenbruchHash=new Hashtable<Spezies, Double>();	
			for(int index=0;index<anzEinspr;index++){
				double m=einspritzungen[index].get_mFuel_Cycle();
				Spezies spez=einspritzungen[index].get_fuel();	
				if(krstMassenbruchHash.containsKey(spez)){
					krstMassenbruchHash.put(spez, krstMassenbruchHash.get(spez)+m/mKrstDampf_ges);			
				}else{				
					krstMassenbruchHash.put(spez, m/mKrstDampf_ges);	
				}
			}	
			GasMixture mixKrst=new GasMixture("KraftstoffMix");
			mixKrst.set_Gasmischung_massenBruch(krstMassenbruchHash);
			return mixKrst;
		} else
			return null;		
	}	
	
	
	private boolean isLWA(double time){
		boolean a=false;		
		if(time>=CP.get_EVO())// &&time<=CP.get_Einlassschluss()+CP.SYS.DAUER_ASP_SEC --> fuehrt aufgrund eines Rundungsfehlers zu einem Fahler grrrrrr
			a=true;

		return a; 
	}

}
