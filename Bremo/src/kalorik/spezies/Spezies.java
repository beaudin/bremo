/**
 * 
 */
package kalorik.spezies;

import bremoExceptions.MiscException;
import misc.PhysKonst;

/**
 * @author juwe
 *
 */
public abstract class Spezies {	
																

	protected double delta_hf298_mol=-1; 						// Standardbildungsenthalpie J/mol
	protected double h_evap_mol=-1; 							// Verdampfungsenthalpie in J/mol
	protected double Hu_mol=-1; 								// unterer Heizwert in J/mol
	protected final double R_allg = PhysKonst.get_R_allg();		// J/mol K	
	protected String name=null;
	protected double anzO_Atome=-1;
	protected double anzC_Atome=-1;	
	protected double anzH_Atome=-1;	
	protected double anzN_Atome=-1;
	protected boolean isGasGemisch;
	protected boolean isToIntegrate;
//	public static Spezies [] allSpez =new Spezies[25]; //erstmal nicht mehr als 25 Spezies bitte
//	protected static int nmbrOfSpez=0; //dann wirds bei der ersten Spezies null
//	protected int index=-1;
//	private boolean wirdSchonIntegriert=false; //gibt an ob die Methode integrierMich shon aufgerufen wurde
	
	protected Spezies(){
		//diese Anweisung sorgt dafuer, dass alle Spezies AUTOMATISCH keine GasGemische sind 
		//Der GasGemsich-Konstruktor ueberschreibt diesen Wert
		isGasGemisch=false; 
		isToIntegrate=false;
//		this.isToIntegrate=isToIntegrate;
//		if(isToIntegrate){
//			nmbrOfSpez=nmbrOfSpez+1;
//			index=nmbrOfSpez-1;			
//			allSpez[index]=this; //Speichern der grade erzeugten Spezies im Vektor
//			wirdSchonIntegriert=true;
//		}
	}
	
	public abstract double get_M();							// kg / mol	
	public abstract double get_h_mol(double T);	
	public abstract double get_cp_mol(double T);

	
	
//	public static Spezies get_Spez(int i){
//		//TODO Fehlerabfrage einbauen fuer indexOutOfBounds
//		return allSpez[i];		
//	}
//	
	/**
	 * Diese Funktion sorgt dafuer, dass die entsprechende Spezies vom 
	 * Solver integriert wird
	 */
//	public void integrierMich(){	
//		if(wirdSchonIntegriert==false){
//			isToIntegrate=true;
//			nmbrOfSpez=nmbrOfSpez+1;
//			index=nmbrOfSpez-1;
//			allSpez[index]=this; //Speichern der grade erzeugten Spezies im Vektor
//			wirdSchonIntegriert=true;
//		}
//	}
	
//	public static int get_NmbrOfAllSpez(){
//		return nmbrOfSpez;
//	}
//	
//	public int get_index(){
//		return index;
//	}
	
	protected void set_isTointegrate(boolean isToint){
		this.isToIntegrate=isToint;
	}
	
	public String get_name() {
		return name;
	}
	
	public double get_delta_hf298_mol(){
		//die direkte Ausgabe von delta_hf298 wäre auch möglich, dann müsste aber eine Unterscheidung 
		//zwischen Gasgemischen und reinen Spezies erfolgen!
//		return get_h_mol(298.15);
		return delta_hf298_mol;
	}
	
	public double get_h_h298_mol(double T){
		//nicht schnell aber sicher
		return get_h_mol(T)-get_delta_hf298_mol();
	}
	
	
	public double get_R() {
		return R_allg/get_M();
	}
	
	
	public double get_O2_min() {
		if (get_Hu_mol()>0){
			return get_AnzC_Atome()+0.25*get_AnzH_Atome()-0.5*get_AnzO_Atome();
		}
		else {
			try{
				throw new MiscException("Es wurde versucht O2min einer Spezies abzufragen," +
						" die keinen Heizwert besitz");
			}catch(MiscException e){
				e.log_Warning();				
			}
			return Double.NaN;
		}
	}	
	
	
	public double get_Lst() {
		//TODO 20.04.2011 noch nicht getestet
		if (get_Hu_mol()>0){
			return get_O2_min()*PhysKonst.get_M_O2()/get_M()/PhysKonst.get_mass_O2_Luft();
		}
		else {
			try{
				throw new MiscException("Es wurde versucht Lst einer Spezies abzufragen," +
						" die keinen Heizwert besitz");
			}catch(MiscException e){
				e.log_Warning();				
			}
			return Double.NaN;
		}
	}	
	

	
	
