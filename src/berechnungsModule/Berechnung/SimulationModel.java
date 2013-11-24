package berechnungsModule.Berechnung;



import misc.VectorBuffer;
import berechnungsModule.ResultsBuffer;
import berechnungsModule.mixtureFormation.MasterInjection;
import bremo.parameter.CasePara;


public abstract class SimulationModel  {
	
	protected final CasePara CP;
	private ResultsBuffer ergBuf;	


	protected SimulationModel(CasePara cp, ResultsBuffer ergBuf){
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
	
	protected abstract Zone [] firstLawCombustionChamber(double time,Zone [] zonen_IN);
	public abstract void bufferResults(double time, Zone [] zn);	
	public abstract Zone[] get_initialZones();
	public abstract Zone[] get_initialZonesWhileRunning();
	public abstract int get_nbrOfZones();
	public abstract VectorBuffer get_dm_buffer();
	public abstract VectorBuffer get_dQw_buffer();
	public abstract VectorBuffer get_dQb_buffer();
	protected abstract void checkInjections(MasterInjection me);	
	
	
////////////////////////////////////////////////////////////////////////////////////////////////
////////////////				--------------------------------				////////////////
////////////////////////////////////////////////////////////////////////////////////////////////	
	

	public Zone [] aktualisiereDifferentiale(double time, Zone[] zn) {
		//Berechnen der aktuellen Differentiale --> Aufstellen des dgl-Systems
		return firstLawCombustionChamber(time,zn);
	}	
	
	protected void buffer_singleResult(String name, double value, int spalte){	
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
