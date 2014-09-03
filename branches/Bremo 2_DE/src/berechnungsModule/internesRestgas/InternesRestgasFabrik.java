package berechnungsModule.internesRestgas;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
/**
 * Aktuell hinterlegte Modelle für die Berechnung der internen Restgasmass:
 * @RestgasVorgabe
 * @MuellerBertling
 * @Heywood
 * @LWA -- einfache LWA
 * @LWA_ZwiKo -- LWA unter Berücksichtigung von Kraftstoffumsatz in der Zwischenkompression
 */
public class InternesRestgasFabrik extends ModulFabrik {

	public static final String INTERNES_RESTGAS_MODELL_FLAG="internesRestgasModell";
	public  final static String[] INTERNES_RESTGAS_MODELLE={"RestgasVorgabe", 	//Vorgabewert für die Restgasmasse in [kg]
															"MuellerBertling",	//Berechnung nach Müller-Bertling
															"Heywood",			//Berechnung nach Heywood
															"LWA",				//Berechnung durch eine "einfache" LWA
															"LWA_ZwiKo"};		//Berechnung durch LWA unter Berücksichtigung von Kraftstoffumsatz in der Zwischenkompression
	public final InternesRestgas RESTGAS_MODELL;
	
	public InternesRestgasFabrik(CasePara cp){
		super(cp);
		
		String internesRestgas_ModellVorgabe=
			get_ModulWahl(INTERNES_RESTGAS_MODELL_FLAG, INTERNES_RESTGAS_MODELLE);
		
		if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[0])){
			RESTGAS_MODELL=(InternesRestgas) new RestgasVorgabe(cp);
			
		}else if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[1])){
			RESTGAS_MODELL=(InternesRestgas) new MuellerBertling(cp); //Kerrom
//			RESTGAS_MODELL=null;
//			try {
//				throw new BirdBrainedProgrammerException(
//						"Das ausgewaehlte Restgasmodell \"" +internesRestgas_ModellVorgabe + 
//						" \" wurde im InputFile " +
//						"zwar als valide akzeptiert, ist im Programm aber noch nicht implementiert worden.");
//			} catch (BirdBrainedProgrammerException e) {
//				e.stopBremo();
//			}
		}else if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[2])){
			RESTGAS_MODELL=(InternesRestgas) new Heywood(cp);
//			RESTGAS_MODELL=null;
//			try {
//				throw new BirdBrainedProgrammerException(
//						"Das ausgewaehlte Restgasmodell \"" +internesRestgas_ModellVorgabe + 
//						" \" wurde im InputFile " +
//						"zwar als valide akzeptiert, ist im Programm aber noch nicht implementiert worden.");
//			} catch (BirdBrainedProgrammerException e) {
//				e.stopBremo();
//			}	
		}else if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[3])){
			RESTGAS_MODELL=(InternesRestgas) new LWA(cp, false);
		}else if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[4])){
			RESTGAS_MODELL=(InternesRestgas) new LWA(cp,true);
		}else{
			RESTGAS_MODELL=null;
			try {
				throw new BirdBrainedProgrammerException(
						"Das ausgewaehlte Restgasmodell \"" +internesRestgas_ModellVorgabe + 
						" \" wurde im InputFile " +
						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. " +
				"Es fehlt der entsprechende else-if-Block oder das  Restgasmodell wurde noch nicht implementiert");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
			
		}
	}
}
