package berechnungsModule.wallHeatTransfer;


import misc.VectorBuffer;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_reciprocatingPiston;
import bremo.parameter.*;


public class Hohenberg extends WallHeatTransfer{

	
	private Motor_reciprocatingPiston motor;
	private double C_1;
	private double C_2;
	private double mittlereKolbengeschwindigkeit;
	
	protected Hohenberg(CasePara cp) {	
		super(cp);
		this.cp=super.cp;
		this.motor=(Motor_reciprocatingPiston) super.motor;
		C_1 = 130;
		C_2 = 1.4;
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_rotSpeedInRotperSec();	//[m/s]
		super.feuerstegMult=0.3;
	}	

	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K)
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt){		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double alpha=C_1 *Math.pow( (motor.get_V(time)),-0.06)*Math.pow( (p*1E-5),0.8)*
			Math.pow(T,-0.4)*Math.pow((mittlereKolbengeschwindigkeit +C_2),0.8) ;
		return alpha;
	}

	
	@Override
	public double get_BrennraumFlaeche(double time) {	
		return motor.get_BrennraumFlaeche(time)+0.3*motor.get_FeuerstegFlaeche();
	}


}
