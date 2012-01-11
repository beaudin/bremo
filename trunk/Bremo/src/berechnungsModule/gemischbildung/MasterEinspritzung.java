package berechnungsModule.gemischbildung;


import java.util.Hashtable;

import berechnungsModule.Berechnung.Zone;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import kalorik.spezies.GasGemisch;
import kalorik.spezies.KoeffizientenSpeziesFabrik;
import kalorik.spezies.Spezies;

public class MasterEinspritzung{

	protected final CasePara CP;
	protected int anzEinspr;
	private static MasterEinspritzung me=null;
	private static MasterEinspritzung meLWA=null;

	//Vielleicht kann hier noch eine extra Klasse verwendet werden, ist aber doppelt gemoppelt. 
	protected final Einspritzung[] einspritzungen; 

	private MasterEinspritzung(CasePara cp,boolean isLWA){
		this.CP=cp;	
		anzEinspr=CP.get_AnzahlEinspritzungen(); //untere Grenze liegt bei eins!

		einspritzungen= new Einspritzung [anzEinspr];	
		for(int index=0;index<anzEinspr;index++){
			if(isLWA)
				einspritzungen[index]=Einspritzung.get_Instance_LWA(cp, index+1); //Im Inputfile beginnen die Einspritzugnen mit eins!
			else
				einspritzungen[index]=Einspritzung.get_Instance(cp, index+1); //Im Inputfile beginnen die Einspritzugnen mit eins!
		}

	}

	/**
	 * Erzeugt die Mastereinspritzung fuer die Druckverlaufsanalyse (DVA)
	 * @param cp
	 * @return
	 */
	public static MasterEinspritzung get_Instance(CasePara cp){
		if(me==null){
			me=new MasterEinspritzung(cp,false);
		}
		return me;		
	}


	/**
	 * Erzeugt die Mastereinspritzung fuer die Ladungswechselanalyse (LWA)
	 * Fuer die DVA und die LWA sind zwei verschiedenen Mastereinspritzungen notwendig da 
	 * sont die Mastereinspritzung der DVA schon Werte der ME der LWA beinhaltet
	 * @param cp
	 * @return
	 */
	public static MasterEinspritzung get_Instance_LWA(CasePara cp){
		if(meLWA==null){
			meLWA=new MasterEinspritzung(cp,true);
		}
		return meLWA;		
	}


	/**
	 * Liefert ein Objekt vom Typ Einspritzung. 
	 * 
	 * @param index --> gibt an um welche Einspritzung es sich handelt. 
	 * Die Nummerierung der Einspritzungen beginnt bei 1
	 * @return Objekt vom Typ Einspritzung
	 */
	public Einspritzung get_Einspritzung(int index){
		return einspritzungen[index];
	}


	public Einspritzung [] get_AlleEinspritzungen(){
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
				if(einspritzungen[index].isLWA==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ " wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}				
				}
			}else{
				double dmKrst=einspritzungen[index].get_diff_mKrst_dampf(time, zn[posZn]);
				zn[posZn].set_dm_ein(dmKrst, einspritzungen[index].get_Tkrst_dampf(time, zn[posZn]), einspritzungen[index].get_Krst());
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
				if(einspritzungen[index].isLWA==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ 
								" wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}
				}
			}else{			
				dQ=einspritzungen[index].get_dQ_krstDampf(time, zn[posZn]);
				zn[posZn].set_dQ_ein_aus(-1*dQ);
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
				if(einspritzungen[index].isLWA==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +index+ 
								" wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}
				}
			}else{			
				dQ=dQ+einspritzungen[index].get_dQ_krstDampf(time, zn[posZn]);
			}
		}	
		return dQ;
	}
	
	



	public  void berechneIntegraleGroessen(double time, Zone[] zn){		
		int posZn = -1;
		for(int index=0;index<anzEinspr;index++){
			for(int x=0;x<zn.length;x++){ //Herausfinden zu welcher Zone die Einspritzung gehˆrt
				if(zn[x].getID()==einspritzungen[index].get_ID_Zone())
					posZn=x;
			}
			if(posZn==-1){
				if(einspritzungen[index].isLWA==false){
					try{
						throw new BirdBrainedProgrammerException("Fuer Einspritzung " +(index+1)+ " wurde die ID der Zone in die eingespritzt wrid "
								+"nicht richtig definiert --> Programmierfehler im Berechnungsmodell");
					}catch(BirdBrainedProgrammerException bbpE){					
						bbpE.stopBremo();				
					}
				}
			}else{				
				einspritzungen[index].berechneIntegraleGroessen(time,zn[posZn]);
			}
		}				

	}	

	/**
	 * <p>Liefert die gesamte Kraftsoffmasse  die im Arbeitsspiel eingespritzt wird in [kg]. </p> 
	 * @return gesamte Kraftstoffmasse (aller Einspritzungen) 
	 */
	public  double get_mKrst_Sum_ASP(){
		double sum=0;
		for(int index=0;index<anzEinspr;index++){
			sum+=einspritzungen[index].get_mKrst_ASP();
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
				krstMassenbruchHash.put(einspritzungen[index].get_Krst(), 
						einspritzungen[index].get_mKrst_verdampft(time)/mKrstDampf_ges_t);
			}	
			GasGemisch mixKrst=new GasGemisch("KraftstoffMix");
			mixKrst.set_Gasmischung_massenBruch(krstMassenbruchHash);

			return mixKrst;
		}else
			return KoeffizientenSpeziesFabrik.get_spezCO2(); //Damit was zur¸ckgegeben wird!
	}	


	/**
	 * <p>Liefert ein Objekt vom Typ Spezies zurueck, das durch die Mischung 
	 * aller Kraftstoffe der </br>
	 * einzelenen Eispritzungen in die Zone mit der ID zonenID gebildet wird. </p>
	 * @param zonenID
	 * @return Objekt vom Typ Spezies
	 */
	public  Spezies get_spezKrstALLZone(int zonenID){
		double mKrstDampf_ges=get_mKrst_Sum_ASP();
		Hashtable<Spezies, Double> krstMassenbruchHash=new Hashtable<Spezies, Double>();		

		for(int index=0;index<anzEinspr;index++){
			if(einspritzungen[index].get_ID_Zone()==zonenID)
				krstMassenbruchHash.put(einspritzungen[index].get_Krst(), 
						einspritzungen[index].get_mKrst_ASP()/mKrstDampf_ges);
		}	
		GasGemisch mixKrst=new GasGemisch("KraftstoffMix");
		mixKrst.set_Gasmischung_massenBruch(krstMassenbruchHash);

		return mixKrst;
	}	




	/**
	 * <p>Liefert ein Objekt vom Typ Spezies zurueck, das durch die Mischung 
	 * aller Kraftstoffe der </br>
	 * einzelenen Eispritzungen gebildet wird. </p>
	 * @return Spezies krst 
	 */
	public  Spezies get_spezKrstALL(){
		double mKrstDampf_ges=get_mKrst_Sum_ASP();
		Hashtable<Spezies, Double> krstMassenbruchHash=new Hashtable<Spezies, Double>();		

		for(int index=0;index<anzEinspr;index++){
			krstMassenbruchHash.put(einspritzungen[index].get_Krst(), 
					einspritzungen[index].get_mKrst_ASP()/mKrstDampf_ges);
		}	
		GasGemisch mixKrst=new GasGemisch("KraftstoffMix");
		mixKrst.set_Gasmischung_massenBruch(krstMassenbruchHash);

		return mixKrst;
	}	

}
