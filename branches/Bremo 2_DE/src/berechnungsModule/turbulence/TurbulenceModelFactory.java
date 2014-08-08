package berechnungsModule.turbulence;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremo.parameter.CasePara.MakeMeUnique;
import bremoExceptions.BirdBrainedProgrammerException;

public class TurbulenceModelFactory extends ModulFabrik {
	
	public static final String MODEL_FLAG="turbModel";
	public static final  String[] TURBLMODELS={"FromFile" , "k", "k-eps", "ohne"};
	private TurbulenceModel turbModel=null;
	
	public TurbulenceModelFactory(CasePara cp, MakeMeUnique mmu) {
		super(cp);
	}
	
	private void createTurbulenceModel(){
		String modelFlagInput=super.get_ModulWahl(MODEL_FLAG, TURBLMODELS);

		if(modelFlagInput.equals("FromFile"))
			turbModel=new Turbulence_FromFile(super.CP) ;
		else if(modelFlagInput.equals("k"))
			turbModel=new Turbulence_k(super.CP);
		else if(modelFlagInput.equals("k-eps"))
			turbModel=new Turbulence_k_eps(super.CP); //für Bargende
		else if(modelFlagInput.equals("ohne"))
			turbModel=new Turbulence_Zero(super.CP);
		else{
			try {
				throw new BirdBrainedProgrammerException(
						"The chosen turbulence model \"" +modelFlagInput + " \" was considered to be valid  " +
						"during the preprocessing of the input file but the model has not been implemented yet! \n" +
						"Blame the german!");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
			turbModel=null;
		}
	}
	
	
	public TurbulenceModel get_TurbulenceModel(){
		if(turbModel==null){
			createTurbulenceModel();
		}
		return turbModel;
	}

}
