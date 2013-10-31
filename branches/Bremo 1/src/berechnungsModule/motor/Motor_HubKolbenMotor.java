package berechnungsModule.motor;

import bremo.parameter.CasePara;


/**
 * @author   hoenl, busch
 */
public class Motor_HubKolbenMotor extends Motor{

	/**
	 * @uml.property  name="Pleuellaenge"
	 */
	private final double PLEUELLAENGE;			//m
	/**
	 * @uml.property  name="bohrung"
	 */
	private final double BOHRUNG ;				//m
	/**
	 * @uml.property  name="hub"
	 */
	private final double HUB ;					//m
	private final double KURBELRADIUS;			//m, nicht gleich 0,5xHub bei einer Desachsierung bzw. einer Schränkung...
	private final double EPS ;  		//-
	
	private final double SCHRAENKUNG ;			//m
	private final double DESACHSIERUNG;			//m	
	private final double AUSLASSSCHLUSS ; 		//°KWnZOT
	private final double AUSLASSOEFFNET ; 		//°KWnZOT
	private final double EINLASSSCHLUSS ; 		//°KWnZOT
	private final double EINLASSOEFFNET ; 		//°KWnZOT
	private double ivStroke=Double.NaN;				//m
	private double pistonArea=Double.NaN; 		//m²
	private double fireDeckArea=Double.NaN; //m²
	private double feuerstegHoehe=Double.NaN;		//m
	private double ivStrokeMax=Double.NaN;			//m
	private double squishHeigth=Double.NaN;;		//m
	private final double SMAX;
	private final CasePara CP;	


	protected Motor_HubKolbenMotor(CasePara cp){
		super(cp);
		CP=super.CP;
		PLEUELLAENGE =CP.get_Pleuellaenge();			//m
		BOHRUNG= CP.get_Bohrung();						//m
		KURBELRADIUS = CP.get_Kurbelradius();		//m
		EPS=CP.get_Verdichtung();  					//-			
		SCHRAENKUNG =CP.get_Schraenkung();			//m
		DESACHSIERUNG =-1*CP.get_Desachsierung();		//m	
		AUSLASSSCHLUSS =CP.get_Auslassoeffnet(); 		//°KWnZOT
		AUSLASSOEFFNET =CP.get_Auslassschluss(); 		//°KWnZOT
		EINLASSSCHLUSS =CP.get_Einlassschluss(); 		//°KWnZOT
		EINLASSOEFFNET =CP.get_Einlassoeffnet(); 		//°KWnZOT	
	
	

		if(SCHRAENKUNG!=0||DESACHSIERUNG!=0){
			//Hub wird aus dem Kolbenweg berechnet
			double sMin=0;
			double toleranz=1E-8; //in °KW
			double a0=0;
			double a1=190;	//10° KW nUT
			int idx=0;
			//Minimum des Abstandes Kolbenbolzne zu KW-Mitte suchen
			while(Math.abs(a0-a1)>Math.PI/180*toleranz){ 
				a0=a1;
				a1=-get_dS_dKW(CP.convert_KW2SEC(a0))/get_d2S_dKW(CP.convert_KW2SEC(a0))+a0;
				idx++;
			}
			if(get_d2S_dKW(CP.convert_KW2SEC(a1))<=0){
				//zweite Ableitung negativ --> Maximum wurde gefunden (sollte nicht passieren können)
				sMin=get_S(CP.convert_KW2SEC(a1));
			}else{
				sMin=get_S(CP.convert_KW2SEC(a1));
			}

			a0=0;
			a1=10;	//10° KW nOT
			//Maximum des Abstandes Kolbenbolzne zu KW-Mitte suchen
			while(Math.abs(a0-a1)>Math.PI/180*toleranz){
				a0=a1;
				a1=-get_dS_dKW(CP.convert_KW2SEC(a0))/get_d2S_dKW(CP.convert_KW2SEC(a0))+a0;
			}
			if(get_d2S_dKW(CP.convert_KW2SEC(a1))<=0){
				//zweite Ableitung negativ --> Maximum wurde gefunden
				SMAX=get_S(CP.convert_KW2SEC(a1));
			}else{
				SMAX=get_S(CP.convert_KW2SEC(a1));
			}
			HUB=SMAX-sMin;	
		}else{
			HUB=2*KURBELRADIUS;
			SMAX=KURBELRADIUS+PLEUELLAENGE;
		}

	}

	///////////////////////////////////////////////////////////////////////
	//Get-Funktionen. Diese sind public, damit jeder beliebige Funktion darauf
	//zugreifen kann
	public double get_Schraenkung() {
		return SCHRAENKUNG;
	}

	public double get_Desachsierung() {
		return DESACHSIERUNG;
	}

	public double get_Kurbelradius() {
		return KURBELRADIUS;
	}

