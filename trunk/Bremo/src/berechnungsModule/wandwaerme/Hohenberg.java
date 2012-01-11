package berechnungsModule.wandwaerme;


import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;


public class Hohenberg extends WandWaermeUebergang{

	
	private Motor_HubKolbenMotor motor;
	private CasePara cp;
	private double C_1;
	private double C_2;
	private double mittlereKolbengeschwindigkeit;
	
	protected Hohenberg(CasePara cp) {	
		super(cp);
		this.cp=super.cp;
		this.motor=(Motor_HubKolbenMotor) super.motor;
		C_1 = 130;
		C_2 = 1.4;
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_DrehzahlInUproSec();	//[m/s]
	}	

	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K)
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt){		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double alpha=C_1 *Math.pow( (motor.get_V(time)),-0.06)*Math.pow( (p*1E-5),0.8)*
			Math.pow(T,-0.4)*Math.pow((mittlereKolbengeschwindigkeit +C_2),0.8) ;
		if(((Double) alpha).isNaN()){
//			System.out.println("");
		}
		return alpha;
	}

	
}
