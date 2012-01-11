package berechnungsModule.Berechnung;


import io.FileWriter_txt;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import matLib.MatLibBase;
import misc.VektorBuffer;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.ModulPrototyp;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;



public abstract class BerechnungsModell extends ModulPrototyp {

	private static BerechnungsModell bm=null;	
	protected final CasePara CP;
//	private Hashtable<String, Vector<Double>> ergebnisHash=new Hashtable<String, Vector<Double>>();
//	private Hashtable<String, Integer> ergebnisSpalteHash=new Hashtable<String, Integer>();
	private ErgebnisBuffer ergBuf;
	public final static String modulFlag="berechnungsModell";
	public final static String [] MOEGLICHE_MODELLE={"DVA_1Zonig", "DVA_2Zonig","DVA_DualFuel"};	
	

	//soll beim einlesen verwendet werden um zu ueberpruefen ob alle Modelleingaben vorhanden sind
	//außerdem ermoeglicht dies die Erstellung einer Auswahlbox fuer ein eventuelles GUI
	protected static   String [] benoetigteModelle=null; 	

	public final static String []  get_benoetigteModelle(){

		if(benoetigteModelle==null){
			try {
				throw new BirdBrainedProgrammerException ("Fuer das ausgewaehlte BerechnungsModell wurde die statische " +
				"Klassenvariable \n  \"benoetigteModelle\" nicht initialisiert --> Programmierfehler");
			} catch (BirdBrainedProgrammerException e1) {					
				e1.stopBremo();
			}		
		}				
		return benoetigteModelle;
	}	
	
	
	public static BerechnungsModell get_Instance(CasePara cp){
				
		if(bm==null){
			String berechnungsModellVorgabe=get_ModulWahl(modulFlag, MOEGLICHE_MODELLE, cp);

			if(berechnungsModellVorgabe.equals("DVA_1Zonig")){			
				bm=new DVA_Homogen_EinZonig(cp);

			}else if(berechnungsModellVorgabe.equals("DVA_2Zonig")){
				bm=new DVA_homogen_ZweiZonig(cp);	

			}else if(berechnungsModellVorgabe.equals("DVA_DualFuel")){			
				bm=new DVA_DualFuel(cp);		
			}

			if(bm==null){
				try {
					throw new BirdBrainedProgrammerException(
							"Das BerechnuingsModell \"" +berechnungsModellVorgabe + 
							" \" wurde im InputFile " +
							"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n" +
					"Es fehlt der entsprechende else-if-Block oder das  BerechnuingsModell wurde noch nicht implementiert");
				} catch (BirdBrainedProgrammerException e) {
					e.stopBremo();
				}
			}
		}

		return bm;		
	}

	protected BerechnungsModell(CasePara cp, ErgebnisBuffer ergBuf){
		CP=cp;	
		this.ergBuf=ergBuf;
	}

	
////////////////////////////////////////////////////////////////////////////////////////////////	
////////////////				Abstract Methods for all children				////////////////
////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public abstract boolean isDVA();
	
	/**
	 * Gibt an ob waehrend des Programmablaufs Zonen entstehen und mit 
	 * bestimmten werten initialisiert werden sollen
	 * @return
	 */
	public abstract boolean initialiseBurntZone();
	
	protected abstract Zone [] ersterHSBrennraum(double time,Zone [] zonen_IN);
	public abstract void bufferErgebnisse(double time, Zone [] zn);	
	public abstract Zone[] get_initialZones();
	public abstract Zone[] get_initialZonesWhileRunning();
	public abstract int get_anzZonen();
	public abstract VektorBuffer get_dm_buffer();
	public abstract VektorBuffer get_dQw_buffer();
	public abstract VektorBuffer get_dQb_buffer();
	protected abstract void checkEinspritzungen(MasterEinspritzung me);
	
//	/**
//	 * Liefert für die Einspritzung mit der Nummer indexInj die Zone in die diese 
//	 * einspritzen darf 
//	 * @param indexInj
//	 * @return
//	 */
//	public  abstract  int get_InjZone(int indexInj);
	
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////				--------------------------------				////////////////
////////////////////////////////////////////////////////////////////////////////////////////////	
	


	public Zone [] aktualisiereDifferentiale(double time, Zone[] zn) {
		//Berechnen der aktuellen Differentiale --> Aufstellen des dgl-Systems
		return ersterHSBrennraum(time,zn);
	}	
	
	protected void buffer_EinzelErgebnis(String name, double value, int spalte){	
		this.ergBuf.buffer_EinzelErgebnis(name, value, spalte);		
	}
	
	public  void schreibeErgebnisFile(String name){		
		this.ergBuf.schreibeErgebnisFile(name);		
	}

	/**
	 * Methode welche die ErgebnisBuffer loescht. Dies wird fuer die LWA benoetigt, 
	 * da heir mehrere Rechnungsdurchlaeufe mit ein und dem selben Objekt vom durchgefuehrt werden.
	 */
	public void clearErgebnisBuffer(){
		this.ergBuf.clearErgebnisBuffer();		
	}
		

}
