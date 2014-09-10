package berechnungsModule.Berechnung;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class BerechnungsModellFabrik extends ModulFabrik {
	public static final String DVAmodulFlag="berechnungsModell";
	public static final String [] DVA_MODULE={"DVA_1Zonig", 
		"DVA_2Zonig",
		"DVA_DualFuel",
		"APR_1Zonig",//fuer Verlustteilung Frank Haertel
		"APR_1Zonig_Vibe",
		"APR_CanteraMultiZoneHomogeneous",
		"APR_Cantera2Zone",
		"APR_CanteraMultiZoneRCCI",
		"Weltformel"};	

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
			
		//fuer Verlustteilung Frank Haertel
		}else if(berechnungsModellVorgabe.equals("APR_1Zonig")){ 
		      BERECHNUNGS_MODELL=new APR_homogen_EinZonig(cp); 
		
		}else if(berechnungsModellVorgabe.equals("APR_1Zonig_Vibe")){ 
		      BERECHNUNGS_MODELL=new APR_homogen_EinZonig(cp,"Vibe"); 
		
		}else if(berechnungsModellVorgabe.equals("APR_CanteraMultiZoneHomogeneous")){			
			BERECHNUNGS_MODELL=new APR_CanteraMultiZoneHomogeneous(cp);	
			
			
//		}else if(berechnungsModellVorgabe.equals("APR_Cantera2Zone")){			
//			BERECHNUNGS_MODELL=new APR_Cantera2Zone(cp);
			
		}else if(berechnungsModellVorgabe.equals("APR_CanteraMultiZoneRCCI")){			
			BERECHNUNGS_MODELL=new APR_CanteraMultiZoneRCCI(cp);
			
		}else if(berechnungsModellVorgabe.equals("Weltformel")){
			BERECHNUNGS_MODELL=new Weltformel(cp);
			
			
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
	

//doof aber anders gehts irgendwie nicht!
	public static  boolean callsCantera(CasePara cp){
		ModulFabrik mf =new ModulFabrik(cp);
		String berechnungsModellVorgabe=mf.get_ModulWahl(DVAmodulFlag, DVA_MODULE);
		if(berechnungsModellVorgabe.equals("APR_CanteraMultiZoneHomogeneous")||
				berechnungsModellVorgabe.equals("APR_Cantera2Zone")||
				berechnungsModellVorgabe.equals("APR_CanteraMultiZoneRCCI"))
			return true;
		else
			return false;
	}
}
