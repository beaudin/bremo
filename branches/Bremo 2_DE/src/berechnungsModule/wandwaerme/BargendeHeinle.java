package berechnungsModule.wandwaerme;

import berechnungsModule.Berechnung.DVA_Homogen_EinZonig;
import berechnungsModule.Berechnung.DVA_homogen_ZweiZonig;
import berechnungsModule.Berechnung.DVA_DualFuel;
import berechnungsModule.Berechnung.APR_homogen_EinZonig;
import berechnungsModule.Berechnung.Zone;
import berechnungsModule.motor.Motor_HubKolbenMotor;
import bremo.parameter.CasePara;

public class BargendeHeinle extends WandWaermeUebergang {
	
	private CasePara cp;
	private Motor_HubKolbenMotor motor;
	private double T_w, T_m, k, w, c_k, u_v, T_zzp, p_zzp, kappa, delta_k, w_k, d;
//	private double T_uv, T_v, y;
	private double dy, dx, dt, x_alt=0.05, y_alt=0, t_alt=0, fortschritt_alt=0, u_v_temp;	//Für u_v Heinle
	private boolean Initialisierung=true;					//Für u_v Heinle
	private boolean kappa_AusInputfile = true;
	private static final double C = 27.97*1e3;
	
	
	protected BargendeHeinle(CasePara cp) {
		super(cp);
		this.cp = super.cp;
		this.motor =(Motor_HubKolbenMotor) super.motor;
		d = motor.get_Bohrung();
		super.feuerstegMult=0.25;
		kappa = cp.get_Kappa_Bargende();				//kappa in FVV konstant!
		//delta_k = cp.get_Delta_k();					//Faktor zur gewichtung Verbrennungsterm
		w_k = 3; //Laut Heinle S.21 //cp.get_W_k();		//Berechnung Bargende konservativ mit w_k = 1, Grill mit w_k = 3
	}
	
	public double get_WaermeUebergangsKoeffizient(double time, Zone[] zonen_IN, double fortschritt) {
		
		//Turbulenz k aus Berechnungsmodell
		k = cp.BERECHNUNGS_MODELL.get_turbFaktor(zonen_IN, time);
		
		double p = zonen_IN[0].get_p();										//Zylinderdruck
		double lambda = zonen_IN[0].get_ggZone().get_lambda();				//Luftverhältnis
		double Lst = cp.MASTER_EINSPRITZUNG.get_spezKrstALL().get_Lst();	//Mindestluftbedarf
		double R = get_R_brennraum(time, zonen_IN);							//Gaskonstante
		double r = (lambda-1)/(lambda+1/Lst);								//Luftgehalt
		double T = get_Tmb(zonen_IN);   									//Mittlere Brennraumtemperatur
		
		//Bestimmung der gemittelten Wandtemp. aus gewichteten Flächentemp
		Motor_HubKolbenMotor hkm = ((Motor_HubKolbenMotor)motor);
		double pistonSurf = hkm.get_Kolbenflaeche()+feuerstegMult*hkm.get_FeuerstegFlaeche();
		double headSurf = hkm.get_fireDeckArea();
		double cylWallSurf = hkm.get_CylinderLinerArea(time);
		double totalSurf = pistonSurf+headSurf+cylWallSurf;
		double T_cyl = cp.get_T_Cyl();										//Temp der einzelnen Flächen
		double T_piston = cp.get_T_Piston();	
		double T_head = cp.get_T_Head();
		T_w = ((pistonSurf*T_piston)+(headSurf*T_head)+(cylWallSurf*T_cyl))
				/totalSurf;													//s. Bargende Gl. 6.3	
		T_m = (T+T_w)/2; 													//Gemittelte Temp. an Grenzschicht
		
		//Wärmeübergangsrelevante Geschwindigkeit
		c_k = motor.get_Kolbengeschwindigkeit(time);						//momentane Kolbengeschwindigkeit
		u_v = this.get_u_v(p, T, fortschritt, time, zonen_IN);															//WWÜ-relevante Konvektionsgeschw. durch Verbrennung
//		w = Math.sqrt(8/w_k*k+Math.pow(c_k,2)+Math.pow(u_v,2))/2;			//+u_v^2				//(8/3)*k statt 8*k 
		w = Math.sqrt( (2/3*k+Math.pow(c_k,2)/4+Math.pow(u_v,2)) );
		
		double verbrennungsterm = get_Verbrennungsterm(p, T, fortschritt, time, zonen_IN);
		
		double alpha = C*(1.15*r+2.02)/(Math.pow(R*(2.57*r+3.55),0.78)) *Math.pow(motor.get_V(time),-0.073) 
				*Math.pow(T_m, -0.477) *Math.pow(p*1e-5,0.78) *Math.pow(w,0.78)*verbrennungsterm;
		return alpha;
	}

