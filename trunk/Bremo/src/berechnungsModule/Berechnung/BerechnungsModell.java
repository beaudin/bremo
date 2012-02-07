package berechnungsModule.Berechnung;



import misc.VektorBuffer;
import berechnungsModule.ErgebnisBuffer;
import berechnungsModule.gemischbildung.MasterEinspritzung;
import bremo.parameter.CasePara;


public abstract class BerechnungsModell  {
	
	protected final CasePara CP;
	private ErgebnisBuffer ergBuf;	


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
