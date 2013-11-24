package berechnungsModule.mixtureFormation;

import kalorik.Spezies;
import berechnungsModule.Berechnung.Zone;
import misc.VectorBuffer;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;


public abstract class Injection{	
	
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
	private double T_fuel_liq_Init=Double.NaN;

	
	//TODO Checken ob die buffer ueberhaupt gebraucht werden --> Bei LWA kann die bisherige (20111012) implementierung falsch sein
	protected VectorBuffer mKrst_fluessig;
	protected VectorBuffer mKrst_dampf;
	public final boolean IS_LWA_EINSPRITZUNG;
	
	
	protected Injection(CasePara cp, int index){
		this.CP=cp;	
		INDEX=index;
		ID_ZONE=CP.get_InjZone(index);

		mKrst=CP.get_mKrst_ASP(index);
		krst=CP.get_kraftsoff(index);
		CP.SPECIES_FACTROY.integrierMich(krst); //Diese Anweisung sorgt dafür, dass nur verwendete Kraftstoffe integriert werden

		boi=CP.get_BOI(index);
		eoi=CP.get_EOI(index);	
		

		double time=-1*CP.SYS.CYCLE_DURATION_SEC;
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
		
		if(boi<CP.get_IVC()&& eoi<=CP.get_IVC())
			IS_LWA_EINSPRITZUNG=true;
		else if(boi<CP.get_IVC()&& eoi>CP.get_IVC()){
			try{
				throw new BirdBrainedProgrammerException("Fuer Einspritzung " +(index+1)+ " lag BOI vor Einlassschluss wohingegen EOI " +
						"nach Einlassschluss lag. Das ist mir zu dumm! EOI wird auf Einlassschluss gelegt. !");
			}catch(BirdBrainedProgrammerException bbpE){
				IS_LWA_EINSPRITZUNG=true;
				eoi=CP.get_IVC();
				bbpE.log_Message();				
			}
		}else
			IS_LWA_EINSPRITZUNG=false;

		mKrst_fluessig = new VectorBuffer(cp);
		mKrst_dampf=new VectorBuffer(cp);
	}	
	
	/**
	 * Returns the flux of kinetic energy into the combustion chamber due to injection
	 * @return d_Ekin	[kg m^2/s^3]
	 */
	public abstract double get_kineticEnergyFlux(double time, double pCyl);
	
	

	/**
	 * <p>Liefert den Betrag des Differentials dQ der Waermemenge die dem Brennraum aufgrund der Kraftstoffverdampfung 
	 * zum Zeitpunkt time [sec n. Rechnungsbeginn] entzogen wird </p>
	 * @param time
	 */
	public abstract double get_dQ_fuelVapor(double time,Zone zn);
	
	
	/**
	 * Returns the instantaneous differential change of fuel mass due to evaporation
	 *  in the zone {@code .zn} 
	 * {@code time} [sec a. start of simulation].
	 * @param time [s n. Rechenbeginn]
	 * @return Änderung der dampffoermigen Kraftstoffmasse [kg/s]
	 */
	public abstract double get_diff_mFuel_vapor(double time, Zone zn);		
	
	
	/**
	 * 	Hier muessen die Vektor_buffers mKrst_dampf und mKrst_fluessig mit Werten gefuellt werden.
	 * 	Die Integration kann innerhalb der Einspritzung erfolgen, 
	 * 	da der Zustand in der Zone während eines Integrationsschrittes als konst. angenommen wird.
	 * 	Diese Methode muss aufgerufen werden wenn die Ergebnisse fuer einen Zeitschritt gespeichert werden.	
	 * @param time
	 * @return
	 */
	public abstract void calculateIntegralvalues(double time, Zone zn);

	
	/**
	 * Liefert die Temperatur des Dampffoermigen Kraftstoffs 
	 * @param time
	 * @param zn
	 * @return
	 */
	public abstract double get_T_fuelVapor(double time, Zone zn);	
	
	
	
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
	public  double get_m_fuelLiquid(double time){		
		return this.mKrst_fluessig.getValue(time);
	}

	
	/**
	 * Liefert den gesamten Kraftsoff (dampfoermig und fluessig) der zum Zeitpunkt time [sec n. Rechnungsbeginn] 
	 * bereits eingespritzt wurde (ungeachtet davon wieviel Krst bereits Verbrannt ist)
	 * @param time
	 * @return
	 */	
	public double get_mKrst_DampfUndFluessig(double time){
		return this.mKrst_fluessig.getValue(time)+this.mKrst_dampf.getValue(time);
	}	
	
	
	/**
	 * Returns the mass of fuel that is injected during that cycle
	 * @return mFuel per cycle in [kg]
	 */
	public double get_mFuel_Cycle() {		
		return mKrst;
	}
	
	public void sayHello() {
		System.out.println("Hallo, schoen von Dir zu hoehren");
	}	
	
	public Spezies get_fuel(){
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
	
	/**
	 * Temperature of liquid fuel at nozzle exit
	 * @return T_fuel_liq_Init [K]
	 */
	public double get_T_fuel_liq(){
		if(Double.isNaN(T_fuel_liq_Init))
			T_fuel_liq_Init=CP.get_T_Krst_fl(INDEX);
		return 	T_fuel_liq_Init;
	}


}
