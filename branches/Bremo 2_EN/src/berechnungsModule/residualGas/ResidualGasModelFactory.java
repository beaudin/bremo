package berechnungsModule.residualGas;

import berechnungsModule.ModuleFactory;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class ResidualGasModelFactory extends ModuleFactory {

	public static final String RESIDUAL_GAS_MODEL_FLAG="residualGasModel";
	public  final static String[] RESIDUAL_GAS_MODELS={"userInput", 
															"MuellerBertling",
															"fillAndEmpty"};	
	public final ResidualGas RESIDUAL_GAS_MODEL;
	
	public ResidualGasModelFactory(CasePara cp){
		super(cp);
		
		String internesRestgas_ModellVorgabe=
			get_ModulWahl(RESIDUAL_GAS_MODEL_FLAG, RESIDUAL_GAS_MODELS);
		
		if(internesRestgas_ModellVorgabe.equals(RESIDUAL_GAS_MODELS[0])){
			RESIDUAL_GAS_MODEL=(ResidualGas) new ResidualGasUserInput(cp);
			
		}else if(internesRestgas_ModellVorgabe.equals(RESIDUAL_GAS_MODELS[1])){
//			restgasModell=(InternesRestgas) new MuellerBertling(cp);
			RESIDUAL_GAS_MODEL=null;
			try {
				throw new BirdBrainedProgrammerException(
						"The chosen residual gas model \"" +internesRestgas_ModellVorgabe + 
						" \" has been identified as valid " +
						"but it seems that it hasn't been implemented yet. " +
				"Feel free to do so ;-)");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
			
		}else if(internesRestgas_ModellVorgabe.equals(RESIDUAL_GAS_MODELS[2])){
			RESIDUAL_GAS_MODEL=(ResidualGas) new LWA(cp);
		}else{
			RESIDUAL_GAS_MODEL=null;
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
