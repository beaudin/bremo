package berechnungsModule.Berechnung;

import java.util.Hashtable;

public class ZonenVerwaltung {
	
	
	private Hashtable<Zone, Integer> zonenHash;
	
	
	public ZonenVerwaltung(int anzahlZonen){
		int loadFactor=(int) (anzahlZonen/0.75);
		zonenHash=new Hashtable<Zone, Integer> (loadFactor);
	}
	
	public ZonenVerwaltung(){		
		zonenHash=new Hashtable<Zone, Integer> (5);
	}
	
	
	public void checkIN(Zone zone){	
		
		//diese Abfrage wird eigentlich bereits im Zonenkonstruktor durchgeführ...
		if(zonenHash.containsValue(zone.getID()))
			throw new IllegalArgumentException(
			"Es wurde versucht eine Zone mit einer bereits bestehenden ID einzuchecken");
		
		zonenHash.put(zone, zone.getID());

	}
	
	public boolean existsID(int id){
		
		if (zonenHash.containsValue(id))
				return true;
		else
			return false;
		
	}
}
