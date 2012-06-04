/**
 * 
 */
package kalorik.spezies;

import java.util.Hashtable;
import java.util.Vector;

import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremo.parameter.CasePara.MakeMeUnique;
import bremoExceptions.MiscException;

import kalorik.spezies.GasGemisch.Gasmischer;
import misc.HeizwertRechner;
import misc.PhysKonst;

/**
 * @author eichmeier
 *
 */
public class SpeziesFabrik {		
	// TODO Heizwerte recherchieren und eintragen
	//damit der Vektor mit den zu integrierenden Spezies nicht ueberfluessiferweise mit 
	//doppelten Grundspezies gefuellt ist wird jede Spezies nur einmal erzeugt!
	
	private Spezies spezH;
	private Spezies spezO;
	private Spezies spezN;
	private Spezies spezH2;
	private Spezies spezOH;
	private Spezies spezCO;
	private Spezies spezNO;
	private Spezies spezO2;
	private Spezies spezH2O;
	private Spezies spezCO2;
	private Spezies spezN2;
	private Spezies spezAr;
	private Spezies spezLuftTr;
	private Spezies spezRON_91;
	private Spezies spezRON_95;
	private Spezies spezRON_98;
	private Spezies spezDiesel;
	
//	private  Spezies [] allSpez =new Spezies[25]; //erstmal nicht mehr als 25 Spezies bitte
	private  int nmbrOfSpez=0; //dann wirds bei der ersten Spezies null

	public final boolean STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD;

	private final CasePara CP;
	private Hashtable<Spezies,Integer> allSpez;
	private Vector<Spezies> allSpezVec;

	
	public SpeziesFabrik(CasePara cp, MakeMeUnique mmu){	
		CP=cp;
		STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD=CP.SYS.STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD;
		allSpez=new Hashtable<Spezies,Integer>();
		allSpezVec =new Vector<Spezies>();
	}
	
	
	public void integrierMich(Spezies spez){	
		if(allSpez.containsKey(spez)==false){
			nmbrOfSpez=nmbrOfSpez+1;
			int index=nmbrOfSpez-1;
			allSpez.put(spez,index); //Speichern der grade erzeugten Spezies im Vektor
			allSpezVec.add(index,spez);
			spez.set_isTointegrate(true);
		}
	}	
	
	public boolean isToIntegrate(Spezies spez){
		if(allSpez.containsKey(spez)&&spez.isToIntegrate)
			return true;
		else if(!allSpez.containsKey(spez)&&!spez.isToIntegrate) 
			return false;
		else{
			try{
				throw new MiscException("Bei Spezies "+ spez.get_name()+" stimmt die" +
						"Belegung von isTointegrate nicht+ \n" +
						"SpeziesFabrik und Spezis haben unterschiedliche Werte");
			}catch(MiscException me){
				me.stopBremo();
				
			}
			return false;
		}
	}
	
	public  int get_NmbrOfAllSpez(){
		return nmbrOfSpez;
	}
	
	public int get_indexOf(Spezies spez){		
		return allSpez.get(spez);
	}
	
	public  Spezies get_Spez(int i){
		//TODO Fehlerabfrage einbauen fuer indexOutOfBounds		
		return allSpezVec.get(i);		
	}
	

	/**
	 * <p>Diese Methode erstellt eine Hashtable mit allen Kraftstoffen (KoeffSpezies mit Heizwert Hu>0). 
	 * Die KrstApproxSpezies ist nicht enthalten! 
	 * Soll ein neuer Kraftstoff fuer die Rechnung zur Verfuegung stehen,  MUSS er hier eingefuegt werden! </p>	
	 * Diese Art der Kraftstoffzuweisung ist nicht schnell aber sicher, da  
	 * ein neuer Krafstoff nur an EINER Stelle, naemlich HIER,  eingebunden werden muss.
	 * --> safty first
	 * @return Hashtable<String,Spezies> krstHash
	 */
	public Hashtable<String,Spezies> get_alleKrafstoffe(){
		Hashtable<String,Spezies> krstHash=new Hashtable<String,Spezies>();
		krstHash.put(get_spezH().get_name(), get_spezH());
		krstHash.put(get_spezH2().get_name(), get_spezH2());
		krstHash.put(get_spezCO().get_name(), get_spezCO());
		krstHash.put(get_spezRON_91().get_name(), get_spezRON_91());
		krstHash.put(get_spezRON_95().get_name(), get_spezRON_95());
		krstHash.put(get_spezRON_98().get_name(), get_spezRON_98());
		krstHash.put(get_spezDiesel().get_name(), get_spezDiesel());

		return krstHash;
	}

