package berechnungsModule.motor;

import berechnungsModule.ModulFabrik;
import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class MotorFabrik extends ModulFabrik {

	public static final String MOTOR_FLAG="Motor";
	public static final  String[] MOTORENTYPEN={"HubKolbenMotor" , "ausDatei"};

	public final Motor MOTOR;


	public  MotorFabrik(CasePara cp){
		super(cp);
		String motorVorgabe=get_ModulWahl(MOTOR_FLAG, MOTORENTYPEN);

		if(motorVorgabe.equals("HubKolbenMotor"))
			MOTOR=new Motor_HubKolbenMotor(cp) ;
		else if(motorVorgabe.equals("ausDatei"))
			MOTOR=null;
		else if(motorVorgabe.equals("DruckKammer"))
			MOTOR=null;
		else{
			try {
				throw new BirdBrainedProgrammerException(
						"Der ausgewaehlte Motroentyp \"" +motorVorgabe + " \" wurde im InputFile " +
						"zwar als valide akzeptiert ist im Programm aber nicht richtig eingepflegt worden. \n "  + 
				"Es fehlt der entsprechende else-if-Block oder der Motortyp wurde noch nicht implementiert");
			} catch (BirdBrainedProgrammerException e) {
				e.stopBremo();
			}
			MOTOR=null;
		}

	}

}
