package berechnungsModule.gemischbildung;

import io.KraftstoffrateFileReader;
import io.TauFileReader;
import kalorik.spezies.Spezies;
import berechnungsModule.Berechnung.Zone;
import misc.VektorBuffer;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import io.KraftstoffrateFileReader;


public abstract class Einspritzung{	
	
	protected final CasePara CP;
	public KraftstoffrateFileReader kr_reader;
	protected TauFileReader tau_reader;
	
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
	protected VektorBuffer mKrst_fluessig;
	protected VektorBuffer mKrst_dampf;
	public final boolean IS_LWA_EINSPRITZUNG, EINSPRITZUNG_VOR_DVA;
	public boolean erstdurchlauf = true;
	public VektorBuffer kraftstoff;
	private boolean isDVA=false;
	
	
	protected Einspritzung(CasePara cp, int index){
		this.CP=cp;	
		INDEX=index;
		ID_ZONE=CP.get_InjZone(index);

		mKrst=CP.get_mKrst_ASP(index);
		krst=CP.get_kraftsoff(index);
		CP.SPEZIES_FABRIK.integrierMich(krst); //Diese Anweisung sorgt dafür, dass nur verwendete Kraftstoffe integriert werden

		boi=CP.get_BOI(index);
		eoi=CP.get_EOI(index);	
		

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

		if(boi>CP.get_Einlassschluss() && boi<CP.SYS.RECHNUNGS_BEGINN_DVA_SEC){
			try{
				throw new BirdBrainedProgrammerException("Fuer Einspritzung " +(index+1)+ " lag BOI zwischen Einlassschluss und Rechenbeginn " +
						"EOI wird auf Rechenbeginn gelegt damit die Kraftstoffmasse komplett berechnet wird");
			}catch(BirdBrainedProgrammerException bbpE){
				EINSPRITZUNG_VOR_DVA=true;
				eoi=CP.SYS.RECHNUNGS_BEGINN_DVA_SEC;
				bbpE.log_Message();				
			}
		}
		else{
			EINSPRITZUNG_VOR_DVA = false;
		}

		mKrst_fluessig = new VektorBuffer(cp);
		mKrst_dampf=new VektorBuffer(cp);
		kraftstoff = new VektorBuffer(cp);
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
	
	/**
	 * Temperature of liquid fuel at nozzle exit
	 * @return T_fuel_liq_Init [K]
	 */
	public double get_T_fuel_liq(){
		if(Double.isNaN(T_fuel_liq_Init))
			T_fuel_liq_Init=CP.get_T_Krst_fl(INDEX);
		return 	T_fuel_liq_Init;
	}
	
	/**
	 * returns the characteristic evaporation time
	 * @param index
	 * @param time
	 * @return tau (konst.) oder tau (variabel)
	 */
	public double get_Tau(double time){
		double tau;
		tau = this.tau_reader.get_Tau(time);
		
		if(tau<=0){
			try {
				throw new BirdBrainedProgrammerException("Der Tau-Verlauf der Einspritzung " 
						+ this.INDEX + " enhaelt negative Bereiche oder besitzt an mindestens "
								+ "einer Stelle den Wert 0. Das ist nicht moeglich !");
			} catch (BirdBrainedProgrammerException e) {
				e.log_Message();
			}
		}
		
		return tau;
	}
	
	/**
	 * returns the mass of fuel
	 * @param time
	 * @return m_Krst
	 */
	public double get_Mass(double time){
		double rate, masse;
		
		if(this.IS_LWA_EINSPRITZUNG){
			if(time>=this.CP.get_Auslassoeffnet()){
				// if external file exists
				if(kr_reader.fileExists){
					if(time>=kr_reader.get_Zeitachse()[0]){
						if(time<=kraftstoff.get_maxTime()){
							return kraftstoff.getValue(time);
						}
						else{
							if(erstdurchlauf){
								// add values in order to sucessfully interpolate
								kraftstoff.addValue(this.boi+CP.SYS.DAUER_ASP_SEC-CP.SYS.WRITE_INTERVAL_SEC, 0);
								kraftstoff.addValue(this.boi+CP.SYS.DAUER_ASP_SEC, 0);
								kraftstoff.addValue(time-CP.SYS.WRITE_INTERVAL_SEC, 0);
								erstdurchlauf=false;
							}
							
							rate = this.kr_reader.get_Rate(time);
							
							if(rate<0){
								try {
									throw new BirdBrainedProgrammerException("Die Kraftstoffrate der Einspritzung " 
								+ this.INDEX + " enhaelt negative Bereiche. Es wird Kraftstoff entnommen !");
								} catch (BirdBrainedProgrammerException e) {
									e.log_Message();
								}
							}
							
							// add up mass
							masse = kraftstoff.getValue(time-CP.SYS.WRITE_INTERVAL_SEC)
									+rate*CP.SYS.WRITE_INTERVAL_SEC;
							
							if(masse>this.mKrst){
								masse = this.mKrst;
							}
							
							kraftstoff.addValue(time, masse);
							return masse;	
						}	
					}
					else{
						return 0;
					}
				}
				else{ // else return constant ratio
					if(time>this.boi+CP.SYS.DAUER_ASP_SEC){
						double mass = kr_reader.get_Rate(this.INDEX)*(time-(this.boi+CP.SYS.DAUER_ASP_SEC));
						if(mass>this.mKrst){
							mass = this.mKrst;
						}
						return mass;
					}else
						return 0;
				}
			}
			else{
				return this.mKrst;
			}
		}
		else{
			if(time==CP.SYS.RECHNUNGS_BEGINN_DVA_SEC){
				this.isDVA = true;
			}
			if(!this.isDVA){
				return 0;
			}
			else{
				// if external file exists
				if(kr_reader.fileExists){
					if(time>=kr_reader.get_Zeitachse()[0]){
						if(time<=kraftstoff.get_maxTime()){
							return kraftstoff.getValue(time);
						}
						else{
							if(erstdurchlauf){
								// add values in order to sucessfully interpolate
								kraftstoff.addValue(this.boi-CP.SYS.WRITE_INTERVAL_SEC, 0);
								kraftstoff.addValue(this.boi, 0);
								kraftstoff.addValue(time-CP.SYS.WRITE_INTERVAL_SEC, 0);
								erstdurchlauf=false;
							}
							
							rate = this.kr_reader.get_Rate(time);
							
							if(rate<0){
								try {
									throw new BirdBrainedProgrammerException("Die Kraftstoffrate der Einspritzung " 
								+ this.INDEX + " enhaelt negative Bereiche. Es wird Kraftstoff entnommen !");
								} catch (BirdBrainedProgrammerException e) {
									e.log_Message();
								}
							}
							
							// add up mass
							masse = kraftstoff.getValue(time-CP.SYS.WRITE_INTERVAL_SEC)
									+rate*CP.SYS.WRITE_INTERVAL_SEC;
							
							if(masse>this.mKrst){
								masse = this.mKrst;
							}
							
							kraftstoff.addValue(time, masse);
							return masse;	
						}	
					}
					else{
						return 0;
					}
				}
				else{ // else return constant ratio
					if(time>this.boi && time <=this.CP.SYS.RECHNUNGS_ENDE_DVA_SEC){
						double mass = kr_reader.get_Rate(this.INDEX)*(time-this.boi);
						if(mass>this.mKrst){
							mass = this.mKrst;
						}
						return mass;
					}else
						return 0;
				}
			}
		}
	}

	/**
	 * returns the fuel ratio
	 * @param time
	 * @return
	 */
	public double get_Rate(double time) {
		// if external file exists
		if(this.kr_reader.fileExists){
			return kr_reader.get_Rate(time);
		}
		/*
		 *  else return either value given in the Input file
		 *  or if it doesn't exist return standart value (checked in CasePara)
		 */
		else{ 
			if(this.IS_LWA_EINSPRITZUNG){
				if(time>=this.boi+CP.SYS.DAUER_ASP_SEC && time <=this.eoi+CP.SYS.DAUER_ASP_SEC)
					return kr_reader.get_Rate(this.INDEX);
				else
					return 0;
			}
			else{
				if(time>this.boi && time<=this.eoi)
					return kr_reader.get_Rate(this.INDEX);
				else
					return 0;
			}	
		}
	}
}