	/////////////////////////////////////////////////////////////////////	
	//massebezogene Größen
	/////////////////////////////////////////////////////////////////////
	public double get_kappa(double T){
		double cp=get_cp_mol(T);
		return cp/(cp-R_allg);
	}


	public double get_cv_mol(double T){
		return get_cp_mol(T)-R_allg;
	}
	

	public double get_u_mol(double T){
		return get_h_mol(T)-R_allg*T;
	}
	
	
	public double get_cp_mass (double T) {
		return get_cp_mol(T)/get_M();
	}
	

	public double get_cv_mass (double T) {
		return get_cv_mol(T)/get_M();
	}
	

	public double get_h_mass (double T) {
		return get_h_mol(T)/get_M();
	}
	

	public double get_h_h298_mass (double T) {
		return get_h_h298_mol(T)/get_M();
	}
	

	public double get_u_mass (double T) {
		return get_u_mol(T)/get_M();
	}
	
	public double get_AnzC_Atome() {
		
		return anzC_Atome;
	}

	
	public double get_AnzH_Atome() {
	
		return anzH_Atome;
	}

	
	public double get_AnzN_Atome() {
	
		return anzN_Atome;
	}

	
	public double get_AnzO_Atome() {
	
		return anzO_Atome;
	}

	
	public double get_Hu_mol() {
	
		return Hu_mol;
	}

	
	public double get_h_evap_mol() {
	
		return h_evap_mol;
	}
	
	public boolean isGasGemisch(){
		return isGasGemisch;
	}
	
	
//	public boolean isToIntegrate(){
//		return isToIntegrate;
//	}
	
	
	/**
	 * Berechnet zur vorgegebenen inneren Energie u_mass_in die passende Temperatur 
	 * @param u_mass_in --> gewünschte innere Energie [J/kg/K]
	 * @return Tu --> die Temperatur bei der das Gas die angegebene innere Energie u_in hat
	 */
	public double get_T4u_mass(double u_mass_in){
		double Tu=278.15,u_mass,xxx;
		int idx=0;
		//Newtonverfahren für F(T)= u_in - u(T)=0 mit dF/dT=-cv
		do{	
			u_mass=get_u_mass(Tu);
			Tu=Tu+0.75*(u_mass_in-u_mass)/get_cv_mass(Tu);			
			idx++;
			xxx=(u_mass_in-u_mass);
		}while(idx<1000 && Math.abs(xxx) >10e-6);
		if(idx>=999){
			try{
				String s="Die Iteration zur Berechnung der Temperatur passend " +
						"zur vorgegebenene inneren Energie konvergierte nicht. "+
						"Nach: " + idx + " Iterationen wurde die Rechnung abgebrochen";
				throw new MiscException(s) ;
			}catch(MiscException e){
				e.log_Warning();
			}		
		}
		return Tu;		
	}
	
	
	/**
	 * Berechnet zur vorgegebenen Enthalpie h_mass_in die passende Temperatur 
	 * @param h_mass_in --> gewünschte Enthalpie [J/kg/K]
	 * @return Th --> die Temperatur bei der das Gas die angegebene innere Energie h_mass_in hat
	 */
	public double get_T4h_mass(double h_mass_in){
		double Th=278.15,h_mass,xxx;
		int idx=0;
		//Newtonverfahren fuer F(T)= h_in - h(T)=0 mit dF/dT=-cp
		do{	
			h_mass=get_h_mass(Th);
			Th=Th+0.75*(h_mass_in-h_mass)/get_cp_mass(Th);	
			if (Th<0)
				Th=1e-10;
			idx++;
			xxx=(h_mass_in-h_mass);
		}while(idx<1000 && Math.abs(xxx) >10e-6);
		if(idx>=999){
			try{
				String s="Die Iteration zur Berechnung der Temperatur passend " +
						"zur vorgegebenene Enthalpie konvergierte nicht. "+
				"Nach: " + idx + " Iterationen wurde die Rechnung abgebrochen";
				throw new MiscException(s) ;
			}catch(MiscException e){
				System.out.println(Th);
				e.log_Warning();
			}		
		}
		return Th;		
	}
	
	



	public double get_Hu_mass() {		
		return get_Hu_mol()/get_M();
	}
	


}
