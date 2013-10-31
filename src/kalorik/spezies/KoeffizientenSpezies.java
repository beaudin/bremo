package kalorik.spezies;

import misc.HeizwertRechner;
import misc.PhysKonst;



/**
 * @author  hoenl, eichmeier
 */
public class KoeffizientenSpezies extends Spezies {


	private final double molarWeight; 						// kg / mol	
	private final double [][]thermodynKoeffs=new double [2][9];
	private final double temperaturGrenze; 						//normalerweise 1000 K	
	private double lhv; //Lower Heating Value in J/mol




	/**
	 * 
	 * @param thermodynKoeffs
	 * @param delta_hf298_mol
	 * @param lhv_mol
	 * @param anzO_Atome
	 * @param anzC_Atome
	 * @param anzH_Atome
	 * @param anzN_Atome
	 * @param name
	 */
	protected KoeffizientenSpezies(double [][]thermodynKoeffs,					
			double delta_hf298_mol,
			double lhv_mol,
			double anzO_Atome,
			double anzC_Atome,
			double anzH_Atome,
			double anzN_Atome,				
			String name) {
		super();			
		this.thermodynKoeffs[0] = thermodynKoeffs[0];
		this.thermodynKoeffs[1] = thermodynKoeffs[1];		
		this.delta_hf298_mol=delta_hf298_mol;
		this.temperaturGrenze=thermodynKoeffs[2][1];
		this.lhv=lhv_mol;
		this.anzO_Atome=anzO_Atome;
		this.anzC_Atome=anzC_Atome;
		this.anzH_Atome=anzH_Atome;
		this.anzN_Atome=anzN_Atome;	
		this.name=name;
		this.molarWeight=this.anzC_Atome*PhysKonst.get_M_C()+
				this.anzH_Atome*PhysKonst.get_M_H()+
				this.anzN_Atome*PhysKonst.get_M_N()+
				this.anzO_Atome*PhysKonst.get_M_O();

	}	

	//berechnet h mit den Koeffizienten für niedrige Temperturen T<1000K
	private double calc_h_low(double T){	
		double h = 0;					
		h = thermodynKoeffs[0][0] * (-1 / (T));
		h = h + thermodynKoeffs[0][1] * Math.log(T);
		h = h + thermodynKoeffs[0][2] * T;
		h = h + thermodynKoeffs[0][3] * T * T / 2;
		h = h + thermodynKoeffs[0][4] * T * T * T / 3;
		h = h + thermodynKoeffs[0][5] * T * T * T * T / 4;
		h = h + thermodynKoeffs[0][6] * T * T * T * T * T / 5;

		return h*R_allg;

	}	

	//berechnet h mit den Koeffizienten für hohe Temperaturen T>1000K
	private double calc_h_high(double T){
		double h = 0;
		h = thermodynKoeffs[1][0] * (-1 / (T));
		h = h + thermodynKoeffs[1][1] * Math.log(T);
		h = h + thermodynKoeffs[1][2] * T;
		h = h + thermodynKoeffs[1][3] * T * T / 2;
		h = h + thermodynKoeffs[1][4] * T * T * T / 3;
		h = h + thermodynKoeffs[1][5] * T * T * T * T / 4;
		h = h + thermodynKoeffs[1][6] * T * T * T * T * T / 5;

		return h*R_allg ;		
	}

	//Berechnet h_abs so dass die Wahl des Nullpunkts der Enthalpie über
	// die Standardbildungsenthalpie frei gewählt werden kann	
	/**
	 * liefert h(T)-h(298.15) + Standardbildungsenthalpie(298.15) in [J / mol] für T in [K] 
	 * Der Nullpunkt wird durch die Wahl der Standardbildungsenthalpie bestimmt. vgl. Justi, Grill u.a.
	 */
	public double get_h_mol(double T){		

		double h=Double.NaN;

		if (T < temperaturGrenze)
		{			
			h=calc_h_low(T)-calc_h_low(298.15)+delta_hf298_mol;
		}
		else
		{
			h=calc_h_high(T)-calc_h_high(temperaturGrenze)+calc_h_low(temperaturGrenze)-calc_h_low(298.15)+delta_hf298_mol;
		}

		return h;
	}	


	/**
	 * liefert cp(T) in [J / mol K] für T in [K]	
	 */
	public double get_cp_mol(double T){

		double cp = 0;

		if (T < temperaturGrenze)
		{
			cp = thermodynKoeffs[0][0] / (T*T);
			cp = cp + thermodynKoeffs[0][1] / T;
			cp = cp + thermodynKoeffs[0][2];
			cp = cp + thermodynKoeffs[0][3] * T ;
			cp = cp + thermodynKoeffs[0][4] * T * T ;
			cp = cp + thermodynKoeffs[0][5] * T * T * T ;
			cp = cp + thermodynKoeffs[0][6] * T * T * T * T ;	

		}
		else
		{
			cp = thermodynKoeffs[1][0] / (T*T);
			cp = cp + thermodynKoeffs[1][1] / T;
			cp = cp + thermodynKoeffs[1][2];
			cp = cp + thermodynKoeffs[1][3] * T ;
			cp = cp + thermodynKoeffs[1][4] * T * T ;
			cp = cp + thermodynKoeffs[1][5] * T * T * T ;
			cp = cp + thermodynKoeffs[1][6] * T * T * T * T ;	

		}

		return (cp * R_allg);		
	}

	/**
	 * liefert die Koeffizienten zur Berechnung kalorischer Daten nach den JANAF-Polynomen 	
	 */
	public double[][] get_Koeffizienten() {

		return thermodynKoeffs;
	}	


	public double get_T_grenz() {
		return temperaturGrenze;
	}

	public double get_M() {		
		return this.molarWeight;
	}

	@Override
	public double get_Hu_mol() {		
		return this.lhv;
	}



}