	public double get_Verbrennungsterm(double p, double T, double fortschritt, double time, Zone[] zonen_IN) {
		
		double T_uv, T_v, y; 
		double verbrennungsbeginn = 0.001;
		
		//Wenn kappa im Inputfile nicht definiert (-5.55 aus Casepara zurück),
		//wird mit dem Zonen-kappa gerechnet
		if(kappa==-5.55){
			kappa_AusInputfile = false;
		}
		if (kappa_AusInputfile == false){
			kappa = zonen_IN[0].get_ggZone().get_kappa(fortschritt);
		}
		
		double X = fortschritt;							//Normierter Summenbrennverlauf X durch fortschritt ausgedrückt
		
		if (fortschritt <= verbrennungsbeginn){						//Hier wird ZZP bei Umsatz von 2% angenommen
			verbrennungsbeginn = fortschritt;
			T_zzp = T;									//Temp und Druck zum ZZP "festhalten"
			p_zzp = p;
		}
		
		//Verbrennungsterm für Hochdruckteil zwischen Umsatz von 2%-99%
		//aus polytroper Verdichtung ausgehend vom ZZP
		if ((fortschritt > verbrennungsbeginn) && (fortschritt <= 0.99)){			
			T_uv = T_zzp * Math.pow( p/p_zzp, (kappa-1)/kappa);						
			T_v = 1/X*T+(X-1)/X*T_uv;
			y = X * T_v/T; //Eigentlich V_v/V
			return (Math.pow(y,2/3)*(T_v-T_uv)+(T_uv-T_w))/(T-T_w);
		}
		else {
			return 1;
		}
		
	}
	
	public double get_u_v(double p, double T, double fortschritt, double time, Zone[] zonen_IN) {
		//PROBLEM: bei DVA wird das für einen Zeitschritt mehrfach aufgerufen!
		
		double K=0; 
		double T_uv, T_v, y;
		double verbrennungsbeginn = 0.001, X = fortschritt;
		
		//Verbrennungsterm für Hochdruckteil zwischen Umsatz von 2%-99%
		//aus polytroper Verdichtung ausgehend vom ZZP
		if ((fortschritt > verbrennungsbeginn) && (fortschritt <= 0.99)&&
			fortschritt!=fortschritt_alt){	
			
			T_uv = T_zzp * Math.pow( p/p_zzp, (kappa-1)/kappa);						
			T_v = 1/X*T+(X-1)/X*T_uv;
			y = X * T_v/T; //Eigentlich V_v/V
				
//			double rho_v = m*T/T_v/motor.get_V(time);
//			double rho_uv = m*T/T_uv/motor.get_V(time);
			
			K = T_uv/T_v; //Eigentlich rho_v/rho_uv
			
			dx=X-x_alt;
			dy=y-y_alt;
			dt=cp.SYS.WRITE_INTERVAL_SEC; //time-t_alt;
			
			if (Initialisierung){				
//				x_alt=X;
//				y_alt=y;
//				t_alt=time;
				dx=0; //damit 0 rauskommt
				dy=0; //damit 0 rauskommt
				dt=1; //Irgendwas, damit nicht durch Null geteilt wird
				Initialisierung=false;
			}
			
			x_alt=X;
			y_alt=y;
			t_alt=time;
			fortschritt_alt=fortschritt;
			
			u_v_temp=Math.pow(y,1/6)*d/4*(dy/dt-K*y/X*dx/dt);
			
			return u_v_temp;
		}
		if(fortschritt==fortschritt_alt){
			return u_v_temp;
		}
		else {
			return 0;
		}
	}
	
	public double get_BrennraumFlaeche(double time) {
		return motor.get_BrennraumFlaeche(time)+0.25*motor.get_FeuerstegFlaeche(); //TODO: Feuerstegterm Heinle
	}

}
