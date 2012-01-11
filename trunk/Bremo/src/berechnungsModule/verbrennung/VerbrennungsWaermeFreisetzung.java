package berechnungsModule.verbrennung;

import berechnungsModule.ModulPrototyp;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public abstract class VerbrennungsWaermeFreisetzung extends ModulPrototyp {
	
	public static final String VERBRENNUNGSWAERME_FLAG="verbrennung";
	public  final static String[] VERBRENNUNGSWAERMETYPEN={"DVA_homogen"};	
	private static VerbrennungsWaermeFreisetzung waermeFreisetzungModell=null;
	
	public abstract double get_dmBurn(double time);
	public abstract double get_dQBurn(double time);
	
	public VerbrennungsWaermeFreisetzung get_Instance(CasePara cp){
		
		if(waermeFreisetzungModell==null){
			String waermeFreisetzungsModellVorgabe=get_ModulWahl(VERBRENNUNGSWAERME_FLAG, VERBRENNUNGSWAERMETYPEN, cp);
			if(waermeFreisetzungsModellVorgabe.equals("DVA_homogen")){
				waermeFreisetzungModell=new DVA_homogen(cp);
			}
			
			if(waermeFreisetzungModell==null){
				try {
					throw new BirdBrainedProgrammerException(
							"Das ausgewaehlte Verbrennungsmodul \"" +waermeFreisetzungsModellVorgabe + 
							" \" wurde im InputFile " +
							"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. " +
					"Es fehlt der entsprechende else-if-Block oder der Verbrennungsmodul wurde noch nicht implementiert");
				} catch (BirdBrainedProgrammerException e) {
					e.stopBremo();
				}
			}
		}
		return waermeFreisetzungModell;		
	}
	
	
	
	
	

}
