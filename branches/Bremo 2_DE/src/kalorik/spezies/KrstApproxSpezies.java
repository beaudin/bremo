/**
 * 
 */
package kalorik.spezies;

import bremoExceptions.MiscException;
import misc.PhysKonst;

/**
 * 
 * 
 * Diese Klasse approximiert die Enthalpie von Kraftstoffen auf Basis des o/h/c-Verhältnisses nach einem 
 * Ansatz von Grill. Ansonsten entspricht das Verhalten einer KrstApproxSpezies 
 * dem einer KoeffizientenSpezies.
 *
 *@author eichmeier, neurohr
 */
public class KrstApproxSpezies extends Spezies  {
	//TODO Ueberpruefen ob das Modell stimmt!!!
	private final double molarWeight; 
	private final double  N_Hn, N_On, K_H, K_O;
	private double lhv;
	
	
	/**
	 * 
	 * @param molekularGewicht (auskommentiert)
	 * @param h_evap_mol -->molare Verdampfungsenthalpie (auskommentiert)
	 * @param Hu_mol --> unterer Heizwert in J/mol 
	 * @param anzO_Atome --> Anzahl der o-Atome
	 * @param anzC_Atome
	 * @param anzH_Atome
	 * @param anzN_Atome
	 * @param name
	 */
	protected KrstApproxSpezies(//double molekularGewicht,							
			//double h_evap_mol,
			double Hu_mol,
			double anzO_Atome,
			double anzC_Atome,
			double anzH_Atome,
			double anzN_Atome,				
			String name){
		
		super();		
		this.lhv=Hu_mol;
		this.anzO_Atome=anzO_Atome;
		this.anzC_Atome=anzC_Atome;
		this.anzH_Atome=anzH_Atome;
		this.anzN_Atome=anzN_Atome;
		 //so wird sichergestellt, dass der Name nicht mit dem einer KoeffSpezies uebereinstimmt
		this.name=name;//+"_krstApprox";
		
		this.molarWeight=this.anzC_Atome*PhysKonst.get_M_C()+
				this.anzH_Atome*PhysKonst.get_M_H()+
				this.anzN_Atome*PhysKonst.get_M_N()+
				this.anzO_Atome*PhysKonst.get_M_O();
		
		N_Hn=anzH_Atome/anzC_Atome;
		N_On=anzO_Atome/anzC_Atome;//Eichmeier
//		N_On=anzO_Atome/anzH_Atome;//lt. Paper ACHTUNG DRUCKFEHLER IM SAE PAPER (FVV-Zylindermodul)
		
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
				throw new MiscException(
			"KrstApproxSpezies: Krafstoffe mit einem O/C-Verhältnis >1 können mit dem Modell nach Grill nicht berechnet werden");
			}catch(MiscException e){
				e.printStackTrace();
				e.stopBremo();		
			}
		}
		this.delta_hf298_mol=-1*calc_h_mol(298.15);		
	}

	public double get_h_mol(double T) {	
		double h=-1;
		if(T<=2000){		
			h=calc_h_mol(T) + delta_hf298_mol;
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
				-46.506)*get_M();	//Neurohr2014, Umrechnung in J/molK erforderlich !!
	}


	private double calc_h_mol(double T){		
		//Die Formel nach Grill liefert die Enthalpie in J/kg		
		return (N_Hn*(K_H+K_O)*(
				2.235*0.00000000001*T*T*T*T*T+
				2.235*0.00000001*T*T*T*T
				-5.587*0.0001*T*T*T
				+1.763*T*T
//				-46.506*T-126575.419))*get_M();	//Eichmeister
				-46.506*T))*get_M();		//Neurohr2014, nach SAE Paper2007-01-0936 
		
	}

	public double get_M() {		
		return molarWeight;
	}

	@Override
	public double get_Hu_mol() {		
		return lhv;
	}
	


	
	

}
