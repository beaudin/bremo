package berechnungsModule.internesRestgas;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class InternesRestgasFabrik extends ModulFabrik {

	public static final String INTERNES_RESTGAS_MODELL_FLAG="internesRestgasModell";
	public  final static String[] INTERNES_RESTGAS_MODELLE={"RestgasVorgabe", 
															"MuellerBertling",
															"LWA"};	
	public final InternesRestgas RESTGAS_MODELL;
	
	public InternesRestgasFabrik(CasePara cp){
		super(cp);
		
		String internesRestgas_ModellVorgabe=
			get_ModulWahl(INTERNES_RESTGAS_MODELL_FLAG, INTERNES_RESTGAS_MODELLE);
		
		if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[0])){
			RESTGAS_MODELL=(InternesRestgas) new RestgasVorgabe(cp);
			
		}else if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[1])){
//			restgasModell=(InternesRestgas) new MuellerBertling(cp);
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
			
		}else if(internesRestgas_ModellVorgabe.equals(INTERNES_RESTGAS_MODELLE[2])){
			RESTGAS_MODELL=(InternesRestgas) new LWA(cp);
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
