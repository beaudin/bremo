package berechnungsModule.gemischbildung;

import kalorik.spezies.Spezies;
import berechnungsModule.ModulPrototyp;
import berechnungsModule.Berechnung.Zone;
import misc.VektorBuffer;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;


public abstract class Einspritzung extends ModulPrototyp {	
	
	protected final CasePara CP;
	public static final String EINSPRITZ_MODELL_FLAG="einspritzModell_"; //gibt den Eintrag im inputfile an
	private  final static String[] MOEGLICHE_EINSPRITZ_MODELLE={SaugrohrEinspritzungHomogen.FLAG,
															SaugrohrEinspritzungHomogen.FLAG2, 
															Spray.FLAG,
															SimpleDirektEinspritzung.FLAG};	
	
	/**
	 * ID der Zone in die eingespritzt wird															
	 */
	protected final int ID_ZONE; 
//	private static Einspritzung einspritzung=null;
	protected Spezies krst;
	protected double mKrst;
	protected double eoi=-1,boi=-1; //Einspritzbeginn und Ende in [s n.Rechenbeginn]
	protected final int INDEX;	
	protected static boolean isLWA=false;
	
	//TODO Checken ob die buffer ueberhaupt gebraucht werden --> Bei LWA kann die bisherige (20111012) implementierung falsch sein
	protected VektorBuffer mKrst_fluesssig;
	protected VektorBuffer mKrst_dampf;
	
	
	protected Einspritzung(CasePara cp, int index){
		this.CP=cp;	
		INDEX=index;
		ID_ZONE=CP.get_InjZone(index);

		mKrst=CP.get_mKrst_ASP(index);
		krst=CP.get_kraftsoff(index);
		krst.integrierMich(); //Diese Anweisung sorgt dafür, dass nur verwendete Kraftstoffe integriert werden

		boi=CP.get_BOI(index);
		eoi=CP.get_EOI(index);		
	

		double time=-1*CP.SYS.DAUER_ASP_SEC;
		double inc=CP.SYS.WRITE_INTERVAL_SEC;
		//Einspritzbeginn so legen, dass er auf einem Rechenschritt liegt!
		do{
			boi=time;//legt boi immer auf den letzten zeitpunkt der kleiener ist als der angegebene
			time+=inc;
		}while(time<=CP.get_BOI(index));

		//Einspritzende so legen, dass es auf einem Rechenschritt liegt!				
		do{	
			eoi=time; //legt eoi immer auf den ersten Zeitpunkt der groesser ist als der angegebene
			time+=inc;				
		}while(time<=CP.get_EOI(index));

		if(eoi==boi)//Dass eoi groesser ist als boi wird in CasePara gecheckt
			eoi=boi+inc;

		mKrst_fluesssig = new VektorBuffer(cp);
		mKrst_dampf=new VektorBuffer(cp);
	}
	
	
	protected static Einspritzung get_Instance(CasePara cp, int index){
		String modulFlag=EINSPRITZ_MODELL_FLAG+index;
		return get_Instance(cp, index,modulFlag);
	}
	
	
	protected static Einspritzung get_Instance_LWA(CasePara cp, int index){
		isLWA=true;
		String modulFlag=EINSPRITZ_MODELL_FLAG+index;  		
		return get_Instance(cp, index,modulFlag);
	}
	
	
	private static Einspritzung get_Instance(CasePara cp, int index,String modulFlag){
		Einspritzung einspritzung=null;	
		
		 String einspritzungsModellVorgabe=
			get_ModulWahl(modulFlag, MOEGLICHE_EINSPRITZ_MODELLE, cp);

		if(einspritzungsModellVorgabe.equals(SaugrohrEinspritzungHomogen.FLAG)|| 
				einspritzungsModellVorgabe.equals(SaugrohrEinspritzungHomogen.FLAG2)){
			
			einspritzung=(Einspritzung) new SaugrohrEinspritzungHomogen(cp, index);

		}else if(einspritzungsModellVorgabe.equals(Spray.FLAG)){
			einspritzung=new Spray(cp, index);	
			
		}else if(einspritzungsModellVorgabe.equals(SimpleDirektEinspritzung.FLAG)){			
			einspritzung=new SimpleDirektEinspritzung(cp, index);			
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
		return CP.get_T_Krst_fl(this.INDEX);		
	}


}