	public double get_Hub() {
		return HUB;
	}

	public double get_Pleuellaenge() {
		return PLEUELLAENGE;
	}

	public double get_Bohrung() {
		return BOHRUNG;
	}

	public double get_Verdichtung() {
		return EPS;
	}

	/**
	 * Surface area of the combustion chamber roof 
	 * respectively fire deck
	 * @return fireDeckArea [m^2]
	 */
	public double get_fireDeckArea() {
		if(Double.isNaN(fireDeckArea))
			fireDeckArea =CP.get_fireDeckArea(); //m^2		
		return fireDeckArea;
	}

	public double get_Feuersteghoehe(){
		if(Double.isNaN(feuerstegHoehe))
			feuerstegHoehe =CP.get_Feuersteghoehe();		//m
		return feuerstegHoehe;
	}

	public double get_Quetschspalthoehe(){
		if(Double.isNaN(squishHeigth))
			squishHeigth=CP.get_Quetschspalthoehe();	//m
		return squishHeigth;
	}

	public double get_Kolbenflaeche() {
		if(Double.isNaN(pistonArea))
			pistonArea =CP.get_pistonArea(); 		//m^2
		return pistonArea;
	}

	public double get_Einlass_oeffnet(){
		return EINLASSOEFFNET;
	}

	public double get_Einlass_schliesst(){
		return EINLASSSCHLUSS;
	}

	public double get_Auslass_oeffnet(){
		return AUSLASSOEFFNET;
	}

	public double get_Auslass_schliesst(){
		return AUSLASSSCHLUSS;
	}

	public double get_IV_Stroke(){
		if(Double.isNaN(ivStroke))
			ivStroke=CP.get_EV_Hub_max();
		return ivStroke;
	}

	public double get_IV_Stroke_max(){
		if(Double.isNaN(ivStrokeMax))
			ivStrokeMax=CP.get_EV_Hub_max();					//m
		return ivStrokeMax;
	}

	//Ab hier müssen die Ausgaben berechnet werden
	public double get_Hubvolumen() {
		double Hubvolumen = HUB * BOHRUNG / 2 * BOHRUNG / 2 *Math.PI; 
		return Hubvolumen;
	}

	public double get_Kompressionsvolumen() {
		double V_c  = get_Hubvolumen() / (EPS - 1);
		return V_c;
	}
	@Override
	public double get_V_MAX() {		
		return this.get_Kompressionsvolumen()+this.get_Hubvolumen();
	}


	/**
	 * @param time in [s n. Rechenbeginn]
	 * @return liefert ausgehend vom oberen Totpunkt den aktuellen Kolbenweg in [m]. 
	 */	
	public double get_Kolbenweg(double time) {		
		double w = 0;	//Kolbenweg
		w=SMAX-this.get_S(time);	
		return w;
	}

	/**
	 * Liefert den Abstand zwischen Kolbenbolzenauge und Kurbelwellenmitte in [m]
	 * @param time [in s n. Rechenbeginn]
	 */
	public double get_S(double time) {
		//Kolbenweg
		//S = r(cos(phi)) + sqrt(L^2-(d+s-r*sin(phi))^2) 
		//[m]
		double phi=convertTime_TO_KW(time)* Math.PI / 180.0;
		double l = PLEUELLAENGE;
		double r = KURBELRADIUS;
		double d = DESACHSIERUNG;
		double s = SCHRAENKUNG;
		double w = 0;	//Kolbenweg
		w=r*Math.cos(phi)+Math.pow(Math.pow(l,2)-Math.pow(d+s+r*Math.sin(phi), 2), 0.5);	
		return w;
	}

	/**
	 * Liefert die momentane Kolbengeschwindigkeit in [m/s]
	 * @param time
	 * @return
	 */	
	public double get_Kolbengeschwindigkeit(double time) {

		//		double kurbelwinkel=convertTime_TO_KW(time);
		//Momentane Kolbengeschwindigkeit = erste Ableitung des Kolbenwegs
		//Ausgabe in m/s
		return convert_dKW_TO_dTime(-1*get_dS_dKW(time));
	}
	/**
	 * Liefert dS/dKW, die erste Ableitung des Abstands Kolbenbolzen zu KW-Mitte, in [m/KW]
	 * @param time
	 * @return
	 */	
	public double get_dS_dKW(double time){				
		double kurbelwinkel=convertTime_TO_KW(time);
		double l = PLEUELLAENGE;
		double r = KURBELRADIUS;
		double d = DESACHSIERUNG;
		double s = SCHRAENKUNG;
		double phi = kurbelwinkel * Math.PI / 180.0;

		//dS = -r*sin(phi)+r*cos(phi)*(d+s-r*sin(phi))/sqrt[L^2-(d+s-r*sin(phi))^2]
		double dS = -r*Math.sin(phi)-r*Math.cos(phi)*(d+s+r*Math.sin(phi))/
				(Math.pow(Math.pow(l,2)-Math.pow(d+s+r*Math.sin(phi),2), 0.5));
		
		return dS*Math.PI/180;
	}
	
