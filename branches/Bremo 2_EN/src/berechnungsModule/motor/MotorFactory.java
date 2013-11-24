package berechnungsModule.motor;

import berechnungsModule.ModuleFactory;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class MotorFactory extends ModuleFactory {

	public static final String MOTOR_FLAG="Motor";
	public static final  String[] MOTORTYPES={"reciprocatingPistonEngine" , "fromFile", "pressureVessel"};

	public final Motor MOTOR;


	public  MotorFactory(CasePara cp){
		super(cp);
		String motorVorgabe=get_ModulWahl(MOTOR_FLAG, MOTORTYPES);

		if(motorVorgabe.equals("reciprocatingPistonEngine"))
			MOTOR=new Motor_reciprocatingPiston(cp) ;
		else if(motorVorgabe.equals("fromFile"))
			MOTOR=null;
		else if(motorVorgabe.equals("pressureVessel"))
			MOTOR=new Motor_pressureVessel(cp);
		else{
			try {
				throw new BirdBrainedProgrammerException(
						"The motor type  \"" +motorVorgabe + " \" you specified was accepted as" +
								"valid in the inputFile but it seems that it " +
								"has not been implemented yet! Feel fre to do so!");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
			MOTOR=null;
		}

	}

}
