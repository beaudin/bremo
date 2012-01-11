package berechnungsModule.motor;

import bremo.parameter.CasePara;

class Motor_VolumenFunktionAusDatei extends Motor{
	
	public Motor_VolumenFunktionAusDatei(CasePara cp){
		super(cp);
	
	}

	public double get_V(double time) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double get_dV(double time) {
		// TODO Auto-generated method stub
		return 0;
	}


	public boolean isHubKolbenMotor() {
		return false;
	}

	@Override
	public double get_BrennraumFlaeche(double time) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double get_Verdichtung() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double get_V_MAX() {
		// TODO Auto-generated method stub
		return 0;
	}

}
