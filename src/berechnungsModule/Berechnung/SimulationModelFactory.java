package berechnungsModule.Berechnung;

import berechnungsModule.ModuleFactory;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class SimulationModelFactory extends ModuleFactory {
	public static final String SimulationModuleFlag="simulationModel";
	public static final String [] SIM_MODULES={"DVA_1Zone", 
		"DVA_2Zones",
		"DVA_DualFuel",
		"APR_CanteraMultiZoneHomogeneous",
		"APR_Cantera2Zone",
	"APR_CanteraMultiZoneRCCI"};	

	public  final SimulationModel SIMULATION_MODEL;	
	

	public SimulationModelFactory(CasePara cp){
		super(cp);
		String berechnungsModellVorgabe=get_ModulWahl(SimulationModuleFlag, SIM_MODULES);

		if(berechnungsModellVorgabe.equals("DVA_1Zone")){			
			SIMULATION_MODEL=new DVA_Homogen_EinZonig(cp);

		}else if(berechnungsModellVorgabe.equals("DVA_2Zones")){
			SIMULATION_MODEL=new DVA_homogen_ZweiZonig(cp);	
			
		}else if(berechnungsModellVorgabe.equals("DVA_DualFuel")){			
			SIMULATION_MODEL=new DVA_DualFuel(cp);	
			
		}else if(berechnungsModellVorgabe.equals("APR_CanteraMultiZoneHomogeneous")){			
			SIMULATION_MODEL=new APR_CanteraMultiZoneHomogeneous(cp);	
			
			
//		}else if(berechnungsModellVorgabe.equals("APR_Cantera2Zone")){			
//			BERECHNUNGS_MODELL=new APR_Cantera2Zone(cp);
			
		}else if(berechnungsModellVorgabe.equals("APR_CanteraMultiZoneRCCI")){			
			SIMULATION_MODEL=new APR_CanteraMultiZoneRCCI(cp);
			
			
		}else{
			try {
				throw new BirdBrainedProgrammerException(
						"The chosen model \"" +berechnungsModellVorgabe + 
						" \" has been validated in the InputFile " +
						"but it seems that the model has not been implemented yet. \n" +
						"Feel free to do so ");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
			SIMULATION_MODEL=null;
		}		
	}	
	

//doof aber anders gehts irgendwie nicht!
	public static  boolean callsCantera(CasePara cp){
		ModuleFactory mf =new ModuleFactory(cp);
		String berechnungsModellVorgabe=mf.get_ModulWahl(SimulationModuleFlag, SIM_MODULES);
		if(berechnungsModellVorgabe.equals("APR_CanteraMultiZoneHomogeneous")||
				berechnungsModellVorgabe.equals("APR_Cantera2Zone")||
				berechnungsModellVorgabe.equals("APR_CanteraMultiZoneRCCI"))
			return true;
		else
			return false;
	}
}
