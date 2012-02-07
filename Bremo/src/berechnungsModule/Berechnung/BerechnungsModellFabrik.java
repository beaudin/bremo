package berechnungsModule.Berechnung;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class BerechnungsModellFabrik extends ModulFabrik {
	public static final String DVAmodulFlag="berechnungsModell";
	public static final String [] DVA_MODULE={"DVA_1Zonig", "DVA_2Zonig","DVA_DualFuel"};
	public  final BerechnungsModell BERECHNUNGS_MODELL;
	
	public BerechnungsModellFabrik(CasePara cp){
		super(cp);
		String berechnungsModellVorgabe=get_ModulWahl(DVAmodulFlag, DVA_MODULE);
		
		if(berechnungsModellVorgabe.equals("DVA_1Zonig")){			
			BERECHNUNGS_MODELL=new DVA_Homogen_EinZonig(cp);

		}else if(berechnungsModellVorgabe.equals("DVA_2Zonig")){
			BERECHNUNGS_MODELL=new DVA_homogen_ZweiZonig(cp);	

		}else if(berechnungsModellVorgabe.equals("DVA_DualFuel")){			
			BERECHNUNGS_MODELL=new DVA_DualFuel(cp);		
		}else{
			try {
				throw new BirdBrainedProgrammerException(
						"Das DVA-Modell \"" +berechnungsModellVorgabe + 
						" \" wurde im InputFile " +
						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n" +
				"Es fehlt der entsprechende else-if-Block oder das  BerechnuingsModell wurde noch nicht implementiert");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
			BERECHNUNGS_MODELL=null;
		}
		
	}

}
