package berechnungsModule.wallHeatTransfer;

import berechnungsModule.ModuleFactory;
import berechnungsModule.motor.Motor;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;
import bremoExceptions.ParameterFileWrongInputException;

public class WallHeatTransferModelFactory extends ModuleFactory {
	
	public static final String WHT_MODEL_FLAG="wallHeatTransferModel";
	private static final String WHT_MODEL_FLAG_FILL_EMPTY="wallHeatTransferModel_FillAndEmpty";
	public  static final String[] WHT_MODELS
	={"WoschniHuber", "Woschni", "Hohenberg", "Hensel", "Bargende", "Chang","without","fromFile"};
	public final  WallHeatTransfer WHT_MODEL;
	public final  WallHeatTransfer WHT_MODEL_FILL_EMPTY;
	
	public WallHeatTransferModelFactory(CasePara cp){
		
		super(cp);
		
		WHT_MODEL=this.initializeWallHeatTransferModel(WHT_MODEL_FLAG, WHT_MODELS);
		
		if(CP.RESIDUAL_GAS_MODEL.involvesGasExchangeCalc())
			WHT_MODEL_FILL_EMPTY
				=this.initializeWallHeatTransferModel(WHT_MODEL_FLAG_FILL_EMPTY, WHT_MODELS);		
		else
			WHT_MODEL_FILL_EMPTY=null;
	}
	
	
	
	private WallHeatTransfer initializeWallHeatTransferModel(String flag,String [] modelle){
		String errMsg="The chosen wall heat transfer model is only valid for reciprocating piston engines." +
				"The simulation will be performed adiabatically!";
		String wandwaermemodellVorgabe=get_ModulWahl(flag,modelle);
		WallHeatTransfer temp = null;
		Motor motor=CP.MOTOR;
		try{
			if(wandwaermemodellVorgabe.equals("WoschniHuber")){
				if(motor.isHubKolbenMotor()){
					temp=new WoschniHuber(CP);
				}else{					
					throw new ParameterFileWrongInputException(errMsg);
				}
			}
			else if(wandwaermemodellVorgabe.equals("Woschni")){
				if(motor.isHubKolbenMotor()){
					temp=new Woschni(CP);						
				}else{
					throw new ParameterFileWrongInputException(errMsg);
				}
			}
			else if(wandwaermemodellVorgabe.equals("Hohenberg")){
				if(motor.isHubKolbenMotor()){
					temp=new Hohenberg(CP);						
				}else{
					throw new ParameterFileWrongInputException(errMsg);
				}
			}
			else if(wandwaermemodellVorgabe.equals("Chang")){
				if(motor.isHubKolbenMotor()){
					temp=new Chang(CP);
				}else{
					throw new ParameterFileWrongInputException(errMsg);
				}
			}
			else if(wandwaermemodellVorgabe.equals("Hensel")){
				if(motor.isHubKolbenMotor()){
					temp=new Hensel(CP);
				}else{
					throw new ParameterFileWrongInputException(errMsg);
				}
			}
			else if(wandwaermemodellVorgabe.equals("Bargende")){	
				if(motor.isHubKolbenMotor()){

					temp=null;// TODO Konstruktor des Wandwaermemodells einfuegen --> is das Modell schon implementiert
				}else{
					throw new ParameterFileWrongInputException(errMsg);
				}
			}
			else if(wandwaermemodellVorgabe.equals("fromFile")){				
				temp=new FromFile(CP);				
			}
			else if(wandwaermemodellVorgabe.equals("without")){
				temp=new PerfektIsoliert(CP);				
			}				
		}
		catch(ParameterFileWrongInputException e){
			e.log_Warning();
			temp=new PerfektIsoliert(CP);	
		}

		if(temp==null){			
			try {
				throw new BirdBrainedProgrammerException(
						"The chosen wall heat transfer model \"" + wandwaermemodellVorgabe + "\" " +
								"has been validated in the inputFile but it seems that the model has not been implemented yet." +
								"Feel free to do so!");
			} catch (BirdBrainedProgrammerException e) {				
				e.log_Warning();
			}
			temp=new PerfektIsoliert(CP);			
		}
		return temp;		
	}

}