	public double get_dS(double time){		
		return convert_dKW_TO_dTime(this.get_dS_dKW(time));
	}
	
	
	
	/**
	 * @param time
	 * @return liefert d2S/dKW, die zweite Ableitung von S, in [m^2/KW^2]
	 */
	public double get_d2S_dKW(double time){
		double kurbelwinkel=convertTime_TO_KW(time);
		double l = PLEUELLAENGE;
		double r = KURBELRADIUS;
		double d = DESACHSIERUNG;
		double s = SCHRAENKUNG;
		double d2S = 0;
		double phi = kurbelwinkel * Math.PI / 180.0;
		
		double wurzel=Math.pow(Math.pow(l,2)-Math.pow(d+s+r*Math.sin(phi),2), 0.5);
		double a= Math.pow((d+s+r*Math.sin(phi))*r*Math.cos(phi),2)/(wurzel) 
						+ wurzel*(r*Math.cos(phi)*r*Math.cos(phi)-(d+s+r*Math.sin(phi))*r*Math.sin(phi));
		d2S=-r*Math.cos(phi)-a/(Math.pow(l,2)-Math.pow(d+s+r*Math.sin(phi),2));
		//Ausgabe in m/°KW^2
		d2S=d2S*Math.pow(Math.PI/180, 2);
		return d2S;
	}
	
	public double get_d2S(double time){		
		return convert_dKW2_TO_dTime2(this.get_d2S_dKW(time));
	}

	/**
	 * @param time
	 * @return liefert das aktuelle Zylindervolumen in [m^3]
	 */	
	public double get_V(double time)
	{
		//Zylindervolumen		 		
		double Vc = get_Kompressionsvolumen();
		double B = BOHRUNG;

		double zylindervolumen = 0;
		// V=Vc + pi*B^2/4 * (L+r-r*cos(phi)-sqrt(L^2-(d+s-r*sin(phi))^2);
		//[m^3]
		zylindervolumen = Vc + Math.PI*Math.pow(B, 2)/4*get_Kolbenweg(time);
		return zylindervolumen;
	}

	public double get_dV_dKW(double time) {
		double B = BOHRUNG;
		double dif_Zylindervolumen = 0;	
		// [m^3/°KW]
		dif_Zylindervolumen = Math.PI*Math.pow(B,2)/4*this.get_dS_dKW(time);
		return -1*dif_Zylindervolumen;
	}

	/**
	 * Liefert die erste Ableitung des Zylindervolumens in [m^3/s]
	 * @param time
	 * @return dV 
	 */	
	public double get_dV(double time){		
		return convert_dKW_TO_dTime(get_dV_dKW(time));		
	}

	public double get_d2V_dKW(double time) {
		double B = BOHRUNG;
		double dif_Zylindervolumen = 0;	
		// [m^3/°KW]
		dif_Zylindervolumen = Math.PI*Math.pow(B,2)/4*this.get_d2S_dKW(time);
		return -1*dif_Zylindervolumen;

	}


	public double get_d2V(double time){		
		return convert_dKW2_TO_dTime2(get_d2V_dKW(time));		
	}

/**
 * 
 */
	/**
	 * Liefert die Oberflaeche des Brennraums OHNE die Flaeche des Feuerstegs.
	 * A = A_ZylKopf + A_Kolben + A_ZylLaufbahn
	 */
	public double get_BrennraumFlaeche(double time) {			
		// A = A_ZK + A_kolben + A_ZLB + 0.25*A_Fst		
		double brennraumFlaeche = 0;
		double zylinderLaufflaeche = java.lang.Math.PI * get_Bohrung() * 
		(get_Kolbenweg(time)+get_Quetschspalthoehe());			

		brennraumFlaeche = get_fireDeckArea() + get_Kolbenflaeche() + 
		zylinderLaufflaeche;
		return brennraumFlaeche;
	}	
	

	/**
	 * Liefert die Flaeche des Feuerstegs in [m^2]
	 * A_Fst = 2 * d * pi * h_Fst
	 * @param time
	 * @param feuerstegMultiplikator
	 * @return
	 */
	public double get_FeuerstegFlaeche() {
		// A_Fst = 2 * d * pi * h_Fst
		double feuerstegFlaeche = 2*java.lang.Math.PI*get_Bohrung()*get_Feuersteghoehe();		
		return feuerstegFlaeche;
	}
	
	
	public boolean isHubKolbenMotor() {		
		return true;
	}

}