	/**
	 * Spezies H wird automatisch integriert 
	 * @return KoeffizientenSpezies spezH
	 */
	public Spezies get_spezH() {	

		if(spezH==null){

			double hf;	
			//Hu gerechnet 
			double hu=338905;
			//Berechnung der Standerdbildungsenthalpie passend zum Heizwert
			//if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) --hf=>218000
			//else (also nach deJaegher und Grill etc.  hf=338905;	
			hf=HeizwertRechner.deltaHf4Hu(CP,0, 1, 0,hu );

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_H_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_H_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_H_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_H_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}
			//Heizwert berechnet --> ziemlich hoch
			spezH=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_H(),								
					hf,-10e10,hu,0,0,1,0,"H");	//TODO Verdampfungsenthalpie + Heizwert korrigieren
			
			this.integrierMich(spezH);
		}
		
		return spezH;
	}

	/**
	 * Spezies O wird automatisch integriert 
	 * @return KoeffizientenSpezies spezO
	 */
	public Spezies get_spezO() {	
		
		if(spezO==null){
			double hf=249200; //kein Unterschied

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_O_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_O_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_O_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_O_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}
			spezO=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_O(),								
					hf,-10e10,0,1,0,0,0,"O"); //TODO Verdampfungsenthalpie korrigieren
			
			this.integrierMich(spezO);
		}
		
		return spezO;
	}

	/**
	 * Spezies N wird automatisch integriert 
	 * @return KoeffizientenSpezies spezN
	 */
	public Spezies get_spezN() {

		if(spezN==null){

			double hf=472680; //kein Unterschied

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_N_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_N_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_N_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_N_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}

			spezN=new KoeffizientenSpezies(	koeffs,
					PhysKonst.get_M_N(),								
					hf,-10e10,0,0,0,0,1,"N");
			
			this.integrierMich(spezN);
		}

		return spezN;
	}

	/**
	 * Spezies H2 wird automatisch integriert 
	 * @return KoeffizientenSpezies spezH2
	 */
	public Spezies get_spezH2() {
		
		if(spezH2==null){				
			
			//Hu gerechnet --> stimmt weitgehend mit Wert 
			//aus Pischinger Thermodynamik der... ueberein (241700) Abweichung is ok!
			double hu=241810;
			//Berechnung der Standerdbildungsenthalpie passend zum Heizwert
			//if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) --hf=>0
			//else hf=241810;
			double hf;
			hf=HeizwertRechner.deltaHf4Hu(CP,0, 2, 0,hu );

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_H2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_H2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_H2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_H2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}
	
			spezH2=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_H2(),								
					hf,-10e10,hu,0,0,2,0,"H2");//TODO Verdampfungsenthalpie korrigieren
			
			this.integrierMich(spezH2);
		}
		return spezH2;
	}

	/**
	 * Spezies OH wird automatisch integriert 
	 * @return KoeffizientenSpezies spezOH
	 */
	public Spezies get_spezOH() {
		
		if(spezOH==null){
			double hf;			
			if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD)
				hf=39300;
			else //nach deJaeghr und Grill
				hf=160205;


			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_OH_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_OH_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_OH_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_OH_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}
			//OH kann nicht mit Luft verbrannt werden Hu=0.....
			spezOH=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_OH(),								
					hf,-10e10,0,1,0,1,0,"OH"); //TODO Heizwert + Verdampfungsenthalpie korrigiren
			
			this.integrierMich(spezOH);
		}
		return spezOH;
	}

	/**
	 * Spezies CO wird automatisch integriert 
	 * @return KoeffizientenSpezies spezCO
	 */
	public Spezies get_spezCO() {
		
		if(spezCO==null){
			double hf;	
			//Hu gerechnet 
			double hu=282970;
			//Berechnung der Standerdbildungsenthalpie passend zum Heizwert
			//if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) --hf=>-110530
			//else (also nach deJaegher und Grill etc.  hf=282970;	
			hf=HeizwertRechner.deltaHf4Hu(CP,1,0,1,hu );

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_CO_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_CO_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_CO_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_CO_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}
			//Hu gerechnet --> stimmt mit Wert aus Pischinger Thermodynamik der... ueberein
			spezCO=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_CO(),								
					hf,-10e10,hu,1,1,0,0,"CO"); //TODO Heizwert korrigieren
			
			this.integrierMich(spezCO);
		}
		return spezCO;
	}

	/**
	 * Spezies NO wird automatisch integriert 
	 * @return KoeffizientenSpezies spezNO
	 */
	public Spezies get_spezNO() {

		if(spezNO==null){
			
//			double hf=90290; //keine Unterscheidung notwendig
			double hf=91290; //nach Janaf Thermobuild
			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_NO_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_NO_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_NO_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_NO_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}	

			spezNO=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_NO(),								
					hf,-10e10,0,1,0,0,1,"NO");
			
			this.integrierMich(spezNO);
		}
		return spezNO;
	}

	/**
	 * Spezies CO2 wird automatisch integriert 	
	 * @return KoeffizientenSpezies spezO2
	 */
	public Spezies get_spezO2() {
		
		if(spezO2==null){
			double hf=0; //kein Unterscheidung notwendig

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_O2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_O2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_O2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_O2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}

			spezO2=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_O2(),								
					hf,-10e10,0,2,0,0,0,"O2");
			
			this.integrierMich(spezO2);
		}
		return spezO2;
	}

	/**
	 * Spezies H2O wird automatisch integriert 
	 * @return KoeffizientenSpezies spezH2O
	 */
	public Spezies get_spezH2O() {
		
		if(spezH2O==null){
			
			double hf;			
			if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) //so wie in der Chemie ueblich
				hf=-241810;
			else //nach deJaegher und Grill etc.
				hf=0;

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_H2O_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_H2O_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_H2O_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_H2O_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}

			spezH2O=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_H2O(),									
					hf,-10e10,0,1,0,2,0,"H2O"); 
			
			this.integrierMich(spezH2O);
		}
		return spezH2O;
	}

	/**
	 * Spezies wird automatisch integriert 
	 * @return KoeffizientenSpezies spezCO2
	 */
	public Spezies get_spezCO2() {
		
		if(spezCO2==null){
			double hf;			
			if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD)
				hf=-393500;
			else //nach deJaegher und Grill etc. 
				hf=0;

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_CO2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_CO2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_CO2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_CO2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}
			spezCO2=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_CO2(),									
					hf,-10e10,0,2,1,0,0,"CO2");
			
			this.integrierMich(spezCO2);
		}
		return spezCO2;
	}

	/**
	 * Spezies N2 wird automatisch integriert 	
	 * @return KoeffizientenSpezies spezN2
	 */
	public Spezies get_spezN2() {
		
		if(spezN2==null){
			double hf=0; //keine Unterscheidung notwendig

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_N2_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_N2_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_N2_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_N2_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}
			spezN2=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_N2(),								
					hf,-10e10,0,0,0,0,2,"N2");	
			
			this.integrierMich(spezN2);
		}
		return spezN2;
	}

	/**
	 * Ar wird NICHT automatisch integriert  
	 * @return KoeffizientenSpezies spezAr
	 */
	public Spezies get_spezAr() {
		
		if(spezAr==null){
			double hf=0; //keine Unterscheidung notwendig

			double koeffs [][]=new double [3][];

			if(CP.SYS.THERMO_POLYNOM_KOEFF_VORGABE.equalsIgnoreCase("Burcat")){		
				koeffs[0] = Koeffs_Burcat.get_koeffs_Ar_l();
				koeffs[1]= Koeffs_Burcat.get_koeffs_Ar_h();
				koeffs[2]=Koeffs_Burcat.get_TemperaturGrenze();
			}else{
				koeffs[0] = Koeffs_NASA.get_koeffs_Ar_l();
				koeffs[1]= Koeffs_NASA.get_koeffs_Ar_h();
				koeffs[2]=Koeffs_NASA.get_TemperaturGrenze();
			}

			spezAr=new KoeffizientenSpezies(koeffs,
					PhysKonst.get_M_Ar(),								
					hf,-10e10,0,0,0,0,0,"Ar");
			
//			this.integrierMich(spezAr);
		}
		return spezAr;
	}

	/**
	 * Gibt trockene Luft als Gasgemisch zurueck. 
	 * Luft wird NICHT automatisch integriert
	 * @return GasGemisch spezAir
	 */
	public Spezies get_spezLuft_trocken() {
		if(spezLuftTr==null){
			Hashtable<Spezies, Double> koeffSpeziesMolenBruchHash= new Hashtable<Spezies,Double>();



			double vol_O2=PhysKonst.get_vol_O2_Luft();
			koeffSpeziesMolenBruchHash.put((KoeffizientenSpezies) get_spezO2(),vol_O2 );
			double vol_CO2=PhysKonst.get_vol_CO2_Luft();
			koeffSpeziesMolenBruchHash.put((KoeffizientenSpezies) get_spezCO2(),vol_CO2 );
			double vol_N2=1-vol_O2-vol_CO2;
//			Ar wird nicht beruecksichtigt, da der Gleichgewichtssolver es nicht beruecksichtigt und die Beruecksichtigung
//			von Ar dann zu unstimmigen Massenbruechen fuehren wuerde
			koeffSpeziesMolenBruchHash.put((KoeffizientenSpezies) get_spezN2(), vol_N2);

			spezLuftTr=new GasGemisch("Luft");
			((GasGemisch)spezLuftTr).set_Gasmischung_molenBruch(koeffSpeziesMolenBruchHash);
		}
		return spezLuftTr;
	}



	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Normalbenzin nach FVV-Zylindermodul zurueck.
	 *  RON_91 wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezRON_91
	 */
	public Spezies get_spezRON_91() {
		//TODO Molare Masse als Eingabeparameter inm Inputfile deklarieren
		if(spezRON_91==null){
			double Hu=CP.get_Hu_RON_91();	
			//Berechnung der Standerdbildungsenthalpie passend zum Heizwert
			//if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) --hf=>-1.3939e+04
			//else (also nach deJaegher und Grill etc.  hf=;
			double hf; //-1.8314266E+05

			hf=HeizwertRechner.deltaHf4Hu(CP,7.317412935,14.19104478,0,Hu );
			
			double koeffs [][]=new double [3][];

			koeffs[0] = Koeffs_KrstFVV.get_koeffs_RON_91_l();
			koeffs[1] = Koeffs_KrstFVV.get_koeffs_RON_91_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();

			spezRON_91=new KoeffizientenSpezies(koeffs, 102.19e-3,								
					hf,0,Hu,0,7.317412935,14.19104478,0,"RON_91"); 
		}
		return spezRON_91;
	}


	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Superbenzin nach FVV-Zylindermodul zurueck.
	 *  RON_95 wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezRON_95
	 */
	public Spezies get_spezRON_95() {
		//TODO Molare Masse als Eingabeparameter inm Inputfile deklarieren
		if(spezRON_95==null){
			double Hu=CP.get_Hu_RON_95();
			double hf;
			//Berechnung der Standerdbildungsenthalpie passend zum Heizwert
			//if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) --hf=>-3.5775e+04;
			//else (also nach deJaegher und Grill etc.  hf=XXX;
			hf=HeizwertRechner.deltaHf4Hu(CP,6.96292482,12.46549949,0,Hu );				

		
			double koeffs [][]=new double [3][];

			koeffs[0] = Koeffs_KrstFVV.get_koeffs_RON_95_l();
			koeffs[1] = Koeffs_KrstFVV.get_koeffs_RON_95_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();

			spezRON_95=new KoeffizientenSpezies(koeffs,
					96.19e-3,								
					hf,0,Hu,0,6.96292482,12.46549949,0,"RON_95");	
		}
		return spezRON_95;
	}

	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Superplusbenzin nach FVV-Zylindermodul zurueck.
	 *  RON_98 wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezRON_98
	 */
	public Spezies get_spezRON_98() {
		//TODO Molare Masse als Eingabeparameter inm Inputfile deklarieren
		if(spezRON_98==null){
			double Hu=CP.get_Hu_RON_98();
			double hf;
			//Berechnung der Standerdbildungsenthalpie passend zum Heizwert
			//if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) --hf=>-4.3438e+04;
			//else (also nach deJaegher und Grill etc.  hf=XXX;
			hf=HeizwertRechner.deltaHf4Hu(CP,6.895209581,11.9001996,0,Hu );		
			
			double koeffs [][]=new double [3][];

			koeffs[0] = Koeffs_KrstFVV.get_koeffs_RON_98_l();
			koeffs[1] = Koeffs_KrstFVV.get_koeffs_RON_98_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();

			spezRON_98=new KoeffizientenSpezies(koeffs,
					94.81e-3,								
					hf,0,Hu,0,6.895209581,11.9001996,0,"RON_98");
		}
		return spezRON_98;
	}

	/**
	 *	Gibt ein Objekt vom Typ KoeffizeintenSpezies mit den Koeffizeinten fuer 
	 *  Diesel nach FVV-Zylindermodul zurueck.
	 *  Diesel wird NICHT automatisch integriert
	 *  @return KoeffizientenSpezies spezDiesel
	 */
	public Spezies get_spezDiesel() {
		//TODO Molare Masse als Eingabeparameter inm Inputfile deklarieren
		if(spezDiesel==null){
			double Hu=CP.get_Hu_Diesel();
			
			//Berechnung der Standerdbildungsenthalpie passend zum Heizwert
			//if(STD_BILDUNGSENTHALPIE_IS_CHEMIE_STANDARD) --hf=>4.5996e+04;
			//else (also nach deJaegher und Grill etc.  hf=8084899.999999999;
			double hf;
			hf=HeizwertRechner.deltaHf4Hu(CP,12.60179278,23.52813985,0,Hu );	

			double koeffs [][]=new double [3][];

			koeffs[0] = Koeffs_KrstFVV.get_koeffs_Diesel_l();
			koeffs[1] = Koeffs_KrstFVV.get_koeffs_Diesel_h();
			koeffs[2]=Koeffs_KrstFVV.get_TemperaturGrenze();

			spezDiesel=new KoeffizientenSpezies(koeffs,
					175.07e-3,								
					hf,0,Hu,0,12.60179278,23.52813985,0,"Diesel");	

		}
		return spezDiesel;
	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////	

	protected static class Koeffs_NASA {

		// Koeffizienten nach den Janaf-Tables --> http://cea.grc.nasa.gov/

		static double[] get_koeffs_H_l() {
			double[] H_l = { 0.000000000E+00, 0.000000000E+00, 2.500000000E+00,
					0.000000000E+00, 0.000000000E+00, 0.000000000E+00,
					0.000000000E+00, 2.547370801E+04, -4.466828530E-01 };
			return H_l;
		}

		static double[] get_koeffs_H_h() {
			double[] H_h = { 6.078774250E+01, -1.819354417E-01, 2.500211817E+00,
					-1.226512864E-07, 3.732876330E-11, -5.687744560E-15,
					3.410210197E-19, 2.547486398E+04, -4.481917770E-01 };
			return H_h;
		}

		// ///////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_O_l() {
			double[] O_l = { -7.953611300E+03, 1.607177787E+02, 1.966226438E+00,
					1.013670310E-03, -1.110415423E-06, 6.517507500E-10,
					-1.584779251E-13, 2.840362437E+04, 8.404241820E+00 };
			return O_l;
		}

		static double[] get_koeffs_O_h() {
			double[] O_h = { 2.619020262E+05, -7.298722030E+02, 3.317177270E+00,
					-4.281334360E-04, 1.036104594E-07, -9.438304330E-12,
					2.725038297E-16, 3.392428060E+04, -6.679585350E-01 };
			return O_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_N_l() {
			double[] N_l = { 0.000000000E+00, 0.000000000E+00, 2.500000000E+00,
					0.000000000E+00, 0.000000000E+00, 0.000000000E+00,
					0.000000000E+00, 5.610463780E+04, 4.193905036E+00 };
			return N_l;
		}

		static double[] get_koeffs_N_h() {
			double[] N_h = { 8.876501380E+04, -1.071231500E+02, 2.362188287E+00,
					2.916720081E-04, -1.729515100E-07, 4.012657880E-11,
					-2.677227571E-15, 5.697351330E+04, 4.865231506E+00 };
			return N_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_H2_l() {
			double[] H2_l = { 4.078323210E+04, -8.009186040E+02, 8.214702010E+00,
					-1.269714457E-02, 1.753605076E-05, -1.202860270E-08,
					3.368093490E-12, 2.682484665E+03, -3.043788844E+01 };
			return H2_l;
		}

		static double[] get_koeffs_H2_h() {
			double[] H2_h = { 5.608128010E+05, -8.371504740E+02, 2.975364532E+00,
					1.252249124E-03, -3.740716190E-07, 5.936625200E-11,
					-3.606994100E-15, 5.339824410E+03, -2.202774769E+00 };
			return H2_h;
		}

		// //////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_OH_l() {
			double[] OH_l = { -1.998858990E+03, 9.300136160E+01, 3.050854229E+00,
					1.529529288E-03, -3.157890998E-06, 3.315446180E-09,
					-1.138762683E-12, 2.991214235E+03, 4.674110790E+00 };
			return OH_l;
		}

		static double[] get_koeffs_OH_h() {
			double[] OH_h = { 1.017393379E+06, -2.509957276E+03, 5.116547860E+00,
					1.305299930E-04, -8.284322260E-08, 2.006475941E-11,
					-1.556993656E-15, 2.019640206E+04, -1.101282337E+01 };
			return OH_h;
		}

		// ///////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_CO_l() {
			double[] CO_l = { 1.489045326E+04, -2.922285939E+02, 5.724527170E+00,
					-8.176235030E-03, 1.456903469E-05, -1.087746302E-08,
					3.027941827E-12, -1.303131878E+04, -7.859241350E+00 };
			return CO_l;
		}

		static double[] get_koeffs_CO_h() {
			double[] CO_h = { 4.619197250E+05, -1.944704863E+03, 5.916714180E+00,
					-5.664282830E-04, 1.398814540E-07, -1.787680361E-11,
					9.620935570E-16, -2.466261084E+03, -1.387413108E+01 };
			return CO_h;
		}

		// ///////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_NO_l() {
			double[] NO_l = { -1.143916503E+04, 1.536467592E+02, 3.431468730E+00,
					-2.668592368E-03, 8.481399120E-06, -7.685111050E-09,
					2.386797655E-12, 9.098214410E+03, 6.728725490E+00 };
			return NO_l;
		}

		static double[] get_koeffs_NO_h() {
			double[] NO_h = { 2.239018716E+05, -1.289651623E+03, 5.433936030E+00,
					-3.656034900E-04, 9.880966450E-08, -1.416076856E-11,
					9.380184620E-16, 1.750317656E+04, -8.501669090E+00 };
			return NO_h;
		}

		// //////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_O2_l() {
			double[] O2_l = { -3.425563420E+04, 4.847000970E+02, 1.119010961E+00,
					4.293889240E-03, -6.836300520E-07, -2.023372700E-09,
					1.039040018E-12, -3.391454870E+03, 1.849699470E+01 };
			return O2_l;
		}

		static double[] get_koeffs_O2_h() {
			double[] O2_h = { -1.037939022E+06, 2.344830282E+03, 1.819732036E+00,
					1.267847582E-03, -2.188067988E-07, 2.053719572E-11,
					-8.193467050E-16, -1.689010929E+04, 1.738716506E+01 };
			return O2_h;
		}

		////////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_H2O_l() {
			double[] H2O_l = { -3.947960830E+04, 5.755731020E+02, 9.317826530E-01,
					7.222712860E-03, -7.342557370E-06, 4.955043490E-09,
					-1.336933246E-12, -3.303974310E+04, 1.724205775E+01 };
			return H2O_l;
		}

		static double[] get_koeffs_H2O_h() {
			double[] H2O_h = { 1.034972096E+06, -2.412698562E+03, 4.646110780E+00,
					2.291998307E-03, -6.836830480E-07, 9.426468930E-11,
					-4.822380530E-15, -1.384286509E+04, -7.978148510E+00 };
			return H2O_h;
		}

		////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_CO2_l() {
			double[] CO2_l = { 4.943650540E+04, -6.264116010E+02, 5.301725240E+00,
					2.503813816E-03, -2.127308728E-07, -7.689988780E-10,
					2.849677801E-13, -4.528198460E+04, -7.048279440E+00 };
			return CO2_l;
		}

		static double[] get_koeffs_CO2_h() {
			double[] CO2_h = { 1.176962419E+05, -1.788791477E+03, 8.291523190E+00,
					-9.223156780E-05, 4.863676880E-09, -1.891053312E-12,
					6.330036590E-16, -3.908350590E+04, -2.652669281E+01 };
			return CO2_h;
		}

		////////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_N2_l() {
			double[] N2_l = { 2.210371497E+04, -3.818461820E+02, 6.082738360E+00,
					-8.530914410E-03, 1.384646189E-05, -9.625793620E-09,
					2.519705809E-12, 7.108460860E+02, -1.076003744E+01 };
			return N2_l;
		}

		static double[] get_koeffs_N2_h() {
			double[] N2_h = { 5.877124060E+05, -2.239249073E+03, 6.066949220E+00,
					-6.139685500E-04, 1.491806679E-07, -1.923105485E-11,
					1.061954386E-15, 1.283210415E+04, -1.586640027E+01 };
			return N2_h;
		}

		////////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_Ar_l() {
			double[] Ar_l = { 0.000000000E+00, 0.000000000E+00, 2.500000000E+00,
					0.000000000E+00, 0.000000000E+00, 0.000000000E+00,
					0.000000000E+00, -7.453750000E+02, 4.379674910E+00 };
			return Ar_l;
		}

		static double[] get_koeffs_Ar_h() {
			double[] Ar_h = { 2.010538475E+01, -5.992661070E-02, 2.500069401E+00,
					-3.992141160E-08, 1.205272140E-11, -1.819015576E-15,
					1.078576636E-19, -7.449939610E+02, 4.379180110E+00 };
			return Ar_h;
		}

		static double [] get_TemperaturGrenze(){
			double [] T=new double [1];
			T[0]=1000;
			return T;	
		}

	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////

	protected static class Koeffs_Burcat {	

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_H_l() {
			double[] H_l = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,2.54736600E+004,-4.46682850E-001};
			return H_l;
		}

		static double[] get_koeffs_H_h() {
			double[] H_h = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,2.54736600E+004,-4.46682850E-001};
			return H_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_O_l() {
			double[] O_l = {0.00000000E+000,0.00000000E+000,3.16826710E+000,
					-3.27931884E-003,6.64306396E-006,-6.12806624E-009,
					2.11265971E-012,2.91222592E+004,2.05193346E+000};
			return O_l;
		}

		static double[] get_koeffs_O_h() {
			double[] O_h = {0.00000000E+000,0.00000000E+000,2.54363697E+000,
					-2.73162486E-005,-4.19029520E-009,4.95481845E-012,
					-4.79553694E-016,2.92260120E+004,4.92229457E+000};
			return O_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_N_l() {
			double[] N_l = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,5.61046380E+004,4.19390880E+000};
			return N_l;
		}

		static double[] get_koeffs_N_h() {
			double[] N_h = {0.00000000E+000,0.00000000E+000,2.41594290E+000,
					1.74890650E-004,-1.19023690E-007,3.02262440E-011,
					-2.03609830E-015,5.61337750E+004,4.64960950E+000};
			return N_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_H2_l() {
			double[] H2_l = {0.00000000E+000,0.00000000E+000,2.34433112E+000,
					7.98052075E-003,-1.94781510E-005,2.01572094E-008,
					-7.37611761E-012,-9.17935173E+002,6.83010238E-001};
			return H2_l;
		}

		static double[] get_koeffs_H2_h() {
			double[] H2_h = {0.00000000E+000,0.00000000E+000,2.93286575E+000,
					8.26608026E-004,-1.46402364E-007,1.54100414E-011,
					-6.88804800E-016,-8.13065581E+002,-1.02432865E+000};
			return H2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_OH_l() {
			double[] OH_l = {0.00000000E+000,0.00000000E+000,3.99198424E+000,
					-2.40106655E-003,4.61664033E-006,-3.87916306E-009,
					1.36319502E-012,3.36889836E+003,-1.03998477E-001};
			return OH_l;
		}

		static double[] get_koeffs_OH_h() {
			double[] OH_h = {0.00000000E+000,0.00000000E+000,2.83853033E+000,
					1.10741289E-003,-2.94000209E-007,4.20698729E-011,
					-2.42289890E-015,3.69780808E+003,5.84494652E+000};
			return OH_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_CO_l() {
			double[] CO_l = {0.00000000E+000,0.00000000E+000,3.57953350E+000,
					-6.10353690E-004,1.01681430E-006,9.07005860E-010,
					-9.04424490E-013,-1.43440860E+004,3.50840930E+000};
			return CO_l;
		}

		static double[] get_koeffs_CO_h() {
			double[] CO_h = {0.00000000E+000,0.00000000E+000,3.04848590E+000,
					1.35172810E-003,-4.85794050E-007,7.88536440E-011,
					-4.69807460E-015,-1.42661170E+004,6.01709770E+000};
			return CO_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_NO_l() {
			double[] NO_l = {0.00000000E+000,0.00000000E+000,4.21859896E+000,
					-4.63988124E-003,1.10443049E-005,-9.34055507E-009,
					2.80554874E-012,9.84509964E+003,2.28061001E+000};
			return NO_l;
		}

		static double[] get_koeffs_NO_h() {
			double[] NO_h = {0.00000000E+000,0.00000000E+000,3.26071234E+000,
					1.19101135E-003,-4.29122646E-007,6.94481463E-011,
					-4.03295681E-015,9.92143132E+003,6.36900518E+000};
			return NO_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_O2_l() {
			double[] O2_l = {0.00000000E+000,0.00000000E+000,3.78245636E+000,
					-2.99673416E-003,9.84730201E-006,-9.68129509E-009,
					3.24372837E-012,-1.06394356E+003,3.65767573E+000};
			return O2_l;
		}

		static double[] get_koeffs_O2_h() {
			double[] O2_h = {0.00000000E+000,0.00000000E+000,3.66096065E+000,
					6.56365811E-004,-1.41149627E-007,2.05797935E-011,
					-1.29913436E-015,-1.21597718E+003,3.41536279E+000};
			return O2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_H2O_l() {
			double[] H2O_l = {0.00000000E+000,0.00000000E+000,4.19863520E+000,
					-2.03640170E-003,6.52034160E-006,-5.48792690E-009,
					1.77196800E-012,-3.02937260E+004,-8.49009010E-001};
			return H2O_l;
		}

		static double[] get_koeffs_H2O_h() {
			double[] H2O_h = {0.00000000E+000,0.00000000E+000,2.67703890E+000,
					2.97318160E-003,-7.73768890E-007,9.44335140E-011,
					-4.26899910E-015,-2.98858940E+004,6.88255000E+000};
			return H2O_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_CO2_l() {
			double[] CO2_l = {0.00000000E+000,0.00000000E+000,2.35681300E+000,
					8.98412990E-003,-7.12206320E-006,2.45730080E-009,
					-1.42885480E-013,-4.83719710E+004,9.90090350E+000};
			return CO2_l;
		}

		static double[] get_koeffs_CO2_h() {
			double[] CO2_h = {0.00000000E+000,0.00000000E+000,4.63651110E+000,
					2.74145690E-003,-9.95897590E-007,1.60386660E-010,
					-9.16198570E-015,-4.90249040E+004,-1.93489550E+000};
			return CO2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_N2_l() {
			double[] N2_l = {0.00000000E+000,0.00000000E+000,3.53100528E+000,
					-1.23660988E-004,-5.02999433E-007,2.43530612E-009,
					-1.40881235E-012,-1.04697628E+003,2.96747038E+000};
			return N2_l;
		}

		static double[] get_koeffs_N2_h() {
			double[] N2_h = {0.00000000E+000,0.00000000E+000,2.95257637E+000,
					1.39690040E-003,-4.92631603E-007,7.86010195E-011,
					-4.60755204E-015,-9.23948688E+002,5.87188762E+000};
			return N2_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		// bei Rechnung mit 7 Koeffizienten MÜSSEN die ersten beiden Werte mit 0
		// besetzt sein!!!!!
		// nur so kann die Enthalpieberechnung unabhängig von der Anzahl der
		// Koeffs. durchgeführt werden

		static double[] get_koeffs_Ar_l() {
			double[] Ar_l = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,-7.45375000E+002,4.37967491E+000};
			return Ar_l;
		}

		static double[] get_koeffs_Ar_h() {
			double[] Ar_h = {0.00000000E+000,0.00000000E+000,2.50000000E+000,
					0.00000000E+000,0.00000000E+000,0.00000000E+000,
					0.00000000E+000,-7.45375000E+002,4.37967491E+000};
			return Ar_h;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double [] get_TemperaturGrenze(){
			double [] T=new double [1];
			T[0]=1000;
			return T;	
		}
	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////

	protected static class Koeffs_KrstFVV{
		// TODO Heizwerte fuer Diesel und Ottokraftstoff recherchieren und einfuegen

		//Koeffizienten aus FVV-Zylindermodul --> ExcelSheet
		//siehe SAE-Paper 2007-01-0936 und FVV Zylinder Modul
		static double[] get_koeffs_RON_91_l() {


			double[] koeffs = {-1.9943761E+05, 3.7460241E+03,
					-2.2353473E+01,	1.3280568E-01, -1.1338874E-04,	
					5.5097532E-08,	-1.2257157E-11,	-2.2028224E+04,	1.4353132E+02};			
			return koeffs;
		}		

		static double[] get_koeffs_RON_91_h() {

			double[] koeffs = {4.0700881E+06,	-1.7225060E+04,
					3.3417377E+01,	3.4175622E-02,	-3.7652645E-06,
					-1.1938575E-08,	4.7162362E-12,	9.7471786E+04,	-1.9863604E+02};			
			return koeffs;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_RON_95_l() {
			double[] koeffs= {-2.3187103E+05,	4.5594879E+03,
					-2.9297345E+01,	1.4729421E-01,	-1.3928231E-04,
					7.2818013E-08,	-1.6327979E-11,	-2.7776078E+04,	1.8254742E+02};
			return koeffs;
		}

		static double[] get_koeffs_RON_95_h() {
			double[] koeffs= {5.5279312E+06,	-2.3723969E+04,
					4.5763687E+01,	1.8537447E-02,	-3.1901522E-06,
					-5.7417747E-09,	2.3590005E-12,	1.3321476E+05,	-2.7913386E+02};
			return koeffs;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_RON_98_l() {
			double[] koeffs= {-2.2095119E+05,	4.3222367E+03,
					-2.7488154E+01,	1.3936959E-01,	-1.2919894E-04,
					6.6384283E-08,	-1.4693411E-11,	-2.7573592E+04,	1.7038527E+02};
			return koeffs;
		}

		static double[] get_koeffs_RON_98_h() {
			double[] koeffs= {5.0073479E+06,	-2.1941778E+04,
					4.2387020E+01,	2.0242401E-02,	-4.0170939E-06,
					-5.5459546E-09,	2.3426894E-12,	1.2161657E+05,	-2.5969122E+02};
			return koeffs;
		}

		// ////////////////////////////////////////////////////////////////////////

		static double[] get_koeffs_Diesel_l() {
			double[] koeffs= {-5.5125885E+04,	1.1888903E+03,
					-5.8565879E+00,	1.2919006E-01,	-4.2765568E-05,
					-1.7715249E-08,	1.0701745E-11,	-5.0147461E+03,	5.3334288E+01};
			return koeffs;
		}	

		static double[] get_koeffs_Diesel_h() {
			double[] koeffs= {1.1909130E+06,	-5.8205100E+03,
					1.5458792E+01,	9.1059407E-02,	-4.8837993E-06,
					-3.6398609E-08,	1.4081972E-11,	3.3768000E+04,	-7.5721020E+01};
			return koeffs;
		}	


		// ////////////////////////////////////////////////////////////////////////

		static double [] get_TemperaturGrenze(){
			double [] T=new double [1];
			T[0]=1000;
			return T;	
		}		

	}



}
