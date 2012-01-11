package berechnungsModule.internesRestgas;

import berechnungsModule.ModulPrototyp;
import bremo.main.Bremo;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public abstract class InternesRestgas extends ModulPrototyp {
	protected final CasePara CP;
	public static final String INTERNES_RESTGAS_MODELL_FLAG="internesRestgasModell";
	public  final static String[] INTERNES_RESTGAS_MODELLE={"RestgasVorgabe", 
															"MuellerBertling",
															"LWA"	};	
	
	
	protected InternesRestgas(CasePara cp){
		CP=cp;
	}
	
	
	/**
	 * 
	 * @return liefert die Masse des internen Restgases in [kg/ASP]
	 */
	public abstract double get_mInternesRestgas_ASP();
	
	
	private static InternesRestgas restgasModell=null;	
	public static InternesRestgas get_Instance(CasePara cp){

		if(restgasModell==null){
			String internesRestgas_ModellVorgabe=
				get_ModulWahl(INTERNES_RESTGAS_MODELL_FLAG, INTERNES_RESTGAS_MODELLE, cp);
			if(internesRestgas_ModellVorgabe.equals("RestgasVorgabe")){
				restgasModell=(InternesRestgas) new RestgasVorgabe(cp);
				
			}else if(internesRestgas_ModellVorgabe.equals("MuellerBertling")){
//				restgasModell=(InternesRestgas) new MuellerBertling(cp);
				
			}else if(internesRestgas_ModellVorgabe.equals("LWA")){
				restgasModell=(InternesRestgas) new LWA(cp);
			}			

			if(restgasModell==null){
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
		return restgasModell;		
	}
	
	
	
	
	public InternesRestgas get_Instance(){
		return get_Instance(Bremo.get_casePara());
	}


}
