/**
 * 
 */
package kalorik.spezies;

/**
 * 
 * 
 * Diese Klasse approximiert die Enthalpie von Kraftstoffen auf Basis des o/h/c-Verhältnisses nach einem 
 * Ansatz von Grill. Ansonsten entspricht das Verhalten einer KrstApproxSpezies 
 * dem einer KoeffizientenSpezies.
 *
 *@author eichmeier
 */
public class KrstApproxSpezies extends Spezies  {
	//TODO Ueberpruefen ob das Modell stimmt!!!
	private final double molekularGewicht; 
	private final double  N_Hn, N_On, K_H, K_O;
	
	
	/**
	 * 
	 * @param molekularGewicht
	 * @param h_evap_mol -->molare Verdampfungsenthalpie
	 * @param Hu_mol --> unterer Heizwert in J/mol
	 * @param anzO_Atome --> Anzahl der o-Atome
	 * @param anzC_Atome
	 * @param anzH_Atome
	 * @param anzN_Atome
	 * @param name
	 */
	public KrstApproxSpezies(double molekularGewicht,							
			double h_evap_mol,
			double Hu_mol,
			double anzO_Atome,
			double anzC_Atome,
			double anzH_Atome,
			double anzN_Atome,				
			String name,
			boolean isToIntegrate){
		
		super(isToIntegrate);		
		this.molekularGewicht=molekularGewicht;
		this.h_evap_mol=h_evap_mol;
		this.Hu_mol=Hu_mol;
		this.anzO_Atome=anzO_Atome;
		this.anzC_Atome=anzC_Atome;
		this.anzH_Atome=anzH_Atome;
		this.anzN_Atome=anzN_Atome;
		 //so wird sichergestellt, dass der Name nicht mit dem einer KoeffSpezies uebereinstimmt
		this.name=name+"krstApprox";
		
		N_Hn=anzH_Atome/anzC_Atome;
		N_On=anzO_Atome/anzC_Atome;
		
		K_H=-0.0265*N_Hn*N_Hn*N_Hn*N_Hn*N_Hn
			+0.3783*N_Hn*N_Hn*N_Hn*N_Hn
			-2.0650*N_Hn*N_Hn*N_Hn
			+5.4209*N_Hn*N_Hn
			-7.1184*N_Hn
			+4.7704;
			
		
		
		if (N_On==0){
			K_O=0;
		}else if (0<N_On&&N_On<=1){
			
			K_O=2.1982*N_On*N_On*N_On*N_On
				-5.4024*N_On*N_On*N_On
				+4.3859*N_On*N_On
				-1.4431*N_On
				+0.0314;
			
		}else{
			K_O=Double.NaN;
			try{
				throw new IllegalArgumentException(
			"KrstApproxSpezies: Krafstoffe mit einem O/C-Verhältnis >1 können mit dem Modell nach Grill nicht berechnet werden");
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				System.exit(0);		
			}
		}
		
		this.delta_hf298_mol=-1*calc_h_mol(298.15);		
	}

	public double get_M() {	
		return molekularGewicht;
	}


	public double get_h_mol(double T) {	
		double h=-1;
		if(T<=2000){		
			h=calc_h_mol(T)-delta_hf298_mol;
		}else{
			try{
			throw new IllegalArgumentException(
			"KrstApproxSpezies: Für Temperaturen über 2000K ist das Modell von Grill nicht geeignet");	
			}catch(IllegalArgumentException e){
				e.printStackTrace();
				System.exit(0);		
			}
		}
		
		return h;
	}

	public double get_cp_mol(double T) {
		
		return N_Hn*(K_H+K_O)*(
				5*2.235*0.00000000001*T*T*T*T+
				4*2.235*0.00000001*T*T*T
				-3*5.587*0.0001*T*T
				+2*1.763*T
				-46.506);	
	}


	private double calc_h_mol(double T){		
		//Die Formel nach Grill liefert die Enthalpie in J/kg		
		return (N_Hn*(K_H+K_O)*(
				2.235*0.00000000001*T*T*T*T*T+
				2.235*0.00000001*T*T*T*T
				-5.587*0.0001*T*T*T
				+1.763*T*T
				-46.506*T-126575.419))*get_M();	
		
	}
	


	
	

}
