package berechnungsModule.gemischbildung;

import kalorik.spezies.Spezies;
import berechnungsModule.Berechnung.Zone;
import misc.VektorBuffer;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;


public abstract class Einspritzung{	
	
	protected final CasePara CP;
	
	/**
	 * ID der Zone in die eingespritzt wird															
	 */
	protected final int ID_ZONE; 
//	private static Einspritzung einspritzung=null;
	protected Spezies krst;
	protected double mKrst;
	protected double eoi=-1,boi=-1; //Einspritzbeginn und Ende in [s n.Rechenbeginn]
	protected final int INDEX;	
	private double T_krst_fl;

	
	//TODO Checken ob die buffer ueberhaupt gebraucht werden --> Bei LWA kann die bisherige (20111012) implementierung falsch sein
	protected VektorBuffer mKrst_fluesssig;
	protected VektorBuffer mKrst_dampf;
	public final boolean IS_LWA_EINSPRITZUNG;
	
	
	protected Einspritzung(CasePara cp, int index){
		this.CP=cp;	
		INDEX=index;
		ID_ZONE=CP.get_InjZone(index);

		mKrst=CP.get_mKrst_ASP(index);
		krst=CP.get_kraftsoff(index);
		CP.SPEZIES_FABRIK.integrierMich(krst); //Diese Anweisung sorgt dafür, dass nur verwendete Kraftstoffe integriert werden

		boi=CP.get_BOI(index);
		eoi=CP.get_EOI(index);	
		
	
		T_krst_fl=CP.get_T_Krst_fl(index);

		double time=-1*CP.SYS.DAUER_ASP_SEC;
		double inc=CP.SYS.WRITE_INTERVAL_SEC;
		//Einspritzbeginn so legen, dass er auf einem Rechenschritt liegt!
		do{
			boi=time;//legt boi immer auf den letzten zeitpunkt der kleiner ist als der angegebene
			time+=inc;
		}while(time<=CP.get_BOI(index));

		//Einspritzende so legen, dass es auf einem Rechenschritt liegt!				
		do{	
			eoi=time; //legt eoi immer auf den ersten Zeitpunkt der groesser ist als der angegebene
			time+=inc;				
		}while(time<=CP.get_EOI(index));

		if(eoi==boi)//Dass eoi groesser ist als boi wird in CasePara gecheckt
			eoi=boi+inc;
		
		if(boi<CP.get_Einlassschluss()&& eoi<=CP.get_Einlassschluss())
			IS_LWA_EINSPRITZUNG=true;
		else if(boi<CP.get_Einlassschluss()&& eoi>CP.get_Einlassschluss()){
			try{
				throw new BirdBrainedProgrammerException("Fuer Einspritzung " +(index+1)+ " lag BOI vor Einlassschluss wohingegen EOI " +
						"nach Einlassschluss lag. Das ist mir zu dumm! EOI wird auf Einlassschluss gelegt. !");
			}catch(BirdBrainedProgrammerException bbpE){
				IS_LWA_EINSPRITZUNG=true;
				eoi=CP.get_Einlassschluss();
				bbpE.log_Message();				
			}
		}else
			IS_LWA_EINSPRITZUNG=false;

		mKrst_fluesssig = new VektorBuffer(cp);
		mKrst_dampf=new VektorBuffer(cp);
	}	
	
	

	/**
	 * <p>Liefert den Betrag des Differentials dQ der Waermemenge die dem Brennraum aufgrund der Kraftstoffverdampfung 
	 * zum Zeitpunkt time [sec n. Rechnungsbeginn] entzogen wird </p>
	 * @param time
	 */
	public abstract double get_dQ_krstDampf(double time,Zone zn);
	
	
	/**
	 * Liefert die Masse an Krafstoff die zum Zeitpunkt time [s n. Rechenbeginn] grade verdampft.
	 * (ungeachtet davon wieviel Krst bereits Verbrannt ist)
	 * @param time [s n. Rechenbeginn]
	 * @return Änderung der dampffoermigen Kraftstoffmasse [kg/s]
	 */
	public abstract double get_diff_mKrst_dampf(double time, Zone zn);		
	
	
	/**
	 * 	Hier muessen die Vektor_buffers mKrst_dampf und mKrst_fluessig mit Werten gefuellt werden.
	 * 	Die Integration kann innerhalb der Einspritzung erfolgen, 
	 * 	da der Zustand in der Zone während eines Integrationsschrittes als konst. angenommen wird.
	 * 	Diese Methode muss aufgerufen werden wenn die Ergebnisse fuer einen Zeitschritt gespeichert werden.	
	 * @param time
	 * @return
	 */
	public abstract void berechneIntegraleGroessen(double time, Zone zn);

	
	/**
	 * Liefert die Temperatur des Dampffoermigen Kraftstoffs 
	 * @param time
	 * @param zn
	 * @return
	 */
	public abstract double get_Tkrst_dampf(double time, Zone zn);	
	
	
	
	/**
	 * Liefert den dampffoermigen Teil des zum Zeitpunkt time [sec n. Rechnungsbeginn] 
	 * bereits eingespritzten Krafstoffs (ungeachtet davon wieviel Krst bereits Verbrannt ist)
	 * @param time
	 * @return
	 */
	public  double get_mKrst_verdampft(double time){
		return this.mKrst_dampf.getValue(time);	
	}
	
	
	/**
	 * Liefert den fluessigen Anteil des zum Zeitpunkt time [sec n. Rechnungsbeginn]
	 * bereits eingespritzten Kraftstoffs (ungeachtet davon wieviel Krst bereits Verbrannt ist)
	 * @param time
	 * @return
	 */
	public  double get_mKrst_fluessig(double time){		
		return this.mKrst_fluesssig.getValue(time);
	}

	
	/**
	 * Liefert den gesamten Kraftsoff (dampfoermig und fluessig) der zum Zeitpunkt time [sec n. Rechnungsbeginn] 
	 * bereits eingespritzt wurde (ungeachtet davon wieviel Krst bereits Verbrannt ist)
	 * @param time
	 * @return
	 */	
	public double get_mKrst_DampfUndFluessig(double time){
		return this.mKrst_fluesssig.getValue(time)+this.mKrst_dampf.getValue(time);
	}	
	
	
	/**
	 * <p>Liefert die gesamte Kraftstoffmenge die von dieser Einspritzung 
	 * waehrend des Arbeitspiels eingespritzt wird 
	 * @return
	 */
	public double get_mKrst_ASP() {		
		return mKrst;
	}
	
	public void sayHello() {
		System.out.println("Hallo, schoen von Dir zu hoehren");
	}	
	
	public Spezies get_Krst(){
		return krst;
	}
	

	public double get_BOI(){
		return boi;
	}
	
	public double get_EOI(){
		return eoi;
	}
	
	
	/**
	 * Liefert die ID der Zone in die eingespritzt wird
	 * @return ID as Integer
	 */
	public int get_ID_Zone(){
		return ID_ZONE;
	}
	
	public double get_T_Krst_fl(){
		return 	T_krst_fl;
	}


}
