package berechnungsModule.wandwaerme;


import misc.VektorBuffer;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.*;
import bremoExceptions.BirdBrainedProgrammerException;


public class Spielwiese extends WandWaermeUebergang{

	
	private Motor_HubKolbenMotor motor;
	private double C_1;
	private double C_2;
	private double C_u;
	private double C_b;
	private double mittlereKolbengeschwindigkeit;
	
	protected Spielwiese(CasePara cp) {	
		super(cp);
		this.cp=super.cp;
		this.motor=(Motor_HubKolbenMotor) super.motor;
		C_u = 2;
		C_b = 0.7;
		C_1 = 130;
		C_2 = 1.4;
		mittlereKolbengeschwindigkeit= 2*motor.get_Hub()*cp.get_DrehzahlInUproSec();	//[m/s]
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
	
	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K) für die unverbrannte Zone
	public double get_WaermeUebergangsKoeffizientUnverbrannt(double time, Zone[] zonen_IN, double fortschritt){		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		//double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		double T=zonen_IN[0].get_T();	//Temperatur unverbrannte Zone
		double alpha=C_u*C_1 *Math.pow( (motor.get_V(time)),-0.06)*Math.pow( (p*1E-5),0.8)*
			Math.pow(T,-0.4)*Math.pow((mittlereKolbengeschwindigkeit +C_2),0.8) ;
		return alpha;
	}	
	
	//Hiermit bekommt der Benutzer den Alpha-Wert in W/(m^2K) für die verbrannte Zone
	public double get_WaermeUebergangsKoeffizientVerbrannt(double time, Zone[] zonen_IN, double fortschritt){		
		double p=zonen_IN[0].get_p();	//Zylinderdruck
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		//double T=zonen_IN[1].get_T();	//Temperatur verbrannte Zone
		double alpha=C_b*C_1 *Math.pow( (motor.get_V(time)),-0.06)*Math.pow( (p*1E-5),0.8)*
			Math.pow(T,-0.4)*Math.pow((mittlereKolbengeschwindigkeit +C_2),0.8) ;
		return alpha;
	}
	
	public double get_WandWaermeStromDichteUnverbrannt(double time, Zone[] zonen_IN, double fortschritt){	
	double qw=0;
	double alpha=this.get_WaermeUebergangsKoeffizientUnverbrannt(time, zonen_IN, fortschritt);
	if(motor.isHubKolbenMotor()){

		if(Double.isNaN(T_head))	{
			T_head=cp.get_T_Head();	
		}
		
		double T=zonen_IN[0].get_T();	//Temperatur unverbrannte Zone
		qw=alpha*(T-T_head);

	}else{
		try{
			throw new BirdBrainedProgrammerException("WHT-Models " +
					"for non Piston engines must override this method!");					
		}catch(BirdBrainedProgrammerException bbpe){
			bbpe.stopBremo();
		}
	}
	return qw; // [W/m^2]	
	}

	public double get_WandWaermeStromDichteVerbrannt(double time, Zone[] zonen_IN, double fortschritt){	
	double qw=0;
	double alpha=this.get_WaermeUebergangsKoeffizientVerbrannt(time, zonen_IN, fortschritt);
	if(motor.isHubKolbenMotor()){
		
		if(Double.isNaN(T_piston))	{
			T_piston=cp.get_T_Piston();	
		}
		
		double T=get_Tmb(zonen_IN);		//Mittlere Brennraumtemperatur
		//double T=zonen_IN[1].get_T();	//Temperatur verbrannte Zone
		qw=alpha*(T-T_piston);

	}else{
		try{
			throw new BirdBrainedProgrammerException("WHT-Models " +
					"for non Piston engines must override this method!");					
		}catch(BirdBrainedProgrammerException bbpe){
			bbpe.stopBremo();
		}
	}
	return qw; // [W/m^2]	
	}

}
