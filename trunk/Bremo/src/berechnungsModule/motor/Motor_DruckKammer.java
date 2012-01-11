package berechnungsModule.motor;

import bremo.parameter.CasePara;
import bremoExceptions.BirdBrainedProgrammerException;

public class Motor_DruckKammer extends Motor {

	private double V;
	protected Motor_DruckKammer(CasePara cp) {
		super(cp);
		V=super.CP.get_DruckKammerVolumen();
		
	}

	@Override
	public double get_BrennraumFlaeche(double time) {
		try{
			throw new BirdBrainedProgrammerException("Fuer den Motortyp \"DruckKammer\"" +
					"wurde diese Funktion nicht implementiert. \n Bis jetzt gab es auch keine " +
					"Wandwaermeuebergangsmodelle" +
					"die dies Unterstuezt");
		}catch(BirdBrainedProgrammerException bbpe){
			bbpe.stopBremo();
		}
		return 0;
		
	}

	@Override
	public double get_V(double time) {	
		return V;
	}

	@Override
	public double get_dV(double time) {		
		return 0;
	}

	@Override
	public boolean isHubKolbenMotor() {
		return false;
	}

	@Override
	public double get_Verdichtung() {
		return 0;
	}

	@Override
	public double get_V_MAX() {
		// TODO Auto-generated method stub
		return V;
	}

}
